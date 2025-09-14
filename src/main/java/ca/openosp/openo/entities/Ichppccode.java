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
 * International Classification of Health Problems in Primary Care (ICHPPC) diagnostic codes entity.
 *
 * This entity represents ICHPPC diagnostic codes, which are used for standardized classification
 * of health problems encountered in primary care settings. ICHPPC codes provide a systematic
 * way to categorize and code diagnoses in family practice and primary healthcare environments.
 *
 * The ICHPPC classification system was developed specifically for primary care use and includes:
 * - Diagnostic codes for common primary care conditions
 * - Standardized descriptions of health problems
 * - Cross-references to other classification systems
 *
 * These codes are essential for:
 * - Clinical documentation and record keeping
 * - Billing and administrative purposes
 * - Statistical analysis and quality improvement
 * - Integration with other healthcare coding systems
 *
 * @see <a href="https://www.who.int/classifications/icd/en/">WHO International Classifications</a>
 * @since November 1, 2004
 */
public class Ichppccode {
    private String _ichppccode;
    private String diagnosticCode;
    private String description;

    /**
     * Default constructor for ICHPPC diagnostic code entity.
     * Initializes all fields to their default values.
     */
    public Ichppccode() {
    }

    /**
     * Complete constructor for ICHPPC diagnostic code entity.
     * Creates a fully initialized ICHPPC code with all required fields.
     *
     * @param _ichppccode    String the ICHPPC code identifier
     * @param diagnosticCode String the diagnostic code value
     * @param description    String the human-readable description of the diagnosis
     */
    public Ichppccode(String _ichppccode, String diagnosticCode,
                      String description) {
        this._ichppccode = _ichppccode;
        this.diagnosticCode = diagnosticCode;
        this.description = description;
    }

    /**
     * Gets the ICHPPC code identifier.
     * This is the unique identifier for this ICHPPC diagnostic code.
     *
     * @return String the ICHPPC code identifier, empty string if null
     */
    public String get_ichppccode() {
        return (_ichppccode != null ? _ichppccode : "");
    }

    /**
     * Gets the diagnostic code value.
     * This is the actual diagnostic code used for clinical documentation and billing.
     *
     * @return String the diagnostic code, empty string if null
     */
    public String getDiagnosticCode() {
        return (diagnosticCode != null ? diagnosticCode : "");
    }

    /**
     * Gets the human-readable description of the diagnosis.
     * Provides a clear, clinical description of the health problem or condition.
     *
     * @return String the diagnostic description, empty string if null
     */
    public String getDescription() {
        return (description != null ? description : "");
    }

    /**
     * Sets the ICHPPC code identifier.
     * This is the unique identifier for this ICHPPC diagnostic code.
     *
     * @param _ichppccode String the ICHPPC code identifier
     */
    public void set_ichppccode(String _ichppccode) {
        this._ichppccode = _ichppccode;
    }

    /**
     * Sets the diagnostic code value.
     * This is the actual diagnostic code used for clinical documentation and billing.
     *
     * @param diagnosticCode String the diagnostic code
     */
    public void setDiagnosticCode(String diagnosticCode) {
        this.diagnosticCode = diagnosticCode;
    }

    /**
     * Sets the human-readable description of the diagnosis.
     * Provides a clear, clinical description of the health problem or condition.
     *
     * @param description String the diagnostic description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
