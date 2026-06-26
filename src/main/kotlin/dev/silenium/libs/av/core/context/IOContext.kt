package dev.silenium.libs.av.core.context

import dev.silenium.libs.av.foreign.DoubleDestructionProtection
import dev.silenium.libs.av.foreign.pointerTo
import dev.silenium.libs.av.foreign.upcallStub
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.*

sealed class IOContext : AutoCloseable, DoubleDestructionProtection<MemorySegment>() {
    class Custom private constructor(
        override val value: MemorySegment,
        val callback: Callback,
        private val arena: Arena
    ) : IOContext() {
        interface Callback : AutoCloseable {
            fun read(ptr: MemorySegment): Int
            fun seek(offset: Long, whence: Int): Long
        }

        interface WritableCallback : Callback {
            fun write(ptr: MemorySegment): Int
        }

        private class CallbackWrapper(val callback: Callback) {
            fun read(unused: MemorySegment, buffer: MemorySegment, size: Int): Int =
                callback.read(buffer.reinterpret(size.toLong()))

            fun write(unused: MemorySegment, buffer: MemorySegment, size: Int): Int =
                if (callback is WritableCallback) {
                    callback.write(buffer.reinterpret(size.toLong()))
                } else {
                    0
                }

            fun seek(unused: MemorySegment, offset: Long, whence: Int): Long =
                callback.seek(offset, whence)
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
                    CallbackWrapper::seek.upcallStub(wrapper, linker, SEEK_SIGNATURE, arena),
                    CallbackWrapper::write.upcallStub(wrapper, linker, WRITE_SIGNATURE, arena),
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
                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT
            )
        }
    }

    class Default(override val value: MemorySegment) : IOContext() {
        override fun destroyInternal() {
            FFMPEG.avio_close(value)
        }
    }
}
