//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.EncounterForm;

public interface EncounterFormDao extends AbstractDao<EncounterForm> {

    public List<EncounterForm> findAll();

    public List<EncounterForm> findAllNotHidden();

    public List<EncounterForm> findByFormName(String formName);

    public List<EncounterForm> findByFormTable(String formTable);

}
