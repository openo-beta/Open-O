package ca.openosp.openo.managers.model;

import java.util.List;

/**
 * What the prescription window's signature card shows: the signature the prescription prints, whether
 * it is saved on the prescription already, whether the signatures are the user's own, and, when the
 * user may still choose, the list to choose from. A class with getters rather than a record, so the
 * JSP can read it.
 *
 * @since 2026-10-26
 */
public class SignatureCard {

    private final String scriptNo;
    private final String signature;
    private final boolean locked;
    private final boolean ownSignatures;
    private final List<SignatureChoice> choices;

    private SignatureCard(String scriptNo, String signature, boolean locked, boolean ownSignatures, List<SignatureChoice> choices) {
        this.scriptNo = scriptNo;
        this.signature = signature;
        this.locked = locked;
        this.ownSignatures = ownSignatures;
        this.choices = choices;
    }

    /**
     * Creates the card for a prescription whose signature is saved: it shows the saved one, with nothing to choose.
     *
     * @param scriptNo String the prescription's number
     * @param savedSignature String the signature saved on the prescription
     * @param ownSignatures boolean whether the user is the prescriber, so the signatures are their own
     * @return SignatureCard the locked card
     */
    public static SignatureCard saved(String scriptNo, String savedSignature, boolean ownSignatures) {
        return new SignatureCard(scriptNo, savedSignature, true, ownSignatures, List.of());
    }

    /**
     * Creates the card for a prescription not printed yet: it shows the prescriber's default, and the
     * prescriber may choose another.
     *
     * @param scriptNo String the prescription's number, or null when there isn't one
     * @param defaultSignature String the prescriber's default
     * @param ownSignatures boolean whether the user is the prescriber, so the signatures are their own
     * @param choices List&lt;SignatureChoice&gt; the prescriber's list when the signatures are the user's own, otherwise empty
     * @return SignatureCard the card
     */
    public static SignatureCard unsaved(String scriptNo, String defaultSignature, boolean ownSignatures, List<SignatureChoice> choices) {
        return new SignatureCard(scriptNo, defaultSignature, false, ownSignatures, choices);
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
     * @return boolean whether the user is the prescriber, so the signatures are their own to add and
     *         edit, before or after the first print; the card's settings cog shows only then
     */
    public boolean isOwnSignatures() {
        return ownSignatures;
    }

    /**
     * @return boolean whether the user may choose the signature: only the prescriber, and only before the first print
     */
    public boolean isEditable() {
        return ownSignatures && !locked;
    }

    /**
     * @return List&lt;SignatureChoice&gt; the prescriber's signature list, empty when the user can't choose
     */
    public List<SignatureChoice> getChoices() {
        return choices;
    }
}
