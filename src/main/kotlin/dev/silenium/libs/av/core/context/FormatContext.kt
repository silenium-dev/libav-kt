package dev.silenium.libs.av.core.context

import dev.silenium.libs.av.foreign.DoubleDestructionProtection
import dev.silenium.libs.av.foreign.pointerTo
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

sealed class FormatContext : AutoCloseable, DoubleDestructionProtection<MemorySegment>() {
    abstract val ioCtx: IOContext?

    class Input(override val value: MemorySegment, override val ioCtx: IOContext? = null) : FormatContext() {
        override fun destroyInternal(): Unit = Arena.ofConfined().use { arena ->
            FFMPEG.avformat_close_input(arena.pointerTo(value))
            ioCtx?.close()
        }
    }

    class Output(override val value: MemorySegment, override val ioCtx: IOContext? = null) : FormatContext() {
        override fun destroyInternal() {
            FFMPEG.avformat_free_context(value)
            ioCtx?.close()
        }
    }
}
