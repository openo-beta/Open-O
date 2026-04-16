//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.HrmLogEntry;
import org.springframework.stereotype.Repository;

@Repository
public class HrmLogEntryDaoImpl extends AbstractDaoImpl<HrmLogEntry> implements HrmLogEntryDao {

    public HrmLogEntryDaoImpl() {
        super(HrmLogEntry.class);
    }

    @SuppressWarnings("unchecked")
    public List<HrmLogEntry> findByHrmLogId(int hrmLogId) {
        Query query = entityManager.createQuery("FROM HrmLogEntry d where d.hrmLogId=?1");
        query.setParameter(1, hrmLogId);

        return query.getResultList();
    }
}
