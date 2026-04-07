//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.OLISQueryLog;
import org.springframework.stereotype.Repository;

@Repository
public class OLISQueryLogDaoImpl extends AbstractDaoImpl<OLISQueryLog> implements OLISQueryLogDao {

    public OLISQueryLogDaoImpl() {
        super(OLISQueryLog.class);
    }

    @Override
    public OLISQueryLog findByUUID(String uuid) {
        Query query = entityManager.createQuery("select x from OLISQueryLog x where x.uuid=?1");
        query.setParameter(1, uuid);

        return this.getSingleResultOrNull(query);
    }
}
