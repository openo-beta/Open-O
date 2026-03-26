//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MeasurementType;

public interface MeasurementTypeDao extends AbstractDao<MeasurementType> {
    List<MeasurementType> findAll();

    List<MeasurementType> findAllOrderByName();

    List<MeasurementType> findAllOrderById();

    List<MeasurementType> findByType(String type);

    List<MeasurementType> findByMeasuringInstructionAndTypeDisplayName(String measuringInstruction, String typeDisplayName);

    List<MeasurementType> findByTypeDisplayName(String typeDisplayName);

    List<MeasurementType> findByTypeAndMeasuringInstruction(String type, String measuringInstruction);

    List<Object> findUniqueTypeDisplayNames();
}
