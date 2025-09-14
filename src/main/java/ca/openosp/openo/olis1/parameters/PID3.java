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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Patient Identifier parameter (PID-3) for OLIS queries.
 *
 * This parameter represents patient identification information used in HL7 messages.
 * The PID-3 field contains the primary patient identifier along with associated
 * metadata such as the issuing authority and identifier type.
 *
 * The parameter supports multiple identification schemes including:
 * - Health card numbers (OHIP, provincial health numbers)
 * - Medical record numbers (MRN)
 * - Other institutional identifiers
 *
 * Additional demographic information (sex, date of birth) can be included
 * to improve patient matching accuracy in the OLIS system.
 *
 * HL7 field structure:
 * - PID.3.1: ID Number (primary identifier)
 * - PID.3.4.2: Universal ID (assigning authority ID)
 * - PID.3.4.3: Universal ID Type (type of universal ID)
 * - PID.3.5: Identifier Type Code (e.g., MR, OHIP)
 * - PID.3.9.1: Assigning Jurisdiction
 * - PID.3.9.3: Assigning Jurisdiction Coding System
 * - PID.7: Date of Birth (cross-referenced)
 * - PID.8: Sex (cross-referenced)
 *
 * @since 2008-01-01
 */
public class PID3 implements Parameter {

    /**
     * Primary patient identifier number.
     * This is the main ID value (e.g., health card number, MRN).
     */
    private String idNumber;

    /**
     * Universal identifier of the assigning authority.
     * Identifies the organization that issued the identifier.
     */
    private String universalId;

    /**
     * Type of universal identifier.
     * Specifies the format/standard of the universal ID (e.g., ISO, UUID).
     */
    private String universalIdType;

    /**
     * Code indicating the type of identifier.
     * Common values: MR (Medical Record), OHIP (Ontario Health Insurance Plan).
     */
    private String idTypeCode;

    /**
     * Jurisdiction that assigned the identifier.
     * Typically a province or healthcare region code.
     */
    private String assigningJurisdiction;

    /**
     * Coding system for the assigning jurisdiction.
     * Specifies the standard used for jurisdiction codes.
     */
    private String assigningJurisdictionCodingSystem;

    /**
     * Patient's administrative sex.
     * Standard HL7 values: M (Male), F (Female), O (Other), U (Unknown).
     */
    private String sex;

    /**
     * Patient's date of birth.
     * Format: YYYYMMDD as per HL7 specifications.
     */
    private String dateOfBirth;

    /**
     * Date formatter for converting Date objects to HL7 format.
     * Uses YYYYMMDD pattern required by HL7 v2.5.
     */
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");


    /**
     * Constructs a PID3 parameter with all patient identification fields.
     *
     * @param idNumber String the primary patient identifier
     * @param universalId String the assigning authority universal ID
     * @param universalIdType String the type of universal ID
     * @param idTypeCode String the identifier type code (e.g., MR, OHIP)
     * @param assigningJurisdiction String the jurisdiction code
     * @param assigningJurisdictionCodingSystem String the jurisdiction coding system
     * @param sex String the patient's sex (M/F/O/U)
     * @param dateOfBirth String the birth date in YYYYMMDD format
     * @since 2008-01-01
     */
    public PID3(String idNumber, String universalId, String universalIdType, String idTypeCode, String assigningJurisdiction, String assigningJurisdictionCodingSystem, String sex, String dateOfBirth) {
        this.idNumber = idNumber;
        this.universalId = universalId;
        this.universalIdType = universalIdType;
        this.idTypeCode = idTypeCode;
        this.assigningJurisdiction = assigningJurisdiction;
        this.assigningJurisdictionCodingSystem = assigningJurisdictionCodingSystem;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Default constructor for PID3 parameter.
     *
     * Creates an empty PID3 parameter. Fields can be set individually
     * using the setValue methods.
     *
     * @since 2008-01-01
     */
    public PID3() {
        // Empty constructor for field-by-field initialization
    }

    /**
     * Converts the PID3 parameter to its OLIS HL7 string representation.
     *
     * Formats all patient identifier components according to OLIS specifications.
     * Each field is prefixed with its HL7 path and separated by "~" delimiters.
     * Null values are handled by omitting the field value after the "^" delimiter.
     *
     * @return String the formatted HL7 parameter string
     * @since 2008-01-01
     */
    @Override
    public String toOlisString() {
        return getQueryCode() + ".1^" + idNumber + "~" +
                getQueryCode() + ".4.2" + (universalId != null ? "^" + universalId : "") + "~" +
                getQueryCode() + ".4.3" + (universalIdType != null ? "^" + universalIdType : "") + "~" +
                getQueryCode() + ".5" + (idTypeCode != null ? "^" + idTypeCode : "") + "~" +
                getQueryCode() + ".9.1" + (assigningJurisdiction != null ? "^" + assigningJurisdiction : "") + "~" +
                getQueryCode() + ".9.3" + (assigningJurisdictionCodingSystem != null ? "^" + assigningJurisdictionCodingSystem : "") + "~" +
                "@PID.8" + (sex != null ? "^" + sex : "") + "~" +
                "@PID.7" + (dateOfBirth != null ? "^" + dateOfBirth : "");
    }

    /**
     * Sets the primary patient identifier value.
     *
     * @param value Object the identifier value (must be String)
     * @since 2008-01-01
     */
    @Override
    public void setValue(Object value) {
        if (value instanceof String)
            idNumber = (String) value;
    }

    /**
     * Sets a specific component value of the PID3 parameter.
     *
     * Component mappings:
     * - 5: Identifier type code
     * - 7: Date of birth (accepts Date object, formats to YYYYMMDD)
     * - 8: Sex
     *
     * @param part Integer the component number
     * @param value Object the value to set
     * @since 2008-01-01
     */
    @Override
    public void setValue(Integer part, Object value) {
        if (part == 5)
            idTypeCode = (String) value;
        else if (part == 8)
            sex = (String) value;
        else if (part == 7)
            // Convert Date to HL7 format
            dateOfBirth = dateFormatter.format((Date) value);
    }

    /**
     * Sets a specific subcomponent value of the PID3 parameter.
     *
     * Subcomponent mappings:
     * - 4.2: Universal ID
     * - 4.3: Universal ID type
     * - 9.1: Assigning jurisdiction
     * - 9.3: Assigning jurisdiction coding system
     *
     * @param part Integer the component number
     * @param part2 Integer the subcomponent number
     * @param value Object the value to set (must be String)
     * @since 2008-01-01
     */
    @Override
    public void setValue(Integer part, Integer part2, Object value) {
        if (part == 9 && part2 == 1) {
            assigningJurisdiction = (String) value;
        } else if (part == 9 && part2 == 3) {
            assigningJurisdictionCodingSystem = (String) value;
        } else if (part == 4 && part2 == 2) {
            universalId = (String) value;
        } else if (part == 4 && part2 == 3) {
            universalIdType = (String) value;
        }
    }

    /**
     * Returns the HL7 field identifier for patient identifier.
     *
     * @return String "@PID.3" indicating this is the PID-3 field
     * @since 2008-01-01
     */
    @Override
    public String getQueryCode() {
        return "@PID.3";
    }

}
