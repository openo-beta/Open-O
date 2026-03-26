//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ClinicLocation;

public interface ClinicLocationDao extends AbstractDao<ClinicLocation> {
    List<ClinicLocation> findAll();

    List<ClinicLocation> findByClinicNo(Integer clinicNo);

    String searchVisitLocation(String clinicLocationNo);

    ClinicLocation searchBillLocation(Integer clinicNo, String clinicLocationNo);

    void removeByClinicLocationNo(String clinicLocationNo);
}
