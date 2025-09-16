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
 * Healthcare clinical factor entity representing clinical data elements that influence patient care.
 * This entity encapsulates clinical factors that may affect diagnosis, treatment decisions,
 * or billing considerations in healthcare management.
 *
 * Clinical factors can include patient conditions, risk factors, comorbidities, or other
 * clinical considerations that healthcare providers need to track and document.
 * Each factor has a defined time period during which it is considered active.
 *
 * @see Condition
 * @since November 1, 2004
 */
public class ClinicalFactor {

    /**
     * Unique identifier for the type of clinical factor
     */
    private int factorTypeId;

    /**
     * Name of the clinical factor
     */
    private String factorName;

    /**
     * Detailed description of the clinical factor
     */
    private String factorDesc;

    /**
     * Date when this clinical factor becomes effective or was first noted
     */
    private String startDate;

    /**
     * Date when this clinical factor is no longer active or relevant
     */
    private String endDate;

    /**
     * Additional clinical notes or observations related to this factor
     */
    private String clinicalNote;

    /**
     * Default constructor creating an empty ClinicalFactor instance.
     * All fields will be initialized to their default values.
     */
    public ClinicalFactor() {
    }

    /**
     * Gets the unique identifier for the type of clinical factor.
     *
     * @return int the factor type ID
     */
    public int getFactorTypeId() {
        return factorTypeId;
    }

    /**
     * Sets the unique identifier for the type of clinical factor.
     *
     * @param factorTypeId int the factor type ID
     */
    public void setFactorTypeId(int factorTypeId) {
        this.factorTypeId = factorTypeId;
    }

    /**
     * Gets the name of the clinical factor.
     *
     * @return String the factor name
     */
    public String getFactorName() {
        return factorName;
    }

    /**
     * Sets the name of the clinical factor.
     *
     * @param factorName String the factor name
     */
    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }

    /**
     * Gets the detailed description of the clinical factor.
     *
     * @return String the factor description
     */
    public String getFactorDesc() {
        return factorDesc;
    }

    /**
     * Sets the detailed description of the clinical factor.
     *
     * @param factorDesc String the factor description
     */
    public void setFactorDesc(String factorDesc) {
        this.factorDesc = factorDesc;
    }

    /**
     * Gets the date when this clinical factor becomes effective.
     *
     * @return String the start date
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the date when this clinical factor becomes effective.
     *
     * @param startDate String the start date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the date when this clinical factor is no longer active.
     *
     * @return String the end date
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Sets the date when this clinical factor is no longer active.
     *
     * @param endDate String the end date
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets additional clinical notes related to this factor.
     *
     * @return String the clinical notes
     */
    public String getClinicalNote() {
        return clinicalNote;
    }

    /**
     * Sets additional clinical notes related to this factor.
     *
     * @param clinicalNote String the clinical notes
     */
    public void setClinicalNote(String clinicalNote) {
        this.clinicalNote = clinicalNote;
    }
}