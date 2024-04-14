package asia.hombre.keccak

import asia.hombre.keccak.internal.KeccakMath
import kotlin.random.Random
import kotlin.random.nextULong
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalUnsignedTypes::class)
class Tests {
    @Test
    fun playground() {
        /*val sha3_224 = KeccakHash.generate(KeccakParameter.SHA3_224, "".encodeToByteArray())
        println(sha3_224.toHexString(HexFormat.UpperCase))
        val sha3_256 = KeccakHash.generate(KeccakParameter.SHA3_256, "".encodeToByteArray())
        println(sha3_256.toHexString(HexFormat.UpperCase))
        val sha3_384 = KeccakHash.generate(KeccakParameter.SHA3_384, "".encodeToByteArray())
        println(sha3_384.toHexString(HexFormat.UpperCase))
        val sha3_512 = KeccakHash.generate(KeccakParameter.SHA3_512, "".encodeToByteArray())
        println(sha3_512.toHexString(HexFormat.UpperCase))

        //Extendable-Output Functions
        //A third parameter called 'lengthInBytes' is used to modify the output length.
        val rawshake_128 = KeccakHash.generate(KeccakParameter.RAWSHAKE_128, "".encodeToByteArray())
        println(rawshake_128.toHexString(HexFormat.UpperCase))
        val rawshake_256 = KeccakHash.generate(KeccakParameter.RAWSHAKE_256, "".encodeToByteArray())
        println(rawshake_256.toHexString(HexFormat.UpperCase))
        val shake_128 = KeccakHash.generate(KeccakParameter.SHAKE_128, "".encodeToByteArray())
        println(shake_128.toHexString(HexFormat.UpperCase))
        val shake_256 = KeccakHash.generate(KeccakParameter.SHAKE_256, "".encodeToByteArray())
        println(shake_256.toHexString(HexFormat.UpperCase))*/
    }

    //Ensure that padding optimizations are equal.
    @Test
    fun PADDING_EQUALITY() {
        for(p in KeccakParameter.entries.indices) {
            val param = KeccakParameter.entries[p]
            val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            var input = ""
            for(i in 0..<param.BYTERATE * 2) {
                input += alphabet[Random.nextInt(0, 52)]
                val inputBytes = input.encodeToByteArray()
                val paddedInput = KeccakMath.pad10n1(inputBytes, param.BITRATE, param.SUFFIX)
                val comparedPaddedInput = KeccakMath.pad10n1Flex(FlexiByteArray(inputBytes) + param.SUFFIX, param.BITRATE).toByteArray()

                assertContentEquals(paddedInput, comparedPaddedInput, "Padding equality failed at $input with length: " + input.length + " on Parameter: " + param.name + "!")
            }
        }
    }

