//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlBillingServiceAgeRules;

public interface CtlBillingServiceAgeRulesDao extends AbstractDao<CtlBillingServiceAgeRules> {
    List<CtlBillingServiceAgeRules> findByServiceCode(String serviceCode);
}
