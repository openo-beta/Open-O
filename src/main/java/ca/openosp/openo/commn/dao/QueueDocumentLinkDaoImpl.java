//CHECKSTYLE:OFF



package ca.openosp.openo.commn.dao;

import java.util.List;

import javax.persistence.Query;

import ca.openosp.openo.commn.model.QueueDocumentLink;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.stereotype.Repository;

/**
 * @author jackson bi
 */
@Repository
public class QueueDocumentLinkDaoImpl extends AbstractDaoImpl<QueueDocumentLink> implements QueueDocumentLinkDao {

    public QueueDocumentLinkDaoImpl() {
        super(QueueDocumentLink.class);
    }

    @Override
    public List<QueueDocumentLink> getQueueDocLinks() {
        Query query = entityManager.createQuery("SELECT q from QueueDocumentLink q");

        @SuppressWarnings("unchecked")
        List<QueueDocumentLink> queues = query.getResultList();
        return queues;
    }

    @Override
    public List<QueueDocumentLink> getActiveQueueDocLink() {
        Query query = entityManager.createNativeQuery(
                "SELECT q.* FROM queue_document_link q JOIN document d ON q.document_id = d.document_no WHERE q.status = ?1 ORDER BY d.updatedatetime ASC",
                QueueDocumentLink.class);
        query.setParameter(1, "A");

        @SuppressWarnings("unchecked")
        List<QueueDocumentLink> queues = query.getResultList();

        return queues;
    }

    @Override
    public List<QueueDocumentLink> getQueueFromDocument(Integer docId) {
        Query query = entityManager.createQuery("SELECT q from QueueDocumentLink q where q.docId=?1");
        query.setParameter(1, docId);

        @SuppressWarnings("unchecked")
        List<QueueDocumentLink> queues = query.getResultList();

        return queues;
    }

    @Override
    public List<QueueDocumentLink> getDocumentFromQueue(Integer qId) {
        Query query = entityManager.createQuery("SELECT q from QueueDocumentLink q where queueId=?1");
        query.setParameter(1, qId);

        @SuppressWarnings("unchecked")
        List<QueueDocumentLink> queues = query.getResultList();

        return queues;
    }

    @Override
    public boolean hasQueueBeenLinkedWithDocument(Integer dId, Integer qId) {
        Query query = entityManager.createQuery("SELECT q from QueueDocumentLink q where q.docId=?1 and q.queueId=?2");
        query.setParameter(1, dId);
        query.setParameter(2, qId);
        @SuppressWarnings("unchecked")
        List<QueueDocumentLink> queues = query.getResultList();

        return (queues.size() > 0);
    }

    @Override
    public boolean setStatusInactive(Integer docId) {
        if (docId == null) return false;

        List<QueueDocumentLink> qs = getQueueFromDocument(docId);
        if (qs.size() > 0) {
            QueueDocumentLink q = qs.get(0);
            if (q.getStatus() != null && !q.getStatus().equals("I")) {
                q.setStatus("I");
                merge(q);
                return true;
            } else {
                return false;
            }
        }
        return false;
        //if status is not I, change to I
        //if status is I, do nothing
    }

    @Override
    public void addActiveQueueDocumentLink(Integer qId, Integer dId) {
        try {
            if (!hasQueueBeenLinkedWithDocument(dId, qId)) {
                QueueDocumentLink qdl = new QueueDocumentLink();
                qdl.setDocId(dId);
                qdl.setStatus("A");
                qdl.setQueueId(qId);
                persist(qdl);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        }
    }

    @Override
    public void addToQueueDocumentLink(Integer qId, Integer dId) {
        try {
            if (!hasQueueBeenLinkedWithDocument(dId, qId)) {
                QueueDocumentLink qdl = new QueueDocumentLink();
                qdl.setDocId(dId);
                qdl.setQueueId(qId);
                persist(qdl);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        }
    }
}
