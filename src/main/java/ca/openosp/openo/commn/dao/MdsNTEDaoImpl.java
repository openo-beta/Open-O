//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import org.springframework.stereotype.Repository;
import ca.openosp.openo.commn.model.MdsNTE;

@Repository
public class MdsNTEDaoImpl extends AbstractDaoImpl<MdsNTE> implements MdsNTEDao {

    public MdsNTEDaoImpl() {
        super(MdsNTE.class);
    }
}
