//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.BillingCdmServiceCodes;
import org.springframework.stereotype.Repository;

@Repository
public class BillingCdmServiceCodesDaoImpl extends AbstractDaoImpl<BillingCdmServiceCodes> implements BillingCdmServiceCodesDao {

    public BillingCdmServiceCodesDaoImpl() {
        super(BillingCdmServiceCodes.class);
    }

    @SuppressWarnings("unchecked")
    public List<BillingCdmServiceCodes> findAll() {
        Query query = entityManager.createQuery("FROM " + modelClass.getSimpleName());
        return query.getResultList();
    }

}
