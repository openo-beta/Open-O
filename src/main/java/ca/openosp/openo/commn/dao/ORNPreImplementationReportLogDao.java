//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ORNPreImplementationReportLog;

public interface ORNPreImplementationReportLogDao extends AbstractDao<ORNPreImplementationReportLog> {
    List<ORNPreImplementationReportLog> getAllReports();
}
