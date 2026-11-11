/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

function resetStash(ctx, myDrugRefEnabled) {
    const url = ctx + "/oscarRx/deleteRx.do?parameterValue=clearStash";
    const data = "";
    fetch(url, {
        method: 'POST',
        body: data
    }).then(() => {
        updateCurrentInteractions(myDrugRefEnabled);
    });
    parent.document.getElementById('rxText').innerHTML = "";//make pending prescriptions disappear.
    parent.document.getElementById('searchString').focus();
}

function updateCurrentInteractions(myDrugRefEnabled) {

    if(myDrugRefEnabled){
      fetch("GetmyDrugrefInfo.do?method=findInteractingDrugList")
        .then(response => {
          return fetch("UpdateInteractingDrugs.jsp")
            .then(response => response.text())
            .then(str => {
              str = str.replace('<script type="text/javascript">', '');
              str = str.replace('\<\/script\>', '');
              eval(str);
              if (myDrugRefEnabled) {
                callReplacementWebService("GetmyDrugrefInfo.do?method=view", 'interactionsRxMyD');
              }
            });
        });
    }
}

/**
 * Resolves the application context path used to build absolute request URLs.
 * When a context path is supplied (typically threaded from the JSP via
 * ${pageContext.request.contextPath}) it is returned unchanged; otherwise it is
 * derived from the current location so callers that cannot inject it still produce
 * absolute, deployment-correct URLs.
 *
 * @param {string} ctx the context path to use, if already known
 * @returns {string} an absolute context path (origin + first path segment when derived)
 */
function resolveContextPath(ctx) {
    if (!ctx) {
        const contextPath = window.location.pathname.split('/')[1];
        ctx = window.location.origin + '/' + contextPath;
    }
    return ctx;
}

function resetReRxDrugList(ctx) {
    ctx = resolveContextPath(ctx);
    const url = ctx + "/oscarRx/deleteRx.do?parameterValue=clearReRxDrugList";
    const data = "";
    fetch(url, {
        method: 'POST',
        body: data
    });
}

function setComment() {
    document.getElementById('additNotes').innerHTML = '<%=Encode.forJavaScript(comment.replaceAll("\n", "<br>"))%>';
    document.getElementsByName('additNotes')[0].value = frames['preview'].document.getElementById('additNotes').innerHTML;
}

function setDefaultAddr() {
    const url = "setDefaultAddr.jsp";
    const ran_number = Math.round(Math.random() * 1000000);
    const addr = encodeURIComponent(document.getElementById('addressSel').value);
    const params = `addr=${addr}&rand=${ran_number}`;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: params
    })
    .catch(error => {
        console.error('Error:', error);
    });
}

function addNotes(scriptId) {
    const url = "AddRxComment.jsp";
    const ran_number = Math.round(Math.random() * 1000000);
    const comment = encodeURIComponent(document.getElementById('additionalNotes').value);
    const params = `scriptNo=${scriptId}&comment=${comment}&rand=${ran_number}`;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: params
    });

    document.getElementById('additNotes').innerHTML = document.getElementById('additionalNotes').value.replace(/\n/g, "<br>");
    document.getElementsByName('additNotes')[0].value = document.getElementById('additionalNotes').value.replace(/\n/g, "\r\n");
}

function printIframe(providerNo, demographicNo, userName, ctx) {
    const browserName = navigator.appName;
    if (browserName === "Microsoft Internet Explorer") {
        alert("Use of Microsoft Internet Explorer is not permitted")
    } else {
        // Saved before the print dialog, because the window closes right after it
        whenSignatureSaved(ctx, () => {
            if ('function' === typeof window.onbeforeunload) {
                window.onbeforeunload = null;
            }

            printDivContent('printableContent');

            self.onfocus = function () {
                self.setTimeout(function () {
                    if (demographicNo) {
                        openEncounter(providerNo, demographicNo, providerNo, userName, ctx);
                    }
                    self.parent.close();
                }, 1000);
            };
        });
    }
}