    //Ensure that both KeccakByteStream and KeccakHash generate the same hash outputs.
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun HASH_EQUALITY() {
        for(p in KeccakParameter.entries.indices) {
            val param = KeccakParameter.entries[p]
            val input = "".encodeToByteArray()

            val standardOutput = KeccakHash.generate(param, input, param.BYTERATE * 2).toHexString()
            var streamOutput = ""

            val keccakByteStream = KeccakByteStream(param)
            keccakByteStream.absorb(input)

            while(keccakByteStream.hasNext && streamOutput.length < standardOutput.length) {
                streamOutput += keccakByteStream.next().toHexString()
            }

            assertEquals(standardOutput, streamOutput, "Hash equality on Parameter: " + param.name + "!")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun KNOWN_ANSWER_TEST() {
        val sha3_224 = KeccakHash.generate(KeccakParameter.SHA3_224, "".encodeToByteArray())
        assertEquals(
            sha3_224.toHexString(),
            "6b4e03423667dbb73b6e15454f0eb1abd4597f9a1b078e3f5b5a6bc7",
            "Incorrect SHA3-224 answer!")
        val sha3_256 = KeccakHash.generate(KeccakParameter.SHA3_256, "".encodeToByteArray())
        assertEquals(
            sha3_256.toHexString(),
            "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a",
            "Incorrect SHA3-256 answer!")
        val sha3_384 = KeccakHash.generate(KeccakParameter.SHA3_384, "".encodeToByteArray())
        assertEquals(
            sha3_384.toHexString(),
            "0c63a75b845e4f7d01107d852e4c2485c51a50aaaa94fc61995e71bbee983a2ac3713831264adb47fb6bd1e058d5f004",
            "Incorrect SHA3-384 answer!")
        val sha3_512 = KeccakHash.generate(KeccakParameter.SHA3_512, "".encodeToByteArray())
        assertEquals(
            sha3_512.toHexString(),
            "a69f73cca23a9ac5c8b567dc185a756e97c982164fe25859e0d1dcc1475c80a615b2123af1f5f94c11e3e9402c3ac558f500199d95b6d3e301758586281dcd26",
            "Incorrect SHA3-512 answer!")

        //Extendable-Output Functions
        //A third parameter called 'lengthInBytes' is used to modify the output length.
        val rawshake_128 = KeccakHash.generate(KeccakParameter.RAWSHAKE_128, "".encodeToByteArray())
        assertEquals(
            rawshake_128.toHexString(),
            "fa019a3b17630df6014853b5470773f1",
            "Incorrect RawSHAKE128 answer!")
        val rawshake_256 = KeccakHash.generate(KeccakParameter.RAWSHAKE_256, "".encodeToByteArray())
        assertEquals(
            rawshake_256.toHexString(),
            "3a1108d4a90a31b85a10bdce77f4bfbdcc5b1d70dd405686f8bbde834aa1a410",
            "Incorrect RawSHAKE256 answer!")
        val shake_128 = KeccakHash.generate(KeccakParameter.SHAKE_128, "".encodeToByteArray(), 256 / 8)
        assertEquals(
            shake_128.toHexString(),
            "7f9c2ba4e88f827d616045507605853ed73b8093f6efbc88eb1a6eacfa66ef26",
            "Incorrect SHAKE128 answer!")
        val shake_256 = KeccakHash.generate(KeccakParameter.SHAKE_256, "".encodeToByteArray(), 512 / 8)
        assertEquals(
            shake_256.toHexString(),
            "46b9dd2b0ba88d13233b3feb743eeb243fcd52ea62b81b82b50c27646ed5762fd75dc4ddd8c0f200cb05019d67b592f6fc821c49479ab48640292eacb3b7c4be",
            "Incorrect SHAKE256 answer!")
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun SHAKE128_CSRC_1600bit() {
        val matrix = arrayOf(
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u).toLongArray(),
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0u).toLongArray(),
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0u).toLongArray(),
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0u).toLongArray(),
            ulongArrayOf(0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0xa3a3a3a3a3a3a3a3u, 0u).toLongArray()
        )

        val final = KeccakMath.permute(matrix)

        val expectedFinal = arrayOf(
            ulongArrayOf(0x7faaf4d610e331d4u, 0x5dca2cb6b8135ef3u, 0x9685ddd2d1fb3436u, 0xe3fe653fade68ae4u, 0xf1ae12a1024a32d9u).toLongArray(),
            ulongArrayOf(0xfc27f4f1e29ac527u, 0x3e4c41332facd237u, 0x7116548228f2b75bu, 0xefaad2efd5e05e2bu, 0x169af419f135e342u).toLongArray(),
            ulongArrayOf(0x5fee7d55aa1a19fau, 0xaf50852021380859u, 0xe12f9c5d8e1f7162u, 0xe9eab8dd8028d528u, 0xf63d920ed69388a9u).toLongArray(),
            ulongArrayOf(0xe575f6bd726c65f1u, 0x4e2d8df4e14ccfb6u, 0x168339a7d1d4a375u, 0x6a53d41257f5dfceu, 0x9e54006c34ab489au).toLongArray(),
            ulongArrayOf(0xaf17793d1c9684d5u, 0xc51a82a2779d0d31u, 0xb0c71017f6a8b2c4u, 0xf0b716798d1c0d71u, 0x85b91bb9d95adf58u).toLongArray()
        )

        assertTrue(expectedFinal.contentDeepEquals(final), "Incorrect Experimental SHAKE128 final state from 1600 bit input!")
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun longToBytesConversion() {
        val randomLong = Random.nextLong()
        val bytes = KeccakMath.longToBytes(randomLong)
        val convertedLong = KeccakMath.bytesToLong(bytes)

        assertEquals(randomLong, convertedLong, "Unequal Long Conversion!")
    }
}