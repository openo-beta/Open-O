//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.RemoteReferral;

public interface RemoteReferralDao extends AbstractDao<RemoteReferral> {
    List<RemoteReferral> findByFacilityIdDemogprahicId(Integer facilityId, Integer demographicId);
}
