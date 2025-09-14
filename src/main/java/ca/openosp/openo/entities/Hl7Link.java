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
 * Healthcare HL7 link entity representing the relationship between HL7 messages and patient demographics.
 * This entity encapsulates the data from the hl7_link table, which establishes connections between
 * incoming HL7 messages (laboratory results, reports, etc.) and specific patient records in the system.
 *
 * The Hl7Link serves as a mapping mechanism to associate HL7 Patient Identification (PID) segments
 * with demographic records, enabling automatic matching of incoming healthcare data with the correct
 * patient profiles. It also tracks the processing status and provider ownership of these links.
 *
 * @see Hl7Message
 * @see ca.openosp.openo.commn.model.Demographic
 * @see ca.openosp.openo.commn.model.Provider
 * @since November 1, 2004
 */
public class Hl7Link {
    /**
     * Unique identifier from the HL7 PID (Patient Identification) segment
     */
    private int pidId;

    /**
     * Reference to the patient demographic record that this HL7 data relates to
     */
    private int demographicNo;

    /**
     * Processing status of this HL7 link (e.g., 'A' for active, 'I' for inactive)
     */
    private String status;

    /**
     * Provider number of the healthcare provider responsible for this link
     */
    private String providerNo;

    /**
     * Date and time when this link was signed or acknowledged by the provider
     */
    private String signedOn;

    /**
     * Default constructor creating an empty Hl7Link instance.
     * All fields will be initialized to their default values.
     */
    public Hl7Link() {
    }

    /**
     * Full constructor creating an Hl7Link instance with all field values.
     *
     * @param pidId         int the HL7 PID segment identifier
     * @param demographicNo int the patient demographic record number
     * @param status        String the processing status
     * @param providerNo    String the healthcare provider number
     * @param signedOn      String the date and time when signed
     */
    public Hl7Link(int pidId, int demographicNo, String status, String providerNo,
                   String signedOn) {
        this.pidId = pidId;
        this.demographicNo = demographicNo;
        this.status = status;
        this.providerNo = providerNo;
        this.signedOn = signedOn;
    }

    /**
     * Gets the unique identifier from the HL7 PID segment.
     *
     * @return int the HL7 Patient Identification segment ID
     */
    public int getPidId() {
        return pidId;
    }

    /**
     * Gets the reference to the patient demographic record.
     *
     * @return int the demographic record number that this HL7 data relates to
     */
    public int getDemographicNo() {
        return demographicNo;
    }

    /**
     * Gets the processing status of this HL7 link.
     *
     * @return String the status (e.g., 'A' for active, 'I' for inactive), never null (empty string if null)
     */
    public String getStatus() {
        return (status != null ? status : "");
    }

    /**
     * Gets the provider number of the healthcare provider responsible for this link.
     *
     * @return String the healthcare provider number, never null (empty string if null)
     */
    public String getProviderNo() {
        return (providerNo != null ? providerNo : "");
    }

    /**
     * Gets the date and time when this link was signed or acknowledged.
     *
     * @return String the signed on timestamp
     */
    public String getSignedOn() {
        return signedOn;
    }

    /**
     * Sets the unique identifier from the HL7 PID segment.
     *
     * @param pidId int the HL7 Patient Identification segment ID
     */
    public void setPidId(int pidId) {
        this.pidId = pidId;
    }

    /**
     * Sets the reference to the patient demographic record.
     *
     * @param demographicNo int the demographic record number
     */
    public void setDemographicNo(int demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Sets the processing status of this HL7 link.
     *
     * @param status String the status (e.g., 'A' for active, 'I' for inactive)
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets the provider number of the healthcare provider responsible for this link.
     *
     * @param providerNo String the healthcare provider number
     */
    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * Sets the date and time when this link was signed or acknowledged.
     *
     * @param signedOn String the signed on timestamp
     */
    public void setSignedOn(String signedOn) {
        this.signedOn = signedOn;
    }
}
