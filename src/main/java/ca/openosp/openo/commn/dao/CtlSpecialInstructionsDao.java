//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CtlSpecialInstructions;

public interface CtlSpecialInstructionsDao extends AbstractDao<CtlSpecialInstructions> {
    List<CtlSpecialInstructions> findAll();
    List<String> findDescriptionsMatching(String descQuery);
}
