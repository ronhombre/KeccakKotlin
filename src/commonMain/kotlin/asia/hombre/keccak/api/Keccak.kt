package asia.hombre.keccak.api

import asia.hombre.keccak.KeccakConstants
import asia.hombre.keccak.internal.KeccakMath
import asia.hombre.keccak.streams.HashInputStream

internal fun HashInputStream.addCSHAKEPrePadding(functionName: ByteArray, customization: ByteArray) {
    write(KeccakMath.leftEncode(this.PARAMETER.BYTERATE.toLong()))
    write(KeccakMath.leftEncode(functionName.size.toLong() * 8))
    write(functionName)
    write(KeccakMath.leftEncode(customization.size.toLong() * 8))
    write(customization)
    forcePermute()
}

internal fun HashInputStream.addKMACPrePadding(key: ByteArray, customization: ByteArray) {
    write(KeccakMath.leftEncode(this.PARAMETER.BYTERATE.toLong()))
    write(KeccakMath.leftEncode(32L)) //KMAC_BYTES.size * 8L
    write(KeccakConstants.KMAC_BYTES)
    write(KeccakMath.leftEncode(customization.size.toLong() * 8))
    write(customization)
    forcePermute()

    write(KeccakMath.leftEncode(this.PARAMETER.BYTERATE.toLong()))
    write(KeccakMath.leftEncode(key.size.toLong() * 8))
    write(key)
    forcePermute()
}