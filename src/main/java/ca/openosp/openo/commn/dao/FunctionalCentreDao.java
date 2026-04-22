//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.FunctionalCentre;

public interface FunctionalCentreDao extends AbstractDao<FunctionalCentre> {
    List<FunctionalCentre> findAll();

    List<FunctionalCentre> findInUseByFacility(Integer facilityId);
}
