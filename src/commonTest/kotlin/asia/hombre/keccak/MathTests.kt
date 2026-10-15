package asia.hombre.keccak

import asia.hombre.keccak.internal.CommonKeccakMath
import asia.hombre.keccak.internal.KeccakMath
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class MathTests {
    @Test
    fun permutationMatchesCommonImplementation() {
        val random = Random(2026)
        val states = listOf(
            LongArray(25),
            LongArray(25) { -1L },
            LongArray(25) { Long.MIN_VALUE },
            LongArray(25) { it.toLong() },
        ) + List(32) { LongArray(25) { random.nextLong() } }

        for ((index, state) in states.withIndex()) {
            val expected = state.copyOf()
            CommonKeccakMath.directPermute(expected)
            KeccakMath.directPermute(state)
            assertContentEquals(expected, state, "Permutation state $index differs from the common implementation")
        }
    }

    @Test
    fun invalidStateSizesAreRejected() {
        for (size in listOf(0, 24, 26)) {
            assertFailsWith<IllegalArgumentException> {
                KeccakMath.directPermute(LongArray(size))
            }
        }
    }
}
