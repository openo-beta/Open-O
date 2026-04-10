//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CdsClientFormData;
import org.springframework.stereotype.Repository;

@Repository
public class CdsClientFormDataDaoImpl extends AbstractDaoImpl<CdsClientFormData> implements CdsClientFormDataDao {

    public CdsClientFormDataDaoImpl() {
        super(CdsClientFormData.class);
    }

    public List<CdsClientFormData> findByQuestion(Integer cdsClientFormId, String question) {

        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.cdsClientFormId=?1 and x.question=?2";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, cdsClientFormId);
        query.setParameter(2, question);

        @SuppressWarnings("unchecked")
        List<CdsClientFormData> results = query.getResultList();

        return (results);
    }

    /**
     * Generally speaking this method is good for getting a form if
     * the answer is a CDS category, i.e. "019-04", this method is not
     * useful for answers which are random strings like "days hosipitalised"
     * where the answer may conflict with other numeric answers.
     */
    public CdsClientFormData findByAnswer(Integer cdsClientFormId, String answer) {

        String sqlCommand = "select x from " + modelClass.getSimpleName() + " x where x.cdsClientFormId=?1 and x.answer=?2";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, cdsClientFormId);
        query.setParameter(2, answer);

        return (getSingleResultOrNull(query));
    }

}
