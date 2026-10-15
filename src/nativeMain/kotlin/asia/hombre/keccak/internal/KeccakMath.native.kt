/*
 * Copyright 2026 Ron Lauren Hombre
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

package asia.hombre.keccak.internal

import asia.hombre.keccak.rust.keccakrust_permute
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned

internal actual object KeccakMath {
    @Suppress("NOTHING_TO_INLINE")
    actual inline fun pad10n1Direct(bytes: ByteArray, offset: Int, flexiByte: FlexiByte) =
        CommonKeccakMath.pad10n1Direct(bytes, offset, flexiByte)

    @OptIn(ExperimentalForeignApi::class)
    actual fun directPermute(state: LongArray) {
        require(state.size == 25) { "Matrix too small" }

        state.usePinned { pinned ->
            check(keccakrust_permute(pinned.addressOf(0).reinterpret(), state.size) == 0) {
                "Failed to permute state using KeccakRust FFI backend."
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    actual inline fun getLongAt(source: ByteArray, x: Int, y: Int): Long =
        CommonKeccakMath.getLongAt(source, x, y)

    actual fun directMatrixToBytes(matrix: LongArray, destination: ByteArray) =
        CommonKeccakMath.directMatrixToBytes(matrix, destination)

    actual fun encodeToBytes(number: Long, appendOrPrepend: Boolean): ByteArray =
        CommonKeccakMath.encodeToBytes(number, appendOrPrepend)

    actual fun leftEncode(x: Long): ByteArray =
        CommonKeccakMath.leftEncode(x)

    actual fun rightEncode(x: Long): ByteArray =
        CommonKeccakMath.rightEncode(x)
}
