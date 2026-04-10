//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.MeasurementsDeleted;

public interface MeasurementsDeletedDao extends AbstractDao<MeasurementsDeleted> {
    List<MeasurementsDeleted> findDeletedAfterDatetime(Date dateTime);
}
