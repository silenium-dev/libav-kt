package dev.silenium.libs.av.core

import dev.silenium.libs.av.foreign.NativeEnum

enum class ColorRange(override val value: Int) : NativeEnum {}

enum class ColorPrimaries(override val value: Int) : NativeEnum {}

enum class ColorTransferCharacteristics(override val value: Int) : NativeEnum {}

enum class ColorSpace(override val value: Int) : NativeEnum {}

enum class ChromaLocation(override val value: Int) : NativeEnum {}

enum class AlphaMode(override val value: Int) : NativeEnum {}
