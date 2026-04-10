//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.MdsZLB;
import org.springframework.stereotype.Repository;

@Repository
public class MdsZLBDaoImpl extends AbstractDaoImpl<MdsZLB> implements MdsZLBDao {

    public MdsZLBDaoImpl() {
        super(MdsZLB.class);
    }

}
