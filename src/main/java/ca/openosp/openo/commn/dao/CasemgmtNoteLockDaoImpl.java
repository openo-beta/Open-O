//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.CasemgmtNoteLock;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository
public class CasemgmtNoteLockDaoImpl extends AbstractDaoImpl<CasemgmtNoteLock> implements CasemgmtNoteLockDao {
    public CasemgmtNoteLockDaoImpl() {
        super(CasemgmtNoteLock.class);
    }

    @Override
    public CasemgmtNoteLock findByNoteDemo(Integer demographicNo, Long note_id) {
        Query query = entityManager.createQuery("select lock from CasemgmtNoteLock lock where lock.demographicNo = :demo and lock.noteId = :noteId");

        query.setParameter("demo", demographicNo);
        query.setParameter("noteId", note_id);

        return getSingleResultOrNull(query);
    }

    @Override
    public void remove(String providerNo, Integer demographicNo, Long note_id) {
        Query query = entityManager.createQuery("select lock from CasemgmtNoteLock lock where lock.demographicNo = :demo and lock.providerNo = :providerNo" +
                " and lock.noteId = :note_id");

        query.setParameter("demo", demographicNo);
        query.setParameter("providerNo", providerNo);
        query.setParameter("note_id", note_id);

        CasemgmtNoteLock casemgmtNoteLock = getSingleResultOrNull(query);

        if (casemgmtNoteLock != null) {
            remove(casemgmtNoteLock);
        }
    }

    @Override
    public List<CasemgmtNoteLock> findBySession(String sessionId) {
        Query query = entityManager.createQuery("select lock from CasemgmtNoteLock lock where lock.sessionId = :sessionId");

        query.setParameter("sessionId", sessionId);

        List<CasemgmtNoteLock> results = query.getResultList();

        return results;
    }
}
