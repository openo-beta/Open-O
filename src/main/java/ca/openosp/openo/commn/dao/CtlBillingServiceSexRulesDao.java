//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlBillingServiceSexRules;

public interface CtlBillingServiceSexRulesDao extends AbstractDao<CtlBillingServiceSexRules> {
    List<CtlBillingServiceSexRules> findByServiceCode(String serviceCode);
}
