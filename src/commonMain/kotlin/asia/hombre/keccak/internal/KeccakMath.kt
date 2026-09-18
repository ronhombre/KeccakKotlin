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

package asia.hombre.keccak.internal

import asia.hombre.keccak.KeccakConstants
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.experimental.or
import kotlin.jvm.JvmSynthetic
import kotlin.math.max

/**
 * As part of the master branch, this Keccak implementation has optimizations that make it relatively unreadable.
 * For a more understandable version, please view the standard branch.
 *
 * @author Ron Lauren Hombre
 */
internal object KeccakMath {

    /**
     * Long to ByteArray conversion.
     */
    @JvmSynthetic
    fun longToBytes(long: Long): ByteArray {
        return byteArrayOf(
            ((long shr 56) and 0xFF).toByte(),
            ((long shr 48) and 0xFF).toByte(),
            ((long shr 40) and 0xFF).toByte(),
            ((long shr 32) and 0xFF).toByte(),
            ((long shr 24) and 0xFF).toByte(),
            ((long shr 16) and 0xFF).toByte(),
            ((long shr 8)  and 0xFF).toByte(),
            ( long         and 0xFF).toByte()
        )
    }

    /**
     * THIS SECTION CONTAINS A SET OF FAST AND EFFICIENT ALGORITHMS TO MINIMIZE MEMORY COPYING.
     */

    @JvmSynthetic
    fun pad10n1Direct(bytes: ByteArray, offset: Int, flexiByte: FlexiByte) {
        bytes[offset] = flexiByte.toByte() or (0b1 shl (flexiByte.bitIndex + 1)).toByte()
        bytes[bytes.lastIndex] = bytes[bytes.lastIndex] or (-128).toByte()
    }

    /**
     * An intermediate arena for temporary Keccak data.
     */
    @OptIn(ExperimentalAtomicApi::class)
    internal class GhostArena {
        val a = LongArray(5)
        val b = LongArray(25)

        private val inUse = AtomicBoolean(false)

        inline fun <T> use(block: () -> T): T {
            check(inUse.compareAndSet(expectedValue = false, newValue = true)) {
                """
                    Concurrent use of the same AbstractKeccakFunction instance detected.
                    
                    Each AbstractKeccakFunction and its input and output streams share a single
                    permute scratch arena (GhostArena). Two threads cannot permute() at the same
                    time on the same arena.
                    
                    To fix this, either:
                      - Call detachScratch() on the HashOutputStream (or HashInputStream) before
                        handing it to another thread, or
                      - Use a separate AbstractKeccakFunction instance per thread.
                """.trimIndent()
            }
            try {
                return block()
            } finally {
                a.fill(0)
                b.fill(0)
                inUse.store(false)
            }
        }
    }

