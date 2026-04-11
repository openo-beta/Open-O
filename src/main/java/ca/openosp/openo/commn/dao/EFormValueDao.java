//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.EFormValue;

public interface EFormValueDao extends AbstractDao<EFormValue> {
    List<EFormValue> findByDemographicId(Integer demographicId);

    List<EFormValue> findByApptNo(int apptNo);

    List<EFormValue> findByFormDataId(int fdid);

    EFormValue findByFormDataIdAndKey(int fdid, String varName);

    List<EFormValue> findByFormDataIdList(List<Integer> fdids);

    List<String> findAllVarNamesForEForm(Integer fid);
}
