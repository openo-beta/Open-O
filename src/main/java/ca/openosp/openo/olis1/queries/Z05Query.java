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
import ca.openosp.openo.olis1.parameters.ZBR8;
import ca.openosp.openo.olis1.parameters.ZPD1;

/**
 * Z05 Query - Retrieve Laboratory Information Updates for Destination Laboratory.
 *
 * This query type retrieves laboratory information updates based on the destination
 * laboratory filter. It is specifically designed to fetch laboratory results that
 * have been sent or are intended for a particular destination laboratory.
 *
 * The Z05 query is typically used by laboratory information systems to track
 * and retrieve test results that have been distributed to specific destination
 * laboratories. This is useful for inter-laboratory communication and result
 * sharing workflows.
 *
 * Supported parameters:
 * - Start and end timestamp (mandatory) - Defines the date range for updates
 * - Quantity limited request (optional) - Limits the number of results returned
 * - Destination laboratory (mandatory) - Specifies which laboratory to filter by
 *
 * Mandatory parameters:
 * - Start/End timestamp (OBR22) - Time range for the query
 * - Destination laboratory (ZBR8) - Target laboratory identifier
 *
 * The query results include all laboratory information updates for the specified
 * destination laboratory within the given time frame, subject to quantity limits
 * if specified.
 *
 * @since 2011-06-24
 */
public class Z05Query extends Query {

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
     * Destination laboratory identifier (mandatory).
     * Specifies the target laboratory for which updates are being retrieved.
     */
    private ZBR8 destinationLaboratory = new ZBR8();

    /**
     * Generates the HL7 query string for the Z05 query.
     *
     * Constructs the query parameter string by concatenating all non-null
     * parameters in the proper sequence, separated by "~" delimiters.
     * The order follows OLIS specifications for Z05 queries.
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

        // Add mandatory destination laboratory
        if (destinationLaboratory != null)
            query += destinationLaboratory.toOlisString() + "~";

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
     * Sets the destination laboratory identifier.
     *
     * This is a mandatory parameter that specifies which destination
     * laboratory's updates should be retrieved.
     *
     * @param destinationLaboratory ZBR8 the destination laboratory parameter
     * @since 2011-06-24
     */
    public void setDestinationLaboratory(ZBR8 destinationLaboratory) {
        this.destinationLaboratory = destinationLaboratory;
    }

    /**
     * Returns the query type identifier for this query.
     *
     * @return QueryType.Z05 indicating this is a destination laboratory updates query
     * @since 2011-06-24
     */
    @Override
    public QueryType getQueryType() {
        return QueryType.Z05;
    }

    /**
     * Sets the consent parameter for viewing blocked information.
     *
     * This method is not supported for Z05 queries as they do not
     * support patient consent parameters.
     *
     * @param consentToViewBlockedInformation ZPD1 the consent parameter
     * @throws RuntimeException always, as this operation is not valid for Z05 queries
     * @since 2011-06-24
     */
    @Override
    public void setConsentToViewBlockedInformation(ZPD1 consentToViewBlockedInformation) {
        throw new RuntimeException("Not valid for this type of query.");
    }
}
