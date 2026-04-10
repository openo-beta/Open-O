//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.FaxClientLog;
import org.springframework.stereotype.Repository;

@Repository
public class FaxClientLogDaoImpl extends AbstractDaoImpl<FaxClientLog> implements FaxClientLogDao {

    public FaxClientLogDaoImpl() {
        super(FaxClientLog.class);
    }

    @Override
    public FaxClientLog findClientLogbyFaxId(int faxId) {
        Query query = entityManager.createQuery("select log from FaxClientLog log where log.faxId = ?1");

        // faxId is the id for an entry in the Faxes table.
        query.setParameter(1, faxId);

        return super.getSingleResultOrNull(query);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FaxClientLog> findClientLogbyFaxIds(List<Integer> faxIds) {
        if (faxIds == null || faxIds.size() == 0) {
            return Collections.emptyList();
        }

        Query query = entityManager.createNativeQuery("SELECT * FROM FaxClientLog WHERE faxId IN (?1)",
                FaxClientLog.class);
        query.setParameter(1, faxIds);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FaxClientLog> findClientLogbyRequestId(int requestId) {

        // only the most recent entries
        Query query = entityManager.createQuery(
                "select log from FaxClientLog log where log.requestId = ?1 order by log.startTime desc");

        // faxId is the id for an entry in the Faxes table.
        query.setParameter(1, requestId);
        List<FaxClientLog> results = query.getResultList();
        if (results == null) {
            results = Collections.emptyList();
        }
        return results;
    }
}
