//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.BillingPaymentType;

public interface BillingPaymentTypeDao extends AbstractDao<BillingPaymentType> {
    List<BillingPaymentType> findAll();

    Integer findIdByName(String name);

    BillingPaymentType getPaymentTypeByName(String typeName);
}
