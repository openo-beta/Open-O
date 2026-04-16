//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.OscarJobType;
import org.springframework.stereotype.Repository;

@Repository
public class OscarJobTypeDaoImpl extends AbstractDaoImpl<OscarJobType> implements OscarJobTypeDao {

    public OscarJobTypeDaoImpl() {
        super(OscarJobType.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<OscarJobType> findByClassName(String className) {
        Query query = entityManager.createQuery("FROM OscarJobType d WHERE d.className = ?1");
        query.setParameter(1, className);

        return query.getResultList();
    }

}
