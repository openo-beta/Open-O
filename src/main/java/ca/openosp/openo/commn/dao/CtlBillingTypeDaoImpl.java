//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CtlBillingType;
import org.springframework.stereotype.Repository;

@Repository
public class CtlBillingTypeDaoImpl extends AbstractDaoImpl<CtlBillingType> implements CtlBillingTypeDao {

    public CtlBillingTypeDaoImpl() {
        super(CtlBillingType.class);
    }

    @Override
    public List<CtlBillingType> findByServiceType(String serviceType) {
        Query query = entityManager.createQuery("select b from CtlBillingType b where b.id like ?1");
        query.setParameter(1, serviceType);

        @SuppressWarnings("unchecked")
        List<CtlBillingType> results = query.getResultList();

        return results;
    }
}
