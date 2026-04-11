//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.MdsPV1;
import org.springframework.stereotype.Repository;

@Repository
public class MdsPV1DaoImpl extends AbstractDaoImpl<MdsPV1> implements MdsPV1Dao {

    public MdsPV1DaoImpl() {
        super(MdsPV1.class);
    }
}
