//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.BatchBilling;

public interface BatchBillingDAO extends AbstractDao<BatchBilling> {
    List<BatchBilling> find(Integer demographicNo, String service_code);

    List<BatchBilling> findByProvider(String providerNo);

    List<BatchBilling> findByProvider(String providerNo, String service_code);

    List<BatchBilling> findByServiceCode(String service_code);

    List<BatchBilling> findAll();

    List<String> findDistinctServiceCodes();
}
