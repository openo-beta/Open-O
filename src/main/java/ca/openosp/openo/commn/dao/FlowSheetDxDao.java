//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.HashMap;
import java.util.List;

import ca.openosp.openo.commn.model.FlowSheetDx;

public interface FlowSheetDxDao extends AbstractDao<FlowSheetDx> {
    List<FlowSheetDx> getFlowSheetDx(String flowsheet, Integer demographic);

    HashMap<String, String> getFlowSheetDxMap(String flowsheet, Integer demographic);
}
