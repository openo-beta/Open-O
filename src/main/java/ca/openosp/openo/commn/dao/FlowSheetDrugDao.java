//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.FlowSheetDrug;

public interface FlowSheetDrugDao extends AbstractDao<FlowSheetDrug> {
    FlowSheetDrug getFlowSheetDrug(Integer id);

    List<FlowSheetDrug> getFlowSheetDrugs(String flowsheet, Integer demographic);
}
