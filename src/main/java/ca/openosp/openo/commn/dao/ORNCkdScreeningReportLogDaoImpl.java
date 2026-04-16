//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ORNCkdScreeningReportLog;
import org.springframework.stereotype.Repository;

@Repository
public class ORNCkdScreeningReportLogDaoImpl extends AbstractDaoImpl<ORNCkdScreeningReportLog> implements ORNCkdScreeningReportLogDao {

    public ORNCkdScreeningReportLogDaoImpl() {
        super(ORNCkdScreeningReportLog.class);
    }

    @Override
    public List<ORNCkdScreeningReportLog> getAllReports() {
        Query query = entityManager.createQuery("SELECT x FROM ORNCkdScreeningReportLog x ORDER BY x.lastUpdateDate DESC");
        @SuppressWarnings("unchecked")
        List<ORNCkdScreeningReportLog> results = query.getResultList();
        return results;
    }
}
