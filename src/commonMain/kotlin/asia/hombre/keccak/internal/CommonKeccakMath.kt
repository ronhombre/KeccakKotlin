/*
 * Copyright 2026 Ron Lauren Hombre
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

package asia.hombre.keccak.internal

import asia.hombre.keccak.KeccakConstants
import kotlin.experimental.or
import kotlin.jvm.JvmSynthetic

/**
 * As part of the master branch, this Keccak implementation has optimizations that make it relatively unreadable.
 * For a more understandable version, please view the standard branch.
 *
 * @author Ron Lauren Hombre
 */
internal object CommonKeccakMath {
    /**
     * THIS SECTION CONTAINS A SET OF FAST AND EFFICIENT ALGORITHMS TO MINIMIZE MEMORY COPYING.
     */

    @Suppress("NOTHING_TO_INLINE")
    @JvmSynthetic
    inline fun pad10n1Direct(bytes: ByteArray, offset: Int, flexiByte: FlexiByte) {
        bytes[offset] = flexiByte.toByte() or (0b1 shl (flexiByte.bitIndex + 1)).toByte()
        bytes[bytes.lastIndex] = bytes[bytes.lastIndex] or (-128).toByte()
    }

    /**
     * Gemini 4 Argon optimized to reduce memory usage. Please scrutinize if you have time.
     */
    @JvmSynthetic
    fun directPermute(state: LongArray) {
        require(state.size == 25) { "Matrix too small" } //JVM JIT guarantee

        repeat(24) { i ->
            // Theta Parity: Compute column parities into 5 CPU registers (g0..g4)
            var g0 = state[ 0] xor state[ 1] xor state[ 2] xor state[ 3] xor state[ 4]
            var g1 = state[ 5] xor state[ 6] xor state[ 7] xor state[ 8] xor state[ 9]
            var g2 = state[10] xor state[11] xor state[12] xor state[13] xor state[14]
            var g3 = state[15] xor state[16] xor state[17] xor state[18] xor state[19]
            var g4 = state[20] xor state[21] xor state[22] xor state[23] xor state[24]

            // Theta Effect: Convert parities C[0..4] into deltas D[0..4] in-register
            // Result mapping: D[0]=g0, D[1]=g2, D[2]=g3, D[3]=g4, D[4]=g5 (leaves g1 free)
            var g5 = g0
            g0 = g4 xor g1.rotateLeft(1) // D[0]
            g4 = g2 xor g4.rotateLeft(1) // D[3] (retires C[4])
            g2 = g5 xor g2.rotateLeft(1) // D[1] (retires C[2])
            g5 = g3 xor g5.rotateLeft(1) // D[4] (retires C[0])
            g3 = g1 xor g3.rotateLeft(1) // D[2] (retires C[3] and C[1])

            // Fused Theta + Rho + Pi: Walk the 24-cycle in-place using g1 as swap register
            state[ 0] = state[ 0] xor g0
            g1 = state[ 1] xor g0
            state[ 1] = (state[15] xor g4).rotateLeft(28)
            state[15] = (state[18] xor g4).rotateLeft(21)
            state[18] = (state[13] xor g3).rotateLeft(15)
            state[13] = (state[ 7] xor g2).rotateLeft(10)
            state[ 7] = (state[11] xor g3).rotateLeft(6)
            state[11] = (state[ 2] xor g0).rotateLeft(3)
            state[ 2] = (state[ 5] xor g2).rotateLeft(1)
            state[ 5] = (state[ 6] xor g2).rotateLeft(44)
            state[ 6] = (state[21] xor g5).rotateLeft(20)
            state[21] = (state[14] xor g3).rotateLeft(61)
            state[14] = (state[22] xor g5).rotateLeft(39)
            state[22] = (state[ 4] xor g0).rotateLeft(18)
            state[ 4] = (state[10] xor g3).rotateLeft(62)
            state[10] = (state[12] xor g3).rotateLeft(43)
            state[12] = (state[17] xor g4).rotateLeft(25)
            state[17] = (state[23] xor g5).rotateLeft(8)
            state[23] = (state[19] xor g4).rotateLeft(56)
            state[19] = (state[ 3] xor g0).rotateLeft(41)
            state[ 3] = (state[20] xor g5).rotateLeft(27)
            state[20] = (state[24] xor g5).rotateLeft(14)
            state[24] = (state[ 9] xor g2).rotateLeft(2)
            state[ 9] = (state[16] xor g4).rotateLeft(55)
            state[16] = (state[ 8] xor g2).rotateLeft(45)
            state[ 8] = g1.rotateLeft(36)

            // Chi + Iota: Update each independent 5-cycle in-place using g0 and g1
            g0 = state[ 0]; g1 = state[ 5]
            state[ 0] = g0 xor (g1.inv() and state[10]) xor KeccakConstants.ROUND[i]
            state[ 5] = g1 xor (state[10].inv() and state[15])
            state[10] = state[10] xor (state[15].inv() and state[20])
            state[15] = state[15] xor (state[20].inv() and g0)
            state[20] = state[20] xor (g0.inv() and g1)

            g0 = state[ 1]; g1 = state[ 6]
            state[ 1] = g0 xor (g1.inv() and state[11])
            state[ 6] = g1 xor (state[11].inv() and state[16])
            state[11] = state[11] xor (state[16].inv() and state[21])
            state[16] = state[16] xor (state[21].inv() and g0)
            state[21] = state[21] xor (g0.inv() and g1)

            g0 = state[ 2]; g1 = state[ 7]
            state[ 2] = g0 xor (g1.inv() and state[12])
            state[ 7] = g1 xor (state[12].inv() and state[17])
            state[12] = state[12] xor (state[17].inv() and state[22])
            state[17] = state[17] xor (state[22].inv() and g0)
            state[22] = state[22] xor (g0.inv() and g1)

            g0 = state[ 3]; g1 = state[ 8]
            state[ 3] = g0 xor (g1.inv() and state[13])
            state[ 8] = g1 xor (state[13].inv() and state[18])
            state[13] = state[13] xor (state[18].inv() and state[23])
            state[18] = state[18] xor (state[23].inv() and g0)
            state[23] = state[23] xor (g0.inv() and g1)

            g0 = state[ 4]; g1 = state[ 9]
            state[ 4] = g0 xor (g1.inv() and state[14])
            state[ 9] = g1 xor (state[14].inv() and state[19])
            state[14] = state[14] xor (state[19].inv() and state[24])
            state[19] = state[19] xor (state[24].inv() and g0)
            state[24] = state[24] xor (g0.inv() and g1)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    @JvmSynthetic
    inline fun getLongAt(source: ByteArray, x: Int, y: Int): Long {
        val offset = (x + (5 * y)) shl 3

        if(offset + 8 > source.size) return 0

        return (  source[offset    ].toLong() and 0xFF) or
                ((source[offset + 1].toLong() and 0xFF) shl  8) or
                ((source[offset + 2].toLong() and 0xFF) shl 16) or
                ((source[offset + 3].toLong() and 0xFF) shl 24) or
                ((source[offset + 4].toLong() and 0xFF) shl 32) or
                ((source[offset + 5].toLong() and 0xFF) shl 40) or
                ((source[offset + 6].toLong() and 0xFF) shl 48) or
                ( source[offset + 7].toLong()           shl 56)
    }

    @JvmSynthetic
    fun directMatrixToBytes(matrix: LongArray, destination: ByteArray) {
        require(matrix.size == 25) { "Matrix too small" } //JVM JIT guarantee

        var idx = 0
        for (offset in destination.indices step 8) {
            val v = matrix[idx]
            destination[offset]     =  v.toByte()
            destination[offset + 1] = (v ushr 8).toByte()
            destination[offset + 2] = (v ushr 16).toByte()
            destination[offset + 3] = (v ushr 24).toByte()
            destination[offset + 4] = (v ushr 32).toByte()
            destination[offset + 5] = (v ushr 40).toByte()
            destination[offset + 6] = (v ushr 48).toByte()
            destination[offset + 7] = (v ushr 56).toByte()

            idx += 5
            if (idx >= 25) idx -= 24
        }
    }

    /**
     * THIS SECTION CONTAINS EXTENDED FUNCTIONS USED FOR SP 800-185. NOT ALL OF THESE FUNCTIONS ARE DESCRIBED IN THE
     * SPECIAL PUBLICATION AS THEY ARE MADE BY THE AUTHOR OF THIS CODE.
     */
    @JvmSynthetic
    fun encodeToBytes(number: Long, appendOrPrepend: Boolean): ByteArray {
        val size = 8 - ((number or 1L).countLeadingZeroBits() shr 3)
        val n = (((64 - (number ushr 1).countLeadingZeroBits()) ushr 3) + 1).toByte()
        val bytes = ByteArray(size + 1)

        val offset = if (appendOrPrepend) 1 else 0
        bytes[if (appendOrPrepend) 0 else size] = n

        var temp = number
        for (i in (size - 1 + offset) downTo offset) {
            bytes[i] = temp.toByte()
            temp = temp ushr 8
        }
        return bytes
    }

    /**
     * Valid only for 0 <= x < 2^2040 because we only use 1 byte to encode n. (2^2040 = 2^(8 * 255))
     *
     * This validity condition is outlined in SP 800-185.
     *
     * Although in reality, reaching this value is unrealistic since we are limited to 2^64 with Long and before that
     * with 2^32 with Int, which is the unit for the size of byte arrays.
     */
    @JvmSynthetic
    fun leftEncode(x: Long): ByteArray = encodeToBytes(x, true)

    /**
     * Valid only for 0 <= x < 2^2040 because we only use 1 byte to encode n. (2^2040 = 2^(8 * 255))
     *
     * This validity condition is outlined in SP 800-185.
     *
     * Although in reality, reaching this value is unrealistic since we are limited to 2^64 with Long and before that
     * with 2^32 with Int, which is the unit for the size of byte arrays.
     */
    @JvmSynthetic
    fun rightEncode(x: Long): ByteArray = encodeToBytes(x, false)
}
