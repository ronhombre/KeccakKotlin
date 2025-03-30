package asia.hombre.examples.keccak;

import asia.hombre.keccak.api.*;
import asia.hombre.keccak.streams.HashInputStream;
import asia.hombre.keccak.streams.HashOutputStream;

import javax.xml.bind.DatatypeConverter;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World! This is an example usage for KeccakKotlin using Java.");
        System.out.println("For all intents and purposes, the outputs here should match the values published in https://en.wikipedia.org/wiki/SHA-3, or more accurately, https://csrc.nist.gov/projects/cryptographic-standards-and-guidelines/example-values#aHashing \n");

        SHA3_224 sha3_224 = new SHA3_224();
        byte[] sha3_224_hash = sha3_224.digest(new byte[0]);
        System.out.println("SHA3-224(\"\") = " + DatatypeConverter.printHexBinary(sha3_224_hash).toLowerCase());

        SHA3_256 sha3_256 = new SHA3_256();
        byte[] sha3_256_hash = sha3_256.digest(new byte[0]);
        System.out.println("SHA3-256(\"\") = " + DatatypeConverter.printHexBinary(sha3_256_hash).toLowerCase());

        SHA3_384 sha3_384 = new SHA3_384();
        byte[] sha3_384_hash = sha3_384.digest(new byte[0]);
        System.out.println("SHA3-384(\"\") = " + DatatypeConverter.printHexBinary(sha3_384_hash).toLowerCase());

        SHA3_512 sha3_512 = new SHA3_512();
        byte[] sha3_512_hash = sha3_512.digest(new byte[0]);
        System.out.println("SHA3-512(\"\") = " + DatatypeConverter.printHexBinary(sha3_512_hash).toLowerCase());

        SHAKE128 shake128 = new SHAKE128(32); //32 bytes = 256 bits
        byte[] shake128_hash = shake128.digest(new byte[0]);
        System.out.println("SHAKE128(\"\") = " + DatatypeConverter.printHexBinary(shake128_hash).toLowerCase());

        SHAKE256 shake256 = new SHAKE256(64); //64 bytes = 512 bits
        byte[] shake256_hash = shake256.digest(new byte[0]);
        System.out.println("SHAKE256(\"\") = " + DatatypeConverter.printHexBinary(shake256_hash).toLowerCase());

        System.out.println("\nIn essence, these functions are derived using a source function called Keccak() which permutes the input data.\n");

        System.out.println("Here are some more functions outlined in NIST FIPS 202 but no official reference intermediates are available.\n");

        RawSHAKE128 rawSHAKE128 = new RawSHAKE128(32); //32 bytes = 256 bits
        byte[] rawSHAKE128_hash = rawSHAKE128.digest(new byte[0]);
        System.out.println("RawSHAKE128(\"\") = " + DatatypeConverter.printHexBinary(rawSHAKE128_hash).toLowerCase());

        RawSHAKE256 rawSHAKE256 = new RawSHAKE256(64); //64 bytes = 512 bits
        byte[] rawSHAKE256_hash = rawSHAKE256.digest(new byte[0]);
        System.out.println("RawSHAKE256(\"\") = " + DatatypeConverter.printHexBinary(rawSHAKE256_hash).toLowerCase());

        System.out.println("\nThese below are published as SHA-3 Derived Hash Functions. They are basically extensions of SHA-3 using the Keccak() function.");
        System.out.println("For more details on cSHAKE visit: https://csrc.nist.gov/CSRC/media/Projects/Cryptographic-Standards-and-Guidelines/documents/examples/cSHAKE_samples.pdf\n");

        cSHAKE128 cSHAKE128 = new cSHAKE128(32, new byte[0], "Email Signature".getBytes()); //32 bytes = 256 bits
        byte[] cSHAKE128_hash = cSHAKE128.digest(DatatypeConverter.parseHexBinary("00010203"));
        System.out.println("cSHAKE128(00010203, customization = \"Email Signature\") = " + DatatypeConverter.printHexBinary(cSHAKE128_hash).toLowerCase()); //Output is cSHAKE Sample #1

        cSHAKE256 cSHAKE256 = new cSHAKE256(64, new byte[0], "Email Signature".getBytes()); //64 bytes = 512 bits
        byte[] cSHAKE256_hash = cSHAKE256.digest(DatatypeConverter.parseHexBinary("00010203"));
        System.out.println("cSHAKE256(00010203, customization = \"Email Signature\") = " + DatatypeConverter.printHexBinary(cSHAKE256_hash).toLowerCase()); //Output is cSHAKE Sample #3

        System.out.println("\nFor more details on KMAC visit: https://csrc.nist.gov/CSRC/media/Projects/Cryptographic-Standards-and-Guidelines/documents/examples/KMAC_samples.pdf\n");

        KMAC128 kmac128 = new KMAC128(DatatypeConverter.parseHexBinary("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F"), 32, new byte[0]); //32 bytes = 256 bits
        byte[] kmac128_hash = kmac128.digest(DatatypeConverter.parseHexBinary("00010203"));
        System.out.println("KMAC128(00010203, key = 404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F) = " + DatatypeConverter.printHexBinary(kmac128_hash).toLowerCase()); //Output is KMAC Sample #1

        KMAC256 kmac256 = new KMAC256(DatatypeConverter.parseHexBinary("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F"), 64, "My Tagged Application".getBytes()); //64 bytes = 512 bits
        byte[] kmac256_hash = kmac256.digest(DatatypeConverter.parseHexBinary("00010203"));
        System.out.println("KMAC256(00010203, key = 404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F, customization = \"My Tagged Application\") = " + DatatypeConverter.printHexBinary(kmac256_hash).toLowerCase()); //Output is KMAC Sample #4

        System.out.println("\nFor more details on KMACXOF visit: https://csrc.nist.gov/CSRC/media/Projects/Cryptographic-Standards-and-Guidelines/documents/examples/KMACXOF_samples.pdf\n");

        KMACXOF128 kmacxof128 = new KMACXOF128(DatatypeConverter.parseHexBinary("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F"), 32, new byte[0]); //32 bytes = 256 bits
        byte[] kmacxof128_hash = kmacxof128.digest(DatatypeConverter.parseHexBinary("00010203"));
        System.out.println("KMACXOF128(00010203, key = 404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F) = " + DatatypeConverter.printHexBinary(kmacxof128_hash).toLowerCase()); //Output is KMACXOF Sample #1

        KMACXOF256 kmacxof256 = new KMACXOF256(DatatypeConverter.parseHexBinary("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F"), 64, "My Tagged Application".getBytes()); //64 bytes = 512 bits
        byte[] kmacxof256_hash = kmacxof256.digest(DatatypeConverter.parseHexBinary("00010203"));
        System.out.println("KMACXOF256(00010203, key = 404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F, customization = \"My Tagged Application\") = " + DatatypeConverter.printHexBinary(kmacxof256_hash).toLowerCase()); //Output is KMACXOF Sample #4

        System.out.println("\nIt is also possible to hash an input stream of bytes by continuously feeding bytes into a HashInputStream wherein no large buffers are used to store the input bytes before hashing.\n");

        HashInputStream sha3_224_hashInputStream = SHA3_224.Companion.newInputStream();
        sha3_224_hashInputStream.write(new byte[0]);
        HashOutputStream sha3_224_hashOutputStream = sha3_224_hashInputStream.close();
        System.out.print("SHA3-224(\"\") = ");
        while(sha3_224_hashOutputStream.hasNext()) {
            System.out.print(DatatypeConverter.printHexBinary(new byte[] {sha3_224_hashOutputStream.nextByte()}).toLowerCase());
        }
        System.out.println();

        HashInputStream sha3_256_hashInputStream = SHA3_256.Companion.newInputStream();
        sha3_256_hashInputStream.write(new byte[0]);
        HashOutputStream sha3_256_hashOutputStream = sha3_256_hashInputStream.close();
        System.out.println("SHA3-256(\"\") = " + DatatypeConverter.printHexBinary(sha3_256_hashOutputStream.nextBytes(32)).toLowerCase());

        HashOutputStream shake128_hashOutputStream = shake128.stream();
        System.out.print("SHAKE128(\"\", 1024 bytes) = ");
        for(int i = 0; i < 1024; i++) {
            System.out.print(DatatypeConverter.printHexBinary(new byte[] {shake128_hashOutputStream.nextByte()}).toLowerCase());
        }
        System.out.println();
    }
}