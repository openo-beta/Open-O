//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.Prescribe;
import org.springframework.stereotype.Repository;

@Repository
public class PrescribeDaoImpl extends AbstractDaoImpl<Prescribe> implements PrescribeDao {

    public PrescribeDaoImpl() {
        super(Prescribe.class);
    }
}
