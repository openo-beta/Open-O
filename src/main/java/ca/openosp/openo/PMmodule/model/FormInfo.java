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
 * Data transfer object for clinical form metadata.
 *
 * FormInfo encapsulates summary information about clinical forms
 * completed by healthcare providers. This lightweight object is used
 * to display form listings, track form completion, and provide
 * quick access to form details without loading full form content.
 *
 * Key features:
 * - Form identification and tracking
 * - Provider association and attribution
 * - Temporal tracking of form completion
 * - Display optimization for form lists
 *
 * Common form types:
 * - Intake assessments
 * - Clinical evaluations
 * - Treatment plans
 * - Progress notes
 * - Discharge summaries
 *
 * Use cases:
 * - Form history displays
 * - Provider workload tracking
 * - Audit trail generation
 * - Quick form lookup
 * - Report generation
 *
 * This DTO pattern:
 * - Reduces database queries
 * - Optimizes memory usage
 * - Simplifies UI data binding
 * - Supports pagination
 *
 * @since 2005-01-01
 * @see FieldDefinition
 */
public class FormInfo {

    /**
     * Unique identifier of the form instance.
     * References the primary key of the form record.
     * Used for form retrieval and updates.
     */
    private Long formId;

    /**
     * Provider identifier who completed the form.
     * Links to the healthcare provider record.
     * Used for attribution and access control.
     */
    private Long providerNo;

    /**
     * Date and time when the form was completed.
     * Tracks temporal aspects of clinical documentation.
     * Used for chronological ordering and auditing.
     */
    private Date formDate;

    /**
     * Display name of the provider.
     * Cached for performance to avoid joins.
     * Format: "LastName, FirstName" or custom display format.
     */
    private String providerName;

    /**
     * Gets the form completion date.
     *
     * Returns the timestamp when the form was submitted
     * or last modified by the provider.
     *
     * @return Date the form completion date
     * @since 2005-01-01
     */
    public Date getFormDate() {
        return formDate;
    }

    /**
     * Sets the form completion date.
     *
     * Updates the timestamp for when the form was submitted.
     * Should reflect the actual completion time, not creation time.
     *
     * @param formDate Date the completion timestamp
     * @since 2005-01-01
     */
    public void setFormDate(Date formDate) {
        this.formDate = formDate;
    }

    /**
     * Gets the form identifier.
     *
     * Returns the unique ID used to retrieve the full form
     * content from the database.
     *
     * @return Long the form ID
     * @since 2005-01-01
     */
    public Long getFormId() {
        return formId;
    }

    /**
     * Sets the form identifier.
     *
     * Assigns the unique ID for this form instance.
     * Typically set when creating new form records.
     *
     * @param formId Long the unique form identifier
     * @since 2005-01-01
     */
    public void setFormId(Long formId) {
        this.formId = formId;
    }

    /**
     * Gets the provider display name.
     *
     * Returns the formatted name of the provider who
     * completed the form, cached for display purposes.
     *
     * @return String the provider's display name
     * @since 2005-01-01
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Sets the provider display name.
     *
     * Caches the provider's name for display without
     * requiring additional database queries.
     *
     * @param providerName String the formatted provider name
     * @since 2005-01-01
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * Gets the provider number.
     *
     * Returns the unique identifier of the healthcare provider
     * who completed or owns this form.
     *
     * @return Long the provider number
     * @since 2005-01-01
     */
    public Long getProviderNo() {
        return providerNo;
    }

    /**
     * Sets the provider number.
     *
     * Associates the form with a specific healthcare provider
     * for attribution and access control purposes.
     *
     * @param providerNo Long the provider identifier
     * @since 2005-01-01
     */
    public void setProviderNo(Long providerNo) {
        this.providerNo = providerNo;
    }
}
