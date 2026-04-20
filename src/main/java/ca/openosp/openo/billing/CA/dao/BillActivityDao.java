//CHECKSTYLE:OFF


package ca.openosp.openo.billing.CA.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.billing.CA.model.BillActivity;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface BillActivityDao extends AbstractDao<BillActivity> {

    public List<BillActivity> findCurrentByMonthCodeAndGroupNo(String monthCode, String groupNo, Date updateDateTime);

    public List<BillActivity> findCurrentByDateRange(Date startDate, Date endDate);
}
