//CHECKSTYLE:OFF
package ca.openosp.openo.documentManager.actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ca.openosp.openo.eform.EFormUtil;
import ca.openosp.openo.encounter.data.EctFormData;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.Logger;
import ca.openosp.OscarProperties;
import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.commn.model.enumerator.DocumentType;
import ca.openosp.openo.documentManager.DocumentAttachmentManager;
import ca.openosp.openo.documentManager.EDoc;
import ca.openosp.openo.documentManager.EDocUtil;
import ca.openosp.openo.documentManager.data.AttachmentLabResultData;
import ca.openosp.openo.hospitalReportManager.HRMUtil;
import ca.openosp.openo.managers.FormsManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PathValidationUtils;
import ca.openosp.openo.utility.PDFGenerationException;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.util.StringUtils;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for previewing and rendering medical documents as PDFs in the OpenO EMR system.
 *
 * This action handles the preview and rendering of various healthcare document types including
 * electronic documents (EDocs), electronic forms (EForms), hospital report manager documents (HRM),
 * laboratory results, encounter forms, and consultation documents. It provides secure PDF generation
 * and delivery for clinical documentation while enforcing path traversal protection to maintain
 * PHI (Patient Health Information) security.
 *
 * The action supports method-based routing via the "method" request parameter to handle different
 * document rendering operations and document retrieval workflows.
 *
 * @since 2026-01-24
 * @see DocumentAttachmentManager
 * @see DocumentType
 * @see PathValidationUtils
 */
