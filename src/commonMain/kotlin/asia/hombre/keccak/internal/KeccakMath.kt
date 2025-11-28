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
            ((long shr 8) and 0xFF).toByte(),
            (long and 0xFF).toByte()
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
     * I believe this is already quite close to the most optimal version but additional performance might be found in
     * re-ordering and simplifying the common operations. Please investigate if you have time.
     */
    @JvmSynthetic
    fun directPermute(state: Array<LongArray>) {
        val c = LongArray(5)
        val d = LongArray(5)

        val preliminaryState = Array<LongArray>(5) { LongArray(5) }

        for(i in 0..<24) {
            //Theta (Parity Calculation) + Rho (Rotate bits) + Pi (Rearrange lanes)
            c[0] = state[0][0] xor state[0][1] xor state[0][2] xor state[0][3] xor state[0][4]
            c[1] = state[1][0] xor state[1][1] xor state[1][2] xor state[1][3] xor state[1][4]
            c[2] = state[2][0] xor state[2][1] xor state[2][2] xor state[2][3] xor state[2][4]
            c[3] = state[3][0] xor state[3][1] xor state[3][2] xor state[3][3] xor state[3][4]
            c[4] = state[4][0] xor state[4][1] xor state[4][2] xor state[4][3] xor state[4][4]

            d[0] = c[4] xor c[1].rotateLeft(1)
            d[1] = c[0] xor c[2].rotateLeft(1)
            d[2] = c[1] xor c[3].rotateLeft(1)
            d[3] = c[2] xor c[4].rotateLeft(1)
            d[4] = c[3] xor c[0].rotateLeft(1)

            preliminaryState[0][0] = (state[0][0] xor d[0])
            preliminaryState[0][1] = (state[3][0] xor d[3]).rotateLeft(28)
            preliminaryState[0][2] = (state[1][0] xor d[1]).rotateLeft(1)
            preliminaryState[0][3] = (state[4][0] xor d[4]).rotateLeft(27)
            preliminaryState[0][4] = (state[2][0] xor d[2]).rotateLeft(62)
            preliminaryState[1][0] = (state[1][1] xor d[1]).rotateLeft(44)
            preliminaryState[1][1] = (state[4][1] xor d[4]).rotateLeft(20)
            preliminaryState[1][2] = (state[2][1] xor d[2]).rotateLeft(6)
            preliminaryState[1][3] = (state[0][1] xor d[0]).rotateLeft(36)
            preliminaryState[1][4] = (state[3][1] xor d[3]).rotateLeft(55)
            preliminaryState[2][0] = (state[2][2] xor d[2]).rotateLeft(43)
            preliminaryState[2][1] = (state[0][2] xor d[0]).rotateLeft(3)
            preliminaryState[2][2] = (state[3][2] xor d[3]).rotateLeft(25)
            preliminaryState[2][3] = (state[1][2] xor d[1]).rotateLeft(10)
            preliminaryState[2][4] = (state[4][2] xor d[4]).rotateLeft(39)
            preliminaryState[3][0] = (state[3][3] xor d[3]).rotateLeft(21)
            preliminaryState[3][1] = (state[1][3] xor d[1]).rotateLeft(45)
            preliminaryState[3][2] = (state[4][3] xor d[4]).rotateLeft(8)
            preliminaryState[3][3] = (state[2][3] xor d[2]).rotateLeft(15)
            preliminaryState[3][4] = (state[0][3] xor d[0]).rotateLeft(41)
            preliminaryState[4][0] = (state[4][4] xor d[4]).rotateLeft(14)
            preliminaryState[4][1] = (state[2][4] xor d[2]).rotateLeft(61)
            preliminaryState[4][2] = (state[0][4] xor d[0]).rotateLeft(18)
            preliminaryState[4][3] = (state[3][4] xor d[3]).rotateLeft(56)
            preliminaryState[4][4] = (state[1][4] xor d[1]).rotateLeft(2)

            //Chi (XOR lanes) + Iota (Modify the first lane with a predefined value unique for each round)
            state[0][0] = preliminaryState[0][0] xor (preliminaryState[1][0].inv() and preliminaryState[2][0]) xor KeccakConstants.ROUND[i]
            state[0][1] = preliminaryState[0][1] xor (preliminaryState[1][1].inv() and preliminaryState[2][1])
            state[0][2] = preliminaryState[0][2] xor (preliminaryState[1][2].inv() and preliminaryState[2][2])
            state[0][3] = preliminaryState[0][3] xor (preliminaryState[1][3].inv() and preliminaryState[2][3])
            state[0][4] = preliminaryState[0][4] xor (preliminaryState[1][4].inv() and preliminaryState[2][4])
            state[1][0] = preliminaryState[1][0] xor (preliminaryState[2][0].inv() and preliminaryState[3][0])
            state[1][1] = preliminaryState[1][1] xor (preliminaryState[2][1].inv() and preliminaryState[3][1])
            state[1][2] = preliminaryState[1][2] xor (preliminaryState[2][2].inv() and preliminaryState[3][2])
            state[1][3] = preliminaryState[1][3] xor (preliminaryState[2][3].inv() and preliminaryState[3][3])
            state[1][4] = preliminaryState[1][4] xor (preliminaryState[2][4].inv() and preliminaryState[3][4])
            state[2][0] = preliminaryState[2][0] xor (preliminaryState[3][0].inv() and preliminaryState[4][0])
            state[2][1] = preliminaryState[2][1] xor (preliminaryState[3][1].inv() and preliminaryState[4][1])
            state[2][2] = preliminaryState[2][2] xor (preliminaryState[3][2].inv() and preliminaryState[4][2])
            state[2][3] = preliminaryState[2][3] xor (preliminaryState[3][3].inv() and preliminaryState[4][3])
            state[2][4] = preliminaryState[2][4] xor (preliminaryState[3][4].inv() and preliminaryState[4][4])
            state[3][0] = preliminaryState[3][0] xor (preliminaryState[4][0].inv() and preliminaryState[0][0])
            state[3][1] = preliminaryState[3][1] xor (preliminaryState[4][1].inv() and preliminaryState[0][1])
            state[3][2] = preliminaryState[3][2] xor (preliminaryState[4][2].inv() and preliminaryState[0][2])
            state[3][3] = preliminaryState[3][3] xor (preliminaryState[4][3].inv() and preliminaryState[0][3])
            state[3][4] = preliminaryState[3][4] xor (preliminaryState[4][4].inv() and preliminaryState[0][4])
            state[4][0] = preliminaryState[4][0] xor (preliminaryState[0][0].inv() and preliminaryState[1][0])
            state[4][1] = preliminaryState[4][1] xor (preliminaryState[0][1].inv() and preliminaryState[1][1])
            state[4][2] = preliminaryState[4][2] xor (preliminaryState[0][2].inv() and preliminaryState[1][2])
            state[4][3] = preliminaryState[4][3] xor (preliminaryState[0][3].inv() and preliminaryState[1][3])
            state[4][4] = preliminaryState[4][4] xor (preliminaryState[0][4].inv() and preliminaryState[1][4])
        }

        //Clear arrays after use
        c.fill(0)
        d.fill(0)
        preliminaryState.forEach { it.fill(0) }
    }

    @JvmSynthetic
    fun getLongAt(source: SplitByteArray, x: Int, y: Int, except: Int): Long {
        val offset = (x + (5 * y)) shl 3

        if((offset + 7) > except) return 0L

        return (source[offset].toLong() and 0xFF) or
                ((source[offset + 1].toLong() and 0xFF) shl 8)   or
                ((source[offset + 2].toLong() and 0xFF) shl 16)  or
                ((source[offset + 3].toLong() and 0xFF) shl 24)  or
                ((source[offset + 4].toLong() and 0xFF) shl 32)  or
                ((source[offset + 5].toLong() and 0xFF) shl 40)  or
                ((source[offset + 6].toLong() and 0xFF) shl 48)  or
                (source[offset + 7].toLong() shl 56)
    }

    @JvmSynthetic
    fun directMatrixToBytes(matrix: Array<LongArray>, destination: SplitByteArray) {
        for(x in 0..<5)
            for(y in 0..<5)
                directLongToBytes(matrix[x][y], destination, (x + (5 * y)) shl 3)
    }

    @JvmSynthetic
    fun directLongToBytes(long: Long, destination: SplitByteArray, offset: Int) {
        require(offset + 7 < destination.size) { "Out of bounds" }
        destination[offset] = (long and 0xFF).toByte()
        destination[offset + 1] = ((long shr 8) and 0xFF).toByte()
        destination[offset + 2] = ((long shr 16) and 0xFF).toByte()
        destination[offset + 3] = ((long shr 24) and 0xFF).toByte()
        destination[offset + 4] = ((long shr 32) and 0xFF).toByte()
        destination[offset + 5] = ((long shr 40) and 0xFF).toByte()
        destination[offset + 6] = ((long shr 48) and 0xFF).toByte()
        destination[offset + 7] = ((long shr 56) and 0xFF).toByte()
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