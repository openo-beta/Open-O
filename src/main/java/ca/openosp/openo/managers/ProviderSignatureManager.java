package ca.openosp.openo.managers;

import ca.openosp.openo.commn.exception.ProviderSignatureException;
import ca.openosp.openo.managers.model.SignatureCard;
import ca.openosp.openo.managers.model.SignatureChoice;
import ca.openosp.openo.utility.LoggedInInfo;

import java.util.List;
import java.util.Optional;

/**
 * Looks up and keeps the signatures a provider prints on prescriptions.
 *
 * <p>Each provider has a list of signatures to choose from when they print a prescription, and
 * their prescriber name is always in it. Their default is the text saved in their preferences
 * ({@code providerExt.signature}). When it is missing or blank, the default is the prescriber
 * name, as it always has been.
 *
 * @since 2026-10-26
 */
public interface ProviderSignatureManager {

    /** Longest signature that can be saved, the size of the signature columns. */
    int MAX_LENGTH = 255;

    /** Message key for a signature with no text. */
    String ERROR_BLANK = "provider.providerSignature.errBlank";

    /** Message key for a signature longer than {@link #MAX_LENGTH}. */
    String ERROR_TOO_LONG = "provider.providerSignature.errTooLong";

    /** Message key for a signature already in the provider's list. */
    String ERROR_DUPLICATE = "provider.providerSignature.errDuplicate";

    /** Message key for a signature deleted, in another window, after the page showing it opened. */
    String ERROR_DELETED = "provider.providerSignature.errDeleted";

    /**
     * Gets the signature printed for a provider when no other one is chosen.
     *
     * @param providerNo String the provider's number
     * @return String the saved signature, or the provider's prescriber name when none is saved
     */
    String getDefaultSignature(String providerNo);

    /**
     * Gets the signature a prescription prints: the one saved at its first print, fax or PDF, or
     * before then the prescriber's default.
     *
     * @param scriptNo String the prescription's number, or null when there isn't one yet
     * @param prescriberNo String the prescriber's number
     * @return String the signature to print
     */
    String getPrintedSignature(String scriptNo, String prescriberNo);

    /**
     * Gets what the prescription window's signature card shows. Only the prescriber may choose, and
     * only until the first print, fax or PDF saves the signature on the prescription. A prescription
     * for another patient is treated as not printed yet, since saving on it is refused.
     *
     * @param loggedInInfo LoggedInInfo the user who opened the window
     * @param scriptNo String the prescription's number, or null when there isn't one yet
     * @param demographicNo Integer the patient in the prescription window
     * @param prescriberNo String the prescriber's number
     * @return SignatureCard the signature the prescription prints, and the list to choose from when the user may choose
     */
    SignatureCard getSignatureCard(LoggedInInfo loggedInInfo, String scriptNo, Integer demographicNo, String prescriberNo);

    /**
     * Saves the signature on a prescription at its first print, fax or PDF, so every reprint shows
     * the same one, and writes it on the first line of the prescription's text copy. Only the
     * prescriber chooses; anyone else saves the prescriber's default. When the prescription already
     * has a signature, nothing changes. Only a save writes an audit entry.
     *
     * @param loggedInInfo LoggedInInfo the user printing the prescription
     * @param scriptNo Integer the prescription's number
     * @param demographicNo Integer the patient of the user's prescription window
     * @param signatureId Integer the prescriber's choice: the id of an entry in their {@link #getChoices} list
     * @return String the signature saved on the prescription, or null when there is none to save
     *         because the prescriber has no name or default on file (their provider record is gone)
     * @throws SecurityException when the prescription isn't for that patient
     * @throws ProviderSignatureException with {@link #ERROR_DELETED} when the choice isn't in the
     *                                    prescriber's list, e.g. deleted in another window
     */
    String lockForPrescription(LoggedInInfo loggedInInfo, Integer scriptNo, Integer demographicNo, Integer signatureId);

    /**
     * Gets a provider's signature list: their prescriber name first, then their saved signatures
     * in the order they were added, then their default if it isn't in the list, with the default
     * marked. A saved signature that is the prescriber name isn't listed again; when it is the
     * default, the prescriber name entry is marked instead. Nothing is saved.
     *
     * @param providerNo String the provider's number
     * @return List&lt;SignatureChoice&gt; the provider's choices, never empty
     */
    List<SignatureChoice> getChoices(String providerNo);

    /**
     * Finds one entry of a provider's {@link #getChoices} list by its id.
     *
     * @param providerNo String the provider's number
     * @param signatureId Integer the entry's id, null for the prescriber name
     * @return Optional&lt;SignatureChoice&gt; the entry, or empty when it isn't in the list, e.g. deleted in another window
     */
    Optional<SignatureChoice> findChoice(String providerNo, Integer signatureId);

    /**
     * Adds a signature to the logged-in provider's list. It does not become the default.
     *
     * @param loggedInInfo LoggedInInfo the logged-in provider
     * @param text String the signature text
     * @throws ProviderSignatureException with {@link #ERROR_BLANK}, {@link #ERROR_TOO_LONG} or
     *                                    {@link #ERROR_DUPLICATE} when the text can't be saved
     */
    void addSignature(LoggedInInfo loggedInInfo, String text);

    /**
     * Changes the text of one of the logged-in provider's signatures. When it is their default,
     * the default changes with it. Prescriptions already printed keep the text they were printed with.
     *
     * @param loggedInInfo LoggedInInfo the logged-in provider
     * @param signatureId Integer the signature to change
     * @param text String the new text
     * @throws ProviderSignatureException with {@link #ERROR_BLANK}, {@link #ERROR_TOO_LONG} or
     *                                    {@link #ERROR_DUPLICATE} when the text can't be saved, or
     *                                    {@link #ERROR_DELETED} when the signature has been deleted
     * @throws SecurityException when the signature isn't the logged-in provider's
     */
    void editSignature(LoggedInInfo loggedInInfo, Integer signatureId, String text);

    /**
     * Deletes one of the logged-in provider's signatures. When it is their default, their
     * prescriber name becomes the default. Nothing changes when it has been deleted already.
     *
     * @param loggedInInfo LoggedInInfo the logged-in provider
     * @param signatureId Integer the signature to delete
     * @throws SecurityException when the signature isn't the logged-in provider's
     */
    void deleteSignature(LoggedInInfo loggedInInfo, Integer signatureId);

    /**
     * Makes a signature, or the prescriber name, the logged-in provider's default. Nothing changes
     * when the signature has been deleted.
     *
     * @param loggedInInfo LoggedInInfo the logged-in provider
     * @param signatureId Integer the signature to make the default, or null for the prescriber name
     * @throws SecurityException when the signature isn't the logged-in provider's
     */
    void makeDefault(LoggedInInfo loggedInInfo, Integer signatureId);

    /**
     * Sets a provider's default from its text, as the provider settings web service does. A text
     * not yet in their list is added to it; a blank one makes the prescriber name the default.
     *
     * @param loggedInInfo LoggedInInfo the user saving the settings
     * @param providerNo String the provider whose default changes
     * @param text String the new default text, or null or blank for the prescriber name
     */
    void saveDefaultSignatureText(LoggedInInfo loggedInInfo, String providerNo, String text);
}
