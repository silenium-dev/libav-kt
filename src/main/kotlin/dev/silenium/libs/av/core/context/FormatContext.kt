package dev.silenium.libs.av.core.context

import dev.silenium.libs.av.core.IOFlag
import dev.silenium.libs.av.core.av
import dev.silenium.libs.av.core.avResult
import dev.silenium.libs.av.core.checkAV
import dev.silenium.libs.av.core.data.*
import dev.silenium.libs.av.foreign.*
import org.ffmpeg.bindings.AVFormatContext
import org.ffmpeg.bindings.AVStream
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.nio.ByteBuffer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

sealed class FormatContext : AutoCloseable, DoubleDestructionProtection<MemorySegment>() {
    abstract val arena: Arena

    abstract val ioCtx: IOContext?

    val ctxFlags: Set<ContextFlags>
        get() = AVFormatContext.ctx_flags(value).let(::parseNativeEnumSet)
    val streams: List<Stream>
        get() = AVFormatContext.streams(value)
            .asPointerArray(AVFormatContext.nb_streams(value)) {
                Stream(it.reinterpret(AVStream.sizeof()))
            }

    //    val streamGroups: List<StreamGroup> // TODO
//    val chapters: List<Chapter> // TODO
    val url: String?
        get() = AVFormatContext.url(value)
            .takeIf { it != MemorySegment.NULL }
            ?.reinterpret(Long.MAX_VALUE)
            ?.getString(0L)
    val startTime: Long
        get() = AVFormatContext.start_time(value)
    val duration: Long
        get() = AVFormatContext.duration(value)
    val bitRate: Long
        get() = AVFormatContext.bit_rate(value)
    val packetSize: Int
        get() = AVFormatContext.packet_size(value)
    val maxDelay: Int
        get() = AVFormatContext.max_delay(value)
    var flags: Set<Flags>
        get() = AVFormatContext.flags(value).let(::parseNativeEnumSet)
        set(value) = AVFormatContext.flags(this.value, value.asNative())
    var probesize: Long
        get() = AVFormatContext.probesize(value)
        set(value) = AVFormatContext.probesize(this.value, value)
    var maxAnalyzeDuration: Long
        get() = AVFormatContext.max_analyze_duration(value)
        set(value) = AVFormatContext.max_analyze_duration(this.value, value)
    var key: ByteBuffer
        get() = AVFormatContext.key(value).asByteBuffer()
        set(value) {
            val segment = arena.allocate(value.limit().toLong())
            segment.copyFrom(MemorySegment.ofBuffer(value))
            AVFormatContext.key(this.value, segment)
            AVFormatContext.keylen(this.value, segment.byteSize().toInt())
        }

    //    val programs: List<Program> // TODO
    var metadata: Dictionary
        get() = Dictionary(AVFormatContext.metadata(value))
        set(value) = AVFormatContext.metadata(this.value, value.value)
    var startTimeRealtime: Duration
        get() = AVFormatContext.start_time_realtime(value).microseconds
        set(value) = AVFormatContext.start_time_realtime(this.value, value.inWholeMicroseconds)

    //        var interruptCallback: () -> Int // TODO
    var debug: Set<DebugFlag>
        get() = AVFormatContext.debug(value).let(::parseNativeEnumSet)
        set(value) = AVFormatContext.debug(this.value, value.asNative())

    var strictStdCompliance: Int
        get() = AVFormatContext.strict_std_compliance(value)
        set(value) = AVFormatContext.strict_std_compliance(this.value, value)

    var eventFlags: Set<EventFlag>
        get() = AVFormatContext.event_flags(value).let(::parseNativeEnumSet)
        set(value) = AVFormatContext.event_flags(this.value, value.asNative())
    var ioRepositioned: Boolean
        get() = AVFormatContext.io_repositioned(value) != 0
        set(value) = AVFormatContext.io_repositioned(this.value, if (value) 1 else 0)
//    var controlMessageCb: (FormatContext, Int, ByteBuffer) -> Int // TODO

    var dumpSeparator: String
        get() = AVFormatContext.dump_separator(value).reinterpret(Long.MAX_VALUE).getString(0L)
        set(value) = AVFormatContext.dump_separator(this.value, arena.allocateFrom(value))

    //    var ioOpen2: (FormatContext, Pointer<IOContext>, String, Int, Dictionary) -> Int // TODO
//    var ioClose: (FormatContext, IOContext) -> Int // TODO
    val name: String
        get() = AVFormatContext.name(value).reinterpret(Long.MAX_VALUE).getString(0L)

