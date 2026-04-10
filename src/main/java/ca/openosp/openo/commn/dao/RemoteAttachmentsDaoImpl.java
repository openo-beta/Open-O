//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.RemoteAttachments;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class RemoteAttachmentsDaoImpl extends AbstractDaoImpl<RemoteAttachments> implements RemoteAttachmentsDao {

    public RemoteAttachmentsDaoImpl() {
        super(RemoteAttachments.class);
    }

    @Override
    public List<RemoteAttachments> findByDemoNo(Integer demoNo) {
        Query query = createQuery("ra", "ra.demographicNo = ?1 ORDER BY ra.date");
        query.setParameter(1, demoNo);
        return query.getResultList();
    }

    @Override
    public List<RemoteAttachments> findByDemoNoAndMessageId(Integer demographicNo, Integer messageId) {
        Query query = createQuery("a", "a.demographicNo = ?1 and a.messageId = ?2");
        query.setParameter(1, demographicNo);
        query.setParameter(2, messageId);
        return query.getResultList();
    }
}
