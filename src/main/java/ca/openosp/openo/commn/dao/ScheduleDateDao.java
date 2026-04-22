//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.ScheduleDate;

public interface ScheduleDateDao extends AbstractDao<ScheduleDate> {
    ScheduleDate findByProviderNoAndDate(String providerNo, Date date);

    List<ScheduleDate> findByProviderPriorityAndDateRange(String providerNo, char priority, Date date, Date date2);

    List<ScheduleDate> findByProviderAndDateRange(String providerNo, Date date, Date date2);

    List<ScheduleDate> search_scheduledate_c(String providerNo);

    List<ScheduleDate> search_numgrpscheduledate(String myGroupNo, Date sDate);

    List<Object[]> search_appttimecode(Date sDate, String providerNo);

    List<ScheduleDate> search_scheduledate_teamp(Date date, Date date2, Character status, List<String> providerNos);

    List<ScheduleDate> search_scheduledate_datep(Date date, Date date2, Character status);

    List<ScheduleDate> findByProviderStartDateAndPriority(String providerNo, Date apptDate, String priority);
}
