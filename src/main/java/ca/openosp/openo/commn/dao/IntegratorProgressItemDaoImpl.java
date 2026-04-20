//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.IntegratorProgressItem;
import org.springframework.stereotype.Repository;

@Repository
public class IntegratorProgressItemDaoImpl extends AbstractDaoImpl<IntegratorProgressItem> implements IntegratorProgressItemDao {

    public IntegratorProgressItemDaoImpl() {
        super(IntegratorProgressItem.class);
    }

    public IntegratorProgressItem find(int integratorProgressId, int demographicNo) {
        Query query = entityManager.createQuery("SELECT f FROM IntegratorProgressItem f WHERE f.integratorProgressId=?1 and f.demographicNo = ?2");
        query.setParameter(1, integratorProgressId);
        query.setParameter(2, demographicNo);

        return getSingleResultOrNull(query);
    }

    public List<Integer> findOutstandingDemographicNos(int integratorProgressId) {
        Query query = entityManager.createQuery("SELECT f.demographicNo FROM IntegratorProgressItem f WHERE f.integratorProgressId=?1 and f.status != ?2");
        query.setParameter(1, integratorProgressId);
        query.setParameter(2, IntegratorProgressItem.STATUS_COMPLETED);

        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();

        return results;

    }

    public Integer findTotalOutstandingDemographicNos(int integratorProgressId) {
        Query query = entityManager.createQuery("SELECT count(f) FROM IntegratorProgressItem f WHERE f.integratorProgressId=?1 and f.status != ?2");
        query.setParameter(1, integratorProgressId);
        query.setParameter(2, IntegratorProgressItem.STATUS_COMPLETED);

        Long total = (Long) query.getSingleResult();

        return total.intValue();
    }

    public Integer findTotalDemographicNos(int integratorProgressId) {
        Query query = entityManager.createQuery("SELECT count(f) FROM IntegratorProgressItem f WHERE f.integratorProgressId=?1");
        query.setParameter(1, integratorProgressId);

        Long total = (Long) query.getSingleResult();

        return total.intValue();
    }

}
