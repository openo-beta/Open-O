//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ProviderLabRoutingFavorite;

import org.springframework.stereotype.Repository;

import java.util.List;

import javax.persistence.Query;

@Repository
public class ProviderLabRoutingFavoritesDaoImpl extends AbstractDaoImpl<ProviderLabRoutingFavorite> implements ProviderLabRoutingFavoritesDao {

    public ProviderLabRoutingFavoritesDaoImpl() {
        super(ProviderLabRoutingFavorite.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProviderLabRoutingFavorite> findFavorites(String provider_no) {
        String sql = "select fav from ProviderLabRoutingFavorite fav where fav.provider_no = ?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, provider_no);
        return query.getResultList();
    }
}
