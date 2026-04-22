//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.CVCMedicationLotNumber;
import org.springframework.stereotype.Repository;

@Repository
public class CVCMedicationLotNumberDaoImpl extends AbstractDaoImpl<CVCMedicationLotNumber>
        implements CVCMedicationLotNumberDao {

    public CVCMedicationLotNumberDaoImpl() {
        super(CVCMedicationLotNumber.class);
    }

    @Override
    public void removeAll() {
        Query query = entityManager.createQuery("DELETE FROM CVCMedicationLotNumber");
        query.executeUpdate();
    }

    @Override
    public CVCMedicationLotNumber findByLotNumber(String lotNumber) {
        Query query = entityManager.createQuery("SELECT x FROM CVCMedicationLotNumber x WHERE x.lotNumber = :ln");
        query.setParameter("ln", lotNumber);

        CVCMedicationLotNumber result = this.getSingleResultOrNull(query);

        return result;
    }

    @Override
    public List<CVCMedicationLotNumber> query(String term) {
        Query query = entityManager.createQuery("SELECT x FROM CVCMedicationLotNumber x WHERE x.lotNumber like :term");
        query.setParameter("term", term + "%");
        List<CVCMedicationLotNumber> results = query.getResultList();
        return results;
    }
}
