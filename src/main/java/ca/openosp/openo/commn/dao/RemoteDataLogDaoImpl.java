//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.RemoteDataLog;
import org.springframework.stereotype.Repository;

@Repository
public class RemoteDataLogDaoImpl extends AbstractDaoImpl<RemoteDataLog> implements RemoteDataLogDao {
    public RemoteDataLogDaoImpl() {
        super(RemoteDataLog.class);
    }
}
