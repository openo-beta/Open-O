//CHECKSTYLE:OFF
package ca.openosp.openo.email.action;

import java.io.IOException;
import org.owasp.encoder.Encode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.model.EmailLog;
import ca.openosp.openo.commn.model.EmailLog.EmailStatus;
import ca.openosp.openo.email.core.EmailAttachmentSettings;
import ca.openosp.openo.email.core.EmailComposeStore;
import ca.openosp.openo.email.core.EmailData;
import ca.openosp.openo.managers.EformDataManager;
import ca.openosp.openo.managers.EmailManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action controller for handling email sending functionality within the OpenO EMR system.
 *
 * <p>This action supports multiple email sending workflows including:</p>
 * <ul>
 *   <li>Sending emails directly with healthcare data and attachments</li>
 *   <li>Sending electronic forms (EForms) via email with optional deletion after send</li>
 *   <li>Handling email encryption and password protection for PHI compliance</li>
 *   <li>Sending only the attachments stored under the compose page's key</li>
 *   <li>Canceling email operations and redirecting to source contexts</li>
 * </ul>
 *
 * <p>The action integrates with the EmailManager service for core email functionality and
 * EformDataManager for electronic form handling. All email operations are logged via
 * EmailLog entities for audit trail and compliance purposes.</p>
 *
 * <p>This action follows the 2Action pattern for Struts2 migration, using method-based
 * routing via the "method" request parameter to handle different email workflows within
 * a single action class.</p>
 *
 * <p><strong>Security Considerations:</strong> This action handles Protected Health Information (PHI)
 * and supports encryption for both email bodies and attachments. All operations are performed
 * within the context of a logged-in provider using LoggedInInfo.</p>
 *
 * @since 2026-01-24
 * @see ca.openosp.openo.managers.EmailManager
 * @see ca.openosp.openo.managers.EformDataManager
 * @see ca.openosp.openo.commn.model.EmailLog
 * @see ca.openosp.openo.email.core.EmailData
 */
