#![no_std]

use keccakrust::keccakmath;

/// Permutes the 25 lanes in KeccakKotlin's column-major layout in place.
///
/// Returns 0 on success
///
/// # Safety
/// For a non-null pointer and a lane count of 25, `state` must point to 25
/// aligned, initialized, writable u64 values with exclusive access during this call.
#[unsafe(no_mangle)]
pub unsafe extern "C" fn keccakrust_permute(state: *mut u64) {
    let state = unsafe { &mut *state.cast::<[u64; 25]>() };

    keccakmath::permute(state);
}

#[cfg(not(test))]
unsafe extern "C" {
    fn abort() -> !;
}

// Precompiled compiler_builtins can retain an unwind-personality reference even
// when this crate uses panic=abort. Provide it for the no_std static archive;
// any attempted unwinding terminates rather than crossing into Kotlin.
// Before C interop embeds the archive, Gradle weakens this fallback on ELF/Mach-O
// or renames it and its references to keccakrust_eh_personality on Windows COFF.
#[cfg(not(test))]
#[unsafe(no_mangle)]
pub extern "C" fn rust_eh_personality() -> ! {
    unsafe { abort() }
}
