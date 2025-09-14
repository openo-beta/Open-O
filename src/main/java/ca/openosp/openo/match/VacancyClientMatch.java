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
package ca.openosp.openo.match;

import java.util.Date;

/**
 * Healthcare patient-provider match result with scoring and status tracking.
 *
 * <p>This class represents the result of matching a specific patient (client) to a
 * healthcare provider vacancy (appointment slot). Each match includes a comprehensive
 * scoring system that evaluates how well the patient fits the vacancy requirements,
 * along with status tracking for the healthcare workflow management.</p>
 *
 * <p>Healthcare Matching Features:
 * <ul>
 *   <li>Weighted percentage matching score (0-100%)</li>
 *   <li>Match proportion display (matched criteria / total criteria)</li>
 *   <li>Workflow status tracking (pending, accepted, forwarded, rejected)</li>
 *   <li>Contact attempt logging for patient engagement</li>
 *   <li>Rejection reason tracking for quality improvement</li>
 * </ul>
 * </p>
 *
 * <p>Match Status Workflow:
 * <ol>
 *   <li>PENDING - Initial match created, awaiting review</li>
 *   <li>FORWARDED - Match sent to patient for consideration</li>
 *   <li>ACCEPTED - Patient accepts the provider appointment</li>
 *   <li>REJECTED - Match declined with documented reason</li>
 * </ol>
 * </p>
 *
 * <p>Sorting Behavior: Matches are automatically sorted by match percentage in
 * descending order (highest matches first) to prioritize optimal patient-provider
 * assignments for healthcare workflow efficiency.</p>
 *
 * @see Matcher
 * @see VacancyData
 * @see ClientData
 * @since 2012-11-10
 */
public class VacancyClientMatch implements Comparable<VacancyClientMatch> {
    /**
     * Healthcare workflow status for patient-provider matches.
     *
     * <p>These status values track the progression of a patient-provider match
     * through the healthcare appointment scheduling workflow.</p>
     */
    public enum VacancyClientMatchStatus {
        /** Patient has accepted the provider appointment match */
        ACCEPTED,

        /** Match has been forwarded to patient for consideration */
        FORWARDED,

        /** Initial match state awaiting healthcare workflow action */
        PENDING,

        /** Patient or provider has declined the match with documented reason */
        REJECTED;
    }

    /** Constant for PENDING status - used in matching algorithms */
    public static final VacancyClientMatchStatus PENDING = VacancyClientMatchStatus.PENDING;

    /** Unique identifier for this match record */
    private Integer id;

    /** Patient identifier (demographic number) */
    private int client_id;

    /** Healthcare provider vacancy identifier */
    private int vacancy_id;

    /** Assessment form identifier used for matching */
    private int form_id;

    /** Number of contact attempts made to patient */
    private int contactAttempts;

    /** Date of last contact attempt with patient */
    private Date last_contact_date;

    /** Current workflow status of this match */
    private VacancyClientMatchStatus status = VacancyClientMatchStatus.PENDING;

    /** Reason provided when match is rejected */
    private String rejectionReason;

    /** Calculated match score as percentage (0.0-100.0) */
    private double matchPercentage;

    /** Human-readable proportion of matched criteria (e.g., "3/5") */
    private String proportion;

    /**
     * Gets the unique identifier for this match record.
     *
     * @return Integer the match record ID, null for new unsaved matches
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this match record.
     *
     * @param id Integer the match record ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the patient identifier for this match.
     *
     * @return int the patient demographic number
     */
    public int getClient_id() {
        return client_id;
    }

    /**
     * Sets the patient identifier for this match.
     *
     * @param client_id int the patient demographic number
     */
    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    /**
     * Gets the healthcare provider vacancy identifier for this match.
     *
     * @return int the vacancy ID
     */
    public int getVacancy_id() {
        return vacancy_id;
    }

    /**
     * Sets the healthcare provider vacancy identifier for this match.
     *
     * @param vacancy_id int the vacancy ID
     */
    public void setVacancy_id(int vacancy_id) {
        this.vacancy_id = vacancy_id;
    }

    /**
     * Gets the assessment form identifier used for this match.
     *
     * @return int the form ID that provided patient data for matching
     */
    public int getForm_id() {
        return form_id;
    }

    /**
     * Sets the assessment form identifier used for this match.
     *
     * @param form_id int the form ID that provided patient data for matching
     */
    public void setForm_id(int form_id) {
        this.form_id = form_id;
    }

