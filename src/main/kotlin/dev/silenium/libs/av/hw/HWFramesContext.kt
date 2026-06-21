package dev.silenium.libs.av.hw

import dev.silenium.libs.av.core.BufferRef
import dev.silenium.libs.av.core.PixelFormat
import dev.silenium.libs.av.core.av
import dev.silenium.libs.av.foreign.DoubleDestructionProtection
import dev.silenium.libs.av.foreign.parseNativeEnum
import org.ffmpeg.bindings.AVHWFramesContext
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.MemorySegment

sealed class HWFramesContext : DoubleDestructionProtection<BufferRef>() {
    protected val hwFramesCtx: MemorySegment
        get() = value.data.reinterpret(AVHWFramesContext.sizeof())

    open val width: Int
        get() = AVHWFramesContext.width(hwFramesCtx)
    open val height: Int
        get() = AVHWFramesContext.height(hwFramesCtx)
    open val format: PixelFormat
        get() = parseNativeEnum(AVHWFramesContext.format(hwFramesCtx))
    open val swFormat: PixelFormat
        get() = parseNativeEnum(AVHWFramesContext.sw_format(hwFramesCtx))
    open val initialPoolSize: Int
        get() = AVHWFramesContext.initial_pool_size(hwFramesCtx)

    data class Uninitialized(override val value: BufferRef) : HWFramesContext() {
        constructor(deviceCtx: HWDeviceContext) : this(BufferRef(FFMPEG.av_hwframe_ctx_alloc(deviceCtx.value.value)))

        override var width: Int
            get() = AVHWFramesContext.width(hwFramesCtx)
            set(value) = AVHWFramesContext.width(hwFramesCtx, value)
        override var height: Int
            get() = AVHWFramesContext.height(hwFramesCtx)
            set(value) = AVHWFramesContext.height(hwFramesCtx, value)
        override var format: PixelFormat
            get() = parseNativeEnum(AVHWFramesContext.format(hwFramesCtx))
            set(value) = AVHWFramesContext.format(hwFramesCtx, value.value)
        override var swFormat: PixelFormat
            get() = parseNativeEnum(AVHWFramesContext.sw_format(hwFramesCtx))
            set(value) = AVHWFramesContext.sw_format(hwFramesCtx, value.value)
        override var initialPoolSize: Int
            get() = AVHWFramesContext.initial_pool_size(hwFramesCtx)
            set(value) = AVHWFramesContext.initial_pool_size(hwFramesCtx, value)

        fun initialize(): Result<Initialized> {
            val ret = FFMPEG.av_hwframe_ctx_init(value.value)
            return Result.av(ret, "av_hwframe_ctx_init") { Initialized(value) }
        }
    }

    data class Initialized(override val value: BufferRef) : HWFramesContext()

    override fun destroyInternal() = value.destroy()
}
