//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.PaginationQuery;
import ca.openosp.openo.commn.model.ConsultationRequest;
import ca.openosp.openo.consultations.ConsultationQuery;
import ca.openosp.openo.consultations.ConsultationRequestSearchFilter;

public interface ConsultRequestDao extends AbstractDao<ConsultationRequest> {

    public int getConsultationCount(PaginationQuery paginationQuery);

    public List<ConsultationRequest> listConsultationRequests(ConsultationQuery consultationQuery);

    public int getConsultationCount2(ConsultationRequestSearchFilter filter);

    public List<Object[]> search(ConsultationRequestSearchFilter filter);
}
