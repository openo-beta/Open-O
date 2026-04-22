//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ReportConfig;

public interface ReportConfigDao extends AbstractDao<ReportConfig> {
    List<ReportConfig> findByReportIdAndNameAndCaptionAndTableNameAndSave(int reportId, String name, String caption, String tableName, String save);

    List<ReportConfig> findByReportIdAndSaveAndGtOrderNo(int reportId, String save, int orderNo);
}
