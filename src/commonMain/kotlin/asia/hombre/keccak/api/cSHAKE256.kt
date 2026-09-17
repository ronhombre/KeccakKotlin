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

import asia.hombre.keccak.KeccakParameter
import asia.hombre.keccak.internal.AbstractKeccakFunction
import asia.hombre.keccak.internal.FlexiByte
import asia.hombre.keccak.internal.KeccakMath
import asia.hombre.keccak.streams.HashInputStream
import kotlin.jvm.JvmName

/**
 * cSHAKE256 Hash Function as defined in SP 800-185.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
@Suppress("ClassName")
class cSHAKE256(
    /**
     * The number of bytes to output on `digest()` or `stream()`.
     */
    override val outputLength: Int = PARAMETER.minLength / 8,
    /**
     * Function name customization. This acts like a salt.
     */
    val functionName: ByteArray = ByteArray(0),
    /**
     * Generic customization. This acts like a salt.
     */
    val customization: ByteArray = ByteArray(0)
): AbstractKeccakFunction(PARAMETER.BYTERATE, PARAMETER, outputLength) {
    override fun newInputStream(ghostArena: KeccakMath.GhostArena): HashInputStream =
        newGhostedInputStream(functionName, customization, ghostArena)

    companion object {
        @get:JvmName("getParameter")
        val PARAMETER = KeccakParameter.CSHAKE_256

        /**
         * Creates a [HashInputStream] for this parameter.
         *
         * @param functionName Function name customization. This acts like a salt.
         * @param customization Generic customization. This acts like a salt.
         * @return [HashInputStream]
         * @since 2.0.0
         */
        fun newInputStream(
            functionName: ByteArray = ByteArray(0),
            customization: ByteArray = ByteArray(0)
        ): HashInputStream =
            newGhostedInputStream(functionName, customization, KeccakMath.GhostArena())

        internal fun newGhostedInputStream(
            functionName: ByteArray,
            customization: ByteArray,
            ghost: KeccakMath.GhostArena
        ): HashInputStream = object : HashInputStream(PARAMETER, ghost = ghost) {
            override val SUFFIX: FlexiByte
                get() =
                    if(functionName.size + customization.size != 0)
                        Companion.PARAMETER.SUFFIX
                    else
                        KeccakParameter.SHAKE_256.SUFFIX

            init {
                if(functionName.size + customization.size != 0) {
                    addCSHAKEPrePadding(functionName, customization)
                }
            }
        }
    }
}