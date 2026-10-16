<#import "source_set_selector.ftl" as source_set_selector>
<#macro display>
    <nav class="navigation theme-dark" id="navigation-wrapper">
        <@template_cmd name="pathToRoot">
            <div class="navigation-title">
                <button class="menu-toggle" id="menu-toggle" type="button">toggle menu</button>
                <div class="library-name">
                    <a class="library-name--link" href="${pathToRoot}index.html">
                        <@template_cmd name="projectName">
                            ${projectName}
                        </@template_cmd>
                    </a>
                </div>
                <div class="library-version">
                    <@version/>
                    <a class="nightly-btn" id="version-switch-btn" href="${pathToRoot}nightly/">Nightly</a>
                    <script>
                        (function() {
                            if (window.location.pathname.includes('/nightly/')) {
                                var btn = document.getElementById('version-switch-btn');
                                if (btn) {
                                    btn.textContent = 'Latest Release';
                                    btn.href = '/';
                                }
                            }
                        })();
                    </script>
                </div>
            </div>
            <div class="navigation-controls">
                <@source_set_selector.display/>
                <button class="navigation-controls--btn navigation-controls--theme" id="theme-toggle-button"
                        type="button">switch theme
                </button>
                <div class="navigation-controls--btn navigation-controls--search" id="searchBar" role="button">search in
                    API
                </div>
            </div>
        </@template_cmd>
    </nav>
</#macro>