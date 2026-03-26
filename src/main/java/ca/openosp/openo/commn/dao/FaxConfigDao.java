//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.FaxConfig;

public interface FaxConfigDao extends AbstractDao<FaxConfig> {
    FaxConfig getConfigByNumber(String number);

    FaxConfig getActiveConfigByNumber(String number);
}