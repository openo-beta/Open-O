//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Collections;
import java.util.List;

import ca.openosp.openo.commn.model.MessageList;

public interface MessageListDao extends AbstractDao<MessageList> {

    public List<MessageList> findByProviderNoAndMessageNo(String providerNo, Long messageNo);

    public List<MessageList> findByProviderNoAndLocationNo(String providerNo, Integer locationNo);

    public List<MessageList> findAllByMessageNoAndLocationNo(Long messageNo, Integer locationNo);

    public List<MessageList> findByMessageNoAndLocationNo(Long messageNo, Integer locationNo);

    public List<MessageList> findByMessage(Long messageNo);

    public List<MessageList> findByProviderAndStatus(String providerNo, String status);

    public List<MessageList> findUnreadByProvider(String providerNo);

    public int findUnreadByProviderAndAttachedCount(String providerNo);

    public int countUnreadByProviderAndFromIntegratedFacility(String providerNo);

    public int countUnreadByProvider(String providerNo);

    public List<MessageList> search(String providerNo, String status, int start, int max);

    public Integer searchAndReturnTotal(String providerNo, String status);

    public Integer messagesTotal(int type, String providerNo, Integer remoteLocation, String searchFilter);

    public List<MessageList> findByIntegratedFacility(int facilityId, String status);

    public List<MessageList> findByMessageAndIntegratedFacility(Long messageNo, int facilityId);

}
