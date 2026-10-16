config.files.push({
    pattern: require('path').resolve(config.basePath, 'src/commonTest/resources/**/*.bin'),
    included: false,
    served: true,
    watched: false,
});