    class Input(
        override val value: MemorySegment,
        override val ioCtx: IOContext? = null,
        override val arena: Arena = Arena.ofAuto(),
    ) : FormatContext() {
        val iFormat: InputFormat
            get() = InputFormat(AVFormatContext.iformat(value))

        fun findStreamInfo() = FFMPEG.avformat_find_stream_info(value, MemorySegment.NULL)
            .avResult("avformat_find_stream_info")

        var videoCodecId: Codec.ID
            get() = AVFormatContext.video_codec_id(value).let(::parseNativeEnum)
            set(value) = AVFormatContext.video_codec_id(this.value, value.value)
        var audioCodecId: Codec.ID
            get() = AVFormatContext.audio_codec_id(value).let(::parseNativeEnum)
            set(value) = AVFormatContext.audio_codec_id(this.value, value.value)
        var subtitleCodecId: Codec.ID
            get() = AVFormatContext.subtitle_codec_id(value).let(::parseNativeEnum)
            set(value) = AVFormatContext.subtitle_codec_id(this.value, value.value)
        var dataCodecId: Codec.ID
            get() = AVFormatContext.data_codec_id(value).let(::parseNativeEnum)
            set(value) = AVFormatContext.data_codec_id(this.value, value.value)
        var fpsProbeSize: Int
            get() = AVFormatContext.fps_probe_size(value)
            set(value) = AVFormatContext.fps_probe_size(this.value, value)
        var errorRecognition: Int
            get() = AVFormatContext.error_recognition(value)
            set(value) = AVFormatContext.error_recognition(this.value, value)
        var maxStreams: Int
            get() = AVFormatContext.max_streams(value)
            set(value) = AVFormatContext.max_streams(this.value, value)
        var maxIndexSize: UInt
            get() = AVFormatContext.max_index_size(value).toUInt()
            set(value) = AVFormatContext.max_index_size(this.value, value.toInt())
        var maxPictureBuffer: UInt
            get() = AVFormatContext.max_picture_buffer(value).toUInt()
            set(value) = AVFormatContext.max_picture_buffer(this.value, value.toInt())
        var maxTsProbe: Int
            get() = AVFormatContext.max_ts_probe(value)
            set(value) = AVFormatContext.max_ts_probe(this.value, value)
        var maxProbePackets: Int
            get() = AVFormatContext.max_probe_packets(value)
            set(value) = AVFormatContext.max_probe_packets(this.value, value)
        var useWallclockAsTimestamps: Boolean
            get() = AVFormatContext.use_wallclock_as_timestamps(value) != 0
            set(value) = AVFormatContext.use_wallclock_as_timestamps(this.value, if (value) 1 else 0)
        var skipEstimateDurationFromPts: Boolean
            get() = AVFormatContext.skip_estimate_duration_from_pts(value) != 0
            set(value) = AVFormatContext.skip_estimate_duration_from_pts(this.value, if (value) 1 else 0)
        var avioFlags: Set<IOFlag>
            get() = AVFormatContext.avio_flags(value).let(::parseNativeEnumSet)
            set(value) = AVFormatContext.avio_flags(this.value, value.asNative())
        val durationEstimationMethod: DurationEstimationMethod
            get() = AVFormatContext.duration_estimation_method(value).let(::parseNativeEnum)
        var skipInitialBytes: Long
            get() = AVFormatContext.skip_initial_bytes(value)
            set(value) = AVFormatContext.skip_initial_bytes(this.value, value)
        var correctTsOverflow: Boolean
            get() = AVFormatContext.correct_ts_overflow(value) != 0
            set(value) = AVFormatContext.correct_ts_overflow(this.value, if (value) 1 else 0)
        var seek2Any: Boolean
            get() = AVFormatContext.seek2any(value) != 0
            set(value) = AVFormatContext.seek2any(this.value, if (value) 1 else 0)
        val probeScore: Int
            get() = AVFormatContext.probe_score(value)
        var formatProbesize: Int
            get() = AVFormatContext.format_probesize(value)
            set(value) = AVFormatContext.format_probesize(this.value, value)
        var codecWhitelist: Set<String>
            get() = AVFormatContext.codec_whitelist(value).reinterpret(Long.MAX_VALUE).getString(0L).split(",").toSet()
            set(value) = AVFormatContext.codec_whitelist(this.value, arena.allocateFrom(value.joinToString(",")))
        var formatWhitelist: Set<String>
            get() = AVFormatContext.format_whitelist(value).reinterpret(Long.MAX_VALUE).getString(0L).split(",").toSet()
            set(value) = AVFormatContext.format_whitelist(this.value, arena.allocateFrom(value.joinToString(",")))
        var protocolWhitelist: Set<String>
            get() = AVFormatContext.protocol_whitelist(value).reinterpret(Long.MAX_VALUE).getString(0L).split(",")
                .toSet()
            set(value) = AVFormatContext.protocol_whitelist(this.value, arena.allocateFrom(value.joinToString(",")))
        var protocolBlacklist: Set<String>
            get() = AVFormatContext.protocol_blacklist(value).reinterpret(Long.MAX_VALUE).getString(0L).split(",")
                .toSet()
            set(value) = AVFormatContext.protocol_blacklist(this.value, arena.allocateFrom(value.joinToString(",")))
        var videoCodec: Codec?
            get() = Codec(AVFormatContext.video_codec(value))
            set(value) = AVFormatContext.video_codec(this.value, value?.value ?: MemorySegment.NULL)
        var audioCodec: Codec?
            get() = Codec(AVFormatContext.audio_codec(value))
            set(value) = AVFormatContext.audio_codec(this.value, value?.value ?: MemorySegment.NULL)
        var subtitleCodec: Codec?
            get() = Codec(AVFormatContext.subtitle_codec(value))
            set(value) = AVFormatContext.subtitle_codec(this.value, value?.value ?: MemorySegment.NULL)
        var dataCodec: Codec?
            get() = Codec(AVFormatContext.data_codec(value))
            set(value) = AVFormatContext.data_codec(this.value, value?.value ?: MemorySegment.NULL)
        var durationProbesize: Long
            get() = AVFormatContext.duration_probesize(value)
            set(value) = AVFormatContext.duration_probesize(this.value, value)

        override fun destroyInternal(): Unit = Arena.ofConfined().use { arena ->
            FFMPEG.avformat_close_input(arena.pointerTo(value))
            ioCtx?.close()
        }

        fun readPacket(): Result<Packet> {
            val packet = Packet()
            val ret = FFMPEG.av_read_frame(value, packet.value)
            return Result.av(ret, "av_read_frame") { packet }
        }

        companion object {
            fun openCustom(ioCtx: IOContext, format: InputFormat? = null) = Arena.ofConfined().use { arena ->
                val ctx = FFMPEG.avformat_alloc_context().reinterpret(AVFormatContext.sizeof())
                AVFormatContext.pb(ctx, ioCtx.value)
                val ret = FFMPEG.avformat_open_input(
                    arena.pointerTo(ctx),
                    format?.value ?: MemorySegment.NULL,
                    MemorySegment.NULL,
                    MemorySegment.NULL,
                )
                Result.av(ret, "avformat_open_input") {
                    Input(ctx, ioCtx)
                }
            }
        }
    }

