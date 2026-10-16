#ifndef KECCAKKOTLIN_RUST_H
#define KECCAKKOTLIN_RUST_H

#ifdef __cplusplus
extern "C" {
#endif

/* Borrows 25 writable 64-bit lanes in column-major order (5*x + y). */
void keccakrust_permute(unsigned long long *state);

#ifdef __cplusplus
}
#endif

#endif
