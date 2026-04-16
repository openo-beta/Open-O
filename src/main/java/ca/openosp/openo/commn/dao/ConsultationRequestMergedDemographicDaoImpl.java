//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.merge.MergedDemographicTemplate;
import ca.openosp.openo.commn.model.ConsultationRequest;
import org.springframework.stereotype.Repository;

@Repository("consultationRequestDao")
public class ConsultationRequestMergedDemographicDaoImpl extends ConsultationRequestDaoImpl implements ConsultationRequestMergedDemographicDao {

    @Override
    public List<ConsultationRequest> getConsults(Integer demoNo) {
        List<ConsultationRequest> result = super.getConsults(demoNo);
        MergedDemographicTemplate<ConsultationRequest> template = new MergedDemographicTemplate<ConsultationRequest>() {
            @Override
            protected List<ConsultationRequest> findById(Integer demographic_no) {
                return ConsultationRequestMergedDemographicDaoImpl.super.getConsults(demographic_no);
            }
        };
        return template.findMerged(demoNo, result);
    }

    @Override
    public List<ConsultationRequest> getConsultationsByStatus(Integer demographicNo, final String status) {
        List<ConsultationRequest> result = super.getConsultationsByStatus(demographicNo, status);
        MergedDemographicTemplate<ConsultationRequest> template = new MergedDemographicTemplate<ConsultationRequest>() {
            @Override
            protected List<ConsultationRequest> findById(Integer demographic_no) {
                return ConsultationRequestMergedDemographicDaoImpl.super.getConsultationsByStatus(demographic_no, status);
            }
        };
        return template.findMerged(demographicNo, result);
    }

}
