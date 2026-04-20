//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CVCMedicationLotNumber;

public interface CVCMedicationLotNumberDao extends AbstractDao<CVCMedicationLotNumber> {

    public void removeAll();

    public CVCMedicationLotNumber findByLotNumber(String lotNumber);

    public List<CVCMedicationLotNumber> query(String term);
}
