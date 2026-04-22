//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.OLISQueryLog;

public interface OLISQueryLogDao extends AbstractDao<OLISQueryLog> {
    OLISQueryLog findByUUID(String uuid);
}
