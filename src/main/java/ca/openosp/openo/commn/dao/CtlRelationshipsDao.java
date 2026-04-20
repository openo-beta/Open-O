//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlRelationships;

public interface CtlRelationshipsDao extends AbstractDao<CtlRelationships> {
    List<CtlRelationships> findAllActive();

    CtlRelationships findByValue(String value);
}
