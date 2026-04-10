//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ProviderFacility;

public interface ProviderFacilityDao extends AbstractDao<ProviderFacility> {
    List<ProviderFacility> findByProviderNoAndFacilityId(String providerNo, int facilityId);
}
