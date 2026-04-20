//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Facility;

public interface FacilityDao extends AbstractDao<Facility> {
    List<Facility> findAll(Boolean active);
}
