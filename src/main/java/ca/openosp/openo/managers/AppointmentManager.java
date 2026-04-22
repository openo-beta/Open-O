//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.List;

import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.utility.LoggedInInfo;

public interface AppointmentManager {

    public List<Appointment> getAppointmentHistoryWithoutDeleted(LoggedInInfo loggedInInfo, Integer demographicNo, Integer offset, Integer limit);

    public List<Object> getAppointmentHistoryWithDeleted(LoggedInInfo loggedInInfo, Integer demographicNo, Integer offset, Integer limit);

    /**
     * Adds an appointment to the system.
     *
     * @param loggedInInfo logged in provider information
     * @param appointment appointment data to add
     */
    public void addAppointment(LoggedInInfo loggedInInfo, Appointment appointment);

    public void updateAppointment(LoggedInInfo loggedInInfo, Appointment appointment);

    public void deleteAppointment(LoggedInInfo loggedInInfo, int apptNo);

    public Appointment getAppointment(LoggedInInfo loggedInInfo, int apptNo);

    public Appointment updateAppointmentStatus(LoggedInInfo loggedInInfo, int apptNo, String status);


    public Appointment updateAppointmentType(LoggedInInfo loggedInInfo, int apptNo, String type);

    public Appointment updateAppointmentUrgency(LoggedInInfo loggedInInfo, int apptNo, String urgency);

    public List<AppointmentStatus> getAppointmentStatuses();

    public List<LookupListItem> getReasons();

    public List<Appointment> findMonthlyAppointments(LoggedInInfo loggedInInfo, String providerNo, int year, int month);

    public String getNextAppointmentDate(Integer demographicNo);
}
