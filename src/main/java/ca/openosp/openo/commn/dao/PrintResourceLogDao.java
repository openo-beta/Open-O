//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.PrintResourceLog;

public interface PrintResourceLogDao extends AbstractDao<PrintResourceLog> {
    List<PrintResourceLog> findByResource(String resourceName, String resourceId);
}
