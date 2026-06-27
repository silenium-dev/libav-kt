package dev.silenium.libs.av.core.data

import dev.silenium.libs.av.core.*
import dev.silenium.libs.av.foreign.*
import org.ffmpeg.bindings.AVFrame
import org.ffmpeg.bindings.AVFrameSideData
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

sealed class NativeFrame : DoubleDestructionProtection<MemorySegment>(), Frame {
    protected val arena: Arena = Arena.ofAuto()

    override var linesize: List<Int>
        get() = AVFrame.linesize(value)
            .asIntArray(AVFrame.`linesize$layout`().elementCount().toInt())
        set(value) = AVFrame.linesize(this.value, value.asNativeArray(arena))

    override var data: List<MemorySegment?>
        get() = AVFrame.data(value)
            .asPointerArray(AVFrame.`data$layout`().elementCount().toInt())
        set(value) = AVFrame.data(this.value, value.asNativeArray(arena) { it })
    override var buf: List<BufferRef?>
        get() = AVFrame.buf(value)
            .asPointerArray(AVFrame.`buf$layout`().elementCount().toInt(), BufferRef::of)
        set(value) = AVFrame.buf(this.value, value.asNativeArray(arena) { it?.value })

    override var extendedBuf: List<BufferRef?>
        get() = AVFrame.extended_buf(value)
            .asPointerArray(AVFrame.nb_extended_buf(value), BufferRef::of)
        set(value) = AVFrame.extended_buf(this.value, value.asNativeArray(arena) { it?.value })

    override var extendedData: List<MemorySegment?>
        get() = AVFrame.extended_data(value)
            .asPointerArray(AVFrame.nb_extended_buf(value))
        set(value) = AVFrame.extended_data(this.value, value.asNativeArray(arena))

    override var sideData: List<FrameSideData>
        get() = AVFrame.side_data(value)
            .asPointerArray(AVFrame.nb_side_data(value)) {
                FrameSideData(it.reinterpret(AVFrameSideData.sizeof()))
            }
        set(value) = AVFrame.side_data(this.value, value.asNativeArray(arena, FrameSideData::value))

    override var flags: Set<Frame.Flags>
        get() = AVFrame.flags(value)
            .let(::parseNativeEnumSet)
        set(value) = AVFrame.flags(this.value, value.asNative())

    override var timeBase: Rational
        get() = Rational(AVFrame.time_base(value))
        set(value) = AVFrame.time_base(this.value, value.value)
    override var duration: Long
        get() = AVFrame.duration(value)
        set(value) = AVFrame.duration(this.value, value)
    override var pts: Long
        get() = AVFrame.pts(value)
        set(value) = AVFrame.pts(this.value, value)
    override var pktDts: Long
        get() = AVFrame.pkt_dts(value)
        set(value) = AVFrame.pkt_dts(this.value, value)
    override var bestEffortTimestamp: Long
        get() = AVFrame.best_effort_timestamp(value)
        set(value) = AVFrame.best_effort_timestamp(this.value, value)

    override var quality: Int
        get() = AVFrame.quality(value)
        set(value) = AVFrame.quality(this.value, value)

    override var opaque: MemorySegment
        get() = AVFrame.opaque(value)
        set(value) = AVFrame.opaque(this.value, value)
    override var metadata: Dictionary
        get() = Dictionary(AVFrame.metadata(value))
        set(value) = AVFrame.metadata(this.value, value.value)
    override var privateRef: MemorySegment
        get() = AVFrame.private_ref(value)
        set(value) = AVFrame.private_ref(this.value, value)
    override var decodeErrorFlags: Set<Frame.DecodeErrorFlags>
        get() = AVFrame.decode_error_flags(value)
            .let(::parseNativeEnumSet)
        set(value) = AVFrame.decode_error_flags(this.value, value.asNative())

    override var hwFramesCtx: BufferRef?
        get() = AVFrame.hw_frames_ctx(value).let(BufferRef::of)
        set(value) = AVFrame.hw_frames_ctx(this.value, value?.value ?: MemorySegment.NULL)
    override var opaqueRef: BufferRef?
        get() = AVFrame.opaque_ref(value).let(BufferRef::of)
        set(value) = AVFrame.opaque_ref(this.value, value?.value ?: MemorySegment.NULL)

    override fun destroyInternal() = Arena.ofConfined().use {
        FFMPEG.av_frame_free(it.pointerTo(value))
    }

    data class Video(override val value: MemorySegment) : NativeFrame(), Frame.Video {
        override var width: Int
            get() = AVFrame.width(value)
            set(value) = AVFrame.width(this.value, value)
        override var height: Int
            get() = AVFrame.height(value)
            set(value) = AVFrame.height(this.value, value)
        override var format: PixelFormat
            get() = parseNativeEnum(AVFrame.format(value))
            set(value) = AVFrame.format(this.value, value.value)
        override var pictureType: PictureType
            get() = parseNativeEnum(AVFrame.pict_type(value))
            set(value) = AVFrame.pict_type(this.value, value.value)
        override var sampleAspectRatio: Rational
            get() = Rational(AVFrame.sample_aspect_ratio(value))
            set(value) = AVFrame.sample_aspect_ratio(this.value, value.value)
        override var repeatPict: Int
            get() = AVFrame.repeat_pict(value)
            set(value) = AVFrame.repeat_pict(this.value, value)
        override var crop: Crop
            get() = Crop.from(value)
            set(value) = value.apply(this.value)

        override var colorRange: ColorRange
            get() = parseNativeEnum(AVFrame.color_range(value))
            set(value) = AVFrame.color_range(this.value, value.value)
        override var colorPrimaries: ColorPrimaries
            get() = parseNativeEnum(AVFrame.color_primaries(value))
            set(value) = AVFrame.color_primaries(this.value, value.value)
        override var colorTrc: ColorTransferCharacteristics
            get() = parseNativeEnum(AVFrame.color_trc(value))
            set(value) = AVFrame.color_trc(this.value, value.value)
        override var colorSpace: ColorSpace
            get() = parseNativeEnum(AVFrame.colorspace(value))
            set(value) = AVFrame.colorspace(this.value, value.value)
        override var chromaLocation: ChromaLocation
            get() = parseNativeEnum(AVFrame.chroma_location(value))
            set(value) = AVFrame.chroma_location(this.value, value.value)

        override var alphaMode: AlphaMode
            get() = parseNativeEnum(AVFrame.alpha_mode(value))
            set(value) = AVFrame.alpha_mode(this.value, value.value)

        companion object {
            fun allocate(): Video = FFMPEG.av_frame_alloc().reinterpret(AVFrame.sizeof()).let(::Video)
        }
    }

    data class Audio(override val value: MemorySegment) : NativeFrame(), Frame.Audio {
        override var channelLayout: ChannelLayout
            get() = ChannelLayout(AVFrame.ch_layout(value))
            set(value) = AVFrame.ch_layout(this.value, value.value)
        override var format: SampleFormat
            get() = parseNativeEnum(AVFrame.format(value))
            set(value) = AVFrame.format(this.value, value.value)
        override var sampleRate: Int
            get() = AVFrame.sample_rate(value)
            set(value) = AVFrame.sample_rate(this.value, value)
        override var samples: Int
            get() = AVFrame.nb_samples(value)
            set(value) = AVFrame.nb_samples(this.value, value)

        companion object {
            fun allocate(): Audio = FFMPEG.av_frame_alloc().reinterpret(AVFrame.sizeof()).let(::Audio)
        }
    }
}
