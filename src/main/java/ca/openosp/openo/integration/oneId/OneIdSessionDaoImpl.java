package ca.openosp.openo.integration.oneId;

import ca.openosp.openo.commn.dao.AbstractDaoImpl;
import org.springframework.stereotype.Repository;

@Repository
public class OneIdSessionDaoImpl extends AbstractDaoImpl<OneIdSession> implements OneIdSessionDao{
    public OneIdSessionDaoImpl() {
        super(OneIdSession.class);
    }
}
