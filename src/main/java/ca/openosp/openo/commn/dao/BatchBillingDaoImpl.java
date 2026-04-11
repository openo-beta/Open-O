//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.BatchBilling;
import org.springframework.stereotype.Repository;

@Repository
public class BatchBillingDaoImpl extends AbstractDaoImpl<BatchBilling> implements BatchBillingDAO {

    public BatchBillingDaoImpl() {
        super(BatchBilling.class);
    }

    @SuppressWarnings("unchecked")
    public List<BatchBilling> find(Integer demographicNo, String service_code) {
        Query query = entityManager.createQuery("select b from BatchBilling b where b.demographicNo = :demo and b.serviceCode = :service_code");
        query.setParameter("demo", demographicNo);
        query.setParameter("service_code", service_code);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<BatchBilling> findByProvider(String providerNo) {
        Query query = entityManager.createQuery("select b from BatchBilling b where b.billingProviderNo = :provider");
        query.setParameter("provider", providerNo);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<BatchBilling> findByProvider(String providerNo, String service_code) {
        Query query = entityManager.createQuery("select b from BatchBilling b where b.billingProviderNo = :provider and b.serviceCode = :service_code");
        query.setParameter("provider", providerNo);
        query.setParameter("service_code", service_code);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<BatchBilling> findByServiceCode(String service_code) {
        Query query = entityManager.createQuery("select b from BatchBilling b where b.serviceCode = :service_code");
        query.setParameter("service_code", service_code);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<BatchBilling> findAll() {
        Query query = entityManager.createQuery("select b from BatchBilling b");
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> findDistinctServiceCodes() {
        Query query = entityManager.createQuery("select distinct b.serviceCode from BatchBilling b");
        return query.getResultList();
    }
}
