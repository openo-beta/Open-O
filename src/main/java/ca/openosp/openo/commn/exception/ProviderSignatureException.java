package ca.openosp.openo.commn.exception;

/**
 * A change to a provider's signatures that can't be made, for a reason the page shows the user:
 * a blank, too long or duplicate text, or a signature deleted in another window.
 *
 * @since 2026-10-26
 */
public class ProviderSignatureException extends RuntimeException {

    /**
     * Creates the exception for one reason.
     *
     * @param messageKey String the resource key of the reason, one of the {@code ProviderSignatureManager.ERROR_*} keys
     */
    public ProviderSignatureException(String messageKey) {
        super(messageKey);
    }

    /**
     * @return String the resource key of the reason, which the page shows the user
     */
    public String getMessageKey() {
        return getMessage();
    }
}
