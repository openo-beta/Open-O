package ca.openosp.openo.documentManager;

import org.owasp.encoder.Encode;

/**
 * Builds the URL that opens a patient document, choosing the viewer the same way the eChart does.
 *
 * <p>A local PDF opens in the EMR document viewer ({@code showDocument.jsp}), which shows the PDF
 * beside the document details and the Acknowledge / Comment / Forward / Tickler / E-Chart actions.
 * Any other document opens as the raw file ({@code ManageDocument.do?method=display}), since the
 * viewer only renders PDFs. Opening a PDF as the raw file leaves its display to the browser, which
 * may download it and hand it to an external reader such as Adobe Acrobat.</p>
 *
 * @see ca.openosp.openo.encounter.pageUtil.EctDisplayDocs2Action
 * @since 2026-11-13
 */
public final class DocumentViewUrl {

    /** Context-relative path of the EMR document viewer. */
    public static final String DOCUMENT_VIEWER_PATH = "/documentManager/showDocument.jsp";

    private DocumentViewUrl() {
    }

    /**
     * Whether a document content type is a PDF, using the same test as {@link EDoc#isPDF()}.
     *
     * @param contentType String the document's content type; may be null
     * @return boolean true when the content type contains {@code /pdf}
     */
    public static boolean isPdfContentType(String contentType) {
        return contentType != null && contentType.contains("/pdf");
    }

    /**
     * Builds the context-relative URL that opens a document.
     *
     * @param documentNo String the document number
     * @param pdf        boolean true when the document is a PDF, which opens in the EMR document viewer
     * @param providerNo String the current provider number, passed to the raw-file viewer; may be null
     * @return String the context-relative URL (prefix the context path)
     */
    public static String forDocument(String documentNo, boolean pdf, String providerNo) {
        String id = Encode.forUriComponent(String.valueOf(documentNo));
        if (pdf) {
            return DOCUMENT_VIEWER_PATH + "?inWindow=true&segmentID=" + id;
        }
        String provider = Encode.forUriComponent(String.valueOf(providerNo));
        return "/documentManager/ManageDocument.do?method=display&doc_no=" + id
                + "&providerNo=" + provider + "&searchProviderNo=" + provider + "&status=";
    }
}
