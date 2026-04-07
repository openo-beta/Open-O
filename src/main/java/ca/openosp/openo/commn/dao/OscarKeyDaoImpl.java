//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.OscarKey;
import org.springframework.stereotype.Repository;

@Repository
public class OscarKeyDaoImpl extends AbstractDaoImpl<OscarKey> implements OscarKeyDao {

    public OscarKeyDaoImpl() {
        super(OscarKey.class);
    }
}