public class EmailSend2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final Logger logger = MiscUtils.getLogger();
    private EmailManager emailManager = SpringUtils.getBean(EmailManager.class);
    private EformDataManager eformDataManager = SpringUtils.getBean(EformDataManager.class);

    /**
     * Main execution method that routes to specific email handling methods based on the "method" request parameter.
     *
     * <p>This method implements method-based routing for the following email workflows:</p>
     * <ul>
     *   <li><strong>sendDirectEmail</strong> - Sends email directly without EForm context</li>
     *   <li><strong>cancel</strong> - Cancels email operation and redirects to source</li>
     *   <li><strong>default</strong> - Sends email with EForm context (if no method parameter specified)</li>
     * </ul>
     *
     * @return String Struts2 result identifier - "success" for successful email operations,
     *         or transaction type name for cancel operations
     */
    public String execute () {
        if ("sendDirectEmail".equals(request.getParameter("method"))) {
            return sendDirectEmail();
        } else if ("cancel".equals(request.getParameter("method"))) {
            return cancel();
        }
        return sendEFormEmail();
    }

    /**
     * Sends an email with electronic form (EForm) context and optionally deletes the EForm after successful send.
     *
     * <p>This method handles the complete workflow for emailing EForms including:</p>
     * <ul>
     *   <li>Taking the patient, eForm and attachments stored under the compose page's key; the email is not sent when the key is unknown or already used</li>
     *   <li>Processing email send operation via EmailManager</li>
     *   <li>Optionally deleting the source EForm if send is successful and deletion is requested</li>
     *   <li>Setting request attributes for success status, EForm opening preference, and email log</li>
     * </ul>
     *
     * <p>The EForm id and the choices to delete or reopen the EForm after sending come from the
     * entry stored under the compose key, not from the request.</p>
     *
     * @return String Struts2 SUCCESS result for rendering the email result page
     */
    public String sendEFormEmail() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        EmailComposeStore.Entry entry = takeComposeEntry();
        if (entry == null) {
            return refuseSend();
        }
        EmailLog emailLog = sendEntry(loggedInInfo, entry);

        boolean isEmailSuccessful = emailLog.getStatus() == EmailStatus.SUCCESS;
        request.setAttribute("isEmailSuccessful", isEmailSuccessful);
        // A key from a resend page reaches here with no eForm settings
        EmailAttachmentSettings settings = entry.settings();
        if (isEmailSuccessful && settings != null && settings.deleteEFormAfterEmail() && !entry.fdid().isEmpty()) {
            eformDataManager.removeEFormData(loggedInInfo, entry.fdid());
        }
        request.setAttribute("isOpenEForm", settings != null && settings.openAfterEmail());
        request.setAttribute("fdid", entry.fdid());
        request.setAttribute("emailLog", emailLog);
        return SUCCESS;
    }

    /**
     * Sends an email directly without electronic form (EForm) context.
     *
     * <p>This method provides a simplified email sending workflow for scenarios where
     * the email is not associated with an EForm, such as resending an old email. It handles:</p>
     * <ul>
     *   <li>Taking the patient and attachments stored under the compose page's key; the email is not sent when the key is unknown or already used</li>
     *   <li>Processing the email send operation via EmailManager</li>
     *   <li>Setting request attributes for success status and email log</li>
     * </ul>
     *
     * @return String Struts2 SUCCESS result for rendering the email result page
     */
    public String sendDirectEmail() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        EmailComposeStore.Entry entry = takeComposeEntry();
        if (entry == null) {
            return refuseSend();
        }
        EmailLog emailLog = sendEntry(loggedInInfo, entry);
        boolean isEmailSuccessful = emailLog.getStatus() == EmailStatus.SUCCESS;
        request.setAttribute("isEmailSuccessful", isEmailSuccessful);
        request.setAttribute("emailLog", emailLog);
        return SUCCESS;
    }

    /**
     * Cancels the email operation and redirects the user back to the appropriate source context.
     *
     * <p>This method handles the cancel workflow by:</p>
     * <ul>
     *   <li>Removing the entry stored under this compose page's key</li>
     *   <li>Reading the transaction type from the request</li>
     *   <li>For EFORM transactions: redirects to the EForm display page with original form data</li>
     * </ul>
     *
     * @return String Struts2 result identifier matching the transaction type name
     * @throws RuntimeException if IOException occurs during redirect for EFORM transactions
     */
    public String cancel() {
        EmailComposeStore.remove(request.getSession(), request.getParameter("composeKey"));
        EmailData emailData = new EmailData();
        emailData.setTransactionType(request.getParameter("transactionType"));
        String emailRedirect = emailData.getTransactionType().name();
        if (emailData.getTransactionType().equals(EmailLog.TransactionType.EFORM)) {
            try {
                response.sendRedirect(request.getContextPath() + "/eform/efmshowform_data.jsp?fdid="  + Encode.forUriComponent(request.getParameter("fdid")) + "&parentAjaxId=eforms");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return emailRedirect;
    }

    /**
     * Takes the entry stored under the compose page's key, so a second Send of the same page gets nothing.
     *
     * <p>Returns null when the key is missing, unknown, already sent or cancelled, or dropped at logout.
     * The email is then not sent, so it never goes out twice, with another window's attachments,
     * or silently without its attachments.</p>
     *
     * @return EmailComposeStore.Entry the patient, eForm id, email type and attachments to send, or null
     */
    private EmailComposeStore.Entry takeComposeEntry() {
        EmailComposeStore.Entry entry = EmailComposeStore.take(request.getSession(), request.getParameter("composeKey"));
        if (entry == null) {
            logger.warn("Email not sent: the compose page key is missing, unknown or already used");
        }
        return entry;
    }

    /**
     * Sends the entry's email and puts the entry back when it was not sent, so the page can send again.
     *
     * <p>The entry stays out of the store during the send, so a second Send of the page is refused meanwhile.</p>
     *
     * @param loggedInInfo LoggedInInfo the logged in user
     * @param entry EmailComposeStore.Entry the entry taken for this compose page
     * @return EmailLog the log of this send, with its status
     */
    private EmailLog sendEntry(LoggedInInfo loggedInInfo, EmailComposeStore.Entry entry) {
        EmailLog emailLog = null;
        try {
            emailLog = emailManager.sendEmail(loggedInInfo, prepareEmailFields(request, entry));
            return emailLog;
        } finally {
            if (emailLog == null || emailLog.getStatus() != EmailStatus.SUCCESS) {
                EmailComposeStore.restore(request.getSession(), request.getParameter("composeKey"), entry);
            }
        }
    }

    /**
     * Shows the "already used or expired" message on the compose page instead of sending the email.
     *
     * @return String Struts2 SUCCESS result; the compose page shows the message and closes
     */
    private String refuseSend() {
        request.setAttribute("isEmailError", true);
        request.setAttribute("emailErrorMessage", "This email page has already been used or has expired, so nothing was sent just now. If you already pressed Send, check Manage Emails or the patient's chart before sending again.");
        return SUCCESS;
    }

    /**
     * Prepares email data from the HTTP request parameters and the entry stored under the compose page's key.
     *
     * <p>Subject, body, recipients, encryption and chart options come from the request. The patient,
     * email type and attachments come only from the stored entry.</p>
     *
     * @param request HttpServletRequest containing email form parameters
     * @param entry EmailComposeStore.Entry the patient, email type and attachments of this compose page
     * @return EmailData populated data transfer object containing all email parameters
     *         ready for processing by EmailManager
     */
    private EmailData prepareEmailFields(HttpServletRequest request, EmailComposeStore.Entry entry) {
        String senderConfigId = request.getParameter("senderConfigId");
        String[] receiverEmails = request.getParameterValues("receiverEmailAddress");
        String subject = request.getParameter("subjectEmail");
        String body = request.getParameter("bodyEmail");
        String footer = request.getParameter("footerEmail");
        String encryptedMessage = request.getParameter("encryptedMessage");
        String password = request.getParameter("emailPDFPassword");
        String passwordClue = request.getParameter("emailPDFPasswordClue");
        String isEncrypted = request.getParameter("isEmailEncrypted");
        String isAttachmentEncrypted = request.getParameter("isEmailAttachmentEncrypted");
        String chartDisplayOption = request.getParameter("patientChartOption");
        String internalComment = request.getParameter("internalComment");
        String additionalParams = request.getParameter("additionalURLParams");

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();

        EmailData emailData = new EmailData();
        emailData.setSenderConfigId(senderConfigId);
        emailData.setRecipients(receiverEmails);
        emailData.setSubject(subject);
        emailData.setBody(body);
        emailData.setFooter(footer);
        emailData.setEncryptedMessage(encryptedMessage);
        emailData.setPassword(password);
        emailData.setPasswordClue(passwordClue);
        emailData.setIsEncrypted(isEncrypted);
        emailData.setIsAttachmentEncrypted(isAttachmentEncrypted);
        emailData.setChartDisplayOption(chartDisplayOption);
        emailData.setInternalComment(internalComment);
        emailData.setTransactionType(entry.transactionType());
        emailData.setDemographicNo(entry.demographicNo());
        emailData.setProviderNo(providerNo);
        emailData.setAdditionalParams(additionalParams);
        emailData.setAttachments(entry.copyOfAttachments());

        return emailData;
    }
}
