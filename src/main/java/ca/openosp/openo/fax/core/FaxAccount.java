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

import ca.openosp.openo.commn.model.FaxConfig;

/**
 * Represents a healthcare facility's fax account information for medical document transmission.
 *
 * This class encapsulates the essential contact and identification information for healthcare
 * facilities participating in fax-based medical communications. Fax transmission remains a critical
 * component of healthcare infrastructure due to legal requirements, regulatory compliance, and
 * the need for secure document exchange between providers.
 *
 * The FaxAccount provides structured storage for:
 * - Healthcare facility identification and branding information
 * - Contact details for sender identification on transmitted documents
 * - Letterhead information for professional medical correspondence
 * - Address and telecommunications data for recipient verification
 *
 * This information is used to:
 * - Generate professional cover pages with facility branding
 * - Provide sender identification on all transmitted medical documents
 * - Enable recipient verification of document authenticity
 * - Support regulatory compliance for healthcare communications
 * - Maintain professional standards for inter-provider communications
 *
 * The account information ensures that all medical fax transmissions include proper
 * identification and contact information, which is essential for patient safety and
 * regulatory compliance in healthcare environments.
 *
 * @see ca.openosp.openo.commn.model.FaxConfig
 * @see ca.openosp.openo.fax.util.PdfCoverPageCreator
 * @since 2022-04-21
 */
public class FaxAccount {

    /** Name of the healthcare facility for identification purposes */
    private String facilityName;

    /** Custom letterhead name for professional correspondence, defaults to facilityName if null */
    private String letterheadName;

    /** Owner or responsible party for the fax number, defaults to facilityName if null */
    private String faxNumberOwner;

    /** Individual or department name associated with the fax account */
    private String name;

    /** Subtitle or additional text for facility identification, defaults to facilityName if null */
    private String subText;

    /** Fax number for this healthcare facility account */
    private String fax;

    /** Phone number for voice contact with the healthcare facility */
    private String phone;

    /** Complete mailing address of the healthcare facility */
    private String address;

    /**
     * Default constructor for creating an empty fax account.
     */
    public FaxAccount() {
        // default constructor
    }

    /**
     * Constructs a FaxAccount from a FaxConfig database entity.
     *
     * This constructor extracts essential account information from the persistent
     * configuration entity, providing the basic fax number and account ownership
     * information needed for medical document transmission.
     *
     * @param faxConfig FaxConfig database entity containing account configuration
     */
    public FaxAccount(FaxConfig faxConfig) {
        fax = faxConfig.getFaxNumber();
        faxNumberOwner = faxConfig.getAccountName();
    }

    /**
     * Gets the name of the healthcare facility.
     *
     * @return String name of the healthcare facility
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * Sets the name of the healthcare facility.
     *
     * @param facilityName String name of the healthcare facility
     */
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    /**
     * Gets the letterhead name for professional medical correspondence.
     *
     * Returns the custom letterhead name if specified, otherwise defaults to the
     * facility name to ensure all medical documents have proper identification.
     *
     * @return String letterhead name for document headers and cover pages
     */
    public String getLetterheadName() {
        if (letterheadName == null) {
            return facilityName;
        }
        return letterheadName;
    }

    /**
     * Sets the custom letterhead name for professional correspondence.
     *
     * @param letterheadName String custom name for document letterheads
     */
    public void setLetterheadName(String letterheadName) {
        this.letterheadName = letterheadName;
    }

    /**
     * Gets the individual or department name for this fax account.
     *
     * @return String name associated with the fax account
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the individual or department name for this fax account.
     *
     * @param name String name to associate with the fax account
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the owner or responsible party for this fax number.
     *
     * Returns the designated fax number owner if specified, otherwise defaults to the
     * facility name to ensure proper identification on all medical communications.
     *
     * @return String name of the party responsible for this fax account
     */
    public String getFaxNumberOwner() {
        if (faxNumberOwner == null) {
            return facilityName;
        }
        return faxNumberOwner;
    }

    /**
     * Sets the owner or responsible party for this fax number.
     *
     * @param faxNumberOwner String name of the party responsible for this fax account
     */
    public void setFaxNumberOwner(String faxNumberOwner) {
        this.faxNumberOwner = faxNumberOwner;
    }

    /**
     * Gets the fax number for this healthcare facility account.
     *
     * @return String fax number for medical document transmission
     */
    public String getFax() {
        return fax;
    }

    /**
     * Sets the fax number for this healthcare facility account.
     *
     * @param fax String fax number for medical document transmission
     */
    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     * Gets the phone number for voice contact with the healthcare facility.
     *
     * @return String phone number for facility contact
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number for voice contact with the healthcare facility.
     *
     * @param phone String phone number for facility contact
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the complete mailing address of the healthcare facility.
     *
     * @return String complete mailing address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the complete mailing address of the healthcare facility.
     *
     * @param address String complete mailing address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets subtitle or additional identification text for the healthcare facility.
     *
     * Returns custom subtitle text if specified, otherwise defaults to the facility name
     * to provide consistent identification across all medical document transmissions.
     *
     * @return String subtitle text for additional facility identification
     */
    public String getSubText() {
        if (subText == null) {
            return facilityName;
        }
        return subText;
    }

    /**
     * Sets subtitle or additional identification text for the healthcare facility.
     *
     * @param subText String subtitle text for additional facility identification
     */
    public void setSubText(String subText) {
        this.subText = subText;
    }
}
