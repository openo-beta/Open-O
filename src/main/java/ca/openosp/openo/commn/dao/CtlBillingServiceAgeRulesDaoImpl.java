//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CtlBillingServiceAgeRules;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class CtlBillingServiceAgeRulesDaoImpl extends AbstractDaoImpl<CtlBillingServiceAgeRules> implements CtlBillingServiceAgeRulesDao {

    public CtlBillingServiceAgeRulesDaoImpl() {
        super(CtlBillingServiceAgeRules.class);
    }

    @Override
    public List<CtlBillingServiceAgeRules> findByServiceCode(String serviceCode) {
        Query query = createQuery("r", "r.serviceCode = ?1");
        query.setParameter(1, serviceCode);
        return query.getResultList();
    }
}
