pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    val kmm: String by settings
    val dokka: String by settings
    plugins {
        kotlin("multiplatform") version kmm
        id("org.jetbrains.dokka") version dokka
    }
}
rootProject.name = "KeccakKotlin"
include("java-example")
