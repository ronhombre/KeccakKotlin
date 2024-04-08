plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

group = "asia.hombre"
version = "0.0.2"

repositories {
    mavenCentral()
}

kotlin {
    jvm {

    }
    js(IR) {
        nodejs()
        browser {

        }
        binaries.executable()
    }
    mingwX64("windows") {
        binaries {
            sharedLib {  }
        }
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

publishing {
    repositories {
        mavenLocal()
        /*mavenLocal {
            url = uri("${System.getProperty("user.home")}/.m2/repository")
        }*/
    }
    publications {
        create<MavenPublication>("keccak") {
            artifactId = "keccak"
            from(components["kotlin"])
        }
        getByName<MavenPublication>("kotlinMultiplatform") {
            artifactId = "keccak-kmm"
        }
        getByName<MavenPublication>("js") {
            artifactId = "keccak-js"
        }
        getByName<MavenPublication>("jvm") {
            artifactId = "keccak-jvm"
        }
        getByName<MavenPublication>("windows") {
            artifactId = "keccak-win"
        }
    }
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("Keccak Kotlin Multiplatform Library")
            description.set("SHA-3 Hash Functions in Kotlin")
            url.set("https://github.com/ronhombre/KeccakKotlin")

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    name.set("Ron Lauren Hombre")
                    email.set("ronlauren@hombre.asia")
                }
            }
            scm {
                url.set("https://github.com/ronhombre/KeccakKotlin")
            }
        }
    }
}