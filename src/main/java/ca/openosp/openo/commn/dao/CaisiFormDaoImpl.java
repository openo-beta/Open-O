//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.CaisiForm;
import org.springframework.stereotype.Repository;

@Repository
public class CaisiFormDaoImpl extends AbstractDaoImpl<CaisiForm> implements CaisiFormDao {

    public CaisiFormDaoImpl() {
        super(CaisiForm.class);
    }

    public List<CaisiForm> getActiveForms() {
        Query query = entityManager.createQuery("SELECT x FROM CaisiForm x  where x.status = 1 order by x.description ASC ");
        @SuppressWarnings("unchecked")
        List<CaisiForm> results = query.getResultList();
        return results;
    }

    public List<CaisiForm> getCaisiForms() {
        Query query = entityManager.createQuery("SELECT x FROM CaisiForm x");
        @SuppressWarnings("unchecked")
        List<CaisiForm> results = query.getResultList();
        return results;
    }

    public void updateStatus(Integer formId, Integer status) {
        CaisiForm form = find(formId);
        if (form != null) {
            form.setStatus(status);
            merge(form);
        }
    }

    public List<CaisiForm> getCaisiForms(Integer formId, Integer clientId) {
        Query query = entityManager.createQuery("SELECT x FROM CaisiForm x where x.formId = ?1 and x.clientId = ?2");
        query.setParameter(1, formId);
        query.setParameter(2, clientId);
        @SuppressWarnings("unchecked")
        List<CaisiForm> results = query.getResultList();
        return results;
    }

    public List<CaisiForm> getCaisiFormsByClientId(Integer clientId) {
        Query query = entityManager.createQuery("SELECT x FROM CaisiForm x where x.clientId = ?1");
        query.setParameter(1, clientId);
        @SuppressWarnings("unchecked")
        List<CaisiForm> results = query.getResultList();
        return results;
    }

    public List<CaisiForm> findActiveByFacilityIdOrNull(Integer facilityId) {
        Query query = entityManager.createQuery("SELECT x FROM CaisiForm x where x.status=1 and (x.facilityId is null or x.facilityId = ?1)");
        query.setParameter(1, facilityId);
        @SuppressWarnings("unchecked")
        List<CaisiForm> results = query.getResultList();
        return results;
    }
}
