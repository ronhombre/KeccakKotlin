package asia.hombre.keccak

import asia.hombre.keccak.api.*
import asia.hombre.keccak.internal.AbstractKeccakFunction
import asia.hombre.keccak.streams.HashInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalStdlibApi::class)
class Tests {
    /**
     * Tests KAT, Reusability, and Equality all together. Plus, the underlying functions since they are used as well.
     */
    fun standardKATTest(api: AbstractKeccakFunction, inputStream: HashInputStream, length: Int, correct: String, data: ByteArray? = null) {
        data?.let { api.update(it) }
        val digestOutput = api.digest().toHexString()
        data?.let { api.update(it) }
        val streamOutput = api.stream().nextBytes(length).toHexString()
        data?.let { inputStream.write(it) }
        val digestInputStreamOutput = inputStream.close().nextBytes(length).toHexString()

        assertEquals(correct, digestOutput, "${api::class.simpleName}: Digest Output Incorrect!")
        assertEquals(correct, streamOutput, "${api::class.simpleName}: Stream Output Incorrect!")
        assertEquals(correct, digestInputStreamOutput, "${api::class.simpleName}: Digest Input Stream Output Incorrect!")
    }

    @Test
    fun SHA3_224_KAT() {
        val api = SHA3_224()
        val apiInputStream = SHA3_224.newInputStream()

        standardKATTest(api, apiInputStream, api.outputLength, "6b4e03423667dbb73b6e15454f0eb1abd4597f9a1b078e3f5b5a6bc7")
    }

    @Test
    fun SHA3_256_KAT() {
        val api = SHA3_256()
        val apiInputStream = SHA3_256.newInputStream()

        standardKATTest(api, apiInputStream, api.outputLength, "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a")
    }

    @Test
    fun SHA3_384_KAT() {
        val api = SHA3_384()
        val apiInputStream = SHA3_384.newInputStream()

        standardKATTest(api, apiInputStream, api.outputLength, "0c63a75b845e4f7d01107d852e4c2485c51a50aaaa94fc61995e71bbee983a2ac3713831264adb47fb6bd1e058d5f004")
    }

    @Test
    fun SHA3_512_KAT() {
        val api = SHA3_512()
        val apiInputStream = SHA3_512.newInputStream()

        standardKATTest(api, apiInputStream, api.outputLength, "a69f73cca23a9ac5c8b567dc185a756e97c982164fe25859e0d1dcc1475c80a615b2123af1f5f94c11e3e9402c3ac558f500199d95b6d3e301758586281dcd26")
    }

    @Test
    fun SHAKE128_KAT() {
        val api = SHAKE128()
        val apiInputStream = SHAKE128.newInputStream()

        standardKATTest(api, apiInputStream, api.outputLength, "7f9c2ba4e88f827d616045507605853e")
    }

    @Test
    fun SHAKE256_KAT() {
        val api = SHAKE256()
        val apiInputStream = SHAKE256.newInputStream()

        standardKATTest(api, apiInputStream, api.outputLength, "46b9dd2b0ba88d13233b3feb743eeb243fcd52ea62b81b82b50c27646ed5762f")
    }

    @Test
    fun RawSHAKE128_KAT() {
        val api = RawSHAKE128()
        val apiInputStream = RawSHAKE128.newInputStream()

        standardKATTest(api, apiInputStream, api.outputLength, "fa019a3b17630df6014853b5470773f1")
    }

    @Test
    fun RawSHAKE256_KAT() {
        val api = RawSHAKE256()
        val apiInputStream = RawSHAKE256.newInputStream()

        standardKATTest(api, apiInputStream, api.outputLength, "3a1108d4a90a31b85a10bdce77f4bfbdcc5b1d70dd405686f8bbde834aa1a410")
    }

    @Test
    fun cSHAKE128_KAT() {
        val api = cSHAKE128(functionName = "TAK".encodeToByteArray(), customization = "KAT".encodeToByteArray())
        val apiInputStream = cSHAKE128.newInputStream(functionName = "TAK".encodeToByteArray(), customization = "KAT".encodeToByteArray())

        standardKATTest(api, apiInputStream, api.outputLength, "3284fd3b44c6d5e3a3acec6c81cebf62")
    }

    @Test
    fun cSHAKE256_KAT() {
        val api = cSHAKE256(functionName = "TAK".encodeToByteArray(), customization = "KAT".encodeToByteArray())
        val apiInputStream = cSHAKE256.newInputStream(functionName = "TAK".encodeToByteArray(), customization = "KAT".encodeToByteArray())

        standardKATTest(api, apiInputStream, api.outputLength, "c1495f818da538983d382ba9675cad7c44f5df0940d24a6c11d0edcabf235308")
    }

