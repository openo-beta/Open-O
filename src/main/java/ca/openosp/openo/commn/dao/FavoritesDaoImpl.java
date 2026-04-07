//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Favorites;
import org.springframework.stereotype.Repository;

@Repository
public class FavoritesDaoImpl extends AbstractDaoImpl<Favorites> implements FavoritesDao {

    public FavoritesDaoImpl() {
        super(Favorites.class);
    }

    public List<Favorites> findByProviderNo(String providerNo) {
        Query query = entityManager.createQuery("select x from Favorites x where x.providerNo=?1");
        query.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<Favorites> results = query.getResultList();

        return results;
    }
}
