//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.FavoritesPrivilege;

public interface FavoritesPrivilegeDao extends AbstractDao<FavoritesPrivilege> {
    List<String> getProviders();

    FavoritesPrivilege findByProviderNo(String providerNo);

    void setFavoritesPrivilege(String providerNo, boolean openpublic, boolean writeable);
}
