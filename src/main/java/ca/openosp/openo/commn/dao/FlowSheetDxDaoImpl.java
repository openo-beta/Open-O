//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.FlowSheetDx;
import org.springframework.stereotype.Repository;

@Repository
public class FlowSheetDxDaoImpl extends AbstractDaoImpl<FlowSheetDx> implements FlowSheetDxDao {

    public FlowSheetDxDaoImpl() {
        super(FlowSheetDx.class);
    }

    public List<FlowSheetDx> getFlowSheetDx(String flowsheet, Integer demographic) {
        Query query = entityManager.createQuery("select fd from FlowSheetDx fd where fd.flowsheet = ?1 and fd.archived=0 and fd.demographicNo=?2");
        query.setParameter(1, flowsheet);
        query.setParameter(2, demographic);
        @SuppressWarnings("unchecked")
        List<FlowSheetDx> fds = query.getResultList();

        return fds;
    }

    public HashMap<String, String> getFlowSheetDxMap(String flowsheet, Integer demographic) {
        List<FlowSheetDx> fldx = getFlowSheetDx(flowsheet, demographic);
        HashMap<String, String> hm = new HashMap<String, String>();

        for (FlowSheetDx fs : fldx) {
            hm.put(fs.getDxCodeType() + fs.getDxCode(), fs.getProviderNo());
        }
        return hm;
    }
}
