//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.CaisiFormData;
import ca.openosp.openo.commn.model.CaisiFormInstance;
import ca.openosp.openo.commn.model.Demographic;

public interface CaisiFormInstanceDao extends AbstractDao<CaisiFormInstance> {
    CaisiFormInstance getCurrentFormById(Integer formInstanceId);

    List<CaisiFormInstance> findByFormId(Integer formId);

    List<CaisiFormInstance> getForms(Integer formId, Integer clientId);

    CaisiFormInstance getLatestForm(Integer formId, Integer clientId);

    List<CaisiFormInstance> getFormsByFacility(Integer clientId, Integer facilityId);

    List<CaisiFormInstance> getCurrentForms(String formId, List<Demographic> clients);

    List<CaisiFormInstance> getForms(Long clientId);

    Integer countAnswersByQuestions(String value, Integer formId, Date startDate, Date endDate, Integer pageNumber, Integer sectionId, Integer questionId);

    List<CaisiFormData> query1(Integer formId, Date startDate, Date endDate, int pageNumber, int sectionId, int questionId);

    List<CaisiFormData> query2(Integer formId, Date startDate, Date endDate, int pageNumber, int sectionId, int questionId);
}
