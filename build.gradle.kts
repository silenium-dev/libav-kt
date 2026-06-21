import dev.silenium.libs.jni.nixJavaLauncher

plugins {
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.atomicfu") version "2.4.0"
    id("dev.silenium.libs.jni.nix-natives") version "0.6.4" apply false
}

group = "dev.silenium.libs.libav"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation(project(":bindings"))
    implementation("org.jetbrains.kotlinx:atomicfu:0.33.0")
    implementation("org.slf4j:slf4j-api:2.0.18")
    testImplementation("ch.qos.logback:logback-classic:1.5.34")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    javaLauncher = nixJavaLauncher()
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
