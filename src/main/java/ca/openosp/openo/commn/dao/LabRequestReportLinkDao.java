//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.LabRequestReportLink;

public interface LabRequestReportLinkDao extends AbstractDao<LabRequestReportLink> {
    List<LabRequestReportLink> findByReportTableAndReportId(String reportTable, int reportId);

    List<LabRequestReportLink> findByRequestTableAndRequestId(String requestTable, int requestId);
}
