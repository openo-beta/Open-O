//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.MdsZCL;
import org.springframework.stereotype.Repository;

@Repository
public class MdsZCLDaoImpl extends AbstractDaoImpl<MdsZCL> implements MdsZCLDao {

    public MdsZCLDaoImpl() {
        super(MdsZCL.class);
    }
}
