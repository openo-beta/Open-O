//CHECKSTYLE:OFF
package ca.openosp.openo.email.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.owasp.encoder.Encode;

import ca.openosp.OscarProperties;
import ca.openosp.openo.commn.exception.DocumentNotSendableException;
import ca.openosp.openo.commn.model.EmailAttachment;
import ca.openosp.openo.commn.model.EmailLog.TransactionType;
import ca.openosp.openo.email.core.EmailAttachmentSettings;
import ca.openosp.openo.email.core.EmailComposeStore;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.managers.DocumentManager;
import ca.openosp.openo.managers.EmailComposeManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PDFGenerationException;
import ca.openosp.openo.utility.SpringUtils;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for composing and preparing email messages with patient-related attachments.
 *
 * This action handles the preparation of email composition screens for sending electronic forms (eForms),
 * documents, laboratory results, and other patient health information via email. It reads the email
 * settings handed over by the eForm, prepares attachments with optional PDF encryption, retrieves patient email consent
 * status, and validates recipient information before presenting the compose interface.
 *
 * Key Features:
 * <ul>
 *   <li>Prepares email composition interface for eForms and patient documents</li>
 *   <li>Reads the eForm email settings stored under the key on the compose URL</li>
 *   <li>Stores this page's attachment list under its key, so Send uses only this page's attachments</li>
 *   <li>Handles multiple attachment types: eForms, eDocuments, lab results, forms, HRM documents</li>
 *   <li>Generates and manages PDF password encryption for patient privacy</li>
 *   <li>Validates patient email consent status before sending</li>
 *   <li>Retrieves and validates recipient email addresses</li>
 *   <li>Sanitizes attachment filenames for security</li>
 *   <li>Validates numeric form ID (fid) parameters to prevent injection attacks</li>
 * </ul>
 *
 * Healthcare Context:
 * This action is part of OpenO EMR's secure patient communication system, ensuring that Protected Health
 * Information (PHI) is transmitted with appropriate encryption, consent verification, and audit logging.
 * It supports PIPEDA/HIPAA compliance by enforcing patient consent for email communications and providing
 * password-protected PDF attachments based on patient demographic data.
 *
 * Session Management:
 * The eForm stores its email settings with {@link EmailComposeStore} and redirects here with the key.
 * The entry stays until Send or Cancel, so a refresh shows the same email. The attachment list is stored under the
 * same key, and Send uses only that list.
 *
 * Security Considerations:
 * <ul>
 *   <li>Validates fid parameter to ensure numeric format (prevents injection)</li>
 *   <li>Uses OWASP Encode.forJava() for sanitizing invalid fid values in logs</li>
 *   <li>Generates patient-specific PDF passwords based on demographic information</li>
 *   <li>Sanitizes attachment filenames through EmailComposeManager</li>
 *   <li>Each compose page sends only its own attachment list, never another window's</li>
 * </ul>
 *
 * @see ca.openosp.openo.managers.EmailComposeManager
 * @see ca.openosp.openo.managers.DemographicManager
 * @see ca.openosp.openo.commn.model.EmailAttachment
 * @see ca.openosp.openo.commn.model.EmailConfig
 * @see ca.openosp.openo.commn.model.EmailLog.TransactionType
 * @since 2026-01-24
 */
