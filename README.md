# KeccakKotlin
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

## Capabilities
* SHA3-224
* SHA3-256
* SHA3-384
* SHA3-512
* RawSHAKE128 (Extendable, Byte Stream-able)
* RawSHAKE256 (Extendable, Byte Stream-able)
* SHAKE128 (Extendable, Byte Stream-able)
* SHAKE256 (Extendable, Byte Stream-able)

## Tested Platforms
* JVM (Kotlin, Java)
* JS (Node, Bun)

## Supported Platforms

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

**These targets are currently available, but I have no ability to compile them **yet**. Once I have more free time, I will setup a publishing server to compile to all targets.

## Installation
Maven/Gradle
```kotlin
dependencies {
    implementation("asia.hombre:keccak:1.1.0")
}
```
NPM
```text
npm install keccakkotlin@1.1.0
```

## Usage
```kotlin
import asia.hombre.keccak.KeccakHash

val sha3_224 = KeccakHash.generate(KeccakParameter.SHA3_224, "".encodeToByteArray())
println(sha3_224.toHexString(HexFormat.UpperCase))
val sha3_256 = KeccakHash.generate(KeccakParameter.SHA3_256, "".encodeToByteArray())
println(sha3_256.toHexString(HexFormat.UpperCase))
val sha3_384 = KeccakHash.generate(KeccakParameter.SHA3_384, "".encodeToByteArray())
println(sha3_384.toHexString(HexFormat.UpperCase))
val sha3_512 = KeccakHash.generate(KeccakParameter.SHA3_512, "".encodeToByteArray())
println(sha3_512.toHexString(HexFormat.UpperCase))

//Extendable-Output Functions
//A third parameter called 'lengthInBytes' is used to modify the output length.
val rawshake_128 = KeccakHash.generate(KeccakParameter.RAWSHAKE_128, "".encodeToByteArray())
println(rawshake_128.toHexString(HexFormat.UpperCase))
val rawshake_256 = KeccakHash.generate(KeccakParameter.RAWSHAKE_256, "".encodeToByteArray())
println(rawshake_256.toHexString(HexFormat.UpperCase))
val shake_128 = KeccakHash.generate(KeccakParameter.SHAKE_128, "".encodeToByteArray())
println(shake_128.toHexString(HexFormat.UpperCase))
val shake_256 = KeccakHash.generate(KeccakParameter.SHAKE_256, "".encodeToByteArray())
println(shake_256.toHexString(HexFormat.UpperCase))

//Byte Streaming
val input = "".encodeToByteArray()
val rawshake_128_stream = KeccakByteStream(KeccakParameter.RAWSHAKE_128)
rawshake_128_stream.absorb(input) //Can be used many times, and the bytes will be concatenated.
rawshake_128_stream.next() //Automatically generates more bytes
val rawshake_256_stream = KeccakByteStream(KeccakParameter.RAWSHAKE_256)
rawshake_256_stream.absorb(input) //Can be used many times, and the bytes will be concatenated.
rawshake_256_stream.next() //Automatically generates more bytes
val shake_128_stream = KeccakByteStream(KeccakParameter.SHAKE_128)
shake_128_stream.absorb(input) //Can be used many times, and the bytes will be concatenated.
shake_128_stream.next() //Automatically generates more bytes
val shake_256_stream = KeccakByteStream(KeccakParameter.SHAKE_256)
shake_256_stream.absorb(input) //Can be used many times, and the bytes will be concatenated.
shake_256_stream.next() //Automatically generates more bytes
```

## Documentation
* [keccak.hombre.asia](https://keccak.hombre.asia)

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
