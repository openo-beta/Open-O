//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.QueueDocumentLink;


/**
 * @author jackson bi
 */

public interface QueueDocumentLinkDao extends AbstractDao<QueueDocumentLink> {

    public List<QueueDocumentLink> getQueueDocLinks();

    public List<QueueDocumentLink> getActiveQueueDocLink();

    public List<QueueDocumentLink> getQueueFromDocument(Integer docId);

    public List<QueueDocumentLink> getDocumentFromQueue(Integer qId);

    public boolean hasQueueBeenLinkedWithDocument(Integer dId, Integer qId);

    public boolean setStatusInactive(Integer docId);

    public void addActiveQueueDocumentLink(Integer qId, Integer dId);

    public void addToQueueDocumentLink(Integer qId, Integer dId);
}
 