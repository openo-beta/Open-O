//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ReportByExamples;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class ReportByExamplesDaoImpl extends AbstractDaoImpl<ReportByExamples> implements ReportByExamplesDao {

    public ReportByExamplesDaoImpl() {
        super(ReportByExamples.class);
    }

    @Override
    public List<Object[]> findReportsAndProviders() {
        String sql = "FROM ReportByExamples r, Provider p "
                + "WHERE r.providerNo = p.ProviderNo "
                + "ORDER BY r.date DESC";
        Query query = entityManager.createQuery(sql);
        return query.getResultList();
    }

    @Override
    public List<Object[]> findReportsAndProviders(Date startDate, Date endDate) {
        String sql = "FROM ReportByExamples r, Provider p "
                + "WHERE r.providerNo = p.ProviderNo "
                + "AND r.date >= ?1 "
                + "AND r.date <= ?2 "
                + "ORDER BY r.date DESC";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, startDate);
        query.setParameter(2, endDate);
        return query.getResultList();
    }
}
