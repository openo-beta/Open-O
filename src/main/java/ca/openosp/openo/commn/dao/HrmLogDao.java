//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.HrmLog;

public interface HrmLogDao extends AbstractDao<HrmLog> {
    List<HrmLog> query(int start, int length, String orderColumn, String orderDirection, String providerNo);

    /**
     * Counts the rows {@link #query} pages through for the same provider limit.
     *
     * @param providerNo String provider who started the transactions, null for all providers
     * @return long number of HrmLog rows
     */
    long count(String providerNo);
}
