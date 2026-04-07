//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.FaxConfig;
import org.springframework.stereotype.Repository;

@Repository
public class FaxConfigDaoImpl extends AbstractDaoImpl<FaxConfig> implements FaxConfigDao {

    public FaxConfigDaoImpl() {
        super(FaxConfig.class);
    }


    public FaxConfig getConfigByNumber(String number) {
        Query query = entityManager.createQuery("select config from FaxConfig config where config.faxNumber = :number");

        query.setParameter("number", number);

        return getSingleResultOrNull(query);
    }

    public FaxConfig getActiveConfigByNumber(String number) {
        Query query = entityManager.createQuery("select config from FaxConfig config where config.faxNumber = :number and config.active = 1");

        query.setParameter("number", number);

        return getSingleResultOrNull(query);
    }
}
