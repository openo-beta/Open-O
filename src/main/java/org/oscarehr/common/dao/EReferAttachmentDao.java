//CHECKSTYLE:OFF
package org.oscarehr.common.dao;

import org.oscarehr.common.model.EReferAttachment;

import java.util.Date;


public interface EReferAttachmentDao extends AbstractDao<EReferAttachment> {
    EReferAttachment getRecentByDemographic(Integer demographicNo, Date expiry);
}
