//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CaisiFormQuestion;
import org.springframework.stereotype.Repository;

@Repository
public class CaisiFormQuestionDaoImpl extends AbstractDaoImpl<CaisiFormQuestion> implements CaisiFormQuestionDao {

    public CaisiFormQuestionDaoImpl() {
        super(CaisiFormQuestion.class);
    }

    public List<CaisiFormQuestion> findByFormId(Integer formId) {
        Query query = entityManager.createQuery("SELECT x FROM CaisiFormQuestion x where x.formId = ?1");
        query.setParameter(1, formId);

        @SuppressWarnings("unchecked")
        List<CaisiFormQuestion> results = query.getResultList();
        return results;
    }
}
