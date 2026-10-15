#![cfg_attr(not(test), no_std)]

use keccakrust::keccakmath;

/// Permutes the 25 lanes in KeccakKotlin's column-major layout in place.
///
/// Returns 0 on success, or -1 for a null pointer or an incorrect lane count.
/// The pointer is borrowed only for the duration of the call.
///
/// # Safety
/// For a non-null pointer and a lane count of 25, `state` must point to 25
/// aligned, initialized, writable u64 values with exclusive access during this call.
#[unsafe(no_mangle)]
pub unsafe extern "C" fn keccakrust_permute(state: *mut u64, lane_count: i32) -> i32 {
    if state.is_null() || lane_count != 25 {
        return -1;
    }

    // The Kotlin adapter validates the length and pins its LongArray for this call.
    let state = unsafe { &mut *state.cast::<[u64; 25]>() };
    keccakmath::permute(state);
    0
}

#[cfg(not(test))]
unsafe extern "C" {
    fn abort() -> !;
}

// Precompiled compiler_builtins can retain an unwind-personality reference even
// when this crate uses panic=abort. Provide it for the no_std static archive;
// any attempted unwinding terminates rather than crossing into Kotlin.
#[cfg(not(test))]
#[unsafe(no_mangle)]
pub extern "C" fn rust_eh_personality() -> ! {
    unsafe { abort() }
}

/*#[cfg(not(test))]
#[panic_handler]
fn panic(_: &core::panic::PanicInfo<'_>) -> ! {
    // A Rust panic must never unwind across the C ABI boundary.
    unsafe { abort() }
}*/

#[cfg(test)]
mod tests {
    use super::keccakrust_permute;

    #[test]
    fn null_state_is_rejected() {
        assert_eq!(unsafe { keccakrust_permute(core::ptr::null_mut(), 25) }, -1);
    }

    #[test]
    fn incorrect_lane_counts_leave_the_state_untouched() {
        for lane_count in [-1, 0, 24, 26] {
            let mut state = [0x8000_0000_0000_0001_u64; 25];
            let original = state;
            assert_eq!(unsafe { keccakrust_permute(state.as_mut_ptr(), lane_count) }, -1);
            assert_eq!(state, original);
        }
    }
}
