//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ProviderInboxItem;

/**
 * @author jay gallagher
 */
public interface ProviderInboxRoutingDao extends AbstractDao<ProviderInboxItem> {

    public boolean removeLinkFromDocument(String docType, Integer docId, String providerNo);

    public List<ProviderInboxItem> getProvidersWithRoutingForDocument(String docType, Integer docId);

    public boolean hasProviderBeenLinkedWithDocument(String docType, Integer docId, String providerNo);

    public int howManyDocumentsLinkedWithAProvider(String providerNo);

	public List<ProviderInboxItem> findDocumentsLinkedWithProvider(String docType, Integer docId, String providerNo);

    public void addToProviderInbox(String providerNo, Integer labNo, String labType);

}
