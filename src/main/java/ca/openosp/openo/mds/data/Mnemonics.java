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
 * Contains mnemonic information for laboratory test parameters including display names, units, and reference ranges.
 * <p>
 * This class encapsulates the metadata associated with laboratory test mnemonics in the MDS (Medical Data Systems)
 * laboratory provider interface. Each mnemonic represents a specific laboratory parameter (e.g., "Hemoglobin",
 * "Glucose", "Sodium") and includes the information necessary for proper display and interpretation of test results.
 * <p>
 * Key components include:
 * <ul>
 * <li>Human-readable report names for laboratory parameters</li>
 * <li>Units of measurement (e.g., "mg/dL", "mmol/L", "g/L")</li>
 * <li>Reference ranges for normal values interpretation</li>
 * <li>Error handling for missing or corrupted mnemonic data</li>
 * </ul>
 * This class is essential for translating cryptic laboratory codes into meaningful clinical information
 * that healthcare providers can interpret and act upon.
 *
 * @since February 4, 2004
 */
public class Mnemonics {

    /**
     * Default constructor initializing with error state values.
     * <p>
     * Creates a Mnemonics instance with default "Error" state, indicating
     * that mnemonic data could not be retrieved or is corrupted. This provides
     * a safe fallback when laboratory mnemonic lookup fails.
     */
    public Mnemonics() {
        reportName = "Error";
        units = "";
        referenceRange = "";
    }

    /**
     * Constructs a Mnemonics instance with complete laboratory parameter information.
     * <p>
     * This constructor initializes all mnemonic data for a specific laboratory parameter,
     * providing the information necessary for proper result display and clinical interpretation.
     *
     * @param rN String the human-readable report name for this laboratory parameter
     * @param u String the units of measurement for this parameter
     * @param rR String the reference range indicating normal values for this parameter
     */
    public Mnemonics(String rN, String u, String rR) {
        reportName = rN;
        units = u;
        referenceRange = rR;
    }

    /**
     * Updates this Mnemonics instance with data from another Mnemonics object.
     * <p>
     * This method provides a safe way to copy mnemonic data from another instance,
     * with error handling for null input. If the source data is null or corrupted,
     * the instance is set to an error state with a descriptive error message.
     *
     * @param newData Mnemonics the source mnemonic data to copy from (can be null)
     */
    public void update(Mnemonics newData) {
        if (newData != null) {
            // Copy all mnemonic data from the source instance
            reportName = newData.reportName;
            units = newData.units;
            referenceRange = newData.referenceRange;
        } else {
            // Set error state when source data is null or invalid
            reportName = "Error in Mnemonics.update";
            units = "";
            referenceRange = "";
        }
    }

    /** Human-readable name for the laboratory parameter (e.g., "Hemoglobin", "White Blood Cell Count") */
    public String reportName;
    /** Units of measurement for this parameter (e.g., "g/L", "10^9/L", "mmol/L") */
    public String units;
    /** Reference range indicating normal values for clinical interpretation */
    public String referenceRange;

}
