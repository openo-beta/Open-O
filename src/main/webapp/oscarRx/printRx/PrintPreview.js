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
        fetch(url, {
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
    imgFileElement.value = imgFileElement.dataset.tempPath || signatureImg;
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

function closeRxPreviewBootstrapModal() {
    const modal = document.getElementById('rxPreviewBootstrapModal');
    let bsModal = bootstrap.Modal.getInstance(modal);
    if (bsModal) {
        bsModal.hide();
    }
}

function onPrint2(method, scriptId, useSC, scAddress, ctx) {
    ctx = resolveContextPath(ctx);
    const rxPageSize = $('printPageSize').value;
    console.log("rxPagesize  " + rxPageSize);

    const action = ctx + "/form/createcustomedpdf?__title=Rx&__method=" + method + "&useSC=" + useSC + "&scAddress=" + scAddress + "&rxPageSize=" + rxPageSize + "&scriptId=" + scriptId;
    const form = document.getElementById("preview2Form");

    if (method === "oscarRxFax") {
        // The preview is now inlined into the host page (it used to be its own iframe). Rather than
        // navigate the window with a form POST, submit the fax via AJAX and handle the result in-page.
        // URLSearchParams keeps the body application/x-www-form-urlencoded so the servlet's getParameter
        // still reads the fields. The servlet returns JSON when asked with __format=json; the legacy
        // ViewScript2.jsp flow omits the flag and still receives its original HTML response, so this
        // change does not affect that path.
        const modalBody = document.getElementById("rxPreviewBootstrapModalBody");
        // Disable the fax buttons while the request is in flight so a second click cannot queue a
        // duplicate fax. They are re-enabled in showFaxResult only if the fax fails.
        toggleFaxButtons(true);
        fetch(action + "&__format=json", {method: "POST", body: new URLSearchParams(new FormData(form))})
            .then(response => response.json())
            .then(result => showFaxResult(result, form, modalBody))
            .catch(() => showFaxResult({status: "error"}, form, modalBody));
        return true;
    }

    form.action = action;
    form.target = "_blank";
    form.submit();

    return true;
}

/**
 * Renders the fax result inside the Rx preview modal.
 *
 * On success it replaces the preview with the confirmation (filling the same area) and closes the window
 * after a short delay, matching the previous behaviour. On failure it leaves the preview - and its fax
 * controls - visible, re-enables the fax buttons, and shows the reason inline so the user can correct it
 * and retry without reopening the preview.
 *
 * @param result    {{status: string}} the JSON response from the fax servlet
 * @param form      the preview form, used as the source of the pharmacy name/fax for the message
 * @param modalBody the modal body element the preview was rendered into
 */
function showFaxResult(result, form, modalBody) {
    const status = result && result.status;

    if (status === "success") {
        // Replace the preview with the confirmation, filling the area the preview occupied so the modal
        // does not shrink, and leave the message in normal top-left document flow. Capture the height
        // before hiding the preview (the result is plain DOM now, not the old fixed-height iframe).
        const previewHeight = modalBody.clientHeight;
        Array.from(modalBody.children).forEach(el => el.style.display = "none");

        const confirmation = document.createElement("div");
        confirmation.id = "rxFaxResult";
        confirmation.style.minHeight = previewHeight + "px";
        confirmation.style.color = "green";
        modalBody.appendChild(confirmation);

        const heading = document.createElement("h3");
        heading.textContent = "Fax successfully generated";
        // Pharmacy name/fax are read from the submitted form and rendered via textContent (never innerHTML).
        const pharmaName = (form.querySelector("[name='pharmaName']") || {}).value || "";
        const pharmaFax = (form.querySelector("[name='pharmaFax']") || {}).value || "";
        const recipient = document.createElement("p");
        recipient.textContent = pharmaName + " (" + pharmaFax + ")";
        const closing = document.createElement("p");
        const countdown = document.createElement("b");
        countdown.textContent = "3";
        closing.append("This window will close in ", countdown, " seconds...");
        confirmation.append(heading, recipient, document.createElement("br"), closing);

        // Match the legacy behaviour: close the window once the fax is confirmed.
        setTimeout(() => window.top.close(), 3000);
        return;
    }

    // Failure: keep the preview and its controls visible, re-enable the fax buttons (disabled on submit),
    // and surface the reason inline so the user can correct it and retry.
    toggleFaxButtons(false);

    let message;
    if (status === "invalid_number") {
        message = "Error: Valid fax number not found!";
    } else if (status === "no_fax_config") {
        message = "Error: No matching fax line is configured.";
    } else {
        message = "Error: The fax could not be sent. Please try again.";
    }
    showFaxError(message);
}

/**
 * Shows (or updates) an inline fax error next to the fax button without disturbing the preview. Reuses a
 * single error element so repeated failures replace the message rather than stacking.
 *
 * @param message the error text to display
 */
function showFaxError(message) {
    const faxButton = document.getElementById("faxButton");
    if (!faxButton) {
        return;
    }
    let errorNote = document.getElementById("rxFaxError");
    if (!errorNote) {
        errorNote = document.createElement("div");
        errorNote.id = "rxFaxError";
        errorNote.style.color = "red";
        errorNote.style.fontWeight = "bold";
        errorNote.style.margin = "6px 0";
        faxButton.parentNode.insertBefore(errorNote, faxButton);
    }
    errorNote.textContent = message;
}

function sendFax(scriptId, signatureRequestId, useSC, scAddress, ctx) {
    if ('function' === typeof window.onbeforeunload) {
        window.onbeforeunload = null;
    }
    let faxNumber = document.getElementById('faxNumber');
    document.getElementById('finalFax').value = faxNumber.options[faxNumber.selectedIndex].value;
    document.getElementById('pdfId').value = signatureRequestId;

    onPrint2('oscarRxFax', scriptId, useSC, scAddress, ctx);
}

function printPasteToParent(ctx, rxPasteAsterisk, prefPharmacy, demographicNo, providerName, providerNo, pharmacyName,
                            pharmacyFax, prescribedBy) {
    printPaste2Parent(ctx, true, false, true, rxPasteAsterisk, prefPharmacy, demographicNo, providerName,
        providerNo, pharmacyName, pharmacyFax, prescribedBy)
}

function faxPasteToParent(ctx, rxPasteAsterisk, prefPharmacy, demographicNo, providerName, providerNo, pharmacyName,
                            pharmacyFax, prescribedBy) {
    printPaste2Parent(ctx, false, true, true, rxPasteAsterisk, prefPharmacy, demographicNo, providerName,
        providerNo, pharmacyName, pharmacyFax, prescribedBy)
}

function printPaste2Parent(ctx, print, fax, pasteRx, rxPasteAsterisk, prefPharmacy, demographicNo, providerName,
                           providerNo, pharmacyName, pharmacyFax, prescribedBy) {
    try {
        let text = "";
        const timeStamp = new Date().toLocaleString('en-US', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true,
            timeZone: 'UTC'
        });
        if (rxPasteAsterisk) {
            text += "**********************************************************************************\n";

        }
        if (print) {
            text += "Prescribed and printed by " + prescribedBy + "\n";

        } else if (fax) {
            text = "[Rx faxed to " + pharmacyName.replace(/\\n/g, '\n') + " Fax#: " + pharmacyFax;

            text += " prescribed by " + prescribedBy;

            text += "," + timeStamp + "]\n";
        }

        if (pasteRx) {
            const rxNoNewLines = document.getElementById("rx_no_newlines");
            if (rxNoNewLines && rxNoNewLines.value.length > 0) {
                text += rxNoNewLines.value + "\n";
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
                writeToEncounter(ctx, print, text, prefPharmacy, providerNo, demographicNo,
                    new Date().toISOString().substring(0, 10), providerName);
            }
        } else {
            writeToEncounter(ctx, print, text, prefPharmacy, providerNo, demographicNo,
                new Date().toISOString().substring(0, 10), providerName);
        }

    } catch (e) {
        alert("ERROR: could not paste to EMR" + e);
        if (print) {
            printIframe();
        }
    }

}