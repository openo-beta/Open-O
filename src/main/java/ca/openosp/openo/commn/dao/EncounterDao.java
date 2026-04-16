//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Encounter;

public interface EncounterDao extends AbstractDao<Encounter> {
    List<Encounter> findByDemographicNo(Integer demographicNo);
}
