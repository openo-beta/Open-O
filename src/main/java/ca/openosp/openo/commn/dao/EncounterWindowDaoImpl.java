//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.EncounterWindow;
import org.springframework.stereotype.Repository;

@Repository
public class EncounterWindowDaoImpl extends AbstractDaoImpl<EncounterWindow> implements EncounterWindowDao {

    public EncounterWindowDaoImpl() {
        super(EncounterWindow.class);
    }

    @Override
    public EncounterWindow findByProvider(String providerNo) {
        Query query = createQuery("ew", "ew.providerNo = ?1");
        query.setParameter(1, providerNo);
        return getSingleResultOrNull(query);
    }
}
