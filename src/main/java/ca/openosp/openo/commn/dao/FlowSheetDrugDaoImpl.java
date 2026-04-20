//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.FlowSheetDrug;
import org.springframework.stereotype.Repository;

@Repository
public class FlowSheetDrugDaoImpl extends AbstractDaoImpl<FlowSheetDrug> implements FlowSheetDrugDao {

    public FlowSheetDrugDaoImpl() {
        super(FlowSheetDrug.class);
    }

    public FlowSheetDrug getFlowSheetDrug(Integer id) {
        return this.find(id);
    }

    public List<FlowSheetDrug> getFlowSheetDrugs(String flowsheet, Integer demographic) {
        Query query = entityManager.createQuery("SELECT fd FROM FlowSheetDrug fd WHERE fd.flowsheet=?1 and fd.archived=0 and fd.demographicNo=?2");
        query.setParameter(1, flowsheet);
        query.setParameter(2, demographic);

        @SuppressWarnings("unchecked")
        List<FlowSheetDrug> list = query.getResultList();
        return list;
    }
}
