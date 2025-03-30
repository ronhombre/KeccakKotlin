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

import asia.hombre.keccak.FlexiByte
import asia.hombre.keccak.KeccakHash
import asia.hombre.keccak.KeccakParameter
import asia.hombre.keccak.internal.KeccakMath
import asia.hombre.keccak.internal.SplitByteArray
import kotlin.jvm.JvmName
import kotlin.math.min

/**
 * Outputs a stream of bytes for the Keccak Hash Functions with methods to substitute as a CSPRNG.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
class HashOutputStream {
    /**
     * The [KeccakParameter] used to generate the hash stream.
     */
    @get:JvmName("getParameter")
    val PARAMETER: KeccakParameter

    private val state: Array<LongArray>
    private val stateBuffer: SplitByteArray
    private var used = 0
    private val squeezable: Boolean
        get() = when(PARAMETER) {
            //SHA-3
            KeccakParameter.SHA3_224,
            KeccakParameter.SHA3_256,
            KeccakParameter.SHA3_384,
            KeccakParameter.SHA3_512,
            //KMACs
            KeccakParameter.KMAC_128,
            KeccakParameter.KMAC_256 -> false
            else -> true
        }
    private val maxOutputLength: Int
    private var totalOutputLength = 0

    /**
     * This constructor assumes that the caller can be trusted which is the case since the visibility is `internal`.
     *
     * DO NOT MAKE THIS PUBLIC.
     */
    internal constructor(parameter: KeccakParameter, suffix: FlexiByte, chunks: Pair<Array<ByteArray>, Int>, maxOutputLength: Int) {
        if(chunks.first.isEmpty()) throw IllegalArgumentException("Must have at least one chunk.")

        PARAMETER = parameter

        //Commented out since it's redundant given this is an internal part.
        /*chunks.first.forEachIndexed { i, it ->
            if(it.size != PARAMETER.BYTERATE)
                throw IllegalStateException("Bad chunks supplied. Expected ${PARAMETER.BYTERATE} but got ${it.size}")
        }*/

        stateBuffer = SplitByteArray(ByteArray(PARAMETER.BYTERATE), ByteArray(200 - PARAMETER.BYTERATE))
        state = Array(5) { LongArray(5) }

        this.maxOutputLength = maxOutputLength

        KeccakHash.generateDirect(PARAMETER, chunks, suffix, stateBuffer, state)

        //Drops reference to a ByteArray which is a part of `chunks` to allow the GC to clean `chunks` up.
        stateBuffer.a = ByteArray(PARAMETER.BYTERATE)
        KeccakMath.directMatrixToBytes(state, stateBuffer)
    }

    /**
     * This constructor assumes that the caller can be trusted which is the case since the visibility is `internal`.
     *
     * DO NOT MAKE THIS PUBLIC.
     */
    internal constructor(parameter: KeccakParameter, completedState: Array<LongArray>, maxOutputLength: Int) {
        PARAMETER = parameter

        //Commented out since it's redundant given this is an internal part.
        /*if(completedState.size != 5)
            throw IllegalStateException("Bad completedState supplied. Expected a 5x5 matrix but got a column with size ${completedState.size}")
        completedState.forEach {
            if(it.size != 5)
                throw IllegalStateException("Bad completedState supplied. Expected a 5x5 matrix but got a row with size ${it.size}")
        }*/

        this.maxOutputLength = maxOutputLength

        state = completedState
        stateBuffer = SplitByteArray(ByteArray(PARAMETER.BYTERATE), ByteArray(200 - PARAMETER.BYTERATE))
        KeccakMath.directMatrixToBytes(state, stateBuffer)
    }

    /**
     * Checks if we need to squeeze more bytes from the state matrix. Throws an [IllegalStateException] if the
     * parameter does not allow it.
     */
    private fun trySqueeze() {
        if(!hasNext()) throw IllegalArgumentException("This parameter $PARAMETER only supports a total output of $maxOutputLength bytes. This is not an extendable function.")
        if(used < stateBuffer.a.size) return

        KeccakMath.directPermute(state)
        KeccakMath.directMatrixToBytes(state, stateBuffer)

        used = 0
    }

    /**
     * Directly copies as many bytes as the parameter allows into the destination array.
     */
    private fun getAsManyBytes(destination: ByteArray, offset: Int): Int {
        trySqueeze()

        val asMuch = min(stateBuffer.a.size - used, destination.size - offset)

        stateBuffer.a
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

        return stateBuffer.a[used++].also { totalOutputLength++ }
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
     * @param length The number of bytes to copy starting from the offset
     * @param offset The offset from the start of the destination byte array
     * @since 2.0.0
     */
    fun nextBytes(destination: ByteArray, length: Int, offset: Int) {
        if(!squeezable && totalOutputLength + length > maxOutputLength) throw IllegalArgumentException("This parameter $PARAMETER only supports a total output of $maxOutputLength bytes. This is not an extendable function.")

        var offset = offset
        while(offset < length) {
            offset = getAsManyBytes(destination, offset)
        }
    }

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
}