//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.FormeCARES;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Repository
public class FormeCARESDaoImpl extends AbstractDaoImpl<FormeCARES> implements FormeCARESDao {
    protected FormeCARESDaoImpl() {
        super(FormeCARES.class);
    }

    @Override
    public List<FormeCARES> findAllByFormCreatedDateDemographicNo(Date createDate, int demographicNo) {
        String sql = "select x from " + modelClass.getSimpleName() + " x where DATE(x.formCreated) = ?1 and x.demographicNo = ?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, createDate);
        query.setParameter(2, demographicNo);
        @SuppressWarnings("unchecked")
        List<FormeCARES> formeCARESList = query.getResultList();
        if (formeCARESList == null) {
            formeCARESList = Collections.emptyList();
        }
        return formeCARESList;
    }

    @Override
    public List<FormeCARES> findAllIncompleteByDemographicNumber(int demographicNo) {
        String sql = "select x from " + modelClass.getSimpleName() + " x where x.completed = ?1 and x.demographicNo = ?2";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, false);
        query.setParameter(2, demographicNo);
        @SuppressWarnings("unchecked")
        List<FormeCARES> formeCARESList = query.getResultList();
        if (formeCARESList == null) {
            formeCARESList = Collections.emptyList();
        }
        return formeCARESList;
    }

}
