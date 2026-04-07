//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.ScheduleHoliday;
import org.springframework.stereotype.Repository;

@Repository
public class ScheduleHolidayDaoImpl extends AbstractDaoImpl<ScheduleHoliday> implements ScheduleHolidayDao {

    public ScheduleHolidayDaoImpl() {
        super(ScheduleHoliday.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ScheduleHoliday> findAll() {
        Query query = createQuery("x", null);
        return query.getResultList();
    }

    @Override
    public List<ScheduleHoliday> findAfterDate(Date date) {
        Query query = entityManager.createQuery("select s from ScheduleHoliday s where s.id > ?1");
        query.setParameter(1, date);

        @SuppressWarnings("unchecked")
        List<ScheduleHoliday> results = query.getResultList();
        return results;
    }

}
