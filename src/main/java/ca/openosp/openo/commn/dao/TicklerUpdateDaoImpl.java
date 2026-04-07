//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.TicklerUpdate;
import org.springframework.stereotype.Repository;

@Repository
public class TicklerUpdateDaoImpl extends AbstractDaoImpl<TicklerUpdate> implements TicklerUpdateDao {

    public TicklerUpdateDaoImpl() {
        super(TicklerUpdate.class);
    }

}