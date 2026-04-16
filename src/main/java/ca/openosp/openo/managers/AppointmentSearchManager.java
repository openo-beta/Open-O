//CHECKSTYLE:OFF

package ca.openosp.openo.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.openosp.openo.appointment.search.SearchConfig;
import ca.openosp.openo.appointment.search.TimeSlot;
import ca.openosp.openo.appointment.search.AppointmentType;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.webserv.transfer_objects.CalendarScheduleCodePairTransfer;

public interface AppointmentSearchManager {
    //Right now these two methods return the same but in the future this could be customized based on the demographic
    public List<AppointmentType> getAppointmentTypes(SearchConfig config, Integer demographicNo);

    public List<AppointmentType> getAppointmentTypes(SearchConfig config, String providerNo);

    public SearchConfig getProviderSearchConfig(String providerNo);

    public List<TimeSlot> findAppointment(LoggedInInfo loggedInInfo, SearchConfig config, Integer demographicNo, Long appointmentTypeId, Calendar startDate) throws java.lang.ClassNotFoundException, java.lang.InstantiationException, java.lang.IllegalAccessException;

    public static List<TimeSlot> getAllowedTimesByType(DayWorkSchedule dayWorkSchedule, Character[] codes, String providerNo) {
        ArrayList<TimeSlot> allowedTimesFilteredByType = new ArrayList<TimeSlot>();
        for (CalendarScheduleCodePairTransfer entry : CalendarScheduleCodePairTransfer.toTransfer(dayWorkSchedule.getTimeSlots())) {
            char c = entry.getScheduleCode();
            if (Arrays.binarySearch(codes, c) >= 0) {
                allowedTimesFilteredByType.add(new TimeSlot(providerNo, null, entry.getDate(), c));
            }
        }
        return allowedTimesFilteredByType;
    }
}
