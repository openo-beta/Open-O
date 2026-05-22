package ca.openosp.openo.hospitalReportManager.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import ca.openosp.openo.commn.model.AbstractModel;

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

    @Override
    public Integer getId() {
        return id;
    }

    public String getSendingFacilityId() {
        return sendingFacilityId;
    }

    public void setSendingFacilityId(String sendingFacilityId) {
        this.sendingFacilityId = sendingFacilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }
}