    class Output(
        override val value: MemorySegment,
        override val ioCtx: IOContext? = null,
        override val arena: Arena = Arena.ofAuto(),
    ) : FormatContext() {
        val oFormat: OutputFormat
            get() = OutputFormat(AVFormatContext.oformat(value))
        var maxInterleaveDelta: Long
            get() = AVFormatContext.max_interleave_delta(value)
            set(value) = AVFormatContext.max_interleave_delta(this.value, value)
        var maxChunkDuration: Duration
            get() = AVFormatContext.max_chunk_duration(value).microseconds
            set(value) = AVFormatContext.max_chunk_duration(this.value, value.inWholeMicroseconds.toInt())
        var maxChunkSize: Int
            get() = AVFormatContext.max_chunk_size(value)
            set(value) = AVFormatContext.max_chunk_size(this.value, value)
        var avoidNegativeTs: AvoidNegativeTs
            get() = AVFormatContext.avoid_negative_ts(value).let(::parseNativeEnum)
            set(value) = AVFormatContext.avoid_negative_ts(this.value, value.value)
        var audioPreload: Duration
            get() = AVFormatContext.audio_preload(value).microseconds
            set(value) = AVFormatContext.audio_preload(this.value, value.inWholeMicroseconds.toInt())
        var flushPackets: Boolean
            get() = AVFormatContext.flush_packets(value) != 0
            set(value) = AVFormatContext.flush_packets(this.value, if (value) 1 else 0)
        var metadataHeaderPadding: Int
            get() = AVFormatContext.metadata_header_padding(value)
            set(value) = AVFormatContext.metadata_header_padding(this.value, value)
        val opaque: MemorySegment?
            get() = AVFormatContext.opaque(value).takeIf { it != MemorySegment.NULL }
        var outputTsOffset: Duration
            get() = AVFormatContext.output_ts_offset(value).microseconds
            set(value) = AVFormatContext.output_ts_offset(this.value, value.inWholeMicroseconds)

        override fun destroyInternal() {
            FFMPEG.avformat_free_context(value)
            ioCtx?.close()
        }

        fun addStream(codec: Codec): Stream {
            return Stream(FFMPEG.avformat_new_stream(value, codec.value))
        }

        fun writePacketInterleaved(packet: Packet) =
            Result.av(FFMPEG.av_interleaved_write_frame(value, packet.value), "av_interleaved_write_frame")

        fun writePacket(packet: Packet) =
            Result.av(FFMPEG.av_write_frame(value, packet.value), "av_write_frame")

        fun flush() = Result.av(FFMPEG.av_write_frame(value, MemorySegment.NULL), "av_write_frame")

        fun writeHeader() = Result.av(FFMPEG.avformat_write_header(value, MemorySegment.NULL), "avformat_write_header")

        fun writeTrailer() = Result.av(FFMPEG.av_write_trailer(value), "av_write_trailer")

        companion object {
            fun open(ioCtx: IOContext, format: OutputFormat) = Arena.ofConfined().use { arena ->
                val ptr = arena.allocate(ValueLayout.ADDRESS)
                val ret = FFMPEG.avformat_alloc_output_context2(
                    ptr,
                    format.value,
                    MemorySegment.NULL,
                    MemorySegment.NULL
                )
                Result.av(ret, "avformat_alloc_output_context2") {
                    val ctx = ptr.get(ValueLayout.ADDRESS, 0).reinterpret(AVFormatContext.sizeof())
                    AVFormatContext.pb(ctx, ioCtx.value)
                    Output(ctx, ioCtx)
                }
            }
        }
    }

