package asia.hombre.keccak.internal

import asia.hombre.keccak.FlexiByte
import asia.hombre.keccak.FlexiByteArray
import asia.hombre.keccak.KeccakConstants
import kotlin.experimental.or
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.jvm.JvmSynthetic

/**
 * As part of the master branch, this Keccak implementation has optimizations that make it relatively unreadable.
 * For a more understandable version, please view the standard branch.
 */
@OptIn(ExperimentalUnsignedTypes::class, ExperimentalJsExport::class)
@JsExport
internal class KeccakMath {
    internal companion object {
        /**
         * Optimized for exact bytes.
         */
        fun pad10n1(bytes: ByteArray, multiple: Int, flexiByte: FlexiByte): ByteArray {
            val multipleInBytes = multiple shr 3
            val paddedSize = bytes.size
            val paddedBytes = ByteArray(paddedSize + (multipleInBytes - (paddedSize % multipleInBytes)))

            bytes.copyInto(paddedBytes)
            paddedBytes[bytes.size] = flexiByte.toByte() or (0b1 shl (flexiByte.bitIndex + 1)).toByte()
            paddedBytes[paddedBytes.lastIndex] = paddedBytes[paddedBytes.lastIndex] or (0b10000000u).toByte()

            return paddedBytes
        }

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
         * Do a single Keccak-f permutation round.
         */
        @JvmSynthetic
        fun doRound(state: Array<ULongArray>, round: Int): Array<ULongArray> {
            //Theta (Parity Calculation)
            val c = ULongArray(5) {
                x -> state[x].reduce(ULong::xor)
            }
            val d = ULongArray(5) {
                x -> c[(x + 4) % 5] xor c[(x + 1) % 5].rotateLeft(1)
            }
            val newState = Array(5) {
                x -> ULongArray(5) {
                    y -> state[x][y] xor d[x]
                }
            }

            //Rho + Pi (Rotate bits and rearrange lanes)
            val piState = Array(5) { ULongArray(5) }
            for (x in 0 until 5)
                for (y in 0 until 5) {
                    val rotatedIndex = (x + (3 * y)) % 5
                    piState[x][y] = newState[rotatedIndex][x].rotateLeft(KeccakConstants.SHIFTS[rotatedIndex][x])
                }

            //Chi (XOR lanes)
            val chiState = Array(5) {
                x -> ULongArray(5) {
                    y -> piState[x][y] xor ((piState[(x + 1) % 5][y] xor ULong.MAX_VALUE) and piState[(x + 2) % 5][y])
                }
            }

            //Iota (Modify the first lane with a predefined value unique for each round)
            chiState[0][0] = chiState[0][0] xor KeccakConstants.ROUND[round]

            return chiState
        }

        /**
         * Do the full 24 rounds to complete a permutation.
         */
        @JvmSynthetic
        fun permute(state: Array<ULongArray>): Array<ULongArray> {
            var newState = state.copyOf()

            for(i in 0 until 24) {
                newState = doRound(newState, i)
            }

            return newState
        }

        /**
         * Convert up to 200 bytes into a state matrix.
         */
        @JvmSynthetic
        fun bytesToMatrix(bytes: ByteArray): Array<ULongArray> {
            val state = Array(5) { ULongArray(5) }

            val emptyByteArray = ByteArray(200)
            bytes.copyInto(emptyByteArray)

            for(x in 0 until 5) {
                for(y in 0 until 5) {
                    state[x][y] = bytesToULong(emptyByteArray.copyOfRange((x + (5 * y)) shl 3, ((x + (5 * y)) shl 3) + 8))
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

            for(x in 0 until 5) {
                for(y in 0 until 5) {
                    val ulongBytes = ulongToBytes(state[x][y])
                    for (i in 0..7) {
                        emptyByteArray[((x + (5 * y)) shl 3) + i] = ulongBytes[i]
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