//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.RaHeader;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class RaHeaderDaoImpl extends AbstractDaoImpl<RaHeader> implements RaHeaderDao {

    public RaHeaderDaoImpl() {
        super(RaHeader.class);
    }

    @Override
    public List<RaHeader> findCurrentByFilenamePaymentDate(String filename, String paymentDate) {
        Query query = entityManager.createQuery("SELECT r from RaHeader r WHERE r.filename = ?1 and r.paymentDate = ?2 and status != ?3 ORDER BY r.paymentDate");
        query.setParameter(1, filename);
        query.setParameter(2, paymentDate);
        query.setParameter(3, "D");
        return query.getResultList();
    }

    @Override
    public List<RaHeader> findByFilenamePaymentDate(String filename, String paymentDate) {
        Query query = entityManager.createQuery("SELECT r from RaHeader r WHERE r.filename = ?1 and r.paymentDate = ?2 ORDER BY r.paymentDate");
        query.setParameter(1, filename);
        query.setParameter(2, paymentDate);
        return query.getResultList();
    }

    @Override
    public List<RaHeader> findAllExcludeStatus(String status) {
        Query query = entityManager.createQuery("SELECT r FROM RaHeader r WHERE r.status != ?1 ORDER BY r.paymentDate DESC, r.readDate DESC");
        query.setParameter(1, status);
        return query.getResultList();
    }

    @Override
    public List<RaHeader> findByHeaderDetailsAndProviderMagic(String status, String providerNo) {
        String sql =
                "SELECT r " +
                        "FROM RaHeader r, RaDetail t, Provider p " +
                        "WHERE r.id = t.raHeaderNo " +
                        "AND p.OhipNo = t.providerOhipNo " +
                        "AND r.status <> ?1 " +
                        "AND (" +
                        "   p.ProviderNo = ?2" +
                        "   OR p.Team = (" +
                        "       SELECT pp.Team FROM Provider pp WHERE pp.ProviderNo = ?2 " +
                        "   ) " +
                        ") GROUP BY r.id" +
                        " ORDER BY r.paymentDate DESC, r.readDate DESC";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, status);
        query.setParameter(2, providerNo);
        return query.getResultList();
    }

    @Override
    public List<RaHeader> findByStatusAndProviderMagic(String status, String providerNo) {
        String sql = "SELECT r " +
                "FROM RaHeader r, RaDetail t, Provider p " +
                "WHERE r.id = t.raHeaderNo " +
                "AND p.OhipNo = t.providerOhipNo " +
                "AND r.status <> ?1 " +
                "AND EXISTS (" +
                "   FROM ProviderSite s " +
                "   WHERE p.ProviderNo = s.id.providerNo " +
                "   AND s.id.siteId IN (" +
                "       SELECT ss.id.siteId FROM ProviderSite ss WHERE ss.id.providerNo = ?2 " +
                "   ) " +
                ") " +
                "GROUP BY r.id " +
                "ORDER BY r.paymentDate DESC, r.readDate DESC";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, status);
        query.setParameter(2, providerNo);
        return query.getResultList();
    }

    @Override
    public List<Object[]> findHeadersAndProvidersById(Integer id) {
        String sql = "FROM RaDetail r, Provider p " +
                "WHERE p.OhipNo = r.providerOhipNo " +
                "AND r.raHeaderNo = ?1 " +
                "GROUP BY r.providerOhipNo";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, id);
        return query.getResultList();
    }
}
