package ca.openosp.openo.managers;

/**
 * Looks up the signature a provider prints on prescriptions.
 *
 * <p>A provider's default signature is the text saved in their preferences
 * ({@code providerExt.signature}). When it is missing or blank, prescriptions print the
 * provider's name instead, as they always have.
 *
 * @since 2026-10-26
 */
public interface ProviderSignatureManager {

    /**
     * Gets the signature printed for a provider when no other one is chosen.
     *
     * @param providerNo String the provider's number
     * @return String the saved signature, or the provider's prescriber name when none is saved
     */
    String getDefaultSignature(String providerNo);

    /**
     * Gets a provider's name the way prescriptions print it: "Dr. " (unless the first name
     * already has it), the first name, then the last name.
     *
     * @param providerNo String the provider's number
     * @return String the prescriber name, or an empty string when the provider doesn't exist
     */
    String getPrescriberName(String providerNo);
}
