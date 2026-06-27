package dev.silenium.libs.av.core.data

import dev.silenium.libs.av.foreign.DoubleDestructionProtection
import dev.silenium.libs.av.foreign.pointerTo
import org.ffmpeg.bindings.AVBufferRef
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

data class BufferRef(override val value: MemorySegment) : DoubleDestructionProtection<MemorySegment>() {
    val data: MemorySegment
        get() = AVBufferRef.data(value).reinterpret(AVBufferRef.size(value))

    override fun toString() = "BufferRef(data=0x%x, size=%d)".format(data.address(), data.byteSize())

    override fun destroyInternal() = Arena.ofConfined().use { arena ->
        FFMPEG.av_buffer_unref(arena.pointerTo(value))
    }

    companion object {
        fun of(buf: MemorySegment) = if (buf != MemorySegment.NULL) {
            BufferRef(buf.reinterpret(AVBufferRef.sizeof()))
        } else {
            null
        }

        fun createPtrPtr(layout: MemoryLayout, block: (Arena, MemorySegment) -> Unit): Result<BufferRef> =
            Arena.ofConfined().use { arena ->
                val ptr = arena.allocate(ValueLayout.ADDRESS)
                runCatching {
                    block(arena, ptr)
                    BufferRef(ptr.get(ValueLayout.ADDRESS, 0).reinterpret(layout.byteSize()))
                }
            }
    }
}
