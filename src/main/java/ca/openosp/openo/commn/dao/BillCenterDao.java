//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.BillCenter;

public interface BillCenterDao extends AbstractDao<BillCenter> {
    List<BillCenter> findAll();

    List<BillCenter> findByBillCenterDesc(String descr);
}
