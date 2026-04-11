//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.PMmodule.model.CriteriaSelectionOption;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class CriteriaSelectionOptionDaoImpl extends AbstractDaoImpl<CriteriaSelectionOption> implements CriteriaSelectionOptionDao {

    public CriteriaSelectionOptionDaoImpl() {
        super(CriteriaSelectionOption.class);
    }

    public List<CriteriaSelectionOption> getCriteriaSelectedOptionsByCriteriaId(Integer criteriaId) {
        Query query = entityManager.createQuery("select x from CriteriaSelectionOption x where x.criteriaId=?1");
        query.setParameter(1, criteriaId);

        @SuppressWarnings("unchecked")
        List<CriteriaSelectionOption> results = query.getResultList();

        return results;
    }


}

