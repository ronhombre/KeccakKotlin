@file:OptIn(ExperimentalForeignApi::class)

package asia.hombre.keccak

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.posix.getenv

internal actual fun readTestResource(path: String): ByteArray {
    if (SystemFileSystem.exists(Path(path))) return readFileSystemTestResource(path)

    val projectDir = getenv("KECCAK_TEST_PROJECT_DIR")?.toKString()?.takeIf { it.isNotEmpty() }
    val resolvedPath = if (projectDir != null) "$projectDir/$path" else path
    return readFileSystemTestResource(resolvedPath)
}
