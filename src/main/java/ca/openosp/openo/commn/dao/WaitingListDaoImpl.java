//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.WaitingList;
import org.springframework.stereotype.Repository;

@Repository
public class WaitingListDaoImpl extends AbstractDaoImpl<WaitingList> implements WaitingListDao {

    public WaitingListDaoImpl() {
        super(WaitingList.class);
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findByDemographic(Integer demographicNo) {
        Query query = entityManager.createQuery("FROM WaitingListName wn, WaitingList w WHERE wn.id = w.listId AND w.demographicNo = ?1 AND w.isHistory != 'Y'");
        query.setParameter(1, demographicNo);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> findWaitingListsAndDemographics(Integer listId) {
        Query query = entityManager.createQuery("FROM WaitingList w, Demographic d WHERE w.demographicNo = d.DemographicNo AND  w.listId = ?1 AND w.isHistory = 'N' ORDER BY w.position");
        query.setParameter(1, listId);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<WaitingList> findByWaitingListId(Integer listId) {
        Query query = entityManager.createQuery("FROM WaitingList w WHERE w.listId = ?1 AND w.isHistory = 'N' ORDER BY w.onListSince");
        query.setParameter(1, listId);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Appointment> findAppointmentFor(WaitingList waitingList) {
        Query query = entityManager.createQuery("From Appointment a where a.appointmentDate >= ?1 AND a.demographicNo = ?2");
        query.setParameter(1, waitingList.getOnListSince());
        query.setParameter(2, waitingList.getDemographicNo());
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<WaitingList> findByWaitingListIdAndDemographicId(Integer waitingListId, Integer demographicId) {
        Query query = createQuery("wl", "wl.demographicNo = ?1 AND wl.listId = ?2");
        query.setParameter(1, demographicId);
        query.setParameter(2, waitingListId);
        return query.getResultList();
    }

    public Integer getMaxPosition(Integer listId) {
        Query query = entityManager.createQuery("select max(w.position) from WaitingList w where w.listId = ?1 AND w.isHistory = 'N'");
        query.setParameter(1, listId);
        Long result = (Long) query.getSingleResult();
        if (result == null) {
            return 0;
        }
        return result.intValue();
    }

    @SuppressWarnings("unchecked")
    public List<WaitingList> search_wlstatus(Integer demographicId) {
        Query query = entityManager.createQuery("select wl from WaitingList wl where wl.demographicNo = ?1 AND wl.isHistory = 'N' order BY wl.onListSince desc");
        query.setParameter(1, demographicId);
        return query.getResultList();
    }
}
