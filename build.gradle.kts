import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import java.io.ByteArrayOutputStream

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")  //KDocs
    id("maven-publish")
    id("signing")
}

group = "asia.hombre"
version = "2.2.0"
description = "SHA-3 Hash Functions in Kotlin Multiplatform"

val projectName = "keccak"

val mavenDir = projectDir.resolve("maven")

val isAutomated = false

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js {
        nodejs()
        browser {

        }
        binaries.executable()
    }
    linuxX64()
    linuxArm64()
    mingwX64()
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    sourceSets {
        getByName("commonTest") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}

publishing {
    repositories {
        maven {
            url = mavenDir.toURI()
        }
    }
    publications.withType<MavenPublication>().configureEach {
        artifactId = projectName + if (artifactId.contains("-")) "-" + artifactId.split("-").last() else ""
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
            description.set(project.description)
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

fun parseArtifactId(artifactId: String): String {
    val list = artifactId.splitToSequence("-").map { it.replaceFirstChar(Char::uppercase) }

    return list.joinToString("")
}

fun parseArtifactArchiveName(artifact: MavenPublication): String {
    return artifact.artifactId + "-" + artifact.version + "-bundle.zip"
}

val bundleAllTask = tasks.register("bundleAll") {
    description = "Bundles all the buildable Maven Artifacts"
    group = "Bundle"
    //dependsOn("publish")
}

val publishAllTask = tasks.register("publishAllToMavenCentral") {
    description = "Publishes and bundles all the buildable Maven Artifacts"
    group = "Publish"
    dependsOn("bundleAll")
}

afterEvaluate {
    publishing.publications.withType<MavenPublication>().configureEach {
        val artifact = this
        val pubNameCap = artifact.name.replaceFirstChar { it.uppercase() }
        val bundleFileName = parseArtifactArchiveName(artifact)

        val bundleTask = tasks.register<Zip>("bundle$pubNameCap") {
            description = "Bundles the Maven Artifact"
            group = "Bundle"
            from(mavenDir)
            val mavenDeepDir = artifact.groupId.replace(".", "/") + "/" + artifact.artifactId
            include("$mavenDeepDir/*/*")
            destinationDirectory.set(mavenDir)
            archiveFileName.set(bundleFileName)
        }

        val publishTask = tasks.register<Exec>("publish${pubNameCap}ToMavenCentral") {
            description = "Publish the Maven Artifact to Maven Central"
            mustRunAfter(bundleTask)
            group = "Publish"

            commandLine(
                "curl", "-X", "POST",
                "https://central.sonatype.com/api/v1/publisher/upload?name=${artifact.artifactId}&publishingType=" + if(isAutomated) "AUTOMATED" else "USER_MANAGED",
                "-H", "accept: text/plain",
                "-H", "Content-Type: multipart/form-data",
                "-H", "Authorization: Bearer " + System.getenv("SONATYPE_TOKEN"),
                "-F", "bundle=@$bundleFileName;type=application/x-zip-compressed"
            )
            workingDir(mavenDir)

            val stdOut = ByteArrayOutputStream()
            val errOut = ByteArrayOutputStream()
            standardOutput = stdOut
            errorOutput = errOut

            doLast {
                println(stdOut.toString())
                println(errOut.toString())
            }
        }

        // Attach dynamic tasks to root tasks
        bundleAllTask.configure { dependsOn(bundleTask) }
        publishAllTask.configure { dependsOn(publishTask) }
    }
}

dokka {
    pluginsConfiguration.html {
        footerMessage = "Copyright (c) 2025 Ron Lauren Hombre"
    }

    dokkaPublications.html {
        dokkaSourceSets {
            named("commonMain") {
                perPackageOption {
                    matchingRegex.set(".*")
                }
                reportUndocumented.set(true)
                documentedVisibilities(
                    VisibilityModifier.Public,
                    VisibilityModifier.Protected,
                )
            }
        }
    }
}