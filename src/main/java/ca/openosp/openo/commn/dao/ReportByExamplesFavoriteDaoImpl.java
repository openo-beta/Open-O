//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ReportByExamplesFavorite;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class ReportByExamplesFavoriteDaoImpl extends AbstractDaoImpl<ReportByExamplesFavorite> implements ReportByExamplesFavoriteDao {

    public ReportByExamplesFavoriteDaoImpl() {
        super(ReportByExamplesFavorite.class);
    }

    @Override
    public List<ReportByExamplesFavorite> findByQuery(String query) {
        Query q = createQuery("ex", "ex.query LIKE ?1");
        q.setParameter(1, query);
        return q.getResultList();
    }

    @Override
    public List<ReportByExamplesFavorite> findByEverything(String providerNo, String favoriteName, String queryString) {
        Query query = createQuery("ex", "ex.providerNo = ?1 AND ex.name LIKE ?2 OR ex.query LIKE ?3");
        query.setParameter(1, providerNo);
        query.setParameter(2, favoriteName);
        query.setParameter(3, queryString);
        return query.getResultList();
    }

    @Override
    public List<ReportByExamplesFavorite> findByProvider(String providerNo) {
        Query query = createQuery("ex", "ex.providerNo = ?1");
        query.setParameter(1, providerNo);
        return query.getResultList();
    }
}
