# KeccakKotlin (2.0.1)
## _Implements SHA-3 Hash Functions_
_**Digital security for all, everywhere, no matter who they are, or what they believe in.**_

[![Maven Central](https://img.shields.io/maven-central/v/asia.hombre/keccak.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22asia.hombre%22)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![JS](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Linux Arm64 & X64](https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black)
![Windows X64](https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white)
![iOS Arm64 & X64](https://img.shields.io/badge/iOS-000000?style=for-the-badge&logo=ios&logoColor=white)
![Android Arm32, Arm64, & X64](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

## Introduction

This is a 100% Kotlin Multiplatform implementation of SHA-3. It does not depend on any third-party library.

This is used in [KyberKotlin](https://github.com/ronhombre/KyberKotlin), an ML-KEM implemention of NIST FIPS 203.

## 2.0.1 Update!
> [!NOTE]
> Uses 1/5th of memory allocations compared to version 2.0.0 when used in KyberKotlin. It brought down 275.57GB to
> 53.88GB which is about ~20% of the previous version! It also minimally made for some speed improvements. (Tested on
> JVMBenchmark in KyberKotlin).
> No need to change anything in your codebase. This is purely an internal optimization.

> [!NOTE]
> OLD NOTE: This major update brings in a Standard API to use the Keccak Hash Functions as well as a new HashInputStream and
> HashOutputStream classes. Furthermore, SHA-3 Derived Hash Functions have been implemented as well!

## Capabilities (NIST FIPS 202)
* SHA3-224 (Byte Stream-able)
* SHA3-256 (Byte Stream-able)
* SHA3-384 (Byte Stream-able)
* SHA3-512 (Byte Stream-able)
* RawSHAKE128 (Extendable, Byte Stream-able)
* RawSHAKE256 (Extendable, Byte Stream-able)
* SHAKE128 (Extendable, Byte Stream-able)
* SHAKE256 (Extendable, Byte Stream-able)

## Extended Capabilities / SHA-3 Derived Functions (NIST SP 800-185)
* cSHAKE128 (Extendable, Byte Stream-able)
* cSHAKE256 (Extendable, Byte Stream-able)
* KMAC128 (Byte Stream-able)
* KMAC256 (Byte Stream-able)
* KMACXOF128 (Extendable, Byte Stream-able)
* KMACXOF256 (Extendable, Byte Stream-able)

## To Be Supported
> [!NOTE]
> These hash functions will be gradually implemented.

* TupleHash128 (Byte Stream-able)
* TupleHash256 (Byte Stream-able)
* TupleHashXOF128 (Extendable, Byte Stream-able)
* TupleHashXOF256 (Extendable, Byte Stream-able)
* ParallelHash128 (Byte Stream-able)
* ParallelHash256 (Byte Stream-able)
* ParallelHashXOF128 (Extendable, Byte Stream-able)
* ParallelHashXOF256 (Extendable, Byte Stream-able)

ParallelHash Functions might need to add coroutine as a dependency.

## Tested Platforms
* JVM (Kotlin, Java)

## Supported Targets

| Target                    | Arm32              | Arm64              | X64                |
|---------------------------|--------------------|--------------------|--------------------|
| JVM (Kotlin & Java)       | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| JS (Node, Bun, & Browser) | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| Linux                     | :x: *              | :x: **             | :white_check_mark: |
| Windows (Mingw)           | :x: *              | :x: *              | :white_check_mark: |
| Android                   | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| iOS                       | :x: *              | :x: **             | :x: **             |
| iOS Simulator             | :x: *              | :x: **             | :x: *              |

*Note: Some platforms are unavailable/deprecated as targets in Kotlin Multiplatform. Please send your complaints to Jetbrains.

**These targets are currently available, but I have no ability to compile them **yet**. Once I have more free time, I will set up a publishing server to compile to all targets.

## Installation
Maven/Gradle
```kotlin
dependencies {
    implementation("asia.hombre:keccak:2.0.1")
}
```

## Usage
```kotlin
val sha3_224 = SHA3_224().digest()
println(sha3_224.toHexString(HexFormat.UpperCase))
val sha3_256 = SHA3_256().digest()
println(sha3_256.toHexString(HexFormat.UpperCase))
val sha3_384 = SHA3_384().digest()
println(sha3_384.toHexString(HexFormat.UpperCase))
val sha3_512 = SHA3_512().digest()
println(sha3_512.toHexString(HexFormat.UpperCase))

val kmac_128 = KMAC128("key".encodeToByteArray(), 32).digest()
println(kmac_128.toHexString(HexFormat.UpperCase))
val kmac_256 = KMAC256("key".encodeToByteArray(), 64).digest()
println(kmac_256.toHexString(HexFormat.UpperCase))

//Extendable-Output Functions
val rawshake_128 = RawSHAKE128().digest()
println(rawshake_128.toHexString(HexFormat.UpperCase))
val rawshake_256 = RawSHAKE256().digest()
println(rawshake_256.toHexString(HexFormat.UpperCase))
val shake_128 = SHAKE128().digest()
println(shake_128.toHexString(HexFormat.UpperCase))
val shake_256 = SHAKE256().digest()
println(shake_256.toHexString(HexFormat.UpperCase))
val cshake_128 = cSHAKE128().digest()
println(cshake_128.toHexString(HexFormat.UpperCase))
val cshake_256 = cSHAKE256().digest()
println(cshake_256.toHexString(HexFormat.UpperCase))
val kmacxof_128 = KMACXOF128("key".encodeToByteArray()).digest()
println(kmacxof_128.toHexString(HexFormat.UpperCase))
val kmacxof_256 = KMACXOF256("key".encodeToByteArray()).digest()
println(kmacxof_256.toHexString(HexFormat.UpperCase))

//Input Streaming
val shake128_in = SHAKE128.newInputStream()
shake128_in.write("Hello!".encodeToByteArray())
//Output Streaming
val shake128_out = shake128_in.close()
println(shake128_out.nextBytes(1024).toHexString(HexFormat.UpperCase))
```

View the java example [here](https://github.com/ronhombre/KeccakKotlin/blob/master/java-example/src/main/java/asia/hombre/examples/keccak/Main.java)!

## Documentation
* [keccak.hombre.asia](https://keccak.hombre.asia)

### References

* [Online SHA-3 Keccak Calculator](https://leventozturk.com/engineering/sha3/)
* [NIST CSRC](https://csrc.nist.gov/projects/cryptographic-standards-and-guidelines/example-values)
* [XKCP CompactFIPS202](https://github.com/XKCP/XKCP/blob/master/Standalone/CompactFIPS202/Python/CompactFIPS202.py)
* [FIPS 202](https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.202.pdf)

### License

```
Copyright 2025 Ron Lauren Hombre

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
