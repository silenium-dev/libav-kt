package dev.silenium.libs.libav

import dev.silenium.libs.av.core.PixelFormat
import dev.silenium.libs.av.hw.HWDeviceContext
import dev.silenium.libs.av.hw.HWDeviceType
import dev.silenium.libs.av.hw.HWFramesContext
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import kotlin.test.Test

fun Arena.pointerTo(segment: MemorySegment): MemorySegment {
    val pointer = allocate(ValueLayout.ADDRESS)
    pointer.set(ValueLayout.ADDRESS, 0, segment)
    return pointer
}

class MainTest {
    @Test
    fun simple() {
        HWDeviceContext.create(HWDeviceType.DRM, "/dev/dri/renderD128").getOrThrow().use { parentCtx ->
            println("Parent Context: ${parentCtx.value}")
            HWDeviceContext.derive(parentCtx, HWDeviceType.VAAPI).getOrThrow().use { derivedCtx ->
                println("Derived Context: ${derivedCtx.value}")

                HWFramesContext.Uninitialized(derivedCtx).let {
                    it.width = 1920
                    it.height = 1080
                    it.format = PixelFormat.AV_PIX_FMT_VAAPI
                    it.swFormat = PixelFormat.AV_PIX_FMT_NV12
                    it.initialPoolSize = 1
                    it.initialize()
                }.getOrThrow().use { framesCtx ->
                    println("Frames Context: ${framesCtx.value}")
                    println("Frames Context config: ${framesCtx.width}x${framesCtx.height} ${framesCtx.format}(${framesCtx.swFormat})")
                }
            }
        }
    }
}