function printDivContent(divId) {
    const content = document.getElementById(divId).innerHTML;

    const printFrame = document.createElement('iframe');
    printFrame.style.position = 'fixed';
    printFrame.style.right = '0';
    printFrame.style.bottom = '0';
    printFrame.style.width = '0';
    printFrame.style.height = '0';
    printFrame.style.border = '0';
    document.body.appendChild(printFrame);

    // Wait for iframe to load content, then print
    printFrame.onload = function () {
        printFrame.contentWindow.focus();
        printFrame.contentWindow.print();

        // change the focus back to the parent window
        self.onfocus();
    };

    // Write the content into the iframe
    const doc = printFrame.contentDocument || printFrame.contentWindow.document;
    doc.open();
    doc.write(`
  <html>
    <head>
      <title>Print Preview</title>
      <style>
        body { font-family: Arial, sans-serif; margin: 20px; border: 1px transparent; }

        table {
          width: 100%;
          border-collapse: collapse;
          page-break-inside: auto;
        }

        thead {
          display: table-header-group;
        }

        tbody {
          display: table-row-group;
        }

        tr {
          page-break-inside: avoid;
          page-break-after: auto;
        }

        th, td {
          padding: 8px;
          text-align: left;
        }
      </style>
    </head>
    <body>
      ${content}
    </body>
  </html>
`);
    doc.close();
}

function writeToEncounter(ctx, print, text, prefPharmacy, providerNo, demographicNo, curDate, userName) {
    try {
        const url = ctx + "/oscarRx/WriteToEncounter.do";
        // Return the chain so callers (e.g. Fax & Add) can sequence after the note write
        // completes; the trailing .catch resolves it on failure too, so faxing still proceeds.
        return fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: "prefPharmacy=" + encodeURIComponent(prefPharmacy) + "&additionalNotes=" + "&body=" + encodeURIComponent(text)
        })
            .then(() => {
                if (print) {
                    printIframe(providerNo, demographicNo, userName, ctx);
                }
            })
            .catch((e) => {
                alert("ERROR: could not paste to EMR" + e);
                if (print) {
                    printIframe(providerNo, demographicNo, userName, ctx);
                }
            });
    } catch (e) {
        alert("ERROR: could not paste to EMR" + e);
        return Promise.resolve();
    }
}

function openEncounter(providerNo, demographicNo, curProviderNo, userName, ctx) {
    ctx = resolveContextPath(ctx);
    const windowProps = "height=710,width=1024,location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=50,screenY=50,top=20,left=20";
    const currentDate = new Date().toISOString().substring(0, 10);
    const url = ctx + "/oscarEncounter/IncomingEncounter.do?providerNo=" + providerNo + "&demographicNo=" + demographicNo + "&curProviderNo=" + curProviderNo + "&userName=" + userName + "&curDate=" + currentDate;

    if (window.parent.opener && window.parent.opener.document.forms["caseManagementEntryForm"] !== undefined) {
        // redirect if encounter window open
        window.parent.opener.location = url;
        return window.parent.opener;
    }

    return window.open(url, "encounter", windowProps);
}

function togglePharmaInfoVisibility(show) {
    const pharmaInfoDiv = document.getElementById("pharmInfo");
    if (pharmaInfoDiv) {
        pharmaInfoDiv.style.visibility = show ? "visible" : "hidden";
    }
}

function toggleView(form) {
    var dxCode = (form.hsfo_Hypertension.checked ? 1 : 0) + (form.hsfo_Diabetes.checked ? 2 : 0) + (form.hsfo_Dyslipidemia.checked ? 4 : 0);
    // send dx code to HsfoPreview.jsp so that it will be displayed and persisted there
    document.getElementById("hsfoPop").style.display = "none";
    document.getElementById("bodyView").style.display = "block";
    document.getElementById("preview").src = "HsfoPreview.jsp?dxCode=" + dxCode;
}

function  ShowDrugInfo(drug) {
    window.open("drugInfo.do?GN=" + escape(drug), "_blank", "location=no, menubar=no, toolbar=no, scrollbars=yes, status=yes, resizable=yes");
}

function refreshImage(imgURL, signatureImg) {
    if (document.getElementById("signature") != null) {
        document.getElementById("signature").src = imgURL;
    }
    // The PDF servlet loads the signature via Image.getInstance(imgFile), which expects a
    // local file path (or absolute URL) - not the context-relative preview servlet URL.
    // Submit the signature's temp file path (rendered server-side as data-temp-path),
    // mirroring the working ViewScript2.jsp flow; fall back to the preview URL if absent.
    const imgFileElement = document.getElementById('imgFile');
    if (imgFileElement) {
        imgFileElement.value = imgFileElement.dataset.tempPath || signatureImg;
    }
}

