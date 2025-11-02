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
import asia.hombre.keccak.internal.KeccakMath
import asia.hombre.keccak.internal.SplitByteArray
import kotlin.jvm.JvmSynthetic
import kotlin.math.min

/**
 * A generator class for all the Keccak Hash Functions.
 *
 * @author Ron Lauren Hombre
 */
object KeccakHash {
    @JvmSynthetic
    internal fun generateDirect(parameter: KeccakParameter, chunks: Pair<Array<ByteArray>, Int>, suffix: FlexiByte, stateBuffer: SplitByteArray, state: Array<LongArray>) {
        val addAnother = chunks.second == parameter.BYTERATE
        val validatedChunks = Array<ByteArray>(chunks.first.size + (if(addAnother) 1 else 0)) { i ->
            if(addAnother && i == chunks.first.size) {
                ByteArray(parameter.BYTERATE)
            } else {
                chunks.first[i]
            }
        }

        KeccakMath.pad10n1Direct(validatedChunks[validatedChunks.lastIndex], (if(addAnother) 0 else chunks.second), suffix)

        validatedChunks.forEachIndexed { i, it ->
            stateBuffer.a = it

            for(x in 0..<5) for(y in 0..<5)
                state[x][y] = state[x][y] xor KeccakMath.getLongAt(stateBuffer, x, y, parameter.BYTERATE)

            KeccakMath.directPermute(state)
        }
    }

    @JvmSynthetic
    internal fun generateDirectOutput(parameter: KeccakParameter, length: Int, chunks: Pair<Array<ByteArray>, Int>, suffix: FlexiByte): ByteArray {
        val stateBuffer = SplitByteArray(ByteArray(parameter.BYTERATE), ByteArray(200 - parameter.BYTERATE))
        val state = Array(5) { LongArray(5) }

        generateDirect(parameter, chunks, suffix, stateBuffer, state)

        stateBuffer.a = ByteArray(parameter.BYTERATE)

        val output = ByteArray(length)
        var totalOutput = 0

        while (totalOutput < length) {
            KeccakMath.directMatrixToBytes(state, stateBuffer)

            val toCopy = min(length - totalOutput, parameter.BYTERATE)

            stateBuffer.a
                .copyInto(output, totalOutput, 0, toCopy) //We assume here that we will only see toCopy < BYTERATE if it's the last few bytes.
                .also { totalOutput += toCopy }

            if(totalOutput < length) KeccakMath.directPermute(state)
        }

        return output
    }
}