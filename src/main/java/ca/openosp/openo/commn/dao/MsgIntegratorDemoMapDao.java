//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import ca.openosp.openo.commn.model.MsgIntegratorDemoMap;

public interface MsgIntegratorDemoMapDao extends AbstractDao<MsgIntegratorDemoMap> {

    public List<MsgIntegratorDemoMap> findByMessageIdandMsgDemoMapId(Integer messageId, long msgDemoMapId);
}
