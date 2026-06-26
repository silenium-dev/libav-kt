package dev.silenium.libs.av.core.data

import dev.silenium.libs.av.core.PacketFlag
import dev.silenium.libs.av.foreign.DoubleDestructionProtection
import dev.silenium.libs.av.foreign.asNative
import dev.silenium.libs.av.foreign.asNativeArray
import dev.silenium.libs.av.foreign.asPointerArray
import dev.silenium.libs.av.foreign.parseNativeEnumSet
import dev.silenium.libs.av.foreign.pointerTo
import org.ffmpeg.bindings.AVPacket
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer

data class Packet private constructor(
    override val value: MemorySegment,
    private val arena: Arena,
) : DoubleDestructionProtection<MemorySegment>() {
    constructor(value: MemorySegment) : this(value, Arena.ofAuto())

    val size: Int
        get() = AVPacket.size(value)
    var data: ByteBuffer
        get() = AVPacket.data(value).reinterpret(size.toLong()).asByteBuffer()
        set(value) {
            val segment = arena.allocate(value.limit().toLong())
            segment.copyFrom(MemorySegment.ofBuffer(value))
            AVPacket.data(this.value, segment)
            AVPacket.size(this.value, segment.byteSize().toInt())
        }
    var pos: Long
        get() = AVPacket.pos(value)
        set(value) = AVPacket.pos(this.value, value)
    var streamIndex: Int
        get() = AVPacket.stream_index(value)
        set(value) = AVPacket.stream_index(this.value, value)

    var timeBase: Rational
        get() = Rational(AVPacket.time_base(value))
        set(value) = AVPacket.time_base(this.value, value.value)
    var pts: Long
        get() = AVPacket.pts(value)
        set(value) = AVPacket.pts(this.value, value)
    var dts: Long
        get() = AVPacket.dts(value)
        set(value) = AVPacket.dts(this.value, value)
    var duration: Long
        get() = AVPacket.duration(value)
        set(value) = AVPacket.duration(this.value, value)

    var sideData: List<PacketSideData>
        get() = AVPacket.side_data(value).asPointerArray(AVPacket.side_data_elems(value), ::PacketSideData)
        set(value) = AVPacket.side_data(this.value, value.asNativeArray(arena, PacketSideData::value))
    var flags: Set<PacketFlag>
        get() = AVPacket.flags(value).let(::parseNativeEnumSet)
        set(value) = AVPacket.flags(this.value, value.asNative())

    val opaque: MemorySegment?
        get() = AVPacket.opaque(value).takeIf { it != MemorySegment.NULL }
    val opaqueRef: BufferRef?
        get() = AVPacket.opaque_ref(value).takeIf { it != MemorySegment.NULL }?.let(BufferRef::of)

    override fun destroyInternal() {
        FFMPEG.av_packet_free(arena.pointerTo(value))
    }

    override fun toString(): String {
        return "Packet(value=$value)"
    }

    companion object {
        operator fun invoke() =
            FFMPEG.av_packet_alloc().reinterpret(AVPacket.sizeof()).let(::Packet)
    }
}
