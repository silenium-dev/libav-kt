package dev.silenium.libs.libav

import dev.silenium.libs.av.core.PacketFlag
import dev.silenium.libs.av.core.PixelFormat
import dev.silenium.libs.av.core.data.Packet
import dev.silenium.libs.av.hw.HWDeviceContext
import dev.silenium.libs.av.hw.HWDeviceType
import dev.silenium.libs.av.hw.HWFramesContext
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.ffmpeg.bindings.AVPacket
import java.nio.ByteBuffer

class MainTest : FunSpec({
    test("simple") {
        Packet().use { packet ->
            packet.data = "Hello".encodeToByteArray().let(ByteBuffer::wrap)
            packet.size shouldBe 5
            val buf = ByteArray(packet.size)
            packet.data.get(buf)
            buf.decodeToString() shouldBe "Hello"
            println("Packet: $packet")
            packet.flags = setOf(PacketFlag.KEY, PacketFlag.TRUSTED)
            packet.flags shouldBe setOf(PacketFlag.KEY, PacketFlag.TRUSTED)
            AVPacket.flags(packet.value) shouldBe (PacketFlag.KEY.value or PacketFlag.TRUSTED.value)
        }

        HWDeviceContext.create(HWDeviceType.DRM, "/dev/dri/renderD128").getOrThrow()
            .use { parentCtx ->
                ProcessHandle.current().info().commandLine().get().let { println("Command line: $it") }
                println("Parent Context: ${parentCtx.value}")
                HWDeviceContext.derive(parentCtx, HWDeviceType.VAAPI)
                    .getOrThrow().use { derivedCtx ->
                        println("Derived Context: ${derivedCtx.value}")

                        HWFramesContext.Uninitialized(derivedCtx).let {
                            it.width = 1920
                            it.height = 1080
                            it.format = PixelFormat.AV_PIX_FMT_VAAPI
                            it.swFormat = PixelFormat.AV_PIX_FMT_NV12
                            it.initialPoolSize = 3
                            it.initialize()
                        }.getOrThrow().use { framesCtx ->
                            println("Frames Context: ${framesCtx.value}")
                            println("Frames Context config: ${framesCtx.width}x${framesCtx.height} ${framesCtx.format}(${framesCtx.swFormat})")
                            framesCtx.allocateFrame().getOrThrow().use {
                                println("Frame: $it")
                                println(
                                    "Frame config: ${it.width}x${it.height} ${it.format} VASurface(0x%x)".format(
                                        it.data[3]?.address()
                                    )
                                )
                            }
                        }
                    }
            }
    }
})
