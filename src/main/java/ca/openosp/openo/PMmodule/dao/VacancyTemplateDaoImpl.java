//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.PMmodule.model.VacancyTemplate;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class VacancyTemplateDaoImpl extends AbstractDaoImpl<VacancyTemplate> implements VacancyTemplateDao {

    public VacancyTemplateDaoImpl() {
        super(VacancyTemplate.class);
    }

    @Override
    public void saveVacancyTemplate(VacancyTemplate obj) {
        persist(obj);
    }

    @Override
    public void mergeVacancyTemplate(VacancyTemplate obj) {
        merge(obj);
    }

    @Override
    public VacancyTemplate getVacancyTemplate(Integer templateId) {
        return find(templateId);
    }

    @Override
    public List<VacancyTemplate> getVacancyTemplateByWlProgramId(Integer wlProgramId) {
        Query query = entityManager.createQuery("select x from VacancyTemplate x where x.wlProgramId=?1");
        query.setParameter(1, wlProgramId);

        @SuppressWarnings("unchecked")
        List<VacancyTemplate> results = query.getResultList();

        return results;
    }

    @Override
    public List<VacancyTemplate> getActiveVacancyTemplatesByWlProgramId(Integer wlProgramId) {
        Query query = entityManager.createQuery("select x from VacancyTemplate x where x.wlProgramId=?1 and x.active=?2");
        query.setParameter(1, wlProgramId);
        query.setParameter(2, true);

        @SuppressWarnings("unchecked")
        List<VacancyTemplate> results = query.getResultList();

        return results;
    }
}
