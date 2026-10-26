package ca.openosp.openo.managers;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.casemgmt.model.ProviderExt;
import ca.openosp.openo.commn.dao.ProviderExtDao;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.prescript.data.RxProviderData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Looks up provider signatures from {@code providerExt} and provider names from the provider table.
 *
 * @since 2026-10-26
 */
@Service
public class ProviderSignatureManagerImpl implements ProviderSignatureManager {

    @Autowired
    private ProviderExtDao providerExtDao;

    @Autowired
    private ProviderDao providerDao;

    @Override
    public String getDefaultSignature(String providerNo) {
        ProviderExt providerExt = providerExtDao.find(providerNo);
        String signature = providerExt == null ? null : providerExt.getSignature();
        // A blank signature counts as none, so the prescription doesn't print an empty name.
        if (signature == null || signature.isBlank()) {
            return getPrescriberName(providerNo);
        }
        return signature.strip();
    }

    @Override
    public String getPrescriberName(String providerNo) {
        Provider provider = providerDao.getProvider(providerNo);
        if (provider == null) {
            return "";
        }
        return RxProviderData.prescriberFirstName(provider.getFirstName()) + " " + provider.getLastName();
    }
}
