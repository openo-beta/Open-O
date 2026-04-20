//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ReportProvider;
import org.springframework.stereotype.Repository;

@Repository
public class ReportProviderDaoImpl extends AbstractDaoImpl<ReportProvider> implements ReportProviderDao {

    public ReportProviderDaoImpl() {
        super(ReportProvider.class);
    }

    @Override
    public List<ReportProvider> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @Override
    public List<ReportProvider> findByAction(String action) {
        String sql = "select x from ReportProvider x where x.action=?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, action);

        List<ReportProvider> results = query.getResultList();
        return results;
    }

    @Override
    public List<ReportProvider> findByProviderNoTeamAndAction(String providerNo, String team, String action) {
        String sql = "select x from ReportProvider x where x.providerNo=?1 and x.team=?2 and x.action=?3";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, providerNo);
        query.setParameter(2, team);
        query.setParameter(3, action);

        List<ReportProvider> results = query.getResultList();
        return results;
    }

    @Override
    public List<Object[]> search_reportprovider(String action) {
        String sql = "from ReportProvider r, Provider p where r.providerNo=p.ProviderNo and r.status<>'D' and r.action=?1 order by r.team";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, action);

        List<Object[]> results = query.getResultList();
        return results;
    }

    @Override
    public List<Object[]> search_reportprovider(String action, String providerNo) {
        String sql = "from ReportProvider r, Provider p where r.providerNo=p.ProviderNo and r.status<>'D' and r.action=?1 and p.ProviderNo like ?2 order by r.team";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, action);
        query.setParameter(2, providerNo);

        List<Object[]> results = query.getResultList();
        return results;
    }
}
