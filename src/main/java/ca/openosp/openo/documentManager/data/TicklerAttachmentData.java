//CHECKSTYLE:OFF
package ca.openosp.openo.documentManager.data;

import ca.openosp.openo.commn.model.enumerator.DocumentType;

/**
 * A tickler attachment's type, id, human-readable display name and viewer URL.
 *
 * @since 2026-07-19
 */
public class TicklerAttachmentData {
    private DocumentType documentType;
    private String documentId;
    private String displayName;
    private String viewUrl;

    public TicklerAttachmentData() {
    }

    public TicklerAttachmentData(DocumentType documentType, String documentId, String displayName) {
        this(documentType, documentId, displayName, null);
    }

    /**
     * @param viewUrl String context-relative viewer URL, or null when the attachment can't be addressed
     */
    public TicklerAttachmentData(DocumentType documentType, String documentId, String displayName, String viewUrl) {
        this.documentType = documentType;
        this.documentId = documentId;
        this.displayName = displayName;
        this.viewUrl = viewUrl;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return String context-relative URL of the attachment's viewer (prefix the context path),
     *         or null when it can't be addressed, e.g. a form whose name no longer resolves
     */
    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }
}
