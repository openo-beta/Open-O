//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.DemographicQueryFavourite;
import org.springframework.stereotype.Repository;

@Repository
public class DemographicQueryFavouritesDaoImpl extends AbstractDaoImpl<DemographicQueryFavourite> implements DemographicQueryFavouritesDao {

    public DemographicQueryFavouritesDaoImpl() {
        super(DemographicQueryFavourite.class);
    }

    @Override
    public List<DemographicQueryFavourite> findByArchived(String archived) {
        String sql = "select x from DemographicQueryFavourite x where x.archived=?1 order by x.queryName";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, archived);
        @SuppressWarnings("unchecked")
        List<DemographicQueryFavourite> results = query.getResultList();
        return results;
    }
}
