@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package asia.hombre.keccak

internal actual fun readTestResource(path: String): ByteArray {
    if (isNodeJs()) return readFileSystemTestResource(path)

    val contents = readBrowserTestResource(path)
    return ByteArray(contents.length) { contents[it].code.toByte() }
}

private fun isNodeJs(): Boolean =
    js("typeof process !== 'undefined' && process.versions != null && process.versions.node != null")

private fun readBrowserTestResource(path: String): String = js("""(() => {
    const request = new XMLHttpRequest();
    request.open('GET', '/base/' + path, false);
    // Preserve every byte while keeping the common test API synchronous.
    request.overrideMimeType('text/plain; charset=x-user-defined');
    request.send();
    if (request.status !== 200) {
        throw new Error('Cannot load test resource ' + path + ': HTTP ' + request.status);
    }
    return request.responseText;
})()""")
