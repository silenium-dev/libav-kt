package dev.silenium.libs.libav

import dev.silenium.libs.av.core.AVException
import dev.silenium.libs.av.core.PacketFlag
import dev.silenium.libs.av.core.PixelFormat
import dev.silenium.libs.av.core.context.ClasspathIOCallback
import dev.silenium.libs.av.core.context.FormatContext
import dev.silenium.libs.av.core.context.IOContext
import dev.silenium.libs.av.core.data.Codec
import dev.silenium.libs.av.core.data.OutputFormat
import dev.silenium.libs.av.core.data.Packet
import dev.silenium.libs.av.hw.HWDeviceContext
import dev.silenium.libs.av.hw.HWDeviceType
import dev.silenium.libs.av.hw.HWFramesContext
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.ffmpeg.bindings.AVPacket
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.MemorySegment
import java.nio.ByteBuffer

class MainTest : FunSpec({
    test("remux") {
        val outCtx = object : IOContext.Custom.WritableCallback {
            override fun write(ptr: MemorySegment): Int {
                println("Wrote ${ptr.byteSize()} bytes")
                return ptr.byteSize().toInt()
            }

            override fun read(ptr: MemorySegment): Int {
                println("Read ${ptr.byteSize()} bytes")
                return 0
            }

            override fun seek(offset: Long, whence: IOContext.Custom.Whence): Long {
                println("Seeked to $offset ($whence)")
                return 0L
            }

            override fun close() {
                println("Closed")
            }
        }
        FormatContext.Output.open(IOContext.Custom(outCtx), OutputFormat.guess("matroska")!!).getOrThrow()
            .use { outCtx ->
                val outStream = outCtx.addStream(Codec.findEncoder(Codec.ID.CODEC_VP9).getOrThrow())
                FormatContext.Input.open(IOContext.Custom(ClasspathIOCallback("test.webm"))).getOrThrow()
                    .use { fmtCtx ->
                        println("Input format: ${fmtCtx.iFormat.name}")
                        var packet = fmtCtx.readPacket().getOrThrow()
                        val inStream = fmtCtx.streams[packet.streamIndex]
                        outStream.timeBase = inStream.timeBase
                        outStream.codecParams = inStream.codecParams
                        outCtx.writeHeader().getOrThrow()
                        while (true) {
                            packet.streamIndex = outStream.index
                            outCtx.writePacketInterleaved(packet).getOrThrow()
                            packet = fmtCtx.readPacket().getOrElse {
                                if (it is AVException && it.ret == FFMPEG.AVERROR_EOF()) break
                                throw it
                            }
                        }
                    }
                outCtx.writeTrailer().getOrThrow()
            }
        val cb = ClasspathIOCallback("test.webm")
        cb.seek(0L, IOContext.Custom.Whence(IOContext.Custom.SeekOrigin.END)) shouldBeGreaterThan 0L
        cb.seek(0L, IOContext.Custom.Whence(IOContext.Custom.SeekOrigin.SET)) shouldBe 0L
        val buf = ByteArray(1024)
        cb.read(MemorySegment.ofArray(buf)) shouldBeGreaterThan 0
        cb.close()
    }

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
