//CHECKSTYLE:OFF
package ca.openosp.openo.documentManager;

import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.documentManager.data.AttachmentLabResultData;
import ca.openosp.openo.documentManager.data.TicklerAttachmentData;
import ca.openosp.openo.documentManager.data.AttachmentSections;
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
     * Maps every encounter form belonging to a patient to its form name, keyed by form id.
     *
     * <p>Attachment tables store only {@code (document_no, doctype)}, which cannot identify a form
     * because encounter forms span roughly forty tables with independent id sequences. Ids shared
     * by two form types are therefore omitted rather than resolved to one of them at random.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param demographicNo Integer the patient's unique demographic identifier
     * @return Map&lt;String, String&gt; form id to form name; empty when the demographic is null or
     *         the user lacks "_form" read, so callers should render the attachment as non-clickable
     */
    public Map<String, String> getFormNamesByFormId(LoggedInInfo loggedInInfo, Integer demographicNo);

    /**
     * Batched form of {@link #getFormNamesByFormId}, for rendering lists spanning many patients.
     *
     * <p>Reads the encounter form configuration once for the whole batch rather than once per
     * patient, which is where nearly all of the per-patient cost otherwise goes.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param demographicNos Collection&lt;Integer&gt; the patients to resolve; may be empty
     * @return Map&lt;Integer, Map&lt;String, String&gt;&gt; demographic number to that patient's form
     *         id to form name lookup; empty when the user lacks "_form" read
     */
    public Map<Integer, Map<String, String>> getFormNamesByDemographic(LoggedInInfo loggedInInfo, Collection<Integer> demographicNos);

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

    /**
     * Retrieves all attachments of a given type associated with a specific tickler.
     *
     * <p>Tickler counterpart of {@link #getConsultAttachments} and {@link #getEFormAttachments}.
     * Returns the document identifiers currently attached to the tickler for the requested
     * document type (document, lab, eForm, HRM report or encounter form).</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information for security and audit purposes
     * @param ticklerId Integer the unique identifier of the tickler
     * @param documentType DocumentType the type of documents to retrieve
     * @param demographicNo Integer the patient's unique demographic identifier
     * @return List&lt;String&gt; list of document identifiers attached to the tickler
     */
    public List<String> getTicklerAttachments(LoggedInInfo loggedInInfo, Integer ticklerId, DocumentType documentType, Integer demographicNo);

    /**
     * Retrieves every attachment on a tickler, of all types, with display names resolved.
     * Used to render the named attachment lists in the Add/Edit Tickler windows.
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param ticklerId Integer the unique identifier of the tickler
     * @param demographicNo Integer the patient's unique demographic identifier
     * @return List&lt;TicklerAttachmentData&gt; all attachments with display names (empty if none)
     */
    public List<TicklerAttachmentData> getTicklerAttachmentDetails(LoggedInInfo loggedInInfo, Integer ticklerId, Integer demographicNo);

    /**
     * Attaches documents to a tickler.
     *
     * <p>Tickler counterpart of {@link #attachToConsult} and {@link #attachToEForm}. Synchronises the
     * supplied set of document identifiers with the tickler's existing attachments of the given type:
     * new identifiers are persisted and identifiers no longer present are soft-deleted.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information for security and audit purposes
     * @param documentType DocumentType the type of documents being attached
     * @param attachments String[] array of document identifiers that should be attached to the tickler
     * @param providerNo String the provider number performing the attachment operation
     * @param ticklerId Integer the unique identifier of the tickler
     * @param demographicNo Integer the patient's unique demographic identifier
     */
    public void attachToTickler(LoggedInInfo loggedInInfo, DocumentType documentType, String[] attachments, String providerNo, Integer ticklerId, Integer demographicNo);

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
     * Merges the items attached to a consult/eForm into the attachment window's sections so
     * every attached item is listed and can be unchecked to detach it. Each attached item is
     * added to the top of its section unless that section already lists it — e.g. deleted items,
     * other providers' private docs, and docs no longer listed for this patient or facility. Also
     * records the attached doc/eForm ids (for pre-checking) and the ids of attached private docs
     * owned by another provider (for labelling).
     *
     * @param loggedInInfo   LoggedInInfo the current user's session (for current-provider comparison)
     * @param attachedDocs   List&lt;EDoc&gt; the docs attached to the current consult/eForm; may be null/empty
     * @param attachedEForms List&lt;EFormData&gt; the eForms attached to the current consult/eForm; may be null/empty
     * @param sections       AttachmentSections the sections to merge into; mutated in place
     */
    public void mergeAttachedIntoSections(LoggedInInfo loggedInInfo, List<EDoc> attachedDocs, List<EFormData> attachedEForms, AttachmentSections sections);

    /**
     * Returns the EDocs currently attached to a consultation request, or an empty
     * list when {@code requestId} is absent or the consultation isn't this patient's.
     * Used by the attachment-dialog flow to render pre-checked and cross-provider
     * markers alongside the patient's document library.
     *
     * @param loggedInInfo  LoggedInInfo the current user's session
     * @param demographicNo String the patient's demographic number; the consultation must belong to this patient
     * @param requestId     String the consultation request id; {@code null} short-circuits to an empty list
     * @return List&lt;EDoc&gt; attached EDocs, or empty list when {@code requestId} is {@code null} or not this patient's
     */
    public List<EDoc> getAttachedDocsForConsult(LoggedInInfo loggedInInfo, String demographicNo, String requestId);

    /**
     * Returns the EDocs currently attached to an eForm instance, or an empty
     * list when {@code fdid} is absent or the eForm isn't this patient's. Used by
     * the attachment-dialog flow to render pre-checked and cross-provider markers
     * alongside the patient's document library.
     *
     * @param loggedInInfo  LoggedInInfo the current user's session
     * @param demographicNo String the patient's demographic number; the eForm must belong to this patient
     * @param fdid          String the form-data id; {@code null} short-circuits to an empty list
     * @return List&lt;EDoc&gt; attached EDocs, or empty list when {@code fdid} is {@code null} or not this patient's
     */
    public List<EDoc> getAttachedDocsForEForm(LoggedInInfo loggedInInfo, String demographicNo, String fdid);

    /**
     * Returns the eForms currently attached to a consultation request, deleted ones included,
     * or an empty list when {@code requestId} is absent or the consultation isn't this patient's.
     *
     * @param demographicNo String the patient's demographic number; the consultation must belong to this patient
     * @param requestId     String the consultation request id; {@code null} short-circuits to an empty list
     * @return List&lt;EFormData&gt; attached eForms, or empty list when {@code requestId} is {@code null} or not this patient's
     */
    public List<EFormData> getAttachedEFormsForConsult(String demographicNo, String requestId);

    /**
     * Returns the eForms currently attached to an eForm instance, deleted ones included,
     * or an empty list when {@code fdid} is absent or the eForm isn't this patient's.
     *
     * @param demographicNo String the patient's demographic number; the eForm must belong to this patient
     * @param fdid          String the form-data id; {@code null} short-circuits to an empty list
     * @return List&lt;EFormData&gt; attached eForms, or empty list when {@code fdid} is {@code null} or not this patient's
     */
    public List<EFormData> getAttachedEFormsForEForm(String demographicNo, String fdid);
}

	
