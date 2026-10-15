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

import asia.hombre.keccak.KeccakConstants
import asia.hombre.keccak.KeccakParameter
import asia.hombre.keccak.internal.FlexiByte
import asia.hombre.keccak.internal.KeccakMath
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.math.min

/**
 * Writable for streams of input bytes and creates a [HashOutputStream] once closed.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
@Suppress("unused")
open class HashInputStream internal constructor(
    /**
     * The [KeccakParameter] used to absorb the bytes.
     */
    @get:JvmName("getParameter")
    val PARAMETER: KeccakParameter,
    private val maxOutputLength: Int = PARAMETER.maxLength / 8
) {
    private val incompleteState = LongArray(25)
    private val buffer = ByteArray(PARAMETER.BYTERATE)

    /**
     * The suffix for this instance used as input for the algorithm.
     */
    internal open val SUFFIX: FlexiByte
        get() = PARAMETER.SUFFIX

    private var isClosed = false
    private var inputPos = 0

    internal fun forcePermute() {
        inputPos = buffer.size
        tryPermute()
    }

    /**
     * Permutes if there are no more usable bytes.
     */
    private fun tryPermute() {
        if(inputPos < buffer.size) return
        var i: Int

        for(x in 0..<5) {
            for(y in 0..<5) {
                i = 5 * x + y
                incompleteState[i] = incompleteState[i] xor KeccakMath.getLongAt(buffer, x, y)
            }
        }

        KeccakMath.directPermute(incompleteState)

        buffer.fill(0)
        inputPos = 0
    }

    @JvmSynthetic
    internal open fun onAbsorb(bytes: ByteArray, offset: Int, length: Int) {
        if(isClosed) throw IllegalStateException("Already closed.")

        var inputIndex = offset
        val endIndex = offset + length

        while(inputIndex < endIndex) {
            val bytesToDigest = min(endIndex - inputIndex, buffer.size - inputPos)
            bytes.copyInto(buffer, inputPos, inputIndex, inputIndex + bytesToDigest)
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
     * Detaches this stream from its parent's permute scratch arena, giving it its own.
     *
     * As of 2.5.1, this is no longer needed or used. Due to internal optimizations,
     * the scratch arena has been removed.
     *
     * It was very ugly, and I hated having to ship it. Now that a new optimization has
     * taken place, I'm very much happy to get rid of it. If you are one of the few
     * needing to read this, please feel free to remove this line from your code.
     *
     * @since 2.4.0
     */
    @Deprecated("No longer needed. This does nothing now.")
    fun detachScratch() {}

    /**
     * Closes this [HashInputStream] and prevents further usage.
     *
     * @return [HashOutputStream]
     */
    fun close(): HashOutputStream {
        if(isClosed) throw IllegalStateException("Already closed.")

        beforeClose()

        KeccakMath.pad10n1Direct(buffer, inputPos, SUFFIX)
        inputPos = buffer.size

        tryPermute()

        return HashOutputStream(PARAMETER, incompleteState, buffer, maxOutputLength)
    }

    /**
     * Internal helper function to add the CSHAKE pre-padding.
     */
    @JvmSynthetic
    internal fun addCSHAKEPrePadding(functionName: ByteArray, customization: ByteArray) {
        write(KeccakMath.leftEncode(this.PARAMETER.BYTERATE.toLong()))
        write(KeccakMath.leftEncode(functionName.size.toLong() * 8))
        write(functionName)
        write(KeccakMath.leftEncode(customization.size.toLong() * 8))
        write(customization)
        forcePermute()
    }

    /**
     * Internal helper function to add the KMAC pre-padding.
     */
    @JvmSynthetic
    internal fun addKMACPrePadding(key: ByteArray, customization: ByteArray) {
        write(KeccakMath.leftEncode(this.PARAMETER.BYTERATE.toLong()))
        write(KeccakMath.leftEncode(32L)) //KMAC_BYTES.size * 8L
        write(KeccakConstants.KMAC_BYTES)
        write(KeccakMath.leftEncode(customization.size.toLong() * 8))
        write(customization)
        forcePermute()

        write(KeccakMath.leftEncode(this.PARAMETER.BYTERATE.toLong()))
        write(KeccakMath.leftEncode(key.size.toLong() * 8))
        write(key)
        forcePermute()
    }
}