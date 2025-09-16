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


package ca.openosp.openo.mds.data;

/**
 * Represents aggregated patient information for laboratory result management and display.
 * <p>
 * This class encapsulates patient demographic information along with aggregated counts of various
 * laboratory result types. It is primarily used in the laboratory inbox and summary views to provide
 * healthcare providers with an overview of pending laboratory work for each patient.
 * <p>
 * Key features include:
 * <ul>
 * <li>Patient demographic identification (name, demographic ID)</li>
 * <li>Document-type laboratory result counts</li>
 * <li>HL7 laboratory result counts</li>
 * <li>HRM (Hospital Report Manager) document counts</li>
 * <li>Comparable implementation for alphabetical sorting by name</li>
 * </ul>
 * The class implements Comparable to support consistent alphabetical ordering in patient lists,
 * sorting first by last name, then by first name for patients with the same last name.
 *
 * @since June 18, 2011
 */
public class PatientInfo implements Comparable<PatientInfo> {
    /** Patient's first name for display and sorting purposes */
    private String firstName = "";
    /** Patient's last name for display and primary sorting criteria */
    private String lastName = "";
    /** Patient demographic ID for database linking and identification */
    private int id;
    /** Count of document-type laboratory results for this patient */
    private int docCount = 0;
    /** Count of HL7 laboratory results for this patient */
    private int labCount = 0;
    /** Count of HRM (Hospital Report Manager) documents for this patient */
    private int hrmCount = 0;

    /**
     * Returns a formatted string representation of the patient name.
     * <p>
     * Formats the patient name in clinical display format as "Last, First",
     * handling cases where the last name might be empty gracefully.
     *
     * @return String formatted patient name in "Last, First" format
     */
    @Override
    public String toString() {
        return lastName + ("".equals(lastName) ? "" : ", ") + firstName;
    }

    /**
     * Determines equality based on patient demographic ID.
     * <p>
     * Two PatientInfo objects are considered equal if they represent the same
     * patient demographic, regardless of name or count differences.
     *
     * @param obj Object to compare with this PatientInfo
     * @return boolean true if objects represent the same patient demographic
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof PatientInfo && ((PatientInfo) obj).id == this.id;
    }

    /**
     * Constructs a PatientInfo instance with demographic identification information.
     * <p>
     * This constructor initializes the patient information with basic demographics.
     * Laboratory result counts are initialized to zero and can be updated using
     * the appropriate setter methods as data is aggregated.
     *
     * @param id int the patient demographic ID for database linking
     * @param firstName String the patient's first name
     * @param lastName String the patient's last name
     */
    public PatientInfo(int id, String firstName, String lastName) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Gets the patient's first name.
     *
     * @return String the patient's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the patient's first name.
     *
     * @param firstName String the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the patient's last name.
     *
     * @return String the patient's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the patient's last name.
     *
     * @param lastName String the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the patient demographic ID.
     *
     * @return int the patient's demographic identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the patient demographic ID.
     *
     * @param id int the demographic identifier to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the count of document-type laboratory results for this patient.
     *
     * @return int the number of document-type lab results
     */
    public int getDocCount() {
        return docCount;
    }

    /**
     * Sets the count of document-type laboratory results for this patient.
     *
     * @param docCount int the document count to set
     */
    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }

    /**
     * Gets the count of HL7 laboratory results for this patient.
     *
     * @return int the number of HL7 lab results
     */
    public int getLabCount() {
        return labCount;
    }

    /**
     * Adds to the count of HL7 laboratory results for this patient.
     * <p>
     * Note: This method adds to the existing count rather than replacing it,
     * allowing for aggregation across multiple queries.
     *
     * @param labCount int the lab count to add to the current total
     */
    public void setLabCount(int labCount) {
        this.labCount += labCount;
    }
    /**
     * Gets the count of HRM (Hospital Report Manager) documents for this patient.
     *
     * @return int the number of HRM documents
     */
    public int getHrmCount() {
        return hrmCount;
    }
    /**
     * Adds to the count of HRM (Hospital Report Manager) documents for this patient.
     * <p>
     * Note: This method adds to the existing count rather than replacing it,
     * allowing for aggregation across multiple queries.
     *
     * @param hrmCount int the HRM document count to add to the current total
     */
    public void setHrmCount(int hrmCount) {
        this.hrmCount += hrmCount;
    }
    /**
     * Compares patients for alphabetical sorting by name.
     * <p>
     * Implements natural ordering by last name first, then by first name for patients
     * with the same last name. This ensures consistent alphabetical ordering in
     * patient lists and laboratory inbox displays.
     *
     * @param that PatientInfo the other patient to compare with
     * @return int negative if this patient comes before, positive if after, zero if equal names
     */
    @Override
    public int compareTo(PatientInfo that) {
        return this.lastName.equals(that.lastName) ? this.firstName.compareTo(that.firstName) : this.lastName.compareTo(that.lastName);
    }
}
