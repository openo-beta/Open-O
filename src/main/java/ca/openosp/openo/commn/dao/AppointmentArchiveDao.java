//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.AppointmentArchive;

public interface AppointmentArchiveDao extends AbstractDao<AppointmentArchive> {

    public AppointmentArchive archiveAppointment(Appointment appointment);

    public List<AppointmentArchive> findByUpdateDate(Date updatedAfterThisDateExclusive, int itemsToReturn);
}
