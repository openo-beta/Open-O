//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.PMmodule.model.CriteriaType;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class CriteriaTypeDaoImpl extends AbstractDaoImpl<CriteriaType> implements CriteriaTypeDao {

    public CriteriaTypeDaoImpl() {
        super(CriteriaType.class);
    }

    public List<CriteriaType> findAll() {
        Query query = entityManager.createQuery("select x from CriteriaType x");

        @SuppressWarnings("unchecked")
        List<CriteriaType> results = query.getResultList();

        return results;
    }

    public CriteriaType findByName(String fieldName) {
        Query query = entityManager.createQuery("select x from CriteriaType x where x.fieldName=?1");
        query.setParameter(1, fieldName);

        @SuppressWarnings("unchecked")
        List<CriteriaType> results = query.getResultList();

        if (results.size() > 0)
            return results.get(0);

        return null;
    }

    public List<CriteriaType> getAllCriteriaTypes() {
        Query query = entityManager.createQuery("select x from CriteriaType x where x.wlProgramId=?1 order by x.fieldType DESC");
        query.setParameter(1, 1);

        @SuppressWarnings("unchecked")
        List<CriteriaType> results = query.getResultList();

        return results;
    }

    public List<CriteriaType> getAllCriteriaTypesByWlProgramId(Integer wlProgramId) {
        Query query = entityManager.createQuery("select x from CriteriaType x where x.wlProgramId=?1 order by x.fieldType DESC");
        query.setParameter(1, wlProgramId);

        @SuppressWarnings("unchecked")
        List<CriteriaType> results = query.getResultList();

        return results;
    }
}
