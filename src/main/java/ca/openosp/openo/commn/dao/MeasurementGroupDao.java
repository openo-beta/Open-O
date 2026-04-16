//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MeasurementGroup;

public interface MeasurementGroupDao extends AbstractDao<MeasurementGroup> {
    List<MeasurementGroup> findAll();

    List<MeasurementGroup> findByNameAndTypeDisplayName(String name, String typeDisplayName);

    List<MeasurementGroup> findByTypeDisplayName(String typeDisplayName);

    List<MeasurementGroup> findByName(String name);

    List<Object> findUniqueTypeDisplayNamesByGroupName(String groupName);
}
