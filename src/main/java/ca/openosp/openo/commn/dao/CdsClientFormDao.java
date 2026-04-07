//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.CdsClientForm;

public interface CdsClientFormDao extends AbstractDao<CdsClientForm> {
    CdsClientForm findLatestByFacilityClient(Integer facilityId, Integer clientId);

    CdsClientForm findLatestByFacilityAdmissionId(Integer facilityId, Integer admissionId, Boolean signed);

    List<CdsClientForm> findByFacilityClient(Integer facilityId, Integer clientId);

    List<CdsClientForm> findSignedCdsForms(Integer facilityId, String formVersion, Date startDate, Date endDate);
}
