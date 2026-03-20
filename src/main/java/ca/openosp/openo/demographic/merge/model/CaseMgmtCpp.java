//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 */
package ca.openosp.openo.demographic.merge.model;

import javax.persistence.*;
import java.util.Date;

/**
 * JPA entity for the {@code casemgmt_cpp} table.
 * Stores cumulative patient profile (social history, family history, medical history, etc.)
 * for case management.
 *
 * @since 2026-03-19
 */
@Entity
@Table(name = "casemgmt_cpp")
public class CaseMgmtCpp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "demographic_no")
    private String demographicNo;

    @Column(name = "provider_no")
    private String providerNo;

    @Lob
    @Column(name = "socialHistory")
    private String socialHistory;

    @Lob
    @Column(name = "familyHistory")
    private String familyHistory;

    @Lob
    @Column(name = "medicalHistory")
    private String medicalHistory;

    @Lob
    @Column(name = "ongoingConcerns")
    private String ongoingConcerns;

    @Lob
    @Column(name = "reminders")
    private String reminders;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_date")
    private Date updateDate;

    @Column(name = "primaryPhysician")
    private String primaryPhysician;

    @Column(name = "primaryCounsellor")
    private String primaryCounsellor;

    @Column(name = "otherFileNumber")
    private String otherFileNumber;

    @Lob
    @Column(name = "otherSupportSystems")
    private String otherSupportSystems;

    @Lob
    @Column(name = "pastMedications")
    private String pastMedications;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDemographicNo() { return demographicNo; }
    public void setDemographicNo(String demographicNo) { this.demographicNo = demographicNo; }

    public String getProviderNo() { return providerNo; }
    public void setProviderNo(String providerNo) { this.providerNo = providerNo; }

    public String getSocialHistory() { return socialHistory; }
    public void setSocialHistory(String socialHistory) { this.socialHistory = socialHistory; }

    public String getFamilyHistory() { return familyHistory; }
    public void setFamilyHistory(String familyHistory) { this.familyHistory = familyHistory; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getOngoingConcerns() { return ongoingConcerns; }
    public void setOngoingConcerns(String ongoingConcerns) { this.ongoingConcerns = ongoingConcerns; }

    public String getReminders() { return reminders; }
    public void setReminders(String reminders) { this.reminders = reminders; }

    public Date getUpdateDate() { return updateDate; }
    public void setUpdateDate(Date updateDate) { this.updateDate = updateDate; }

    public String getPrimaryPhysician() { return primaryPhysician; }
    public void setPrimaryPhysician(String primaryPhysician) { this.primaryPhysician = primaryPhysician; }

    public String getPrimaryCounsellor() { return primaryCounsellor; }
    public void setPrimaryCounsellor(String primaryCounsellor) { this.primaryCounsellor = primaryCounsellor; }

    public String getOtherFileNumber() { return otherFileNumber; }
    public void setOtherFileNumber(String otherFileNumber) { this.otherFileNumber = otherFileNumber; }

    public String getOtherSupportSystems() { return otherSupportSystems; }
    public void setOtherSupportSystems(String otherSupportSystems) { this.otherSupportSystems = otherSupportSystems; }

    public String getPastMedications() { return pastMedications; }
    public void setPastMedications(String pastMedications) { this.pastMedications = pastMedications; }
}
