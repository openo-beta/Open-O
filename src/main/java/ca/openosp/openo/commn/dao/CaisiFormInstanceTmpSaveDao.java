//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CaisiFormInstance;
import ca.openosp.openo.commn.model.CaisiFormInstanceTmpSave;

public interface CaisiFormInstanceTmpSaveDao extends AbstractDao<CaisiFormInstanceTmpSave> {
    List<CaisiFormInstance> getTmpForms(Integer instanceId, Integer formId, Integer clientId, Integer providerNo);
}
