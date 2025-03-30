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

import asia.hombre.keccak.internal.exception.DigestTooLargeException
import kotlin.math.min

/**
 * A helper class to hold the input bytes into a buffer before permutation.
 *
 * @author Ron Lauren Hombre
 * @since 2.0.0
 */
internal class UniversalDigestor(private val initialCapacity: Int) {
    init {
        if(initialCapacity <= 0) throw IllegalArgumentException("initialCapacity must be greater than zero.")
    }

    private val bufferStorage = mutableListOf<ByteArray>()
    private var buffer = ByteArray(initialCapacity)
    private var bufferPos = 0

    private fun willItFit(size: Long) {
        val expectedTotalSize = bufferPos.toLong() + (bufferStorage.size.toLong() * initialCapacity.toLong()) + size

        if(expectedTotalSize > Int.MAX_VALUE || expectedTotalSize < 0L) {
            throw DigestTooLargeException(expectedTotalSize)
        }
    }

    private fun allocateMoreIfRequired() {
        if(bufferPos == buffer.size) {
            bufferStorage.add(buffer)

            buffer = ByteArray(initialCapacity)
            bufferPos = 0
        }
    }

    fun digestSingle(byte: Byte) {
        willItFit(1L)

        buffer[bufferPos++] = byte

        allocateMoreIfRequired()
    }

    fun digest(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size - offset) {
        if(offset > bytes.size)
            throw IndexOutOfBoundsException("Offset exceeds the size of the input bytes.")
        if((offset + length) > bytes.size)
            throw IndexOutOfBoundsException("Offset + Length exceeds the size of the input bytes.")
        if(length < 0)
            throw IndexOutOfBoundsException("Length $length is out of bounds.")
        if(offset < 0)
            throw IndexOutOfBoundsException("Offset $offset is out of bounds.")

        willItFit(length.toLong())

        var inputIndex = offset
        val endIndex = offset + length

        while(inputIndex < endIndex) {
            val bytesToDigest = min(endIndex - inputIndex, buffer.size - bufferPos)
            bytes.copyInto(buffer, bufferPos, inputIndex, inputIndex + bytesToDigest)
            inputIndex += bytesToDigest
            bufferPos += bytesToDigest

            allocateMoreIfRequired()
        }
    }

    /**
     * Returns a contiguous array of bytes of the buffer.
     *
     * This is inefficient so this is no longer used.
     */
    fun extractAndReset(): ByteArray {
        try {
            val arraySize: Long = (bufferStorage.size.toLong() * initialCapacity.toLong()) + bufferPos.toLong()
            val extractArray = ByteArray(arraySize.toInt())

            bufferStorage.forEachIndexed { index, storedBuffer ->
                storedBuffer.copyInto(extractArray, index * initialCapacity, 0, storedBuffer.size)
                storedBuffer.fill(0)
            }

            buffer.copyInto(extractArray, bufferStorage.size * initialCapacity, 0, bufferPos)
            bufferStorage.clear()
            buffer.fill(0)
            bufferPos = 0

            return extractArray
        } catch (e: Throwable) {
            bufferStorage.forEach { it.fill(0) }
            buffer.fill(0)
            bufferPos = 0

            throw e
        }
    }

    /**
     * Returns the chunks of the buffer. (Almost zero copy operation. Only references are copied here)
     */
    fun extractChunksAndReset(): Pair<Array<ByteArray>, Int> {
        try {
            bufferStorage.add(buffer)

            val extractArray = bufferStorage.toTypedArray()
            val lastBufferPos = bufferPos

            bufferStorage.clear()
            buffer = ByteArray(initialCapacity)
            bufferPos = 0

            return extractArray to lastBufferPos
        } catch (e: Throwable) {
            bufferStorage.clear()
            buffer = ByteArray(initialCapacity)
            bufferPos = 0

            throw e
        }
    }
}