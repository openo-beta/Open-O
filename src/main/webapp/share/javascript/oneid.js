/*
 * Launch and window control for the provincial EHR services (ONE ID Viewlets).
 * launchViewlet fetches the launch URL for a patient and opens it in the Viewlet's
 * configured interface: a shared named window, or an in-page modal dialog. It then
 * waits for the Viewlet's completion message within the configured wait time, reads
 * the message for the real success or failure outcome, allows a bounded number of
 * reloads, records the outcome, and informs the user if the Viewlet does not
 * respond. Closing either the window or the dialog without a completion message is
 * recorded as a failure and clears the patient from the EHR context. A launch refused only for want of
 * a ONE ID sign-in starts the mid-session ONE ID step-up and resumes the launch when
 * the clinician returns to the page.
 */

var VIEWLET_DEFAULT_TIMEOUT_MS = 65000;
var VIEWLET_MAX_RELOAD_ATTEMPTS = 3;

function launchViewlet(ctx, demographicNo, key, displayMode) {
    var url = ctx + '/viewletLaunch.do?demographicNo=' + encodeURIComponent(demographicNo)
        + '&key=' + encodeURIComponent(key);
    fetch(url, {credentials: 'same-origin'})
        .then(function (response) {
            var contentType = response.headers.get('content-type') || '';
            if (contentType.indexOf('application/json') === -1) {
                // A non-JSON reply means the request never reached the launch endpoint,
                // usually an expired ONE ID session or a missing EHR service permission.
                alert('The EHR service could not be launched. Your ONE ID session or EHR service access may have expired. Sign in again and retry.');
                return null;
            }
            return response.json().then(function (data) {
                return {status: response.status, data: data};
            });
        })
        .then(function (result) {
            if (!result) {
                return;
            }
            if (result.status === 268) {
                var summary = result.data && result.data.summary ? result.data.summary : 'The EHR service could not be launched.';
                if (result.data && result.data.stepUp) {
                    startOneIdStepUp(ctx, demographicNo, key, displayMode, summary);
                } else {
                    alert(summary);
                }
                return;
            }
            if (!result.data || !result.data.viewletUrl) {
                alert('The EHR service returned no address to open.');
                return;
            }
            var timeout = viewletTimeout(result.data.timeoutMillis);
            var uuid = result.data.uuid;
            if (displayMode === 'modal') {
                openViewletModal(result.data.viewletUrl, demographicNo, ctx, timeout, key, uuid);
            } else {
                popupEHRService(result.data.viewletUrl, demographicNo, ctx, timeout, key, uuid);
            }
        })
        .catch(function () {
            alert('The EHR service could not be launched. Please try again.');
        });
}

function viewletTimeout(value) {
    var ms = parseInt(value, 10);
    return (isNaN(ms) || ms <= 0) ? VIEWLET_DEFAULT_TIMEOUT_MS : ms;
}

function viewletOrigin(url) {
    try {
        return new URL(url, window.location.href).origin;
    } catch (e) {
        return null;
    }
}

function viewletMessagePayload(data) {
    try {
        if (data === null || data === undefined) {
            return '';
        }
        var text = (typeof data === 'string') ? data : JSON.stringify(data);
        return text.slice(0, 500);
    } catch (e) {
        return '';
    }
}

// Reads a Viewlet completion message. The Viewlet Framework response is a JSON object
// with optional errors[] and successes[] (each entry carrying code[]/microService/reason)
// and utility.code[] instructions for the launching page, such as a user cancellation.
// Returns the outcome and a loggable summary, or null when the message is not a
// completion message.
function parseViewletCompletion(data) {
    var body = data;
    if (typeof body === 'string') {
        try {
            body = JSON.parse(body);
        } catch (e) {
            return null;
        }
    }
    if (!body || typeof body !== 'object') {
        return null;
    }
    var errors = Array.isArray(body.errors) ? body.errors : [];
    var successes = Array.isArray(body.successes) ? body.successes : [];
    var utilityCodes = (body.utility && Array.isArray(body.utility.code)) ? body.utility.code : [];
    if (errors.length === 0 && successes.length === 0 && utilityCodes.length === 0) {
        return null;
    }
    var codes = [];
    function collectCodes(entries) {
        for (var i = 0; i < entries.length; i++) {
            var entry = entries[i] || {};
            var entryCodes = Array.isArray(entry.code) ? entry.code
                : (entry.code === null || entry.code === undefined ? [] : [entry.code]);
            for (var j = 0; j < entryCodes.length; j++) {
                codes.push(String(entryCodes[j]));
            }
        }
    }
    collectCodes(errors);
    collectCodes(successes);
    var cancelled = false;
    for (var k = 0; k < utilityCodes.length; k++) {
        var utilityCode = String(utilityCodes[k]);
        codes.push(utilityCode);
        if (utilityCode.toUpperCase().indexOf('CANCELLED') !== -1) {
            cancelled = true;
        }
    }
    var status = (errors.length > 0 || cancelled) ? 'failure' : 'success';
    var summary = codes.length ? ('codes: ' + codes.slice(0, 10).join(', ') + ' | ') : '';
    return {status: status, message: summary + viewletMessagePayload(data)};
}