function unloadMess() {
    let mess = "Signature found, but fax has not been sent.";
    if (isSignatureDirty) {
        mess = "Signature changed, but not saved and fax not sent.";
    }
    return mess;
}

function setDigitalSignatureToRx(ctx, digitalSignatureId, scriptId) {
    fetch(ctx + '/oscarRx/saveDigitalSignature.do', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'method=saveDigitalSignature&digitalSignatureId=' + digitalSignatureId + '&scriptId=' + scriptId
    }).then(r => console.log(r));  // TODO (Chitrank Dave): handle response properly
}

function toggleFaxButtons(disabled) {
    document.getElementById("faxButton").disabled = disabled;
    document.getElementById("faxPasteButton").disabled = disabled;
}

function showFaxWarning() {
    if (typeof hasFaxNumber !== 'undefined' && !hasFaxNumber) {
        document.getElementById("faxWarningNote").style.display = "block";
    }
}

function signatureHandler(result, contextPath, scriptNo) {
    isSignatureDirty = result.isDirty;
    isSignatureSaved = result.isSave;
    result.target.onbeforeunload = null;

    if (result.isSave) {

        if (contextPath && scriptNo) {
            try {
                let signId = new URLSearchParams(result.storedImageUrl.split('?')[1]).get('digitalSignatureId');
                setDigitalSignatureToRx(contextPath, signId, scriptNo);
            } catch (e) {
                console.error(e);
            }
        }

        if (result.storedImageUrl) {
            refreshImage(result.storedImageUrl, result.previewImageUrl);

            if (document.getElementById("faxButton")) {
                document.getElementById("faxButton").disabled = false;
                document.getElementById("faxPasteButton").disabled = false;
            }
        }
    }
}

/**
 * Shows a signature in the preview: the prescriber name in the header, on the signature line, and in
 * the field posted for the PDF and fax.
 *
 * @param {string} text the signature
 */
function showPrescriberSignature(text) {
    document.getElementById('prescriberSignatureHeader').textContent = text;
    document.getElementById('prescriberSignatureLine').textContent = text;
    document.getElementById('sigDoctorName').value = text;
}

/**
 * Shows the signature picked in the dropdown. Nothing is saved until the prescription is printed.
 *
 * @param {HTMLSelectElement} select the signature dropdown
 */
function previewPrescriberSignature(select) {
    showPrescriberSignature(select.options[select.selectedIndex].dataset.signature);
}

/**
 * Saves the picked signature on the prescription before its first print, fax or PDF, so the paper and
 * the chart show the same one, then locks the dropdown on it. Does nothing when it is saved already or
 * the page has no signature dropdown.
 *
 * @param {string} ctx the context path, or empty to work it out from the page
 * @returns {Promise<boolean>} true once the signature is saved, or when there is none to save; false, after
 *          telling the user, when it couldn't be saved or the picked one has been deleted
 */
function lockPrescriberSignature(ctx) {
    const select = document.getElementById('prescriberSignature');
    if (!select || select.dataset.locked === 'true') {
        return Promise.resolve(true);
    }

    const body = new URLSearchParams({
        scriptNo: select.dataset.scriptNo,
        demographicNo: select.dataset.demographicNo,
        signatureId: select.value
    });
    const csrfToken = document.getElementById('previewCsrfToken');
    if (csrfToken) {
        body.append(csrfToken.name, csrfToken.value);
    }

    return fetch(resolveContextPath(ctx) + '/oscarRx/prescriberSignature.do', {method: 'POST', body: body})
        .then(response => {
            if (response.status === 409) {
                // Deleted in another window: drop it, so the next pick is one that still exists, and go
                // back to the default (the prescriber name, the first option, if the default was the one deleted)
                select.remove(select.selectedIndex);
                const defaultOption = select.querySelector('option[data-default="true"]');
                select.selectedIndex = defaultOption ? defaultOption.index : 0;
                previewPrescriberSignature(select);
                alert(select.dataset.deletedError);
                return false;
            }
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            return response.json().then(result => {
                if (result.signature !== null) {
                    lockPrescriberSignatureCard(select, result.signature);
                }
                // Null when there was none to save (the prescriber has no name on file): print as shown
                return true;
            });
        })
        .catch(error => {
            console.error('Could not save the prescription signature', error);
            alert(select.dataset.saveError);
            return false;
        });
}

