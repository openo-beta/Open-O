//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CVCMedication;

public interface CVCMedicationDao extends AbstractDao<CVCMedication> {

    public List<CVCMedication> findByDIN(String din);

    public CVCMedication findBySNOMED(String conceptId);

    public void removeAll();
}
