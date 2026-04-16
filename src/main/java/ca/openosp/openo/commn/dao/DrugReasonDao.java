//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.DrugReason;

public interface DrugReasonDao extends AbstractDao<DrugReason> {

    boolean addNewDrugReason(DrugReason d);

    Boolean hasReason(Integer drugId, String codingSystem, String code, boolean onlyActive);

    List<DrugReason> getReasonsForDrugID(Integer drugId, boolean onlyActive);

    List<DrugReason> getReasonsByIcd9CodeAndDemographicNo(String icd9Code, Integer demographicNo);
}
