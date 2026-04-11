//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.ArrayList;
import java.util.List;

import ca.openosp.openo.commn.model.MessageTbl;
import ca.openosp.openo.commn.model.MsgDemoMap;

public interface MessageTblDao extends AbstractDao<MessageTbl> {

    public List<MessageTbl> findByMaps(List<MsgDemoMap> m);

    public List<MessageTbl> findByProviderAndSendBy(String providerNo, Integer sendBy);

    public List<MessageTbl> findByIds(List<Integer> ids);
}
