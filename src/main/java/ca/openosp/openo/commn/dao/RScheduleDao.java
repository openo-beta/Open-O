//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.RSchedule;

public interface RScheduleDao extends AbstractDao<RSchedule> {
    List<RSchedule> findByProviderAvailableAndDate(String providerNo, String available, Date sdate);

    Long search_rschedule_overlaps(String providerNo, Date d1, Date d2, Date d3, Date d4, Date d5, Date d6, Date d7, Date d8, Date d9, Date d10, Date d11, Date d12, Date d13, Date d14);

    Long search_rschedule_exists(String providerNo, Date d1, Date d2);

    RSchedule search_rschedule_current(String providerNo, String available, Date sdate);

    List<RSchedule> search_rschedule_future(String providerNo, String available, Date sdate);

    RSchedule search_rschedule_current1(String providerNo, Date sdate);

    RSchedule search_rschedule_current2(String providerNo, Date sdate);

    List<RSchedule> search_rschedule_future1(String providerNo, Date sdate);

    List<RSchedule> findByProviderNoAndDates(String providerNo, Date apptDate);
}
