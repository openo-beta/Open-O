//CHECKSTYLE:OFF
package ca.openosp.openo.documentManager;

import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.documentManager.data.AttachmentLabResultData;
import ca.openosp.openo.commn.model.enumerator.DocumentType;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.PDFGenerationException;

import ca.openosp.openo.encounter.data.EctFormData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.util.*;

public interface DocumentAttachmentManager {

    public List<String> getConsultAttachments(LoggedInInfo loggedInInfo, Integer requestId, DocumentType documentType, Integer demographicNo);

    public List<String> getEFormAttachments(LoggedInInfo loggedInInfo, Integer fdid, DocumentType documentType, Integer demographicNo);

    public List<EctFormData.PatientForm> getFormsAttachedToEForms(LoggedInInfo loggedInInfo, Integer fdid, DocumentType documentType, Integer demographicNo);

    /**
     * This method is responsible for lab version sorting and is intended for use in the attachment window (attachDocument.jsp).
     * In other parts of the application, developers should utilize CommonLabResultData.populateLabResultsData() to access all available lab data.
     */
    public List<AttachmentLabResultData> getAllLabsSortedByVersions(LoggedInInfo loggedInInfo, String demographicNo);

    /**
     * This method is intended for use in the attachment window (attachDocument.jsp) and is designed to retrieve a list of eForms except one.
     * In other parts of the application, developers are encouraged to use EFormUtil.listPatientEformsCurrent() to access all available eForms.
     * The reason for this function is to ensure a user cannot attach an eForm to itself.
     */
    public List<EFormData> getAllEFormsExpectFdid(LoggedInInfo loggedInInfo, Integer demographicNo, Integer fdid);

    public void attachToConsult(LoggedInInfo loggedInInfo, DocumentType documentType, String[] attachments, String providerNo, Integer requestId, Integer demographicNo);

    /*
     * @param editOnOcean When editOnOcean is set to false, it signifies a normal consult request, performing just attach or detach operations on the consult request form.
     * When editOnOcean is set to true, it signifies that the attach or detach operation is being performed on a consult request created by OceanMD.
     * In this case, it will do two things:
     * 1. Attach or detach attachments from the consult request.
     * 2. Add those new attachments to the 'EreferAttachment' table, so Oscar can sent those attachment to OceanMD.
     * By doing this, the user will not have to manually upload new attachments to e-refer. They will be automatically fetched.
     */
    public void attachToConsult(LoggedInInfo loggedInInfo, DocumentType documentType, String[] attachments, String providerNo, Integer requestId, Integer demographicNo, Boolean editOnOcean);

    public void attachToEForm(LoggedInInfo loggedInInfo, DocumentType documentType, String[] attachments, String providerNo, Integer fdid, Integer demographicNo);

    public Path concatPDF(ArrayList<Object> pdfDocumentList) throws PDFGenerationException;

    public Path concatPDF(List<Path> pdfDocuments) throws PDFGenerationException;

    public Path renderDocument(HttpServletRequest request, HttpServletResponse response, DocumentType documentType) throws PDFGenerationException;

    /**
     * This renderDocument method is written to render EForms, Docs, HRMs and Labs.
     *
     * @param loggedInInfo The LoggedInInfo object.
     * @param documentType The type of the document to be rendered.
     * @param documentId   The documentId integer.
     * @return The Path to the rendered document.
     */
    public Path renderDocument(LoggedInInfo loggedInInfo, DocumentType documentType, Integer documentId) throws PDFGenerationException;

    public Path renderConsultationFormWithAttachments(HttpServletRequest request, HttpServletResponse response) throws PDFGenerationException;

    public Path renderEFormWithAttachments(HttpServletRequest request, HttpServletResponse response) throws PDFGenerationException;

    public Integer saveEFormAsEDoc(HttpServletRequest request, HttpServletResponse response) throws PDFGenerationException;

    public String convertPDFToBase64(Path renderedDocument) throws PDFGenerationException;

    public void flattenPDFFormFields(Path pdfPath) throws PDFGenerationException;

    /**
     * Validates that all documents in the array belong to the specified patient demographic.
     * Each entry is prefixed with a type letter (D, L, E, H) followed by the document ID
     * (e.g. "D42", "L7").
     *
     * @param loggedInInfo  The logged-in provider context
     * @param demographicNo The patient's demographic number
     * @param documents     Array of typed document ID strings
     * @return {@code true} if every document belongs to the patient; {@code false} otherwise
     */
    public boolean validateDocumentsBelongToPatient(LoggedInInfo loggedInInfo, Integer demographicNo, String[] documents);

    /**
     * Classifies the supplied attached docs into the three section lists
     * (patient documents, provider public eDocs, provider private eDocs) and
     * populates the two ID sets the view needs for pre-checking and foreign-owner
     * labelling. The three section lists are mutated in place — deleted docs and
     * foreign private docs are appended to the matching list so the view still
     * renders them. {@code allDocuments}, {@code providerPrivateDocs}, and
     * {@code providerPublicDocs} may be {@code null}: in that case only
     * {@code attachedDocumentIds} is populated (for pre-checking) and the
     * section merge is skipped.
     *
     * @param loggedInInfo        LoggedInInfo the current user's session (for current-provider comparison)
     * @param attachedDocs        List&lt;EDoc&gt; the docs attached to the current consult/eForm; may be null/empty
     * @param allDocuments        List&lt;EDoc&gt; mutable list of patient documents; may be null
     * @param providerPrivateDocs List&lt;EDoc&gt; mutable list of the current provider's private eDocs; may be null
     * @param providerPublicDocs  List&lt;EDoc&gt; mutable list of public provider eDocs; may be null
     * @param attachedDocumentIds Set&lt;String&gt; populated with the doc IDs of every attached doc
     * @param foreignPrivateDocIds Set&lt;String&gt; populated with the doc IDs of attached private docs not owned by the current provider
     */
    public void mergeAttachedIntoSections(LoggedInInfo loggedInInfo, List<EDoc> attachedDocs, List<EDoc> allDocuments, List<EDoc> providerPrivateDocs, List<EDoc> providerPublicDocs, Set<String> attachedDocumentIds, Set<String> foreignPrivateDocIds);

    /**
     * Returns the EDocs currently attached to a consultation request, or an empty
     * list when {@code requestId} is absent. Used by the attachment-dialog flow
     * to render pre-checked and cross-provider markers alongside the patient's
     * document library.
     *
     * @param loggedInInfo  LoggedInInfo the current user's session
     * @param demographicNo String the patient's demographic number
     * @param requestId     String the consultation request id; {@code null} short-circuits to an empty list
     * @return List&lt;EDoc&gt; attached EDocs, or empty list when {@code requestId} is {@code null}
     */
    public List<EDoc> getAttachedDocsForConsult(LoggedInInfo loggedInInfo, String demographicNo, String requestId);

    /**
     * Returns the EDocs currently attached to an eForm instance, or an empty
     * list when {@code fdid} is absent. Used by the attachment-dialog flow to
     * render pre-checked and cross-provider markers alongside the patient's
     * document library.
     *
     * @param loggedInInfo  LoggedInInfo the current user's session
     * @param demographicNo String the patient's demographic number
     * @param fdid          String the form-data id; {@code null} short-circuits to an empty list
     * @return List&lt;EDoc&gt; attached EDocs, or empty list when {@code fdid} is {@code null}
     */
    public List<EDoc> getAttachedDocsForEForm(LoggedInInfo loggedInInfo, String demographicNo, String fdid);
}

	
