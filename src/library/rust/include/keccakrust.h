#ifndef KECCAKKOTLIN_RUST_H
#define KECCAKKOTLIN_RUST_H

#ifdef __cplusplus
extern "C" {
#endif

/* Borrows 25 writable 64-bit lanes in column-major order (5*x + y).
 * Returns 0 on success, -1 for null state or an incorrect lane count. */
int keccakrust_permute(unsigned long long *state, int lane_count);

#ifdef __cplusplus
}
#endif

#endif
