package dev.silenium.libs.av.foreign

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

fun Arena.pointerTo(segment: MemorySegment): MemorySegment {
    val pointer = allocate(ValueLayout.ADDRESS)
    pointer.set(ValueLayout.ADDRESS, 0, segment)
    return pointer
}

fun MemorySegment.asPointerArray(size: Int): List<MemorySegment?> {
    return List(size) { n -> getAtIndex(ValueLayout.ADDRESS, n.toLong()).takeIf { it != MemorySegment.NULL } }
}

inline fun <reified T> MemorySegment.asPointerArray(size: Int, wrapper: (MemorySegment) -> T): List<T> {
    return List(size) { getAtIndex(ValueLayout.ADDRESS, it.toLong()).let(wrapper) }
}

fun MemorySegment.asIntArray(size: Int): List<Int> {
    return List(size) { getAtIndex(ValueLayout.JAVA_INT, it.toLong()) }
}

fun <T> Collection<T>.asNativeArray(arena: Arena, mapper: (T) -> MemorySegment?): MemorySegment {
    val allocated = arena.allocate(ValueLayout.ADDRESS, size.toLong())
    forEachIndexed { idx, it ->
        allocated.setAtIndex(ValueLayout.ADDRESS, idx.toLong(), mapper(it) ?: MemorySegment.NULL)
    }
    return allocated
}

@JvmName("asNativeArrayInt")
fun Collection<Int>.asNativeArray(arena: Arena): MemorySegment {
    return arena.allocateFrom(ValueLayout.JAVA_INT, *toIntArray())
}

@JvmName("asNativeArrayMemorySegment")
fun Collection<MemorySegment?>.asNativeArray(arena: Arena): MemorySegment {
    return asNativeArray(arena) { it }
}

fun <E> Set<E>.asNative() where E : Enum<E>, E : NativeEnum = fold(0) { acc, e -> acc or e.value }
