//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ProviderData;

public interface ProviderDataDao extends AbstractDao<ProviderData> {
    ProviderData findByOhipNumber(String ohipNumber);

    ProviderData findByProviderNo(String providerNo);

    List<ProviderData> findByProviderNo(String providerNo, String status, int limit, int offset);

    List<ProviderData> findByProviderName(String searchStr, String status, int limit, int offset);

    List<ProviderData> findAllOrderByLastName();

    List<ProviderData> findByProviderSite(String providerNo);

    List<Object[]> findProviderSecUserRoles(String lastName, String firstName);

    List<ProviderData> findByProviderTeam(String providerNo);

    List<ProviderData> findAllBilling(String active);

    List<ProviderData> findByTypeAndOhip(String providerType, String insuranceNo);

    List<ProviderData> findByType(String providerType);

    List<ProviderData> findByName(String firstName, String lastName, boolean onlyActive);

    List<ProviderData> findAll();

    List<ProviderData> findAll(boolean inactive);

    Integer getLastId();
}
