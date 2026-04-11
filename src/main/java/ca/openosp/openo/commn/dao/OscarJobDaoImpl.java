//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.OscarJob;
import ca.openosp.openo.commn.model.OscarJobType;
import org.springframework.stereotype.Repository;

@Repository
public class OscarJobDaoImpl extends AbstractDaoImpl<OscarJob> implements OscarJobDao {

    public OscarJobDaoImpl() {
        super(OscarJob.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<OscarJob> findByType(OscarJobType oscarJobType) {
        Query query = entityManager.createQuery("FROM OscarJob d WHERE d.oscarJobType = ?1");
        query.setParameter(1, oscarJobType);

        return query.getResultList();
    }

    @Override
    public List<OscarJob> getJobByName(String name) {
        Query query = entityManager.createQuery("select x from " + modelClass.getSimpleName() + " x where x.name = ?1");
        query.setParameter(1, name);
        return query.getResultList();
    }
}
