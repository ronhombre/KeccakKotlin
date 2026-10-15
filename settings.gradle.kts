pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    val kmm: String = providers.gradleProperty("kmm").get()
    val dokka: String = providers.gradleProperty("dokka").get()
    val nrgp: String = providers.gradleProperty("nrgp").get()
    plugins {
        kotlin("multiplatform") version kmm
        id("org.jetbrains.dokka") version dokka
        id("asia.hombre.neorust") version nrgp
    }
}
rootProject.name = "KeccakKotlin"
include("keccakrust")
include("java-example")
