//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.ProviderSite;

public interface ProviderSiteDao extends AbstractDao<ProviderSite> {
    List<ProviderSite> findByProviderNo(String providerNo);

    List<Provider> findActiveProvidersWithSites(String provider_no);

    List<String> findByProviderNoBySiteName(String siteName);

    List<ProviderSite> findBySiteId(Integer siteId);
}
