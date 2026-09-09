pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    val kmm: String = providers.gradleProperty("kmm").get()
    val dokka: String = providers.gradleProperty("dokka").get()
    plugins {
        kotlin("multiplatform") version kmm
        id("org.jetbrains.dokka") version dokka
    }
}
rootProject.name = "KeccakKotlin"
include("java-example")
