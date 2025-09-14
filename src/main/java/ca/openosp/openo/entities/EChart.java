//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */

package ca.openosp.openo.entities;

/**
 * Healthcare electronic chart entity representing comprehensive patient chart information.
 * This entity encapsulates the data from the eChart table, which stores structured
 * patient information including social history, family history, medical history,
 * ongoing concerns, and clinical encounters.
 *
 * The EChart serves as a centralized repository for patient information that supports
 * clinical decision-making and provides a comprehensive view of the patient's health status.
 * Each chart is associated with a specific patient (demographic) and healthcare provider.
 *
 * @see ca.openosp.openo.commn.model.Demographic
 * @see ca.openosp.openo.commn.model.Provider
 * @since November 1, 2004
 */
public class EChart {
    /**
     * Auto-increment unique identifier for this electronic chart record
     */
    private int eChartId;

    /**
     * Timestamp indicating when this chart was last updated
     */
    private String timeStamp;

    /**
     * Reference to the patient demographic record
     */
    private int demographicNo;

    /**
     * Provider number of the healthcare provider responsible for this chart
     */
    private String providerNo;

    /**
     * Subject or title summarizing the chart content
     */
    private String subject;

    /**
     * Patient's social history including lifestyle factors and social determinants of health
     */
    private String socialHistory;

    /**
     * Patient's family medical history including hereditary conditions and genetic factors
     */
    private String familyHistory;

    /**
     * Patient's past medical history including previous diagnoses, surgeries, and treatments
     */
    private String medicalHistory;

    /**
     * Current ongoing medical concerns and active health issues
     */
    private String ongoingConcerns;

    /**
     * Clinical reminders and follow-up notes for future care
     */
    private String reminders;

    /**
     * Details of the current clinical encounter or visit
     */
    private String encounter;

    /**
     * Default constructor creating an empty EChart instance.
     * All fields will be initialized to their default values.
     */
    public EChart() {
    }

    /**
     * Full constructor creating an EChart instance with all field values.
     *
     * @param eChartId        int the unique electronic chart ID
     * @param timeStamp       String the timestamp of last update
     * @param demographicNo   int the patient demographic record number
     * @param providerNo      String the healthcare provider number
     * @param subject         String the chart subject or title
     * @param socialHistory   String the patient's social history
     * @param familyHistory   String the patient's family medical history
     * @param medicalHistory  String the patient's past medical history
     * @param ongoingConcerns String current ongoing medical concerns
     * @param reminders       String clinical reminders and follow-up notes
     * @param encounter       String details of the current clinical encounter
     */
    public EChart(int eChartId, String timeStamp, int demographicNo,
                  String providerNo, String subject, String socialHistory,
                  String familyHistory, String medicalHistory,
                  String ongoingConcerns, String reminders, String encounter) {
        this.eChartId = eChartId;
        this.timeStamp = timeStamp;
        this.demographicNo = demographicNo;
        this.providerNo = providerNo;
        this.subject = subject;
        this.socialHistory = socialHistory;
        this.familyHistory = familyHistory;
        this.medicalHistory = medicalHistory;
        this.ongoingConcerns = ongoingConcerns;
        this.reminders = reminders;
        this.encounter = encounter;
    }

    /**
     * Gets the unique identifier for this electronic chart record.
     *
     * @return int the auto-increment electronic chart ID
     */
    public int getEChartId() {
        return eChartId;
    }

    /**
     * Gets the timestamp indicating when this chart was last updated.
     *
     * @return String the timestamp of last update
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * Gets the reference to the patient demographic record.
     *
     * @return int the patient demographic record number
     */
    public int getDemographicNo() {
        return demographicNo;
    }

    /**
     * Gets the provider number of the healthcare provider responsible for this chart.
     *
     * @return String the healthcare provider number, never null (empty string if null)
     */
    public String getProviderNo() {
        return (providerNo != null ? providerNo : "");
    }

    /**
     * Gets the subject or title summarizing the chart content.
     *
     * @return String the chart subject, never null (empty string if null)
     */
    public String getSubject() {
        return (subject != null ? subject : "");
    }

    /**
     * Gets the patient's social history including lifestyle factors.
     *
     * @return String the social history, never null (empty string if null)
     */
    public String getSocialHistory() {
        return (socialHistory != null ? socialHistory : "");
    }

    /**
     * Gets the patient's family medical history.
     *
     * @return String the family history including hereditary conditions, never null (empty string if null)
     */
    public String getFamilyHistory() {
        return (familyHistory != null ? familyHistory : "");
    }

    /**
     * Gets the patient's past medical history.
     *
     * @return String the medical history including previous diagnoses and treatments, never null (empty string if null)
     */
    public String getMedicalHistory() {
        return (medicalHistory != null ? medicalHistory : "");
    }

    /**
     * Gets the current ongoing medical concerns and active health issues.
     *
     * @return String the ongoing concerns, never null (empty string if null)
     */
    public String getOngoingConcerns() {
        return (ongoingConcerns != null ? ongoingConcerns : "");
    }

    /**
     * Gets the clinical reminders and follow-up notes for future care.
     *
     * @return String the reminders, never null (empty string if null)
     */
    public String getReminders() {
        return (reminders != null ? reminders : "");
    }

    /**
     * Gets the details of the current clinical encounter or visit.
     *
     * @return String the encounter details, never null (empty string if null)
     */
    public String getEncounter() {
        return (encounter != null ? encounter : "");
    }

    /**
     * Sets the unique identifier for this electronic chart record.
     *
     * @param eChartId int the electronic chart ID
     */
    public void setEChartId(int eChartId) {
        this.eChartId = eChartId;
    }

    /**
     * Sets the timestamp indicating when this chart was last updated.
     *
     * @param timeStamp String the timestamp of last update
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Sets the reference to the patient demographic record.
     *
     * @param demographicNo int the patient demographic record number
     */
    public void setDemographicNo(int demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Sets the provider number of the healthcare provider responsible for this chart.
     *
     * @param providerNo String the healthcare provider number
     */
    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * Sets the subject or title summarizing the chart content.
     *
     * @param subject String the chart subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Sets the patient's social history including lifestyle factors.
     *
     * @param socialHistory String the social history
     */
    public void setSocialHistory(String socialHistory) {
        this.socialHistory = socialHistory;
    }

    /**
     * Sets the patient's family medical history.
     *
     * @param familyHistory String the family history including hereditary conditions
     */
    public void setFamilyHistory(String familyHistory) {
        this.familyHistory = familyHistory;
    }

    /**
     * Sets the patient's past medical history.
     *
     * @param medicalHistory String the medical history including previous diagnoses and treatments
     */
    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    /**
     * Sets the current ongoing medical concerns and active health issues.
     *
     * @param ongoingConcerns String the ongoing concerns
     */
    public void setOngoingConcerns(String ongoingConcerns) {
        this.ongoingConcerns = ongoingConcerns;
    }

    /**
     * Sets the clinical reminders and follow-up notes for future care.
     *
     * @param reminders String the reminders
     */
    public void setReminders(String reminders) {
        this.reminders = reminders;
    }

    /**
     * Sets the details of the current clinical encounter or visit.
     *
     * @param encounter String the encounter details
     */
    public void setEncounter(String encounter) {
        this.encounter = encounter;
    }

}
