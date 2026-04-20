//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ConsultationRequestExt;

public interface ConsultationRequestExtDao extends AbstractDao<ConsultationRequestExt> {
    List<ConsultationRequestExt> getConsultationRequestExts(int requestId);

    String getConsultationRequestExtsByKey(int requestId, String key);

    void clear(int requestId);
}
