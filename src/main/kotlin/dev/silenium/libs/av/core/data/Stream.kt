package dev.silenium.libs.av.core.data

import dev.silenium.libs.av.core.Discard
import dev.silenium.libs.av.core.checkAV
import dev.silenium.libs.av.foreign.NativeEnum
import dev.silenium.libs.av.foreign.asNative
import dev.silenium.libs.av.foreign.parseNativeEnum
import dev.silenium.libs.av.foreign.parseNativeEnumSet
import org.ffmpeg.bindings.AVStream
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.MemorySegment

class Stream(val value: MemorySegment) {
    var index: Int
        get() = AVStream.index(value)
        set(value) = AVStream.index(this.value, value)
    var id: Int
        get() = AVStream.id(value)
        set(value) = AVStream.id(this.value, value)
    var codecParams: CodecParameters
        get() = CodecParameters(AVStream.codecpar(value))
        set(value) = FFMPEG.avcodec_parameters_copy(AVStream.codecpar(this.value), value.value)
            .checkAV("avcodec_parameters_copy")

    var timeBase: Rational
        get() = Rational(AVStream.time_base(value))
        set(value) = AVStream.time_base(this.value, value.value)
    var startTime: Long
        get() = AVStream.start_time(value)
        set(value) = AVStream.start_time(this.value, value)
    var duration: Long
        get() = AVStream.duration(value)
        set(value) = AVStream.duration(this.value, value)
    var nbFrames: Long
        get() = AVStream.nb_frames(value)
        set(value) = AVStream.nb_frames(this.value, value)

    var disposition: Int
        get() = AVStream.disposition(value)
        set(value) = AVStream.disposition(this.value, value)
    var discard: Discard
        get() = AVStream.discard(value).let(::parseNativeEnum)
        set(value) = AVStream.discard(this.value, value.value)
    var sampleAspectRatio: Rational
        get() = Rational(AVStream.sample_aspect_ratio(value))
        set(value) = AVStream.sample_aspect_ratio(this.value, value.value)

    var metadata: Dictionary
        get() = AVStream.metadata(value).let(::Dictionary)
        set(value) = AVStream.metadata(this.value, value.value)
    var avgFrameRate: Rational
        get() = Rational(AVStream.avg_frame_rate(value))
        set(value) = AVStream.avg_frame_rate(this.value, value.value)

    var attachedPic: Packet
        get() = Packet.ofUnowned(AVStream.attached_pic(value))
        set(value) = AVStream.attached_pic(this.value, value.value)
    var eventFlags: Set<EventFlag>
        get() = AVStream.event_flags(value).let(::parseNativeEnumSet)
        set(value) = AVStream.event_flags(this.value, value.asNative())

    enum class EventFlag(override val value: Int) : NativeEnum {
        METADATA_UPDATED(FFMPEG.AVSTREAM_EVENT_FLAG_METADATA_UPDATED()),
        NEW_PACKETS(FFMPEG.AVSTREAM_EVENT_FLAG_NEW_PACKETS()),
    }
}
