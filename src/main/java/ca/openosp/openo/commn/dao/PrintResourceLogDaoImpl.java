//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.PrintResourceLog;
import org.springframework.stereotype.Repository;

@Repository
public class PrintResourceLogDaoImpl extends AbstractDaoImpl<PrintResourceLog> implements PrintResourceLogDao {

    public PrintResourceLogDaoImpl() {
        super(PrintResourceLog.class);
    }

    @Override
    public List<PrintResourceLog> findByResource(String resourceName, String resourceId) {
        Query query = entityManager.createQuery("select x from " + modelClass.getSimpleName() + " x WHERE x.resourceName=?1 and x.resourceId = ?2 order by x.dateTime DESC");
        query.setParameter(1, resourceName);
        query.setParameter(2, resourceId);

        @SuppressWarnings("unchecked")
        List<PrintResourceLog> results = query.getResultList();

        return results;
    }
}
