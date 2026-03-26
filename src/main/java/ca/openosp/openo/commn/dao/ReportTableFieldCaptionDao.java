//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ReportTableFieldCaption;

public interface ReportTableFieldCaptionDao extends AbstractDao<ReportTableFieldCaption> {
    List<ReportTableFieldCaption> findByTableNameAndName(String tableName, String name);
}
