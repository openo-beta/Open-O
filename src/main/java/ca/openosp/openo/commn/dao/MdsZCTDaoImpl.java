//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.MdsZCT;
import org.springframework.stereotype.Repository;

@Repository
public class MdsZCTDaoImpl extends AbstractDaoImpl<MdsZCT> implements MdsZCTDao {

    public MdsZCTDaoImpl() {
        super(MdsZCT.class);
    }
}
