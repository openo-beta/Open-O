//CHECKSTYLE:OFF

package ca.openosp.openo.services;

import java.util.List;

import ca.openosp.openo.commn.dao.FacilityMessageDao;
import ca.openosp.openo.commn.model.FacilityMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class OrganizationMessageManagerImpl implements OrganizationMessageManager {

    @Autowired
    private FacilityMessageDao facilityMessageDao;

    public FacilityMessage getMessage(String messageId) {
        return facilityMessageDao.find(Integer.valueOf(messageId));
    }

    public void saveFacilityMessage(FacilityMessage msg) {
        if (msg.getId() == null || msg.getId().intValue() == 0) {
            msg.setId(null);
            facilityMessageDao.persist(msg);
        } else {
            facilityMessageDao.merge(msg);
        }
    }

    public List<FacilityMessage> getMessages() {
        return facilityMessageDao.getMessages();
    }

    public List<FacilityMessage> getMessagesByFacilityId(Integer facilityId) {
        if (facilityId == null || facilityId.intValue() == 0) {
            return null;
        }
        return facilityMessageDao.getMessagesByFacilityId(facilityId);
    }

    public List<FacilityMessage> getMessagesByFacilityIdOrNull(Integer facilityId) {
        if (facilityId == null || facilityId.intValue() == 0) {
            return null;
        }
        return facilityMessageDao.getMessagesByFacilityIdOrNull(facilityId);
    }

    public List<FacilityMessage> getMessagesByFacilityIdAndProgramId(Integer facilityId, Integer programId) {
        if (facilityId == null || facilityId.intValue() == 0) {
            return null;
        }
        return facilityMessageDao.getMessagesByFacilityIdAndProgramId(facilityId, programId);
    }

    public List<FacilityMessage> getMessagesByFacilityIdOrNullAndProgramIdOrNull(Integer facilityId, Integer programId) {
        if (facilityId == null || facilityId.intValue() == 0) {
            return null;
        }
        return facilityMessageDao.getMessagesByFacilityIdOrNullAndProgramIdOrNull(facilityId, programId);
    }
}
