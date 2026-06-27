package dev.silenium.libs.av.core

import dev.silenium.libs.av.foreign.NativeEnum
import org.ffmpeg.bindings.FFMPEG

enum class IOFlag(override val value: Int) : NativeEnum {
    DIRECT(FFMPEG.AVIO_FLAG_DIRECT()),
    NONBLOCK(FFMPEG.AVIO_FLAG_NONBLOCK()),
    READ(FFMPEG.AVIO_FLAG_READ()),
    READ_WRITE(FFMPEG.AVIO_FLAG_READ_WRITE()),
    WRITE(FFMPEG.AVIO_FLAG_WRITE()),
}
