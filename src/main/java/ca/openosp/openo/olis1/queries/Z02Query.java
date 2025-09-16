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

import ca.openosp.openo.olis1.parameters.ORC4;
import ca.openosp.openo.olis1.parameters.PID3;
import ca.openosp.openo.olis1.parameters.ZBX1;
import ca.openosp.openo.olis1.parameters.ZPD1;
import ca.openosp.openo.olis1.parameters.ZPD3;
import ca.openosp.openo.olis1.parameters.ZRP1;

/**
 * Z02 Query - Retrieve Laboratory Information by Order ID.
 *
 * This query type retrieves laboratory results using a specific order identifier
 * (requisition number or accession number). It provides the most direct and efficient
 * method to retrieve results when the order ID is known.
 *
 * The Z02 query is typically used when:
 * - Following up on a specific laboratory order
 * - Retrieving results for a known requisition
 * - Accessing results by accession number
 *
 * Mandatory parameters:
 * - Patient identifier (PID3) - For patient verification
 * - Requesting HIC (ZRP1) - Health Information Custodian identifier
 * - Placer group number (ORC4) - The order/requisition identifier
 *
 * The query returns all test results associated with the specified order ID,
 * subject to privacy and consent restrictions.
 *
 * @since 2008-01-01
 */
public class Z02Query extends Query {

    /**
     * Flag to retrieve all test results for the order.
     * When set, returns complete result set including preliminary results.
     */
    private ZBX1 retrieveAllTestResults = null;

    /**
     * Requesting Health Information Custodian identifier (mandatory).
     * Identifies the healthcare facility or organization making the request.
     */
    private ZRP1 requestingHic = new ZRP1();

    /**
     * Consent parameter for viewing blocked information.
     * Required when accessing privacy-protected results.
     */
    private ZPD1 consentToViewBlockedInformation = null;

    /**
     * Patient consent block all indicator.
     * Indicates if patient has blocked all information sharing.
     */
    private ZPD3 patientConsentBlockAllIndicator = null;

    /**
     * Patient identifier (mandatory).
     * Used to verify patient identity for the order.
     */
    private PID3 patientIdentifier = new PID3();

    /**
     * Placer group number (mandatory).
     * The order ID, requisition number, or accession number to query.
     */
    private ORC4 placerGroupNumber = new ORC4();


    /**
     * Generates the HL7 query string for the Z02 query.
     *
     * Constructs the query parameter string by concatenating all non-null
     * parameters in the proper sequence, separated by "~" delimiters.
     * The order of parameters is significant and must follow OLIS specifications.
     *
     * @return String the formatted HL7 query parameter string
     * @since 2008-01-01
     */
    @Override
    public String getQueryHL7String() {
        String query = "";

        // Add retrieve all flag if specified
        if (retrieveAllTestResults != null)
            query += retrieveAllTestResults.toOlisString() + "~";

        // Add mandatory requesting HIC
        if (requestingHic != null)
            query += requestingHic.toOlisString() + "~";

        // Add consent parameters
        if (consentToViewBlockedInformation != null)
            query += consentToViewBlockedInformation.toOlisString() + "~";

        if (patientConsentBlockAllIndicator != null)
            query += patientConsentBlockAllIndicator.toOlisString() + "~";

        // Add mandatory patient identifier
        if (patientIdentifier != null)
            query += patientIdentifier.toOlisString() + "~";

        // Add mandatory order identifier
        if (placerGroupNumber != null)
            query += placerGroupNumber.toOlisString() + "~";

        // Remove trailing delimiter
        if (query.endsWith("~")) {
            query = query.substring(0, query.length() - 1);
        }

        return query;
    }

    /**
     * Returns the query type identifier for this query.
     *
     * @return QueryType.Z02 indicating this is an order ID query
     * @since 2008-01-01
     */
    @Override
    public QueryType getQueryType() {
        return QueryType.Z02;
    }

    /**
     * Sets the retrieve all test results flag.
     *
     * @param retrieveAllTestResults ZBX1 the retrieve all flag parameter
     * @since 2008-01-01
     */
    public void setRetrieveAllTestResults(ZBX1 retrieveAllTestResults) {
        this.retrieveAllTestResults = retrieveAllTestResults;
    }

    /**
     * Sets the requesting Health Information Custodian identifier.
     *
     * This is a mandatory parameter that identifies the healthcare facility
     * or organization making the request.
     *
     * @param requestingHic ZRP1 the HIC identifier parameter
     * @since 2008-01-01
     */
    public void setRequestingHic(ZRP1 requestingHic) {
        this.requestingHic = requestingHic;
    }

    /**
     * Sets the consent parameter for viewing blocked information.
     *
     * @param consentToViewBlockedInformation ZPD1 the consent parameter
     * @since 2008-01-01
     */
    public void setConsentToViewBlockedInformation(ZPD1 consentToViewBlockedInformation) {
        this.consentToViewBlockedInformation = consentToViewBlockedInformation;
    }

    /**
     * Sets the patient consent block all indicator.
     *
     * @param patientConsentBlockAllIndicator ZPD3 the block all indicator
     * @since 2008-01-01
     */
    public void setPatientConsentBlockAllIndicator(ZPD3 patientConsentBlockAllIndicator) {
        this.patientConsentBlockAllIndicator = patientConsentBlockAllIndicator;
    }

    /**
     * Sets the patient identifier.
     *
     * This is a mandatory parameter used to verify the patient identity
     * associated with the order.
     *
     * @param patientIdentifier PID3 the patient identifier parameter
     * @since 2008-01-01
     */
    public void setPatientIdentifier(PID3 patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    /**
     * Sets the placer group number (order identifier).
     *
     * This is a mandatory parameter containing the requisition number,
     * accession number, or other order identifier to query.
     *
     * @param placerGroupNumber ORC4 the order identifier parameter
     * @since 2008-01-01
     */
    public void setPlacerGroupNumber(ORC4 placerGroupNumber) {
        this.placerGroupNumber = placerGroupNumber;
    }


}