    /**
     * I believe this is already quite close to the most optimal version but additional performance might be found in
     * re-ordering and simplifying the common operations. Please investigate if you have time.
     */
    @JvmSynthetic
    fun directPermute(state: LongArray, ghost: GhostArena) = ghost.use {
        repeat(24) { i ->
            //Theta (Parity Calculation) + Rho (Rotate bits) + Pi (Rearrange lanes)
            ghost.b[0] = state[0] xor state[1] xor state[2] xor state[3] xor state[4]
            ghost.b[1] = state[5] xor state[6] xor state[7] xor state[8] xor state[9]
            ghost.b[2] = state[10] xor state[11] xor state[12] xor state[13] xor state[14]
            ghost.b[3] = state[15] xor state[16] xor state[17] xor state[18] xor state[19]
            ghost.b[4] = state[20] xor state[21] xor state[22] xor state[23] xor state[24]

            //Reuse parts of the preliminary state to prevent the need to initialize another LongArray
            ghost.a[0] = ghost.b[4] xor ghost.b[1].rotateLeft(1)
            ghost.a[1] = ghost.b[0] xor ghost.b[2].rotateLeft(1)
            ghost.a[2] = ghost.b[1] xor ghost.b[3].rotateLeft(1)
            ghost.a[3] = ghost.b[2] xor ghost.b[4].rotateLeft(1)
            ghost.a[4] = ghost.b[3] xor ghost.b[0].rotateLeft(1)

            ghost.b[0] = (state[0] xor ghost.a[0])
            ghost.b[1] = (state[15] xor ghost.a[3]).rotateLeft(28)
            ghost.b[2] = (state[5] xor ghost.a[1]).rotateLeft(1)
            ghost.b[3] = (state[20] xor ghost.a[4]).rotateLeft(27)
            ghost.b[4] = (state[10] xor ghost.a[2]).rotateLeft(62)
            ghost.b[5] = (state[6] xor ghost.a[1]).rotateLeft(44)
            ghost.b[6] = (state[21] xor ghost.a[4]).rotateLeft(20)
            ghost.b[7] = (state[11] xor ghost.a[2]).rotateLeft(6)
            ghost.b[8] = (state[1] xor ghost.a[0]).rotateLeft(36)
            ghost.b[9] = (state[16] xor ghost.a[3]).rotateLeft(55)
            ghost.b[10] = (state[12] xor ghost.a[2]).rotateLeft(43)
            ghost.b[11] = (state[2] xor ghost.a[0]).rotateLeft(3)
            ghost.b[12] = (state[17] xor ghost.a[3]).rotateLeft(25)
            ghost.b[13] = (state[7] xor ghost.a[1]).rotateLeft(10)
            ghost.b[14] = (state[22] xor ghost.a[4]).rotateLeft(39)
            ghost.b[15] = (state[18] xor ghost.a[3]).rotateLeft(21)
            ghost.b[16] = (state[8] xor ghost.a[1]).rotateLeft(45)
            ghost.b[17] = (state[23] xor ghost.a[4]).rotateLeft(8)
            ghost.b[18] = (state[13] xor ghost.a[2]).rotateLeft(15)
            ghost.b[19] = (state[3] xor ghost.a[0]).rotateLeft(41)
            ghost.b[20] = (state[24] xor ghost.a[4]).rotateLeft(14)
            ghost.b[21] = (state[14] xor ghost.a[2]).rotateLeft(61)
            ghost.b[22] = (state[4] xor ghost.a[0]).rotateLeft(18)
            ghost.b[23] = (state[19] xor ghost.a[3]).rotateLeft(56)
            ghost.b[24] = (state[9] xor ghost.a[1]).rotateLeft(2)

            //Chi (XOR lanes) + Iota (Modify the first lane with a predefined value unique for each round)
            state[0] = ghost.b[0] xor (ghost.b[5].inv() and ghost.b[10]) xor KeccakConstants.ROUND[i]
            state[1] = ghost.b[1] xor (ghost.b[6].inv() and ghost.b[11])
            state[2] = ghost.b[2] xor (ghost.b[7].inv() and ghost.b[12])
            state[3] = ghost.b[3] xor (ghost.b[8].inv() and ghost.b[13])
            state[4] = ghost.b[4] xor (ghost.b[9].inv() and ghost.b[14])
            state[5] = ghost.b[5] xor (ghost.b[10].inv() and ghost.b[15])
            state[6] = ghost.b[6] xor (ghost.b[11].inv() and ghost.b[16])
            state[7] = ghost.b[7] xor (ghost.b[12].inv() and ghost.b[17])
            state[8] = ghost.b[8] xor (ghost.b[13].inv() and ghost.b[18])
            state[9] = ghost.b[9] xor (ghost.b[14].inv() and ghost.b[19])
            state[10] = ghost.b[10] xor (ghost.b[15].inv() and ghost.b[20])
            state[11] = ghost.b[11] xor (ghost.b[16].inv() and ghost.b[21])
            state[12] = ghost.b[12] xor (ghost.b[17].inv() and ghost.b[22])
            state[13] = ghost.b[13] xor (ghost.b[18].inv() and ghost.b[23])
            state[14] = ghost.b[14] xor (ghost.b[19].inv() and ghost.b[24])
            state[15] = ghost.b[15] xor (ghost.b[20].inv() and ghost.b[0])
            state[16] = ghost.b[16] xor (ghost.b[21].inv() and ghost.b[1])
            state[17] = ghost.b[17] xor (ghost.b[22].inv() and ghost.b[2])
            state[18] = ghost.b[18] xor (ghost.b[23].inv() and ghost.b[3])
            state[19] = ghost.b[19] xor (ghost.b[24].inv() and ghost.b[4])
            state[20] = ghost.b[20] xor (ghost.b[0].inv() and ghost.b[5])
            state[21] = ghost.b[21] xor (ghost.b[1].inv() and ghost.b[6])
            state[22] = ghost.b[22] xor (ghost.b[2].inv() and ghost.b[7])
            state[23] = ghost.b[23] xor (ghost.b[3].inv() and ghost.b[8])
            state[24] = ghost.b[24] xor (ghost.b[4].inv() and ghost.b[9])
        }
    }

    @JvmSynthetic
    fun getLongAt(source: ByteArray, x: Int, y: Int): Long {
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
    fun computeForNGivenX(x: Long): Byte = (((64 - (x ushr 1).countLeadingZeroBits()) ushr 3) + 1).toByte()

    @JvmSynthetic
    fun encodeToBytes(number: Long, min: Int = 0): ByteArray =
        longToBytes(number)
            .copyOfRange(8 - max((Long.SIZE_BITS - number.countLeadingZeroBits() + 7) shr 3, min), 8)

    /**
     * Valid only for 0 <= x < 2^2040 because we only use 1 byte to encode n. (2^2040 = 2^(8 * 255))
     *
     * This validity condition is outlined in SP 800-185.
     *
     * Although in reality, reaching this value is unrealistic since we are limited to 2^64 with Long and before that
     * with 2^32 with Int, which is the unit for the size of byte arrays.
     */
    @JvmSynthetic
    fun leftEncode(x: Long): ByteArray {
        return byteArrayOf(computeForNGivenX(x), *encodeToBytes(x, 1))
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
    fun rightEncode(x: Long): ByteArray {
        return byteArrayOf(*encodeToBytes(x, 1), computeForNGivenX(x))
    }
}