//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.BillingCdmServiceCodes;

public interface BillingCdmServiceCodesDao extends AbstractDao<BillingCdmServiceCodes> {
    List<BillingCdmServiceCodes> findAll();
}
