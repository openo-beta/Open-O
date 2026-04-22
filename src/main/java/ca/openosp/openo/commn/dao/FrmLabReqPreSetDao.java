//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Properties;

import ca.openosp.openo.commn.model.FrmLabReqPreSet;

public interface FrmLabReqPreSetDao extends AbstractDao<FrmLabReqPreSet> {
    Properties fillPropertiesByLabType(String labType, Properties prop);
}
