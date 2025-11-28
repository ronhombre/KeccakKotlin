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

import asia.hombre.keccak.internal.KeccakMath

/**
 * Constants for Keccak.
 *
 * This class contains precomputed values for ease of use and optimization purposes.
 *
 * @author Ron Lauren Hombre
 */
object KeccakConstants {
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

    val KMAC_BYTES = byteArrayOf(
        'K'.code.toByte(),
        'M'.code.toByte(),
        'A'.code.toByte(),
        'C'.code.toByte()
    )
    val KMACXOF_RIGHT_ENCODED = KeccakMath.rightEncode(0L)
}