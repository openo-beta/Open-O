//CHECKSTYLE:OFF
/**
 * Copyright (c) 2015-2019. The Pharmacists Clinic, Faculty of Pharmaceutical Sciences, University of British Columbia. All Rights Reserved.
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
 * The Pharmacists Clinic
 * Faculty of Pharmaceutical Sciences
 * University of British Columbia
 * Vancouver, British Columbia, Canada
 */

package ca.openosp.openo.fax.core;

import java.util.Date;

import ca.openosp.openo.commn.model.FaxJob.STATUS;

import net.sf.json.JSONObject;

/**
 * Represents a healthcare provider or facility that will receive medical fax transmissions.
 *
 * This class encapsulates the essential information needed to identify and contact healthcare
 * recipients for medical document transmission. In healthcare environments, accurate recipient
 * information is critical for ensuring that sensitive medical documents reach the correct
 * providers, specialists, laboratories, or medical facilities.
 *
 * The FaxRecipient class supports various healthcare communication scenarios:
 * - Specialist referrals requiring consultation requests and patient information
 * - Laboratory orders and result transmissions between providers and labs
 * - Inter-facility patient transfers with comprehensive medical records
 * - Prescription transmissions to pharmacies and medication management
 * - Insurance and regulatory reporting with proper recipient verification
 *
 * Key features include:
 * - Automatic fax number normalization to ensure consistent formatting
 * - Integration with JSON-based recipient selection from healthcare directories
 * - Status tracking for transmission confirmation and delivery verification
 * - Timestamp management for audit trails and regulatory compliance
 *
 * The class maintains recipient contact information in a standardized format that
 * supports both automated fax transmission systems and manual verification processes,
 * ensuring reliable delivery of critical healthcare information.
 *
 * @see ca.openosp.openo.commn.model.FaxJob
 * @see ca.openosp.openo.fax.util.PdfCoverPageCreator
 * @since 2022-04-21
 */
public class FaxRecipient {

    /** Name of the healthcare provider, facility, or department receiving the fax */
    private String name;

    /** Normalized fax number for the recipient (digits only) */
    private String fax;

    /** Timestamp when the fax transmission was sent */
    private Date sent;

    /** Current transmission status (WAITING, SENT, ERROR, etc.) */
    private STATUS status;

    /**
     * Default constructor for creating an empty fax recipient.
     */
    public FaxRecipient() {
        //default
    }

    /**
     * Constructs a fax recipient from JSON data, typically from healthcare provider directories.
     *
     * This constructor supports integration with healthcare provider lookup systems and
     * directory services that provide recipient information in JSON format. The fax number
     * is automatically normalized during construction.
     *
     * @param json JSONObject containing recipient information with "name" and "fax" fields
     */
    public FaxRecipient(JSONObject json) {
        this.name = json.getString("name");
        this.setFax(json.getString("fax"));
    }

    /**
     * Constructs a fax recipient with name and fax number.
     *
     * This constructor provides direct creation of recipient information for healthcare
     * scenarios where provider details are known. The fax number is automatically
     * normalized to ensure consistent formatting.
     *
     * @param name String name of the healthcare provider or facility
     * @param fax String fax number (will be normalized to digits only)
     */
    public FaxRecipient(String name, String fax) {
        this.name = name;
        this.setFax(fax);
    }

    /**
     * Gets the name of the healthcare recipient.
     *
     * @return String name of the healthcare provider, facility, or department
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the healthcare recipient.
     *
     * @param name String name of the healthcare provider, facility, or department
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the normalized fax number for medical document transmission.
     *
     * @return String fax number containing only digits
     */
    public String getFax() {
        return fax;
    }

    /**
     * Sets and normalizes the fax number for consistent healthcare communication.
     *
     * This method automatically removes all non-digit characters from the fax number
     * to ensure consistent formatting across different healthcare provider directory
     * sources and user input methods. Proper fax number formatting is critical for
     * reliable medical document transmission.
     *
     * @param fax String fax number (will be normalized to digits only)
     */
    public void setFax(String fax) {
        if (fax != null && !fax.trim().isEmpty()) {
            // Normalize fax number by removing all non-digit characters
            this.fax = fax.replaceAll("\\D", "").trim();
        }
    }

    /**
     * Gets the timestamp when the medical fax transmission was sent.
     *
     * @return Date timestamp of transmission, or null if not yet sent
     */
    public Date getSent() {
        return sent;
    }

    /**
     * Sets the timestamp when the medical fax transmission was sent.
     *
     * @param sent Date timestamp of transmission for audit tracking
     */
    public void setSent(Date sent) {
        this.sent = sent;
    }

    /**
     * Gets the current transmission status for this medical fax recipient.
     *
     * @return STATUS current transmission status (WAITING, SENT, ERROR, etc.)
     */
    public STATUS getStatus() {
        return status;
    }

    /**
     * Sets the transmission status for tracking delivery of medical documents.
     *
     * @param status STATUS transmission status for audit and monitoring purposes
     */
    public void setStatus(STATUS status) {
        this.status = status;
    }

}
