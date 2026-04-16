//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.PMmodule.model.CriteriaTypeOption;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class CriteriaTypeOptionDaoImpl extends AbstractDaoImpl<CriteriaTypeOption> implements CriteriaTypeOptionDao {

    public CriteriaTypeOptionDaoImpl() {
        super(CriteriaTypeOption.class);
    }

    public List<CriteriaTypeOption> findAll() {
        Query query = entityManager.createQuery("select x from CriteriaTypeOption x");

        @SuppressWarnings("unchecked")
        List<CriteriaTypeOption> results = query.getResultList();

        return results;
    }

    public List<CriteriaTypeOption> getCriteriaTypeOptionByTypeId(Integer typeId) {
        Query query = entityManager.createQuery("select x from CriteriaTypeOption x where x.criteriaTypeId=?1");
        query.setParameter(1, typeId);

        @SuppressWarnings("unchecked")
        List<CriteriaTypeOption> results = query.getResultList();

        return results;
    }

    public CriteriaTypeOption getByValue(String optionValue) {
        Query query = entityManager.createQuery("select x from CriteriaTypeOption x where x.optionValue=?1");
        query.setParameter(1, optionValue);

        return this.getSingleResultOrNull(query);
    }

    public CriteriaTypeOption getByValueAndTypeId(String optionValue, Integer typeId) {
        Query query = entityManager.createQuery("select x from CriteriaTypeOption x where x.optionValue=?1 and x.criteriaTypeId=?2");
        query.setParameter(1, optionValue);
        query.setParameter(2, typeId);

        return this.getSingleResultOrNull(query);
    }
}
