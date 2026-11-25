function popupPatient(height, width, url, windowName, docId) {
    d = document.getElementById('demofind' + docId).value; //demog  //attachedDemoNo
    urlNew = url + d;

    return popup2(height, width, 0, 0, urlNew, windowName);
}

function popupPatientTicklerPlus(height, width, url, windowName, docId) {
    d = document.getElementById('demofind' + docId).value; //demog  //attachedDemoNo
    n = document.getElementById('demofindName' + docId).value;
    urlNew = url + "method=edit&tickler.demographic_webName=" + n + "&tickler.demographicNo=" + d + "&docType=DOC&docId=" + docId;

    return popup2(height, width, 0, 0, urlNew, windowName);
}

function popupPatientTickler(height, width, url, windowName, docId) {
    d = document.getElementById('demofind' + docId).value; //demog  //attachedDemoNo
    n = document.getElementById('demofindName' + docId).value;
    urlNew = url + "demographic_no=" + d + "&name=" + n + "&chart_no=&bFirstDisp=false&messageID=null&remoteFacilityId=&docType=DOC&docId=" + docId;
    return popup2(height, width, 0, 0, urlNew, windowName);
}

function setupDemoAutoCompletion(docId, contextPath) {
	let autoCompleteId = "#autocompletedemo" + docId;
	if (jQuery(autoCompleteId)) {

		var url = contextPath + "/demographic/SearchDemographic.do?jqueryJSON=true&activeOnly=" + jQuery("#activeOnly" + docId).is(":checked");

		jQuery(autoCompleteId).autocomplete({
			source: url, minLength: 2,

			focus: function (event, ui) {
				jQuery(autoCompleteId).val(ui.item.label);
				return false;
			}, select: function (event, ui) {
				jQuery(autoCompleteId).val(ui.item.label);
				jQuery("#demofind" + docId).val(ui.item.value);
				jQuery("#demofindName" + docId).val(ui.item.formattedName);
				selectedDemos.push(ui.item.label);
				console.log(ui.item.providerNo);

				if (ui.item.providerNo !== undefined && ui.item.providerNo != null && ui.item.providerNo !== "" && ui.item.providerNo !== "null") {
					addDocToList(ui.item.providerNo, ui.item.provider + " (MRP)", docId);
				}

				// enable Save button whenever a selection is made
				jQuery('#save' + docId).removeAttr('disabled');
				jQuery('#saveNext' + docId).removeAttr('disabled');

				jQuery('#msgBtn_' + docId).removeAttr('disabled');
				jQuery('#mainTickler_' + docId).removeAttr('disabled');
				jQuery('#mainEchart_' + docId).removeAttr('disabled');
				jQuery('#mainMaster_' + docId).removeAttr('disabled');
				jQuery('#mainApptHistory_' + docId).removeAttr('disabled');

				return false;
			}
		});
	}
}

/**
 * Shows one document's Fax and Email buttons from the server's DocumentSendButtons: a greyed-out button
 * shows why as its hover tip. Runs when the document view loads, and again after a save (refreshSendButtons).
 *
 * @param {string} docId the document number
 * @param {Object} sendButtons the DocumentSendButtons JSON
 */
function showSendButtons(docId, sendButtons) {
    showSendButton('faxBtn_' + docId, sendButtons.faxBlockedReason, 'Fax this document', () =>
        popup(900, 1000, contextpath + '/fax/faxAction.do?method=prepareFax&transactionType=DOCUMENT&transactionId='
            + encodeURIComponent(docId) + '&demographicNo=' + encodeURIComponent(sendButtons.demographicNo), 'faxDocument' + docId));

    showSendButton('emailBtn_' + docId, sendButtons.emailBlockedReason, 'Email this document to the patient', () => {
        if (confirmEmailRecipient(sendButtons)) {
            popup(900, 1000, contextpath + '/email/emailComposeAction.do?method=prepareComposeDocumentMailer&documentNo='
                + encodeURIComponent(docId), 'emailDocument' + docId);
        }
    });
}

// Greys out the button with the reason as its tip, or turns it on with the usual tip
function showSendButton(buttonId, blockedReason, tip, send) {
    const button = document.getElementById(buttonId);
    button.disabled = blockedReason !== null;
    button.title = blockedReason || tip;
    button.onclick = send;
}

/**
 * The eForm toolbar's checks before emailing a patient: stop when the patient has no valid email address,
 * and ask staff to type "Yes" when the patient hasn't explicitly opted in. Keep in step with remoteEmail()
 * in eform/eformFloatingToolbar/eform_floating_toolbar.js.
 *
 * @param {Object} sendButtons the DocumentSendButtons JSON
 * @returns {boolean} true when the email window can open
 */
function confirmEmailRecipient(sendButtons) {
    if (!sendButtons.hasValidEmail) {
        alert("Sorry - this patient does not have a valid email address in their demographic. Please update their demographic and try again.");
        return false;
    }
    if (sendButtons.emailConsentStatus !== "Explicit Opt-In") {
        const userResponse = prompt("This patient has not explicitly opted-in: [" + sendButtons.emailConsentName + "]\nType 'Yes' to acknowledge you understand the risks before proceeding.", "No");
        return userResponse !== null && userResponse.toLowerCase() === 'yes';
    }
    return true;
}

/**
 * Asks the server for one document's Fax and Email buttons again, after a save that may have linked the
 * document to a patient.
 *
 * @param {string} docId the document number
 */
function refreshSendButtons(docId) {
    fetch(contextpath + '/documentManager/documentSendButtons.do?documentNo=' + encodeURIComponent(docId))
        .then(response => {
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            return response.json();
        })
        .then(sendButtons => showSendButtons(docId, sendButtons))
        .catch(error => console.error('Could not refresh the Fax and Email buttons:', error));
}
