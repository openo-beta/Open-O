//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.HashAudit;
import org.springframework.stereotype.Repository;

@Repository
public class HashAuditDaoImpl extends AbstractDaoImpl<HashAudit> implements HashAuditDao {

    public HashAuditDaoImpl() {
        super(HashAudit.class);
    }
}
