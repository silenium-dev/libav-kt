package dev.silenium.libs.av.core.data

import dev.silenium.libs.av.core.AlphaMode
import dev.silenium.libs.av.core.ChromaLocation
import dev.silenium.libs.av.core.ColorPrimaries
import dev.silenium.libs.av.core.ColorRange
import dev.silenium.libs.av.core.ColorSpace
import dev.silenium.libs.av.core.ColorTransferCharacteristics
import dev.silenium.libs.av.core.PixelFormat
import dev.silenium.libs.av.core.SampleFormat
import dev.silenium.libs.av.foreign.NativeEnum
import org.ffmpeg.bindings.AVDictionaryEntry
import org.ffmpeg.bindings.AVFrame
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

data class Crop(val top: ULong, val bottom: ULong, val left: ULong, val right: ULong) {
    internal fun apply(frame: MemorySegment) {
        AVFrame.crop_top(frame, top.toLong())
        AVFrame.crop_bottom(frame, bottom.toLong())
        AVFrame.crop_left(frame, left.toLong())
        AVFrame.crop_right(frame, right.toLong())
    }

    companion object {
        internal fun from(frame: MemorySegment) = Crop(
            AVFrame.crop_top(frame).toULong(),
            AVFrame.crop_bottom(frame).toULong(),
            AVFrame.crop_left(frame).toULong(),
            AVFrame.crop_right(frame).toULong(),
        )
    }
}

enum class PictureType : NativeEnum

sealed interface Frame : AutoCloseable {
    sealed interface Video : Frame {
        var width: Int
        var height: Int
        var format: PixelFormat
        var pictureType: PictureType
        var sampleAspectRatio: Rational
        var repeatPict: Int

        var colorRange: ColorRange
        var colorPrimaries: ColorPrimaries
        var colorTrc: ColorTransferCharacteristics
        var colorSpace: ColorSpace
        var chromaLocation: ChromaLocation

        var alphaMode: AlphaMode
        var crop: Crop
    }

    sealed interface Audio : Frame {
        var samples: Int
        var sampleRate: Int
        var format: SampleFormat
        var channelLayout: ChannelLayout
    }

    var linesize: List<Int>

    var buf: List<BufferRef?>
    var data: List<MemorySegment?>

    var pts: Long
    var pktDts: Long
    var timeBase: Rational
    var bestEffortTimestamp: Long
    var duration: Long
    var quality: Int
    var opaque: MemorySegment

    var extendedBuf: List<BufferRef?>
    var extendedData: List<MemorySegment?>

    var sideData: List<FrameSideData>
    var flags: Set<Flags>

    var metadata: Dictionary
    var decodeErrorFlags: Set<DecodeErrorFlags>
    var hwFramesCtx: BufferRef?
    var opaqueRef: BufferRef?
    var privateRef: MemorySegment

    enum class Flags : NativeEnum {}
    enum class DecodeErrorFlags : NativeEnum {}
}

data class Dictionary(val value: MemorySegment) {
    val asMap: Map<String, String> by lazy {
        val result = mutableMapOf<String, String>()
        var entryPtr = FFMPEG.av_dict_iterate(value, MemorySegment.NULL)
        while (entryPtr != MemorySegment.NULL) {
            val entry = entryPtr
                .get(ValueLayout.ADDRESS, 0)
                .reinterpret(AVDictionaryEntry.sizeof())
            val key = AVDictionaryEntry.key(entry).reinterpret(Long.MAX_VALUE).getString(0)
            val value = AVDictionaryEntry.value(entry).reinterpret(Long.MAX_VALUE).getString(0)
            result[key] = value
            entryPtr = FFMPEG.av_dict_iterate(this.value, entryPtr)
        }
        result
    }
}
