/*
 * Launch and window control for the provincial EHR services (ONE ID Viewlets).
 * launchViewlet fetches the launch URL for a patient and opens it in the Viewlet's
 * configured interface: a shared named window, or an in-page modal dialog. It then
 * waits for the Viewlet's completion message within the configured wait time, reads
 * the message for the real success or failure outcome, allows a bounded number of
 * reloads, records the outcome, and informs the user if the Viewlet does not
 * respond. Closing either the window or the dialog without a completion message is
 * recorded as a failure and clears the patient from the EHR context. A launch that
 * may put a second EHR service window on screen warns the clinician first; the
 * warning can be turned off and back on per user from the provider preferences. A
 * launch refused only for want of a ONE ID sign-in starts the mid-session ONE ID
 * step-up and resumes the launch when the clinician returns to the page.
 */

var VIEWLET_DEFAULT_TIMEOUT_MS = 65000;
var VIEWLET_MAX_RELOAD_ATTEMPTS = 3;

// A tab-scoped id and a shared localStorage marker let a launch in one tab see EHR
// service windows opened from other tabs; a heartbeat keeps the marker fresh while
// any viewlet opened from this tab is alive.
var ONEID_VIEWLET_WINDOW_KEY = 'oneIdViewletWindow';
var ONEID_VIEWLET_HEARTBEAT_MS = 15000;
var ONEID_VIEWLET_STALE_MS = 45000;
var oneIdTabId = Math.random().toString(36).slice(2) + '-' + Date.now().toString(36);
var openPopupViewlet = null;
var openViewletCount = 0;
var viewletHeartbeatTimer = null;
var currentModalDispose = null;

function writeViewletWindowMarker() {
    try {
        localStorage.setItem(ONEID_VIEWLET_WINDOW_KEY, JSON.stringify({tab: oneIdTabId, at: Date.now()}));
    } catch (e) {
    }
}

function viewletWindowOpened() {
    openViewletCount++;
    writeViewletWindowMarker();
    if (!viewletHeartbeatTimer) {
        viewletHeartbeatTimer = setInterval(writeViewletWindowMarker, ONEID_VIEWLET_HEARTBEAT_MS);
    }
}

function viewletWindowClosed() {
    openViewletCount = Math.max(0, openViewletCount - 1);
    if (openViewletCount === 0) {
        if (viewletHeartbeatTimer) {
            clearInterval(viewletHeartbeatTimer);
            viewletHeartbeatTimer = null;
        }
        try {
            var raw = localStorage.getItem(ONEID_VIEWLET_WINDOW_KEY);
            if (raw && JSON.parse(raw).tab === oneIdTabId) {
                localStorage.removeItem(ONEID_VIEWLET_WINDOW_KEY);
            }
        } catch (e) {
        }
    }
}

// True when this launch could put a second EHR service window on screen: the other
// display mode is open in this tab, or another tab reports an open viewlet. A
// same-mode launch in this tab reuses the named window or replaces the dialog, so
// it is never simultaneous.
function anotherViewletMayBeOpen(displayMode) {
    var popupAlive = openPopupViewlet && !openPopupViewlet.closed;
    var modalOpen = !!document.getElementById('viewletModalOverlay');
    if (displayMode === 'modal' && popupAlive) {
        return true;
    }
    if (displayMode !== 'modal' && modalOpen) {
        return true;
    }
    try {
        var raw = localStorage.getItem(ONEID_VIEWLET_WINDOW_KEY);
        if (raw) {
            var marker = JSON.parse(raw);
            if (marker && marker.tab !== oneIdTabId && marker.at
                && (Date.now() - marker.at) < ONEID_VIEWLET_STALE_MS) {
                return true;
            }
        }
    } catch (e) {
    }
    return false;
}

