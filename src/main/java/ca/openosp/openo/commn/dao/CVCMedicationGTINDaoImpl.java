//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CVCMedicationGTIN;
import org.springframework.stereotype.Repository;

@Repository
public class CVCMedicationGTINDaoImpl extends AbstractDaoImpl<CVCMedicationGTIN> implements CVCMedicationGTINDao {

    public CVCMedicationGTINDaoImpl() {
        super(CVCMedicationGTIN.class);
    }

    @Override
    public void removeAll() {
        Query query = entityManager.createQuery("DELETE FROM CVCMedicationGTIN");
        query.executeUpdate();
    }

    @Override
    public List<CVCMedicationGTIN> query(String term) {
        Query query = entityManager.createQuery("SELECT x FROM CVCMedicationGTIN x WHERE x.gtin = ?1");
        query.setParameter(1, term);
        List<CVCMedicationGTIN> results = query.getResultList();
        return results;
    }
}
