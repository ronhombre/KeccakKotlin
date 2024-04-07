package asia.hombre.keccak

/**
 * Constants for SHA-3.
 *
 * This class contains precomputed values for ease of use and optimization purposes.
 *
 * @author Ron Lauren Hombre
 */
@OptIn(ExperimentalUnsignedTypes::class)
class KeccakConstants {
    companion object {
        val ROUND = ulongArrayOf(
            0x0000000000000001u, 0x0000000000008082u, 0x800000000000808Au, 0x8000000080008000u,
            0x000000000000808Bu, 0x0000000080000001u, 0x8000000080008081u, 0x8000000000008009u,
            0x000000000000008Au, 0x0000000000000088u, 0x0000000080008009u, 0x000000008000000Au,
            0x000000008000808Bu, 0x800000000000008Bu, 0x8000000000008089u, 0x8000000000008003u,
            0x8000000000008002u, 0x8000000000000080u, 0x000000000000800Au, 0x800000008000000Au,
            0x8000000080008081u, 0x8000000000008080u, 0x0000000080000001u, 0x8000000080008008u
        )
    }
}