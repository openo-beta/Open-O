//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlBillingServicePremium;

public interface CtlBillingServicePremiumDao extends AbstractDao<CtlBillingServicePremium> {
    List<CtlBillingServicePremium> findByServiceCode(String serviceCode);

    List<CtlBillingServicePremium> findByStatus(String status);

    List<Object[]> search_ctlpremium(String status);

    List<CtlBillingServicePremium> findByServceCodes(List<String> serviceCodes);
}
