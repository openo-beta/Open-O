/*
 * Launch and window control for the provincial EHR services (ONE ID Viewlets).
 * launchViewlet fetches the launch URL for a patient and opens it in the Viewlet's
 * configured interface: a shared named window, or an in-page modal dialog. Closing
 * either clears the patient from the EHR context.
 */

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
                alert(result.data && result.data.summary ? result.data.summary : 'The EHR service could not be launched.');
                return;
            }
            if (!result.data || !result.data.viewletUrl) {
                alert('The EHR service returned no address to open.');
                return;
            }
            if (displayMode === 'modal') {
                openViewletModal(result.data.viewletUrl, demographicNo, ctx);
            } else {
                popupEHRService(result.data.viewletUrl, demographicNo, ctx);
            }
        })
        .catch(function () {
            alert('The EHR service could not be launched. Please try again.');
        });
}

function popupEHRService(url, demographicNo, ctx) {
    var windowProperties = 'height=800,width=1300,location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0';
    var popup = window.open(url, 'EHR Service', windowProperties);
    if (!popup) {
        alert('The EHR service window was blocked. Allow pop-ups for this site and try again.');
        return;
    }
    if (demographicNo) {
        var poll = setInterval(function () {
            if (popup.closed) {
                clearInterval(poll);
                closeViewletPatientContext(ctx, demographicNo);
            }
        }, 1000);
    }
    popup.focus();
}

function openViewletModal(url, demographicNo, ctx) {
    // Only one modal viewlet is shown at a time; replace any that is already open.
    var existingOverlay = document.getElementById('viewletModalOverlay');
    if (existingOverlay) {
        document.body.removeChild(existingOverlay);
    }
    var overlay = document.createElement('div');
    overlay.id = 'viewletModalOverlay';
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:10000;display:flex;align-items:center;justify-content:center;';

    var dialog = document.createElement('div');
    dialog.style.cssText = 'background:#fff;border-radius:4px;padding:8px;box-shadow:0 4px 24px rgba(0,0,0,0.4);display:flex;flex-direction:column;';

    var closeButton = document.createElement('button');
    closeButton.type = 'button';
    closeButton.textContent = 'Close';
    closeButton.style.cssText = 'align-self:flex-end;margin-bottom:6px;';
    closeButton.onclick = function () {
        document.body.removeChild(overlay);
        if (demographicNo) {
            closeViewletPatientContext(ctx, demographicNo);
        }
    };

    var frame = document.createElement('iframe');
    frame.id = 'viewletModalFrame';
    frame.src = url;
    frame.width = '540';
    frame.height = '600';
    frame.style.border = 'none';
    frame.setAttribute('sandbox', 'allow-forms allow-scripts allow-same-origin allow-modals');

    dialog.appendChild(closeButton);
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