/**
 * Shows the signature saved on the prescription and locks the dropdown on it.
 *
 * @param {HTMLSelectElement} select the signature dropdown
 * @param {string} signature the saved signature
 */
function lockPrescriberSignatureCard(select, signature) {
    // Another window may have printed it first, so show what was actually saved
    showPrescriberSignature(signature);
    select.replaceChildren(new Option(signature, ''));
    select.disabled = true;
    select.dataset.locked = 'true';
    document.getElementById('prescriberSignatureSaved').hidden = false;
}

/**
 * Saves the signature (see lockPrescriberSignature), then does what the button does.
 *
 * @param {string} ctx the context path, or empty to work it out from the page
 * @param {function(): *} action what the button does once the signature is saved
 * @param {function(): *} [refused] what to do instead when it couldn't be saved; the user has been told already
 * @returns {Promise<*>} what action, or refused, returns
 */
function whenSignatureSaved(ctx, action, refused = () => undefined) {
    return lockPrescriberSignature(ctx).then(saved => saved ? action() : refused());
}

/**
 * A promise that never settles, for a button whose caller chains the next step with then(): that step
 * then never runs.
 *
 * @returns {Promise<never>} the promise
 */
function neverSettles() {
    return new Promise(() => {});
}

/**
 * The Rx icon: saves the signature, then opens the prescription as a PDF in a new tab.
 *
 * @param {string} method "print", or "rePrint" for a reprint
 * @param {string} scriptId the prescription's number
 * @param {boolean} useSC whether a satellite clinic address is used
 * @param {string} scAddress the satellite clinic address
 * @param {string} ctx the context path
 */
function printRxPdf(method, scriptId, useSC, scAddress, ctx) {
    // Opened during the click, so the pop-up blocker allows it although the PDF is posted after the save
    const target = 'rxPdf' + Date.now();
    const pdfWindow = window.open('', target);
    whenSignatureSaved(ctx,
        () => onPrint2(method, scriptId, useSC, scAddress, ctx, target),
        () => pdfWindow && pdfWindow.close());
}

function closeRxPreviewBootstrapModal() {
    const modal = document.getElementById('rxPreviewBootstrapModal');
    let bsModal = bootstrap.Modal.getInstance(modal);
    if (bsModal) {
        bsModal.hide();
    }
}

function onPrint2(method, scriptId, useSC, scAddress, ctx, target) {
    ctx = resolveContextPath(ctx);
    const rxPageSize = $('printPageSize').value;
    console.log("rxPagesize  " + rxPageSize);

    let action = ctx
        + "/form/createcustomedpdf?__title=Rx"
        + "&__method=" + encodeURIComponent(method)
        + "&useSC=" + encodeURIComponent(useSC)
        + "&scAddress=" + encodeURIComponent(scAddress)
        + "&rxPageSize=" + encodeURIComponent(rxPageSize)
        + "&scriptId=" + encodeURIComponent(scriptId);
    document.getElementById("preview2Form").action = action;
    if (method !== "oscarRxFax") {
        document.getElementById("preview2Form").target = target || "_blank";
    }
    document.getElementById("preview2Form").submit();

    return true;
}

function sendFax(scriptId, signatureRequestId, useSC, scAddress, ctx) {
    // The fax carries the signature saved on the prescription, so it is saved first
    whenSignatureSaved(ctx, () => {
        if ('function' === typeof window.onbeforeunload) {
            window.onbeforeunload = null;
        }
        let faxNumber = document.getElementById('faxNumber');
        document.getElementById('finalFax').value = faxNumber.options[faxNumber.selectedIndex].value;
        document.getElementById('pdfId').value = signatureRequestId;

        onPrint2('oscarRxFax', scriptId, useSC, scAddress, ctx);
    });
}

