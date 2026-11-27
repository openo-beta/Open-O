package ca.openosp.openo.documentManager;

import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.managers.DocumentManager;
import ca.openosp.openo.managers.EmailComposeManager;
import ca.openosp.openo.managers.FaxManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;

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
    static final String NO_FAX_RIGHT = "You don't have permission to send faxes";
    static final String NO_EMAIL_RIGHT = "You don't have permission to send emails";
    static final String UNLINKED = "Link this document to a patient to fax or email it";
    static final String NO_PATIENT_ACCESS = "You don't have access to this patient's record";
    static final String UNAVAILABLE = "Fax and Email are unavailable for this document";

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Works out the Fax and Email buttons for a document, for the logged-in user.
     *
     * <p>A greyed-out button gets one reason, checked in this order: the document is deleted; it is neither a PDF
     * nor an image; the clinic hasn't set up faxing or email; the user doesn't have the write right needed to
     * send; the document isn't filed to a patient; the user can't open that patient's record. The last is the
     * rule the fax and email windows apply ({@link DocumentManager#getSendableDemographicNo}), and Email also
     * needs the right to read the patient's details, which the email window shows. The patient's email details
     * are looked up only when Email can be used.</p>
     *
     * @param loggedInInfo LoggedInInfo the logged-in user
     * @param document EDoc the document the view shows
     * @return DocumentSendButtons the state of both buttons
     * @throws RuntimeException if the user may not read documents, or a lookup fails
     */
    public static DocumentSendButtons forDocument(LoggedInInfo loggedInInfo, EDoc document) {
        SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
        EmailComposeManager emailComposeManager = SpringUtils.getBean(EmailComposeManager.class);
        DocumentManager documentManager = SpringUtils.getBean(DocumentManager.class);
        Integer demographicNo = documentManager.getLinkedDemographicNo(loggedInInfo, Integer.valueOf(document.getDocId()));

        String documentReason = document.getStatus() == Document.STATUS_DELETED ? DELETED
                : document.isPrintable() ? null : NOT_PDF_OR_IMAGE;

        String faxBlockedReason = firstReason(documentReason,
                FaxManager.isEnabled() ? null : NO_FAX_SENDERS,
                hasWriteRight(securityInfoManager, loggedInInfo, "_fax") ? null : NO_FAX_RIGHT,
                patientReason(securityInfoManager, loggedInInfo, demographicNo, false));
        String emailBlockedReason = firstReason(documentReason,
                emailComposeManager.isEmailEnabled() ? null : EMAIL_NOT_SET_UP,
                hasWriteRight(securityInfoManager, loggedInInfo, "_email") ? null : NO_EMAIL_RIGHT,
                patientReason(securityInfoManager, loggedInInfo, demographicNo, true));
        if (emailBlockedReason != null) {
            return new DocumentSendButtons(faxBlockedReason, emailBlockedReason, demographicNo, false, "", "");
        }

        String[] consent = emailComposeManager.getEmailConsentStatus(loggedInInfo, demographicNo);
        return new DocumentSendButtons(faxBlockedReason, null, demographicNo,
                emailComposeManager.hasValidRecipient(loggedInInfo, demographicNo), consent[0], consent[1]);
    }

    /**
     * Returns the buttons for a document as JSON, for the document view's script. When they can't be worked out,
     * both are greyed out, so the document view still loads.
     *
     * @param loggedInInfo LoggedInInfo the logged-in user
     * @param document EDoc the document the view shows
     * @return String the JSON object, with one field per record component
     */
    public static String jsonFor(LoggedInInfo loggedInInfo, EDoc document) {
        DocumentSendButtons buttons;
        try {
            buttons = forDocument(loggedInInfo, document);
        } catch (RuntimeException e) {
            logger.error("Could not work out the Fax and Email buttons for document " + document.getDocId(), e);
            buttons = new DocumentSendButtons(UNAVAILABLE, UNAVAILABLE, null, false, "", "");
        }
        return buttons.toJson();
    }

    /**
     * Returns this state as JSON, for the document view's script.
     *
     * @return String the JSON object, with one field per record component
     */
    public String toJson() {
        try {
            return JSON.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // Every component is a string, number or boolean
            throw new IllegalStateException(e);
        }
    }

    private static boolean hasWriteRight(SecurityInfoManager securityInfoManager, LoggedInInfo loggedInInfo, String securityObject) {
        return securityInfoManager.hasPrivilege(loggedInInfo, securityObject, SecurityInfoManager.WRITE, null);
    }

    // Saving a patient on the view clears "unlinked", so it comes after the reasons that saving won't fix
    private static String patientReason(SecurityInfoManager securityInfoManager, LoggedInInfo loggedInInfo,
                                        Integer demographicNo, boolean readsPatientDetails) {
        if (demographicNo == null) {
            return UNLINKED;
        }
        boolean canOpenPatient = securityInfoManager.hasPrivilege(loggedInInfo, "_edoc", SecurityInfoManager.READ, demographicNo)
                && securityInfoManager.isAllowedAccessToPatientRecord(loggedInInfo, demographicNo)
                && (!readsPatientDetails
                    || securityInfoManager.hasPrivilege(loggedInInfo, "_demographic", SecurityInfoManager.READ, demographicNo));
        return canOpenPatient ? null : NO_PATIENT_ACCESS;
    }

    private static String firstReason(String... reasons) {
        for (String reason : reasons) {
            if (reason != null) {
                return reason;
            }
        }
        return null;
    }
}
