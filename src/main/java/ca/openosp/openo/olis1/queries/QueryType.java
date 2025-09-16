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

/**
 * Enumeration of OLIS query types supported by the Ontario Laboratories Information System.
 *
 * Each query type represents a different method of searching for laboratory results
 * in the OLIS database. The query types are identified by Z-codes, which correspond
 * to specific stored procedures in the OLIS system.
 *
 * Query types and their purposes:
 * - Z01: Query by patient demographics (name, date of birth, gender)
 * - Z02: Query by requisition/accession number
 * - Z04: Provider inbox query for recent unacknowledged results
 * - Z05: Query by specimen collection number
 * - Z06: Query by test performed date range
 * - Z07: Query by health card number
 * - Z08: Query by ordering provider number
 * - Z50: Retrieve blocked information with explicit patient consent
 *
 * The choice of query type determines:
 * - Required and optional parameters
 * - Search criteria and filters
 * - Result set structure and content
 * - Performance characteristics
 *
 * Each query type has a corresponding Query implementation class (e.g., Z01Query)
 * that defines the specific parameters and formatting for that query type.
 *
 * @since 2008-01-01
 */
public enum QueryType {
    /**
     * Query by patient demographics.
     * Searches using patient name, date of birth, gender, and optional identifiers.
     * Most commonly used for patient-specific result retrieval.
     */
    Z01,

    /**
     * Query by requisition/accession number.
     * Direct lookup using the laboratory's requisition or accession number.
     * Provides the most specific and fastest result retrieval.
     */
    Z02,

    /**
     * Provider inbox query.
     * Retrieves recent unacknowledged laboratory results for a specific provider.
     * Used for provider workflow to review new results.
     */
    Z04,

    /**
     * Query by specimen collection number.
     * Searches using the unique identifier assigned to a specimen collection.
     * Useful for tracking specific specimens through the testing process.
     */
    Z05,

    /**
     * Query by test performed date range.
     * Retrieves results for tests performed within a specified time period.
     * Can be combined with patient or provider filters.
     */
    Z06,

    /**
     * Query by health card number.
     * Searches using the patient's Ontario health insurance number.
     * Provides reliable patient identification for result retrieval.
     */
    Z07,

    /**
     * Query by ordering provider.
     * Retrieves results for tests ordered by a specific healthcare provider.
     * Used for provider-centric result management and review.
     */
    Z08,

    /**
     * Retrieve blocked information with consent.
     * Special query type for accessing privacy-protected results.
     * Requires explicit patient consent documentation.
     */
    Z50
}
