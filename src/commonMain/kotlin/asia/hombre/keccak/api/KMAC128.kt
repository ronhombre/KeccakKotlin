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

package asia.hombre.keccak.api

import asia.hombre.keccak.KeccakHash
import asia.hombre.keccak.KeccakParameter
import asia.hombre.keccak.internal.AbstractKeccakFunction
import asia.hombre.keccak.internal.KeccakMath
import asia.hombre.keccak.streams.HashInputStream
import asia.hombre.keccak.streams.HashOutputStream
import kotlin.jvm.JvmName

/**
 * KMAC128 Hash Function as defined in SP 800-185.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
class KMAC128(
    /**
     * The key.
     *
     * This should be longer than the required bit security strength. For this parameter, the key should at least be 16
     * bytes or 128 bits.
     */
    private val key: ByteArray,
    /**
     * The number of bytes to output on `digest()` or `stream()`.
     *
     * Even with the same inputs, a different output length will produce a different hash for this parameter.
     */
    val outputLength: Int = PARAMETER.minLength / 8,
    /**
     * Generic customization. This acts like a salt.
     */
    val customization: ByteArray = ByteArray(0)): AbstractKeccakFunction(PARAMETER.BYTERATE) {
    override val parameter: KeccakParameter = PARAMETER

    init {
        addKMACPrePadding(key, customization)
    }

    override fun addLast(): ByteArray = KeccakMath.rightEncode(outputLength * 8L)

    override fun computeDigest(chunks: Pair<Array<ByteArray>, Int>): ByteArray =
        KeccakHash.generateDirectOutput(PARAMETER, outputLength, chunks, parameter.SUFFIX)
            .also { addKMACPrePadding(key, customization) }

    override fun computeAsHashStream(chunks: Pair<Array<ByteArray>, Int>): HashOutputStream =
        HashOutputStream(parameter, PARAMETER.SUFFIX, chunks, outputLength)
            .also { addKMACPrePadding(key, customization) }

    companion object {
        @get:JvmName("getParameter")
        val PARAMETER = KeccakParameter.KMAC_128

        /**
         * Creates a [HashInputStream] for this parameter.
         *
         * @param key This should be longer than the required bit security strength. For this parameter, the key should
         * at least be 16 bytes or 128 bits.
         * @param outputLength The number of bytes to output on `close()`. Even with the same inputs, a different output
         * length will produce a different hash for this parameter.
         * @param customization Generic customization. This acts like a salt.
         * @return [HashInputStream]
         * @since 2.0.0
         */
        fun newInputStream(
            key: ByteArray,
            outputLength: Int = PARAMETER.minLength / 8,
            customization: ByteArray = ByteArray(0)): HashInputStream = object : HashInputStream(PARAMETER, outputLength) {
                init {
                    addKMACPrePadding(key, customization)
                }

            override fun beforeClose() {
                super.write(KeccakMath.rightEncode(outputLength * 8L))
            }
        }
    }
}