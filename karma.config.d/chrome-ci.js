// GitHub Actions runners may not support Chrome's sandbox.
if (process.env.GITHUB_ACTIONS === 'true') {
    config.customLaunchers.ChromeHeadlessNoSandbox = {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-setuid-sandbox'],
    };
    config.browsers = config.browsers.map(browser =>
        browser === 'ChromeHeadless' ? 'ChromeHeadlessNoSandbox' : browser
    );
}
