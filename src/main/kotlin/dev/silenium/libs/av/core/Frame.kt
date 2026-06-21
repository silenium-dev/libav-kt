package dev.silenium.libs.av.core

import dev.silenium.libs.av.foreign.DoubleDestructionProtection
import dev.silenium.libs.av.foreign.asIntArray
import dev.silenium.libs.av.foreign.asPointerArray
import dev.silenium.libs.av.foreign.parseNativeEnum
import dev.silenium.libs.av.foreign.pointerTo
import org.ffmpeg.bindings.AVFrame
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

sealed class Frame(override val value: MemorySegment) : DoubleDestructionProtection<MemorySegment>() {
    init {
        require(value != MemorySegment.NULL) { "Frame cannot be null" }
    }

    val width get() = AVFrame.width(value)
    val height get() = AVFrame.height(value)
    val format get() = parseNativeEnum<PixelFormat>(AVFrame.format(value))
    val linesize: Array<Int>
        get() = AVFrame.linesize(value).asIntArray(8)

    data class Simple(override val value: MemorySegment) : Frame(value) {
        val data: Array<MemorySegment>
            get() = AVFrame.data(value).asPointerArray(8)
    }

    data class VAAPI(override val value: MemorySegment) : Frame(value) {
        val surfaceId: Long
            get() = AVFrame.data(value).getAtIndex(ValueLayout.ADDRESS, 3).address()
    }

    override fun destroyInternal() = Arena.ofConfined().use {
        FFMPEG.av_frame_free(it.pointerTo(value))
    }
}
