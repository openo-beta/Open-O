//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.LabTestResults;

import java.util.List;

public interface LabTestResultsDao extends AbstractDao<LabTestResults> {

    List<LabTestResults> findByTitleAndLabInfoId(Integer labId);

    List<LabTestResults> findByLabInfoId(Integer labId);

    List<LabTestResults> findByAbnAndLabInfoId(String abn, Integer labId);

    List<Object[]> findUniqueTestNames(Integer demoNo, String labType);

    List<LabTestResults> findByAbnAndPhysicianId(String abn, Integer lppii);

    List<LabTestResults> findByLabPatientPhysicialInfoId(Integer labid);
}
