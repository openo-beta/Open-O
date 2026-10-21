//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.ConsultationServices;
import ca.openosp.openo.encounter.oscarConsultationRequest.config.data.ConsultationServiceDto;

public interface ConsultationServiceDao extends AbstractDao<ConsultationServices> {
    public String REFERRING_DOCTOR = "Referring Doctor";
    public String ACTIVE = "1";
    public String INACTIVE = "02";
    public boolean ACTIVE_ONLY = true;
    public boolean WITH_INACTIVE = false;

    public List<ConsultationServices> findAll();

    public List<ConsultationServices> findActive();

    public List<ConsultationServices> findActiveNames();

    public ConsultationServices findByDescription(String description);

    public ConsultationServices findReferringDoctorService(boolean activeOnly);

    /**
     * Retrieves only the service description for a given service ID, without loading
     * the full entity or its associated specialists collection.
     *
     * @param serviceId Integer the consultation service ID
     * @return String the service description, or null if not found
     */
    public String getServiceDescription(Integer serviceId);

    /**
     * Retrieves active consultation service summaries (ID and description only) without
     * loading the full entity or its associated specialists collection.
     *
     * @return List&lt;ConsultationServiceDto&gt; containing serviceId and serviceDesc
     */
    public List<ConsultationServiceDto> findActiveServiceSummaries();
}