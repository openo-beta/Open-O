//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CaisiFormData;

public interface CaisiFormDataDao extends AbstractDao<CaisiFormData> {
    List<CaisiFormData> findByInstanceId(Integer instanceId);

    List<CaisiFormData> find(Integer instanceId, Integer pageNumber, Integer sectionId, Integer questionId);
}
