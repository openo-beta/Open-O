//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlBillingType;

public interface CtlBillingTypeDao extends AbstractDao<CtlBillingType> {
    List<CtlBillingType> findByServiceType(String serviceType);
}
