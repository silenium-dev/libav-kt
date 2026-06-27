package dev.silenium.libs.av.core.data

import org.ffmpeg.bindings.AVInputFormat
import java.lang.foreign.MemorySegment

class InputFormat(val value: MemorySegment) {
    val name: String
        get() = AVInputFormat.name(value).reinterpret(Long.MAX_VALUE).getString(0L)
}
