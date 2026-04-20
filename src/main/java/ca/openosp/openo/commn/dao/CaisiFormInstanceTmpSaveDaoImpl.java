//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CaisiFormInstance;
import ca.openosp.openo.commn.model.CaisiFormInstanceTmpSave;
import org.springframework.stereotype.Repository;

@Repository
public class CaisiFormInstanceTmpSaveDaoImpl extends AbstractDaoImpl<CaisiFormInstanceTmpSave> implements CaisiFormInstanceTmpSaveDao {

    public CaisiFormInstanceTmpSaveDaoImpl() {
        super(CaisiFormInstanceTmpSave.class);
    }

    public List<CaisiFormInstance> getTmpForms(Integer instanceId, Integer formId, Integer clientId, Integer providerNo) {
        Query query = entityManager.createQuery("select f from CaisiFormInstanceTmpSave f where f.instanceId = ?1 and f.formId = ?2 and f.clientId = ?3 and f.userId = ?4 order by f.dateCreated DESC");
        query.setParameter(1, instanceId);
        query.setParameter(2, formId);
        query.setParameter(3, clientId);
        query.setParameter(4, providerNo);
        @SuppressWarnings("unchecked")
        List<CaisiFormInstance> result = query.getResultList();

        return result;
    }
}
