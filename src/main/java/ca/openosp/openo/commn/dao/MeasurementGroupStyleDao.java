//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MeasurementGroupStyle;

public interface MeasurementGroupStyleDao extends AbstractDao<MeasurementGroupStyle> {
    List<MeasurementGroupStyle> findAll();

    List<MeasurementGroupStyle> findByGroupName(String groupName);

    List<MeasurementGroupStyle> findByCssId(Integer cssId);
}
