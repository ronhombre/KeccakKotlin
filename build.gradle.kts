import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")  //KDocs
    id("maven-publish")
    id("signing")
}

group = "asia.hombre"
version = "2.5.0"
description = "SHA-3 Hash Functions in Kotlin Multiplatform"

val projectName = "keccak"

val mavenDir = projectDir.resolve("maven")

val isAutomated = false

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.getByName("main") {
            compileTaskProvider.configure {
                //Set up the Kotlin compiler options for the 'main' compilation:
                compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
            }

            compileTaskProvider //Get the Kotlin task 'compileKotlinJvm'
            output //Get the main compilation output
        }
    }
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
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.1")
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

fun parseArtifactArchiveName(artifactName: String, version: String): String {
    return "$artifactName-$version-bundle.zip"
}

val bundleAllTask = tasks.register("bundleAll") {
    description = "Bundles all the buildable Maven Artifacts"
    group = "Bundle"
    //dependsOn("publish")
}

val bundleAllSingleTask = tasks.register<Zip>("bundleAllSingle") {
    description = "Bundles all the buildable Maven Artifacts into a single package"
    group = "Bundle"
    dependsOn("publishAllPublicationsToMavenRepository")
    from(mavenDir)
    destinationDirectory.set(mavenDir)
    archiveFileName.set(projectName + "-" + project.version + "-singlebundle.zip")
}

val publishAllTask = tasks.register("publishAllToMavenCentral") {
    description = "Publishes and bundles all the buildable Maven Artifacts"
    group = "Publish"
    dependsOn("bundleAll")
}

val publishAllSingleTask = tasks.register<Exec>("publishAllSingleToMavenCentral") {
    description = "Publish the Maven Artifact to Maven Central"
    dependsOn(bundleAllSingleTask)
    mustRunAfter(bundleAllSingleTask)
    group = "Publish"

    commandLine(
        "curl", "-X", "POST",
        "https://central.sonatype.com/api/v1/publisher/upload?name=$projectName&publishingType=" + if(isAutomated) "AUTOMATED" else "USER_MANAGED",
        "-H", "accept: text/plain",
        "-H", "Content-Type: multipart/form-data",
        "-H", "Authorization: Bearer " + System.getenv("SONATYPE_TOKEN"),
        "-F", "bundle=@${bundleAllSingleTask.get().archiveFileName.get()};type=application/x-zip-compressed"
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

// Most of these will be deprecated and removed in the future while the new pipeline undergoes field-testing.
gradle.projectsEvaluated {
    publishing.publications.withType<MavenPublication>().configureEach {
        artifactId = projectName + if (artifactId.contains("-")) "-" + artifactId.split("-").last() else ""
        val artifact = this
        val mavenDeepDir = artifact.groupId.replace(".", "/") + "/" + artifact.artifactId
        val pubNameCap = artifact.name.replaceFirstChar { it.uppercase() }
        val bundleFileName = parseArtifactArchiveName(artifact.artifactId, artifact.version)
        val bundleTask = tasks.register<Zip>("bundle$pubNameCap") {
            description = "Bundles the Maven Artifact"
            group = "Bundle"
            from(mavenDir)
            include("$mavenDeepDir/**")
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
        bundleAllSingleTask.configure { include("$mavenDeepDir/**") }
        publishAllTask.configure { dependsOn(publishTask) }
    }
}

dokka {
    pluginsConfiguration.html {
        footerMessage = "Copyright (c) 2026 Ron Lauren Hombre"
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