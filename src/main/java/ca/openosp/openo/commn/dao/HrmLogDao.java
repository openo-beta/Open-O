//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.HrmLog;

public interface HrmLogDao extends AbstractDao<HrmLog> {
    List<HrmLog> query(int start, int length, String orderColumn, String orderDirection, String providerNo);
}
