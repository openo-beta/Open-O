//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ORNCkdScreeningReportLog;

public interface ORNCkdScreeningReportLogDao extends AbstractDao<ORNCkdScreeningReportLog> {
    List<ORNCkdScreeningReportLog> getAllReports();
}
