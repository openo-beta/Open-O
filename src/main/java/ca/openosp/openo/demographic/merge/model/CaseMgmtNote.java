//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 */
package ca.openosp.openo.demographic.merge.model;

import javax.persistence.*;
import java.util.Date;

/**
 * JPA entity for the {@code casemgmt_note} table.
 * Stores clinical encounter notes for case management.
 *
 * @since 2026-03-19
 */
@Entity
@Table(name = "casemgmt_note")
public class CaseMgmtNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Integer noteId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_date", nullable = false)
    private Date updateDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "observation_date", nullable = false)
    private Date observationDate;

    @Column(name = "demographic_no", nullable = false)
    private Integer demographicNo;

    @Column(name = "provider_no", nullable = false)
    private String providerNo;

    @Lob
    @Column(name = "note", nullable = false)
    private String note;

    @Column(name = "signed", nullable = false)
    private boolean signed;

    @Column(name = "include_issue_innote", nullable = false)
    private boolean includeIssueInNote;

    @Column(name = "signing_provider_no", nullable = false)
    private String signingProviderNo;

    @Column(name = "encounter_type", nullable = false)
    private String encounterType;

    @Column(name = "billing_code", nullable = false)
    private String billingCode;

    @Column(name = "program_no", nullable = false)
    private String programNo;

    @Column(name = "reporter_caisi_role", nullable = false)
    private String reporterCaisiRole;

    @Column(name = "reporter_program_team", nullable = false)
    private String reporterProgramTeam;

    @Lob
    @Column(name = "history", nullable = false)
    private String history;

    @Column(name = "password")
    private String password;

    @Column(name = "locked")
    private String locked;

    @Column(name = "archived")
    private Boolean archived;

    @Column(name = "position")
    private Integer position;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "appointmentNo")
    private Integer appointmentNo;

    @Column(name = "hourOfEncounterTime")
    private Integer hourOfEncounterTime;

    @Column(name = "minuteOfEncounterTime")
    private Integer minuteOfEncounterTime;

    @Column(name = "hourOfEncTransportationTime")
    private Integer hourOfEncTransportationTime;

    @Column(name = "minuteOfEncTransportationTime")
    private Integer minuteOfEncTransportationTime;

    public Integer getNoteId() { return noteId; }
    public void setNoteId(Integer noteId) { this.noteId = noteId; }

    /** Alias so the generic copy helper can use getId()/setId() uniformly. */
    public Integer getId() { return noteId; }
    public void setId(Integer id) { this.noteId = id; }

    public Integer getDemographicNo() { return demographicNo; }
    public void setDemographicNo(Integer demographicNo) { this.demographicNo = demographicNo; }

    public Date getUpdateDate() { return updateDate; }
    public void setUpdateDate(Date updateDate) { this.updateDate = updateDate; }

    public Date getObservationDate() { return observationDate; }
    public void setObservationDate(Date observationDate) { this.observationDate = observationDate; }

    public String getProviderNo() { return providerNo; }
    public void setProviderNo(String providerNo) { this.providerNo = providerNo; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isSigned() { return signed; }
    public void setSigned(boolean signed) { this.signed = signed; }

    public boolean isIncludeIssueInNote() { return includeIssueInNote; }
    public void setIncludeIssueInNote(boolean includeIssueInNote) { this.includeIssueInNote = includeIssueInNote; }

    public String getSigningProviderNo() { return signingProviderNo; }
    public void setSigningProviderNo(String signingProviderNo) { this.signingProviderNo = signingProviderNo; }

    public String getEncounterType() { return encounterType; }
    public void setEncounterType(String encounterType) { this.encounterType = encounterType; }

    public String getBillingCode() { return billingCode; }
    public void setBillingCode(String billingCode) { this.billingCode = billingCode; }

    public String getProgramNo() { return programNo; }
    public void setProgramNo(String programNo) { this.programNo = programNo; }

    public String getReporterCaisiRole() { return reporterCaisiRole; }
    public void setReporterCaisiRole(String reporterCaisiRole) { this.reporterCaisiRole = reporterCaisiRole; }

    public String getReporterProgramTeam() { return reporterProgramTeam; }
    public void setReporterProgramTeam(String reporterProgramTeam) { this.reporterProgramTeam = reporterProgramTeam; }

    public String getHistory() { return history; }
    public void setHistory(String history) { this.history = history; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getLocked() { return locked; }
    public void setLocked(String locked) { this.locked = locked; }

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Integer getAppointmentNo() { return appointmentNo; }
    public void setAppointmentNo(Integer appointmentNo) { this.appointmentNo = appointmentNo; }

    public Integer getHourOfEncounterTime() { return hourOfEncounterTime; }
    public void setHourOfEncounterTime(Integer hourOfEncounterTime) { this.hourOfEncounterTime = hourOfEncounterTime; }

    public Integer getMinuteOfEncounterTime() { return minuteOfEncounterTime; }
    public void setMinuteOfEncounterTime(Integer minuteOfEncounterTime) { this.minuteOfEncounterTime = minuteOfEncounterTime; }

    public Integer getHourOfEncTransportationTime() { return hourOfEncTransportationTime; }
    public void setHourOfEncTransportationTime(Integer h) { this.hourOfEncTransportationTime = h; }

    public Integer getMinuteOfEncTransportationTime() { return minuteOfEncTransportationTime; }
    public void setMinuteOfEncTransportationTime(Integer m) { this.minuteOfEncTransportationTime = m; }
}
