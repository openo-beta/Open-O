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
 * Patient immunization record entity.
 *
 * This entity represents immunization records for patients, storing vaccination history and
 * immunization status information. It provides essential tracking for preventive care and
 * public health monitoring in accordance with immunization schedules and guidelines.
 *
 * Key clinical information includes:
 * - Patient demographic linkage for vaccination records
 * - Healthcare provider who administered or recorded the immunization
 * - Immunization details and vaccine information
 * - Vaccination dates and schedule compliance
 * - Record archival status for data management
 *
 * This entity supports:
 * - Vaccine administration tracking
 * - Immunization schedule management
 * - Public health reporting requirements
 * - Clinical decision support for preventive care
 * - Integration with provincial immunization registries
 *
 * @see ca.openosp.openo.entities.Demographic for patient demographic information
 * @since November 1, 2004
 */
public class Immunizations {
    /**
     * auto_increment
     */
    private int id;
    private int demographicNo;
    private String providerNo;
    private String _immunizations;
    private String saveDate;
    private short archived;

    /**
     * Default constructor for patient immunization record entity.
     * Initializes all fields to their default values.
     */
    public Immunizations() {
    }

    /**
     * Complete constructor for patient immunization record entity.
     * Creates a fully initialized immunization record with all required fields.
     *
     * @param id             int unique identifier for this immunization record
     * @param demographicNo  int patient demographic number linking to patient record
     * @param providerNo     String healthcare provider number who administered/recorded
     * @param _immunizations String immunization details and vaccine information
     * @param saveDate       String date when this immunization record was saved
     * @param archived       short archival status (0=active, 1=archived)
     */
    public Immunizations(int id, int demographicNo, String providerNo,
                         String _immunizations, String saveDate,
                         short archived) {
        this.id = id;
        this.demographicNo = demographicNo;
        this.providerNo = providerNo;
        this._immunizations = _immunizations;
        this.saveDate = saveDate;
        this.archived = archived;
    }

    /**
     * Gets the unique immunization record identifier.
     * This is the database primary key for this immunization record.
     *
     * @return int the unique immunization record identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the patient demographic number.
     * Links this immunization record to the specific patient demographic record.
     *
     * @return int the patient demographic number
     */
    public int getDemographicNo() {
        return demographicNo;
    }

    /**
     * Gets the healthcare provider number.
     * Identifies the provider who administered the vaccine or recorded the immunization.
     *
     * @return String the healthcare provider number, empty string if null
     */
    public String getProviderNo() {
        return (providerNo != null ? providerNo : "");
    }

    /**
     * Gets the immunization details and vaccine information.
     * Contains specific information about the vaccines administered, including
     * vaccine type, dosage, batch numbers, and administration details.
     *
     * @return String the immunization details, empty string if null
     */
    public String get_immunizations() {
        return (_immunizations != null ? _immunizations : "");
    }

    /**
     * Gets the save date for this immunization record.
     * The date when this immunization record was created or last updated in the system.
     *
     * @return String the save date, may be null
     */
    public String getSaveDate() {
        return saveDate;
    }

    /**
     * Gets the archival status of this immunization record.
     * Indicates whether this record is active or has been archived.
     *
     * @return short the archival status (0=active, 1=archived)
     */
    public short getArchived() {
        return archived;
    }

    /**
     * Sets the unique immunization record identifier.
     * This is the database primary key for this immunization record.
     *
     * @param id int the unique immunization record identifier
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the demographicNo
     *
     * @param demographicNo int
     */
    public void setDemographicNo(int demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Sets the providerNo
     *
     * @param providerNo String
     */
    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * Sets the _immunizations
     *
     * @param _immunizations String
     */
    public void set_immunizations(String _immunizations) {
        this._immunizations = _immunizations;
    }

    /**
     * Sets the saveDate
     *
     * @param saveDate String
     */
    public void setSaveDate(String saveDate) {
        this.saveDate = saveDate;
    }

    /**
     * Sets the archived
     *
     * @param archived short
     */
    public void setArchived(short archived) {
        this.archived = archived;
    }
}
