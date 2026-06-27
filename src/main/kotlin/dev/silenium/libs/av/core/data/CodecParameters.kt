package dev.silenium.libs.av.core.data

import dev.silenium.libs.av.foreign.DoubleDestructionProtection
import dev.silenium.libs.av.foreign.parseNativeEnum
import dev.silenium.libs.av.foreign.pointerTo
import org.ffmpeg.bindings.AVCodecParameters
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

class CodecParameters(override val value: MemorySegment, private val arena: Arena? = null) :
    DoubleDestructionProtection<MemorySegment>() {
    constructor(value: MemorySegment) : this(value, null)

    var codecId: Codec.ID
        get() = AVCodecParameters.codec_id(value).let(::parseNativeEnum)
        set(value) = AVCodecParameters.codec_id(this.value, value.value)

    override fun destroyInternal() {
        if (arena != null) {
            FFMPEG.avcodec_parameters_free(arena.pointerTo(value))
        }
    }
}
