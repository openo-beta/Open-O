//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DemographicMerged;
import org.springframework.stereotype.Repository;

@Repository
public class DemographicMergedDaoImpl extends AbstractDaoImpl<DemographicMerged> implements DemographicMergedDao {

    public DemographicMergedDaoImpl() {
        super(DemographicMerged.class);
    }

    @Override
    public List<DemographicMerged> findCurrentByMergedTo(int demographicNo) {
        Query q = entityManager.createQuery("select d from DemographicMerged d where d.mergedTo=?1 and d.deleted=0");
        q.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<DemographicMerged> results = q.getResultList();

        return results;
    }

    @Override
    public List<DemographicMerged> findCurrentByDemographicNo(int demographicNo) {
        Query q = entityManager.createQuery("select d from DemographicMerged d where d.demographicNo=?1 and d.deleted=0");
        q.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<DemographicMerged> results = q.getResultList();

        return results;
    }

    @Override
    public List<DemographicMerged> findByDemographicNo(int demographicNo) {
        Query q = entityManager.createQuery("select d from DemographicMerged d where d.demographicNo=?1");
        q.setParameter(1, demographicNo);

        @SuppressWarnings("unchecked")
        List<DemographicMerged> results = q.getResultList();

        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DemographicMerged> findByParentAndChildIds(Integer parentId, Integer childId) {
        Query q = createQuery("d", "d.demographicNo = ?1 AND d.mergedTo = ?2");
        q.setParameter(1, parentId);
        q.setParameter(2, childId);
        return q.getResultList();
    }
}