function printPasteToParent(ctx, rxPasteAsterisk, prefPharmacy, demographicNo, providerName, providerNo, pharmacyName,
                            pharmacyFax, prescribedBy, pharmacyPhone = "") {
    // Saved before the note is pasted, so a failed save pastes nothing
    return whenSignatureSaved(ctx, () => printPaste2Parent(ctx, true, false, true, rxPasteAsterisk, prefPharmacy,
        demographicNo, providerName, providerNo, pharmacyName, pharmacyFax, prescribedBy, pharmacyPhone));
}

function faxPasteToParent(ctx, rxPasteAsterisk, prefPharmacy, demographicNo, providerName, providerNo, pharmacyName,
                            pharmacyFax, prescribedBy, pharmacyPhone = "") {
    // Saved before the note is pasted; when it fails, the button's chained fax doesn't run either
    return whenSignatureSaved(ctx, () => printPaste2Parent(ctx, false, true, true, rxPasteAsterisk, prefPharmacy,
        demographicNo, providerName, providerNo, pharmacyName, pharmacyFax, prescribedBy, pharmacyPhone), neverSettles);
}

function printPaste2Parent(ctx, print, fax, pasteRx, rxPasteAsterisk, prefPharmacy, demographicNo, providerName,
                           providerNo, pharmacyName, pharmacyFax, prescribedBy, pharmacyPhone = "") {
    try {
        let text = "";
        const timeStamp = new Date().toLocaleString('en-US', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
        if (rxPasteAsterisk) {
            text += "**********************************************************************************\n";

        }
        if (print) {
            text += "Prescribed and printed by " + prescribedBy + "\n";

        } else if (fax) {
            text = "[Rx faxed to " + pharmacyName.replace(/\\n/g, '\n') + " Fax#: " + pharmacyFax;

            if (pharmacyPhone && pharmacyPhone.trim() !== "") {
                text += " Tel: " + pharmacyPhone.trim();
            }

            text += " prescribed by " + prescribedBy;

            text += ", " + timeStamp + "]\n";
        }

        if (pasteRx) {
            const rxNoNewLines = document.getElementById("rx_no_newlines");
            if (rxNoNewLines && rxNoNewLines.value.length > 0) {
                text += rxNoNewLines.value + "\n";
            }
            const additionalNotes = document.getElementById("additionalNotes");
            if (additionalNotes && additionalNotes.value.trim().length > 0) {
                text += "\n" + additionalNotes.value.trim() + "\n";
            }
        }
        if (rxPasteAsterisk) {
            if (prefPharmacy != null && prefPharmacy.trim() !== "") {
                text += prefPharmacy + "\n"
            }
            text += "****" + providerName
                + "********************************************************************************\n";
        }

        text = text.replace(/\\n/g, '\n');

        let noteEditor = "noteEditor" + demographicNo;
        if (window.parent.opener) {
            if (window.parent.opener.document.forms["caseManagementEntryForm"] !== undefined
                && window.parent.opener.document.forms["caseManagementEntryForm"].demographicNo
                && window.parent.opener.document.forms["caseManagementEntryForm"].demographicNo.value === demographicNo) {
                //oscarLog("3");
                window.parent.opener.pasteToEncounterNote(text);
                if (print) {
                    printIframe();
                }
            } else if (window.parent.opener.document.encForm !== undefined) {
                //oscarLog("4");
                window.parent.opener.document.encForm.enTextarea.value =
                    window.parent.opener.document.encForm.enTextarea.value + text;
                if (print) {
                    printIframe();
                }
            } else if (window.parent.opener.document.getElementById(noteEditor) !== null) {
                window.parent.opener.document.getElementById(noteEditor).value =
                    window.parent.opener.document.getElementById(noteEditor).value + text;
                if (print) {
                    printIframe();
                }
            } else if (pasteRx) {
                // Standalone path: return the async write so the caller can wait for it.
                return writeToEncounter(ctx, print, text, prefPharmacy, providerNo, demographicNo,
                    new Date().toISOString().substring(0, 10), providerName);
            }
        } else {
            // Standalone path: return the async write so the caller can wait for it.
            return writeToEncounter(ctx, print, text, prefPharmacy, providerNo, demographicNo,
                new Date().toISOString().substring(0, 10), providerName);
        }

        // Opener paths above mutate the note synchronously; resolve immediately.
        return Promise.resolve();
    } catch (e) {
        alert("ERROR: could not paste to EMR" + e);
        if (print) {
            printIframe();
        }
        return Promise.resolve();
    }

}