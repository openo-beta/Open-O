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

import ca.openosp.openo.olis1.parameters.OBR22;
import ca.openosp.openo.olis1.parameters.QRD7;
import ca.openosp.openo.olis1.parameters.ZPD1;

/**
 * Z08 Query - Retrieve Test Results Reportable to Cancer Care Ontario.
 *
 * This query type retrieves laboratory test results that are reportable to
 * Cancer Care Ontario (CCO). It is specifically designed to identify and fetch
 * test results that require notification to CCO as part of cancer surveillance
 * and reporting requirements in Ontario, Canada.
 *
 * The Z08 query is used by healthcare facilities and cancer reporting systems
 * to comply with mandatory cancer reporting requirements. It helps identify
 * test results that indicate the presence of malignant conditions or cancer-related
 * markers that require reporting to the provincial cancer agency.
 *
 * Supported parameters:
 * - Start and end timestamp (mandatory) - Defines the date range for results
 * - Quantity limited request (optional) - Limits the number of results returned
 *
 * Mandatory parameters:
 * - Start/End timestamp (OBR22) - Time range for the query
 *
 * The query results include all test results that meet CCO reporting criteria
 * within the specified time frame, subject to quantity limits if specified.
 * Results are filtered based on test codes and result values that indicate
 * cancer-related conditions requiring CCO notification.
 *
 * @since 2011-06-24
 */
public class Z08Query extends Query {

    /**
     * Start and end timestamp for the query date range (mandatory).
     * Defines the time period for which CCO-reportable test results should be retrieved.
     */
    private OBR22 startEndTimestamp = new OBR22();

    /**
     * Quantity limited request parameter (optional).
     * Specifies the maximum number of results to return.
     */
    private QRD7 quantityLimitedRequest = null;

    /**
     * Generates the HL7 query string for the Z08 query.
     *
     * Constructs the query parameter string by concatenating all non-null
     * parameters in the proper sequence, separated by "~" delimiters.
     * The order follows OLIS specifications for Z08 queries.
     * Note: This query intentionally does not remove trailing delimiters.
     *
     * @return String the formatted HL7 query parameter string
     * @since 2011-06-24
     */
    @Override
    public String getQueryHL7String() {
        String query = "";

        // Add mandatory timestamp range
        if (startEndTimestamp != null)
            query += startEndTimestamp.toOlisString() + "~";

        // Add optional quantity limit
        if (quantityLimitedRequest != null)
            query += quantityLimitedRequest.toOlisString() + "~";

        return query;
    }

    /**
     * Sets the start and end timestamp for the query date range.
     *
     * This is a mandatory parameter that defines the time window
     * for which CCO-reportable test results should be retrieved.
     *
     * @param startEndTimestamp OBR22 the date range parameter
     * @since 2011-06-24
     */
    public void setStartEndTimestamp(OBR22 startEndTimestamp) {
        this.startEndTimestamp = startEndTimestamp;
    }

    /**
     * Sets the quantity limit for the number of results to return.
     *
     * This optional parameter allows limiting the number of results
     * returned by the query to prevent large result sets.
     *
     * @param quantityLimitedRequest QRD7 the quantity limit parameter
     * @since 2011-06-24
     */
    public void setQuantityLimitedRequest(QRD7 quantityLimitedRequest) {
        this.quantityLimitedRequest = quantityLimitedRequest;
    }

    /**
     * Returns the query type identifier for this query.
     *
     * @return QueryType.Z08 indicating this is a CCO reportable results query
     * @since 2011-06-24
     */
    @Override
    public QueryType getQueryType() {
        return QueryType.Z08;
    }

    /**
     * Sets the consent parameter for viewing blocked information.
     *
     * This method is not supported for Z08 queries as they do not
     * support patient consent parameters. CCO reportable results
     * have specific privacy and consent handling requirements for
     * cancer surveillance purposes.
     *
     * @param consentToViewBlockedInformation ZPD1 the consent parameter
     * @throws RuntimeException always, as this operation is not valid for Z08 queries
     * @since 2011-06-24
     */
    @Override
    public void setConsentToViewBlockedInformation(ZPD1 consentToViewBlockedInformation) {
        throw new RuntimeException("Not valid for this type of query.");
    }
}
