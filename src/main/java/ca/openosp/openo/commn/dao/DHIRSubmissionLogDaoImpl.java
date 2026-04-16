//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.DHIRSubmissionLog;
import org.springframework.stereotype.Repository;

@Repository
public class DHIRSubmissionLogDaoImpl extends AbstractDaoImpl<DHIRSubmissionLog> implements DHIRSubmissionLogDao {

    public DHIRSubmissionLogDaoImpl() {
        super(DHIRSubmissionLog.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DHIRSubmissionLog> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @Override
    public DHIRSubmissionLog findLatestPendingByPreventionId(Integer preventionId) {
        String sqlCommand = "select x from DHIRSubmissionLog x where x.preventionId=?1 and x.status = ?2 order by x.dateCreated DESC";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, preventionId);
        query.setParameter(2, "pending");

        List<DHIRSubmissionLog> results = query.getResultList();

        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }

    @Override
    public List<DHIRSubmissionLog> findByPreventionId(Integer preventionId) {
        String sqlCommand = "select x from DHIRSubmissionLog x where x.preventionId=?1 order by x.dateCreated DESC";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, preventionId);

        List<DHIRSubmissionLog> results = query.getResultList();

        return results;
    }
}
