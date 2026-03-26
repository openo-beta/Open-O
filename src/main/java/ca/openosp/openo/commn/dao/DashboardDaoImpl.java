//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.Dashboard;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardDaoImpl extends AbstractDaoImpl<Dashboard> implements DashboardDao {

    public DashboardDaoImpl() {
        super(Dashboard.class);
    }

    @Override
    public List<Dashboard> getActiveDashboards() {
        return getDashboardsByStatus(Boolean.TRUE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Dashboard> getDashboardsByStatus(boolean status) {
        Query query = entityManager.createQuery("SELECT x FROM Dashboard x WHERE x.active = :status");
        query.setParameter("status", status);
        List<Dashboard> result = query.getResultList();
        return result;
    }

    /**
     * This is a safe operation because the database is not expected to grow
     * large enough to cause performance issues.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Dashboard> getDashboards() {
        Query query = entityManager.createQuery("SELECT x FROM Dashboard x");
        List<Dashboard> result = query.getResultList();
        return result;
    }

}