// Records the outcome of a launch (success or failure) in the gateway log. Best-effort:
// a logging failure never disrupts the clinician.
function reportViewletResult(ctx, demographicNo, key, uuid, status, message) {
    if (!demographicNo || !key) {
        return;
    }
    var url = ctx + '/viewletResult.do?demographicNo=' + encodeURIComponent(demographicNo)
        + '&key=' + encodeURIComponent(key)
        + '&uuid=' + encodeURIComponent(uuid || '')
        + '&status=' + encodeURIComponent(status)
        + '&message=' + encodeURIComponent(message || '');
    fetch(url, {credentials: 'same-origin'}).catch(function () {
    });
}

function popupEHRService(url, demographicNo, ctx, timeout, key, uuid) {
    var windowProperties = 'height=800,width=1300,location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0';
    var popup = window.open(url, 'EHR Service', windowProperties);
    if (!popup) {
        alert('The EHR service window was blocked. Allow pop-ups for this site and try again.');
        return;
    }
    var expectedOrigin = viewletOrigin(url);
    var responded = false;
    var reported = false;

    // Record the outcome once; the first of a response or the window closing wins.
    function reportOnce(status, message) {
        if (reported) {
            return;
        }
        reported = true;
        reportViewletResult(ctx, demographicNo, key, uuid, status, message);
    }

    // A message from the Viewlet's own origin marks it as alive; only a completion
    // message decides the outcome. A window closed without one is recorded as a
    // failure by the close poller.
    function onMessage(event) {
        if (expectedOrigin && event.origin === expectedOrigin) {
            responded = true;
            var completion = parseViewletCompletion(event.data);
            if (completion) {
                reportOnce(completion.status, completion.message);
            }
        }
    }
    window.addEventListener('message', onMessage);

    // If the Viewlet has not responded within the wait time and its window is still
    // open, tell the user it may not have loaded.
    var waitTimer = setTimeout(function () {
        if (!responded && !popup.closed) {
            alert('The EHR service has not responded. If its window is blank, close it and try again.');
        }
    }, timeout);

    if (demographicNo) {
        var poll = setInterval(function () {
            if (popup.closed) {
                clearInterval(poll);
                clearTimeout(waitTimer);
                window.removeEventListener('message', onMessage);
                reportOnce('failure', 'The EHR service window was closed without a response.');
                closeViewletPatientContext(ctx, demographicNo);
            }
        }, 1000);
    }
    popup.focus();
}

