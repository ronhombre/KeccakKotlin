import asia.hombre.keccak.internal.KeccakMath
import kotlin.random.Random
import kotlin.random.nextULong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Test {
    @Test
    fun playground() {

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun SHAKE128_CSRC_1600bit() {
        val matrix = arrayOf(
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u),
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0u),
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0u),
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0u),
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0u)
        )

        val final = KeccakMath.permute(matrix)

        val expectedFinal = arrayOf(
            ulongArrayOf(0x7faaf4d610e331d4u, 0x5dca2cb6b8135ef3u, 0x9685ddd2d1fb3436u, 0xe3fe653fade68ae4u, 0xf1ae12a1024a32d9u),
            ulongArrayOf(0xfc27f4f1e29ac527u, 0x3e4c41332facd237u, 0x7116548228f2b75bu, 0xefaad2efd5e05e2bu, 0x169af419f135e342u),
            ulongArrayOf(0x5fee7d55aa1a19fau, 0xaf50852021380859u, 0xe12f9c5d8e1f7162u, 0xe9eab8dd8028d528u, 0xf63d920ed69388a9u),
            ulongArrayOf(0xe575f6bd726c65f1u, 0x4e2d8df4e14ccfb6u, 0x168339a7d1d4a375u, 0x6a53d41257f5dfceu, 0x9e54006c34ab489au),
            ulongArrayOf(0xaf17793d1c9684d5u, 0xc51a82a2779d0d31u, 0xb0c71017f6a8b2c4u, 0xf0b716798d1c0d71u, 0x85b91bb9d95adf58u)
        )

        assertTrue(expectedFinal.contentDeepEquals(final), "Incorrect SHAKE128 final state from 1600 bit input!")
    }

    @Test
    fun longToBytesConversion() {
        val randomLong = Random.nextULong()
        val bytes = KeccakMath.ulongToBytes(randomLong)
        val convertedLong = KeccakMath.bytesToULong(bytes)

        assertEquals(randomLong, convertedLong, "Unequal ULong Conversion!")
    }
}