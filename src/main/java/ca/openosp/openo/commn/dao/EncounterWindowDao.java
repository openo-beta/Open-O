//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.EncounterWindow;

public interface EncounterWindowDao extends AbstractDao<EncounterWindow> {
    EncounterWindow findByProvider(String providerNo);
}
