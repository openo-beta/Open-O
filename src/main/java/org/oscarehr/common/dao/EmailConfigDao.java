//CHECKSTYLE:OFF
package org.oscarehr.common.dao;

import org.oscarehr.common.model.EmailConfig;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

public interface EmailConfigDao extends AbstractDao<EmailConfig> {
    EmailConfig findActiveEmailConfig(EmailConfig emailConfig);

    EmailConfig findActiveEmailConfig(String senderEmail);

    @SuppressWarnings("unchecked")
    List<EmailConfig> fillAllActiveEmailConfigs();
}
