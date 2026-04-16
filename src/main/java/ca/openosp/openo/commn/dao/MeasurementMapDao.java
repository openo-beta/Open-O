//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MeasurementMap;

public interface MeasurementMapDao extends AbstractDao<MeasurementMap> {

    public void addMeasurementMap(MeasurementMap measurementMap);

    public List<MeasurementMap> getAllMaps();

    public List<MeasurementMap> getMapsByIdent(String identCode);

    public List<MeasurementMap> findByLoincCode(String loincCode);

    public List<MeasurementMap> getMapsByLoinc(String loinc);

    public List<MeasurementMap> findByLoincCodeAndLabType(String loincCode, String labType);

    public MeasurementMap findByLonicCodeLabTypeAndMeasurementName(String loincCode, String labType,
                                                                   String measurementName);

    public List<String> findDistinctLabTypes();

    public List<String> findDistinctLoincCodes();

    public List<String> findDistinctLoincCodesByLabType(MeasurementMap.LAB_TYPE lab_type);

    public List<Object[]> findMeasurements(String labType, String idCode, String name);

    public List<MeasurementMap> findMeasurementsByName(String searchString);

    public List<MeasurementMap> searchMeasurementsByName(String searchString);
}