public class EmailCompose2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final Logger logger = MiscUtils.getLogger();
    // Start of every reason a document can't be emailed; the compose page turns each literal \n into a line break
    private static final String DOCUMENT_NOT_EMAILED = "This document cannot be emailed.\\n\\n";
    private DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
    private EmailComposeManager emailComposeManager = SpringUtils.getBean(EmailComposeManager.class);
    private DocumentManager documentManager = SpringUtils.getBean(DocumentManager.class);
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Executes the default action for email composition.
     *
     * Routes on the "method" request parameter: prepareComposeDocumentMailer prepares the email of a
     * patient document; anything else prepares the email of an eForm.
     *
     * @return String the Struts2 result name, either "compose" for successful preparation
     *         or "eFormError" if PDF generation fails for an eForm email
     * @see #prepareComposeEFormMailer()
     * @see #prepareComposeDocumentMailer()
     */
    public String execute() {
        if ("prepareComposeDocumentMailer".equals(request.getParameter("method"))) {
            return prepareComposeDocumentMailer();
        }
        return prepareComposeEFormMailer();
    }

    /**
     * Prepares the email composition interface with patient information, attachments, and email settings.
     *
     * This method orchestrates the complete email composition preparation workflow:
     * <ol>
     *   <li>Reads the eForm email settings stored under the compose key on the URL</li>
     *   <li>Validates form ID (fid) parameter for numeric format to prevent injection</li>
     *   <li>Retrieves patient email consent status and validates consent settings</li>
     *   <li>Fetches patient demographic information for recipient name display</li>
     *   <li>Retrieves and validates recipient email addresses (separates valid/invalid)</li>
     *   <li>Loads available sender email account configurations</li>
     *   <li>Generates PDF password encryption based on patient demographics if not already set</li>
     *   <li>Prepares all attachment types: eForms, eDocuments, labs, forms, HRM documents</li>
     *   <li>Sanitizes attachment filenames for security</li>
     *   <li>Stores the attachment list under the compose key and sets request attributes for JSP rendering</li>
     * </ol>
     *
     * Stored Settings Used (EmailAttachmentSettings):
     * <ul>
     *   <li>attachEFormItSelf (Boolean) - whether to attach the eForm itself</li>
     *   <li>fdid (String) - form data ID for the eForm</li>
     *   <li>demographicId (String) - patient demographic identifier (required)</li>
     *   <li>emailPDFPassword (String) - password for PDF encryption</li>
     *   <li>emailPDFPasswordClue (String) - hint for PDF password</li>
     *   <li>attachedDocuments (String[]) - array of document IDs to attach</li>
     *   <li>attachedLabs (String[]) - array of lab result IDs to attach</li>
     *   <li>attachedForms (String[]) - array of form IDs to attach</li>
     *   <li>attachedEForms (String[]) - array of eForm IDs to attach</li>
     *   <li>attachedHRMDocuments (String[]) - array of HRM document IDs to attach</li>
     *   <li>senderEmail (String) - sender email address</li>
     *   <li>subjectEmail (String) - email subject line</li>
     *   <li>bodyEmail (String) - email message body</li>
     *   <li>footerEmail (String) - footer appended to the email but kept out of the chart note</li>
     *   <li>encryptedMessageEmail (String) - encrypted message content</li>
     *   <li>emailPatientChartOption (String) - patient chart email option setting</li>
     * </ul>
     *
     * Request Parameters:
     * <ul>
     *   <li>composeKey (String) - key of the eForm email settings; when unknown (sent, cancelled or logged out), an expired message is shown</li>
     *   <li>fid (String, optional) - form identifier, validated for numeric format</li>
     * </ul>
     *
     * Request Attributes Set:
     * <ul>
     *   <li>transactionType (TransactionType) - set to EFORM for transaction logging</li>
     *   <li>emailConsentName (String) - patient consent form name</li>
     *   <li>emailConsentStatus (String) - patient email consent status (Yes/No)</li>
     *   <li>receiverName (String) - formatted patient name for display</li>
     *   <li>receiverEmailList (List) - list of valid recipient email addresses</li>
     *   <li>invalidReceiverEmailList (List) - list of invalid email addresses</li>
     *   <li>senderAccounts (List&lt;EmailConfig&gt;) - available sender account configurations</li>
     *   <li>emailPDFPassword (String) - generated or existing PDF password</li>
     *   <li>emailPDFPasswordClue (String) - password hint for recipient</li>
     *   <li>demographicId (String) - patient demographic identifier</li>
     *   <li>fdid (String) - form data ID</li>
     *   <li>fid (String) - validated form ID or null if invalid</li>
     *   <li>emailAttachmentList (List&lt;EmailAttachment&gt;) - prepared and sanitized attachments</li>
     *   <li>composeKey (String) - key of this compose page, posted back on Send and Cancel</li>
     * </ul>
     *
     * Security Features:
     * <ul>
     *   <li>Validates fid parameter with regex pattern to ensure numeric format only</li>
     *   <li>Logs warnings for invalid fid values using OWASP-encoded output</li>
     *   <li>Generates patient-specific PDF passwords: YYYYMMDD (DOB) + 10-digit HIN</li>
     *   <li>Sanitizes all attachment filenames to prevent path traversal attacks</li>
     *   <li>Verifies patient email consent before allowing composition</li>
     *   <li>Keeps the attachment list on the server; the page posts back only its key</li>
     * </ul>
     *
     * Error Handling:
     * If PDF generation fails for any attachment (eForm, document, lab, form, HRM), the method
     * returns the "eFormError" result with a descriptive error message. This prevents incomplete
     * emails from being composed when required attachments cannot be generated.
     * If the compose key is unknown (the email was already sent or cancelled, or the user logged out),
     * the compose page shows an expired message and closes instead of composing an email without its settings.
     *
     * @return String the Struts2 result name: "compose" for successful preparation,
     *         "eFormError" if PDF generation fails for any attachment
     * @see ca.openosp.openo.managers.EmailComposeManager#getEmailConsentStatus(LoggedInInfo, Integer)
     * @see ca.openosp.openo.managers.EmailComposeManager#getRecipients(LoggedInInfo, Integer)
     * @see ca.openosp.openo.managers.EmailComposeManager#createEmailPDFPassword(LoggedInInfo, Integer)
     * @see ca.openosp.openo.managers.EmailComposeManager#prepareEFormAttachments(LoggedInInfo, String, String[])
     * @see ca.openosp.openo.managers.EmailComposeManager#sanitizeAttachments(List)
     * @see #eFormPageError(HttpServletRequest, String)
     * @see #composePageError(String)
     */
    public String prepareComposeEFormMailer() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        // Settings of the eForm that redirected here, stored under the key on this URL
        String composeKey = request.getParameter("composeKey");
        EmailComposeStore.Entry entry = EmailComposeStore.get(request.getSession(), composeKey);
        EmailAttachmentSettings settings = entry == null ? null : entry.settings();
        if (settings == null) {
            return composePageError("This email page has already been used or has expired. If you already pressed Send, check Manage Emails or the patient's chart. Otherwise open the eForm again and click Email.");
        }

        String fdid = settings.attachEFormItSelf() ? settings.fdid() : "";
        String demographicId = settings.demographicNo();
        String fid = request.getParameter("fid");
        String emailPDFPassword = settings.emailPDFPassword();
        String emailPDFPasswordClue = settings.emailPDFPasswordClue();

        // Validate fid is numeric if provided
        if (fid != null && !fid.matches("\\d+")) {
            String sanitizedFid = Encode.forJava(fid);
            logger.warn("Invalid fid parameter received: {}", sanitizedFid);
            fid = null;
        }

        setPatientAttributes(loggedInInfo, Integer.parseInt(demographicId), emailPDFPassword, emailPDFPasswordClue);

        List<EmailAttachment> emailAttachmentList = new ArrayList<>();
        try {
            emailAttachmentList.addAll(emailComposeManager.prepareEFormAttachments(loggedInInfo, fdid, settings.attachedEForms()));
            emailAttachmentList.addAll(emailComposeManager.prepareEDocAttachments(loggedInInfo, settings.attachedDocuments()));
            emailAttachmentList.addAll(emailComposeManager.prepareLabAttachments(loggedInInfo, settings.attachedLabs()));
            emailAttachmentList.addAll(emailComposeManager.prepareHRMAttachments(loggedInInfo, settings.attachedHRMDocuments()));
            emailAttachmentList.addAll(emailComposeManager.prepareFormAttachments(request, response, settings.attachedForms(), Integer.parseInt(demographicId)));
        } catch (PDFGenerationException e) {
            logger.error(e.getMessage(), e);
            EmailComposeStore.remove(request.getSession(), composeKey);
            return eFormPageError(request,"This eForm (and attachments, if applicable) could not be emailed. \\n\\n" + e.getMessage());
        }
        emailComposeManager.sanitizeAttachments(emailAttachmentList);

        // Set request attributes for JSP
        request.setAttribute("transactionType", TransactionType.EFORM);
        request.setAttribute("senderEmail", settings.senderEmail());
        request.setAttribute("subjectEmail", settings.subjectEmail());
        request.setAttribute("bodyEmail", settings.bodyEmail());
        request.setAttribute("footerEmail", settings.footerEmail());
        request.setAttribute("encryptedMessageEmail", settings.encryptedMessageEmail());
        request.setAttribute("emailPatientChartOption", settings.emailPatientChartOption());
        request.setAttribute("fdid", settings.fdid());
        request.setAttribute("fid", fid);
        request.setAttribute("isEmailEncrypted", settings.isEmailEncrypted());
        request.setAttribute("isEmailAttachmentEncrypted", settings.isEmailAttachmentEncrypted());
        request.setAttribute("isEmailAutoSend", settings.isEmailAutoSend());
        request.setAttribute("emailAttachmentList", emailAttachmentList);
        // Send uses only this list, stored under this page's key
        EmailComposeStore.setAttachments(request.getSession(), composeKey, emailAttachmentList);
        request.setAttribute("composeKey", composeKey);

        return "compose";
    }

    /**
     * Prepares the compose page for emailing one patient document from the document view.
     *
     * <p>The patient comes from the document itself, so the email can only go to the patient the
     * document is filed to, at the addresses on file. The subject and message start blank, and
     * encryption starts on, as for an eForm that doesn't set it. Send posts to sendDirectEmail with
     * this page's key; Cancel closes the popup.</p>
     *
     * <p>When the document can't be emailed (missing, deleted, not filed to a patient, or not a PDF or
     * an image), the compose page alerts the reason and closes.</p>
     *
     * Request Parameters:
     * <ul>
     *   <li>documentNo (String) - the document to email</li>
     * </ul>
     *
     * @return String the Struts2 result name "compose"
     * @throws SecurityException if the user may not send emails, read the patient's documents or open the patient's record
     * @since 2026-11-25
     */
    public String prepareComposeDocumentMailer() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        // The page shows the patient's addresses and attachment password, so only someone who may send gets it
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_email", SecurityInfoManager.WRITE, null)) {
            throw new SecurityException("missing required sec object (_email)");
        }
        String documentNo = request.getParameter("documentNo");
        if (documentNo == null || !documentNo.matches("\\d{1,9}")) {
            return composePageError(DOCUMENT_NOT_EMAILED + "No document was given.");
        }
        int documentId = Integer.parseInt(documentNo);

        // The email goes to the patient the document is filed to
        int demographicNo;
        List<EmailAttachment> emailAttachmentList;
        try {
            demographicNo = documentManager.getSendableDemographicNo(loggedInInfo, documentManager.getDocument(loggedInInfo, documentId));
            emailAttachmentList = emailComposeManager.prepareEDocAttachments(loggedInInfo, new String[]{documentNo});
        } catch (DocumentNotSendableException e) {
            return composePageError(DOCUMENT_NOT_EMAILED + e.getMessage());
        } catch (PDFGenerationException e) {
            // The message can hold the document description, so show a fixed reason and log only the cause
            logger.warn("Document " + documentId + " could not be emailed", e.getCause());
            return composePageError(DOCUMENT_NOT_EMAILED + "It couldn't be turned into a PDF attachment. Only PDFs and images can be emailed.");
        }
        emailComposeManager.sanitizeAttachments(emailAttachmentList);

        setPatientAttributes(loggedInInfo, demographicNo, null, null);
        request.setAttribute("transactionType", TransactionType.DOCUMENT);
        // A document has no eForm settings, so use the eForm default: both kinds of encryption on unless an eForm turns them off
        request.setAttribute("isEmailEncrypted", true);
        request.setAttribute("isEmailAttachmentEncrypted", true);
        request.setAttribute("emailAttachmentList", emailAttachmentList);
        // Nothing is handed over from another page, so this page stores its own entry, as a resend does; Send uses only this list
        request.setAttribute("composeKey", EmailComposeStore.putDocument(request.getSession(), demographicNo, emailAttachmentList));
        return "compose";
    }

    /**
     * Sets what the compose page shows about the patient: email consent, name, the addresses on file
     * and the sender accounts, plus the attachment password.
     *
     * @param loggedInInfo LoggedInInfo the logged in user
     * @param demographicNo int the patient the email is for
     * @param emailPDFPassword String the attachment password; null means the patient's default password
     *                         (date of birth and health number) with {@link EmailComposeManager#DEFAULT_PDF_PASSWORD_CLUE}
     * @param emailPDFPasswordClue String the password clue, used only with a password that isn't null
     */
    private void setPatientAttributes(LoggedInInfo loggedInInfo, int demographicNo, String emailPDFPassword, String emailPDFPasswordClue) {
        String[] emailConsent = emailComposeManager.getEmailConsentStatus(loggedInInfo, demographicNo);
        List<?>[] receiverEmailList = emailComposeManager.getRecipients(loggedInInfo, demographicNo);
        if (emailPDFPassword == null) {
            emailPDFPassword = emailComposeManager.createEmailPDFPassword(loggedInInfo, demographicNo);
            emailPDFPasswordClue = EmailComposeManager.DEFAULT_PDF_PASSWORD_CLUE;
        }

        request.setAttribute("emailConsentName", emailConsent[0]);
        request.setAttribute("emailConsentStatus", emailConsent[1]);
        request.setAttribute("receiverName", demographicManager.getDemographicFormattedName(loggedInInfo, demographicNo));
        request.setAttribute("receiverEmailList", receiverEmailList[0]);
        request.setAttribute("invalidReceiverEmailList", receiverEmailList[1]);
        request.setAttribute("senderAccounts", emailComposeManager.getAllSenderAccounts());
        request.setAttribute("emailPDFPassword", emailPDFPassword);
        request.setAttribute("emailPDFPasswordClue", emailPDFPasswordClue);
        request.setAttribute("demographicId", String.valueOf(demographicNo));
    }

    /**
     * Shows an error on the compose page, which alerts the message and closes.
     *
     * <p>The page puts the message into the page without encoding it, so it must be fixed text written
     * for staff, never document or patient content. The page turns each literal \n into a line break.</p>
     *
     * @param message String the fixed message to show
     * @return String the Struts2 result name "compose"
     */
    private String composePageError(String message) {
        request.setAttribute("isEmailError", true);
        request.setAttribute("emailErrorMessage", message);
        return "compose";
    }

    /**
     * Shows an eForm email error on the eForm page, which the eForm email came from.
     *
     * This method is called when eForm email composition preparation fails, typically due to PDF generation
     * errors for attachments. It sets the error message as a request attribute for display on the
     * eForm page. Errors that belong on the compose page itself use {@link #composePageError(String)}.
     *
     * Common Error Scenarios:
     * <ul>
     *   <li>PDF generation failure for eForms, documents, or forms</li>
     *   <li>Missing or inaccessible attachment files</li>
     *   <li>File I/O errors during attachment preparation</li>
     *   <li>Encryption errors for PDF password protection</li>
     * </ul>
     *
     * @param request HttpServletRequest the HTTP servlet request to store the error message
     * @param errorMessage String the error message to display to the user, typically includes
     *                     the specific exception message from PDFGenerationException
     * @return String the Struts2 result name "eFormError" which maps to the error display page
     * @see ca.openosp.openo.utility.PDFGenerationException
     */
    private String eFormPageError(HttpServletRequest request, String errorMessage) {
        request.setAttribute("errorMessage", errorMessage);
        return "eFormError";
    }
}
