//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.MsgDemoMap;

public interface MsgDemoMapDao extends AbstractDao<MsgDemoMap> {

    public List<MsgDemoMap> findByDemographicNo(Integer demographicNo);

    public List<MsgDemoMap> findByMessageId(Integer messageId);

    public List<Object[]> getMessagesAndDemographicsByMessageId(Integer messageId);

    public List<Object[]> getMapAndMessagesByDemographicNo(Integer demoNo);

    public List<Object[]> getMapAndMessagesByDemographicNoAndType(Integer demoNo, Integer type);

    public void remove(Integer messageID, Integer demographicNo);
}
