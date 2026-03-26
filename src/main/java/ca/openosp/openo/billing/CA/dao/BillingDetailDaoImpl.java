//CHECKSTYLE:OFF


package ca.openosp.openo.billing.CA.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.billing.CA.model.BillingDetail;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class BillingDetailDaoImpl extends AbstractDaoImpl<BillingDetail> implements BillingDetailDao {

    public BillingDetailDaoImpl() {
        super(BillingDetail.class);
    }

    @Override
    public List<BillingDetail> findByBillingNo(int billingNo) {
        Query q = entityManager.createQuery("select x from BillingDetail x where x.billingNo=?1");
        q.setParameter(1, billingNo);
        List<BillingDetail> results = q.getResultList();
        return results;
    }

    @Override
    public List<BillingDetail> findByBillingNoAndStatus(Integer billingNo, String status) {
        Query query = createQuery("bd", "bd.billingNo = :billingNo AND bd.status = :status");
        query.setParameter("billingNo", billingNo);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public List<BillingDetail> findByBillingNo(Integer billingNo) {
        Query query = createQuery("bd", "bd.billingNo = :billingNo AND bd.status <> 'D' ORDER BY service_code");
        query.setParameter("billingNo", billingNo);
        return query.getResultList();
    }
}
