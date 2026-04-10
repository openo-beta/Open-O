//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ConsultationRequest;

public interface ConsultationRequestMergedDemographicDao {
    List<ConsultationRequest> getConsults(Integer demoNo);

    List<ConsultationRequest> getConsultationsByStatus(Integer demographicNo, final String status);
}
