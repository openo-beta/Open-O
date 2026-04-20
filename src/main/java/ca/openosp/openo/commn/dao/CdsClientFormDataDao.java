//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CdsClientFormData;

public interface CdsClientFormDataDao extends AbstractDao<CdsClientFormData> {
    List<CdsClientFormData> findByQuestion(Integer cdsClientFormId, String question);

    CdsClientFormData findByAnswer(Integer cdsClientFormId, String answer);
}
