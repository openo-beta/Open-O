package ca.openosp.openo.managers;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.casemgmt.model.ProviderExt;
import ca.openosp.openo.commn.dao.PrescriptionDao;
import ca.openosp.openo.commn.dao.ProviderExtDao;
import ca.openosp.openo.commn.dao.ProviderSignatureDao;
import ca.openosp.openo.commn.exception.ProviderSignatureException;
import ca.openosp.openo.commn.model.Prescription;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.ProviderSignature;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.managers.model.SignatureCard;
import ca.openosp.openo.managers.model.SignatureChoice;
import ca.openosp.openo.prescript.data.RxProviderData;
import ca.openosp.openo.utility.LoggedInInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Keeps each provider's signature list in {@code providerSignature} and their default in
 * {@code providerExt.signature}, and looks up provider names from the provider table.
 *
 * @since 2026-10-26
 */
@Service
@Transactional
public class ProviderSignatureManagerImpl implements ProviderSignatureManager {

    @Autowired
    private ProviderExtDao providerExtDao;

    @Autowired
    private ProviderSignatureDao providerSignatureDao;

    @Autowired
    private ProviderDao providerDao;

    @Autowired
    private PrescriptionDao prescriptionDao;

    @Override
    public String getDefaultSignature(String providerNo) {
        String defaultText = getSavedDefaultText(providerNo);
        return defaultText == null ? getPrescriberName(providerNo) : defaultText;
    }

    @Override
    public String getPrintedSignature(String scriptNo, String prescriberNo) {
        Prescription prescription = findPrescription(scriptNo);
        String savedSignature = prescription == null ? null : prescription.getPrescriberSignature();
        return savedSignature == null ? getDefaultSignature(prescriberNo) : savedSignature;
    }

    @Override
    public SignatureCard getSignatureCard(LoggedInInfo loggedInInfo, String scriptNo, Integer demographicNo, String prescriberNo) {
        Prescription prescription = findPrescription(scriptNo);
        // Another patient's prescription shows as not printed, the way lockForPrescription refuses it
        if (prescription != null && Objects.equals(prescription.getDemographicId(), demographicNo)
                && prescription.getPrescriberSignature() != null) {
            return new SignatureCard(scriptNo, prescription.getPrescriberSignature(), true, List.of());
        }
        List<SignatureChoice> choices = choosesSignature(loggedInInfo, prescriberNo) ? getChoices(prescriberNo) : List.of();
        return new SignatureCard(scriptNo, getDefaultSignature(prescriberNo), false, choices);
    }

    @Override
    public String lockForPrescription(LoggedInInfo loggedInInfo, Integer scriptNo, Integer demographicNo, Integer signatureId) {
        Prescription prescription = prescriptionDao.find(scriptNo);
        if (prescription == null || !prescription.getDemographicId().equals(demographicNo)) {
            throw new SecurityException("Prescription " + scriptNo + " is not for the patient in this prescription window");
        }
        if (prescription.getPrescriberSignature() != null) {
            return prescription.getPrescriberSignature();
        }

        String prescriberNo = prescription.getProviderNo();
        // The window posts an id from the same list getSignatureCard gave it; anyone else saves the default
        String signature = choosesSignature(loggedInInfo, prescriberNo)
                ? findChoice(prescriberNo, signatureId).orElseThrow(() -> new ProviderSignatureException(ERROR_DELETED)).getText()
                : getDefaultSignature(prescriberNo);
        if (signature.isBlank()) {
            // Saving a blank would keep it for good; left unsaved, a later print can still save a name
            return null;
        }

        if (prescriptionDao.lockPrescriberSignature(scriptNo, signature, withFirstLine(prescription.getTextView(), signature)) == 0) {
            // Another window printed it first; its signature stands
            prescriptionDao.refresh(prescription);
            return prescription.getPrescriberSignature();
        }
        LogAction.addLog(loggedInInfo, LogConst.UPDATE, LogConst.CON_PRESCRIPTION, String.valueOf(scriptNo),
                String.valueOf(demographicNo), "prescriber signature");
        return signature;
    }

