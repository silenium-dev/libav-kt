package dev.silenium.libs.av.foreign

import java.util.*
import kotlin.enums.enumEntries

interface NativeEnum {
    val value: Int
}

inline fun <reified E> parseNativeEnumOrNull(value: Int): E? where E : Enum<E>, E : NativeEnum =
    enumEntries<E>().firstOrNull { it.value == value }

inline fun <reified E> parseNativeEnum(value: Int): E where E : Enum<E>, E : NativeEnum =
    parseNativeEnumOrNull(value) ?: error("Unknown enum value: $value")

inline fun <reified E> parseNativeEnumSet(value: Int): EnumSet<E> where E : Enum<E>, E : NativeEnum =
    enumEntries<E>().filter { it.value and value != 0 }.let {
        if (it.isNotEmpty()) {
            EnumSet.copyOf(it)
        } else {
            EnumSet.noneOf(E::class.java)
        }
    }
