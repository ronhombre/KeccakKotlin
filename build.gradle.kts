import org.jetbrains.dokka.gradle.DokkaTask
import java.io.ByteArrayOutputStream
import java.nio.file.Files

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")  //KDocs
    id("maven-publish")
    id("signing")
}

group = "asia.hombre"
version = "1.1.0"
description = "SHA-3 Hash Functions in Kotlin"

val projectName = "keccak"

val mavenDir = projectDir.resolve("maven")

val isAutomated = false

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
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
        val commonTest by getting {
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
    publications {
        //Dynamically rename all artifacts
        this.forEach {
            val mavenPublication = it as MavenPublication
            mavenPublication.artifactId = projectName +
                    if(mavenPublication.artifactId.contains("-"))
                        "-" + mavenPublication.artifactId.split("-").last()
                    else
                        ""
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

fun parseArtifactArchiveName(artifact: MavenPublication): String {
    return artifact.artifactId + "-" + artifact.version + "-bundle.zip"
}

for (publication in publishing.publications.asMap) {
    val artifact = publication.value as MavenPublication
    val parsedArtifactId = parseArtifactId(artifact.artifactId)
    val bundleFileName = parseArtifactArchiveName(artifact)

    tasks.register<Zip>("bundle$parsedArtifactId") {
        group = "Bundle"
        from(mavenDir)
        val mavenDeepDir = artifact.groupId.replace(".", "/") + "/" + artifact.artifactId
        include("$mavenDeepDir/*/*")
        destinationDirectory = mavenDir
        archiveFileName = parseArtifactArchiveName(artifact)
    }

    tasks.register<Exec>("publish" + parsedArtifactId + "ToMavenCentral") {
        mustRunAfter("bundle$parsedArtifactId")
        group = "Publish"
        /*if(!mavenDir.resolve(bundleFileName).exists())
            throw RuntimeException("Bundle does not exist! Please run `bundle$parsedArtifactId`")*/

        commandLine(
            "curl", "-X", "POST",
            "https://central.sonatype.com/api/v1/publisher/upload?name=${artifact.artifactId}&publishingType=" + if(isAutomated) "AUTOMATED" else "USER_MANAGED",
            "-H", "accept: text/plain",
            "-H", "Content-Type: multipart/form-data",
            "-H", "Authorization: Bearer " + System.getenv("SONATYPE_TOKEN"),
            "-F", "bundle=@$bundleFileName;type=application/x-zip-compressed"
        )
        workingDir(mavenDir.toString())
        standardOutput = ByteArrayOutputStream()
        errorOutput = ByteArrayOutputStream()

        // Execute some action with the output
        doLast {
            println("$standardOutput")
            println("$errorOutput")
        }
    }
}

tasks.register("bundleAll") {
    group = "Bundle"
    dependsOn("publish")

    for (publication in publishing.publications.asMap) {
        val artifact = publication.value as MavenPublication

        dependsOn("bundle" + parseArtifactId(artifact.artifactId))
    }
}

tasks.register("publishAllToMavenCentral") {
    group = "Publish"
    dependsOn("bundleAll")

    for (publication in publishing.publications.asMap) {
        val artifact = publication.value as MavenPublication

        dependsOn("publish" + parseArtifactId(artifact.artifactId) + "ToMavenCentral")
    }
}

val npmDir = "./npm"

tasks.register("packageNPM") {
    mkdir(npmDir)
    val packageSourcePath = projectDir.toPath().resolve("npm.json")
    val packagePath = projectDir.toPath().resolve("npm").resolve("package.json")

    var packageFile = String(Files.readAllBytes(packageSourcePath))
    packageFile = packageFile.replace("<VERSION>", version.toString()).replace("<DESCRIPTION>", project.description.toString())
    Files.write(packagePath, packageFile.toByteArray())
}
val npmKotlinDir = "$npmDir/kotlin"

tasks.register<Copy>("bundleNPM") {
    dependsOn("jsBrowserProductionWebpack", "packageNPM")

    from(project.layout.buildDirectory.get().asFile.resolve("js").resolve("packages").resolve(project.name).resolve("kotlin"))
    into(npmKotlinDir)

    doFirst {
        delete(npmKotlinDir)
        mkdir(npmKotlinDir)
    }
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("commonMain") {
            // Adjust visibility to include internal and private members
            perPackageOption {
                matchingRegex.set(".*") // Match all packages
                includeNonPublic.set(false)
            }
            // Optionally, report undocumented members
            reportUndocumented.set(true)
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    val dokkaBaseConfiguration = """
    {
      "footerMessage": "(C) 2024 Ron Lauren Hombre"
    }
    """
    pluginsMapConfiguration.set(
        mapOf(
            // fully qualified plugin name to json configuration
            "org.jetbrains.dokka.base.DokkaBase" to dokkaBaseConfiguration
        )
    )
}