function openViewletModal(url, demographicNo, ctx, timeout, key, uuid) {
    // Only one modal viewlet is shown at a time; replace any that is already open.
    var existingOverlay = document.getElementById('viewletModalOverlay');
    if (existingOverlay) {
        document.body.removeChild(existingOverlay);
    }
    var expectedOrigin = viewletOrigin(url);
    var reloadAttempts = 0;
    var loaded = false;
    var closed = false;
    var reported = false;
    var waitTimer = null;

    // Record the outcome once; rendering or a response is a success, closing before
    // either is a failure.
    function reportOnce(status, message) {
        if (reported) {
            return;
        }
        reported = true;
        reportViewletResult(ctx, demographicNo, key, uuid, status, message);
    }

    var overlay = document.createElement('div');
    overlay.id = 'viewletModalOverlay';
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:10000;display:flex;align-items:center;justify-content:center;';

    var dialog = document.createElement('div');
    dialog.style.cssText = 'background:#fff;border-radius:4px;padding:8px;box-shadow:0 4px 24px rgba(0,0,0,0.4);display:flex;flex-direction:column;';

    var bar = document.createElement('div');
    bar.style.cssText = 'display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;';

    var status = document.createElement('span');
    status.textContent = 'Loading the EHR service…';
    status.style.cssText = 'font-size:13px;color:#555;';

    var actions = document.createElement('div');

    var reloadButton = document.createElement('button');
    reloadButton.type = 'button';
    reloadButton.textContent = 'Reload';
    reloadButton.style.cssText = 'margin-right:6px;';

    var closeButton = document.createElement('button');
    closeButton.type = 'button';
    closeButton.textContent = 'Close';

    var frame = document.createElement('iframe');
    frame.id = 'viewletModalFrame';
    frame.src = url;
    frame.width = '540';
    frame.height = '600';
    frame.style.border = 'none';
    frame.setAttribute('sandbox', 'allow-forms allow-scripts allow-same-origin allow-modals');

    function closeModal() {
        if (closed) {
            return;
        }
        closed = true;
        clearTimeout(waitTimer);
        window.removeEventListener('message', onMessage);
        reportOnce('failure', 'The EHR service was closed without a response.');
        if (overlay.parentNode) {
            document.body.removeChild(overlay);
        }
        if (demographicNo) {
            closeViewletPatientContext(ctx, demographicNo);
        }
    }

    // A completion message from the Viewlet's own origin decides the outcome and closes
    // the dialog; other messages are ignored. A dialog closed without a completion
    // message is recorded as a failure.
    function onMessage(event) {
        if (expectedOrigin && event.origin === expectedOrigin) {
            var completion = parseViewletCompletion(event.data);
            if (completion) {
                reportOnce(completion.status, completion.message);
                closeModal();
            }
        }
    }
    window.addEventListener('message', onMessage);

    // The frame loading only clears the waiting notice; the outcome comes from the
    // Viewlet's completion message, not from the frame rendering.
    frame.addEventListener('load', function () {
        loaded = true;
        status.textContent = '';
        reloadButton.style.display = 'none';
    });

    reloadButton.onclick = function () {
        if (reloadAttempts >= VIEWLET_MAX_RELOAD_ATTEMPTS) {
            status.textContent = 'The EHR service did not load. Please close and try again later.';
            reloadButton.style.display = 'none';
            return;
        }
        reloadAttempts++;
        loaded = false;
        status.textContent = 'Loading the EHR service…';
        frame.src = url;
    };

    closeButton.onclick = closeModal;

    // If the Viewlet neither renders nor responds within the wait time, tell the user
    // so they can reload or cancel.
    waitTimer = setTimeout(function () {
        if (!loaded && !closed) {
            status.textContent = 'The EHR service is not responding.';
        }
    }, timeout);

    actions.appendChild(reloadButton);
    actions.appendChild(closeButton);
    bar.appendChild(status);
    bar.appendChild(actions);
    dialog.appendChild(bar);
    dialog.appendChild(frame);
    overlay.appendChild(dialog);
    document.body.appendChild(overlay);
}

function closeViewletPatientContext(ctx, demographicNo) {
    fetch(ctx + '/viewletPatientClose.do?demographicNo=' + encodeURIComponent(demographicNo), {credentials: 'same-origin'})
        .then(function (response) {
            if (response.status !== 200) {
                alert('There was an error removing the patient from the context. Please try again.');
            }
        })
        .catch(function () {
            alert('There was an error removing the patient from the context. Please try again.');
        });
}

// Sends a signed-in clinician to the ONE ID sign-in without ending their session, remembering
// the launch so it resumes when they come back to this page.
function startOneIdStepUp(ctx, demographicNo, key, displayMode, summary) {
    if (!confirm(summary + ' You will be taken to the Ontario Health sign-in and brought back to this page.')) {
        return;
    }
    try {
        sessionStorage.setItem('oneIdPendingViewlet', JSON.stringify({
            ctx: ctx, demographicNo: demographicNo, key: key, displayMode: displayMode, at: Date.now()
        }));
    } catch (e) {
    }
    window.location.href = ctx + '/oneIdLogin.do?returnUrl='
        + encodeURIComponent(window.location.pathname + window.location.search);
}

var ONEID_STEP_UP_MESSAGES = {
    notLinked: 'Your ONE ID is not linked to this account. Log out and sign in with ONE ID once to link it, or contact your administrator.',
    differentAccount: 'This ONE ID is linked to a different account. Use the ONE ID linked to your user.',
    verifyFailed: 'Your ONE ID sign-in could not be verified. Please try again.'
};

// On page load: explain a step-up failure sent back on the URL, or resume the launch that the
// step-up sign-in interrupted.
(function () {
    function run() {
        var pending = null;
        try {
            var raw = sessionStorage.getItem('oneIdPendingViewlet');
            if (raw) {
                sessionStorage.removeItem('oneIdPendingViewlet');
                pending = JSON.parse(raw);
            }
        } catch (e) {
        }
        var params = new URLSearchParams(window.location.search);
        var errorCode = params.get('oneIdStepUpError');
        if (errorCode) {
            params.delete('oneIdStepUpError');
            var query = params.toString();
            history.replaceState(null, '', window.location.pathname + (query ? '?' + query : ''));
            alert(ONEID_STEP_UP_MESSAGES[errorCode] || 'The ONE ID sign-in could not be completed. Please try again.');
            return;
        }
        if (pending && pending.key && pending.at && (Date.now() - pending.at) < 600000) {
            launchViewlet(pending.ctx, pending.demographicNo, pending.key, pending.displayMode);
        }
    }
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', run);
    } else {
        run();
    }
})();
