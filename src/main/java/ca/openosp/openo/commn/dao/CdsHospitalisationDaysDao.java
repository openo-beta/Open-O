//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.CdsHospitalisationDays;

public interface CdsHospitalisationDaysDao extends AbstractDao<CdsHospitalisationDays> {
    List<CdsHospitalisationDays> findByClientId(Integer clientId);
}
