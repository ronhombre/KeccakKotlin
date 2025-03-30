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

import asia.hombre.keccak.KeccakConstants
import asia.hombre.keccak.KeccakHash
import asia.hombre.keccak.KeccakParameter
import asia.hombre.keccak.internal.AbstractKeccakFunction
import asia.hombre.keccak.internal.KeccakMath
import asia.hombre.keccak.streams.HashInputStream
import asia.hombre.keccak.streams.HashOutputStream
import kotlin.jvm.JvmName

/**
 * KMACXOF256 Hash Function as defined in SP 800-185.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
class KMACXOF256(
    /**
     * The key.
     *
     * This should be longer than the required bit security strength. For this parameter, the key should at least be 32
     * bytes or 256 bits.
     */
    key: ByteArray,
    /**
     * The number of bytes to output on `digest()` or `stream()`.
     *
     * This produces an extendable hash so different lengths will produce the same output.
     */
    val outputLength: Int = PARAMETER.minLength / 8,
    /**
     * Generic customization. This acts like a salt.
     */
    val customization: ByteArray = ByteArray(0)): AbstractKeccakFunction(PARAMETER.BYTERATE) {
    override val parameter: KeccakParameter = PARAMETER

    private val PADDING = prePadding(key, customization)

    init {
        super.update(PADDING)
    }

    override fun addLast(): ByteArray = KeccakConstants.KMACXOF_RIGHT_ENCODED

    override fun computeDigest(chunks: Pair<Array<ByteArray>, Int>): ByteArray =
        KeccakHash.generateDirectOutput(PARAMETER, outputLength, chunks, parameter.SUFFIX)
            .also { super.update(PADDING) }

    override fun computeAsHashStream(chunks: Pair<Array<ByteArray>, Int>): HashOutputStream =
        HashOutputStream(parameter, PARAMETER.SUFFIX, chunks, parameter.maxLength / 8)
            .also { super.update(PADDING) }

    companion object {
        @get:JvmName("getParameter")
        val PARAMETER = KeccakParameter.KMACXOF_256

        /**
         * Creates a [HashInputStream] for this parameter.
         *
         * @param key This should be longer than the required bit security strength. For this parameter, the key should
         * at least be 32 bytes or 256 bits.
         * @param customization Generic customization. This acts like a salt.
         * @return [HashInputStream]
         * @since 2.0.0
         */
        fun newInputStream(
            key: ByteArray,
            customization: ByteArray = ByteArray(0)): HashInputStream = object : HashInputStream(PARAMETER, PARAMETER.maxLength / 8) {
            init {
                super.write(prePadding(key, customization))
            }

            override fun beforeClose() {
                super.write(KeccakConstants.KMACXOF_RIGHT_ENCODED)
            }
        }

        private fun prePadding(key: ByteArray, customization: ByteArray): ByteArray = byteArrayOf(
            *KeccakMath.padBytes( //T
                byteArrayOf(
                    *KeccakConstants.KMAC_ENCODED,
                    *KeccakMath.encodeStringBytes(customization)
                ),
                PARAMETER.BYTERATE
            ),
            *KeccakMath.padBytes( //newX
                byteArrayOf(
                    *KeccakMath.encodeStringBytes(key)
                ),
                PARAMETER.BYTERATE
            )
        )
    }
}