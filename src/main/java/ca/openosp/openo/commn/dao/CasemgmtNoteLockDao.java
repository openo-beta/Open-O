//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.CasemgmtNoteLock;

import java.util.List;

public interface CasemgmtNoteLockDao extends AbstractDao<CasemgmtNoteLock> {
    CasemgmtNoteLock findByNoteDemo(Integer demographicNo, Long note_id);

    void remove(String providerNo, Integer demographicNo, Long note_id);

    List<CasemgmtNoteLock> findBySession(String sessionId);
}
