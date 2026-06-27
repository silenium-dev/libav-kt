package dev.silenium.libs.av.core

import dev.silenium.libs.av.foreign.NativeEnum
import org.ffmpeg.bindings.FFMPEG

enum class Discard(override val value: Int) : NativeEnum {
    NONE(FFMPEG.AVDISCARD_NONE()),
    DEFAULT(FFMPEG.AVDISCARD_DEFAULT()),
    NONREF(FFMPEG.AVDISCARD_NONREF()),
    BIDIR(FFMPEG.AVDISCARD_BIDIR()),
    NONINTRA(FFMPEG.AVDISCARD_NONINTRA()),
    NONKEY(FFMPEG.AVDISCARD_NONKEY()),
    ALL(FFMPEG.AVDISCARD_ALL()),
}
