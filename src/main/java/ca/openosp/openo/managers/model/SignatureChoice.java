package ca.openosp.openo.managers.model;

/**
 * One option in a provider's signature list: one of their saved signatures, or their prescriber
 * name, which is always in the list and has no signature id.
 *
 * @since 2026-10-26
 */
public class SignatureChoice {

    private final Integer signatureId;
    private final String text;
    private final boolean defaultChoice;

    /**
     * Creates a choice.
     *
     * @param signatureId Integer the saved signature's id, or null for the prescriber name
     * @param text String the text printed on the prescription
     * @param defaultChoice boolean whether this is the provider's default
     */
    public SignatureChoice(Integer signatureId, String text, boolean defaultChoice) {
        this.signatureId = signatureId;
        this.text = text;
        this.defaultChoice = defaultChoice;
    }

    /**
     * @return Integer the saved signature's id, or null for the prescriber name
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
}
