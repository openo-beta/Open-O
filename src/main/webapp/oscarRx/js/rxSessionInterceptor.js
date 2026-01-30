/**
 * AJAX Interceptor for Rx Session Management
 * Automatically adds demographicNo to all AJAX requests for per-patient session isolation.
 *
 * This interceptor solves the problem of multiple patient Rx tabs overwriting each other's
 * session data by ensuring every AJAX request includes the demographicNo parameter,
 * allowing the server to use per-patient session keys.
 *
 * Requires: currentDemographicNo variable to be defined before this script loads.
 * Supports: Prototype.js (Ajax.Request, Ajax.Updater) and jQuery AJAX.
 */
(function() {
    'use strict';

    function hasDemographicNo(params) {
        if (!params) return false;
        if (typeof params === 'string') return params.indexOf('demographicNo=') !== -1;
        if (typeof params === 'object') return 'demographicNo' in params;
        return false;
    }

    function addDemographicNo(params) {
        if (typeof currentDemographicNo === 'undefined' || !currentDemographicNo) {
            return params;
        }
        if (!params) {
            return 'demographicNo=' + currentDemographicNo;
        }
        if (typeof params === 'string') {
            return params + '&demographicNo=' + currentDemographicNo;
        }
        if (typeof params === 'object') {
            params.demographicNo = currentDemographicNo;
            return params;
        }
        return params;
    }

    // Intercept Prototype.js Ajax.Request
    if (typeof Ajax !== 'undefined' && Ajax.Request) {
        var OriginalRequest = Ajax.Request;
        Ajax.Request = function(url, options) {
            options = options || {};
            // Handle 'parameters' option
            if (!hasDemographicNo(options.parameters)) {
                options.parameters = addDemographicNo(options.parameters);
            }
            // Handle 'postBody' option (used by some AJAX calls with serialized form data)
            if (options.postBody && !hasDemographicNo(options.postBody)) {
                options.postBody = addDemographicNo(options.postBody);
            }
            return new OriginalRequest(url, options);
        };
        // Copy ALL static properties (Events, etc.) and prototype
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
        // Copy ALL static properties and prototype
        for (var prop in OriginalUpdater) {
            if (OriginalUpdater.hasOwnProperty(prop)) {
                Ajax.Updater[prop] = OriginalUpdater[prop];
            }
        }
        Ajax.Updater.prototype = OriginalUpdater.prototype;
    }

    // Intercept jQuery AJAX (if available, may use noConflict mode)
    if (typeof jQuery !== 'undefined') {
        jQuery.ajaxPrefilter(function(options, originalOptions, jqXHR) {
            if (typeof currentDemographicNo === 'undefined' || !currentDemographicNo) {
                return;
            }
            if (!hasDemographicNo(options.data)) {
                options.data = addDemographicNo(options.data);
            }
        });
    }
})();
