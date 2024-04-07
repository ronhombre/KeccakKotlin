# KeccakKotlin implements SHA-3 Hash Functions
_**Digital security for all, everywhere, no matter who they are, or what they believe in.**_

## Introduction

This is a 100% Kotlin Multiplatform implementation of SHA-3. It does not depend on any third-party library.

> [!WARNING]
> KeccakKotlin is in **BETA** stage. We are confident it works, but it is slow and unoptimized.

### Intent

At the 1.0.0 release, developers from various platforms should be able to use this library if they want to support SHA-3.

## Capabilities
* SHA3-224
* SHA3-256
* SHA3-384
* SHA3-512
* RawSHAKE128 (Extendable, Byte Stream-able)
* RawSHAKE256 (Extendable, Byte Stream-able)
* SHAKE128 (Extendable, Byte Stream-able)
* SHAKE256 (Extendable, Byte Stream-able)

## Supported & Tested Platforms
* JVM (Kotlin)

## Documentation
* TODO

### References

* [Online SHA-3 Keccak Calculator](https://leventozturk.com/engineering/sha3/)
* [NIST CSRC](https://csrc.nist.gov/projects/cryptographic-standards-and-guidelines/example-values)
* [XKCP CompactFIPS202](https://github.com/XKCP/XKCP/blob/master/Standalone/CompactFIPS202/Python/CompactFIPS202.py)
* [FIPS 202](https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.202.pdf)

### License

```
Copyright 2024 Ron Lauren Hombre

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0
       
       and included as LICENSE.txt in this Project.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

Although SHA-3 is Public Domain, this implementation is created through Hard Work by its Contributors.
Thus, the APACHE LICENSE v2.0 has been chosen.