//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.IntegratorConsent;

public interface IntegratorConsentDao extends AbstractDao<IntegratorConsent> {
    IntegratorConsent findLatestByFacilityDemographic(int facilityId, int demographicId);

    List<IntegratorConsent> findByFacilityAndDemographic(int facilityId, int demographicId);

    List<IntegratorConsent> findByFacilityAndDemographicSince(int facilityId, int demographicId, Date lastDataUpdated);

    List<Integer> findDemographicIdsByFacilitySince(int facilityId, Date lastDataUpdated);
}
