//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.ConsultationRequestExtArchive;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultationRequestExtArchiveDaoImpl extends AbstractDaoImpl<ConsultationRequestExtArchive> implements ConsultationRequestExtArchiveDao {

    public ConsultationRequestExtArchiveDaoImpl() {
        super(ConsultationRequestExtArchive.class);
    }
}
