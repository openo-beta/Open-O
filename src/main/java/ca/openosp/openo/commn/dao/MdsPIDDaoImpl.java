//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import org.springframework.stereotype.Repository;
import ca.openosp.openo.commn.model.MdsPID;

@Repository
public class MdsPIDDaoImpl extends AbstractDaoImpl<MdsPID> implements MdsPIDDao {

    public MdsPIDDaoImpl() {
        super(MdsPID.class);
    }
}
