//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.MdsZFR;
import org.springframework.stereotype.Repository;

@Repository
public class MdsZFRDaoImpl extends AbstractDaoImpl<MdsZFR> implements MdsZFRDao {

    public MdsZFRDaoImpl() {
        super(MdsZFR.class);
    }
}
