/**
 * Shared batch renderer for specialist list pages (EditSpecialists, DisplayService).
 *
 * Fetches specialist JSON from SpecialistList2Action via POST and renders rows
 * in batches using requestAnimationFrame for non-blocking DOM updates.
 * Includes OWASP CsrfGuard token in the request header for CSRF protection.
 *
 * @param {Object} options
 * @param {string} options.url - The fetch URL for the specialist data endpoint
 * @param {string} options.tbodyId - The ID of the tbody element to render into
 * @param {Function} options.buildRow - Function that receives a data row array and returns a tr element
 * @param {string} options.csrfTokenName - The CSRF token header name from CsrfGuard
 * @param {string} options.csrfTokenValue - The CSRF token value from CsrfGuard
 * @since 2026-03-10
 */
function renderSpecialistList(options) {
    var BATCH_SIZE = 1000;
    var tbody = document.getElementById(options.tbodyId);
    var firstBatchRendered = false;

    function renderBatch(data, idx) {
        var end = Math.min(idx + BATCH_SIZE, data.length);
        var fragment = document.createDocumentFragment();
        for (; idx < end; idx++) {
            fragment.appendChild(options.buildRow(data[idx]));
        }
        tbody.appendChild(fragment);
        if (!firstBatchRendered) {
            firstBatchRendered = true;
            HideSpin();
        }
        if (idx < data.length) {
            requestAnimationFrame(function() { renderBatch(data, idx); });
        }
    }

    var headers = {};
    if (options.csrfTokenName) {
        headers[options.csrfTokenName] = options.csrfTokenValue;
    }

    fetch(options.url, {
        method: "POST",
        headers: headers,
        credentials: "same-origin"
    })
    .then(function(resp) {
        if (!resp.ok) {
            throw new Error("HTTP " + resp.status);
        }
        return resp.json();
    })
    .then(function(data) {
        if (data.length === 0) {
            HideSpin();
        } else {
            renderBatch(data, 0);
        }
    })
    .catch(function(err) {
        HideSpin();
        console.error("Error loading specialists:", err);
    });
}
