package dev.silenium.libs.av.core.context

import dev.silenium.libs.av.core.AVException
import dev.silenium.libs.av.core.av
import dev.silenium.libs.av.core.data.Codec
import dev.silenium.libs.av.core.data.CodecParameters
import dev.silenium.libs.av.foreign.DoubleDestructionProtection
import dev.silenium.libs.av.foreign.pointerTo
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

class CodecContext private constructor(override val value: MemorySegment, val arena: Arena = Arena.ofAuto()) :
    DoubleDestructionProtection<MemorySegment>() {
    override fun destroyInternal() {
        FFMPEG.avcodec_free_context(arena.pointerTo(value))
    }

    companion object {
        operator fun invoke(codec: Codec, params: CodecParameters): Result<CodecContext> {
            val context = FFMPEG.avcodec_alloc_context3(codec.value)
                ?: return Result.failure(AVException(-0x0c /* AVERROR(ENOMEM) */, "avcodec_alloc_context3"))
            return Result.av(
                FFMPEG.avcodec_parameters_to_context(context, params.value),
                "avcodec_parameters_to_context",
            ) {
                CodecContext(context)
            }
        }
    }
}
