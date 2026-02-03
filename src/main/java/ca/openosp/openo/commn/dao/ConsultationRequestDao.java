//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.Date;
import java.util.List;

import ca.openosp.openo.commn.model.ConsultationRequest;
import ca.openosp.openo.consultation.dto.ConsultationListDTO;

public interface ConsultationRequestDao extends AbstractDao<ConsultationRequest> {

    public static final int DEFAULT_CONSULT_REQUEST_RESULTS_LIMIT = 100;

    int getCountReferralsAfterCutOffDateAndNotCompleted(Date referralDateCutoff);

    int getCountReferralsAfterCutOffDateAndNotCompleted(Date referralDateCutoff, String sendto);

    List<ConsultationRequest> getConsults(Integer demoNo);

    List<ConsultationRequest> getConsults(String team, boolean showCompleted, Date startDate, Date endDate, String orderby, String desc, String searchDate, Integer offset, Integer limit);

    List<ConsultationRequest> getConsultationsByStatus(Integer demographicNo, String status);

    ConsultationRequest getConsultation(Integer requestId);

    List<ConsultationRequest> getReferrals(String providerId, Date cutoffDate);

    List<Object[]> findRequests(Date timeLimit, String providerNo);

    List<ConsultationRequest> findRequestsByDemoNo(Integer demoId, Date cutoffDate);

    List<ConsultationRequest> findByDemographicAndService(Integer demographicNo, String serviceName);

    List<ConsultationRequest> findByDemographicAndServices(Integer demographicNo, List<String> serviceNameList);

    List<Integer> findNewConsultationsSinceDemoKey(String keyName);

    /**
     * Retrieves consultation requests as lightweight DTOs using a single JPQL constructor projection
     * query with LEFT JOINs to Demographic, Provider (MRP and consulting), ConsultationServices, and
     * ProfessionalSpecialist. Extensions (eReferral data) are batch-loaded in one additional query.
     * <p>
     * This replaces the previous N+1 pattern where each consultation triggered individual queries
     * for demographics, providers, services, and extensions, reducing total queries from ~5N to 2.
     * </p>
     *
     * @param team String the team/sendTo filter value (empty string for all teams)
     * @param showCompleted boolean whether to include completed (status 4) consultations
     * @param startDate Date the start date filter (null for no lower bound)
     * @param endDate Date the end date filter (null for no upper bound)
     * @param orderby String the sort column identifier (1-9), null for default referral date desc
     * @param desc String "1" for descending sort, null/other for ascending
     * @param searchDate String "1" to filter on appointment date instead of referral date
     * @param offset Integer the pagination offset (null defaults to 0)
     * @param limit Integer the page size (null defaults to {@link #DEFAULT_CONSULT_REQUEST_RESULTS_LIMIT})
     * @return List of ConsultationListDTO with all display fields populated
     * @since 2026-02-03
     */
    List<ConsultationListDTO> getConsultationDTOs(String team, boolean showCompleted, Date startDate, Date endDate, String orderby, String desc, String searchDate, Integer offset, Integer limit);

    /**
     * Retrieves all consultation requests for a specific patient as lightweight DTOs, ordered by
     * referral date descending. Uses the same DTO projection and batch extension loading as
     * {@link #getConsultationDTOs}.
     *
     * @param demoNo Integer the demographic number of the patient
     * @return List of ConsultationListDTO for the specified patient
     * @since 2026-02-03
     */
    List<ConsultationListDTO> getConsultationDTOsByDemographic(Integer demoNo);
}
