package dev.silenium.libs.av.foreign

import java.util.EnumSet
import kotlin.enums.enumEntries

interface NativeEnum {
    val value: Int
}

inline fun <reified E> parseNativeEnum(value: Int): E where E : Enum<E>, E : NativeEnum =
    enumEntries<E>().first { it.value == value }

inline fun <reified E> parseNativeEnumSet(value: Int): EnumSet<E> where E : Enum<E>, E : NativeEnum =
    EnumSet.copyOf(enumEntries<E>().filter { it.value and value != 0 })
