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

import java.util.LinkedList;
import java.util.List;

import ca.openosp.openo.olis1.parameters.OBR22;
import ca.openosp.openo.olis1.parameters.OBR4;
import ca.openosp.openo.olis1.parameters.OBX3;
import ca.openosp.openo.olis1.parameters.QRD7;
import ca.openosp.openo.olis1.parameters.ZPD1;
import ca.openosp.openo.olis1.parameters.ZRP1;

/**
 * Z04 Query - Retrieve Laboratory Information Updates for Practitioner.
 *
 * This query type retrieves new or updated laboratory results for a specific
 * healthcare provider or facility. It functions as a provider "inbox" to check
 * for recent unacknowledged laboratory results.
 *
 * The Z04 query is typically used for:
 * - Provider workflow to review new results
 * - Periodic polling for updates
 * - Retrieving results for all patients of a provider
 * - Checking for results within a specific time window
 *
 * Mandatory parameters:
 * - Start/End timestamp (OBR22) - Time range for updates
 * - Requesting HIC (ZRP1) - Health Information Custodian identifier
 *
 * Note: This query type does not support consent parameters as it retrieves
 * results at the provider level rather than for specific patients.
 *
 * @since 2008-01-01
 */
public class Z04Query extends Query {

    /**
     * Start and end timestamp for the query date range (mandatory).
     * Defines the time window for which updates should be retrieved.
     */
    private OBR22 startEndTimestamp = new OBR22();

    /**
     * Quantity limited request parameter.
     * Specifies the maximum number of results to return.
     */
    private QRD7 quantityLimitedRequest = null;

    /**
     * Requesting Health Information Custodian identifier (mandatory).
     * Identifies the healthcare facility or provider requesting updates.
     */
    private ZRP1 requestingHic = new ZRP1();

    /**
     * List of test request codes to filter by.
     * Limits results to specific test types.
     */
    private List<OBR4> testRequestCodeList = new LinkedList<OBR4>();

    /**
     * List of test result codes to filter by.
     * Limits results to specific result types.
     */
    private List<OBX3> testResultCodeList = new LinkedList<OBX3>();

    /**
     * Generates the HL7 query string for the Z04 query.
     *
     * Constructs the query parameter string with mandatory timestamp
     * and HIC parameters, plus optional filters.
     *
     * @return String the formatted HL7 query parameter string
     * @since 2008-01-01
     */
    @Override
    public String getQueryHL7String() {
        String query = "";

        // Add mandatory timestamp range
        if (startEndTimestamp != null)
            query += startEndTimestamp.toOlisString() + "~";

        // Add result quantity limit
        if (quantityLimitedRequest != null)
            query += quantityLimitedRequest.toOlisString() + "~";

        // Add mandatory requesting HIC
        if (requestingHic != null)
            query += requestingHic.toOlisString() + "~";

        // Add test code filters
        for (OBR4 testRequestCode : testRequestCodeList)
            query += testRequestCode.toOlisString() + "~";

        // Add result code filters
        for (OBX3 testResultCode : testResultCodeList)
            query += testResultCode.toOlisString() + "~";

        // Remove trailing delimiter
        if (query.endsWith("~")) {
            query = query.substring(0, query.length() - 1);
        }
        return query;
    }

    /**
     * Sets the start and end timestamp for the update window.
     *
     * This is a mandatory parameter defining the time range for updates.
     *
     * @param startEndTimestamp OBR22 the date range parameter
     * @since 2008-01-01
     */
    public void setStartEndTimestamp(OBR22 startEndTimestamp) {
        this.startEndTimestamp = startEndTimestamp;
    }

    /**
     * Sets the quantity limit for the number of results to return.
     *
     * @param quantityLimitedRequest QRD7 the quantity limit parameter
     * @since 2008-01-01
     */
    public void setQuantityLimitedRequest(QRD7 quantityLimitedRequest) {
        this.quantityLimitedRequest = quantityLimitedRequest;
    }

    /**
     * Sets the requesting Health Information Custodian identifier.
     *
     * This is a mandatory parameter identifying the provider or facility.
     *
     * @param requestingHic ZRP1 the HIC identifier parameter
     * @since 2008-01-01
     */
    public void setRequestingHic(ZRP1 requestingHic) {
        this.requestingHic = requestingHic;
    }

    /**
     * Sets the complete list of test request codes to filter by.
     *
     * @param testRequestCodeList List<OBR4> the list of test codes
     * @since 2008-01-01
     */
    public void setTestRequestCodeList(List<OBR4> testRequestCodeList) {
        this.testRequestCodeList = testRequestCodeList;
    }

    /**
     * Sets the complete list of test result codes to filter by.
     *
     * @param testResultCodeList List<OBX3> the list of result codes
     * @since 2008-01-01
     */
    public void setTestResultCodeList(List<OBX3> testResultCodeList) {
        this.testResultCodeList = testResultCodeList;
    }

    /**
     * Adds a test request code to the filter list.
     *
     * @param testRequestCode OBR4 the test code to add
     * @since 2008-01-01
     */
    public void addToTestRequestCodeList(OBR4 testRequestCode) {
        this.testRequestCodeList.add(testRequestCode);
    }

    /**
     * Adds a test result code to the filter list.
     *
     * @param testResultCode OBX3 the result code to add
     * @since 2008-01-01
     */
    public void addToTestResultCodeList(OBX3 testResultCode) {
        this.testResultCodeList.add(testResultCode);
    }

    /**
     * Gets the requesting Health Information Custodian ID number.
     *
     * @return String the HIC identifier number
     * @since 2008-01-01
     */
    public String getRequestingHicIdNumber() {
        return requestingHic.getIdNumber();
    }

    /**
     * Returns the query type identifier for this query.
     *
     * @return QueryType.Z04 indicating this is a provider updates query
     * @since 2008-01-01
     */
    @Override
    public QueryType getQueryType() {
        return QueryType.Z04;
    }

    /**
     * Sets the consent parameter (not supported for Z04).
     *
     * Z04 queries retrieve provider-level updates and do not support
     * patient-specific consent parameters.
     *
     * @param consentToViewBlockedInformation ZPD1 the consent parameter (ignored)
     * @throws RuntimeException always, as consent is not valid for Z04 queries
     * @since 2008-01-01
     */
    @Override
    public void setConsentToViewBlockedInformation(ZPD1 consentToViewBlockedInformation) {
        // Provider-level queries don't support patient consent parameters
        throw new RuntimeException("Not valid for this type of query.");
    }
}
