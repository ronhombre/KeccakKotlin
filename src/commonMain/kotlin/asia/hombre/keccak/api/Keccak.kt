package asia.hombre.keccak.api

import asia.hombre.keccak.KeccakConstants
import asia.hombre.keccak.internal.AbstractKeccakFunction
import asia.hombre.keccak.internal.KeccakMath
import asia.hombre.keccak.streams.HashInputStream

internal fun AbstractKeccakFunction.addCSHAKEPrePadding(functionName: ByteArray, customization: ByteArray) {
    update(KeccakMath.leftEncode(parameter.BYTERATE.toLong()))
    update(KeccakMath.leftEncode(functionName.size.toLong() * 8))
    update(functionName)
    update(KeccakMath.leftEncode(customization.size.toLong() * 8))
    update(customization)
    skipToNextChunk()
}

internal fun HashInputStream.addCSHAKEPrePadding(functionName: ByteArray, customization: ByteArray) {
    write(KeccakMath.leftEncode(this.PARAMETER.BYTERATE.toLong()))
    write(KeccakMath.leftEncode(functionName.size.toLong() * 8))
    write(functionName)
    write(KeccakMath.leftEncode(customization.size.toLong() * 8))
    write(customization)
    forcePermute()
}

internal fun AbstractKeccakFunction.addKMACPrePadding(key: ByteArray, customization: ByteArray) {
    update(KeccakMath.leftEncode(parameter.BYTERATE.toLong()))
    update(KeccakMath.leftEncode(32L)) //KMAC_BYTES.size * 8L
    update(KeccakConstants.KMAC_BYTES)
    update(KeccakMath.leftEncode(customization.size.toLong() * 8))
    update(customization)
    skipToNextChunk()

    update(KeccakMath.leftEncode(parameter.BYTERATE.toLong()))
    update(KeccakMath.leftEncode(key.size.toLong() * 8))
    update(key)
    skipToNextChunk()
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