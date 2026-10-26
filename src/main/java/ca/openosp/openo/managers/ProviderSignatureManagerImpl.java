package ca.openosp.openo.managers;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.casemgmt.model.ProviderExt;
import ca.openosp.openo.commn.dao.ProviderExtDao;
import ca.openosp.openo.commn.dao.ProviderSignatureDao;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.ProviderSignature;
import ca.openosp.openo.managers.model.SignatureChoice;
import ca.openosp.openo.prescript.data.RxProviderData;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public String getDefaultSignature(String providerNo) {
        String defaultText = getSavedDefaultText(providerNo);
        return defaultText == null ? getPrescriberName(providerNo) : defaultText;
    }

    @Override
    public String getPrescriberName(String providerNo) {
        Provider provider = providerDao.getProvider(providerNo);
        if (provider == null) {
            return "";
        }
        return RxProviderData.prescriberFirstName(provider.getFirstName()) + " " + provider.getLastName();
    }

    @Override
    public List<SignatureChoice> getChoices(String providerNo) {
        String defaultText = getSavedDefaultText(providerNo);
        List<ProviderSignature> signatures = new ArrayList<>(providerSignatureDao.findByProviderNo(providerNo));
        // A default saved before the list existed joins it, so the print window can pre-select it.
        if (defaultText != null && signatures.stream().noneMatch(s -> s.getSignature().equals(defaultText))) {
            signatures.add(addRow(providerNo, defaultText, providerNo));
        }

        List<SignatureChoice> choices = new ArrayList<>();
        choices.add(new SignatureChoice(null, getPrescriberName(providerNo), defaultText == null));
        for (ProviderSignature signature : signatures) {
            choices.add(new SignatureChoice(signature.getId(), signature.getSignature(), signature.getSignature().equals(defaultText)));
        }
        return choices;
    }

    @Override
    public void addSignature(LoggedInInfo loggedInInfo, String text) {
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        addRow(providerNo, checkText(providerNo, null, text), providerNo);
    }

    @Override
    public void editSignature(LoggedInInfo loggedInInfo, Integer signatureId, String text) {
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        ProviderSignature signature = getOwnSignature(providerNo, signatureId);
        String newText = checkText(providerNo, signatureId, text);
        if (signature.getSignature().equals(getSavedDefaultText(providerNo))) {
            saveDefaultText(providerNo, newText);
        }
        signature.setSignature(newText);
        signature.setLastUpdateUser(providerNo);
        providerSignatureDao.merge(signature);
    }

    @Override
    public void deleteSignature(LoggedInInfo loggedInInfo, Integer signatureId) {
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        ProviderSignature signature = getOwnSignature(providerNo, signatureId);
        if (signature.getSignature().equals(getSavedDefaultText(providerNo))) {
            saveDefaultText(providerNo, null);
        }
        providerSignatureDao.remove(signature);
    }

    @Override
    public void makeDefault(LoggedInInfo loggedInInfo, Integer signatureId) {
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        if (signatureId == null) {
            saveDefaultText(providerNo, null);
        } else {
            saveDefaultText(providerNo, getOwnSignature(providerNo, signatureId).getSignature());
        }
    }

    @Override
    public void saveDefaultSignatureText(LoggedInInfo loggedInInfo, String providerNo, String text) {
        String defaultText = text == null || text.isBlank() ? null : text.strip();
        if (defaultText != null
                && providerSignatureDao.findByProviderNo(providerNo).stream().noneMatch(s -> s.getSignature().equals(defaultText))) {
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
     * @throws SecurityException when the signature doesn't exist or belongs to another provider
     */
    private ProviderSignature getOwnSignature(String providerNo, Integer signatureId) {
        ProviderSignature signature = signatureId == null ? null : providerSignatureDao.find(signatureId);
        if (signature == null || !signature.getProviderNo().equals(providerNo)) {
            throw new SecurityException("Signature " + signatureId + " is not in the logged-in provider's list");
        }
        return signature;
    }

    /**
     * Checks a signature text before it is saved to a provider's list.
     *
     * @param signatureId Integer the signature being edited, left out of the duplicate check; null when adding
     * @return String the text without surrounding spaces
     * @throws IllegalArgumentException with the message key of the problem
     */
    private String checkText(String providerNo, Integer signatureId, String text) {
        String signature = text == null ? "" : text.strip();
        if (signature.isEmpty()) {
            throw new IllegalArgumentException(ERROR_BLANK);
        }
        if (signature.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(ERROR_TOO_LONG);
        }
        boolean duplicate = signature.equals(getPrescriberName(providerNo))
                || providerSignatureDao.findByProviderNo(providerNo).stream()
                .anyMatch(s -> !s.getId().equals(signatureId) && s.getSignature().equals(signature));
        if (duplicate) {
            throw new IllegalArgumentException(ERROR_DUPLICATE);
        }
        return signature;
    }
}
