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
 * Prescription entity representing medication prescriptions and their printing history
 * within OpenO EMR. This entity tracks prescription issuance, printing status, and
 * reprint history for medication management and regulatory compliance in healthcare
 * settings.
 *
 * <p>This entity serves as a data transfer object for prescription-related operations,
 * including prescription generation, printing workflows, and audit trails. It supports
 * the medication management lifecycle from initial prescription through reprinting
 * for patient or pharmacy use.
 *
 * <p>Key prescription management features include:
 * <ul>
 * <li>Prescription identification through script numbers and demographic links
 * <li>Provider association for prescribing authority tracking
 * <li>Date tracking for prescription issuance and printing history
 * <li>Reprint history for audit and compliance purposes
 * <li>Text-based prescription content for flexible medication formatting
 * <li>Integration with patient demographic and provider systems
 * </ul>
 *
 * <p>The prescription workflow typically involves:
 * <ol>
 * <li>Prescription creation with provider and patient association
 * <li>Initial printing with date stamping
 * <li>Potential reprinting with audit trail maintenance
 * <li>Text view rendering for display and printing purposes
 * </ol>
 *
 * @see ca.openosp.openo.entities.Patient
 * @see ca.openosp.openo.entities.Provider
 * @see ca.openosp.openo.commn.model.Demographic
 * @since November 1, 2004
 */
public class Prescription {
    private String scriptNo;
    private String providerNo;
    private String demographicNo;
    private String datePrescribed;
    private String datePrinted;
    private String datesReprinted;
    private String textView;

    /**
     * Default constructor for Prescription entity.
     * Creates a new Prescription instance with all fields initialized to null.
     */
    public Prescription() {
    }

    /**
     * Gets the prescription script number (unique identifier).
     * This number uniquely identifies the prescription within the system
     * and is used for tracking and reference purposes.
     *
     * @return String the prescription script number
     */
    public String getScriptNo() {
        return scriptNo;
    }

    /**
     * Sets the prescription script number (unique identifier).
     *
     * @param scriptNo String the prescription script number
     */
    public void setScriptNo(String scriptNo) {
        this.scriptNo = scriptNo;
    }

    /**
     * Gets the provider number of the healthcare provider who issued this prescription.
     * This identifies the prescribing authority and is essential for regulatory
     * compliance and clinical responsibility tracking.
     *
     * @return String the provider number
     * @see ca.openosp.openo.entities.Provider
     */
    public String getProviderNo() {
        return providerNo;
    }

    /**
     * Sets the provider number of the healthcare provider who issued this prescription.
     *
     * @param providerNo String the provider number
     */
    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * Gets the demographic number (patient identifier) for whom this prescription was issued.
     * This links the prescription to the specific patient receiving the medication.
     *
     * @return String the patient's demographic number
     * @see ca.openosp.openo.entities.Patient
     */
    public String getDemographicNo() {
        return demographicNo;
    }

    /**
     * Sets the demographic number (patient identifier) for whom this prescription was issued.
     *
     * @param demographicNo String the patient's demographic number
     */
    public void setDemographicNo(String demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Gets the date when the prescription was originally written/prescribed.
     * This date is crucial for medication validity, refill calculations,
     * and regulatory compliance.
     *
     * @return String the date the prescription was prescribed
     */
    public String getDatePrescribed() {
        return datePrescribed;
    }

    /**
     * Sets the date when the prescription was originally written/prescribed.
     *
     * @param datePrescribed String the date the prescription was prescribed
     */
    public void setDatePrescribed(String datePrescribed) {
        this.datePrescribed = datePrescribed;
    }

    /**
     * Gets the date when the prescription was first printed.
     * This date tracks the initial printing event for audit and workflow purposes.
     *
     * @return String the date the prescription was first printed
     */
    public String getDatePrinted() {
        return datePrinted;
    }

    /**
     * Sets the date when the prescription was first printed.
     *
     * @param datePrinted String the date the prescription was first printed
     */
    public void setDatePrinted(String datePrinted) {
        this.datePrinted = datePrinted;
    }

    /**
     * Gets the dates when the prescription was reprinted.
     * This field maintains an audit trail of all reprint events, which is
     * important for tracking potential medication abuse and regulatory compliance.
     *
     * @return String the dates when the prescription was reprinted
     */
    public String getDatesReprinted() {
        return datesReprinted;
    }

    /**
     * Sets the dates when the prescription was reprinted.
     * Multiple dates may be stored to maintain a complete audit trail.
     *
     * @param datesReprinted String the dates when the prescription was reprinted
     */
    public void setDatesReprinted(String datesReprinted) {
        this.datesReprinted = datesReprinted;
    }

    /**
     * Gets the formatted text representation of the prescription.
     * This field contains the complete prescription content as it would appear
     * when printed, including medication details, dosage, instructions, and
     * prescriber information.
     *
     * @return String the formatted prescription text
     */
    public String getTextView() {
        return textView;
    }

    /**
     * Sets the formatted text representation of the prescription.
     * This should include all necessary prescription elements for proper
     * medication dispensing and patient instructions.
     *
     * @param textView String the formatted prescription text
     */
    public void setTextView(String textView) {
        this.textView = textView;
    }
}