    @Test
    fun KMAC128_KAT() {
        val NIST_KEY = decodeHex("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F")
        val NIST_DATA = decodeHex("00010203")
        val NIST_CUSTOMIZATION = "My Tagged Application".encodeToByteArray()
        val NIST_LENGTH = 256 / 8
        val api = KMAC128(NIST_KEY, NIST_LENGTH, NIST_CUSTOMIZATION)
        val apiInputStream = KMAC128.newInputStream(NIST_KEY, NIST_LENGTH, NIST_CUSTOMIZATION)

        standardKATTest(api, apiInputStream, api.outputLength, "3b1fba963cd8b0b59e8c1a6d71888b7143651af8ba0a7070c0979e2811324aa5", NIST_DATA)
    }

    @Test
    fun KMAC256_KAT() {
        val NIST_KEY = decodeHex("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F")
        val NIST_DATA = decodeHex("00010203")
        val NIST_CUSTOMIZATION = "My Tagged Application".encodeToByteArray()
        val NIST_LENGTH = 512 / 8
        val api = KMAC256(NIST_KEY, NIST_LENGTH, NIST_CUSTOMIZATION)
        val apiInputStream = KMAC256.newInputStream(NIST_KEY, NIST_LENGTH, NIST_CUSTOMIZATION)

        standardKATTest(api, apiInputStream, api.outputLength, "20c570c31346f703c9ac36c61c03cb64c3970d0cfc787e9b79599d273a68d2f7f69d4cc3de9d104a351689f27cf6f5951f0103f33f4f24871024d9c27773a8dd", NIST_DATA)
    }

    @Test
    fun KMACXOF128_KAT() {
        val NIST_KEY = decodeHex("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F")
        val NIST_DATA = decodeHex("00010203")
        val NIST_CUSTOMIZATION = "My Tagged Application".encodeToByteArray()
        val api = KMACXOF128(NIST_KEY, customization = NIST_CUSTOMIZATION)
        val apiInputStream = KMACXOF128.newInputStream(NIST_KEY, NIST_CUSTOMIZATION)

        standardKATTest(api, apiInputStream, api.outputLength, "31a44527b4ed9f5c6101d11de6d26f06", NIST_DATA)
    }

    @Test
    fun KMACXOF256_KAT() {
        val NIST_KEY = decodeHex("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F")
        val NIST_DATA = decodeHex("00010203")
        val NIST_CUSTOMIZATION = "My Tagged Application".encodeToByteArray()
        val api = KMACXOF256(NIST_KEY, customization = NIST_CUSTOMIZATION)
        val apiInputStream = KMACXOF256.newInputStream(NIST_KEY, NIST_CUSTOMIZATION)

        standardKATTest(api, apiInputStream, api.outputLength, "1755133f1534752aad0748f2c706fb5c784512cab835cd15676b16c0c6647fa9", NIST_DATA)
    }

    fun readTooManyBytes(api: AbstractKeccakFunction, length: Int) {
        val stream = api.stream()
        stream.nextBytes(length)
        stream.nextByte()
    }

    @Test
    fun SHA3_224_UNEXTENDABLE() {
        assertFails {
            val api = SHA3_224()
            readTooManyBytes(api, api.outputLength)
        }
    }

    @Test
    fun SHA3_256_UNEXTENDABLE() {
        assertFails {
            val api = SHA3_256()
            readTooManyBytes(api, api.outputLength)
        }
    }

    @Test
    fun SHA3_384_UNEXTENDABLE() {
        assertFails {
            val api = SHA3_384()
            readTooManyBytes(api, api.outputLength)
        }
    }

    @Test
    fun SHA3_512_UNEXTENDABLE() {
        assertFails {
            val api = SHA3_512()
            readTooManyBytes(api, api.outputLength)
        }
    }

    @Test
    fun KMAC128_UNEXTENDABLE() {
        assertFails {
            val api = KMAC128(ByteArray(0))
            readTooManyBytes(api, api.outputLength)
        }
    }

    @Test
    fun KMAC256_UNEXTENDABLE() {
        assertFails {
            val api = KMAC256(ByteArray(0))
            readTooManyBytes(api, api.outputLength)
        }
    }

    /**
     * Decodes a hex string sequence to a byte array.
     */
    fun decodeHex(string: String): ByteArray {
        var hexString = string

        if(string.length % 2 == 1)
            hexString += '0' //Append a 0 if the hex is not even to fit into a byte.

        if(string.contains(Regex("[^A-Fa-f0-9]")))
            throw IllegalArgumentException("String cannot contain characters that is not hex characters.")

        return hexString.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}