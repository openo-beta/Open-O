package ca.openosp.openo.managers.model;

import java.util.List;

/**
 * What the prescription window's signature card shows: the signature the prescription prints, whether
 * it is saved on the prescription already, and, when the user may still choose, the list to choose from.
 * A class with getters rather than a record, so the JSP can read it.
 *
 * @since 2026-10-26
 */
public class SignatureCard {

    private final String scriptNo;
    private final String signature;
    private final boolean locked;
    private final List<SignatureChoice> choices;

    /**
     * Creates a card.
     *
     * @param scriptNo String the prescription's number, or null when there isn't one
     * @param signature String the signature the prescription prints
     * @param locked boolean whether the signature is saved on the prescription already
     * @param choices List&lt;SignatureChoice&gt; the list the user may choose from, empty when they can't choose
     */
    public SignatureCard(String scriptNo, String signature, boolean locked, List<SignatureChoice> choices) {
        this.scriptNo = scriptNo;
        this.signature = signature;
        this.locked = locked;
        this.choices = choices;
    }

    /**
     * @return String the prescription's number, or null when there isn't one
     */
    public String getScriptNo() {
        return scriptNo;
    }

    /**
     * @return String the signature the prescription prints: the saved one, or before the first print the prescriber's default
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return boolean whether the signature is saved on the prescription, so it can't change
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @return boolean whether the user may choose the signature: only the prescriber, and only before the first print
     */
    public boolean isEditable() {
        return !choices.isEmpty();
    }

    /**
     * @return List&lt;SignatureChoice&gt; the prescriber's signature list, empty when the user can't choose
     */
    public List<SignatureChoice> getChoices() {
        return choices;
    }
}
