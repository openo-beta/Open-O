package ca.openosp.openo.hospitalReportManager.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import ca.openosp.openo.commn.model.AbstractModel;

/**
 * Registry entry mapping a Hospital Report Manager (HRM) Sending Facility identifier to a
 * human-readable facility name. Reports arrive carrying only the sending-facility ID; this
 * registry lets OpenO display a friendly name across the HRM inbox, report views, class
 * mappings, and generated PDFs, and lets administrators detect unregistered facilities.
 *
 * @since 2026-05-22
 */
@Entity
public class HRMSendingFacility extends AbstractModel<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String sendingFacilityId;

    @Column(nullable = false)
    private String facilityName;

    public HRMSendingFacility() {
    }

    /**
     * Returns the surrogate primary key of this registry entry.
     *
     * @return Integer the generated identifier, or null if not yet persisted
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Returns the HRM sending-facility identifier as it appears in incoming HRM reports.
     *
     * @return String the unique sending-facility identifier
     */
    public String getSendingFacilityId() {
        return sendingFacilityId;
    }

    /**
     * Sets the HRM sending-facility identifier. Must be unique within the registry.
     *
     * @param sendingFacilityId String the sending-facility identifier from the HRM report
     */
    public void setSendingFacilityId(String sendingFacilityId) {
        this.sendingFacilityId = sendingFacilityId;
    }

    /**
     * Returns the human-readable facility name shown in HRM displays.
     *
     * @return String the facility display name
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * Sets the human-readable facility name shown in HRM displays.
     *
     * @param facilityName String the facility display name
     */
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }
}
