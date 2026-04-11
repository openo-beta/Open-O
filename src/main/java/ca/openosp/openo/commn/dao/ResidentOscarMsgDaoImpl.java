//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.ResidentOscarMsg;
import org.springframework.stereotype.Repository;

@Repository
public class ResidentOscarMsgDaoImpl extends AbstractDaoImpl<ResidentOscarMsg> implements ResidentOscarMsgDao {

    public ResidentOscarMsgDaoImpl() {
        super(ResidentOscarMsg.class);
    }

    @Override
    public List<ResidentOscarMsg> findBySupervisor(String supervisor) {
        Query query = entityManager.createQuery("select p from ResidentOscarMsg p where p.supervisor_no = ?1 and p.complete = 0");
        query.setParameter(1, supervisor);

        return query.getResultList();
    }

    @Override
    public ResidentOscarMsg findByNoteId(Long noteId) {
        Query query = entityManager.createQuery("select p from ResidentOscarMsg p where p.note_id = ?1");
        query.setParameter(1, noteId);

        return this.getSingleResultOrNull(query);
    }

}
