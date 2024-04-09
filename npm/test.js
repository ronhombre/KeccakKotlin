const { KeccakHash, KeccakByteStream, KeccakParameter } = require("./kotlin/KeccakKotlin").asia.hombre.keccak;

function test224() {
    console.log("Testing SHA3-224...");
    let hash = KeccakHash.Companion.generate(KeccakParameter.SHA3_224, Buffer.from(""))
    let answer = "6b4e03423667dbb73b6e15454f0eb1abd4597f9a1b078e3f5b5a6bc7"

    console.assert(Buffer.from(hash).toString('hex') === answer, "Incorrect SHA3-224 Hash!");
}

function test256() {
    console.log("Testing SHA3-256...");
    let hash = KeccakHash.Companion.generate(KeccakParameter.SHA3_256, Buffer.from(""))
    let answer = "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a"

    console.assert(Buffer.from(hash).toString('hex') === answer, "Incorrect SHA3-256 Hash!");
}

function test384() {
    console.log("Testing SHA3-384...");
    let hash = KeccakHash.Companion.generate(KeccakParameter.SHA3_384, Buffer.from(""))
    let answer = "0c63a75b845e4f7d01107d852e4c2485c51a50aaaa94fc61995e71bbee983a2ac3713831264adb47fb6bd1e058d5f004"

    console.assert(Buffer.from(hash).toString('hex') === answer, "Incorrect SHA3-384 Hash!");
}

function test512() {
    console.log("Testing SHA3-512...");
    let hash = KeccakHash.Companion.generate(KeccakParameter.SHA3_512, Buffer.from(""))
    let answer = "a69f73cca23a9ac5c8b567dc185a756e97c982164fe25859e0d1dcc1475c80a615b2123af1f5f94c11e3e9402c3ac558f500199d95b6d3e301758586281dcd26"

    console.assert(Buffer.from(hash).toString('hex') === answer, "Incorrect SHA3-512 Hash!");
}

function testrawshake128() {
    console.log("Testing RawSHAKE128...");
    let hash = KeccakHash.Companion.generate(KeccakParameter.RAWSHAKE_128, Buffer.from(""))
    let answer = "fa019a3b17630df6014853b5470773f1"

    console.assert(Buffer.from(hash).toString('hex') === answer, "Incorrect RawSHAKE128 Hash!");
}

function testrawshake256() {
    console.log("Testing RawSHAKE256...");
    let hash = KeccakHash.Companion.generate(KeccakParameter.RAWSHAKE_256, Buffer.from(""))
    let answer = "3a1108d4a90a31b85a10bdce77f4bfbdcc5b1d70dd405686f8bbde834aa1a410"

    console.assert(Buffer.from(hash).toString('hex') === answer, "Incorrect RawSHAKE256 Hash!");
}

function testshake128() {
    console.log("Testing SHAKE128...");
    let hash = KeccakHash.Companion.generate(KeccakParameter.SHAKE_128, Buffer.from(""))
    let answer = "7f9c2ba4e88f827d616045507605853e"

    console.assert(Buffer.from(hash).toString('hex') === answer, "Incorrect SHAKE128 Hash!");
}

function testshake256() {
    console.log("Testing SHAKE256...");
    let hash = KeccakHash.Companion.generate(KeccakParameter.SHAKE_256, Buffer.from(""))
    let answer = "46b9dd2b0ba88d13233b3feb743eeb243fcd52ea62b81b82b50c27646ed5762f"

    console.assert(Buffer.from(hash).toString('hex') === answer, "Incorrect SHAKE256 Hash!");
}

test224();
test256();
test384();
test512();
testrawshake128();
testrawshake256();
testshake128();
testshake256();

console.log("Success!")