// The warning shown before a launch that may open a second EHR service window:
// multiple windows can end up showing unintended patient information. The clinician
// can continue, cancel, or turn the warning off; turning it off is recorded and it
// can be turned back on from the provider preferences.
function showMultiWindowNotice(ctx, proceed) {
    var existing = document.getElementById('viewletNoticeOverlay');
    if (existing) {
        document.body.removeChild(existing);
    }
    var overlay = document.createElement('div');
    overlay.id = 'viewletNoticeOverlay';
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:10001;display:flex;align-items:center;justify-content:center;';
    var dialog = document.createElement('div');
    dialog.style.cssText = 'background:#fff;border-radius:4px;padding:16px;max-width:440px;box-shadow:0 4px 24px rgba(0,0,0,0.4);font-size:13px;';
    var text = document.createElement('p');
    text.textContent = 'Another EHR service window may already be open. Working with multiple EHR service windows can result in inadvertently referencing unintended patient information.';
    text.style.cssText = 'margin:0 0 12px 0;';
    var label = document.createElement('label');
    label.style.cssText = 'display:block;margin:0 0 12px 0;';
    var checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.style.cssText = 'margin-right:6px;';
    label.appendChild(checkbox);
    label.appendChild(document.createTextNode('Do not warn me again (can be turned back on in your preferences)'));
    var actions = document.createElement('div');
    actions.style.cssText = 'text-align:right;';
    var cancelButton = document.createElement('button');
    cancelButton.type = 'button';
    cancelButton.textContent = 'Cancel';
    cancelButton.style.cssText = 'margin-right:6px;';
    var continueButton = document.createElement('button');
    continueButton.type = 'button';
    continueButton.textContent = 'Continue';
    function closeNotice() {
        if (overlay.parentNode) {
            document.body.removeChild(overlay);
        }
    }
    cancelButton.onclick = closeNotice;
    continueButton.onclick = function () {
        if (checkbox.checked) {
            postViewletRequest(ctx + '/viewletNoticeToggle.do?enabled=false', null, null);
        }
        closeNotice();
        proceed();
    };
    actions.appendChild(cancelButton);
    actions.appendChild(continueButton);
    dialog.appendChild(text);
    dialog.appendChild(label);
    dialog.appendChild(actions);
    overlay.appendChild(dialog);
    document.body.appendChild(overlay);
}

function launchViewlet(ctx, demographicNo, key, displayMode) {
    if (!anotherViewletMayBeOpen(displayMode)) {
        doLaunchViewlet(ctx, demographicNo, key, displayMode);
        return;
    }
    fetch(ctx + '/viewletNoticeSetting.do', {credentials: 'same-origin'})
        .then(function (response) {
            return response.json();
        })
        .then(function (setting) {
            if (setting && setting.enabled === false) {
                doLaunchViewlet(ctx, demographicNo, key, displayMode);
            } else {
                showMultiWindowNotice(ctx, function () {
                    doLaunchViewlet(ctx, demographicNo, key, displayMode);
                });
            }
        })
        .catch(function () {
            showMultiWindowNotice(ctx, function () {
                doLaunchViewlet(ctx, demographicNo, key, displayMode);
            });
        });
}

// State-changing calls go out as POST XMLHttpRequests: the CsrfGuard page script wraps
// XMLHttpRequest and attaches the session's CSRF token header to each one, which a
// fetch() call would not carry.
function postViewletRequest(url, onDone, onFail) {
    try {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', url);
        xhr.onload = function () {
            if (onDone) {
                var data = null;
                try {
                    data = JSON.parse(xhr.responseText);
                } catch (e) {
                }
                onDone(xhr.status, data, xhr.getResponseHeader('content-type') || '');
            }
        };
        xhr.onerror = function () {
            if (onFail) {
                onFail();
            }
        };
        xhr.send();
    } catch (e) {
        if (onFail) {
            onFail();
        }
    }
}

