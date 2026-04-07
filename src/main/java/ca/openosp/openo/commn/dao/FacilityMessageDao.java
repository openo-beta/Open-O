//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.FacilityMessage;

public interface FacilityMessageDao extends AbstractDao<FacilityMessage> {

    public List<FacilityMessage> getMessages();

    public List<FacilityMessage> getMessagesByFacilityId(Integer facilityId);

    public List<FacilityMessage> getMessagesByFacilityIdOrNull(Integer facilityId);

    public List<FacilityMessage> getMessagesByFacilityIdAndProgramId(Integer facilityId, Integer programId);

    public List<FacilityMessage> getMessagesByFacilityIdOrNullAndProgramIdOrNull(Integer facilityId, Integer programId);
}
