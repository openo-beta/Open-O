//CHECKSTYLE:OFF


package ca.openosp.openo.billing.CA.dao;

import java.util.List;

import ca.openosp.openo.billing.CA.model.BillingInr;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface BillingInrDao extends AbstractDao<BillingInr> {

    public List<Object[]> search_inrbilling_dt_billno(Integer billingInrNo);

    public List<BillingInr> findCurrentByProviderNo(String providerNo);
}
