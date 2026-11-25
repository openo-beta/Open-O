package ca.openosp.openo.commn.exception;

import ca.openosp.openo.utility.PDFGenerationException;

/**
 * A patient document that can't be faxed or emailed, for a reason written for staff, for example
 * "It has been deleted." or "It isn't linked to a patient."
 *
 * <p>The message is always fixed text and never document content such as its description, so it is
 * safe to show staff on any page, including the email window's error message. It extends
 * {@link PDFGenerationException} because, to a fax or email window, a document that can't be sent and
 * one that can't be rendered both mean there is no PDF to send.</p>
 *
 * @since 2026-11-25
 */
public class DocumentNotSendableException extends PDFGenerationException {

    /**
     * Creates the exception for one reason.
     *
     * @param reason String the reason, written for staff
     */
    public DocumentNotSendableException(String reason) {
        super(reason);
    }
}
