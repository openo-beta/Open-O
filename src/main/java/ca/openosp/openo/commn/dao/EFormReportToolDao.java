//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.EForm;
import ca.openosp.openo.commn.model.EFormReportTool;
import ca.openosp.openo.commn.model.EFormValue;

public interface EFormReportToolDao extends AbstractDao<EFormReportTool> {

    void markLatest(Integer eformReportToolId);

    void addNew(EFormReportTool eformReportTool, EForm eform, List<String> fields, String providerNo);

    void populateReportTableItem(EFormReportTool eft, List<EFormValue> values, Integer fdid, Integer demographicNo, Date dateFormCreated, String providerNo);

    void deleteAllData(EFormReportTool eft);

    void drop(EFormReportTool eft);

    Integer getNumRecords(EFormReportTool eformReportTool);

}
