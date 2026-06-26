package dev.silenium.libs.av.core.data

import java.lang.foreign.MemorySegment

data class FrameSideData(val value: MemorySegment)

data class PacketSideData(val value: MemorySegment)
