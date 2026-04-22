//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.LabReportInformation;

public interface LabReportInformationDao extends AbstractDao<LabReportInformation> {
    List<Object[]> findReportsByPhysicianId(Integer physicianId);
}
