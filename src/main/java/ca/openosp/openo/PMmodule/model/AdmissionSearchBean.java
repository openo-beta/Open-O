//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.PMmodule.model;

import java.util.Date;

/**
 * Data transfer object for searching program admissions.
 *
 * This bean encapsulates search criteria used to query and filter
 * program admissions in the PMmodule. It provides a clean interface
 * for passing multiple search parameters between layers of the application.
 *
 * Search capabilities include:
 * - Provider-based filtering to find admissions by staff member
 * - Status filtering (active, discharged, suspended, etc.)
 * - Client-specific searches
 * - Date range queries for admission periods
 * - Program-specific filtering
 *
 * Common use cases:
 * - Finding all active admissions for a provider's caseload
 * - Locating a specific client's admission history
 * - Generating reports for admissions within a date range
 * - Auditing admission activity for a program
 * - Identifying discharged clients for follow-up
 *
 * The bean supports flexible searching with any combination of criteria.
 * Null values typically indicate that criterion should not be applied,
 * allowing for both broad and narrow searches.
 *
 * Search examples:
 * - All admissions for provider "12345" in program 10
 * - Active admissions between January 1 and March 31
 * - All historical admissions for client 9876
 * - Discharged clients from program 5 last month
 *
 * Implementation notes:
 * - Date ranges are inclusive of both start and end dates
 * - Provider numbers are stored as strings to accommodate various formats
 * - Client IDs use Long to match demographic table primary keys
 * - Program IDs use Integer consistent with program table
 *
 * This bean is typically used with:
 * - AdmissionDao for database queries
 * - AdmissionManager for business logic
 * - Admission controllers for web layer
 * - Report generators for admission statistics
 *
 * @since 2005-01-01
 * @see ProgramClientStatus
 * @see Program
 */
public class AdmissionSearchBean {
    /**
     * Provider number for filtering admissions by staff member.
     * Matches the provider_no in the provider table.
     * Used to find admissions assigned to or managed by a specific provider.
     */
    private String providerNo;

    /**
     * Status filter for admission state.
     * Common values: "current", "discharged", "suspended", "completed".
     * Null indicates all statuses should be included.
     */
    private String admissionStatus;

    /**
     * Client demographic ID for client-specific searches.
     * References the demographic_no in the demographic table.
     * Used to find all admissions for a particular client.
     */
    private Long clientId;

    /**
     * Start date for admission date range queries.
     * Admissions on or after this date will be included.
     * Null indicates no lower bound on admission date.
     */
    private Date startDate;

    /**
     * End date for admission date range queries.
     * Admissions on or before this date will be included.
     * Null indicates no upper bound on admission date.
     */
    private Date endDate;

    /**
     * Program ID for program-specific filtering.
     * References the program_id in the program table.
     * Used to find admissions to a specific program.
     */
    private Integer programId;

    /**
     * Gets the program ID filter criterion.
     *
     * Returns the program identifier used to filter admissions
     * to a specific program. A null value indicates that admissions
     * from all programs should be included in the search results.
     *
     * @return Integer the program ID filter, null for all programs
     * @since 2005-01-01
     */
    public Integer getProgramId() {
        return programId;
    }

    /**
     * Sets the program ID filter criterion.
     *
     * Specifies which program's admissions should be included
     * in the search results. Set to null to include admissions
     * from all programs.
     *
     * @param programId Integer the program ID to filter by, null for all programs
     * @since 2005-01-01
     */
    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

    /**
     * Gets the admission status filter criterion.
     *
     * Returns the status used to filter admissions by their current state.
     * Common values include "current" for active admissions, "discharged"
     * for completed admissions, and "suspended" for temporarily inactive.
     *
     * @return String the admission status filter, null for all statuses
     * @since 2005-01-01
     */
    public String getAdmissionStatus() {
        return admissionStatus;
    }

    /**
     * Sets the admission status filter criterion.
     *
     * Specifies which admission status to filter by. The value should
     * match valid status codes used in the system. Set to null to
     * include admissions of all statuses.
     *
     * @param admissionStatus String the status to filter by, null for all statuses
     * @since 2005-01-01
     */
    public void setAdmissionStatus(String admissionStatus) {
        this.admissionStatus = admissionStatus;
    }

    /**
     * Gets the client ID filter criterion.
     *
     * Returns the demographic number used to filter admissions
     * for a specific client. This allows searching for all
     * admissions (historical and current) for an individual.
     *
     * @return Long the client demographic ID, null for all clients
     * @since 2005-01-01
     */
    public Long getClientId() {
        return clientId;
    }

    /**
     * Sets the client ID filter criterion.
     *
     * Specifies which client's admissions should be retrieved.
     * The ID should correspond to a valid demographic_no.
     * Set to null to include admissions for all clients.
     *
     * @param clientId Long the client demographic ID to filter by
     * @since 2005-01-01
     */
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the end date for admission date range filtering.
     *
     * Returns the upper bound for admission date filtering.
     * Admissions with dates on or before this date will be
     * included in the search results.
     *
     * @return Date the end date of the range, null for no upper limit
     * @since 2005-01-01
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date for admission date range filtering.
     *
     * Specifies the latest admission date to include in results.
     * The search will include admissions on or before this date.
     * Set to null to have no upper limit on admission dates.
     *
     * @param endDate Date the end of the date range, inclusive
     * @since 2005-01-01
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets the provider number filter criterion.
     *
     * Returns the provider number used to filter admissions
     * by the assigned or managing healthcare provider.
     * Useful for generating provider-specific caseload reports.
     *
     * @return String the provider number, null for all providers
     * @since 2005-01-01
     */
    public String getProviderNo() {
        return providerNo;
    }

    /**
     * Sets the provider number filter criterion.
     *
     * Specifies which provider's associated admissions should
     * be included in the search. The provider number should
     * match a valid provider_no in the system.
     *
     * @param providerNo String the provider number to filter by
     * @since 2005-01-01
     */
    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * Gets the start date for admission date range filtering.
     *
     * Returns the lower bound for admission date filtering.
     * Admissions with dates on or after this date will be
     * included in the search results.
     *
     * @return Date the start date of the range, null for no lower limit
     * @since 2005-01-01
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date for admission date range filtering.
     *
     * Specifies the earliest admission date to include in results.
     * The search will include admissions on or after this date.
     * Set to null to have no lower limit on admission dates.
     *
     * @param startDate Date the start of the date range, inclusive
     * @since 2005-01-01
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
