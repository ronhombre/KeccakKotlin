package asia.hombre.keccak

import asia.hombre.keccak.internal.KeccakMath
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.math.max
import kotlin.math.min

/**
 * A generator class for all the Keccak Hash Functions.
 *
 * @author Ron Lauren Hombre
 */
@OptIn(ExperimentalUnsignedTypes::class, ExperimentalJsExport::class)
@JsExport
sealed class KeccakHash { //TODO: Transfer to new KMM library project.
    companion object {
        /**
         * Generate a hash based on the Keccak parameter.
         *
         * @param parameters [KeccakParameter] of the SHA-3 Hash Function.
         * @param byteArray [ByteArray] of inputs.
         * @param lengthInBytes (Optional) Intended for the Extendable Hash Functions.
         */
        fun generate(parameters: KeccakParameter, byteArray: ByteArray, lengthInBytes: Int = 0): ByteArray {
            val outputLength = if(parameters.maxLength == 0)
                max(lengthInBytes, parameters.minLength / 8)
            else
                parameters.minLength / 8

            val paddedBytes = KeccakMath.pad10n1Flex(FlexiByteArray(byteArray) + parameters.SUFFIX, parameters.BITRATE).toByteArray()

            //Absorption
            var inputOffset = 0
            var state = Array(5) { ULongArray(5) }

            while(inputOffset != paddedBytes.size) {
                val permutationState = KeccakMath.bytesToMatrix(paddedBytes.copyOfRange(inputOffset, min(paddedBytes.size, inputOffset + parameters.BYTERATE)))
                for(x in 0..<5)
                    for(y in 0..<5)
                        state[x][y] = state[x][y] xor permutationState[x][y]
                state = KeccakMath.permute(state)
                inputOffset += parameters.BYTERATE
            }

            //Squeezing
            var outputOffset = 0
            val outputBytes = ByteArray(outputLength)

            while(true) {
                KeccakMath.matrixToBytes(state).copyInto(outputBytes, outputOffset, 0, min(parameters.BYTERATE, outputLength - outputOffset))
                outputOffset += parameters.BYTERATE
                if((outputOffset + 1) >= outputLength) break
                state = KeccakMath.permute(state)
            }

            return outputBytes
        }
    }
}