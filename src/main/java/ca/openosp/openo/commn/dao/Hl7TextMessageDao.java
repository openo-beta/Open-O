//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.Hl7TextMessage;

public interface Hl7TextMessageDao extends AbstractDao<Hl7TextMessage> {

    public void updateIfFillerOrderNumberMatches(String base64EncodedeMessage, int fileUploadCheckId, Integer id);

    public List<Hl7TextMessage> findByFileUploadCheckId(int id);

	public List<Hl7TextMessage> findByIds(List<Integer> ids);

    public List<Integer> getLabResultsSince(Integer demographicNo, Date updateDate);

    public List<Hl7TextMessage> findByDemographicNo(Integer demographicNo, int offset, int limit);
}
