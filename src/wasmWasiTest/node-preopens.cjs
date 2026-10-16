const { syncBuiltinESMExports } = require('node:module');
const { resolve } = require('node:path');
const wasi = require('node:wasi');

// Apply only to this test process; leave Kotlin's generated launcher intact.
const WASI = wasi.WASI;
wasi.WASI = class extends WASI {
    constructor(options) {
        super({
            ...options,
            preopens: {
                ...options.preopens,
                '/src/commonTest/resources': resolve(__dirname, '../commonTest/resources'),
            },
        });
    }
};
syncBuiltinESMExports();
