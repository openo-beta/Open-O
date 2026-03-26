//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import java.util.Properties;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.FrmLabReqPreSet;
import org.springframework.stereotype.Repository;

@Repository
public class FrmLabReqPreSetDaoImpl extends AbstractDaoImpl<FrmLabReqPreSet> implements FrmLabReqPreSetDao {

    public FrmLabReqPreSetDaoImpl() {
        super(FrmLabReqPreSet.class);
    }

    @Override
    public Properties fillPropertiesByLabType(String labType, Properties prop) {
        String sql = "select frmPreset from FrmLabReqPreSet frmPreset where labType=?1 and status=?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, labType);
        query.setParameter(2, 1);

        @SuppressWarnings("unchecked")
        List<FrmLabReqPreSet> results = query.getResultList();

        if (results != null) {
            for (FrmLabReqPreSet flrp : results) {
                prop.setProperty(flrp.getPropertyName(), flrp.getPropertyValue());
            }
        }

        return prop;
    }
}
