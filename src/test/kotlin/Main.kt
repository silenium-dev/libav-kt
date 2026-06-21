package dev.silenium.libs.libav

import dev.silenium.libs.av.hw.HWDeviceContext
import dev.silenium.libs.av.hw.HWDeviceType
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
    fun simple() = Arena.ofConfined().use { arena ->
        val parentCtx = HWDeviceContext.create(HWDeviceType.DRM, "/dev/dri/renderD128")
        println("Parent Context: 0x%x".format(parentCtx.value.address()))
        val derivedCtx = HWDeviceContext.derive(parentCtx, HWDeviceType.VAAPI)
        println("Derived Context: 0x%x".format(derivedCtx.value.address()))
        derivedCtx.close()
        parentCtx.close()
    }
}
