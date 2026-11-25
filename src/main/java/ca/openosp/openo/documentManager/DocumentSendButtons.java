package ca.openosp.openo.documentManager;

import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.managers.DocumentManager;
import ca.openosp.openo.managers.EmailComposeManager;
import ca.openosp.openo.managers.FaxManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The document view's Fax and Email buttons for one document: whether each is greyed out and why, the patient
 * the document is filed to, and the patient's email details that the Email button checks before it opens the
 * email window, as the eForm toolbar does.
 *
 * <p>The document view ({@code showDocument.jsp}) builds this when it loads. After a save that may have linked
 * the document to a patient, it asks for it again from {@code documentManager/documentSendButtons.do}. The
 * fax and email windows still apply their own checks when they open and when staff send.</p>
 *
 * @param faxBlockedReason String why Fax is greyed out, shown as its hover tip, or null when Fax can be used
 * @param emailBlockedReason String why Email is greyed out, shown as its hover tip, or null when Email can be used
 * @param demographicNo Integer the patient the document is filed to, or null when it isn't filed to a patient
 * @param hasValidEmail boolean whether the patient has a valid email address; false while Email is greyed out
 * @param emailConsentName String the name of the clinic's email consent; empty while Email is greyed out
 * @param emailConsentStatus String the patient's email consent, such as "Explicit Opt-In"; empty while Email
 *                           is greyed out
 * @since 2026-11-25
 */
public record DocumentSendButtons(String faxBlockedReason, String emailBlockedReason, Integer demographicNo,
                                  boolean hasValidEmail, String emailConsentName, String emailConsentStatus) {

    static final String DELETED = "This document has been deleted";
    static final String NOT_PDF_OR_IMAGE = "Only PDFs and images can be sent";
    static final String NO_FAX_SENDERS = "No active fax senders";
    static final String EMAIL_NOT_SET_UP = "Consent or Sender Details Not Configured";
    static final String UNLINKED = "Link this document to a patient to fax or email it";

    private static final ObjectMapper JSON = new ObjectMapper();

    /**
     * Works out the Fax and Email buttons for a document, for the logged-in user.
     *
     * <p>Fax needs an active fax account and the fax write right, and Email needs a configured email account and
     * consent setting and the email write right, the rights needed to send. The patient's email details are
     * looked up only when Email can be used.</p>
     *
     * @param loggedInInfo LoggedInInfo the logged-in user
     * @param document EDoc the document the view shows
     * @return DocumentSendButtons the state of both buttons
     * @throws RuntimeException if the user may not read documents
     */
    public static DocumentSendButtons forDocument(LoggedInInfo loggedInInfo, EDoc document) {
        SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
        EmailComposeManager emailComposeManager = SpringUtils.getBean(EmailComposeManager.class);
        DocumentManager documentManager = SpringUtils.getBean(DocumentManager.class);
        Integer demographicNo = documentManager.getLinkedDemographicNo(loggedInInfo, Integer.valueOf(document.getDocId()));

        boolean canFax = FaxManager.isEnabled()
                && securityInfoManager.hasPrivilege(loggedInInfo, "_fax", SecurityInfoManager.WRITE, null);
        boolean canEmail = emailComposeManager.isEmailEnabled()
                && securityInfoManager.hasPrivilege(loggedInInfo, "_email", SecurityInfoManager.WRITE, null);

        String faxBlockedReason = blockedReason(document, canFax, NO_FAX_SENDERS, demographicNo);
        String emailBlockedReason = blockedReason(document, canEmail, EMAIL_NOT_SET_UP, demographicNo);
        if (emailBlockedReason != null) {
            return new DocumentSendButtons(faxBlockedReason, emailBlockedReason, demographicNo, false, "", "");
        }

        String[] consent = emailComposeManager.getEmailConsentStatus(loggedInInfo, demographicNo);
        return new DocumentSendButtons(faxBlockedReason, null, demographicNo,
                emailComposeManager.hasValidRecipient(loggedInInfo, demographicNo), consent[0], consent[1]);
    }

    /**
     * Returns this state as JSON, for the document view's script.
     *
     * @return String the JSON object, with one field per record component
     * @throws JsonProcessingException never in practice, since every component is a string, number or boolean
     */
    public String toJson() throws JsonProcessingException {
        return JSON.writeValueAsString(this);
    }

    // Saving a patient on the document view clears "unlinked", so it comes last and the tip names what else to fix
    private static String blockedReason(EDoc document, boolean canSend, String cannotSendReason, Integer demographicNo) {
        if (document.getStatus() == Document.STATUS_DELETED) {
            return DELETED;
        }
        if (!document.isPrintable()) {
            return NOT_PDF_OR_IMAGE;
        }
        if (!canSend) {
            return cannotSendReason;
        }
        if (demographicNo == null) {
            return UNLINKED;
        }
        return null;
    }
}
