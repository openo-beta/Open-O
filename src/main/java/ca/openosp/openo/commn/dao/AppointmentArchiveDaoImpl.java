//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.AppointmentArchive;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentArchiveDaoImpl extends AbstractDaoImpl<AppointmentArchive> implements AppointmentArchiveDao {

    public AppointmentArchiveDaoImpl() {
        super(AppointmentArchive.class);
    }

    @Override
    public AppointmentArchive archiveAppointment(Appointment appointment) {
        AppointmentArchive aa = new AppointmentArchive();
        BeanUtils.copyProperties(appointment, aa, new String[]{"id"});
        aa.setAppointmentNo(appointment.getId());
        persist(aa);
        return aa;
    }

    /**
     * @return results ordered by lastUpdateDate
     */
    @Override
    public List<AppointmentArchive> findByUpdateDate(Date updatedAfterThisDateExclusive, int itemsToReturn) {
        String sqlCommand = "select x from " + modelClass.getSimpleName()
                + " x where x.updateDateTime>?1 order by x.updateDateTime";

        Query query = entityManager.createQuery(sqlCommand);
        query.setParameter(1, updatedAfterThisDateExclusive);
        setLimit(query, itemsToReturn);

        @SuppressWarnings("unchecked")
        List<AppointmentArchive> results = query.getResultList();
        return (results);
    }
}
