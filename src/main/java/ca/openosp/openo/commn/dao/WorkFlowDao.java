//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.WorkFlow;

public interface WorkFlowDao extends AbstractDao<WorkFlow> {
    List<WorkFlow> findByWorkflowType(String type);

    List<WorkFlow> findActiveByWorkflowType(String type);

    List<WorkFlow> findActiveByWorkflowTypeAndDemographicNo(String type, String demographicNo);
}
