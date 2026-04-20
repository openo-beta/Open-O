//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.HnrDataValidation;

public interface HnrDataValidationDao extends AbstractDao<HnrDataValidation> {
    HnrDataValidation findMostCurrentByFacilityIdClientIdType(Integer facilityId, Integer clientId, HnrDataValidation.Type type);
}
