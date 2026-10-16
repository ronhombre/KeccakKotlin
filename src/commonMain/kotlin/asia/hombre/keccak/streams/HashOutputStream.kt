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

import asia.hombre.keccak.KeccakParameter
import asia.hombre.keccak.internal.KeccakMath
import kotlin.jvm.JvmName
import kotlin.math.min

/**
 * Outputs a stream of bytes for the Keccak Hash Functions with methods to substitute as a CSPRNG.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
@Suppress("unused")
class HashOutputStream {
    /**
     * The [KeccakParameter] used to generate the hash stream.
     */
    @get:JvmName("getParameter")
    val PARAMETER: KeccakParameter

    private val state: LongArray
    private val stateBuffer: ByteArray
    private var used = 0
    private val squeezable: Boolean
        get() = PARAMETER.isExtendable()
    internal val maxOutputLength: Int
    private var totalOutputLength = 0

    /**
     * This constructor assumes that the caller can be trusted which is the case since the visibility is `internal`.
     *
     * This inherits everything from [HashInputStream].
     *
     * DO NOT MAKE THIS PUBLIC.
     */
    internal constructor(parameter: KeccakParameter, completedState: LongArray, buffer: ByteArray, maxOutputLength: Int = parameter.maxLength / 8) {
        PARAMETER = parameter

        //Commented out since it's redundant given this is an internal part.
        /*if(completedState.size != 5)
            throw IllegalStateException("Bad completedState supplied. Expected a 5x5 matrix but got a column with size ${completedState.size}")
        completedState.forEach {
            if(it.size != 5)
                throw IllegalStateException("Bad completedState supplied. Expected a 5x5 matrix but got a row with size ${it.size}")
        }*/

        this.maxOutputLength = maxOutputLength

        this.state = completedState
        this.stateBuffer = buffer
        KeccakMath.directMatrixToBytes(state, stateBuffer)
    }

    /**
     * Checks if we need to squeeze more bytes from the state matrix. Throws an [IllegalStateException] if the
     * parameter does not allow it.
     */
    private fun trySqueeze() {
        if(!hasNext()) throw IllegalArgumentException("This parameter $PARAMETER only supports a total output of $maxOutputLength bytes. This is not an extendable function.")
        if(used < stateBuffer.size) return

        KeccakMath.directPermute(state)
        KeccakMath.directMatrixToBytes(state, stateBuffer)

        used = 0
    }

    /**
     * Directly copies as many bytes as the parameter allows into the destination array.
     */
    private fun getAsManyBytes(destination: ByteArray, offset: Int): Int {
        trySqueeze()

        val asMuch = min(stateBuffer.size - used, destination.size - offset)

        stateBuffer
            .copyInto(destination, offset, used, used + asMuch)
            .also { used += asMuch; totalOutputLength += asMuch }

        return (offset + asMuch)
    }

    /**
     * Returns the next byte from the hash stream.
     *
     * @since 2.0.0
     */
    fun nextByte(): Byte {
        trySqueeze()

        return stateBuffer[used++].also { totalOutputLength++ }
    }

    /**
     * Returns a byte array from the hash stream.
     *
     * @param length the requested number of bytes.
     * @since 2.0.0
     */
    fun nextBytes(length: Int): ByteArray {
        if(!squeezable && totalOutputLength + length > maxOutputLength) throw IllegalArgumentException("This parameter $PARAMETER only supports a total output of $maxOutputLength bytes. This is not an extendable function.")
        val destinationArray = ByteArray(length)

        var offset = 0
        while(offset < length) {
            offset = getAsManyBytes(destinationArray, offset)
        }

        return destinationArray
    }

    /**
     * Copies bytes from the hash stream into the byte array.
     *
     * @param destination [ByteArray]
     * @since 2.0.0
     */
    fun nextBytes(destination: ByteArray) {
        if(!squeezable && totalOutputLength + destination.size > maxOutputLength) throw IllegalArgumentException("This parameter $PARAMETER only supports a total output of $maxOutputLength bytes. This is not an extendable function.")

        var offset = 0
        while(offset < destination.size) {
            offset = getAsManyBytes(destination, offset)
        }
    }

    /**
     * Copies bytes from the hash stream into a part of the byte array.
     *
     * @param destination [ByteArray]
     * @param endIndex The end of the index to copy into.
     * @param startIndex The start of the index to copy into.
     * @since 2.0.0
     */
    fun nextBytes(destination: ByteArray, endIndex: Int, startIndex: Int) {
        if(!squeezable && totalOutputLength + endIndex > maxOutputLength) throw IllegalArgumentException("This parameter $PARAMETER only supports a total output of $maxOutputLength bytes. This is not an extendable function.")

        var offset = startIndex
        while(offset < endIndex) {
            offset = getAsManyBytes(destination, offset)
        }
    }

    /**
     * Copies bytes from the hash stream into a part of the byte array.
     *
     * @param length The number of bytes to copy starting from the offset
     * @param offset The offset from the start of the destination byte array
     * @since 2.5.1
    */
    fun nextBytesOffset(destination: ByteArray, length: Int, offset: Int) = nextBytes(destination, offset + length, offset)

    /**
     * Returns a [Short] made up of *two* bytes from the hash stream.
     *
     * The first byte is the most significant bit and so on... (Big-Endian)
     *
     * @since 2.0.0
     */
    fun nextShort(): Short = (((nextByte().toInt() and 0xFF) shl 8) or (nextByte().toInt() and 0xFF)).toShort()

    /**
     * Returns an [Int] made up of *four* bytes from the hash stream.
     *
     * The first byte is the most significant bit and so on... (Big-Endian)
     *
     * @since 2.0.0
     */
    fun nextInt(): Int =
        ((nextByte().toInt() and 0xFF) shl 24) or ((nextByte().toInt() and 0xFF) shl 16) or
                ((nextByte().toInt() and 0xFF) shl 8) or (nextByte().toInt() and 0xFF)

    /**
     * Returns a [Long] made up of *eight* bytes from the hash stream.
     *
     * The first byte is the most significant bit and so on... (Big-Endian)
     *
     * @since 2.0.0
     */
    fun nextLong(): Long =
        ((nextByte().toLong() and 0xFF) shl 56) or ((nextByte().toLong() and 0xFF) shl 48) or
                ((nextByte().toLong() and 0xFF) shl 40) or ((nextByte().toLong() and 0xFF) shl 32) or
                ((nextByte().toLong() and 0xFF) shl 24) or ((nextByte().toLong() and 0xFF) shl 16) or
                ((nextByte().toLong() and 0xFF) shl 8) or (nextByte().toLong() and 0xFF)

    /**
     * Returns a [Float] made up of *four* bytes from the hash stream.
     *
     * The first byte is the most significant bit and so on... (Big-Endian)
     *
     * @since 2.0.0
     */
    fun nextFloat(): Float = Float.fromBits(nextInt())

    /**
     * Returns a [Double] made up of *eight* bytes from the hash stream.
     *
     * The first byte is the most significant bit and so on... (Big-Endian)
     *
     * @since 2.0.0
     */
    fun nextDouble(): Double = Double.fromBits(nextLong())

    /**
     * Returns a [Boolean] using a full byte from the hash stream.
     *
     * Counts the number of *one* bits in the byte and returns `true` if there is an odd number of *one* bits.
     *
     * @since 2.0.0
     */
    fun nextBoolean(): Boolean = nextByte().countOneBits() and 1 == 1

    /**
     * Returns true if the [KeccakParameter] allows more bytes to output.
     *
     * @since 2.0.0
     */
    fun hasNext(): Boolean = squeezable || totalOutputLength < maxOutputLength

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
}