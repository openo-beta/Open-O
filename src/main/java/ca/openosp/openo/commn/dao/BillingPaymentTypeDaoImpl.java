//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.BillingPaymentType;
import org.springframework.stereotype.Repository;

@Repository
public class BillingPaymentTypeDaoImpl extends AbstractDaoImpl<BillingPaymentType> implements BillingPaymentTypeDao {

    public BillingPaymentTypeDaoImpl() {
        super(BillingPaymentType.class);
    }

    @SuppressWarnings("unchecked")
    public List<BillingPaymentType> findAll() {
        Query query = entityManager.createQuery("SELECT x FROM " + modelClass.getSimpleName() + " x");
        List<BillingPaymentType> results = query.getResultList();
        return results;
    }

    public Integer findIdByName(String name) {
        Query query = entityManager.createQuery("SELECT x.id FROM " + modelClass.getSimpleName() + " x WHERE x.paymentType = ?1");
        query.setParameter(1, name);
        query.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();
        if (results.size() == 1) return (results.get(0));
        else if (results.size() == 0) return (null);

        return null;
    }

    public BillingPaymentType getPaymentTypeByName(String typeName) {
        Query query = entityManager.createQuery("from BillingPaymentType bpt where bpt.paymentType = :typeName");
        query.setParameter("typeName", typeName);
        return this.getSingleResultOrNull(query);
    }

}
