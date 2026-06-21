package dev.silenium.libs.av.core

import org.ffmpeg.bindings.AVRational
import java.lang.foreign.MemorySegment

data class Rational(val value: MemorySegment) {
    val num: Int
        get() = AVRational.num(value)
    val den: Int
        get() = AVRational.den(value)

    override fun toString() = "$num/$den"
}
