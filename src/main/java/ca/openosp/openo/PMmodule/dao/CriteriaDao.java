//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.Criteria;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface CriteriaDao extends AbstractDao<Criteria> {
    public List<Criteria> getCriteriaByTemplateId(Integer templateId);

    public Criteria getCriteriaByTemplateIdVacancyIdTypeId(Integer templateId, Integer vacancyId, Integer typeId);

    public List<Criteria> getCriteriasByVacancyId(Integer vacancyId);

    public List<Criteria> getRefinedCriteriasByVacancyId(Integer vacancyId);

    public List<Criteria> getRefinedCriteriasByTemplateId(Integer templateId);


}
 