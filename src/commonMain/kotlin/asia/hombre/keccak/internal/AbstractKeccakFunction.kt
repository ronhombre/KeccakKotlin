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

import asia.hombre.keccak.api.KeccakInstance
import asia.hombre.keccak.streams.HashOutputStream

/**
 * A wrapper class for [UniversalDigestor] so each API class will use the same underlying code with minimal boilerplate.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
@Suppress("unused")
abstract class AbstractKeccakFunction internal constructor(initialCapacity: Int): KeccakInstance {
    private val digestor: UniversalDigestor = UniversalDigestor(initialCapacity)

    /**
     * Copies a byte into the buffer.
     *
     * @param byte [Byte]
     * @since 2.0.0
     */
    fun update(byte: Byte) = digestor.digestSingle(byte)

    /**
     * Copies an array of bytes into the buffer.
     *
     * @param byteArray [ByteArray]
     * @since 2.0.0
     */
    fun update(byteArray: ByteArray) = digestor.digest(byteArray)

    /**
     * Copies a part of an array of bytes into the buffer.
     *
     * @param byteArray [ByteArray]
     * @param offset The offset which part of the bytes to copy.
     * @param length The number of bytes to copy.
     * @since 2.0.0
     */
    fun update(byteArray: ByteArray, offset: Int, length: Int) = digestor.digest(byteArray, offset, length)

    /**
     * Permutes over the buffer.
     *
     * Resets this instance for the next use.
     *
     * @return The [ByteArray] containing the hash based on the parameter used.
     * @since 2.0.0
     */
    fun digest(): ByteArray {
        digestor.digest(addLast())
        
        return computeDigest(digestor.extractChunksAndReset())
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
        digestor.digestSingle(byte)
        digestor.digest(addLast())

        return computeDigest(digestor.extractChunksAndReset())
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
        digestor.digest(byteArray)
        digestor.digest(addLast())

        return computeDigest(digestor.extractChunksAndReset())
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
        digestor.digest(byteArray, offset, length)
        digestor.digest(addLast())

        return computeDigest(digestor.extractChunksAndReset())
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
        digestor.digest(addLast())

        return computeAsHashStream(digestor.extractChunksAndReset())
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
        digestor.digestSingle(byte)
        digestor.digest(addLast())

        return computeAsHashStream(digestor.extractChunksAndReset())
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
        digestor.digest(byteArray)
        digestor.digest(addLast())

        return computeAsHashStream(digestor.extractChunksAndReset())
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
        digestor.digest(byteArray, offset, length)
        digestor.digest(addLast())

        return computeAsHashStream(digestor.extractChunksAndReset())
    }

    internal fun skipToNextChunk() = digestor.skipToNextChunk()

    protected abstract fun computeDigest(chunks: Pair<Array<ByteArray>, Int>): ByteArray
    protected abstract fun computeAsHashStream(chunks: Pair<Array<ByteArray>, Int>): HashOutputStream
    protected open fun addLast(): ByteArray = ByteArray(0)
}