    /**
     * Gets the number of contact attempts made to the patient.
     *
     * @return int number of times healthcare staff attempted to contact the patient
     */
    public int getContactAttempts() {
        return contactAttempts;
    }

    /**
     * Sets the number of contact attempts made to the patient.
     *
     * @param contactAttempts int number of contact attempts
     */
    public void setContactAttempts(int contactAttempts) {
        this.contactAttempts = contactAttempts;
    }

    /**
     * Gets the date of the last contact attempt with the patient.
     *
     * @return Date the most recent contact attempt date, null if no contact attempted
     */
    public Date getLast_contact_date() {
        return last_contact_date;
    }

    /**
     * Sets the date of the last contact attempt with the patient.
     *
     * @param last_contact_date Date the contact attempt date
     */
    public void setLast_contact_date(Date last_contact_date) {
        this.last_contact_date = last_contact_date;
    }

    /**
     * Gets the current healthcare workflow status of this match.
     *
     * @return VacancyClientMatchStatus the current status (PENDING, FORWARDED, ACCEPTED, REJECTED)
     */
    public VacancyClientMatchStatus getStatus() {
        return status;
    }

    /**
     * Sets the current healthcare workflow status of this match.
     *
     * @param status VacancyClientMatchStatus the new status
     */
    public void setStatus(VacancyClientMatchStatus status) {
        this.status = status;
    }

    /**
     * Gets the reason provided when this match was rejected.
     *
     * @return String the rejection reason, null if match not rejected
     */
    public String getRejectionReason() {
        return rejectionReason;
    }

    /**
     * Sets the reason provided when this match was rejected.
     *
     * @param rejectionReason String the rejection reason for quality improvement tracking
     */
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    /**
     * Gets the calculated match score as a percentage.
     *
     * @return double the match score (0.0-100.0) indicating how well patient fits vacancy
     */
    public double getMatchPercentage() {
        return matchPercentage;
    }

    /**
     * Sets the calculated match score as a percentage.
     *
     * @param matchPercentage double the match score (0.0-100.0)
     */
    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    /**
     * Gets the human-readable proportion of matched criteria.
     *
     * @return String the match proportion (e.g., "3/5" meaning 3 out of 5 criteria matched)
     */
    public String getProportion() {
        return proportion;
    }

    /**
     * Sets the human-readable proportion of matched criteria.
     *
     * @param proportion String the match proportion display
     */
    public void setProportion(String proportion) {
        this.proportion = proportion;
    }

    /**
     * Generates hash code based on patient ID, vacancy ID, and form ID.
     *
     * <p>This ensures that matches with the same patient, vacancy, and form
     * are considered equivalent for collections and database operations.</p>
     *
     * @return int hash code for this match
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + client_id;
        result = prime * result + form_id;
        result = prime * result + vacancy_id;
        return result;
    }

    /**
     * Compares this match with another for equality based on patient, vacancy, and form.
     *
     * <p>Two matches are considered equal if they involve the same patient,
     * vacancy, and assessment form, regardless of status or score differences.</p>
     *
     * @param obj Object the other match to compare
     * @return boolean true if matches represent the same patient-vacancy-form combination
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VacancyClientMatch)) {
            return false;
        }
        VacancyClientMatch other = (VacancyClientMatch) obj;
        if (client_id != other.client_id) {
            return false;
        }
        if (form_id != other.form_id) {
            return false;
        }
        if (vacancy_id != other.vacancy_id) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of this healthcare match.
     *
     * @return String detailed string representation including all match properties
     */
    @Override
    public String toString() {
        return "VacancyClientMatch [id=" + id + ", client_id=" + client_id + ", vacancy_id=" + vacancy_id
            + ", form_id=" + form_id + ", contactAttempts=" + contactAttempts
            + ", last_contact_date=" + last_contact_date + ", status=" + status
            + ", rejectionReason=" + rejectionReason + ", matchPercentage=" + matchPercentage
            + ", proportion=" + proportion + "]";
    }

    /**
     * Compares matches for sorting by match percentage (highest scores first).
     *
     * <p>This comparison ensures that patient-provider matches are automatically
     * sorted with the best matches (highest percentages) appearing first in
     * healthcare workflow lists and recommendations.</p>
     *
     * @param o VacancyClientMatch the other match to compare
     * @return int negative if this match is better, positive if other is better, zero if equal
     */
    @Override
    public int compareTo(VacancyClientMatch o) {
        // Sort by match percentage descending (best matches first)
        return (int) (o.matchPercentage - this.matchPercentage);
    }

}
