package dev.silenium.libs.av.core.context

import dev.silenium.libs.av.foreign.*
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.*
import kotlin.math.min

sealed class IOContext : AutoCloseable, DoubleDestructionProtection<MemorySegment>() {
    class Custom private constructor(
        override val value: MemorySegment,
        val callback: Callback,
        private val arena: Arena
    ) : IOContext() {
        enum class SeekOrigin(override val value: Int) : NativeEnum {
            SET(0),
            CUR(1),
            END(2),
        }

        enum class SeekFlag(override val value: Int) : NativeEnum {
            AV_SIZE(FFMPEG.AVSEEK_SIZE()),
            AV_FORCE(FFMPEG.AVSEEK_FORCE()),
        }

        @JvmInline
        value class Whence(val value: Int) {
            constructor(origin: SeekOrigin, flags: Set<SeekFlag> = emptySet()) : this(origin.value or flags.asNative())
            val origin: SeekOrigin get() = parseNativeEnum(value and 0xFFFF)
            val flags: Set<SeekFlag> get() = parseNativeEnumSet<SeekFlag>(value)

            override fun toString(): String {
                return buildString {
                    append(origin)
                    if (flags.isNotEmpty()) {
                        append(' ')
                        append(flags.joinToString())
                    }
                }
            }
        }

        interface Callback : AutoCloseable {
            fun read(ptr: MemorySegment): Int
            fun seek(offset: Long, whence: Whence): Long
        }

        interface WritableCallback : Callback {
            fun write(ptr: MemorySegment): Int
        }

        internal class CallbackWrapper(val callback: Callback) {
            fun read(unused: MemorySegment, buffer: MemorySegment, size: Int): Int =
                callback.read(buffer.reinterpret(size.toLong()))

            fun write(unused: MemorySegment, buffer: MemorySegment, size: Int): Int =
                if (callback is WritableCallback) {
                    callback.write(buffer.reinterpret(size.toLong()))
                } else {
                    0
                }

            fun seek(unused: MemorySegment, offset: Long, whence: Int): Long =
                callback.seek(offset, Whence(whence))
        }

        override fun destroyInternal() = Arena.ofConfined().use { arena ->
            FFMPEG.avio_context_free(arena.pointerTo(value))
            callback.close()
        }

        companion object {
            private val linker = Linker.nativeLinker()
            operator fun invoke(callback: Callback): Custom {
                val arena = Arena.ofAuto()
                val wrapper = CallbackWrapper(callback)
                val ctx = FFMPEG.avio_alloc_context(
                    arena.allocate(BUFFER_SIZE.toLong()),
                    BUFFER_SIZE,
                    if (callback is WritableCallback) 1 else 0,
                    MemorySegment.NULL,
                    CallbackWrapper::read.upcallStub(wrapper, linker, READ_SIGNATURE, arena),
                    CallbackWrapper::write.upcallStub(wrapper, linker, WRITE_SIGNATURE, arena),
                    CallbackWrapper::seek.upcallStub(wrapper, linker, SEEK_SIGNATURE, arena),
                )
                return Custom(ctx, callback, arena)
            }

            private const val BUFFER_SIZE = 1024 * 1024
            private val READ_SIGNATURE = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
            )
            private val SEEK_SIGNATURE = FunctionDescriptor.of(
                ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT
            )
            private val WRITE_SIGNATURE = FunctionDescriptor.of(
                ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
            )
        }
    }

    class Default(override val value: MemorySegment) : IOContext() {
        override fun destroyInternal(): Unit = Arena.ofConfined().use { arena ->
            FFMPEG.avio_closep(arena.pointerTo(value))
        }
    }
}

class ClasspathIOCallback(path: String) : IOContext.Custom.Callback {
    private val resource =
        javaClass.classLoader.getResource(path) ?: throw IllegalArgumentException("Resource not found: $path")
    private var stream = resource.openStream().apply {
        mark(Int.MAX_VALUE)
    }
    private val size = resource.openStream().use { stream ->
        var size = 0L
        val buf = ByteArray(4 * 1024 * 1024)
        while (true) {
            val read = stream.read(buf)
            if (read <= 0) break
            size += read
        }
        size
    }
    private var position = 0L

    override fun read(ptr: MemorySegment): Int {
        val buffer = ByteArray(ptr.byteSize().toInt())
        val read = stream.read(buffer)
        if (read == -1) return FFMPEG.AVERROR_EOF()
        position += read
        MemorySegment.copy(MemorySegment.ofArray(buffer), 0L, ptr, 0L, read.toLong())
        return read
    }

    override fun seek(
        offset: Long,
        whence: IOContext.Custom.Whence
    ): Long {
        if (IOContext.Custom.SeekFlag.AV_SIZE in whence.flags) return size
        val target = when (whence.origin) {
            IOContext.Custom.SeekOrigin.SET -> offset
            IOContext.Custom.SeekOrigin.CUR -> position + offset
            IOContext.Custom.SeekOrigin.END -> size + offset
        }
        if (target !in 0..size) return -1
        if (target < position) {
            try {
                stream.reset()
            } catch (e: Exception) {
                e.printStackTrace()
                return -1
            }
            position = 0
        }
        val buf = ByteArray(4 * 1024 * 1024)
        while (position < target) {
            val read = stream.read(buf, 0, min(target - position, buf.size.toLong()).toInt())
            if (read == -1) return -1
            position += read
        }
        return position
    }

    override fun close() = stream.close()
}
