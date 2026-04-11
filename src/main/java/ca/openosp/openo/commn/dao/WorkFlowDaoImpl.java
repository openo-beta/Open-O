//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.WorkFlow;
import org.springframework.stereotype.Repository;

@Repository
public class WorkFlowDaoImpl extends AbstractDaoImpl<WorkFlow> implements WorkFlowDao {

    public WorkFlowDaoImpl() {
        super(WorkFlow.class);
    }

    public List<WorkFlow> findByWorkflowType(String type) {
        Query q = entityManager.createQuery("SELECT w FROM WorkFlow w WHERE w.workflowType=?1");
        q.setParameter(1, type);

        @SuppressWarnings("unchecked")
        List<WorkFlow> results = q.getResultList();

        return results;
    }

    public List<WorkFlow> findActiveByWorkflowType(String type) {
        Query q = entityManager.createQuery("SELECT w FROM WorkFlow w WHERE w.workflowType=?1 AND w.currentState <> ?2");
        q.setParameter(1, type);
        q.setParameter(2, "C");

        @SuppressWarnings("unchecked")
        List<WorkFlow> results = q.getResultList();

        return results;
    }

    public List<WorkFlow> findActiveByWorkflowTypeAndDemographicNo(String type, String demographicNo) {
        Query q = entityManager.createQuery("SELECT w FROM WorkFlow w WHERE w.workflowType=?1 AND w.demographicNo=?2 AND w.currentState <> ?3");
        q.setParameter(1, type);
        q.setParameter(2, demographicNo);
        q.setParameter(3, "C");

        @SuppressWarnings("unchecked")
        List<WorkFlow> results = q.getResultList();

        return results;
    }
}
