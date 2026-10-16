import asia.hombre.neorust.option.LTOFlag
import asia.hombre.neorust.option.OptLevel
import asia.hombre.neorust.option.PanicFlag
import asia.hombre.neorust.task.CargoBuild
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.ByteArrayOutputStream

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka") // KDocs
    id("maven-publish")
    id("signing")
    id("asia.hombre.neorust") // Rust support
}

group = "asia.hombre"
version = "2.6.0"
description = "SHA-3 Hash Functions in Kotlin Multiplatform"

val projectName = "keccak"

val mavenDir = projectDir.resolve("maven")

val isAutomated = System.getenv("PUBLISH_AUTO") == "true"

val rustTargetMap = mapOf(
    "linuxX64" to "x86_64-unknown-linux-gnu",
    "linuxArm64" to "aarch64-unknown-linux-gnu",
    "mingwX64" to "x86_64-pc-windows-gnu",
    "iosArm64" to "aarch64-apple-ios",
    "iosX64" to "x86_64-apple-ios",
    "iosSimulatorArm64" to "aarch64-apple-ios-sim",
    "androidNativeArm32" to "armv7-linux-androideabi",
    "androidNativeArm64" to "aarch64-linux-android",
    "androidNativeX64" to "x86_64-linux-android",
)

repositories {
    mavenCentral()
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:versioning-plugin:${providers.gradleProperty("dokka").get()}")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

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
        val testCompilation = compilations.getByName("test")
        val prepareResources = tasks.register<Copy>("prepareJsTestResources") {
            description = "Prepares the resources for the JS tests"
            from(layout.projectDirectory.dir("src/commonTest/resources"))
            // Common tests resolve fixture paths relative to the project root.
            into(testCompilation.npmProject.dir.map { it.dir("src/commonTest/resources") })
        }
        nodejs {
            testTask {
                dependsOn(prepareResources)
                inputs.files(prepareResources)
                    .withPropertyName("commonTestFixtures")
                    .withPathSensitivity(PathSensitivity.RELATIVE)
            }
        }
        browser {
            testTask {
                dependsOn(prepareResources)
                inputs.files(prepareResources)
                    .withPropertyName("commonTestFixtures")
                    .withPathSensitivity(PathSensitivity.RELATIVE)
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        val testCompilation = compilations.getByName("test")
        val prepareResources = tasks.register<Copy>("prepareWasmJsTestResources") {
            description = "Prepares the resources for the Wasm JS tests"
            from(layout.projectDirectory.dir("src/commonTest/resources"))
            into(testCompilation.npmProject.dir.map { it.dir("src/commonTest/resources") })
        }
        browser {
            testTask {
                dependsOn(prepareResources)
                inputs.files(prepareResources)
                    .withPropertyName("commonTestFixtures")
                    .withPathSensitivity(PathSensitivity.RELATIVE)
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        nodejs {
            testTask {
                dependsOn(prepareResources)
                inputs.files(prepareResources)
                    .withPropertyName("commonTestFixtures")
                    .withPathSensitivity(PathSensitivity.RELATIVE)
            }
        }
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs {
            testTask {
                // The generated Node launcher does not configure WASI preopens (KT-65179).
                val preopens = layout.projectDirectory.file("src/wasmWasiTest/node-preopens.cjs")
                nodeJsArgs.addAll(listOf("--require", preopens.asFile.absolutePath))
                inputs.file(preopens)
                    .withPropertyName("wasiPreopens")
                    .withPathSensitivity(PathSensitivity.RELATIVE)
                inputs.dir(layout.projectDirectory.dir("src/commonTest/resources"))
                    .withPropertyName("commonTestFixtures")
                    .withPathSensitivity(PathSensitivity.RELATIVE)
            }
        }
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

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("wasm") {
                withWasmJs()
                withWasmWasi()
            }
        }
    }

    sourceSets {
        getByName("commonTest") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.1")
            }
        }
    }
}

