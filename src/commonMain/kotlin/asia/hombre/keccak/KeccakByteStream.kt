package asia.hombre.keccak

import asia.hombre.keccak.internal.KeccakMath
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.math.min

/**
 * A class for streaming bytes of Keccak Hash Functions.
 *
 * Suitable for use with the Extendable Hash Functions(XOF).
 *
 * @constructor Generates a hash based on the [KeccakParameter] during absorption.
 * @author Ron Lauren Hombre
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class KeccakByteStream(val parameters: KeccakParameter) {
    private var state = Array(5) { LongArray(5) }
    private var buffer: ByteArray = ByteArray(parameters.BYTERATE)
    private var absorbBuffer: ByteArray = ByteArray(parameters.BYTERATE)
    private var absorbOffset: Int = 0
    private var index = -1 //This negative 1 identifies that this stream has not been squeezed yet.
    private var outputted = 0

    val hasNext: Boolean
        get() {
            return (index < parameters.BYTERATE) || (parameters.maxLength == 0 || outputted < (parameters.maxLength / 8))
        }

    /**
     * Reset the internal state of the byte stream.
     *
     * This still keeps the supplied parameter during initialization.
     */
    fun reset() {
        state = Array(5) { LongArray(5) }
        buffer = ByteArray(parameters.BYTERATE)
        absorbBuffer = ByteArray(parameters.BYTERATE)
        absorbOffset = 0
        index = -1
        outputted = 0
    }

    /**
     * Absorb a byte array into the Keccak sponge construction.
     *
     * This method can be called multiple times and each byte array is concatenated after the bytes that preceded them.
     */
    fun absorb(byteArray: ByteArray) {
        if (index >= 0) throw IllegalStateException("This KeccakByteStream has already been squeezed.")

        var inputOffset = 0

        while (inputOffset < byteArray.size) {
            val remainingBytes = byteArray.size - inputOffset
            val absorbCapacityLeft = absorbBuffer.size - absorbOffset
            val bytesToCopy = min(absorbCapacityLeft, remainingBytes)

            byteArray.copyInto(absorbBuffer, absorbOffset, inputOffset, inputOffset + bytesToCopy)
            absorbOffset += bytesToCopy
            inputOffset += bytesToCopy

            if (absorbOffset == absorbBuffer.size) {
                val permutationState = KeccakMath.bytesToMatrix(absorbBuffer)
                for (x in 0 until 5)
                    for (y in 0 until 5)
                        state[x][y] = state[x][y] xor permutationState[x][y]

                state = KeccakMath.permute(state)
                absorbOffset = 0
            }
        }
    }

    /**
     * Absorbs a singular byte into the sponge construction.
     *
     * This method concatenates this byte after the bytes that preceded it.
     *
     * Equivalent to: `absorb(byteArrayOf(byte))`
     */
    @JsName("absorbByte")
    fun absorb(byte: Byte) {
        absorb(byteArrayOf(byte))
    }

    /**
     * Get the next byte.
     *
     * @return [Byte] - The next byte of the Keccak Hash.
     * @throws IndexOutOfBoundsException when the Keccak parameter is not extendable and the internal state runs out of bytes.
     */
    fun next(): Byte {
        if (index >= parameters.BYTERATE || index < 0) {
            if (parameters.maxLength != 0 && outputted >= parameters.minLength / 8)
                throw IndexOutOfBoundsException(
                    "There are no further bytes to read. This is the limit of this Keccak parameter."
                )
            squeeze()
        }

        return buffer[index++].also { outputted++ }
    }

    /**
     * Squeeze a new buffer from the state.
     */
    private fun squeeze() {
        //Absorb the rest of the bytes.
        if(index < 0) absorb(KeccakMath.supplyPadding(absorbOffset, parameters.BITRATE, parameters.SUFFIX))
        //Permute the internal state if more bytes are needed.
        else state = KeccakMath.permute(state)

        KeccakMath
            .matrixToBytes(state)
            .copyInto(buffer, 0, 0, parameters.BYTERATE)

        index = 0
    }
}