    enum class ContextFlags(override val value: Int) : NativeEnum {
        NOHEADER(FFMPEG.AVFMTCTX_NOHEADER()),
        UNSEEKABLE(FFMPEG.AVFMTCTX_UNSEEKABLE()),
    }

    enum class Flags(override val value: Int) : NativeEnum {
        AUTO_BSF(FFMPEG.AVFMT_FLAG_AUTO_BSF()),
        BITEXACT(FFMPEG.AVFMT_FLAG_BITEXACT()),
        CUSTOM_IO(FFMPEG.AVFMT_FLAG_CUSTOM_IO()),
        DISCARD_CORRUPT(FFMPEG.AVFMT_FLAG_DISCARD_CORRUPT()),
        FAST_SEEK(FFMPEG.AVFMT_FLAG_FAST_SEEK()),
        FLUSH_PACKETS(FFMPEG.AVFMT_FLAG_FLUSH_PACKETS()),
        GENPTS(FFMPEG.AVFMT_FLAG_GENPTS()),
        IGNDTS(FFMPEG.AVFMT_FLAG_IGNDTS()),
        IGNIDX(FFMPEG.AVFMT_FLAG_IGNIDX()),
        NOBUFFER(FFMPEG.AVFMT_FLAG_NOBUFFER()),
        NOFILLIN(FFMPEG.AVFMT_FLAG_NOFILLIN()),
        NONBLOCK(FFMPEG.AVFMT_FLAG_NONBLOCK()),
        NOPARSE(FFMPEG.AVFMT_FLAG_NOPARSE()),
        SORT_DTS(FFMPEG.AVFMT_FLAG_SORT_DTS()),
    }

    enum class DebugFlag(override val value: Int) : NativeEnum {
        TS(0x0001),
        ID3V2(0x0002),
    }

    enum class EventFlag(override val value: Int) : NativeEnum {
        METADATA_UPDATED(FFMPEG.AVFMT_EVENT_FLAG_METADATA_UPDATED()),
    }

    enum class AvoidNegativeTs(override val value: Int) : NativeEnum {
        AUTO(FFMPEG.AVFMT_AVOID_NEG_TS_AUTO()),
        DISABLED(FFMPEG.AVFMT_AVOID_NEG_TS_DISABLED()),
        MAKE_NON_NEGATIVE(FFMPEG.AVFMT_AVOID_NEG_TS_MAKE_NON_NEGATIVE()),
        MAKE_ZERO(FFMPEG.AVFMT_AVOID_NEG_TS_MAKE_ZERO()),
    }

    enum class DurationEstimationMethod(override val value: Int) : NativeEnum {
        FROM_PTS(FFMPEG.AVFMT_DURATION_FROM_PTS()),
        FROM_STREAM(FFMPEG.AVFMT_DURATION_FROM_STREAM()),
        FROM_BITRATE(FFMPEG.AVFMT_DURATION_FROM_BITRATE()),
    }
}
