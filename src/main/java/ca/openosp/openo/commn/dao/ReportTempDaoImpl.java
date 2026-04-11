//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ReportTemp;
import org.springframework.stereotype.Repository;

@Repository
public class ReportTempDaoImpl extends AbstractDaoImpl<ReportTemp> implements ReportTempDao {

    public ReportTempDaoImpl() {
        super(ReportTemp.class);
    }

    @Override
    public List<ReportTemp> findAll() {
        String sql = "select x from ReportTemp x";
        Query query = entityManager.createQuery(sql);

        @SuppressWarnings("unchecked")
        List<ReportTemp> results = query.getResultList();
        return results;
    }

    @Override
    public List<ReportTemp> findGreateThanEdb(Date edb, int offset, int limit) {
        String sql = "select x from ReportTemp x where x.id.edb >= ?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, edb);
        query.setMaxResults(limit);
        query.setFirstResult(offset);

        @SuppressWarnings("unchecked")
        List<ReportTemp> results = query.getResultList();
        return results;
    }
}
