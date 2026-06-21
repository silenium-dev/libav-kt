package dev.silenium.libs.av.core

import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena

class AVException(ret: Int, operation: String) : Exception("Failed to $operation: ${ret.avErrorMessage()}")

fun Int.avErrorMessage(): String = Arena.ofConfined().use { arena ->
    val str = arena.allocate(FFMPEG.AV_ERROR_MAX_STRING_SIZE().toLong())
    FFMPEG.av_strerror(this, str, FFMPEG.AV_ERROR_MAX_STRING_SIZE().toLong())
    str.getString(0)
}

fun Int.checkAV(operation: String) {
    if (this < 0) {
        throw AVException(this, operation)
    }
}

fun <T> Result.Companion.av(ret: Int, operation: String, block: () -> T): Result<T> = when (ret) {
    0 -> Result.success(block())
    else -> Result.failure(AVException(ret, operation))
}
