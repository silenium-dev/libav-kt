package dev.silenium.libs.av.core.data

import org.ffmpeg.bindings.AVRational
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

data class Rational private constructor(val value: MemorySegment, private val arena: Arena? = null) {
    val isOwned: Boolean = arena != null

    constructor(value: MemorySegment) : this(value, null)

    val num: Int
        get() = AVRational.num(value)
    val den: Int
        get() = AVRational.den(value)

    override fun toString() = "$num/$den"

    companion object {
        operator fun invoke(num: Int, den: Int): Rational {
            val arena = Arena.ofAuto()
            val value = arena.allocate(AVRational.layout())
            AVRational.num(value, num)
            AVRational.den(value, den)
            return Rational(value, arena)
        }
    }
}
