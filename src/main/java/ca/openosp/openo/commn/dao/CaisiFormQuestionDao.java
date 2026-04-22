//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CaisiFormQuestion;

public interface CaisiFormQuestionDao extends AbstractDao<CaisiFormQuestion> {
    List<CaisiFormQuestion> findByFormId(Integer formId);
}
