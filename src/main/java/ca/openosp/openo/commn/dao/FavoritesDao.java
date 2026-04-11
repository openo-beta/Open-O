//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Favorites;

public interface FavoritesDao extends AbstractDao<Favorites> {
    List<Favorites> findByProviderNo(String providerNo);
}
