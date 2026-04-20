//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.casemgmt.model.CaseManagementCPP;
import ca.openosp.openo.casemgmt.model.CaseManagementNote;
import ca.openosp.openo.commn.model.EChart;

public interface EChartDao extends AbstractDao<EChart> {
    EChart getLatestChart(int demographicNo);

    String saveEchart(CaseManagementNote note, CaseManagementCPP cpp, String userName, String lastStr);

    void updateEchartOngoing(CaseManagementCPP cpp);

    void saveCPPIntoEchart(CaseManagementCPP cpp, String providerNo);

    Integer getMaxIdForDemographic(Integer demoNo);

    List<EChart> getChartsForDemographic(Integer demoNo);

    List<EChart> findByDemoIdAndSubject(Integer demoNo, String subj);
}
