//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CtlBillingServicePremium;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class CtlBillingServicePremiumDaoImpl extends AbstractDaoImpl<CtlBillingServicePremium> implements CtlBillingServicePremiumDao {

    public CtlBillingServicePremiumDaoImpl() {
        super(CtlBillingServicePremium.class);
    }

    public List<CtlBillingServicePremium> findByServiceCode(String serviceCode) {
        Query q = entityManager.createQuery("select x from CtlBillingServicePremium x where x.serviceCode=?1");
        q.setParameter(1, serviceCode);


        List<CtlBillingServicePremium> results = q.getResultList();

        return results;
    }

    public List<CtlBillingServicePremium> findByStatus(String status) {
        Query q = entityManager.createQuery("select x from CtlBillingServicePremium x where x.status=?1");
        q.setParameter(1, status);


        List<CtlBillingServicePremium> results = q.getResultList();

        return results;
    }

    public List<Object[]> search_ctlpremium(String status) {
        Query q = entityManager.createQuery("select b.serviceCode, c.description from CtlBillingServicePremium b, BillingService c where b.serviceCode=c.serviceCode and b.status=?1");
        q.setParameter(1, status);

        List<Object[]> results = q.getResultList();

        return results;
    }

    public List<CtlBillingServicePremium> findByServceCodes(List<String> serviceCodes) {
        Query query = createQuery("p", "p.serviceCode in (?1)");
        query.setParameter(1, serviceCodes);
        return query.getResultList();
    }
}
