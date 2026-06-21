package dev.silenium.libs.av.foreign

import kotlin.enums.enumEntries

interface NativeEnum {
    val value: Int
}

inline fun <reified E> parseNativeEnum(value: Int): E where E : Enum<E>, E : NativeEnum =
    enumEntries<E>().first { it.value == value }
