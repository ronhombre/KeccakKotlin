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
import asia.hombre.keccak.internal.AbstractKeccakFunction
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
    private var ghost: KeccakMath.GhostArena
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
    internal val maxOutputLength: Int
    private var totalOutputLength = 0

    /**
     * This constructor assumes that the caller can be trusted which is the case since the visibility is `internal`.
     *
     * This inherits everything from [HashInputStream].
     *
     * DO NOT MAKE THIS PUBLIC.
     */
    internal constructor(parameter: KeccakParameter, completedState: LongArray, buffer: ByteArray, ghost: KeccakMath.GhostArena, maxOutputLength: Int = parameter.maxLength / 8) {
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
        this.ghost = ghost
        KeccakMath.directMatrixToBytes(state, stateBuffer)
    }

    /**
     * Checks if we need to squeeze more bytes from the state matrix. Throws an [IllegalStateException] if the
     * parameter does not allow it.
     */
    private fun trySqueeze() {
        if(!hasNext()) throw IllegalArgumentException("This parameter $PARAMETER only supports a total output of $maxOutputLength bytes. This is not an extendable function.")
        if(used < stateBuffer.size) return

        KeccakMath.directPermute(state, ghost)
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

    /**
     * Detaches this stream from its parent's permute scratch arena, giving it its own.
     *
     * Use this when you intend to hand this stream off to a different thread than the one
     * that owns the [AbstractKeccakFunction] (or [HashInputStream]) that produced it, while
     * the parent continues to be used on the original thread.
     *
     * Redundant if the producing [HashInputStream] already had [HashInputStream.detachScratch]
     * called on it before [HashInputStream.close]; in that case this stream already has its own arena.
     *
     * This does NOT make the stream safe for concurrent access. A single stream must still
     * be used by one thread at a time. If you need concurrent hashing, use a separate
     * [AbstractKeccakFunction] instance per thread.
     *
     * Allocates one [KeccakMath.GhostArena] (~240 bytes). Idempotent; calling twice
     * discards the previous arena and allocates a fresh one.
     *
     * @since 2.4.0
     */
    fun detachScratch() {
        ghost = KeccakMath.GhostArena()
    }
}