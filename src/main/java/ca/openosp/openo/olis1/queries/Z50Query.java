//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.olis1.queries;

import ca.openosp.openo.olis1.parameters.PID51;
import ca.openosp.openo.olis1.parameters.PID52;
import ca.openosp.openo.olis1.parameters.PID7;
import ca.openosp.openo.olis1.parameters.PID8;
import ca.openosp.openo.olis1.parameters.ZPD1;

/**
 * Z50 Query - Identify Patient by Name, Sex, and Date of Birth.
 *
 * This query type performs patient identification using demographic information
 * including first name, last name, sex, and date of birth. It is designed to
 * locate patients in the laboratory system when specific patient identifiers
 * (such as health card numbers) are not available or known.
 *
 * The Z50 query is commonly used for patient matching and identification workflows
 * when only demographic information is available. It helps healthcare providers
 * locate existing patient records and associated laboratory results by searching
 * on the core demographic identifiers.
 *
 * Supported parameters:
 * - First name (mandatory) - Patient's given name
 * - Last name (mandatory) - Patient's family name
 * - Sex (mandatory) - Patient's gender
 * - Date of birth (mandatory) - Patient's birth date
 *
 * Mandatory parameters:
 * - First name (PID52) - Patient's first/given name
 * - Last name (PID51) - Patient's last/family name
 * - Sex (PID8) - Patient's gender identifier
 * - Date of birth (PID7) - Patient's birth date
 *
 * All parameters are mandatory for this query type to ensure accurate patient
 * identification and matching. The query results include patient records that
 * match the provided demographic criteria.
 *
 * @since 2011-06-24
 */
public class Z50Query extends Query {

    /**
     * Patient's first/given name (mandatory).
     * Used for patient identification and matching.
     */
    private PID52 firstName = new PID52();

    /**
     * Patient's last/family name (mandatory).
     * Used for patient identification and matching.
     */
    private PID51 lastName = new PID51();

    /**
     * Patient's gender/sex identifier (mandatory).
     * Used for patient identification and matching.
     */
    private PID8 sex = new PID8();

    /**
     * Patient's date of birth (mandatory).
     * Used for patient identification and matching.
     */
    private PID7 dateOfBirth = new PID7();

    /**
     * Generates the HL7 query string for the Z50 query.
     *
     * Constructs the query parameter string by concatenating all non-null
     * parameters in the proper sequence, separated by "~" delimiters.
     * The order follows OLIS specifications for Z50 queries.
     * Removes trailing delimiter if present.
     *
     * @return String the formatted HL7 query parameter string
     * @since 2011-06-24
     */
    @Override
    public String getQueryHL7String() {
        String query = "";

        // Add mandatory first name
        if (firstName != null)
            query += firstName.toOlisString() + "~";

        // Add mandatory last name
        if (lastName != null)
            query += lastName.toOlisString() + "~";

        // Add mandatory sex
        if (sex != null)
            query += sex.toOlisString() + "~";

        // Add mandatory date of birth
        if (dateOfBirth != null)
            query += dateOfBirth.toOlisString() + "~";

        // Remove trailing delimiter
        if (query.endsWith("~")) {
            query = query.substring(0, query.length() - 1);
        }

        return query;
    }

    /**
     * Sets the patient's first/given name.
     *
     * This is a mandatory parameter required for patient identification.
     *
     * @param firstName PID52 the patient's first name parameter
     * @since 2011-06-24
     */
    public void setFirstName(PID52 firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the patient's last/family name.
     *
     * This is a mandatory parameter required for patient identification.
     *
     * @param lastName PID51 the patient's last name parameter
     * @since 2011-06-24
     */
    public void setLastName(PID51 lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the patient's gender/sex identifier.
     *
     * This is a mandatory parameter required for patient identification.
     *
     * @param sex PID8 the patient's sex parameter
     * @since 2011-06-24
     */
    public void setSex(PID8 sex) {
        this.sex = sex;
    }

    /**
     * Sets the patient's date of birth.
     *
     * This is a mandatory parameter required for patient identification.
     *
     * @param dateOfBirth PID7 the patient's birth date parameter
     * @since 2011-06-24
     */
    public void setDateOfBirth(PID7 dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Returns the query type identifier for this query.
     *
     * @return QueryType.Z50 indicating this is a patient demographic identification query
     * @since 2011-06-24
     */
    @Override
    public QueryType getQueryType() {
        return QueryType.Z50;
    }

    /**
     * Sets the consent parameter for viewing blocked information.
     *
     * This method is not supported for Z50 queries as they are used for
     * patient identification and do not support patient consent parameters.
     * Patient identification queries have different privacy handling requirements.
     *
     * @param consentToViewBlockedInformation ZPD1 the consent parameter
     * @throws RuntimeException always, as this operation is not valid for Z50 queries
     * @since 2011-06-24
     */
    @Override
    public void setConsentToViewBlockedInformation(ZPD1 consentToViewBlockedInformation) {
        throw new RuntimeException("Not valid for this type of query.");
    }

}
