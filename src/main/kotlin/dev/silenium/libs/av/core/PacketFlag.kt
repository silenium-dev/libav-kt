package dev.silenium.libs.av.core

import dev.silenium.libs.av.foreign.NativeEnum
import org.ffmpeg.bindings.FFMPEG

enum class PacketFlag(override val value: Int) : NativeEnum {
    CORRUPT(FFMPEG.AV_PKT_FLAG_CORRUPT()),
    DISCARD(FFMPEG.AV_PKT_FLAG_DISCARD()),
    DISPOSABLE(FFMPEG.AV_PKT_FLAG_DISPOSABLE()),
    KEY(FFMPEG.AV_PKT_FLAG_KEY()),
    TRUSTED(FFMPEG.AV_PKT_FLAG_TRUSTED()),
}
