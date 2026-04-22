//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.WaitingList;

public interface WaitingListDao extends AbstractDao<WaitingList> {
    List<Object[]> findByDemographic(Integer demographicNo);

    List<Object[]> findWaitingListsAndDemographics(Integer listId);

    List<WaitingList> findByWaitingListId(Integer listId);

    List<Appointment> findAppointmentFor(WaitingList waitingList);

    List<WaitingList> findByWaitingListIdAndDemographicId(Integer waitingListId, Integer demographicId);

    Integer getMaxPosition(Integer listId);

    List<WaitingList> search_wlstatus(Integer demographicId);
}
