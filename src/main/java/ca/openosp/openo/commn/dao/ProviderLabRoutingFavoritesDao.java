//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ProviderLabRoutingFavorite;

import java.util.List;

public interface ProviderLabRoutingFavoritesDao extends AbstractDao<ProviderLabRoutingFavorite> {
    List<ProviderLabRoutingFavorite> findFavorites(String provider_no);
}
