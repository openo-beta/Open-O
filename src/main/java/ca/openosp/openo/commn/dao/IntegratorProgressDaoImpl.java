//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.IntegratorProgress;
import org.springframework.stereotype.Repository;

@Repository
public class IntegratorProgressDaoImpl extends AbstractDaoImpl<IntegratorProgress> implements IntegratorProgressDao {

    public IntegratorProgressDaoImpl() {
        super(IntegratorProgress.class);
    }

    public List<IntegratorProgress> findCompleted() {
        Query query = entityManager.createQuery("select f from IntegratorProgress f where f.status = ?1 order by f.dateCreated DESC");
        query.setParameter(1, IntegratorProgress.STATUS_COMPLETED);
        @SuppressWarnings("unchecked")
        List<IntegratorProgress> results = query.getResultList();

        return results;
    }

    public List<IntegratorProgress> findRunning() {
        Query query = entityManager.createQuery("select f from IntegratorProgress f where f.status = ?1");
        query.setParameter(1, IntegratorProgress.STATUS_RUNNING);
        @SuppressWarnings("unchecked")
        List<IntegratorProgress> results = query.getResultList();

        return results;
    }
}
