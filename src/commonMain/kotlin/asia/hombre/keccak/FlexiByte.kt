package asia.hombre.keccak

import kotlin.experimental.or
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.math.min

/**
 * This is an extension of the Byte class. It acts like it, but it is not it.
 *
 * A FlexiByte offers better control over individual bits.
 *
 * @author Ron Lauren Hombre
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class FlexiByte(private val byte: Byte, val bitIndex: Int) : Number(), Comparable<Byte> {
    companion object {
        /**
         * Construct a [FlexiByte] from a string of 1s and 0s.
         */
        fun fromString(string: String): FlexiByte {
            if(string.contains(Regex("[^0-1]")))
                throw IllegalArgumentException("FlexiByte string can only contain 1s and 0s.")

            val cleanedInput = string.trim()

            if(cleanedInput.length > 8)
                throw IllegalArgumentException("A Byte can only have 8 binary values. Trimmed Length: " + cleanedInput.length)

            var outputByte: Byte = 0
            val bitIndex = min(string.length, 8) - 1

            //Should this be modified to be constant time?
            for(i in bitIndex downTo 0) {
                outputByte = outputByte or ((if(cleanedInput[i] == '1') 0b1 else 0b0) shl i).toByte()
            }

            return FlexiByte(outputByte, bitIndex)
        }
    }

    override fun compareTo(other: Byte): Int {
        return byte.compareTo(other)
    }

    override fun toByte(): Byte {
        return byte
    }

    override fun toDouble(): Double {
        return byte.toDouble()
    }

    override fun toFloat(): Float {
        return byte.toFloat()
    }

    override fun toInt(): Int {
        return byte.toInt()
    }

    override fun toLong(): Long {
        return byte.toLong()
    }

    override fun toShort(): Short {
        return byte.toShort()
    }
}