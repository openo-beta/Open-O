//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ResidentOscarMsg;

public interface ResidentOscarMsgDao extends AbstractDao<ResidentOscarMsg> {
    List<ResidentOscarMsg> findBySupervisor(String supervisor);

    ResidentOscarMsg findByNoteId(Long noteId);
}
