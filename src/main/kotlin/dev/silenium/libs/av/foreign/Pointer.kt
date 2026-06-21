package dev.silenium.libs.av.foreign

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

fun Arena.pointerTo(segment: MemorySegment): MemorySegment {
    val pointer = allocate(ValueLayout.ADDRESS)
    pointer.set(ValueLayout.ADDRESS, 0, segment)
    return pointer
}

fun MemorySegment.asPointerArray(size: Int): Array<MemorySegment> {
    return Array(size) { getAtIndex(ValueLayout.ADDRESS, it.toLong()) }
}
fun MemorySegment.asIntArray(size: Int): Array<Int> {
    return Array(size) { getAtIndex(ValueLayout.JAVA_INT, it.toLong()) }
}
