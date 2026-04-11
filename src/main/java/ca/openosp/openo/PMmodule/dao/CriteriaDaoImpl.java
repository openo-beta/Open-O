//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.PMmodule.model.Criteria;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class CriteriaDaoImpl extends AbstractDaoImpl<Criteria> implements CriteriaDao {

    public CriteriaDaoImpl() {
        super(Criteria.class);
    }

    public List<Criteria> getCriteriaByTemplateId(Integer templateId) {
        Query q = entityManager.createQuery("select c from Criteria c where c.templateId=?1");
        q.setParameter(1, templateId);

        @SuppressWarnings("unchecked")
        List<Criteria> results = q.getResultList();

        return results;
    }

    public Criteria getCriteriaByTemplateIdVacancyIdTypeId(Integer templateId, Integer vacancyId, Integer typeId) {
        if (templateId != null && vacancyId != null) {
            Query q = entityManager.createQuery("select c from Criteria c where c.templateId=?1 and c.criteriaTypeId=?2 and c.vacancyId=?3");
            q.setParameter(1, templateId);
            q.setParameter(2, typeId);
            q.setParameter(3, vacancyId);
            return this.getSingleResultOrNull(q);
        } else if (templateId == null && vacancyId != null) {
            Query q = entityManager.createQuery("select c from Criteria c where c.templateId IS NULL and c.criteriaTypeId=?1 and c.vacancyId=?2");
            q.setParameter(1, typeId);
            q.setParameter(2, vacancyId);
            return this.getSingleResultOrNull(q);
        } else if (templateId != null && vacancyId == null) {
            Query q = entityManager.createQuery("select c from Criteria c where c.templateId=?1 and c.criteriaTypeId=?2 and c.vacancyId is null");
            q.setParameter(1, templateId);
            q.setParameter(2, typeId);
            return this.getSingleResultOrNull(q);
        } else {
            return null;
        }


    }

    public List<Criteria> getCriteriasByVacancyId(Integer vacancyId) {
        Query q = entityManager.createQuery("select c from Criteria c where c.vacancyId=?1");
        q.setParameter(1, vacancyId);

        @SuppressWarnings("unchecked")
        List<Criteria> results = q.getResultList();

        return results;
    }

    public List<Criteria> getRefinedCriteriasByVacancyId(Integer vacancyId) {
        Query q = entityManager.createQuery("select c from Criteria c where c.canBeAdhoc!=?1 and c.vacancyId=?2");
        q.setParameter(1, 0);//canBeAdhoc=0 means don't appear in vacancy.
        q.setParameter(2, vacancyId);

        @SuppressWarnings("unchecked")
        List<Criteria> results = q.getResultList();

        return results;
    }

    public List<Criteria> getRefinedCriteriasByTemplateId(Integer templateId) {
        Query q = entityManager.createQuery("select c from Criteria c where c.canBeAdhoc!=?1 and c.templateId=?2");
        q.setParameter(1, 0); //canBeAdhoc=0 means don't appear in vacancy.
        q.setParameter(2, templateId);

        @SuppressWarnings("unchecked")
        List<Criteria> results = q.getResultList();

        return results;
    }


}
