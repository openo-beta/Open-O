package ca.openosp.openo.managers.model;

/**
 * One option in a provider's signature list: one of their saved signatures, or their prescriber
 * name, which is always in the list and has no signature id. A default saved somewhere other than
 * the list (it is kept in {@code providerExt.signature}) shows as one more option, which can't be
 * edited or deleted.
 *
 * @since 2026-10-26
 */
public class SignatureChoice {

    /** Signature id of a default that isn't in the provider's list. No saved signature has this id. */
    public static final int UNLISTED_DEFAULT_ID = 0;

    private final Integer signatureId;
    private final String text;
    private final boolean defaultChoice;

    /**
     * Creates a choice.
     *
     * @param signatureId Integer the saved signature's id, null for the prescriber name, or
     *                    {@link #UNLISTED_DEFAULT_ID} for a default that isn't in the list
     * @param text String the text printed on the prescription
     * @param defaultChoice boolean whether this is the provider's default
     */
    public SignatureChoice(Integer signatureId, String text, boolean defaultChoice) {
        this.signatureId = signatureId;
        this.text = text;
        this.defaultChoice = defaultChoice;
    }

    /**
     * @return Integer the saved signature's id, null for the prescriber name, or
     *         {@link #UNLISTED_DEFAULT_ID} for a default that isn't in the list
     */
    public Integer getSignatureId() {
        return signatureId;
    }

    /**
     * @return String the text printed on the prescription
     */
    public String getText() {
        return text;
    }

    /**
     * @return boolean whether this is the provider's default, pre-selected when they print
     */
    public boolean isDefaultChoice() {
        return defaultChoice;
    }

    /**
     * @return boolean whether this is the prescriber name rather than a saved signature
     */
    public boolean isPrescriberName() {
        return signatureId == null;
    }

    /**
     * @return boolean whether this is a signature saved in the provider's list, which they can edit and delete
     */
    public boolean isSavedSignature() {
        return signatureId != null && signatureId != UNLISTED_DEFAULT_ID;
    }
}
