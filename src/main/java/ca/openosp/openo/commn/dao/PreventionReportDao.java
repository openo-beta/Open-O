//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.PreventionReport;

public interface PreventionReportDao extends AbstractDao<PreventionReport> {

    List<PreventionReport> getPreventionReports();
}
