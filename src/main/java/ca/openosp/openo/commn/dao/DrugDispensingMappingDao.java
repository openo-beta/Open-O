//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DrugDispensingMapping;

public interface DrugDispensingMappingDao extends AbstractDao<DrugDispensingMapping> {
    DrugDispensingMapping findMappingByDin(String din);

    DrugDispensingMapping findMapping(String din, String duration, String durUnit, String freqCode, String quantity, Float takeMin, Float takeMax);
}
