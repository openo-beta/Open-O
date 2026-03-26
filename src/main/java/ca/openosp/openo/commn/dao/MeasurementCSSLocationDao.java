//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MeasurementCSSLocation;

public interface MeasurementCSSLocationDao extends AbstractDao<MeasurementCSSLocation> {
    List<MeasurementCSSLocation> findAll();

    List<MeasurementCSSLocation> findByLocation(String location);
}
