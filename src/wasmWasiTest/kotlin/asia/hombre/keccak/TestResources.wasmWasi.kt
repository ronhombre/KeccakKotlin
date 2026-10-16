package asia.hombre.keccak

// Resolve against the virtual fixture mount configured by node-preopens.cjs.
internal actual fun readTestResource(path: String): ByteArray = readFileSystemTestResource("/$path")
