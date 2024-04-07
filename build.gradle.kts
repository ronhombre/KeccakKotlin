plugins {
    kotlin("multiplatform")
}

group = "asia.hombre.keccak"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvm {

    }
    sourceSets {
        val commonMain by getting {
            dependencies {

            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}