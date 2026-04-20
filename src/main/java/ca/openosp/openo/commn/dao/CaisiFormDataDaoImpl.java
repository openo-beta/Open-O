//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CaisiFormData;
import org.springframework.stereotype.Repository;

@Repository
public class CaisiFormDataDaoImpl extends AbstractDaoImpl<CaisiFormData> implements CaisiFormDataDao {

    public CaisiFormDataDaoImpl() {
        super(CaisiFormData.class);
    }

    public List<CaisiFormData> findByInstanceId(Integer instanceId) {
        Query query = entityManager.createQuery("SELECT f FROM CaisiFormData f where f.instanceId = ?1");
        query.setParameter(1, instanceId);

        @SuppressWarnings("unchecked")
        List<CaisiFormData> result = query.getResultList();

        return result;
    }

    public List<CaisiFormData> find(Integer instanceId, Integer pageNumber, Integer sectionId, Integer questionId) {
        Query query = entityManager.createQuery("SELECT f FROM CaisiFormData f where f.instanceId = ?1 and f.pageNumber = ?2 and f.sectionId = ?3 and f.questionId = ?4");
        query.setParameter(1, instanceId);
        query.setParameter(2, pageNumber);
        query.setParameter(3, sectionId);
        query.setParameter(4, questionId);

        @SuppressWarnings("unchecked")
        List<CaisiFormData> result = query.getResultList();

        return result;
    }
}
