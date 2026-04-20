//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.AppointmentStatus;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentStatusDaoImpl extends AbstractDaoImpl<AppointmentStatus> implements AppointmentStatusDao {

    public AppointmentStatusDaoImpl() {
        super(AppointmentStatus.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AppointmentStatus> findAll() {
        Query query = entityManager.createQuery("FROM " + modelClass.getSimpleName());
        return query.getResultList();
    }

    @Override
    public List<AppointmentStatus> findActive() {
        Query q = entityManager.createQuery("select a from AppointmentStatus a where a.active=?1");
        q.setParameter(1, 1);

        @SuppressWarnings("unchecked")
        List<AppointmentStatus> results = q.getResultList();

        return results;
    }

    @Override
    public AppointmentStatus findByStatus(String status) {
        if (status == null || status.length() == 0) {
            return null;
        }

        Query q = entityManager.createQuery("select a from AppointmentStatus a where a.status like ?1");
        q.setParameter(1, status.substring(0, 1) + "%");

        @SuppressWarnings("unchecked")
        List<AppointmentStatus> results = q.getResultList();

        for (AppointmentStatus r : results) {
            if (r.getStatus() != null && r.getStatus().length() > 0 && r.getStatus().charAt(0) == status.charAt(0)) {
                return r;
            }
        }

        return null;
    }

    @Override
    public void modifyStatus(int ID, String strDesc, String strColor) {
        AppointmentStatus appts = find(ID);
        if (appts != null) {
            appts.setDescription(strDesc);
            appts.setColor(strColor);
        }
    }

    public void changeStatus(int ID, int iActive) {
        AppointmentStatus appts = find(ID);
        if (appts != null) {
            appts.setActive(iActive);
        }
    }

    /**
     * I don't know about this one...but i'm just converting it to a JPA entity for
     * now.
     *
     * @param allStatus
     * @return int
     */
    @Override
    public int checkStatusUsuage(List<AppointmentStatus> allStatus) {
        int iUsuage = 0;
        AppointmentStatus apptStatus = null;
        String sql = null;
        for (int i = 0; i < allStatus.size(); i++) {
            apptStatus = allStatus.get(i);
            if (apptStatus.getActive() == 1)
                continue;
            sql = "select count(*) as total from appointment a where a.status like ?1 ";
            // sql = sql + "collate latin1_general_cs";

            Query q = entityManager.createNativeQuery(sql);
            q.setParameter(1, apptStatus.getStatus() + "%");
            Object result = q.getSingleResult();

            iUsuage = ((BigInteger) result).intValue();
            if (iUsuage > 0) {
                iUsuage = i;
                break;
            }
        }
        return iUsuage;
    }
}
