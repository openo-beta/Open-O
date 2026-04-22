//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.HashMap;
import java.util.List;

import ca.openosp.openo.commn.model.PreventionExt;

public interface PreventionExtDao extends AbstractDao<PreventionExt> {

    public List<PreventionExt> findByPreventionId(Integer preventionId);

    public List<PreventionExt> findByKeyAndValue(String key, String value);

    public List<PreventionExt> findByPreventionIdAndKey(Integer preventionId, String key);

    public HashMap<String, String> getPreventionExt(Integer preventionId);
}
 