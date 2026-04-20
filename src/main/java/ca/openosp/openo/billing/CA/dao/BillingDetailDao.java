//CHECKSTYLE:OFF


package ca.openosp.openo.billing.CA.dao;

import java.util.List;

import ca.openosp.openo.billing.CA.model.BillingDetail;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface BillingDetailDao extends AbstractDao<BillingDetail> {

    public List<BillingDetail> findByBillingNo(int billingNo);

    public List<BillingDetail> findByBillingNoAndStatus(Integer billingNo, String status);

    public List<BillingDetail> findByBillingNo(Integer billingNo);
}
