/*
 * Copyright 2025 Ron Lauren Hombre
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *        and included as LICENSE.txt in this Project.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package asia.hombre.keccak

import asia.hombre.keccak.internal.KeccakMath
import asia.hombre.keccak.internal.SplitByteArray
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlin.math.max
import kotlin.math.min

/**
 * A generator class for all the Keccak Hash Functions.
 *
 * @author Ron Lauren Hombre
 */
object KeccakHash {
    /**
     * Generate a hash based on the Keccak parameter.
     *
     * You should only use this if you know what you are doing. Please use the standard classes to create your hashes.
     *
     * @param parameters [KeccakParameter] of the SHA-3 Hash Function.
     * @param byteArray [ByteArray] of inputs.
     * @param lengthInBytes (Optional) Intended for the Extendable Hash Functions and the SHA-3 Derived Functions.
     * @param functionName (Optional) Used for SHA-3 Derived Functions namely: cSHAKE, KMAC, TupleHash, and ParallelHash.
     * @param customization (Optional) Used for SHA-3 Derived Functions namely: cSHAKE, KMAC, TupleHash, and ParallelHash.
     * @param key (Optional) Used strictly for KMAC.
     */
    @JvmStatic
    @Deprecated("Use the Standard API instead.", level = DeprecationLevel.WARNING)
    fun generate(parameters: KeccakParameter, byteArray: ByteArray, lengthInBytes: Int = parameters.minLength / 8, functionName: ByteArray = ByteArray(0), customization: ByteArray = ByteArray(0), key: ByteArray? = null, noPadding: Boolean = false): ByteArray {
        val paddedBytes = when(parameters) {
            KeccakParameter.CSHAKE_128,
            KeccakParameter.CSHAKE_256 -> {
                if(functionName.isEmpty() && customization.isEmpty())
                    KeccakMath.pad10n1(byteArray, parameters.BITRATE, KeccakParameter.SHAKE_128.SUFFIX)
                else KeccakMath.pad10n1(
                    byteArrayOf(
                        *(if(noPadding) ByteArray(0) else KeccakMath.padBytes(
                            byteArrayOf(
                                *KeccakMath.encodeStringBytes(functionName),
                                *KeccakMath.encodeStringBytes(customization)
                            ),
                            parameters.BYTERATE
                        )),
                        *byteArray,
                    ),
                    parameters.BITRATE,
                    parameters.SUFFIX
                )
            }
            KeccakParameter.KMAC_128,
            KeccakParameter.KMAC_256,
            KeccakParameter.KMACXOF_128,
            KeccakParameter.KMACXOF_256-> {
                if(key == null) throw RuntimeException("'key' required when using the ${parameters.name} parameter.")

                val length = if(parameters == KeccakParameter.KMACXOF_128 || parameters == KeccakParameter.KMACXOF_256)
                    0L
                else
                    lengthInBytes * 8L

                KeccakMath.pad10n1(
                    byteArrayOf(
                        *(if(noPadding) ByteArray(0) else KeccakMath.padBytes( //T
                            byteArrayOf(
                                *KeccakMath.encodeString("KMAC"),
                                *KeccakMath.encodeStringBytes(customization)
                            ),
                            parameters.BYTERATE
                        )),
                        *(if(noPadding) ByteArray(0) else KeccakMath.padBytes( //newX
                            byteArrayOf(
                                *KeccakMath.encodeStringBytes(key)
                            ),
                            parameters.BYTERATE
                        )),
                        *byteArray,
                        *KeccakMath.rightEncode(length),
                    ),
                    parameters.BITRATE,
                    parameters.SUFFIX
                )
            }
            else -> KeccakMath.pad10n1(byteArray, parameters.BITRATE, parameters.SUFFIX)
        }

        return generatePadded(parameters, paddedBytes, lengthInBytes)
    }

    /**
     * Generate a hash based on the Keccak parameter.
     *
     * @param parameters [KeccakParameter] of the SHA-3 Hash Function.
     * @param flexiByteArray [FlexiByteArray] of inputs that aren't byte-size.
     * @param lengthInBytes (Optional) Intended for the Extendable Hash Functions.
     */
    @JvmStatic
    fun generateFlex(parameters: KeccakParameter, flexiByteArray: FlexiByteArray, lengthInBytes: Int = parameters.minLength / 8): ByteArray {
        val paddedBytes = KeccakMath.pad10n1Flex(flexiByteArray + parameters.SUFFIX, parameters.BITRATE).toByteArray()

        return generatePadded(parameters, paddedBytes, lengthInBytes)
    }

    @JvmSynthetic
    internal fun generateDirect(parameter: KeccakParameter, chunks: Pair<Array<ByteArray>, Int>, suffix: FlexiByte, stateBuffer: SplitByteArray, state: Array<LongArray>) {
        val addAnother = chunks.second == parameter.BYTERATE
        val validatedChunks = Array<ByteArray>(chunks.first.size + (if(addAnother) 1 else 0)) { i ->
            if(addAnother && i == chunks.first.size) {
                ByteArray(parameter.BYTERATE)
            } else {
                chunks.first[i]
            }
        }

        KeccakMath.pad10n1Direct(validatedChunks[validatedChunks.lastIndex], (if(addAnother) 0 else chunks.second), suffix)

        validatedChunks.forEachIndexed { i, it ->
            stateBuffer.a = it

            for(x in 0..<5) for(y in 0..<5)
                state[x][y] = state[x][y] xor KeccakMath.getLongAt(stateBuffer, x, y, parameter.BYTERATE)

            KeccakMath.directPermute(state)
        }
    }

    @JvmSynthetic
    internal fun generateDirectOutput(parameter: KeccakParameter, length: Int, chunks: Pair<Array<ByteArray>, Int>, suffix: FlexiByte): ByteArray {
        val stateBuffer = SplitByteArray(ByteArray(parameter.BYTERATE), ByteArray(200 - parameter.BYTERATE))
        val state = Array(5) { LongArray(5) }

        generateDirect(parameter, chunks, suffix, stateBuffer, state)

        stateBuffer.a = ByteArray(parameter.BYTERATE)

        val output = ByteArray(length)
        var totalOutput = 0

        while (totalOutput < length) {
            KeccakMath.directMatrixToBytes(state, stateBuffer)

            val toCopy = min(length - totalOutput, parameter.BYTERATE)

            stateBuffer.a
                .copyInto(output, totalOutput, 0, toCopy) //We assume here that we will only see toCopy < BYTERATE if it's the last few bytes.
                .also { totalOutput += toCopy }

            if(totalOutput < length) KeccakMath.directPermute(state)
        }

        return output
    }

    @JvmSynthetic
    private fun generatePadded(parameters: KeccakParameter, paddedBytes: ByteArray, lengthInBytes: Int = parameters.minLength / 8): ByteArray {
        val outputLength = if(parameters.maxLength == 0)
            max(lengthInBytes, parameters.minLength / 8)
        else
            min(lengthInBytes, parameters.minLength / 8)

        //Absorption
        var inputOffset = 0
        var state = Array(5) { LongArray(5) }

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