//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.RemoteAttachments;

public interface RemoteAttachmentsDao extends AbstractDao<RemoteAttachments> {
    List<RemoteAttachments> findByDemoNo(Integer demoNo);

    List<RemoteAttachments> findByDemoNoAndMessageId(Integer demographicNo, Integer messageId);
}
