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

package asia.hombre.keccak.streams

import asia.hombre.keccak.internal.FlexiByte
import asia.hombre.keccak.KeccakParameter
import asia.hombre.keccak.internal.KeccakMath
import asia.hombre.keccak.internal.SplitByteArray
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.math.min

/**
 * Writable for streams of input bytes and creates a [HashOutputStream] once closed.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
open class HashInputStream internal constructor(
    /**
     * The [KeccakParameter] used to absorb the bytes.
     */
    @get:JvmName("getParameter")
    val PARAMETER: KeccakParameter,
    private val maxOutputLength: Int = PARAMETER.maxLength / 8
) {
    private val incompleteState = Array(5) { LongArray(5) }
    private val buffer = SplitByteArray(ByteArray(PARAMETER.BYTERATE), ByteArray(200 - PARAMETER.BYTERATE))
    private val inputBuffer
        get() = buffer.a

    /**
     * The suffix for this instance used as input for the algorithm.
     */
    internal open val SUFFIX: FlexiByte
        get() = PARAMETER.SUFFIX

    private var isClosed = false
    private var inputPos = 0

    /**
     * Permutes if there are no more usable bytes.
     */
    private fun tryPermute() {
        if(inputPos < inputBuffer.size) return

        for(x in 0..<5) for(y in 0..<5)
            incompleteState[x][y] = incompleteState[x][y] xor KeccakMath.getLongAt(buffer, x, y, PARAMETER.BYTERATE)

        KeccakMath.directPermute(incompleteState)

        inputBuffer.fill(0)
        inputPos = 0
    }

    @JvmSynthetic
    internal open fun onAbsorb(bytes: ByteArray, offset: Int, length: Int) {
        if(isClosed) throw IllegalStateException("Already closed.")

        var inputIndex = offset
        val endIndex = offset + length

        while(inputIndex < endIndex) {
            val bytesToDigest = min(endIndex - inputIndex, inputBuffer.size - inputPos)
            bytes.copyInto(inputBuffer, inputPos, inputIndex, inputIndex + bytesToDigest)
            inputPos += bytesToDigest
            inputIndex += bytesToDigest

            tryPermute()
        }
    }

    @JvmSynthetic
    internal open fun onAbsorbOne(byte: Byte) {
        if(isClosed) throw IllegalStateException("Already closed.")

        buffer[inputPos++] = byte

        tryPermute()
    }

    @JvmSynthetic
    internal open fun beforeClose() {}

    /**
     * Writes a byte which will be absorbed in the sponge construction.
     *
     * @param byte [Byte]
     * @since 2.0.0
     */
    fun write(byte: Byte) = onAbsorbOne(byte)

    /**
     * Writes the bytes which will be absorbed in the sponge construction.
     *
     * @param byteArray [ByteArray]
     * @since 2.0.0
     */
    fun write(byteArray: ByteArray) = onAbsorb(byteArray, 0, byteArray.size)

    /**
     * Writes part of the bytes which will be absorbed in the sponge construction.
     *
     * @param byteArray [ByteArray]
     * @param offset The offset of which part to write.
     * @param length The number of bytes to write.
     * @since 2.0.0
     */
    fun write(byteArray: ByteArray, offset: Int, length: Int) = onAbsorb(byteArray, offset, length)

    /**
     * Writes a Short which will be absorbed in the sponge construction in Big-Endianness.
     *
     * @param short [Short]
     * @since 2.0.0
     */
    fun write(short: Short) {
        write(short.rotateRight(8).toByte())
        write(short.toByte())
    }

    /**
     * Writes an Int which will be absorbed in the sponge construction in Big-Endianness.
     *
     * @param int [Int]
     * @since 2.0.0
     */
    fun write(int: Int) {
        write((int ushr 24).toByte())
        write((int ushr 16).toByte())
        write((int ushr 8).toByte())
        write(int.toByte())
    }

    /**
     * Writes a Long which will be absorbed in the sponge construction in Big-Endianness.
     *
     * @param long [Long]
     * @since 2.0.0
     */
    fun write(long: Long) {
        write((long ushr 56).toByte())
        write((long ushr 48).toByte())
        write((long ushr 40).toByte())
        write((long ushr 32).toByte())
        write((long ushr 24).toByte())
        write((long ushr 16).toByte())
        write((long ushr 8).toByte())
        write(long.toByte())
    }

    /**
     * Writes a Float which will be absorbed in the sponge construction in Big-Endianness while preserving its bit
     * layout in IEEE 754 format.
     *
     * @param float [Float]
     * @since 2.0.0
     */
    fun write(float: Float) = write(float.toRawBits())

    /**
     * Writes a Double which will be absorbed in the sponge construction in Big-Endianness while preserving its bit
     * layout in IEEE 754 format.
     *
     * @param double [Double]
     * @since 2.0.0
     */
    fun write(double: Double) = write(double.toRawBits())

    /**
     * Closes this [HashInputStream] and prevents further usage.
     *
     * @return [HashOutputStream]
     */
    fun close(): HashOutputStream {
        if(isClosed) throw IllegalStateException("Already closed.")

        beforeClose()

        KeccakMath.pad10n1Direct(inputBuffer, inputPos, SUFFIX)
        inputPos = inputBuffer.size

        tryPermute()

        buffer.b.fill(0)

        return HashOutputStream(PARAMETER, incompleteState, maxOutputLength)
    }
}