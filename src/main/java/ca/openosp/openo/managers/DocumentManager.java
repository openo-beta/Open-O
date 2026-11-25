//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.io.IOException;
import java.nio.file.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.dao.DocumentDao;
import ca.openosp.openo.commn.exception.DocumentNotSendableException;
import ca.openosp.openo.commn.model.CtlDocument;
import ca.openosp.openo.commn.model.Document;

import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.PDFGenerationException;

import ca.openosp.openo.documentManager.EDoc;

public interface DocumentManager {

    public Document getDocument(LoggedInInfo loggedInInfo, Integer id);

    public List<Document> getDocumentsByDemographicNo(LoggedInInfo loggedInInfo, Integer demographicNo);

    public CtlDocument getCtlDocumentByDocumentId(LoggedInInfo loggedInInfo, Integer documentId);

    /**
     * Returns the patient a document is filed to.
     *
     * <p>A document has one {@link CtlDocument} link. An uploaded document starts linked to
     * patient -1 until staff save a patient, and a document in a provider's inbox is linked to
     * that provider instead; neither counts as filed to a patient.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param documentId Integer the document number
     * @return Integer the patient's demographic number, or null when the document has no link or isn't filed to a patient
     * @throws RuntimeException if the user may not read documents
     * @since 2026-11-25
     */
    public Integer getLinkedDemographicNo(LoggedInInfo loggedInInfo, Integer documentId);

    /**
     * Returns the patient a document can be faxed or emailed for: the patient it is filed to.
     *
     * <p>This is the one rule for sending a document from the document view. The document must exist,
     * be filed to a patient whose record the user may open, and not be deleted. The deleted check comes
     * after the access checks, so a user who can't open the patient learns nothing about the document.</p>
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param document Document the document the caller loaded, or null when it wasn't found
     * @return int the patient's demographic number
     * @throws DocumentNotSendableException if the document is missing, deleted or not filed to a patient
     * @throws SecurityException if the user may not read this patient's documents or open the patient's record
     * @since 2026-11-25
     */
    public int getSendableDemographicNo(LoggedInInfo loggedInInfo, Document document) throws DocumentNotSendableException;

    /**
     * Creates a document and saves it to the provided demographic
     *
     * @param loggedInInfo  The logged in info of the current user
     * @param document      Document to create
     * @param demographicNo The demographic number to save the document to
     * @param providerNo    The optional providers number to route the document to
     * @param documentData  The document byte data
     * @return Document record from the database once it has been created
     * @throws IOException If actions related to getting document data fail
     */
    public Document createDocument(LoggedInInfo loggedInInfo, Document document, Integer demographicNo, String providerNo, byte[] documentData) throws IOException;

    public List<Document> getDocumentsUpdateAfterDate(LoggedInInfo loggedInInfo, Date updatedAfterThisDateExclusive, int itemsToReturn);

    public List<Document> getDocumentsByDemographicIdUpdateAfterDate(LoggedInInfo loggedInInfo, Integer demographicId, Date updatedAfterThisDateExclusive);

    public List<Document> getDocumentsByProgramProviderDemographicDate(LoggedInInfo loggedInInfo, Integer programId, String providerNo, Integer demographicId, Calendar updatedAfterThisDateExclusive, int itemsToReturn);

    public Integer saveDocument(LoggedInInfo loggedInInfo, EDoc edoc);


    public Integer saveDocument(LoggedInInfo loggedInInfo, Document document, CtlDocument ctlDocument);

    public void moveDocumentToOscarDocuments(LoggedInInfo loggedInInfo, Document document, String fromPath);

    public void moveDocument(LoggedInInfo loggedInInfo, Document document, String fromPath, String toPath);


    public String getPathToDocument(LoggedInInfo loggedInInfo, int documentId);

    public String getFullPathToDocument(String filename);

    /**
     * Fetch by demographic number and given document type
     * ie: get only LAB documents for the given demographic number.
     */
    public List<Document> getDemographicDocumentsByDocumentType(LoggedInInfo loggedInInfo, int demographicNo, DocumentDao.DocumentType documentType);

    public Document getDocumentByDemographicAndFilename(LoggedInInfo loggedInInfo, int demographicNo, String fileName);

    /**
     * Add a document to Oscar's document library.
     * <p>
     * This method actually saves the Document contents to the file system. The document resource
     * MUST contain valid Base64 encoded document binary data.
     *
     * @param loggedInInfo
     * @param document
     * @return
     * @throws Exception
     */
    public Document addDocument(LoggedInInfo loggedInInfo, Document document, CtlDocument ctlDocument) throws Exception;

    public List<String> getProvidersThatHaveAcknowledgedDocument(LoggedInInfo loggedInInfo, Integer documentId);

    public Path renderDocument(LoggedInInfo loggedInInfo, EDoc eDoc) throws PDFGenerationException;

    public Path renderDocument(LoggedInInfo loggedInInfo, String documentId) throws PDFGenerationException;

     public Integer addDocumentToQueue(LoggedInInfo loggedInInfo, Integer documentId, Integer queueId);
}
