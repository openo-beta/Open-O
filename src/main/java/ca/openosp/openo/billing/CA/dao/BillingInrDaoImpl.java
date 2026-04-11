//CHECKSTYLE:OFF


package ca.openosp.openo.billing.CA.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.billing.CA.model.BillingInr;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class BillingInrDaoImpl extends AbstractDaoImpl<BillingInr> implements BillingInrDao {

    public BillingInrDaoImpl() {
        super(BillingInr.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object[]> search_inrbilling_dt_billno(Integer billingInrNo) {
        String sql = "from BillingInr b, Demographic d where d.DemographicNo=b.demographicNo and b.id=?1 and b.status<>'D'";
        Query q = entityManager.createQuery(sql);
        q.setParameter(1, billingInrNo);

        List<Object[]> results = q.getResultList();

        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BillingInr> findCurrentByProviderNo(String providerNo) {
        String sql = "select b from BillingInr b where b.providerNo like ?1 and b.status<>'D'";
        Query q = entityManager.createQuery(sql);
        q.setParameter(1, providerNo);

        List<BillingInr> results = q.getResultList();

        return results;
    }
}
