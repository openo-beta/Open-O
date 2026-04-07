//CHECKSTYLE:OFF


package ca.openosp.openo.managers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.AppointmentArchive;
import ca.openosp.openo.commn.model.AppointmentStatus;
import ca.openosp.openo.commn.model.AppointmentType;
import ca.openosp.openo.commn.model.ScheduleTemplateCode;
import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.utility.LoggedInInfo;

public interface ScheduleManager {

    /*Right now the date object passed is converted to a local time.
     *
     * As in, if the server's timezone is set to EST and the method is called with two data objects set to
     *
     * 2011-11-11 2:01 TZ america/new york
     * 2011-11-10 23:01 TZ america/los angeles
     *
     * They will both return the DayWorkSchedule for November 11 2011;
     *
     * The DayWorkSchedule returned will be in the server's local timezone.
     *
     */
    public DayWorkSchedule getDayWorkSchedule(String providerNo, Calendar date);

    public List<Appointment> getDayAppointments(LoggedInInfo loggedInInfo, String providerNo, Date date);

    public List<Appointment> getDayAppointments(LoggedInInfo loggedInInfo, String providerNo, Calendar date);

    public List<ScheduleTemplateCode> getScheduleTemplateCodes();

    public List<AppointmentType> getAppointmentTypes();

    public void addAppointment(LoggedInInfo loggedInInfo, Security security, Appointment appointment);

    public List<Appointment> getAppointmentsForPatient(LoggedInInfo loggedInInfo, Integer demographicId, int startIndex, int itemsToReturn);

    public List<Appointment> getAppointmentsByProgramProviderDemographicDate(LoggedInInfo loggedInInfo, Integer programId, String providerNo, Integer demographicId, Calendar updatedAfterThisDateExclusive, int itemsToReturn);

    public Appointment getAppointment(LoggedInInfo loggedInInfo, Integer appointmentId);

    public void updateAppointment(LoggedInInfo loggedInInfo, Appointment appointment);

    public List<Appointment> getAppointmentsForDateRangeAndProvider(LoggedInInfo loggedInInfo, Date startTime, Date endTime, String providerNo);

    public List<Appointment> getAppointmentUpdatedAfterDate(LoggedInInfo loggedInInfo, Date updatedAfterThisDateExclusive, int itemsToReturn);

    public List<Appointment> getAppointmentByDemographicIdUpdatedAfterDate(LoggedInInfo loggedInInfo, Integer demographicId, Date updatedAfterThisDateExclusive);

    public List<AppointmentArchive> getAppointmentArchiveUpdatedAfterDate(LoggedInInfo loggedInInfo, Date updatedAfterThisDateExclusive, int itemsToReturn);

    public List<AppointmentStatus> getAppointmentStatuses(LoggedInInfo loggedInInfo);

    public List<Integer> getAllDemographicIdByProgramProvider(LoggedInInfo loggedInInfo, Integer programId, String providerNo);

    public List<Object[]> listAppointmentsByPeriodProvider(LoggedInInfo loggedInInfo, Date sDate, Date eDate, String providers);

    public List<Object[]> listProviderAppointmentCounts(LoggedInInfo loggedInInfo, String sDateStr, String eDateStr);

    public boolean removeIfDoubleBooked(LoggedInInfo loggedInInfo, Calendar startTime, Calendar endTime, String providerNo, Appointment appointment);

}
 