//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.HashMap;
import java.util.List;

import ca.openosp.openo.commn.model.MeasurementsExt;

public interface MeasurementsExtDao extends AbstractDao<MeasurementsExt> {
    List<MeasurementsExt> getMeasurementsExtByMeasurementId(Integer measurementId);

    HashMap<String, MeasurementsExt> getMeasurementsExtMapByMeasurementId(Integer measurementId);

    List<MeasurementsExt> getMeasurementsExtListByMeasurementIdList(List<Integer> measurementIdList);

    MeasurementsExt getMeasurementsExtByMeasurementIdAndKeyVal(Integer measurementId, String keyVal);

    Integer getMeasurementIdByKeyValue(String key, String value);

    public Integer getMeasurementIdByLabNoAndTestName(String labNo, String testName);//new

    List<MeasurementsExt> findByKeyValue(String key, String value);

    List<Integer> findUnmappedMeasuremntIds(List<String> excludeList);
}
