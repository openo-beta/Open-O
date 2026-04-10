//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.AppointmentSearch;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentSearchDaoImpl extends AbstractDaoImpl<AppointmentSearch> implements AppointmentSearchDao {

    public AppointmentSearchDaoImpl() {
        super(AppointmentSearch.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AppointmentSearch> findAll() {
        Query query = entityManager.createQuery("FROM " + modelClass.getSimpleName() + " a order by a.createDate desc");
        return query.getResultList();
    }

    @Override
    public List<AppointmentSearch> findActive() {
        Query q = entityManager.createQuery("select a from AppointmentSearch a where a.active= true");
        @SuppressWarnings("unchecked")
        List<AppointmentSearch> results = q.getResultList();

        return results;
    }

    @Override
    public List<AppointmentSearch> findByUUID(String uuid, Boolean active) {
        String queryStr = "select a from AppointmentSearch a where a.uuid=?1";
        if (active != null && active.booleanValue()) {
            queryStr = queryStr + " and a.active = true";
        }
        Query q = entityManager.createQuery(queryStr);
        q.setParameter(1, uuid);

        @SuppressWarnings("unchecked")
        List<AppointmentSearch> results = q.getResultList();

        return results;
    }

    @Override
    public AppointmentSearch findForProvider(String providerNo) {
        if (providerNo == null || providerNo.length() == 0) {
            return null;
        }

        Query q = entityManager.createQuery(
                "select a from AppointmentSearch a where a.providerNo = ?1 and a.active = true order by a.updateDate desc");
        q.setParameter(1, providerNo);

        @SuppressWarnings("unchecked")
        List<AppointmentSearch> results = q.getResultList();

        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

}
