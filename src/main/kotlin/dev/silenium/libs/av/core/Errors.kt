package dev.silenium.libs.av.core

import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena

fun Int.checkAV(operation: String): Unit = Arena.ofConfined().use { arena ->
    if (this < 0) {
        val str = arena.allocate(FFMPEG.AV_ERROR_MAX_STRING_SIZE().toLong())
        FFMPEG.av_strerror(this, str, FFMPEG.AV_ERROR_MAX_STRING_SIZE().toLong())
        error("Failed to $operation: ${str.getString(0)}")
    }
}