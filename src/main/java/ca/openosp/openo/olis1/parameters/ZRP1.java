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
 * Requesting Health Information Custodian (HIC) parameter (ZRP-1) for OLIS queries.
 *
 * This parameter represents the healthcare facility or organization requesting
 * laboratory information from OLIS. The ZRP segment is an OLIS-specific extension
 * to standard HL7 for identifying the requesting entity.
 *
 * The HIC parameter is mandatory for all OLIS queries and includes:
 * - Facility/organization identifier
 * - Identifier type and jurisdiction
 * - Optional provider name information
 *
 * HL7 field structure:
 * - ZRP.1.1: ID Number (HIC identifier)
 * - ZRP.1.13: ID Type Code
 * - ZRP.1.22.1: Assigning Jurisdiction
 * - ZRP.1.22.3: Assigning Jurisdiction Coding System
 * - ZRP.1.2: Last Name
 * - ZRP.1.3: First Name
 * - ZRP.1.4: Second/Middle Name
 *
 * The HIC identifier is used for:
 * - Authorization and access control
 * - Audit logging
 * - Result routing and delivery
 *
 * @since 2008-01-01
 */
public class ZRP1 implements Parameter {

    /**
     * Health Information Custodian identifier number.
     * The unique identifier for the requesting facility or organization.
     */
    private String idNumber;

    /**
     * Identifier type code.
     * Specifies the type of HIC identifier (e.g., facility code).
     */
    private String idTypeCode;

    /**
     * Assigning jurisdiction.
     * The authority that assigned the HIC identifier (e.g., "ON" for Ontario).
     */
    private String assigningJurisdiction;

    /**
     * Assigning jurisdiction coding system.
     * The coding system used for jurisdiction codes.
     */
    private String assigningJurisdictionCodingSystem;

    /**
     * Last name of the requesting provider.
     * Optional provider surname.
     */
    private String name;

    /**
     * First name of the requesting provider.
     * Optional provider given name.
     */
    private String firstName;

    /**
     * Second/middle name of the requesting provider.
     * Optional provider middle name.
     */
    private String secondName;

    /**
     * Constructs a ZRP1 parameter with complete HIC information.
     *
     * @param idNumber String the HIC identifier
     * @param idTypeCode String the identifier type code
     * @param assigningJurisdiction String the jurisdiction code
     * @param assigningJurisdictionCodingSystem String the jurisdiction coding system
     * @param name String the provider's last name
     * @param firstName String the provider's first name
     * @param secondName String the provider's middle name
     * @since 2008-01-01
     */
    public ZRP1(String idNumber, String idTypeCode, String assigningJurisdiction, String assigningJurisdictionCodingSystem, String name, String firstName, String secondName) {
        this.idNumber = idNumber;
        this.idTypeCode = idTypeCode;
        this.assigningJurisdiction = assigningJurisdiction;
        this.assigningJurisdictionCodingSystem = assigningJurisdictionCodingSystem;
        this.name = name;
        this.firstName = firstName;
        this.secondName = secondName;
    }

    /**
     * Default constructor for ZRP1 parameter.
     *
     * Creates an empty ZRP1 parameter. Fields can be set individually
     * using the setValue methods.
     *
     * @since 2008-01-01
     */
    public ZRP1() {
        // Empty constructor for field-by-field initialization
    }

    /**
     * Converts the ZRP1 parameter to its OLIS HL7 string representation.
     *
     * Formats all HIC components according to OLIS specifications.
     * Each field is prefixed with its HL7 path and separated by "~" delimiters.
     *
     * @return String the formatted HL7 parameter string
     * @since 2008-01-01
     */
    @Override
    public String toOlisString() {
        return getQueryCode() + ".1^" + idNumber + "~" + getQueryCode() + ".13^" + idTypeCode + "~" +
                getQueryCode() + ".22.1^" + (assigningJurisdiction != null ? assigningJurisdiction : "") + "~" +
                getQueryCode() + ".22.3^" + (assigningJurisdictionCodingSystem != null ? assigningJurisdictionCodingSystem : "") + "~" +
                getQueryCode() + ".2^" + (name != null ? name : "") + "~" +
                getQueryCode() + ".3" + (firstName != null ? "^" + firstName : "") + "~" +
                getQueryCode() + ".4" + (secondName != null ? "^" + secondName : "");
    }

    /**
     * Sets the primary HIC identifier value.
     *
     * @param value Object the HIC identifier (must be String)
     * @since 2008-01-01
     */
    @Override
    public void setValue(Object value) {
        if (value instanceof String)
            idNumber = (String) value;
    }

    /**
     * Sets a specific component value of the ZRP1 parameter.
     *
     * Component mappings:
     * - 2: Last name
     * - 3: First name
     * - 4: Second/middle name
     * - 13: ID type code
     *
     * @param part Integer the component number
     * @param value Object the value to set (must be String)
     * @since 2008-01-01
     */
    @Override
    public void setValue(Integer part, Object value) {
        if (part == 13) {
            idTypeCode = (String) value;
        } else if (part == 2) {
            name = (String) value;
        } else if (part == 3) {
            firstName = (String) value;
        } else if (part == 4) {
            secondName = (String) value;
        }
    }

    /**
     * Sets a specific subcomponent value of the ZRP1 parameter.
     *
     * Subcomponent mappings:
     * - 22.1: Assigning jurisdiction
     * - 22.3: Assigning jurisdiction coding system
     *
     * @param part Integer the component number
     * @param part2 Integer the subcomponent number
     * @param value Object the value to set (must be String)
     * @since 2008-01-01
     */
    @Override
    public void setValue(Integer part, Integer part2, Object value) {
        if (part == 22 && part2 == 1) {
            assigningJurisdiction = (String) value;
        } else if (part == 22 && part2 == 3) {
            assigningJurisdictionCodingSystem = (String) value;
        }
    }

    /**
     * Returns the HL7 field identifier for requesting HIC.
     *
     * @return String "@ZRP.1" indicating this is the ZRP-1 field
     * @since 2008-01-01
     */
    @Override
    public String getQueryCode() {
        return "@ZRP.1";
    }

    /**
     * Gets the HIC identifier number.
     *
     * Retrieves the primary identifier for the requesting Health Information Custodian.
     *
     * @return String the HIC identifier
     * @since 2008-01-01
     */
    public String getIdNumber() {
        return this.idNumber;
    }
}
