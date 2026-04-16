//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.BillingONCHeader1;
import ca.openosp.openo.commn.model.BillingONItem;

public interface BillingONItemDao extends AbstractDao<BillingONItem> {
    List<BillingONItem> getBillingItemByCh1Id(Integer ch1_id);

    List<BillingONItem> getActiveBillingItemByCh1Id(Integer ch1_id);

    List<BillingONCHeader1> getCh1ByDemographicNo(Integer demographic_no);

    List<BillingONItem> findByCh1Id(Integer id);

    List<BillingONItem> findByCh1IdAndStatusNotEqual(Integer chId, String string);

    List<BillingONCHeader1> getCh1ByDemographicNoSince(Integer demographic_no, Date lastUpdateDate);

    List<Integer> getDemographicNoSince(Date lastUpdateDate);
}