function doLaunchViewlet(ctx, demographicNo, key, displayMode) {
    var url = ctx + '/viewletLaunch.do?demographicNo=' + encodeURIComponent(demographicNo)
        + '&key=' + encodeURIComponent(key);
    postViewletRequest(url, function (status, data, contentType) {
        if (contentType.indexOf('application/json') === -1 || !data) {
            // A non-JSON reply means the request never reached the launch endpoint,
            // usually an expired ONE ID session or a missing EHR service permission.
            alert('The EHR service could not be launched. Your ONE ID session or EHR service access may have expired. Sign in again and retry.');
            return;
        }
        if (status === 268) {
            var summary = data.summary ? data.summary : 'The EHR service could not be launched.';
            if (data.stepUp) {
                startOneIdStepUp(ctx, demographicNo, key, displayMode, summary);
            } else {
                alert(summary);
            }
            return;
        }
        if (!data.viewletUrl) {
            alert('The EHR service returned no address to open.');
            return;
        }
        var timeout = viewletTimeout(data.timeoutMillis);
        var uuid = data.uuid;
        if (displayMode === 'modal') {
            openViewletModal(data.viewletUrl, demographicNo, ctx, timeout, key, uuid);
        } else {
            popupEHRService(data.viewletUrl, demographicNo, ctx, timeout, key, uuid);
        }
    }, function () {
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
    postViewletRequest(url, null, null);
}

function popupEHRService(url, demographicNo, ctx, timeout, key, uuid) {
    var windowProperties = 'height=800,width=1300,location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0';
    var popup = window.open(url, 'EHR Service', windowProperties);
    if (!popup) {
        alert('The EHR service window was blocked. Allow pop-ups for this site and try again.');
        return;
    }
    openPopupViewlet = popup;
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

    // A window with no acknowledgment from the Viewlet within the wait time is offered
    // a bounded number of reloads; when the attempts run out, or the clinician declines,
    // the user is informed and the launch is cancelled by closing the window (the close
    // poller records the failure and clears the patient context). Any message from the
    // Viewlet counts as its acknowledgment and ends the watch.
    var reloadAttempts = 0;
    var waitTimer = null;
    function armWaitTimer() {
        waitTimer = setTimeout(function () {
            if (responded || !popup || popup.closed) {
                return;
            }
            if (reloadAttempts < VIEWLET_MAX_RELOAD_ATTEMPTS
                && confirm('The EHR service has not responded. Reload it now? Choosing Cancel closes it.')) {
                reloadAttempts++;
                popup = window.open(url, 'EHR Service', windowProperties);
                openPopupViewlet = popup;
                armWaitTimer();
            } else {
                alert('The EHR service failed to launch and will be closed.');
                popup.close();
            }
        }, timeout);
    }
    armWaitTimer();

    if (demographicNo) {
        viewletWindowOpened();
        var poll = setInterval(function () {
            if (!popup || popup.closed) {
                clearInterval(poll);
                clearTimeout(waitTimer);
                window.removeEventListener('message', onMessage);
                reportOnce('failure', 'The EHR service window was closed without a response.');
                if (openPopupViewlet === popup) {
                    openPopupViewlet = null;
                }
                viewletWindowClosed();
                closeViewletPatientContext(ctx, demographicNo);
            }
        }, 1000);
    }
    popup.focus();
}

function openViewletModal(url, demographicNo, ctx, timeout, key, uuid) {
    // Only one modal viewlet is shown at a time; a dialog already open is finalized
    // and replaced. Its patient context is not cleared here because the incoming
    // launch has already moved the context on the server.
    if (currentModalDispose) {
        currentModalDispose();
    }
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

    function finalizeModal(reportStatus, reportMessage, skipPatientClose) {
        if (closed) {
            return;
        }
        closed = true;
        clearTimeout(waitTimer);
        window.removeEventListener('message', onMessage);
        reportOnce(reportStatus, reportMessage);
        if (overlay.parentNode) {
            document.body.removeChild(overlay);
        }
        if (currentModalDispose === dispose) {
            currentModalDispose = null;
        }
        viewletWindowClosed();
        if (!skipPatientClose && demographicNo) {
            closeViewletPatientContext(ctx, demographicNo);
        }
    }

    function closeModal() {
        finalizeModal('failure', 'The EHR service was closed without a response.', false);
    }

    var dispose = function () {
        finalizeModal('failure', 'The EHR service was replaced by another EHR service launch.', true);
    };

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

    function reloadFrame() {
        reloadAttempts++;
        loaded = false;
        status.textContent = 'Loading the EHR service…';
        frame.src = url;
        clearTimeout(waitTimer);
        armWaitTimer();
    }

    reloadButton.onclick = function () {
        if (reloadAttempts >= VIEWLET_MAX_RELOAD_ATTEMPTS) {
            status.textContent = 'The EHR service did not load. Please close and try again later.';
            reloadButton.style.display = 'none';
            return;
        }
        reloadFrame();
    };

    closeButton.onclick = closeModal;

    // A dialog whose frame never rendered within the wait time is offered a bounded
    // number of reloads; when the attempts run out, or the clinician declines, the user
    // is informed and the launch is cancelled with the failure recorded. A rendered
    // frame is left alone - the clinician may be working in it, and its outcome comes
    // from the completion message or the close.
    function armWaitTimer() {
        waitTimer = setTimeout(function () {
            if (loaded || closed) {
                return;
            }
            if (reloadAttempts < VIEWLET_MAX_RELOAD_ATTEMPTS
                && confirm('The EHR service is not loading. Reload it? Choosing Cancel closes it.')) {
                reloadFrame();
            } else {
                alert('The EHR service failed to launch and will be closed.');
                finalizeModal('failure', 'The EHR service did not load within the wait time.', false);
            }
        }, timeout);
    }
    armWaitTimer();

    actions.appendChild(reloadButton);
    actions.appendChild(closeButton);
    bar.appendChild(status);
    bar.appendChild(actions);
    dialog.appendChild(bar);
    dialog.appendChild(frame);
    overlay.appendChild(dialog);
    document.body.appendChild(overlay);
    currentModalDispose = dispose;
    viewletWindowOpened();
}

function closeViewletPatientContext(ctx, demographicNo) {
    postViewletRequest(ctx + '/viewletPatientClose.do?demographicNo=' + encodeURIComponent(demographicNo),
        function (status) {
            if (status !== 200) {
                alert('There was an error removing the patient from the context. Please try again.');
            }
        },
        function () {
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
