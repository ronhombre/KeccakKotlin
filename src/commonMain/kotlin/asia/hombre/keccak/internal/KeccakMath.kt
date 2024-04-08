package asia.hombre.keccak.internal

import asia.hombre.keccak.FlexiByte
import asia.hombre.keccak.FlexiByteArray
import asia.hombre.keccak.KeccakConstants
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.jvm.JvmSynthetic

/**
 * As part of the standard branch, this Keccak implementation is naive and unoptimized. For an optimized but less
 * understandable version, please view the master branch.
 */
@OptIn(ExperimentalUnsignedTypes::class, ExperimentalJsExport::class)
@JsExport
internal class KeccakMath {
    internal companion object {
        /*fun pad10n1(bytes: ByteArray, multiple: Int): ByteArray {
            val paddedBytes = ByteArray(bytes.size + (multiple - (bytes.size % multiple)))

            bytes.copyInto(paddedBytes)
            paddedBytes[bytes.size] = 0b10000000u.toByte()
            paddedBytes[paddedBytes.lastIndex] = (paddedBytes[paddedBytes.lastIndex].toUInt() or 0b1u).toByte()

            return paddedBytes
        }*/
        /**
         * Pad a bit 1 and n amount of bit 0 then end with another bit 1.
         */
        @JvmSynthetic
        fun pad10n1Flex(bytes: FlexiByteArray, multiple: Int): FlexiByteArray {
            val totalBits = ((bytes.size - 1) * 8) + bytes.bitIndex + 1
            var zeroesN = multiple - ((totalBits + 2) % multiple)

            if (zeroesN == multiple) zeroesN = 0

            val paddedBytes = bytes + FlexiByte.fromString("1")
            val fullZeroBytes = zeroesN / 8

            if (fullZeroBytes > 0) {
                paddedBytes.extend(fullZeroBytes)

                for (i in (paddedBytes.size - fullZeroBytes) until paddedBytes.size)
                    paddedBytes[i] = 0
            }

            if (paddedBytes.bitIndex == 7)
                paddedBytes.extend(1)

            paddedBytes.moveBitIndex((paddedBytes.bitIndex + 1) % 8)
            paddedBytes[paddedBytes.size - 1] = (paddedBytes[paddedBytes.size - 1].toInt() or (1 shl 7)).toByte()

            return paddedBytes
        }

        /**
         * Parity calculation.
         */
        @JvmSynthetic
        fun theta(state: Array<ULongArray>): Array<ULongArray> {
            val c = ULongArray(5)
            val d = ULongArray(5)
            val newState = Array(5) { ULongArray(5) }
            for(x in c.indices)
                c[x] = state[x][0] xor state[x][1] xor state[x][2] xor state[x][3] xor state[x][4]
            for(x in d.indices)
                d[x] = c[(x + 4) % 5] xor c[(x + 1) % 5].rotateLeft(1)
            for(y in d.indices)
                for(x in c.indices)
                    newState[x][y] = state[x][y] xor d[x]

            return newState
        }

        /**
         * Rotate the bits of lanes(64 bits) by specified amounts.
         */
        @JvmSynthetic
        fun rho(state: Array<ULongArray>): Array<ULongArray> {
            val newState = Array(5) { ULongArray(5) }
            newState[0][0] = state[0][0]
            var x = 1
            var y = 0
            for (t in 0..<24) {
                newState[x][y] = state[x][y].rotateLeft(((t + 1) * (t + 2)) shr 1)
                val oldx = x
                x = y
                y = moduloOf((2 * oldx) + (3 * y), 5)
            }

            return newState
        }

        /**
         * Rearrange the lanes(64 bits).
         */
        @JvmSynthetic
        fun pi(state: Array<ULongArray>): Array<ULongArray> {
            val newState = Array(5) { ULongArray(5) }
            for(x in 0..<5)
                for(y in 0..<5)
                    newState[x][y] = state[moduloOf((x + (3 * y)), 5)][x]

            return newState
        }

        /**
         * XOR lanes(64 bits).
         */
        @JvmSynthetic
        fun chi(state: Array<ULongArray>): Array<ULongArray> {
            val newState = Array(5) { ULongArray(5) }
            for(x in 0..<5)
                for(y in 0..<5)
                    newState[x][y] = state[x][y] xor ((state[moduloOf((x + 1), 5)][y] xor  ULong.MAX_VALUE) and state[moduloOf((x + 2), 5)][y])

            return newState
        }

        /**
         * Modify the first(0,0) lane(64 bits) depending on the round number of the permutation.
         */
        @JvmSynthetic
        fun iota(state: Array<ULongArray>, round: Int): Array<ULongArray> {
            val newState = state.copyOf()

            newState[0][0] = newState[0][0] xor KeccakConstants.ROUND[round]

            return newState
        }

        /**
         * Do a single Keccak-f permutation round.
         */
        @JvmSynthetic
        fun doRound(state: Array<ULongArray>, round: Int): Array<ULongArray> {
            var newState = state.copyOf()

            newState = theta(newState)
            newState = rho(newState)
            newState = pi(newState)
            newState = chi(newState)
            newState = iota(newState, round)

            return newState
        }

        /**
         * Do the full 24 rounds to complete a permutation.
         */
        @JvmSynthetic
        fun permute(state: Array<ULongArray>): Array<ULongArray> {
            var newState = state.copyOf()

            for(i in 0..<24) {
                newState = doRound(newState, i)
            }

            return newState
        }

        /**
         * Modulo that works for negative and positive x values.
         */
        @JvmSynthetic
        fun moduloOf(x: Int, m: Int): Int {
            return (x % m + m) % m
        }

        /**
         * Convert up to 200 bytes into a state matrix.
         */
        @JvmSynthetic
        fun bytesToMatrix(bytes: ByteArray): Array<ULongArray> {
            val state = Array(5) { ULongArray(5) }

            val emptyByteArray = ByteArray(200)
            bytes.copyInto(emptyByteArray)

            for(x in 0..<5) {
                for(y in 0..<5) {
                    state[x][y] = bytesToULong(emptyByteArray.copyOfRange(8*(x+5*y), 8*(x+5*y)+8))
                }
            }

            return state
        }

        /**
         * Convert a state matrix into an array of 200 bytes.
         */
        @JvmSynthetic
        fun matrixToBytes(state: Array<ULongArray>): ByteArray {
            val emptyByteArray = ByteArray(200)

            for(x in 0..<5) {
                for(y in 0..<5) {
                    val ulongBytes = ulongToBytes(state[x][y])
                    for (i in 0..7) {
                        emptyByteArray[8*(x+5*y) + i] = ulongBytes[i]
                    }
                }
            }

            return emptyByteArray
        }

        /**
         * ULong to ByteArray conversion.
         */
        @JvmSynthetic
        fun ulongToBytes(long: ULong): ByteArray {
            return byteArrayOf(
                long.toUByte().toByte(),
                (long shr 8).toUByte().toByte(),
                (long shr 16).toUByte().toByte(),
                (long shr 24).toUByte().toByte(),
                (long shr 32).toUByte().toByte(),
                (long shr 40).toUByte().toByte(),
                (long shr 48).toUByte().toByte(),
                (long shr 56).toUByte().toByte()
            )
        }

        /**
         * ByteArray(length: 8) to ULong conversion.
         */
        @JvmSynthetic
        fun bytesToULong(bytes: ByteArray): ULong {
            require(bytes.size == 8) { "Requires 8 bytes! Size: " + bytes.size }

            return (bytes[0].toULong() and 0xFFuL) or
                    ((bytes[1].toULong() and 0xFFuL) shl 8)   or
                    ((bytes[2].toULong() and 0xFFuL) shl 16)  or
                    ((bytes[3].toULong() and 0xFFuL) shl 24)  or
                    ((bytes[4].toULong() and 0xFFuL) shl 32)  or
                    ((bytes[5].toULong() and 0xFFuL) shl 40)  or
                    ((bytes[6].toULong() and 0xFFuL) shl 48)  or
                    ((bytes[7].toULong() and 0xFFuL) shl 56)
        }
    }
}