import dev.silenium.gradle.conventions.jvm
import dev.silenium.gradle.conventions.nix
import dev.silenium.gradle.conventions.tests
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("dev.silenium.gradle.conventions.jvm") version "0.1.5-37-g0f1c6b7f"
    kotlin("plugin.atomicfu") version "2.4.0"
    id("dev.silenium.libs.jni.nix-natives") version "0.6.4" apply false
}

group = "dev.silenium.libs.libav"

dependencies {
    implementation(project(":bindings"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:atomicfu:0.33.0")
    implementation("org.slf4j:slf4j-api:2.0.18")
    testImplementation("ch.qos.logback:logback-classic:1.5.34")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
}

conventions {
    jvm {
        jvmTarget = JvmTarget.JVM_25
    }
    tests {
        enabled = true
    }
    nix {
        useJavaForTests = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    compilerOptions.freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
}
