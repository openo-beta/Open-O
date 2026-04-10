//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.MeasurementTypeDeleted;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementTypeDeletedDaoImpl extends AbstractDaoImpl<MeasurementTypeDeleted> implements MeasurementTypeDeletedDao {

    public MeasurementTypeDeletedDaoImpl() {
        super(MeasurementTypeDeleted.class);
    }
}
