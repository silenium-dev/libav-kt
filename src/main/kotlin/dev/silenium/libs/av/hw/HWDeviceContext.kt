package dev.silenium.libs.av.hw

import dev.silenium.libs.av.core.checkAV
import dev.silenium.libs.av.foreign.DoubleDestructionProtection
import dev.silenium.libs.av.foreign.NativeEnum
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

data class HWDeviceContext(val bufferRef: MemorySegment) : DoubleDestructionProtection<MemorySegment>() {
    override val value: MemorySegment by ::bufferRef
    override fun destroyInternal() {
        FFMPEG.av_buffer_unref(value)
    }

    companion object {
        fun create(type: HWDeviceType, device: String /*TODO: Flags and Opts*/): HWDeviceContext =
            Arena.ofConfined().use { arena ->
                val ctxPtr = arena.allocate(ValueLayout.ADDRESS)
                FFMPEG.av_hwdevice_ctx_create(ctxPtr, type.value, arena.allocateFrom(device), MemorySegment.NULL, 0)
                    .checkAV("av_hwdevice_ctx_create")
                HWDeviceContext(ctxPtr.get(ValueLayout.ADDRESS, 0))
            }

        fun derive(parent: HWDeviceContext, type: HWDeviceType /*TODO: Flags*/): HWDeviceContext =
            Arena.ofConfined().use { arena ->
                val ctxPtr = arena.allocate(ValueLayout.ADDRESS)
                FFMPEG.av_hwdevice_ctx_create_derived(ctxPtr, type.value, parent.bufferRef, 0)
                    .checkAV("av_hwdevice_ctx_create_derived")
                HWDeviceContext(ctxPtr.get(ValueLayout.ADDRESS, 0))
            }
    }
}

enum class HWDeviceType(override val value: Int) : NativeEnum {
    NONE(FFMPEG.AV_HWDEVICE_TYPE_NONE()),
    VDPAU(FFMPEG.AV_HWDEVICE_TYPE_VDPAU()),
    CUDA(FFMPEG.AV_HWDEVICE_TYPE_CUDA()),
    VAAPI(FFMPEG.AV_HWDEVICE_TYPE_VAAPI()),
    DXVA2(FFMPEG.AV_HWDEVICE_TYPE_DXVA2()),
    QSV(FFMPEG.AV_HWDEVICE_TYPE_QSV()),
    VIDEOTOOLBOX(FFMPEG.AV_HWDEVICE_TYPE_VIDEOTOOLBOX()),
    D3D11VA(FFMPEG.AV_HWDEVICE_TYPE_D3D11VA()),
    DRM(FFMPEG.AV_HWDEVICE_TYPE_DRM()),
    OPENCL(FFMPEG.AV_HWDEVICE_TYPE_OPENCL()),
    MEDIACODEC(FFMPEG.AV_HWDEVICE_TYPE_MEDIACODEC()),
    VULKAN(FFMPEG.AV_HWDEVICE_TYPE_VULKAN()),
    D3D12VA(FFMPEG.AV_HWDEVICE_TYPE_D3D12VA()),
    AMF(FFMPEG.AV_HWDEVICE_TYPE_AMF()),

    /* OpenHarmony Codec device */
    OHCODEC(FFMPEG.AV_HWDEVICE_TYPE_OHCODEC()),
}
