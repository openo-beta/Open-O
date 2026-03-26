//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.VacancyTemplate;
import ca.openosp.openo.commn.dao.AbstractDao;

public interface VacancyTemplateDao extends AbstractDao<VacancyTemplate> {

    public void saveVacancyTemplate(VacancyTemplate obj);

    public void mergeVacancyTemplate(VacancyTemplate obj);

    public VacancyTemplate getVacancyTemplate(Integer templateId);

    public List<VacancyTemplate> getVacancyTemplateByWlProgramId(Integer wlProgramId);

    public List<VacancyTemplate> getActiveVacancyTemplatesByWlProgramId(Integer wlProgramId);
}
