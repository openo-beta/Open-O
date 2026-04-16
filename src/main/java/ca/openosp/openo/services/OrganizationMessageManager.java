//CHECKSTYLE:OFF

package ca.openosp.openo.services;

import java.util.List;

import ca.openosp.openo.commn.model.FacilityMessage;

public interface OrganizationMessageManager {
    FacilityMessage getMessage(String messageId);

    void saveFacilityMessage(FacilityMessage msg);

    List<FacilityMessage> getMessages();

    List<FacilityMessage> getMessagesByFacilityId(Integer facilityId);

    List<FacilityMessage> getMessagesByFacilityIdOrNull(Integer facilityId);

    List<FacilityMessage> getMessagesByFacilityIdAndProgramId(Integer facilityId, Integer programId);

    List<FacilityMessage> getMessagesByFacilityIdOrNullAndProgramIdOrNull(Integer facilityId, Integer programId);
}
