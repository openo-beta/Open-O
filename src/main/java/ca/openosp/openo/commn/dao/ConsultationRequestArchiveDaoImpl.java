//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ConsultationRequestArchive;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultationRequestArchiveDaoImpl extends AbstractDaoImpl<ConsultationRequestArchive> implements ConsultationRequestArchiveDao {

    public ConsultationRequestArchiveDaoImpl() {
        super(ConsultationRequestArchive.class);
    }
}
