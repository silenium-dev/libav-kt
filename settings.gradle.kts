pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://nexus.silenium.dev/repository/maven-releases/")
        maven("https://nexus.silenium.dev/repository/maven-snapshots/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "libav-kt"
include("bindings")