    @Override
    public List<SignatureChoice> getChoices(String providerNo) {
        String defaultText = getSavedDefaultText(providerNo);
        String prescriberName = getPrescriberName(providerNo);
        // A default saved as the prescriber name prints the same as no default, so the name entry carries it
        boolean defaultListed = defaultText == null || defaultText.equals(prescriberName);
        List<SignatureChoice> choices = new ArrayList<>();
        choices.add(new SignatureChoice(null, prescriberName, defaultListed));
        for (ProviderSignature signature : providerSignatureDao.findByProviderNo(providerNo)) {
            // A saved copy of the prescriber name, e.g. from the upgrade, would list it twice
            if (hasText(signature, prescriberName)) {
                continue;
            }
            boolean isDefault = hasText(signature, defaultText);
            defaultListed |= isDefault;
            choices.add(new SignatureChoice(signature.getId(), signature.getSignature().strip(), isDefault));
        }
        // A default saved outside the list still shows, so the print window can pre-select it
        if (!defaultListed) {
            choices.add(new SignatureChoice(SignatureChoice.UNLISTED_DEFAULT_ID, defaultText, true));
        }
        return choices;
    }

    @Override
    public Optional<SignatureChoice> findChoice(String providerNo, Integer signatureId) {
        return getChoices(providerNo).stream().filter(choice -> Objects.equals(choice.getSignatureId(), signatureId)).findFirst();
    }

    @Override
    public void addSignature(LoggedInInfo loggedInInfo, String text) {
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        addRow(providerNo, checkText(providerNo, null, text), providerNo);
    }

    @Override
    public void editSignature(LoggedInInfo loggedInInfo, Integer signatureId, String text) {
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        ProviderSignature signature = findOwnSignature(providerNo, signatureId);
        if (signature == null) {
            throw new ProviderSignatureException(ERROR_DELETED);
        }
        String newText = checkText(providerNo, signatureId, text);
        if (hasText(signature, getSavedDefaultText(providerNo))) {
            saveDefaultText(providerNo, newText);
        }
        signature.setSignature(newText);
        signature.setLastUpdateUser(providerNo);
        providerSignatureDao.merge(signature);
    }

    @Override
    public void deleteSignature(LoggedInInfo loggedInInfo, Integer signatureId) {
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        ProviderSignature signature = findOwnSignature(providerNo, signatureId);
        if (signature == null) {
            return;
        }
        if (hasText(signature, getSavedDefaultText(providerNo))) {
            saveDefaultText(providerNo, null);
        }
        providerSignatureDao.remove(signature);
    }

    @Override
    public void makeDefault(LoggedInInfo loggedInInfo, Integer signatureId) {
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        if (signatureId == null) {
            saveDefaultText(providerNo, null);
            return;
        }
        ProviderSignature signature = findOwnSignature(providerNo, signatureId);
        if (signature != null) {
            saveDefaultText(providerNo, signature.getSignature().strip());
        }
    }

    @Override
    public void saveDefaultSignatureText(LoggedInInfo loggedInInfo, String providerNo, String text) {
        String defaultText = text == null || text.isBlank() ? null : text.strip();
        if (defaultText != null
                && providerSignatureDao.findByProviderNo(providerNo).stream().noneMatch(s -> hasText(s, defaultText))) {
            addRow(providerNo, defaultText, loggedInInfo.getLoggedInProviderNo());
        }
        saveDefaultText(providerNo, defaultText);
    }

    /**
     * Gets the default saved in a provider's preferences.
     *
     * @param providerNo String the provider's number
     * @return String the saved default without surrounding spaces, or null when none is saved or it is blank
     */
    private String getSavedDefaultText(String providerNo) {
        ProviderExt providerExt = providerExtDao.find(providerNo);
        String signature = providerExt == null ? null : providerExt.getSignature();
        // A blank signature counts as none, so the prescription doesn't print an empty name.
        return signature == null || signature.isBlank() ? null : signature.strip();
    }

    /**
     * Saves a provider's default, updating their {@code providerExt} row or adding one.
     *
     * @param providerNo String the provider's number
     * @param text String the default text, or null for the prescriber name
     */
    private void saveDefaultText(String providerNo, String text) {
        ProviderExt providerExt = providerExtDao.find(providerNo);
        if (providerExt == null) {
            if (text == null) {
                return;
            }
            providerExt = new ProviderExt();
            providerExt.setProviderNo(providerNo);
            providerExt.setSignature(text);
            providerExtDao.persist(providerExt);
        } else {
            providerExt.setSignature(text);
            providerExtDao.merge(providerExt);
        }
    }