rust {
    name = "keccakkotlin-ffi"
    authors.add("Ron Lauren Hombre <ronlauren@hombre.asia>")
    edition = "2024"
    publishEnabled = false
    locked = true
    offline = gradle.startParameter.isOffline

    dependencies {
        crate(project("keccakrust")) {
            defaultFeatures = false
            features.add("ffi")
        }
    }

    library {
        crateType.add("staticlib")
    }

    profiles {
        named("dev") {
            panic = PanicFlag.abort
        }

        named("release") {
            panic = PanicFlag.abort
            optLevel = OptLevel.three
            lto = LTOFlag.fat
            codegenUnits.set(1)
        }
    }

    kotlin.targets.withType<KotlinNativeTarget>().configureEach {
        val canBuildOnCurrentHost = HostManager().isEnabled(konanTarget)
        val goodName = name.replaceFirstChar { it.uppercase() }
        val rustTarget = rustTargetMap.getValue(name)
        val outputName = rust.name.get().lowercase().replace("-", "")
        val ffiTargetDirectory = rust.targetDirectory.dir(name)
        val ffiLibrary = ffiTargetDirectory.map { it.file("$rustTarget/release/lib$outputName.a") }
        val interopLibrary = layout.buildDirectory.file("ffi/$rustTarget/lib$outputName.a")
        val symbolOptions = buildList {
            when {
                name == "mingwX64" -> {
                    // llvm-objcopy cannot weaken COFF symbols. Rename both the
                    // definition and its references to avoid Rust runtime collisions.
                    add("--redefine-sym=rust_eh_personality=keccakrust_eh_personality")
                }
                rustTarget.contains("apple") -> add("--weaken-symbol=_rust_eh_personality")
                else -> add("--weaken-symbol=rust_eh_personality")
            }
            if (name == "androidNativeArm32") {
                // The old NDK libgcc also supplies these atomic helpers.
                add("--wildcard")
                add("--weaken-symbol=__sync_*")
            }
        }

        val buildFFI = tasks.register<CargoBuild>("buildFFI$goodName") {
            description = "Build the KeccakRust C ABI bridge for $rustTarget"
            group = "build ffi"
            dependsOn(":generateCargoManifest")
            onlyIf("Kotlin/Native supports C interop for this target on the current host") {
                canBuildOnCurrentHost
            }

            this.manifestPath = rust.manifestPath
            this.targetDirectory = ffiTargetDirectory

            // CargoBuild's generated manifest is outside the source tree.
            // Track both crates so edits cannot reuse an outdated static library.
            // This is a temporary fix while this is not tracked in NRGP.
            inputs.files(
                fileTree("src/library/rust") { include("**/*.rs") },
                fileTree("keccakrust/src/library/rust") { include("**/*.rs") },
            ).withPropertyName("rustSources").withPathSensitivity(PathSensitivity.RELATIVE)

            target = rustTarget
            release = true
        }

        val prepareFFI = tasks.register<Exec>("prepareFFI$goodName") {
            description = "Prepare the FFI to coexist with other Rust runtimes for $rustTarget"
            group = "build ffi"
            dependsOn(buildFFI)
            onlyIf("Kotlin/Native supports C interop for this target on the current host") {
                canBuildOnCurrentHost
            }
            val llvmObjcopy = providers
                .gradleProperty("keccak.llvmObjcopy")
                .orElse("llvm-objcopy")

            inputs.file(ffiLibrary)
            inputs.property("llvmObjcopy", llvmObjcopy)
            inputs.property("symbolOptions", symbolOptions)
            outputs.file(interopLibrary)

            commandLine(
                listOf(llvmObjcopy.get()) + symbolOptions + listOf(
                    ffiLibrary.get().asFile.absolutePath,
                    interopLibrary.get().asFile.absolutePath,
                )
            )
            doFirst { interopLibrary.get().asFile.parentFile.mkdirs() }
        }

        val rustInterop = compilations.getByName("main").cinterops.create("FFI") {
            definitionFile.set(layout.projectDirectory.dir("native").file("native-ffi.def"))
            includeDirs(layout.projectDirectory.dir("src/library/rust/include").asFile)
            extraOpts("-libraryPath", interopLibrary.get().asFile.parentFile.absolutePath)
        }
        tasks.named(rustInterop.interopProcessingTaskName).configure {
            dependsOn(prepareFFI)
            inputs.file(interopLibrary)
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

val bundleAllSingleTask = tasks.register<Zip>("bundleAllSingle") {
    description = "Bundles all the buildable Maven Artifacts into a single package"
    group = "Bundle"
    dependsOn("publishAllPublicationsToMavenRepository")
    from(mavenDir)
    destinationDirectory.set(mavenDir)
    archiveFileName.set(projectName + "-" + project.version + "-singlebundle.zip")
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
        // Attach dynamic tasks to root tasks
        bundleAllSingleTask.configure { include("$mavenDeepDir/**") }
    }
}

dokka {
    moduleVersion = "nightly"

    pluginsConfiguration.html {
        footerMessage = "Copyright (c) 2026 Ron Lauren Hombre"
    }

    pluginsConfiguration.versioning {
        version = "nightly"
        olderVersionsDir = layout.projectDirectory.dir(
            providers.gradleProperty("dokkaOlderVersionsDir").orElse("versions")
        )
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
