//CHECKSTYLE:OFF
/**
 * Copyright (c) 2015-2019. The Pharmacists Clinic, Faculty of Pharmaceutical Sciences, University of British Columbia. All Rights Reserved.
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
 * This software was written for the
 * The Pharmacists Clinic
 * Faculty of Pharmaceutical Sciences
 * University of British Columbia
 * Vancouver, British Columbia, Canada
 */

package ca.openosp.openo.fax.action;

import com.opensymphony.xwork2.ActionSupport;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import ca.openosp.openo.commn.model.FaxConfig;
import ca.openosp.openo.commn.model.FaxJob;
import ca.openosp.openo.commn.model.FaxJob.STATUS;
import ca.openosp.openo.documentManager.DocumentAttachmentManager;
import ca.openosp.openo.managers.FaxManager;
import ca.openosp.openo.managers.FaxManager.TransactionType;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PDFGenerationException;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.openo.form.JSONUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Struts2 action for handling medical document fax transmission in the OpenO EMR healthcare system.
 *
 * This action provides essential healthcare fax functionality including preparation, preview, and queuing
 * of medical documents for secure transmission. Fax communication remains critical in healthcare due to
 * HIPAA/PIPEDA compliance requirements, regulatory mandates from many healthcare institutions, and the
 * need for secure document transmission between healthcare providers.
 *
 * The fax system is designed to handle various medical documents including:
 * - Electronic forms (eforms) containing patient data and medical assessments
 * - Laboratory results and diagnostic reports
 * - Consultation requests and specialist referrals
 * - Prescription information and medication histories
 * - Medical imaging reports and diagnostic findings
 *
 * All fax transmissions maintain audit trails for regulatory compliance and include automatic
 * cover page generation with confidentiality statements to protect patient health information (PHI).
 *
 * @see ca.openosp.openo.managers.FaxManager
 * @see ca.openosp.openo.fax.core.FaxSender
 * @see ca.openosp.openo.fax.util.PdfCoverPageCreator
 * @since 2020-11-30
 */
