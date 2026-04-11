//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.IntegratorFileLog;
import org.springframework.stereotype.Repository;

@Repository
public class IntegratorFileLogDaoImpl extends AbstractDaoImpl<IntegratorFileLog> implements IntegratorFileLogDao {

    public IntegratorFileLogDaoImpl() {
        super(IntegratorFileLog.class);
    }

    @Override
    public IntegratorFileLog getLastFileData() {
        String queryStr = "FROM IntegratorFileLog c ORDER BY c.id DESC";

        Query query = entityManager.createQuery(queryStr);
        query.setMaxResults(1);

        return this.getSingleResultOrNull(query);

    }

    @Override
    public List<IntegratorFileLog> getFileLogHistory() {
        String queryStr = "FROM IntegratorFileLog c ORDER BY c.id DESC";

        Query query = entityManager.createQuery(queryStr);

        List<IntegratorFileLog> results = query.getResultList();

        return (results);

    }

    @Override
    public IntegratorFileLog findByFilenameAndChecksum(String filename, String checksum) {
        String queryStr = "FROM IntegratorFileLog c WHERE c.filename = ?1 and c.checksum = ?2 ORDER BY c.id DESC";

        Query query = entityManager.createQuery(queryStr);
        query.setParameter(1, filename);
        query.setParameter(2, checksum);

        return this.getSingleResultOrNull(query);

    }

    @Override
    public List<IntegratorFileLog> findAllWithNoCompletedIntegratorStatus() {
        String queryStr = "FROM IntegratorFileLog c WHERE c.integratorStatus IS NULL OR c.integratorStatus != 'COMPLETED' ORDER BY c.id DESC";

        Query query = entityManager.createQuery(queryStr);

        List<IntegratorFileLog> results = query.getResultList();

        return (results);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IntegratorFileLog> findAllWithNoCompletedOrErrorIntegratorStatus() {
        String queryStr = "FROM IntegratorFileLog c WHERE c.integratorStatus IS NULL OR c.integratorStatus NOT LIKE 'COMPLETED' AND c.integratorStatus NOT LIKE 'ERROR' ORDER BY c.id DESC";

        Query query = entityManager.createQuery(queryStr);

        List<IntegratorFileLog> results = query.getResultList();
        if (results == null) {
            results = Collections.emptyList();
        }

        return results;
    }

}
