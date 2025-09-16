//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.olis1.segments;

import java.util.Random;

import ca.openosp.openo.olis1.queries.Query;
import ca.openosp.openo.olis1.queries.QueryType;

/**
 * Stored Procedure Request Segment (SPR) for OLIS HL7 queries.
 *
 * The SPR segment encapsulates query parameters and stored procedure information
 * for OLIS database queries. It serves as the primary query specification segment
 * in SPQ (Stored Procedure Query) messages.
 *
 * This segment contains:
 * - Transaction ID for query tracking and correlation
 * - Query status indicator (R for Request)
 * - Stored procedure name based on query type
 * - Query parameters encoded in HL7 format
 *
 * OLIS uses specific stored procedures for different query types:
 * - Z01: Patient-based laboratory information retrieval
 * - Z02: Order ID-based laboratory information retrieval
 * - Z04: Practitioner update queries
 * - Z05: Laboratory update queries
 * - Z06: Healthcare facility update queries
 * - Z07: Public Health Branch reporting queries
 * - Z08: Cancer Care Ontario reporting queries
 * - Z50: Patient identification by demographics
 *
 * HL7 SPR Segment Structure:
 * - SPR-1: Query Tag (transaction ID)
 * - SPR-2: Query/Response Format Code (always "R" for request)
 * - SPR-3: Stored Procedure ID (procedure name with HL70471 coding)
 * - SPR-4: Input Parameter List (query parameters)
 *
 * The transaction ID is randomly generated and used to correlate
 * requests with responses in asynchronous processing scenarios.
 *
 * @since 2008-01-01
 */
public class SPRSegment implements Segment {

    /**
     * The type of OLIS query being performed.
     * Determines which stored procedure will be invoked.
     */
    private QueryType queryType;

    /**
     * The query object containing all search parameters.
     * Provides the parameter values for the stored procedure.
     */
    private Query query;

    /**
     * Random number generator for transaction ID creation.
     * Ensures unique transaction identifiers for query tracking.
     */
    private Random r = new Random();

    /**
     * Unique transaction identifier for this query.
     * Used to correlate requests with responses and for audit logging.
     * Range: 0 to 999999999
     */
    private Integer transactionId = r.nextInt(999999999);

    /**
     * Gets the unique transaction ID for this query.
     *
     * The transaction ID is used to:
     * - Correlate requests with responses
     * - Track queries in audit logs
     * - Identify queries in error messages
     * - Support asynchronous processing
     *
     * @return Integer the unique transaction identifier
     * @since 2008-01-01
     */
    public Integer getTransactionId() {
        return transactionId;
    }

    /**
     * Constructs an SPR segment for the specified query.
     *
     * Creates a new SPR segment with a randomly generated transaction ID
     * and associates it with the provided query type and parameters.
     *
     * @param queryType QueryType the type of OLIS query to perform
     * @param query Query the query object containing search parameters
     * @since 2008-01-01
     */
    public SPRSegment(QueryType queryType, Query query) {
        this.queryType = queryType;
        this.query = query;
    }

    /**
     * Determines the OLIS stored procedure name based on query type.
     *
     * Maps each query type to its corresponding OLIS database stored procedure:
     * - Z01: Query lab info by patient ID (demographics-based search)
     * - Z02: Query lab info by order ID (accession number search)
     * - Z04: Query updates for practitioner (provider-specific results)
     * - Z05: Query updates for laboratory (lab-specific results)
     * - Z06: Query updates for healthcare facility (facility-wide results)
     * - Z07: Query by Public Health Branch reporting flag
     * - Z08: Query by Cancer Care Ontario reporting flag
     * - Z50: Identify patient by name, sex, and date of birth
     *
     * @return String the OLIS stored procedure name, or empty string if unknown type
     * @since 2008-01-01
     */
    private String getStoredProcedureName() {
        if (queryType == QueryType.Z01)
            return "Z_QryLabInfoForPatientID";
        else if (queryType == QueryType.Z02)
            return "Z_QryLabInfoForOrderID";
        else if (queryType == QueryType.Z04)
            return "Z_QryLabInfoUpdatesForPractitionerID";
        else if (queryType == QueryType.Z05)
            return "Z_QryLabInfoUpdatesForLaboratoryID";
        else if (queryType == QueryType.Z06)
            return "Z_QryLabInfoUpdatesForHCFID";
        else if (queryType == QueryType.Z07)
            return "Z_QryLabInfoByPHBReportFlag";
        else if (queryType == QueryType.Z08)
            return "Z_QryLabInfoByCCOReportFlag";
        else if (queryType == QueryType.Z50)
            return "Z_IDPatientByNameSexDoB";

        return "";
    }

    /**
     * Generates the SPR segment HL7 string.
     *
     * Constructs the complete SPR segment with:
     * - SPR-1: Transaction ID for query tracking
     * - SPR-2: "R" indicating this is a request
     * - SPR-3: Stored procedure name with HL70471 coding system
     * - SPR-4: Query parameters from the Query object
     *
     * Note: The trailing tilde removal logic appears to be redundant
     * as the query parameters are appended after this check. This may
     * be legacy code that could be reviewed for cleanup.
     *
     * @return String the complete SPR segment for the OLIS message
     * @since 2008-01-01
     */
    @Override
    public String getSegmentHL7String() {
        String queryHl7String = query.getQueryHL7String();

        // Remove trailing tilde if present (legacy cleanup)
        if (queryHl7String.charAt(queryHl7String.length() - 1) == '~')
            queryHl7String = queryHl7String.substring(0, queryHl7String.length() - 2);

        return "SPR|" + transactionId + "|R|" + getStoredProcedureName() + "^^HL70471|" + query.getQueryHL7String();
    }

}
