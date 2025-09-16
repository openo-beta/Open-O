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
 * Laboratory test entity extending the base Test class.
 *
 * This entity represents a specific laboratory test with enhanced functionality
 * for lab-specific attributes. It extends the base Test class to provide
 * specialized handling for laboratory observations and results.
 *
 * Key laboratory-specific features include:
 * - LOINC code integration for standardized test identification
 * - Observation date/time tracking for test timeline management
 * - Lab type classification for organizational purposes
 * - Default lab designation for system preferences
 *
 * This entity supports:
 * - Laboratory test catalog management
 * - LOINC-based test standardization
 * - Integration with laboratory information systems
 * - Clinical decision support through standardized test definitions
 *
 * @see Test for base test functionality
 * @see LoincCodes for LOINC code definitions
 * @since October 11, 2004
 */
public class LabTest
        extends Test {
    private String observationDateTime = "";
    private String loincCode = "";
    private String labType = "";
    private boolean defaultLab = true;

    /**
     * Gets the observation date/time for this laboratory test.
     * Represents when the test observation or measurement was performed.
     *
     * @return String the observation date/time, empty string if not set
     */
    public String getObservationDateTime() {
        return observationDateTime;
    }

    /**
     * Sets the observation date/time for this laboratory test.
     * Records when the test observation or measurement was performed.
     *
     * @param observationDateTime String the observation date/time
     */
    public void setObservationDateTime(String observationDateTime) {
        this.observationDateTime = observationDateTime;
    }

    /**
     * Gets the LOINC code for this laboratory test.
     * LOINC (Logical Observation Identifiers Names and Codes) provides
     * universal identifiers for laboratory and clinical observations.
     *
     * @return String the LOINC code, empty string if not set
     * @see LoincCodes for complete LOINC code definitions
     */
    public String getLoincCode() {
        return loincCode;
    }

    /**
     * Sets the LOINC code for this laboratory test.
     * LOINC codes provide standardized identification for lab tests.
     *
     * @param loincCode String the LOINC code
     * @see LoincCodes for available LOINC codes
     */
    public void setLoincCode(String loincCode) {
        this.loincCode = loincCode;
    }

    /**
     * Gets the laboratory type classification.
     * Categorizes the test by laboratory department or specialty
     * (e.g., "Hematology", "Chemistry", "Microbiology").
     *
     * @return String the lab type, empty string if not set
     */
    public String getLabType() {
        return labType;
    }

    /**
     * Sets the laboratory type classification.
     * Categorizes the test by laboratory department or specialty.
     *
     * @param labType String the lab type classification
     */
    public void setLabType(String labType) {
        this.labType = labType;
    }

    /**
     * Checks if this is a default laboratory test.
     * Default lab tests are typically used as system preferences or
     * commonly ordered tests for specific clinical scenarios.
     *
     * @return boolean true if this is a default lab test, false otherwise
     */
    public boolean isDefaultLab() {
        return defaultLab;
    }

    /**
     * Sets whether this is a default laboratory test.
     * Default lab tests are used for system preferences and common ordering.
     *
     * @param defaultLab boolean true to mark as default lab test
     */
    public void setDefaultLab(boolean defaultLab) {
        this.defaultLab = defaultLab;
    }

    // Note: This class extends Test and inherits additional functionality
    // Additional lab-specific fields and methods are defined above
    // Base test functionality is inherited from the Test superclass
}