public class Fax2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final Logger logger = MiscUtils.getLogger();
    private final FaxManager faxManager = SpringUtils.getBean(FaxManager.class);
    private final DocumentAttachmentManager documentAttachmentManager = SpringUtils.getBean(DocumentAttachmentManager.class);

    /**
     * Main execution method that routes fax requests to appropriate handlers based on the method parameter.
     *
     * This method supports medical document fax operations including queuing documents for transmission
     * and preparing PDF documents for fax review. The routing is based on healthcare workflow requirements
     * where documents must be reviewed before transmission to ensure patient data accuracy and compliance.
     *
     * @return String the Struts result name determining the next view or action
     */
    public String execute() {
        String method = request.getParameter("method");
        if ("queue".equals(method)) {
            return queue();
        } else if ("prepareFax".equals(method)) {
            return prepareFax();
        }
        return cancel();
    }

    /**
     * Cancels the current fax operation and performs cleanup of temporary files and resources.
     *
     * This method handles the cancellation workflow for different types of medical documents:
     * - CONSULTATION: Redirects back to the consultation request view
     * - EFORM: Returns to the electronic form display
     * - Other transactions: Returns to the appropriate transaction type view
     *
     * Cleanup includes flushing temporary fax files to prevent PHI from remaining in temporary storage,
     * which is critical for maintaining HIPAA/PIPEDA compliance in healthcare environments.
     *
     * @return String the Struts result name for redirection
     * @throws RuntimeException if redirect fails due to IO issues
     */
    @SuppressWarnings("unused")
    public String cancel() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String faxForward = transactionType;

        // Clean up temporary fax files to prevent PHI exposure
        faxManager.flush(loggedInInfo, faxFilePath);




        if (TransactionType.CONSULTATION.name().equalsIgnoreCase(transactionType)) {
            try {
                response.sendRedirect("/oscarEncounter/ViewRequest.do?de=" + demographicNo + "&requestId=" + transactionId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return NONE;
        } else if (TransactionType.EFORM.name().equalsIgnoreCase(transactionType)) {
            try {
                response.sendRedirect("/eform/efmshowform_data.jsp?fdid=" + transactionId + "&parentAjaxId=eforms");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return NONE;
        }

        return faxForward;
    }

    /**
     * Queues medical documents for batch fax transmission after user review and approval.
     *
     * This method creates and saves fax jobs for healthcare documents that have been reviewed
     * by clinical staff. The batch processing approach ensures efficient transmission of multiple
     * medical documents while maintaining audit trails required for healthcare compliance.
     *
     * The queuing process includes:
     * - Creating fax job records with transmission parameters
     * - Logging fax activities for regulatory audit requirements
     * - Validating document content and recipient information
     * - Setting appropriate status codes for tracking
     *
     * @return String "preview" result to display confirmation of queued fax jobs
     */
    @SuppressWarnings("unused")
    public String queue() {

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        TransactionType transactionType = TransactionType.valueOf(getTransactionType().toUpperCase());
        List<FaxJob> faxJobList = faxManager.createAndSaveFaxJob(loggedInInfo, new HashMap<>());

        boolean success = true;
        for (FaxJob faxJob : faxJobList) {
            faxManager.logFaxJob(loggedInInfo, faxJob, transactionType, transactionId);

            // Any single fax job error marks the entire batch as failed
            // This ensures healthcare staff are aware of any transmission issues
            if (STATUS.ERROR.equals(faxJob.getStatus())) {
                success = false;
            }
        }

        request.setAttribute("faxSuccessful", success);
        request.setAttribute("faxJobList", faxJobList);

        return "preview";
    }


    /**
     * Generates preview images or PDF views of medical documents before fax transmission.
     *
     * This method provides healthcare staff with the ability to review document content before
     * sending, which is critical for ensuring accuracy of patient health information (PHI)
     * and preventing transmission of incorrect medical data. The preview can be displayed as:
     * - PNG images for page-by-page review in web interfaces
     * - Full PDF documents for comprehensive review
     *
     * The preview functionality is essential in healthcare workflows where document accuracy
     * directly impacts patient care and regulatory compliance.
     *
     * @param faxFilePath String path to the document file for preview
     * @param pageNumber String specific page number to preview (optional)
     * @param showAs String display format ("image" for PNG, default for PDF)
     * @param jobId String existing fax job ID for retrieving stored documents
     */
    @SuppressWarnings("unused")
    public void getPreview() {

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String faxFilePath = request.getParameter("faxFilePath");
        String pageNumber = request.getParameter("pageNumber");
        String showAs = request.getParameter("showAs");
        Path outfile = null;
        int page = 1;
        String jobId = request.getParameter("jobId");
        FaxJob faxJob = null;

        if (jobId != null && !jobId.isEmpty()) {
            faxJob = faxManager.getFaxJob(loggedInInfo, Integer.parseInt(jobId));
        }

        if (faxJob != null) {
            faxFilePath = faxJob.getFile_name();
        }

        if (pageNumber != null && !pageNumber.isEmpty()) {
            page = Integer.parseInt(pageNumber);
        }

        // Different display modes for healthcare workflow:
        // - PDF view for eForm review before transmission
        // - Image view for page-by-page examination in fax management
        if (faxFilePath != null && !faxFilePath.isEmpty()) {
            if (showAs != null && showAs.equals("image")) {
                outfile = faxManager.getFaxPreviewImage(loggedInInfo, faxFilePath, page);
                response.setContentType("image/pnsg");
                response.setHeader("Content-Disposition", "attachment;filename=" + outfile.getFileName().toString());
            } else {
                outfile = Paths.get(faxFilePath);
                response.setContentType("application/pdf");
            }
        }

        if (outfile != null) {
            try (InputStream inputStream = Files.newInputStream(outfile);
                 BufferedInputStream bfis = new BufferedInputStream(inputStream);
                 ServletOutputStream outs = response.getOutputStream()) {

                int data;
                while ((data = bfis.read()) != -1) {
                    outs.write(data);
                }
                outs.flush();
            } catch (IOException e) {
                //log.error("Error", e);
            }
        }
    }

    /**
     * Prepares medical documents for fax transmission by generating PDF files and setting up preview.
     *
     * This method handles the critical preparation phase for healthcare document transmission,
     * including PDF generation from electronic forms (eforms) with attachments. The preparation
     * process ensures that complex medical documents containing patient data, assessments, and
     * attachments are properly formatted for secure fax transmission.
     *
     * The method performs several healthcare-specific operations:
     * - Validates that active fax accounts are configured
     * - Renders electronic forms with attached documents into consolidated PDFs
     * - Prepares document metadata for cover page generation
     * - Sets up recipient and sender information for transmission
     *
     * @return String "preview" for successful preparation, "eFormError" for PDF generation failures,
     *         or ERROR for other failures
     * @throws PDFGenerationException when electronic forms cannot be rendered to PDF
     */
    @SuppressWarnings("unused")
    public String prepareFax() {

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        /*
         * Fax recipient info carried forward.
         */
        TransactionType transactionType = TransactionType.valueOf(getTransactionType().toUpperCase());
        String actionForward = ERROR;
        Path pdfPath = null;
        List<FaxConfig> accounts = faxManager.getFaxGatewayAccounts(loggedInInfo);

        // Validate fax service availability before document preparation
        // Medical documents are temporarily stored as PDF for review
        if (!accounts.isEmpty()) {
            if (transactionType.equals(TransactionType.EFORM)) {
                request.setAttribute("fdid", String.valueOf(transactionId));
                request.setAttribute("demographicId", String.valueOf(demographicNo));

                try {
                    pdfPath = documentAttachmentManager.renderEFormWithAttachments(request, response);
                } catch (PDFGenerationException e) {
                    logger.error(e.getMessage(), e);
                    String errorMessage = "This eForm (and attachments, if applicable) cannot be faxed. \\n\\n" + e.getMessage();
                    request.setAttribute("errorMessage", errorMessage);
                    return "eFormError";
                }
            }
        } else {
            request.setAttribute("message", "No active fax accounts found.");
        }

        if (pdfPath != null) {
            List<Path> documents = new ArrayList<>();
            documents.add(pdfPath);
            request.setAttribute("accounts", accounts);
            request.setAttribute("demographicNo", demographicNo);
            request.setAttribute("documents", documents);
            request.setAttribute("transactionType", transactionType.name());
            request.setAttribute("transactionId", transactionId);
            request.setAttribute("faxFilePath", pdfPath);
            request.setAttribute("letterheadFax", letterheadFax);
            request.setAttribute("professionalSpecialistName", recipient);
            request.setAttribute("fax", recipientFaxNumber);
            actionForward = "preview";
        }

        return actionForward;
    }

    /**
     * Retrieves the total page count of a medical document for accurate fax transmission billing.
     *
     * Page count information is critical in healthcare fax operations for:
     * - Estimating transmission time and costs
     * - Validating complete document transmission
     * - Generating accurate cover page information
     * - Meeting regulatory requirements for document completeness tracking
     *
     * The method returns JSON response containing the page count for use in web interfaces
     * where healthcare staff need to confirm document completeness before transmission.
     *
     * @param jobId String the fax job ID to retrieve page count for
     * @return JSON response with jobId and pageCount fields
     */
    @SuppressWarnings("unused")
    public void getPageCount() {

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String jobId = request.getParameter("jobId");
        int pageCount = 0;

        if (jobId != null && !jobId.isEmpty()) {
            pageCount = faxManager.getPageCount(loggedInInfo, Integer.parseInt(jobId));
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jobId", jobId);
        jsonObject.put("pageCount", pageCount);

        JSONUtil.jsonResponse(response, jsonObject);
    }

    /** Path to the temporary fax file containing the medical document */
    private String faxFilePath;

    /** Unique identifier for the medical transaction (consultation, eform, etc.) */
    private Integer transactionId;

    /** Patient demographic number for associating fax with patient record */
    private Integer demographicNo;

    /** Type of medical transaction being faxed (CONSULTATION, EFORM, etc.) */
    private String transactionType;

    /** Name of the healthcare provider or facility receiving the fax */
    private String recipient;

    /** Fax number of the receiving healthcare provider */
    private String recipientFaxNumber;

    /** Letterhead fax number of the sending healthcare facility */
    private String letterheadFax;

    public String getFaxFilePath() {
        return faxFilePath;
    }

    public void setFaxFilePath(String faxFilePath) {
        this.faxFilePath = faxFilePath;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getDemographicNo() {
        return demographicNo;
    }

    public void setDemographicNo(Integer demographicNo) {
        this.demographicNo = demographicNo;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getRecipientFaxNumber() {
        return recipientFaxNumber;
    }

    public void setRecipientFaxNumber(String recipientFaxNumber) {
        this.recipientFaxNumber = recipientFaxNumber;
    }

    public String getLetterheadFax() {
        return letterheadFax;
    }

    public void setLetterheadFax(String letterheadFax) {
        this.letterheadFax = letterheadFax;
    }
}
