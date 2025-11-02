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

package asia.hombre.keccak

import asia.hombre.keccak.internal.FlexiByte
import kotlin.jvm.JvmField

/**
 * Parameter sets for SHA-3(Keccak).
 *
 * This class contains the defined parameter values for each set of SHA-3 Hash Functions defined in FIPS 202 and SHA-3
 * Derived Functions defined in SP 800-185.
 *
 * @author Ron Lauren Hombre
 */
enum class KeccakParameter(val minLength: Int, val maxLength: Int, val BITRATE: Int, val CAPACITY: Int, internal val SUFFIX: FlexiByte) {
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
    SHAKE_256(256, 0, 1088, 512, FlexiByte((0b1111).toByte(), 3)),

    /**
     * SHAKE128 equivalent if N and S is empty "".
     *
     * Keccak[256](bytepad(encode_string(N)||encode_string(S), 168)||X||00, d) = cSHAKE128(encode_string(N)||encode_string(S), 168)||X||00, d)
     */
    CSHAKE_128(128, 0, 1344, 256, FlexiByte((0b00).toByte(), 1)),

    /**
     * SHAKE256 equivalent if N and S is empty "".
     *
     * Keccak[512](bytepad(encode_string(N)||encode_string(S), 136)||X||00, d) = cSHAKE128(encode_string(N)||encode_string(S), 136)||X||00, d)
     */
    CSHAKE_256(256, 0, 1088, 512, FlexiByte((0b00).toByte(), 1)),

    /**
     * newX = bytepad(encode_string(K), 136) || X || right_encode(L);
     * T = bytepad(encode_string("KMAC") || encode_string(S), 136);
     * Keccak[256](T || newX || 00, L)
     */
    KMAC_128(128, 0, 1344, 256, FlexiByte((0b00).toByte(), 1)),

    /**
     * newX = bytepad(encode_string(K), 168) || X || right_encode(L);
     * T = bytepad(encode_string("KMAC") || encode_string(S), 168);
     * Keccak[512](T || newX || 00, L)
     */
    KMAC_256(256, 0, 1088, 512, FlexiByte((0b00).toByte(), 1)),

    /**
     * newX = bytepad(encode_string(K), 136) || X || right_encode(0);
     * T = bytepad(encode_string("KMAC") || encode_string(S), 136);
     * Keccak[256](T || newX || 00, L)
     */
    KMACXOF_128(128, 0, 1344, 256, FlexiByte((0b00).toByte(), 1)),

    /**
     * newX = bytepad(encode_string(K), 168) || X || right_encode(0);
     * T = bytepad(encode_string("KMAC") || encode_string(S), 168);
     * Keccak[512](T || newX || 00, L)
     */
    KMACXOF_256(256, 0, 1088, 512, FlexiByte((0b00).toByte(), 1));

    /**
     * The byte rate of the Hash Function.
     */
    @JvmField
    val BYTERATE = BITRATE shr 3
}