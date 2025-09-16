//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.olis1.parameters;

/**
 * Test Request Code parameter (OBR-4) for OLIS queries.
 *
 * This parameter represents the universal service identifier for laboratory tests
 * in HL7 messages. OBR-4 contains the test code that identifies what laboratory
 * test was ordered or performed.
 *
 * The parameter supports various coding systems including:
 * - LOINC (Logical Observation Identifiers Names and Codes)
 * - Local laboratory test codes
 * - Provincial standard test codes
 *
 * HL7 field structure:
 * - OBR.4.1: Identifier (test code)
 * - OBR.4.3: Name of Coding System (e.g., "LN" for LOINC)
 *
 * This parameter is used to filter query results to specific test types,
 * allowing retrieval of only relevant laboratory results.
 *
 * @since 2008-01-01
 */
public class OBR4 implements Parameter {

    /**
     * Test identifier code.
     * The specific code identifying the laboratory test (e.g., LOINC code).
     */
    private String identifier;

    /**
     * Name of the coding system.
     * Identifies the coding system used (e.g., "LN" for LOINC, "L" for local).
     */
    private String nameOfCodingSystem;

    /**
     * Constructs an OBR4 parameter with test code and coding system.
     *
     * @param identifier String the test code (e.g., LOINC code)
     * @param nameOfCodingSystem String the coding system identifier (e.g., "LN")
     * @since 2008-01-01
     */
    public OBR4(String identifier, String nameOfCodingSystem) {
        this.identifier = identifier;
        this.nameOfCodingSystem = nameOfCodingSystem;
    }

    /**
     * Converts the OBR4 parameter to its OLIS HL7 string representation.
     *
     * Formats the test code and coding system according to OLIS specifications.
     * Empty values are handled by providing empty strings after the "^" delimiter.
     *
     * @return String the formatted HL7 parameter string
     * @since 2008-01-01
     */
    @Override
    public String toOlisString() {
        return getQueryCode() + ".1^" + (identifier != null ? identifier : "") + "~" +
                getQueryCode() + ".3^" + (nameOfCodingSystem != null ? nameOfCodingSystem : "");
    }

    /**
     * Sets the primary value (not supported for OBR4).
     *
     * OBR4 requires component-level setting using setValue(Integer, Object).
     *
     * @param value Object the value to set (ignored)
     * @throws UnsupportedOperationException always, as direct value setting is not supported
     * @since 2008-01-01
     */
    @Override
    public void setValue(Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a specific component value of the OBR4 parameter.
     *
     * Component mappings:
     * - 1: Test identifier code
     * - 3: Name of coding system
     *
     * @param part Integer the component number (1 or 3)
     * @param value Object the value to set (must be String)
     * @since 2008-01-01
     */
    @Override
    public void setValue(Integer part, Object value) {
        if (part == 1)
            this.identifier = (String) value;
        else if (part == 3)
            this.nameOfCodingSystem = (String) value;
    }

    /**
     * Sets a subcomponent value (not supported for OBR4).
     *
     * OBR4 does not have subcomponents.
     *
     * @param part Integer the component number (ignored)
     * @param part2 Integer the subcomponent number (ignored)
     * @param value Object the value to set (ignored)
     * @throws UnsupportedOperationException always, as subcomponents are not supported
     * @since 2008-01-01
     */
    @Override
    public void setValue(Integer part, Integer part2, Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the HL7 field identifier for test request code.
     *
     * @return String "@OBR.4" indicating this is the OBR-4 field
     * @since 2008-01-01
     */
    @Override
    public String getQueryCode() {
        return "@OBR.4";
    }

}
