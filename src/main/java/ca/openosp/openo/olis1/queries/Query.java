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

import java.util.Date;

import ca.openosp.openo.olis1.parameters.ZPD1;

/**
 * Abstract base class for all OLIS query types.
 *
 * This class provides the common structure and functionality for all OLIS queries.
 * Each query represents a specific type of laboratory information request that can
 * be submitted to the Ontario Laboratories Information System.
 *
 * OLIS supports multiple query types, each identified by a Z-code:
 * - Z01: Patient query by demographics
 * - Z02: Query by requisition number
 * - Z04: Provider query for recent results
 * - Z05: Query by specimen ID
 * - Z06: Query by test performed date range
 * - Z07: Query by health card number
 * - Z08: Query by ordering provider
 * - Z50: Retrieve blocked information with consent
 *
 * All queries share common attributes like execution date, provider information,
 * and patient demographics, while each specific query type adds its own parameters
 * and search criteria.
 *
 * The class implements Cloneable to support query reuse and modification for
 * pagination or refined searches.
 *
 * @since 2008-01-01
 */
public abstract class Query implements Cloneable {

    /**
     * Timestamp when the query is executed.
     * Used for audit trails and tracking query history.
     */
    private Date queryExecutionDate;

    /**
     * Provider number of the healthcare provider initiating the query.
     * Required for authorization and audit purposes.
     */
    private String initiatingProviderNo;

    /**
     * Unique identifier for this query instance.
     * Used for tracking and correlating queries with responses.
     */
    private String uuid;

    /**
     * Patient demographic number if this query is patient-specific.
     * May be null for provider-wide or facility-wide queries.
     */
    public String demographicNo;

    /**
     * Gets the patient demographic number associated with this query.
     *
     * @return String the patient demographic number, or null if not patient-specific
     * @since 2008-01-01
     */
    public String getDemographicNo() {
        return demographicNo;
    }

    /**
     * Sets the patient demographic number for this query.
     *
     * Associates the query with a specific patient record. This is optional
     * for some query types that search across multiple patients.
     *
     * @param demographicNo String the patient demographic number
     * @since 2008-01-01
     */
    public void setDemographicNo(String demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Generates the HL7 string representation of the query parameters.
     *
     * Each query type implements this method to format its specific parameters
     * according to HL7 v2.5 and OLIS specifications. The output is used to
     * construct the SPR (Stored Procedure Request) segment of the OLIS message.
     *
     * @return String the HL7-formatted query parameter string
     * @since 2008-01-01
     */
    public abstract String getQueryHL7String();

    /**
     * Returns the specific type of this query.
     *
     * The query type determines how the query is processed by OLIS and what
     * kind of results are returned. Each type corresponds to a specific OLIS
     * stored procedure (Z01 through Z50).
     *
     * @return QueryType the enumerated query type identifier
     * @since 2008-01-01
     */
    public abstract QueryType getQueryType();

    /**
     * Sets the consent parameter for viewing blocked information.
     *
     * Some laboratory results may be blocked for privacy reasons. This method
     * allows setting explicit consent to view such blocked information when
     * the patient has provided appropriate authorization.
     *
     * @param consentToViewBlockedInformation ZPD1 the consent parameter object
     * @since 2008-01-01
     */
    public abstract void setConsentToViewBlockedInformation(ZPD1 consentToViewBlockedInformation);

    /**
     * Creates a shallow copy of this query.
     *
     * Used to duplicate queries for modification or resubmission. The clone
     * maintains the same parameter values but gets a new identity for tracking.
     *
     * @return Object a cloned instance of this query
     * @throws InternalError if cloning is not supported (should never happen)
     * @since 2008-01-01
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new InternalError(e.toString());
        }
    }

    /**
     * Retrieves the requesting HIC (Health Information Custodian) provider number.
     *
     * This method extracts the HIC provider number from queries that support it
     * (currently Z01 and Z04). The HIC number identifies the healthcare facility
     * or organization requesting the information.
     *
     * @return String the HIC provider number, or null if not applicable to this query type
     * @since 2008-01-01
     */
    public String getRequestingHICProviderNo() {
        // Only certain query types have HIC provider numbers
        if (this instanceof Z04Query) {
            return ((Z04Query) this).getRequestingHicIdNumber();
        }
        if (this instanceof Z01Query) {
            return ((Z01Query) this).getRequestingHicIdNumber();
        }
        return null;
    }

    /**
     * Gets the date and time when this query was executed.
     *
     * @return Date the query execution timestamp
     * @since 2008-01-01
     */
    public Date getQueryExecutionDate() {
        return queryExecutionDate;
    }

    /**
     * Sets the date and time when this query is executed.
     *
     * Typically set automatically when the query is submitted to OLIS.
     * Used for audit logging and query history tracking.
     *
     * @param queryExecutionDate Date the execution timestamp
     * @since 2008-01-01
     */
    public void setQueryExecutionDate(Date queryExecutionDate) {
        this.queryExecutionDate = queryExecutionDate;
    }

    /**
     * Gets the provider number of the healthcare provider who initiated this query.
     *
     * @return String the initiating provider's identification number
     * @since 2008-01-01
     */
    public String getInitiatingProviderNo() {
        return initiatingProviderNo;
    }

    /**
     * Sets the provider number of the healthcare provider initiating this query.
     *
     * Required for authorization checks and audit trails. The provider must
     * be authorized to access OLIS and view the requested information.
     *
     * @param initiatingProviderNo String the provider's identification number
     * @since 2008-01-01
     */
    public void setInitiatingProviderNo(String initiatingProviderNo) {
        this.initiatingProviderNo = initiatingProviderNo;
    }

    /**
     * Gets the unique identifier for this query instance.
     *
     * @return String the UUID identifying this specific query
     * @since 2008-01-01
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the unique identifier for this query instance.
     *
     * The UUID is used to track the query through its lifecycle and correlate
     * it with responses and log entries. Should be set to a unique value before
     * query submission.
     *
     * @param uuid String the unique identifier for this query
     * @since 2008-01-01
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
