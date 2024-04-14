package asia.hombre.keccak

import asia.hombre.keccak.internal.KeccakMath
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.math.min

/**
 * A class for streaming bytes of Keccak Hash Functions.
 *
 * Suitable for use with the Extendable Hash Functions(XOF).
 *
 * @constructor Generates a hash based on the [KeccakParameter] during absorption.
 * @author Ron Lauren Hombre
 */
@OptIn(ExperimentalUnsignedTypes::class, ExperimentalJsExport::class)
@JsExport
class KeccakByteStream(val parameters: KeccakParameter) {
    private var state = Array(5) { LongArray(5) }
    private var buffer: ByteArray = ByteArray(parameters.BYTERATE)
    private var index = -1
    private var outputted = 0
    private var isFirstSqueeze = true

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
        isFirstSqueeze = true
    }

    /**
     * Absorb a byte array into the Keccak sponge construction.
     */
    fun absorb(byteArray: ByteArray) {
        if(!isFirstSqueeze) reset()

        val paddedBytes = KeccakMath.pad10n1(byteArray, parameters.BITRATE, parameters.SUFFIX)

        //Absorption
        var inputOffset = 0

        while(inputOffset != paddedBytes.size) {
            val permutationState = KeccakMath.bytesToMatrix(paddedBytes.copyOfRange(inputOffset, min(paddedBytes.size, inputOffset + parameters.BYTERATE)))
            for(x in 0..<5)
                for(y in 0..<5)
                    state[x][y] = state[x][y] xor permutationState[x][y]
            state = KeccakMath.permute(state)
            inputOffset += parameters.BYTERATE
        }

        squeeze()
    }

    /**
     * Get the next byte.
     *
     * @return [Byte] - The next byte of the Keccak Hash.
     * @throws IndexOutOfBoundsException when the Keccak parameter is not extendable and the internal state runs out of bytes.
     */
    fun next(): Byte {
        val canContinue = (parameters.maxLength == 0 || outputted < (parameters.minLength / 8))
        if(++index >= parameters.BYTERATE && canContinue) {
            squeeze()
            index++
        }
        else if(!canContinue)
            throw IndexOutOfBoundsException("There is no further bytes to read. This is the limit of this Keccak parameter.")

        outputted++

        return buffer[index]
    }

    /**
     * Squeeze a new buffer from the state.
     */
    private fun squeeze() {
        if(!isFirstSqueeze)
            state = KeccakMath.permute(state)
        KeccakMath.matrixToBytes(state).copyInto(buffer, 0, 0, parameters.BYTERATE)
        index = -1
        isFirstSqueeze = false
    }
}