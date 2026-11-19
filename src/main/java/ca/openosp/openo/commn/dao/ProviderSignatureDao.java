package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ProviderSignature;

import java.util.List;

/**
 * Data access for the signatures in each provider's list.
 *
 * @since 2026-10-26
 */
public interface ProviderSignatureDao extends AbstractDao<ProviderSignature> {

    /**
     * Gets a provider's signatures in the order they were added.
     *
     * @param providerNo String the provider's number
     * @return List&lt;ProviderSignature&gt; the provider's signatures, empty when they have none
     */
    List<ProviderSignature> findByProviderNo(String providerNo);
}
