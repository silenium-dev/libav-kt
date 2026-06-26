package dev.silenium.libs.av.foreign

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.invoke.MethodHandles
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

fun KFunction<*>.upcallStub(
    thiz: Any,
    linker: Linker,
    descriptor: FunctionDescriptor,
    arena: Arena
): MemorySegment = linker.upcallStub(
    MethodHandles.lookup().unreflect(this.javaMethod).bindTo(thiz),
    descriptor,
    arena,
)
