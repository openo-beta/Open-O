//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ConsultationResponse;
import ca.openosp.openo.consultations.ConsultationResponseSearchFilter;

public interface ConsultResponseDao extends AbstractDao<ConsultationResponse> {
    int getConsultationCount(ConsultationResponseSearchFilter filter);

    List<Object[]> search(ConsultationResponseSearchFilter filter);
}
