//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CtlBillingServiceSexRules;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class CtlBillingServiceSexRulesDaoImpl extends AbstractDaoImpl<CtlBillingServiceSexRules> implements CtlBillingServiceSexRulesDao {

    public CtlBillingServiceSexRulesDaoImpl() {
        super(CtlBillingServiceSexRules.class);
    }

    @Override
    public List<CtlBillingServiceSexRules> findByServiceCode(String serviceCode) {
        Query query = createQuery("r", "r.serviceCode = ?1");
        query.setParameter(1, serviceCode);
        return query.getResultList();
    }
}
