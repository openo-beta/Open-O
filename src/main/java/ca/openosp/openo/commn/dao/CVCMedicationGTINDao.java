//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CVCMedicationGTIN;

public interface CVCMedicationGTINDao extends AbstractDao<CVCMedicationGTIN> {

    public void removeAll();

    public List<CVCMedicationGTIN> query(String term);
}
