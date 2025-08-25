//CHECKSTYLE:OFF
package org.oscarehr.documentManager;

import org.oscarehr.common.model.EFormData;
import org.oscarehr.documentManager.data.AttachmentLabResultData;
import org.oscarehr.common.model.enumerator.DocumentType;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.PDFGenerationException;

import oscar.oscarEncounter.data.EctFormData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.util.*;

public interface DocumentAttachmentManager {

    List<String> getConsultAttachments(LoggedInInfo loggedInInfo, Integer requestId, DocumentType documentType, Integer demographicNo);

    List<String> getEFormAttachments(LoggedInInfo loggedInInfo, Integer fdid, DocumentType documentType, Integer demographicNo);

    List<EctFormData.PatientForm> getFormsAttachedToEForms(LoggedInInfo loggedInInfo, Integer fdid, DocumentType documentType, Integer demographicNo);

    /**
     * This method is responsible for lab version sorting and is intended for use in the attachment window (attachDocument.jsp).
     * In other parts of the application, developers should utilize CommonLabResultData.populateLabResultsData() to access all available lab data.
     */
    List<AttachmentLabResultData> getAllLabsSortedByVersions(LoggedInInfo loggedInInfo, String demographicNo);

    /**
     * This method is intended for use in the attachment window (attachDocument.jsp) and is designed to retrieve a list of eForms except one.
     * In other parts of the application, developers are encouraged to use EFormUtil.listPatientEformsCurrent() to access all available eForms.
     * The reason for this function is to ensure a user cannot attach an eForm to itself.
     */
    List<EFormData> getAllEFormsExpectFdid(LoggedInInfo loggedInInfo, Integer demographicNo, Integer fdid);

    void attachToConsult(LoggedInInfo loggedInInfo, DocumentType documentType, String[] attachments, String providerNo, Integer requestId, Integer demographicNo);

    /*
     * @param editOnOcean When editOnOcean is set to false, it signifies a normal consult request, performing just attach or detach operations on the consult request form.
     * When editOnOcean is set to true, it signifies that the attach or detach operation is being performed on a consult request created by OceanMD.
     * In this case, it will do two things:
     * 1. Attach or detach attachments from the consult request.
     * 2. Add those new attachments to the 'EreferAttachment' table, so Oscar can sent those attachment to OceanMD.
     * By doing this, the user will not have to manually upload new attachments to e-refer. They will be automatically fetched.
     */
    void attachToConsult(LoggedInInfo loggedInInfo, DocumentType documentType, String[] attachments, String providerNo, Integer requestId, Integer demographicNo, Boolean editOnOcean);

    void attachToEForm(LoggedInInfo loggedInInfo, DocumentType documentType, String[] attachments, String providerNo, Integer fdid, Integer demographicNo);

    Path concatPDF(ArrayList<Object> pdfDocumentList) throws PDFGenerationException;

    Path concatPDF(List<Path> pdfDocuments) throws PDFGenerationException;

    Path renderDocument(HttpServletRequest request, HttpServletResponse response, DocumentType documentType) throws PDFGenerationException;

    /**
     * This renderDocument method is written to render EForms, Docs, HRMs and Labs.
     *
     * @param loggedInInfo The LoggedInInfo object.
     * @param documentType The type of the document to be rendered.
     * @param documentId   The documentId integer.
     * @return The Path to the rendered document.
     */
    Path renderDocument(LoggedInInfo loggedInInfo, DocumentType documentType, Integer documentId) throws PDFGenerationException;

    Path renderConsultationFormWithAttachments(HttpServletRequest request, HttpServletResponse response) throws PDFGenerationException;

    Path renderEFormWithAttachments(HttpServletRequest request, HttpServletResponse response) throws PDFGenerationException;

    Integer saveEFormAsEDoc(HttpServletRequest request, HttpServletResponse response) throws PDFGenerationException;

    String convertPDFToBase64(Path renderedDocument) throws PDFGenerationException;

    void flattenPDFFormFields(Path pdfPath) throws PDFGenerationException;
}

	
