package asia.hombre.keccak

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.jvm.JvmField

/**
 * Parameter sets for SHA-3(Keccak).
 *
 * This class contains the defined parameter values for each set of SHA-3 Hash Functions defined in FIPS 202.
 *
 * @author Ron Lauren Hombre
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
enum class KeccakParameter(val minLength: Int, val maxLength: Int, val BITRATE: Int, val CAPACITY: Int, val SUFFIX: FlexiByte) {
    /**
     * Keccak[448](M||01, 224)
     */
    SHA3_224(224, 224, 1152, 448, FlexiByte((0b10).toByte(), 1)),

    /**
     * Keccak[512](M||01, 256)
     */
    SHA3_256(256, 256, 1088, 512, FlexiByte((0b10).toByte(), 1)),

    /**
     * Keccak[768](M||01, 384)
     */
    SHA3_384(384, 384, 832, 768, FlexiByte((0b10).toByte(), 1)),

    /**
     * Keccak[1024](M||01, 512)
     */
    SHA3_512(512, 512, 576, 1024, FlexiByte((0b10).toByte(), 1)),

    /**
     * Keccak[256](M||11, d)
     */
    RAWSHAKE_128(128, 0, 1344, 256, FlexiByte((0b11).toByte(), 1)),

    /**
     * Keccak[512](M||11, d)
     */
    RAWSHAKE_256(256, 0, 1088, 512, FlexiByte((0b11).toByte(), 1)),

    /**
     * Keccak[256](M||1111, d) = RawSHAKE128(M||11, d)
     */
    SHAKE_128(128, 0, 1344, 256, FlexiByte((0b1111).toByte(), 3)),

    /**
     * Keccak[512](M||1111, d) = RawSHAKE256(M||11, d)
     */
    SHAKE_256(256, 0, 1088, 512, FlexiByte((0b1111).toByte(), 3));

    /**
     * The byte rate of the Hash Function.
     */
    @JvmField
    val BYTERATE = BITRATE / 8
}