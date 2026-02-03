/**
 * Enhanced AJAX/Request Interceptor for Rx Session Management
 *
 * Automatically adds demographicNo to all requests for per-patient session isolation.
 * This interceptor works together with RxSessionFilter (server-side) to solve the problem
 * of multiple patient Rx tabs overwriting each other's session data.
 *
 * Handles:
 * - Prototype.js AJAX (Ajax.Request, Ajax.Updater)
 * - jQuery AJAX
 * - Form submissions (adds hidden field)
 * - Iframes with data-rx-src attribute (converts to src with demographicNo)
 *
 * Uses sessionStorage to persist demographicNo across page navigations within the same tab,
 * which helps iframes and sub-pages that don't have direct access to the parent's JS variables.
 *
 * Requires: currentDemographicNo variable to be defined before this script loads,
 *           OR a previously stored value in sessionStorage.
 *
 * @since 2026-01-30
 */
(function() {
    'use strict';

    // Try to get demographicNo from variable or sessionStorage
    var demoNo = null;

    if (typeof currentDemographicNo !== 'undefined' && currentDemographicNo) {
        demoNo = String(currentDemographicNo);
        // Persist to sessionStorage for use across page navigations within this tab
        try {
            sessionStorage.setItem('rxDemographicNo', demoNo);
        } catch (e) { /* sessionStorage not available */ }
    } else {
        // Try to recover from sessionStorage (useful for iframes, redirects)
        try {
            demoNo = sessionStorage.getItem('rxDemographicNo');
        } catch (e) { /* sessionStorage not available */ }
    }

    // Exit if we don't have a demographicNo
    if (!demoNo) {
        return;
    }

    // ============== Helper Functions ==============

    function hasDemographicNo(params) {
        if (!params) return false;
        if (typeof params === 'string') return params.indexOf('demographicNo=') !== -1;
        if (typeof params === 'object') return 'demographicNo' in params;
        return false;
    }

    function addDemographicNoToString(str) {
        if (!str) return 'demographicNo=' + demoNo;
        if (str.indexOf('demographicNo=') !== -1) return str;
        return str + '&demographicNo=' + demoNo;
    }

    function addDemographicNoToUrl(url) {
        if (!url || url.indexOf('demographicNo=') !== -1) return url;
        var separator = url.indexOf('?') !== -1 ? '&' : '?';
        return url + separator + 'demographicNo=' + demoNo;
    }

    function addDemographicNo(params) {
        if (!params) return 'demographicNo=' + demoNo;
        if (typeof params === 'string') return addDemographicNoToString(params);
        if (typeof params === 'object') {
            params.demographicNo = demoNo;
            return params;
        }
        return params;
    }

    // ============== AJAX Interception ==============

    // Intercept Prototype.js Ajax.Request
    if (typeof Ajax !== 'undefined' && Ajax.Request) {
        var OriginalRequest = Ajax.Request;
        Ajax.Request = function(url, options) {
            options = options || {};
            if (!hasDemographicNo(options.parameters)) {
                options.parameters = addDemographicNo(options.parameters);
            }
            if (options.postBody && !hasDemographicNo(options.postBody)) {
                options.postBody = addDemographicNo(options.postBody);
            }
            return new OriginalRequest(url, options);
        };
        // Preserve static properties and prototype chain
        for (var prop in OriginalRequest) {
            if (OriginalRequest.hasOwnProperty(prop)) {
                Ajax.Request[prop] = OriginalRequest[prop];
            }
        }
        Ajax.Request.prototype = OriginalRequest.prototype;
    }

    // Intercept Prototype.js Ajax.Updater
    if (typeof Ajax !== 'undefined' && Ajax.Updater) {
        var OriginalUpdater = Ajax.Updater;
        Ajax.Updater = function(container, url, options) {
            options = options || {};
            if (!hasDemographicNo(options.parameters)) {
                options.parameters = addDemographicNo(options.parameters);
            }
            return new OriginalUpdater(container, url, options);
        };
        for (var prop in OriginalUpdater) {
            if (OriginalUpdater.hasOwnProperty(prop)) {
                Ajax.Updater[prop] = OriginalUpdater[prop];
            }
        }
        Ajax.Updater.prototype = OriginalUpdater.prototype;
    }

    // Intercept jQuery AJAX (if available)
    if (typeof jQuery !== 'undefined') {
        jQuery.ajaxPrefilter(function(options) {
            if (!hasDemographicNo(options.data)) {
                options.data = addDemographicNo(options.data);
            }
        });
    }

    // ============== Form Interception ==============

    function addHiddenFieldToForm(form) {
        // Skip if form already has demographicNo field
        if (form.querySelector('input[name="demographicNo"]')) return;

        // Only add to Rx-related forms (check action URL)
        var action = form.getAttribute('action') || '';
        var isRxForm = action.indexOf('/oscarRx/') !== -1 ||
                       action.indexOf('Rx') !== -1 ||
                       action === ''; // Empty action = same page (likely Rx if we're on Rx page)

        if (!isRxForm) return;

        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'demographicNo';
        input.value = demoNo;
        form.appendChild(input);
    }

    function processAllForms() {
        var forms = document.querySelectorAll('form');
        for (var i = 0; i < forms.length; i++) {
            addHiddenFieldToForm(forms[i]);
        }
    }

    // ============== Iframe Interception ==============

    function processIframe(iframe) {
        var rxSrc = iframe.getAttribute('data-rx-src');
        if (rxSrc) {
            iframe.src = addDemographicNoToUrl(rxSrc);
            iframe.removeAttribute('data-rx-src'); // Prevent reprocessing
        }
    }

    function processAllIframes() {
        var iframes = document.querySelectorAll('iframe[data-rx-src]');
        for (var i = 0; i < iframes.length; i++) {
            processIframe(iframes[i]);
        }
    }

    // ============== MutationObserver for Dynamic Content ==============

    function setupMutationObserver() {
        if (typeof MutationObserver === 'undefined') return;

        var observer = new MutationObserver(function(mutations) {
            for (var i = 0; i < mutations.length; i++) {
                var mutation = mutations[i];
                for (var j = 0; j < mutation.addedNodes.length; j++) {
                    var node = mutation.addedNodes[j];
                    if (node.nodeType !== 1) continue; // Not an element

                    // Check if added node is a form or iframe
                    if (node.nodeName === 'FORM') {
                        addHiddenFieldToForm(node);
                    } else if (node.nodeName === 'IFRAME' && node.hasAttribute('data-rx-src')) {
                        processIframe(node);
                    }

                    // Check descendants
                    if (node.querySelectorAll) {
                        var forms = node.querySelectorAll('form');
                        for (var k = 0; k < forms.length; k++) {
                            addHiddenFieldToForm(forms[k]);
                        }
                        var iframes = node.querySelectorAll('iframe[data-rx-src]');
                        for (var m = 0; m < iframes.length; m++) {
                            processIframe(iframes[m]);
                        }
                    }
                }
            }
        });

        var target = document.body || document.documentElement;
        if (target) {
            observer.observe(target, {
                childList: true,
                subtree: true
            });
        }
    }

    // ============== Initialize ==============

    function init() {
        processAllForms();
        processAllIframes();
        setupMutationObserver();
    }

    // Run on DOMContentLoaded or immediately if already loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
