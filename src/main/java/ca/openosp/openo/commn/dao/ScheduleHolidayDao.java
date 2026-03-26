//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.ScheduleHoliday;

public interface ScheduleHolidayDao extends AbstractDao<ScheduleHoliday> {
    List<ScheduleHoliday> findAll();

    List<ScheduleHoliday> findAfterDate(Date date);
}
