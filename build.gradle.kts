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
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
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

tasks.withType<KotlinNativeTest>().configureEach {
    val projectDirectory = layout.projectDirectory.asFile.absolutePath
    environment("KECCAK_TEST_PROJECT_DIR", projectDirectory, tracked = false)
    inputs.dir(layout.projectDirectory.dir("src/commonTest/resources"))
        .withPropertyName("commonTestFixtures")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    // xcrun simctl spawn does not preserve the host working directory and only forwards SIMCTL_CHILD_* variables.
    environment(
        "SIMCTL_CHILD_KECCAK_TEST_PROJECT_DIR",
        layout.projectDirectory.asFile.absolutePath,
        tracked = false,
    )
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
        val toolBaseDirectory = layout.projectDirectory.asFile
        val objcopyOptions = buildList {
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
            add("--strip-debug")
            add("--remove-section=.llvmbc")
            add("--remove-section=.llvmcmd")
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
        fun llvmTool(property: String, command: String): Provider<String> =
            providers.gradleProperty(property).orElse(command).map { path ->
                if (('/' in path || '\\' in path) && !File(path).isAbsolute) {
                    toolBaseDirectory.resolve(path).absolutePath
                } else path
            }

        val prepareFFI = tasks.register<PrepareFFI>("prepareFFI$goodName") {
            description = "Keep the Keccak FFI and its native dependencies for $rustTarget"
            group = "build ffi"
            dependsOn(buildFFI)
            onlyIf("Kotlin/Native supports C interop for this target on the current host") {
                canBuildOnCurrentHost
            }
            sourceArchive.set(ffiLibrary)
            preparedArchive.set(interopLibrary)
            llvmAr.set(llvmTool("keccak.llvmAr", "llvm-ar"))
            llvmNm.set(llvmTool("keccak.llvmNm", "llvm-nm"))
            llvmObjcopy.set(llvmTool("keccak.llvmObjcopy", "llvm-objcopy"))
            symbolOptions.set(objcopyOptions)

            pruneArchive.set(providers.gradleProperty("keccak.pruneArchive")
                .map { it.toBooleanStrict() }.orElse(true))
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

/**
 * Significantly reduce the FFI's artifact klib from ~700KiB to ~8KiB.
 *
 * This is important because of Maven Central's new publishing quota limits.
 */
@CacheableTask
abstract class PrepareFFI @Inject constructor(private val execOperations: ExecOperations) : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val sourceArchive: RegularFileProperty

    @get:OutputFile
    abstract val preparedArchive: RegularFileProperty

    @get:Input
    abstract val llvmAr: Property<String>

    @get:Input
    abstract val llvmNm: Property<String>

    @get:Input
    abstract val llvmObjcopy: Property<String>

    @get:Input
    abstract val symbolOptions: ListProperty<String>

    @get:Input
    abstract val pruneArchive: Property<Boolean>

    private fun runTool(workDir: File, executable: String, arguments: List<String>): String {
        val stdout = ByteArrayOutputStream()
        execOperations.exec {
            workingDir(workDir)
            commandLine(buildList(arguments.size + 1) {
                add(executable)
                addAll(arguments)
            })
            standardOutput = stdout
        }.assertNormalExitValue()
        return stdout.toString(Charsets.UTF_8.name())
    }

    private fun keepFullArchive(reason: String) {
        logger.warn("$name: keeping the full FFI archive: $reason")
    }

    private fun prune(workDir: File, archiveName: String) {
        val members = ArrayList<String>()
        val uniqueMembers = HashSet<String>()
        for (line in runTool(workDir, llvmAr.get(), listOf("t", archiveName)).lineSequence()) {
            if (line.isNotEmpty()) {
                members.add(line)
                if (!uniqueMembers.add(line)) {
                    keepFullArchive("duplicate member names prevent unambiguous selection")
                    return
                }
            }
        }

        val prefix = "$archiveName["
        val definitions = HashMap<String, MutableSet<String>>()
        val undefinedByMember = HashMap<String, MutableSet<String>>()
        val commonMembers = HashSet<String>()

        val nmOutput = runTool(
            workDir,
            llvmNm.get(),
            listOf(
                "--format=posix",
                "--print-file-name",
                "--extern-only",
                "--no-demangle",
                "--no-sort",
                "--quiet",
                archiveName,
            ),
        )
        for (line in nmOutput.lineSequence()) {
            if (line.isBlank()) continue
            val separator = line.lastIndexOf("]: ")
            check(line.startsWith(prefix) && separator >= prefix.length) {
                "Unexpected llvm-nm output: $line"
            }
            val member = line.substring(prefix.length, separator)
            val rest = line.substring(separator + 3).trimStart()
            val nameEnd = rest.indexOfFirst { it.isWhitespace() }
            check(nameEnd > 0) { "Unexpected llvm-nm symbol: $line" }
            val symName = rest.substring(0, nameEnd)
            var kindIndex = nameEnd + 1
            while (kindIndex < rest.length && rest[kindIndex].isWhitespace()) kindIndex++
            check(kindIndex < rest.length && (kindIndex + 1 == rest.length || rest[kindIndex + 1].isWhitespace())) {
                "Unexpected llvm-nm symbol: $line"
            }

            when (val kind = rest[kindIndex]) {
                'U' -> undefinedByMember.getOrPut(member) { LinkedHashSet() }.add(symName)
                'w', 'v' -> Unit // Optional undefined weak symbols
                else -> {
                    if (kind == 'C') commonMembers.add(member)
                    definitions.getOrPut(symName) { LinkedHashSet() }.add(member)
                }
            }
        }

        var hasPermute = false
        val selected = LinkedHashSet<String>()
        for ((symName, providers) in definitions) {
            val exportedName = symName.removePrefix("_")
            if (exportedName.startsWith("keccakrust_")) {
                if (exportedName == "keccakrust_permute") hasPermute = true
                if (providers.size > 1) {
                    keepFullArchive("an FFI export has multiple providers")
                    return
                }
                selected.addAll(providers)
            }
        }
        check(hasPermute) { "The Rust archive does not export keccakrust_permute" }

        val pending = ArrayDeque(selected)
        while (pending.isNotEmpty()) {
            val member = pending.removeFirst()
            if (member in commonMembers) {
                keepFullArchive("a selected member uses common-symbol resolution")
                return
            }
            for (symName in undefinedByMember[member].orEmpty()) {
                val providers = definitions[symName] ?: continue
                if (providers.any { it in selected }) continue
                if (providers.size > 1) {
                    keepFullArchive("$symName has multiple archive providers")
                    return
                }
                val provider = providers.first()
                if (selected.add(provider)) pending.addLast(provider)
            }
        }

        check(uniqueMembers.containsAll(selected)) { "llvm-nm reported an unknown archive member" }
        val removed = members.filterNot { it in selected }
        if (removed.isNotEmpty()) {
            // Delete from a copy: preserve member bytes, order and archive format.
            // A response file also avoids Windows command-line length limits.
            val response = workDir.resolve("remove.rsp")
            try {
                response.bufferedWriter().use { writer ->
                    for (member in removed) {
                        writer.write("\"")
                        writer.write(member.replace("\\", "\\\\").replace("\"", "\\\""))
                        writer.write("\"\n")
                    }
                }
                runTool(workDir, llvmAr.get(), listOf("--rsp-quoting=posix", "dsD", archiveName, "@${response.name}"))
            } finally {
                response.delete()
            }
        }
        logger.lifecycle("$name: retained ${selected.size}/${members.size} native archive members")
    }

    @TaskAction
    fun prepare() {
        val source = sourceArchive.get().asFile
        val output = preparedArchive.get().asFile
        val workDir = output.parentFile.apply { mkdirs() }
        val input = workDir.resolve("input.a")
        source.copyTo(input, overwrite = true)
        try {
            if (pruneArchive.get()) {
                prune(workDir, input.name)
            }
            runTool(workDir, llvmObjcopy.get(), symbolOptions.get() + listOf(input.name, output.name))
        } finally {
            input.delete()
        }
        logger.lifecycle("$name: ${source.length()} -> ${output.length()} bytes")
    }
}
