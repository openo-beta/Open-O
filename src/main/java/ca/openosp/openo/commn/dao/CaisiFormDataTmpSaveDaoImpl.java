//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CaisiFormDataTmpSave;
import org.springframework.stereotype.Repository;

@Repository
public class CaisiFormDataTmpSaveDaoImpl extends AbstractDaoImpl<CaisiFormDataTmpSave> implements CaisiFormDataTmpSaveDao {

    public CaisiFormDataTmpSaveDaoImpl() {
        super(CaisiFormDataTmpSave.class);
    }

    @Override
    public List<CaisiFormDataTmpSave> getTmpFormData(Long tmpInstanceId) {
        Query query = entityManager.createQuery("select f from CaisiFormDataTmpSave f where f.tmpInstanceId = ?1");
        query.setParameter(1, tmpInstanceId);

        @SuppressWarnings("unchecked")
        List<CaisiFormDataTmpSave> result = query.getResultList();

        return result;
    }
}
