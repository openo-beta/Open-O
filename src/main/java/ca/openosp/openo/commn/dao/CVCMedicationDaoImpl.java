//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CVCMedication;
import org.springframework.stereotype.Repository;

@Repository
public class CVCMedicationDaoImpl extends AbstractDaoImpl<CVCMedication> implements CVCMedicationDao {

    public CVCMedicationDaoImpl() {
        super(CVCMedication.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CVCMedication> findByDIN(String din) {
        Query query = entityManager.createQuery("SELECT x FROM CVCMedication x WHERE x.din = :din");
        query.setParameter("din", din);
        List<CVCMedication> result = query.getResultList();
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CVCMedication findBySNOMED(String conceptId) {
        Query query = entityManager.createQuery("SELECT x FROM CVCMedication x WHERE x.snomedCode = :code");
        query.setParameter("code", conceptId);
        query.setMaxResults(1);
        List<CVCMedication> result = query.getResultList();
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @Override
    public void removeAll() {
        Query query = entityManager.createQuery("DELETE FROM CVCMedication");
        query.executeUpdate();
    }
}
