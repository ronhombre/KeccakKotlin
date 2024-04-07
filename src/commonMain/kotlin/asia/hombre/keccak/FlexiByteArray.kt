package asia.hombre.keccak

import kotlin.experimental.or
import kotlin.math.max
import kotlin.math.min

/**
 * A ByteArray with the capabilities of a FlexiByte.
 *
 * Allows modification of the ending byte's individual bits.
 *
 * @author Ron Lauren Hombre
 */
class FlexiByteArray {
    private var byteArray: ByteArray

    /**
     * The index of the last bit.
     */
    var bitIndex: Int = 0
        private set(value) { field = min(max(0, value), 7) } //Bound to 0 - 7

    /**
     * Create a FlexiByteArray with a specific size and initialize with a specific value.
     */
    constructor(size: Int, init: (Int) -> Byte) {
        byteArray = ByteArray(size, init)
    }

    /**
     * Wrap a ByteArray and set the bit index of the last byte.
     */
    constructor(byteArray: ByteArray, bitIndex: Int = 7) {
        this.byteArray = byteArray.copyOf()
        this.bitIndex = bitIndex
    }

    /**
     * The indices of this array.
     */
    val indices: IntRange
        get() = byteArray.indices

    /**
     * The last index of this array.
     */
    val lastIndex: Int
        get() = byteArray.lastIndex

    /**
     * The size of this array.
     */
    val size: Int
        get() = byteArray.size

    operator fun get(index: Int): Byte {
        return byteArray[index]
    }
    operator fun iterator(): ByteIterator {
        return byteArray.iterator()
    }
    operator fun set(index: Int, value: Byte) {
        return byteArray.set(index, value)
    }

    /**
     * Create an independent copy of this class.
     */
    fun copyOf(): FlexiByteArray {
        return FlexiByteArray(byteArray.copyOf(), bitIndex)
    }

    /**
     * Copy values into another [FlexiByteArray].
     */
    fun copyInto(flexiByteArray: FlexiByteArray, destinationOffset: Int = 0) {
        byteArray.copyInto(flexiByteArray.byteArray, destinationOffset)
        flexiByteArray.bitIndex = bitIndex
    }

    /**
     * Extend the array by a specified length.
     */
    fun extend(nBytes: Int) {
        byteArray = byteArray.copyOf(byteArray.size + nBytes)
    }

    /**
     * Fill all the bytes in this array with the specified byte value.
     *
     * This resets the bit index to 7.
     */
    fun fill(byte: Byte) {
        byteArray.fill(byte)
        this.bitIndex = 7
    }

    /**
     * Manually move the bit index.
     */
    fun moveBitIndex(index: Int) {
        this.bitIndex = index
    }

    /**
     * Reset the bit index to 7.
     */
    fun reset() {
        bitIndex = 7
    }

    /**
     * Convert to a [ByteArray].
     */
    fun toByteArray(): ByteArray {
        return byteArray.copyOf()
    }

    operator fun plus(flexiByte: FlexiByte): FlexiByteArray {
        val totalIndex = flexiByte.bitIndex + bitIndex + 1
        var newArrayLength = byteArray.size
        val newIndex = totalIndex % 8

        if(totalIndex > 7)
            newArrayLength++

        val newByteArray = byteArray.copyOf(newArrayLength)

        if(totalIndex > 7)
            newByteArray[newByteArray.lastIndex] = (flexiByte.toByte().toUInt() shl (7 - bitIndex)).toByte()
        else
            newByteArray[byteArray.lastIndex] = newByteArray[byteArray.lastIndex] or (flexiByte.toByte().toUByte().toUInt() shl (bitIndex + 1)).toByte()

        return FlexiByteArray(newByteArray, newIndex)
    }

    /**
     * Fill all the leftover bits at the end to complete a byte.
     */
    fun completeFill(bit: Boolean) {
        if(bit)
            byteArray[byteArray.lastIndex] = (byteArray[byteArray.lastIndex].toUInt() or (0b11111111u shr (bitIndex + 1))).toByte()
        else
            byteArray[byteArray.lastIndex] = (byteArray[byteArray.lastIndex].toUInt() and (0b11111111u shl (7 - bitIndex))).toByte()
        bitIndex = 7
    }
}