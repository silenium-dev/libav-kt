package dev.silenium.libs.av.core.data

import dev.silenium.libs.av.foreign.NativeEnum
import dev.silenium.libs.av.foreign.parseNativeEnumSet
import org.ffmpeg.bindings.AVOutputFormat
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

class OutputFormat(val value: MemorySegment) {
    val name: String
        get() = AVOutputFormat.name(value).reinterpret(Long.MAX_VALUE).getString(0L)

    val flags: Set<Flag>
        get() = AVOutputFormat.flags(value).let(::parseNativeEnumSet)

    enum class Flag(override val value: Int) : NativeEnum {
        GENERIC_INDEX(FFMPEG.AVFMT_GENERIC_INDEX()),
        GLOBALHEADER(FFMPEG.AVFMT_GLOBALHEADER()),
        NEEDNUMBER(FFMPEG.AVFMT_NEEDNUMBER()),
        NOBINSEARCH(FFMPEG.AVFMT_NOBINSEARCH()),
        NODIMENSIONS(FFMPEG.AVFMT_NODIMENSIONS()),
        NOFILE(FFMPEG.AVFMT_NOFILE()),
        NOGENSEARCH(FFMPEG.AVFMT_NOGENSEARCH()),
        NOSTREAMS(FFMPEG.AVFMT_NOSTREAMS()),
        NOTIMESTAMPS(FFMPEG.AVFMT_NOTIMESTAMPS()),
        NO_BYTE_SEEK(FFMPEG.AVFMT_NO_BYTE_SEEK()),
        SEEK_TO_PTS(FFMPEG.AVFMT_SEEK_TO_PTS()),
        SHOW_IDS(FFMPEG.AVFMT_SHOW_IDS()),
        TBCF_AUTO(FFMPEG.AVFMT_TBCF_AUTO()),
        TBCF_DECODER(FFMPEG.AVFMT_TBCF_DECODER()),
        TBCF_DEMUXER(FFMPEG.AVFMT_TBCF_DEMUXER()),
        TBCF_R_FRAMERATE(FFMPEG.AVFMT_TBCF_R_FRAMERATE()),
        TS_DISCONT(FFMPEG.AVFMT_TS_DISCONT()),
        TS_NEGATIVE(FFMPEG.AVFMT_TS_NEGATIVE()),
        TS_NONSTRICT(FFMPEG.AVFMT_TS_NONSTRICT()),
        VARIABLE_FPS(FFMPEG.AVFMT_VARIABLE_FPS()),
    }

    companion object {
        fun guess(shortName: String? = null, fileName: String? = null, mimeType: String? = null) = Arena.ofConfined().use { arena ->
            val result = FFMPEG.av_guess_format(
                shortName?.let(arena::allocateFrom) ?: MemorySegment.NULL,
                fileName?.let(arena::allocateFrom) ?: MemorySegment.NULL,
                mimeType?.let(arena::allocateFrom) ?: MemorySegment.NULL,
            )
            if (result == MemorySegment.NULL) return@use null
            OutputFormat(result)
        }
    }
}
