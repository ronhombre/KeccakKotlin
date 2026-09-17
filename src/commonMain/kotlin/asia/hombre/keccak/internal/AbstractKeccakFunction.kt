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

import asia.hombre.keccak.KeccakParameter
import asia.hombre.keccak.api.KeccakInstance
import asia.hombre.keccak.streams.HashInputStream
import asia.hombre.keccak.streams.HashOutputStream

/**
 * A wrapper class for [HashInputStream] so each API class will use the same underlying code with minimal boilerplate.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
@Suppress("unused")
abstract class AbstractKeccakFunction internal constructor(
    private val initialCapacity: Int,
    override val parameter: KeccakParameter,
    open val outputLength: Int
): KeccakInstance {
    private val ghost = KeccakMath.GhostArena()
    private var inputStream: HashInputStream? = null

    private val currentStream: HashInputStream
        get() = inputStream ?: newInputStream(ghost).also { inputStream = it }

    /**
     * Copies a byte into the buffer.
     *
     * @param byte [Byte]
     * @since 2.0.0
     */
    fun update(byte: Byte) = currentStream.write(byte)

    /**
     * Copies an array of bytes into the buffer.
     *
     * @param byteArray [ByteArray]
     * @since 2.0.0
     */
    fun update(byteArray: ByteArray) = currentStream.write(byteArray)

    /**
     * Copies a part of an array of bytes into the buffer.
     *
     * @param byteArray [ByteArray]
     * @param offset The offset which part of the bytes to copy.
     * @param length The number of bytes to copy.
     * @since 2.0.0
     */
    fun update(byteArray: ByteArray, offset: Int, length: Int) = currentStream.write(byteArray, offset, length)

    /**
     * Permutes over the buffer.
     *
     * Resets this instance for the next use.
     *
     * @return The [ByteArray] containing the hash based on the parameter used.
     * @since 2.0.0
     */
    fun digest(): ByteArray {
        currentStream.write(addLast())

        return currentStream.close().nextBytes(outputLength).also { inputStream = null }
    }

    /**
     * Copies a byte into the buffer and permutes over the buffer.
     *
     * Resets this instance for the next use.
     *
     * @param byte [Byte]
     * @return The [ByteArray] containing the hash based on the parameter used.
     * @since 2.0.0
     */
    fun digest(byte: Byte): ByteArray {
        currentStream.write(byte)
        currentStream.write(addLast())

        return currentStream.close().nextBytes(outputLength).also { inputStream = null }
    }

    /**
     * Copies an array of bytes into the buffer and permutes over the buffer.
     *
     * Resets this instance for the next use.
     *
     * @param byteArray [ByteArray]
     * @return The [ByteArray] containing the hash based on the parameter used.
     * @since 2.0.0
     */
    fun digest(byteArray: ByteArray): ByteArray {
        currentStream.write(byteArray)
        currentStream.write(addLast())

        return currentStream.close().nextBytes(outputLength).also { inputStream = null }
    }

    /**
     * Copies a part of an array of bytes into the buffer and permutes over the buffer.
     *
     * Resets this instance for the next use.
     *
     * @param byteArray [ByteArray]
     * @param offset The offset which part of the bytes to copy.
     * @param length The number of bytes to copy.
     * @return The [ByteArray] containing the hash based on the parameter used.
     * @since 2.0.0
     */
    fun digest(byteArray: ByteArray, offset: Int, length: Int): ByteArray {
        currentStream.write(byteArray, offset, length)
        currentStream.write(addLast())

        return currentStream.close().nextBytes(outputLength).also { inputStream = null }
    }

    /**
     * Permutes over the buffer.
     *
     * Resets this instance for the next use.
     *
     * @return A [HashOutputStream] to stream the output bytes.
     * @since 2.0.0
     */
    fun stream(): HashOutputStream {
        currentStream.write(addLast())

        return currentStream.close().also { inputStream = null }
    }

    /**
     * Copies a byte into the buffer and permutes over the buffer.
     *
     * Resets this instance for the next use.
     *
     * @param byte [Byte]
     * @return A [HashOutputStream] to stream the output bytes.
     * @since 2.0.0
     */
    fun stream(byte: Byte): HashOutputStream {
        currentStream.write(byte)
        currentStream.write(addLast())

        return currentStream.close().also { inputStream = null }
    }

    /**
     * Copies an array of bytes into the buffer and permutes over the buffer.
     *
     * Resets this instance for the next use.
     *
     * @param byteArray [ByteArray]
     * @return A [HashOutputStream] to stream the output bytes.
     * @since 2.0.0
     */
    fun stream(byteArray: ByteArray): HashOutputStream {
        currentStream.write(byteArray)
        currentStream.write(addLast())

        return currentStream.close().also { inputStream = null }
    }

    /**
     * Copies a part of an array of bytes into the buffer and permutes over the buffer.
     *
     * Resets this instance for the next use.
     *
     * @param byteArray [ByteArray]
     * @param offset The offset which part of the bytes to copy.
     * @param length The number of bytes to copy.
     * @return A [HashOutputStream] to stream the output bytes.
     * @since 2.0.0
     */
    fun stream(byteArray: ByteArray, offset: Int, length: Int): HashOutputStream {
        currentStream.write(byteArray, offset, length)
        currentStream.write(addLast())

        return currentStream.close().also { inputStream = null }
    }

    internal fun skipToNextChunk() = currentStream.forcePermute()
    internal abstract fun newInputStream(ghostArena: KeccakMath.GhostArena): HashInputStream
    protected open fun addLast(): ByteArray = ByteArray(0)
}