package asia.hombre.keccak

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.jvm.JvmField

/**
 * Constants for SHA-3.
 *
 * This class contains precomputed values for ease of use and optimization purposes.
 *
 * @author Ron Lauren Hombre
 */
@OptIn(ExperimentalUnsignedTypes::class, ExperimentalJsExport::class)
@JsExport
class KeccakConstants {
    companion object {
        /**
         * Iota modifications
         */
        val ROUND = longArrayOf(
            1, 32898, -9223372036854742902, -9223372034707259392,
            32907, 2147483649, -9223372034707259263, -9223372036854743031,
            138, 136, 2147516425, 2147483658,
            2147516555, -9223372036854775669, -9223372036854742903, -9223372036854743037,
            -9223372036854743038, -9223372036854775680, 32778, -9223372034707292150,
            -9223372034707259263, -9223372036854742912, 2147483649, -9223372034707259384
        )
    }
}