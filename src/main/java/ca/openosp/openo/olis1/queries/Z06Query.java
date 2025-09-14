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
import ca.openosp.openo.olis1.parameters.ORC21;
import ca.openosp.openo.olis1.parameters.QRD7;
import ca.openosp.openo.olis1.parameters.ZPD1;

/**
 * Z06 Query - Retrieve Laboratory Information Updates for Ordering Facility.
 *
 * This query type retrieves laboratory information updates based on the ordering
 * facility filter. It is specifically designed to fetch laboratory results that
 * have been ordered by or are associated with a particular ordering facility.
 *
 * The Z06 query is commonly used by healthcare facilities to track and retrieve
 * test results for orders they have placed with various laboratories. This supports
 * clinical workflow management and ensures that ordering facilities receive updates
 * on the tests they have requested.
 *
 * Supported parameters:
 * - Start and end timestamp (mandatory) - Defines the date range for updates
 * - Quantity limited request (optional) - Limits the number of results returned
 * - Ordering facility ID (mandatory) - Specifies which facility's orders to track
 *
 * Mandatory parameters:
 * - Start/End timestamp (OBR22) - Time range for the query
 * - Ordering facility ID (ORC21) - Healthcare facility identifier
 *
 * The query results include all laboratory information updates for orders placed
 * by the specified ordering facility within the given time frame, subject to
 * quantity limits if specified.
 *
 * @since 2011-06-24
 */
public class Z06Query extends Query {

    /**
     * Start and end timestamp for the query date range (mandatory).
     * Defines the time period for which laboratory updates should be retrieved.
     */
    private OBR22 startEndTimestamp = new OBR22();

    /**
     * Quantity limited request parameter (optional).
     * Specifies the maximum number of results to return.
     */
    private QRD7 quantityLimitedRequest = null;

    /**
     * Ordering facility identifier (mandatory).
     * Specifies the healthcare facility whose orders are being tracked.
     */
    private ORC21 orderingFacilityId = new ORC21();

    /**
     * Generates the HL7 query string for the Z06 query.
     *
     * Constructs the query parameter string by concatenating all non-null
     * parameters in the proper sequence, separated by "~" delimiters.
     * The order follows OLIS specifications for Z06 queries.
     * Removes trailing delimiter if present.
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

        // Add mandatory ordering facility ID
        if (orderingFacilityId != null)
            query += orderingFacilityId.toOlisString() + "~";

        // Remove trailing delimiter
        if (query.endsWith("~")) {
            query = query.substring(0, query.length() - 1);
        }

        return query;
    }

    /**
     * Sets the start and end timestamp for the query date range.
     *
     * This is a mandatory parameter that defines the time window
     * for which laboratory updates should be retrieved.
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
     * Sets the ordering facility identifier.
     *
     * This is a mandatory parameter that specifies which healthcare
     * facility's orders should be tracked for updates.
     *
     * @param orderingFacilityId ORC21 the ordering facility parameter
     * @since 2011-06-24
     */
    public void setOrderingFacilityId(ORC21 orderingFacilityId) {
        this.orderingFacilityId = orderingFacilityId;
    }

    /**
     * Returns the query type identifier for this query.
     *
     * @return QueryType.Z06 indicating this is an ordering facility updates query
     * @since 2011-06-24
     */
    @Override
    public QueryType getQueryType() {
        return QueryType.Z06;
    }

    /**
     * Sets the consent parameter for viewing blocked information.
     *
     * This method is not supported for Z06 queries as they do not
     * support patient consent parameters.
     *
     * @param consentToViewBlockedInformation ZPD1 the consent parameter
     * @throws RuntimeException always, as this operation is not valid for Z06 queries
     * @since 2011-06-24
     */
    @Override
    public void setConsentToViewBlockedInformation(ZPD1 consentToViewBlockedInformation) {
        throw new RuntimeException("Not valid for this type of query.");
    }
}
