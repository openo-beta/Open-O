//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.EncounterTemplate;

public interface EncounterTemplateDao extends AbstractDao<EncounterTemplate> {

    List<EncounterTemplate> findAll();

    List<EncounterTemplate> findByName(String name);

    List<EncounterTemplate> findByName(String name, Integer startIndex, Integer itemsToReturn);
}
