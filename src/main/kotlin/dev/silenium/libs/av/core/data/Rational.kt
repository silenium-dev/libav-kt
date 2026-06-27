package dev.silenium.libs.av.core.data

import org.ffmpeg.bindings.AVRational
import org.ffmpeg.bindings.FFMPEG
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

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

fun Long.durationFromAV(num: Int, den: Int) = (toDouble() * num / den).seconds
fun Long.durationFromAV(timeBase: Rational) = durationFromAV(timeBase.num, timeBase.den)

fun Duration.toAV(num: Int, den: Int) = toAV(Rational(num, den))
fun Duration.toAV(timeBase: Rational): Long {
    return FFMPEG.av_rescale_q(inWholeNanoseconds, nanosecondsTimeBase.value, timeBase.value)
}

private val nanosecondsTimeBase = Rational(1, 1_000_000_000)
