//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CaisiFormDataTmpSave;

public interface CaisiFormDataTmpSaveDao extends AbstractDao<CaisiFormDataTmpSave> {
    List<CaisiFormDataTmpSave> getTmpFormData(Long tmpInstanceId);
}