public class DocumentPreview2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final Logger logger = MiscUtils.getLogger();
    private final DocumentAttachmentManager documentAttachmentManager = SpringUtils.getBean(DocumentAttachmentManager.class);
    private final FormsManager formsManager = SpringUtils.getBean(FormsManager.class);
    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Main execution entry point for the DocumentPreview2Action.
     *
     * Routes requests to appropriate document handling methods based on the "method" request parameter.
     * Supports the following methods:
     * - fetchEFormDocuments: Retrieves electronic forms for document selection
     * - renderEDocPDF: Renders electronic documents as PDF
     * - renderEFormPDF: Renders electronic forms as PDF
     * - renderHrmPDF: Renders hospital report manager documents as PDF
     * - renderLabPDF: Renders laboratory results as PDF
     * - renderFormPDF: Renders encounter forms as PDF
     * - renderPDF: Renders arbitrary PDF files with security validation
     * - fetchConsultDocuments: Retrieves consultation-related documents (default)
     *
     * @return String result name for Struts2 result mapping, or null for direct response rendering
     */
    public String execute() {
        String method = request.getParameter("method");

        if (method != null) {
            if (method.equalsIgnoreCase("fetchEFormDocuments"))
                return fetchEFormDocuments();
            else if (method.equalsIgnoreCase("renderEDocPDF")) {
                renderEDocPDF();
                return null;
            }
            else if (method.equalsIgnoreCase("renderEFormPDF")) {
                renderEFormPDF();
                return null;
            }
            else if (method.equalsIgnoreCase("renderHrmPDF")) {
                renderHrmPDF();
                return null;
            }
            else if (method.equalsIgnoreCase("renderLabPDF")) {
                renderLabPDF();
                return null;
            }
            else if (method.equalsIgnoreCase("renderFormPDF")) {
                renderFormPDF();
                return null;
            }
            else if (method.equalsIgnoreCase("renderPDF")) {
                renderPDF();
                return null;
            }
            else if (method.equalsIgnoreCase("fetchConsultDocuments"))
                return fetchConsultDocuments();
        }

        return fetchConsultDocuments();
    }

    /**
     * Renders an electronic document (EDoc) as a PDF and returns it as base64-encoded JSON.
     *
     * Retrieves the specified EDoc by ID and generates a PDF representation using the
     * DocumentAttachmentManager. The resulting PDF is converted to base64 and returned
     * in a JSON response. This method writes directly to the HTTP response and returns
     * null to prevent additional view rendering.
     *
     * Expected request parameters:
     * - eDocId: String the unique identifier of the electronic document to render
     *
     * Response format: JSON object with "base64Data" field containing the PDF data,
     * or "errorMessage" field if PDF generation fails.
     */
    public void renderEDocPDF() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String eDocId = request.getParameter("eDocId");
        try {
            Path docPDFPath = documentAttachmentManager.renderDocument(loggedInInfo, DocumentType.DOC, Integer.parseInt(eDocId));
            generateResponse(response, docPDFPath);
        } catch (PDFGenerationException e) {
            logger.error("Error occured while rendering eDoc. " + e.getMessage(), e);
            generateResponse(response, e.getMessage());
        }
    }

    /**
     * Renders an electronic form (EForm) as a PDF and returns it as base64-encoded JSON.
     *
     * Retrieves the specified EForm by ID and generates a PDF representation using the
     * DocumentAttachmentManager. Electronic forms are structured clinical data entry forms
     * used throughout the OpenO EMR system. The resulting PDF is converted to base64 and
     * returned in a JSON response.
     *
     * Expected request parameters:
     * - eFormId: String the unique identifier of the electronic form to render
     *
     * Response format: JSON object with "base64Data" field containing the PDF data,
     * or "errorMessage" field if PDF generation fails.
     */
    public void renderEFormPDF() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String eFormId = request.getParameter("eFormId");
        try {
            Path eFormPDFPath = documentAttachmentManager.renderDocument(loggedInInfo, DocumentType.EFORM, Integer.parseInt(eFormId));
            generateResponse(response, eFormPDFPath);
        } catch (PDFGenerationException e) {
            logger.error("Error occured while rendering eForm. " + e.getMessage(), e);
            generateResponse(response, e.getMessage());
        }
    }

    /**
     * Renders a Hospital Report Manager (HRM) document as a PDF and returns it as base64-encoded JSON.
     *
     * Retrieves the specified HRM document by ID and generates a PDF representation. HRM documents
     * contain reports from hospitals and external healthcare facilities, typically including lab
     * results, diagnostic imaging reports, and consultation notes from specialists. The resulting
     * PDF is converted to base64 and returned in a JSON response.
     *
     * Expected request parameters:
     * - hrmId: String the unique identifier of the HRM document to render
     *
     * Response format: JSON object with "base64Data" field containing the PDF data,
     * or "errorMessage" field if PDF generation fails.
     */
    public void renderHrmPDF() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String hrmId = request.getParameter("hrmId");
        try {
            Path hrmPDFPath = documentAttachmentManager.renderDocument(loggedInInfo, DocumentType.HRM, Integer.parseInt(hrmId));
            generateResponse(response, hrmPDFPath);
        } catch (PDFGenerationException e) {
            logger.error("Error occured while rendering HRM. " + e.getMessage(), e);
            generateResponse(response, e.getMessage());
        }
    }

    /**
     * Renders a laboratory result document as a PDF and returns it as base64-encoded JSON.
     *
     * Retrieves the specified lab result by segment ID and generates a PDF representation.
     * Laboratory results include HL7-formatted lab reports from provincial lab systems such
     * as OLIS (Ontario Laboratory Information System) and other integrated laboratory
     * information systems. The resulting PDF is converted to base64 and returned in a JSON response.
     *
     * Expected request parameters:
     * - segmentId: String the unique segment identifier of the laboratory result to render
     *
     * Response format: JSON object with "base64Data" field containing the PDF data,
     * or "errorMessage" field if PDF generation fails.
     */
    public void renderLabPDF() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String segmentID = request.getParameter("segmentId");
        try {
            Path labPDFPath = documentAttachmentManager.renderDocument(loggedInInfo, DocumentType.LAB, Integer.parseInt(segmentID));
            generateResponse(response, labPDFPath);
        } catch (PDFGenerationException e) {
            logger.error("Error occured while rendering Lab. " + e.getMessage(), e);
            generateResponse(response, e.getMessage());
        }
    }

    /**
     * Renders an encounter form as a PDF and returns it as base64-encoded JSON.
     *
     * Retrieves and generates a PDF representation of an encounter form (classic form data).
     * Encounter forms include various clinical assessment forms such as Rourke growth charts,
     * BCAR (British Columbia Antenatal Record), mental health assessments, and other
     * province-specific medical forms. The resulting PDF is converted to base64 and returned
     * in a JSON response.
     *
     * Response format: JSON object with "base64Data" field containing the PDF data,
     * or "errorMessage" field if PDF generation fails.
     */
    public void renderFormPDF() {
        try {
            Path formPDFPath = documentAttachmentManager.renderDocument(request, response, DocumentType.FORM);
            generateResponse(response, formPDFPath);
        } catch (PDFGenerationException e) {
            logger.error("Error occured while rendering Form. " + e.getMessage(), e);
            generateResponse(response, e.getMessage());
        }
    }

    /**
     * Renders a PDF file from a validated file path and streams it directly to the HTTP response.
     *
     * This method performs comprehensive security validation to prevent path traversal attacks
     * before serving PDF files. It validates that the requested file path exists within allowed
     * directories (DOCUMENT_DIR, TMP_DIR, eform_image, or system temp directory) using
     * PathValidationUtils. Only files that pass canonical path validation and exist as regular
     * files are served. This method is critical for maintaining PHI security and preventing
     * unauthorized file access.
     *
     * Expected request parameters:
     * - pdfPath: String the file system path to the PDF file to render
     *
     * Security measures:
     * - Validates path is not empty
     * - Resolves canonical path to detect traversal attempts
     * - Validates path is within allowed directories using PathValidationUtils
     * - Verifies file exists and is a regular file
     * - Sets appropriate HTTP status codes (400 for bad requests, 403 for forbidden paths,
     *   404 for missing files, 500 for server errors)
     *
     * Response: Streams PDF content directly with "application/pdf" content type, or sets
     * appropriate HTTP error status code if validation fails.
     */
    public void renderPDF() {
        String pdfPathString = StringUtils.isNullOrEmpty(request.getParameter("pdfPath")) ? "" : request.getParameter("pdfPath");
        
        if (pdfPathString.isEmpty()) {
            logger.error("Empty PDF path provided");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        // Validate the PDF path to prevent path traversal attacks
        Path pdfPath = Paths.get(pdfPathString);
        
        try {
            // Get the canonical path to resolve any path traversal attempts
            Path canonicalPdfPath = pdfPath.toRealPath();
            
            // Define allowed directories based on OSCAR configuration
            String[] allowedBasePaths = {
                OscarProperties.getInstance().getProperty("DOCUMENT_DIR", "/var/lib/OscarDocument/"),
                OscarProperties.getInstance().getProperty("TMP_DIR", "/tmp/"),
                OscarProperties.getInstance().getProperty("eform_image", "/var/lib/OscarDocument/eform/images/"),
                System.getProperty("java.io.tmpdir")
            };

            boolean isValidPath = false;
            for (String basePath : allowedBasePaths) {
                if (basePath != null && !basePath.isEmpty()) {
                    java.io.File baseDir = new java.io.File(basePath);
                    if (baseDir.exists()) {
                        try {
                            PathValidationUtils.validateExistingPath(canonicalPdfPath.toFile(), baseDir);
                            isValidPath = true;
                            break;
                        } catch (SecurityException e) {
                            // File not in this directory, try next
                        }
                    }
                }
            }
            
            if (!isValidPath) {
                logger.error("Access denied: Path traversal attempt detected for path: " + pdfPathString);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // Additional check: ensure the file exists and is a regular file
            if (!Files.exists(canonicalPdfPath) || !Files.isRegularFile(canonicalPdfPath)) {
                logger.error("PDF file not found or is not a regular file: " + pdfPathString);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Serve the validated PDF file
            response.setContentType("application/pdf");
            try (InputStream inputStream = Files.newInputStream(canonicalPdfPath);
                 BufferedInputStream bfis = new BufferedInputStream(inputStream);
                 ServletOutputStream outs = response.getOutputStream()) {

                int data;
                while ((data = bfis.read()) != -1) {
                    outs.write(data);
                }

                outs.flush();
            }
        } catch (IOException e) {
            logger.error("Error processing PDF file: " + pdfPathString, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Fetches all consultation-related documents for a specified patient.
     *
     * Retrieves comprehensive medical documentation for consultation workflows including
     * electronic documents, hospital reports, laboratory results sorted by versions,
     * encounter forms, and current electronic forms. The documents are populated as
     * request attributes for rendering in the consultation document selection interface.
     *
     * Expected request parameters:
     * - demographicNo: String the patient's demographic number (defaults to "0" if not provided)
     * - requestId: String the consultation request ID (optional; enables attached-state rendering)
     *
     * Request attributes set:
     * - allDocuments: List&lt;EDoc&gt; all electronic documents for the patient
     * - providerPrivateDocs: List&lt;EDoc&gt; the current provider's private eDocs (plus any foreign
     *   cross-provider private docs attached to this consult)
     * - providerPublicDocs: List&lt;EDoc&gt; all public provider eDocs
     * - allHRMDocuments: ArrayList&lt;HashMap&lt;String,? extends Object&gt;&gt; all HRM documents
     * - allLabsSortedByVersions: List&lt;AttachmentLabResultData&gt; lab results sorted by versions
     * - allForms: List&lt;EctFormData.PatientForm&gt; all encounter forms
     * - allEForms: List&lt;EFormData&gt; all current electronic forms
     * - attachedDocumentIds: Set&lt;String&gt; doc IDs already attached to this consult
     * - foreignPrivateDocIds: Set&lt;String&gt; attached private docs not owned by the current provider
     *
     * @return String "fetchDocuments" result name for Struts2 result mapping
     */
    public String fetchConsultDocuments() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_edoc", "r", null)) {
            throw new SecurityException("missing required security object (_edoc)");
        }

        String demographicNo = positiveIntParamOrDefault(request.getParameter("demographicNo"), "0");
        String requestId = positiveIntParamOrNull(request.getParameter("requestId"));

        populateCommonDocs(loggedInInfo, demographicNo);
		List<EFormData> allEForms = EFormUtil.listPatientEformsCurrent(Integer.valueOf(demographicNo), true);
        request.setAttribute("allEForms", allEForms);

        populateAttachedContextForConsult(loggedInInfo, demographicNo, requestId);

        return "fetchDocuments";
    }

    /**
     * Fetches electronic form documents for a specified patient, excluding a specific form.
     *
     * Retrieves comprehensive medical documentation similar to fetchConsultDocuments, but
     * filters out a specific electronic form by form data ID (fdid). This is typically used
     * when attaching documents to an existing eForm to prevent self-reference. The documents
     * are populated as request attributes for rendering in the document selection interface.
     *
     * Expected request parameters:
     * - demographicNo: String the patient's demographic number (defaults to "0" if not provided)
     * - fdid: String the form data ID to exclude from the eForm list (defaults to "0" if not provided)
     *
     * Request attributes set:
     * - allDocuments: List&lt;EDoc&gt; all electronic documents for the patient
     * - providerPrivateDocs: List&lt;EDoc&gt; the current provider's private eDocs (plus any foreign
     *   cross-provider private docs attached to this eForm)
     * - providerPublicDocs: List&lt;EDoc&gt; all public provider eDocs
     * - allHRMDocuments: ArrayList&lt;HashMap&lt;String,? extends Object&gt;&gt; all HRM documents
     * - allLabsSortedByVersions: List&lt;AttachmentLabResultData&gt; lab results sorted by versions
     * - allForms: List&lt;EctFormData.PatientForm&gt; all encounter forms
     * - allEForms: List&lt;EFormData&gt; all electronic forms excluding the specified fdid
     * - attachedDocumentIds: Set&lt;String&gt; doc IDs already attached to this eForm
     * - foreignPrivateDocIds: Set&lt;String&gt; attached private docs not owned by the current provider
     *
     * @return String "fetchDocuments" result name for Struts2 result mapping
     */
    public String fetchEFormDocuments() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_edoc", "r", null)) {
            throw new SecurityException("missing required security object (_edoc)");
        }

        String demographicNo = positiveIntParamOrDefault(request.getParameter("demographicNo"), "0");
        String rawFdid = request.getParameter("fdid");
        String fdidForEformList = positiveIntParamOrDefault(rawFdid, "0");
        String fdidForAttached = positiveIntParamOrNull(rawFdid);

        populateCommonDocs(loggedInInfo, demographicNo);
		List<EFormData> allEForms = documentAttachmentManager.getAllEFormsExpectFdid(loggedInInfo, Integer.parseInt(demographicNo), Integer.parseInt(fdidForEformList));
		request.setAttribute("allEForms", allEForms);

        populateAttachedContextForEForm(loggedInInfo, demographicNo, fdidForAttached);

        return "fetchDocuments";
    }

    /**
     * Generates a JSON response containing a base64-encoded PDF document.
     *
     * Converts the PDF file at the specified path to base64 encoding and wraps it in a JSON
     * object for transmission to the client. This method is used by the various renderXXXPDF
     * methods to return PDF data in a format suitable for JavaScript-based document viewers.
     *
     * @param response HttpServletResponse the HTTP response object to write to
     * @param pdfPath Path the file system path to the PDF file to encode
     * @throws PDFGenerationException if an error occurs during base64 conversion or writing the response
     */
    private void generateResponse(HttpServletResponse response, Path pdfPath) throws PDFGenerationException {
        ObjectNode json = objectMapper.createObjectNode();
        String base64Data = documentAttachmentManager.convertPDFToBase64(pdfPath);
        json.put("base64Data", base64Data);
        response.setContentType("text/javascript");
        try {
            response.getWriter().write(json.toString());
        } catch (IOException e) {
            throw new PDFGenerationException("An error occurred while writing JSON response to the output stream", e);
        }
    }

    /**
     * Generates a JSON error response for PDF generation failures.
     *
     * Creates a JSON object containing the error message and writes it to the HTTP response.
     * This method provides consistent error reporting for PDF generation failures across
     * all document rendering methods.
     *
     * @param response HttpServletResponse the HTTP response object to write to
     * @param errorMessage String the error message describing the PDF generation failure
     */
    private void generateResponse(HttpServletResponse response, String errorMessage) {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("errorMessage", errorMessage);
        response.setContentType("text/javascript");
        try {
            response.getWriter().write(json.toString());
        } catch (IOException e) {
            logger.error("An error occurred while writing JSON response to the output stream", e);
        }
    }

    /** @see #mergeAttachedContext */
    private void populateAttachedContextForConsult(LoggedInInfo loggedInInfo, String demographicNo, String requestId) {
        List<EDoc> attachedDocs = (requestId != null)
                ? EDocUtil.listDocs(loggedInInfo, demographicNo, requestId, EDocUtil.ATTACHED)
                : null;
        mergeAttachedContext(loggedInInfo, attachedDocs);
    }

    /** @see #mergeAttachedContext */
    private void populateAttachedContextForEForm(LoggedInInfo loggedInInfo, String demographicNo, String fdid) {
        List<EDoc> attachedDocs = (fdid != null)
                ? EDocUtil.listDocsAttachedToEForm(loggedInInfo, demographicNo, fdid, EDocUtil.ATTACHED)
                : null;
        mergeAttachedContext(loggedInInfo, attachedDocs);
    }

    /** Returns {@code value} if it parses as a positive integer, else null. Treats "null"/empty/non-numeric as absent. */
    private static String positiveIntParamOrNull(String value) {
        if (StringUtils.isNullOrEmpty(value)) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) return null;
        try {
            return Integer.parseInt(trimmed) > 0 ? trimmed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Like {@link #positiveIntParamOrNull} but returns {@code defaultValue} instead of null. */
    private static String positiveIntParamOrDefault(String value, String defaultValue) {
        String parsed = positiveIntParamOrNull(value);
        return parsed != null ? parsed : defaultValue;
    }

    /**
     * Sets {@code attachedDocumentIds} and {@code foreignPrivateDocIds} request
     * attributes, and merges deleted/cross-provider docs into the existing
     * {@code allDocuments} / {@code providerPrivateDocs} / {@code providerPublicDocs}
     * lists so the JSP can render them in the right section with the right markers.
     *
     * @since 2026-04-20
     */
    @SuppressWarnings("unchecked")
    private void mergeAttachedContext(LoggedInInfo loggedInInfo, List<EDoc> attachedDocs) {
        Set<String> attachedDocumentIds = new HashSet<>();
        Set<String> foreignPrivateDocIds = new HashSet<>();
        request.setAttribute("attachedDocumentIds", attachedDocumentIds);
        request.setAttribute("foreignPrivateDocIds", foreignPrivateDocIds);

        if (attachedDocs == null || attachedDocs.isEmpty()) {
            logger.debug("mergeAttachedContext: no attached docs; nothing to merge");
            return;
        }

        List<EDoc> allDocuments = (List<EDoc>) request.getAttribute("allDocuments");
        List<EDoc> providerPrivateDocs = (List<EDoc>) request.getAttribute("providerPrivateDocs");
        List<EDoc> providerPublicDocs = (List<EDoc>) request.getAttribute("providerPublicDocs");

        if (allDocuments == null || providerPrivateDocs == null || providerPublicDocs == null) {
            logger.warn("mergeAttachedContext: expected document list attributes missing; skipping merge");
            return;
        }

        String currentProviderNo = loggedInInfo.getLoggedInProviderNo();

        for (EDoc attachedDoc : attachedDocs) {
            attachedDocumentIds.add(attachedDoc.getDocId());
            mergeSingleAttachedDoc(
                    attachedDoc,
                    currentProviderNo,
                    allDocuments,
                    providerPrivateDocs,
                    providerPublicDocs,
                    foreignPrivateDocIds);
        }
    }

    /**
     * Classifies one attached doc and appends it to the appropriate section list.
     *
     * @since 2026-04-21
     */
    static void mergeSingleAttachedDoc(
            EDoc attachedDoc,
            String currentProviderNo,
            List<EDoc> allDocuments,
            List<EDoc> providerPrivateDocs,
            List<EDoc> providerPublicDocs,
            Set<String> foreignPrivateDocIds) {

        boolean isDeleted = attachedDoc.getStatus() == 'D';
        boolean isProvider = isProviderModule(attachedDoc.getModule());
        boolean isPublic = "1".equals(attachedDoc.getDocPublic());
        boolean ownedByCurrent = isOwnedByCurrentProvider(attachedDoc, currentProviderNo);

        // Patient doc: only deleted ones need re-injecting.
        if (!isProvider) {
            if (isDeleted) allDocuments.add(attachedDoc);
            return;
        }
        // Public provider doc: only deleted ones need re-injecting.
        if (isPublic) {
            if (isDeleted) providerPublicDocs.add(attachedDoc);
            return;
        }
        // Private provider doc: skip active-own (already listed); merge everything else.
        if (!isDeleted && ownedByCurrent) return;
        providerPrivateDocs.add(attachedDoc);
        if (!ownedByCurrent) foreignPrivateDocIds.add(attachedDoc.getDocId());
    }

    static boolean isOwnedByCurrentProvider(EDoc doc, String currentProviderNo) {
        return currentProviderNo != null && currentProviderNo.equals(doc.getModuleId());
    }

    /**
     * Local copy of {@link EDocUtil#isProviderModule} to avoid triggering EDocUtil's
     * class-load (and its many SpringUtils-bean static initializers) from this
     * classifier. Keeps {@link #mergeSingleAttachedDoc} unit-testable without Spring.
     */
    static boolean isProviderModule(String module) {
        return "provider".equalsIgnoreCase(module) || "providers".equalsIgnoreCase(module);
    }

    /**
     * Populate common documents like EDocs, Labs, Forms, HRM documents
     * @param loggedInInfo Information about the logged-in user
     * @param demographicNo Demographic number of the patient
     */
    private void populateCommonDocs(LoggedInInfo loggedInInfo, String demographicNo) {
        List<EDoc> allDocuments = new ArrayList<>(EDocUtil.listDocs(loggedInInfo, "demographic", demographicNo, null, EDocUtil.PRIVATE, EDocUtil.EDocSort.OBSERVATIONDATE));
        List<EDoc> providerPrivateDocs = new ArrayList<>(EDocUtil.getProviderPrivateDocs(loggedInInfo));
        List<EDoc> providerPublicDocs = new ArrayList<>(EDocUtil.getProviderPublicDocs(loggedInInfo));
        ArrayList<HashMap<String,? extends Object>> allHRMDocuments = HRMUtil.listHRMDocuments(loggedInInfo, "report_date", false, demographicNo,false);
        List<AttachmentLabResultData> allLabsSortedByVersions = documentAttachmentManager.getAllLabsSortedByVersions(loggedInInfo, demographicNo);
        List<EctFormData.PatientForm> allForms = formsManager.getEncounterFormsbyDemographicNumber(loggedInInfo, Integer.parseInt(demographicNo), false, true);

        request.setAttribute("allDocuments", allDocuments);
        request.setAttribute("providerPrivateDocs", providerPrivateDocs);
        request.setAttribute("providerPublicDocs", providerPublicDocs);
        request.setAttribute("allHRMDocuments", allHRMDocuments);
		request.setAttribute("allLabsSortedByVersions", allLabsSortedByVersions);
		request.setAttribute("allForms", allForms);
    }
}