    /**
     * Puts a signature on the first line of a prescription's text copy, where the prescriber's
     * signature has been written since prescriptions were first saved.
     *
     * @param textView String the text copy, which may be null or empty
     * @param signature String the signature
     * @return String the text copy with its first line replaced, or the text copy unchanged when it is null or empty
     */
    static String withFirstLine(String textView, String signature) {
        if (textView == null || textView.isEmpty()) {
            return textView;
        }
        int endOfFirstLine = textView.indexOf('\n');
        return endOfFirstLine < 0 ? signature : signature + textView.substring(endOfFirstLine);
    }

    /**
     * Whether a saved signature has the given text, ignoring surrounding spaces, which signatures
     * copied into the list when it was created may still have.
     */
    private static boolean hasText(ProviderSignature signature, String text) {
        return signature.getSignature().strip().equals(text);
    }

    private ProviderSignature addRow(String providerNo, String text, String updatedBy) {
        ProviderSignature signature = new ProviderSignature();
        signature.setProviderNo(providerNo);
        signature.setSignature(text);
        signature.setLastUpdateUser(updatedBy);
        providerSignatureDao.persist(signature);
        return signature;
    }

    /**
     * Gets one of a provider's own signatures.
     *
     * @return ProviderSignature the signature, or null when it doesn't exist, e.g. deleted in another window
     * @throws SecurityException when the signature belongs to another provider
     */
    private ProviderSignature findOwnSignature(String providerNo, Integer signatureId) {
        ProviderSignature signature = signatureId == null ? null : providerSignatureDao.find(signatureId);
        if (signature != null && !signature.getProviderNo().equals(providerNo)) {
            throw new SecurityException("Signature " + signatureId + " is not in the logged-in provider's list");
        }
        return signature;
    }

    /**
     * Checks a signature text before it is saved to a provider's list.
     *
     * @param signatureId Integer the signature being edited, left out of the duplicate check; null when adding
     * @return String the text without surrounding spaces
     * @throws ProviderSignatureException with the message key of the problem
     */
    private String checkText(String providerNo, Integer signatureId, String text) {
        String signature = text == null ? "" : text.strip();
        if (signature.isEmpty()) {
            throw new ProviderSignatureException(ERROR_BLANK);
        }
        if (signature.length() > MAX_LENGTH) {
            throw new ProviderSignatureException(ERROR_TOO_LONG);
        }
        boolean duplicate = signature.equals(getPrescriberName(providerNo))
                || providerSignatureDao.findByProviderNo(providerNo).stream()
                .anyMatch(s -> !s.getId().equals(signatureId) && hasText(s, signature));
        if (duplicate) {
            throw new ProviderSignatureException(ERROR_DUPLICATE);
        }
        return signature;
    }

    /**
     * Whether the user picks the prescription's signature: only the prescriber does. Anyone else
     * printing it gets the prescriber's default.
     */
    private static boolean choosesSignature(LoggedInInfo loggedInInfo, String prescriberNo) {
        return loggedInInfo.getLoggedInProviderNo().equals(prescriberNo);
    }

    /**
     * Gets a provider's name the way prescriptions print it: "Dr. " (unless the first name
     * already has it), the first name, then the last name.
     *
     * @return String the prescriber name, or an empty string when the provider doesn't exist
     */
    private String getPrescriberName(String providerNo) {
        Provider provider = providerDao.getProvider(providerNo);
        if (provider == null) {
            return "";
        }
        return RxProviderData.prescriberFirstName(provider.getFirstName()) + " " + provider.getLastName();
    }

    /**
     * Finds a prescription by the number a page gives for it.
     *
     * @param scriptNo String the prescription's number, or null when there isn't one yet
     * @return Prescription the prescription, or null when there is no such number
     */
    private Prescription findPrescription(String scriptNo) {
        try {
            return StringUtils.isNumeric(scriptNo) ? prescriptionDao.find(Integer.valueOf(scriptNo)) : null;
        } catch (NumberFormatException e) {
            // Too big to be a prescription number
            return null;
        }
    }
}
