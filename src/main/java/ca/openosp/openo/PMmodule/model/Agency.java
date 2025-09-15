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

import java.io.Serializable;
import java.util.Map;

/**
 * Entity representing a healthcare or social service agency in the system.
 *
 * An Agency represents an organization that provides healthcare or social services
 * and uses the PMmodule for program management. Agencies configure intake forms
 * and workflows specific to their organizational needs.
 *
 * Key features:
 * - Configures quick and in-depth intake forms
 * - Manages intake workflow states
 * - Supports multi-agency deployments
 * - Provides agency-specific customizations
 *
 * The agency model supports two types of intake processes:
 * - Quick intake: Rapid client registration for immediate services
 * - In-depth intake: Comprehensive assessment for long-term programs
 *
 * Each intake type has:
 * - Form configuration (form ID reference)
 * - Workflow state (typically "HSC" for Health Service Center)
 *
 * Static members:
 * - localAgency: The primary agency for this installation
 * - agencyMap: Cache of all agencies for multi-agency support
 *
 * Workflow states:
 * - "HSC": Health Service Center standard workflow
 * - Custom states can be defined per agency requirements
 *
 * Common agency types:
 * - Hospitals and health centers
 * - Community service organizations
 * - Shelters and housing programs
 * - Mental health and addiction services
 * - Social service departments
 *
 * Database mapping:
 * - Table: agency
 * - Primary key: id (auto-generated)
 * - Intake form references stored as IDs
 *
 * @since 2005-01-01
 * @see Program
 * @see FormInfo
 */
public class Agency implements Serializable {

    /**
     * Static reference to the local/primary agency.
     * This represents the main agency for single-agency deployments
     * or the currently active agency in multi-agency contexts.
     */
    private static Agency localAgency;

    /**
     * Static cache of all agencies in the system.
     * Used for multi-agency deployments to avoid repeated database queries.
     * Map structure allows quick lookup by agency identifier.
     */
    private static Map<?, ?> agencyMap;

    /**
     * Cached hash code for performance optimization.
     * Set to Integer.MIN_VALUE to indicate uncalculated state.
     * Recalculated when the ID changes.
     */
    private int hashCode = Integer.MIN_VALUE;

    /**
     * Primary key from the agency table.
     * Uniquely identifies this agency in the database.
     * Auto-generated on insert.
     */
    private Long id;

    /**
     * Form ID for quick intake process.
     * References the form configuration used for rapid client registration.
     * Null if quick intake is not configured for this agency.
     */
    private Integer intakeQuick;

    /**
     * Workflow state for quick intake process.
     * Defines the workflow rules and transitions for quick intake.
     * Default: "HSC" (Health Service Center standard workflow).
     */
    private String intakeQuickState = "HSC";

    /**
     * Form ID for in-depth intake process.
     * References the comprehensive assessment form configuration.
     * Null if in-depth intake is not configured for this agency.
     */
    private Integer intakeIndepth;

    /**
     * Workflow state for in-depth intake process.
     * Defines the workflow rules and transitions for comprehensive intake.
     * Default: "HSC" (Health Service Center standard workflow).
     */
    private String intakeIndepthState = "HSC";

    /**
     * Gets the local/primary agency for this installation.
     *
     * Returns the main agency configuration used by the system.
     * In single-agency deployments, this is the only agency.
     * In multi-agency deployments, this represents the current context.
     *
     * @return Agency the local agency instance, null if not set
     * @since 2005-01-01
     */
    public static Agency getLocalAgency() {
        return localAgency;
    }

    /**
     * Gets the map of all agencies in the system.
     *
     * Returns the cached map of agencies for multi-agency support.
     * The map structure depends on the implementation but typically
     * uses agency ID or code as the key.
     *
     * @return Map the agency map, null if not initialized
     * @since 2005-01-01
     */
    public static Map<?, ?> getAgencyMap() {
        return agencyMap;
    }

    /**
     * Sets the local/primary agency for this installation.
     *
     * Updates the static reference to the main agency.
     * This should be called during system initialization
     * or when switching agency context.
     *
     * @param agency Agency the agency to set as local/primary
     * @since 2005-01-01
     */
    public static void setLocalAgency(Agency agency) {
        localAgency = agency;
    }

    /**
     * Sets the map of all agencies in the system.
     *
     * Updates the cached agency map for multi-agency support.
     * Typically called during system initialization or when
     * agency configurations are refreshed.
     *
     * @param map Map the new agency map to cache
     * @since 2005-01-01
     */
    public static void setAgencyMap(Map<?, ?> map) {
        agencyMap = map;
    }

    /**
     * Default constructor for creating a new Agency instance.
     *
     * Creates an empty Agency with default workflow states.
     * The intake states default to "HSC" for both quick and
     * in-depth processes.
     *
     * @since 2005-01-01
     */
    public Agency() {
        // Default initialization handled by field declarations
    }

    /**
     * Constructor for creating an Agency with a specific ID.
     *
     * Used when loading an existing agency from the database
     * or when the ID is known in advance. The setId method
     * handles hash code invalidation.
     *
     * @param id Long the unique identifier for this agency
     * @since 2005-01-01
     */
    public Agency(Long id) {
        this.setId(id);
    }

    /**
     * Constructor for creating an Agency with required fields.
     *
     * Creates a fully configured agency with both quick intake
     * settings and workflow states. Note that intakeIndepth is
     * not included despite having a state parameter.
     *
     * @param id Long the unique identifier
     * @param intakeQuick Integer the quick intake form ID
     * @param intakeQuickState String the quick intake workflow state
     * @param intakeIndepthState String the in-depth intake workflow state
     * @since 2005-01-01
     */
    public Agency(Long id, Integer intakeQuick, String intakeQuickState, String intakeIndepthState) {
        this.setId(id);
        this.setIntakeQuick(intakeQuick);
        this.setIntakeQuickState(intakeQuickState);
        this.setIntakeIndepthState(intakeIndepthState);
    }

    /**
     * Gets the unique identifier of this agency.
     *
     * Returns the primary key value from the agency table.
     * This ID is used to reference the agency throughout
     * the system and in related tables.
     *
     * Database mapping:
     * - Column: id
     * - Generator: native (auto-increment)
     *
     * @return Long the unique identifier, null if not persisted
     * @since 2005-01-01
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this agency.
     *
     * Updates the primary key value and invalidates the cached
     * hash code to ensure it's recalculated with the new ID.
     * Typically called by persistence frameworks.
     *
     * @param id Long the new unique identifier
     * @since 2005-01-01
     */
    public void setId(Long id) {
        this.id = id;
        this.hashCode = Integer.MIN_VALUE;
    }

    /**
     * Gets the quick intake form configuration ID.
     *
     * Returns the form ID used for rapid client registration.
     * Quick intake is designed for immediate service needs
     * with minimal data collection.
     *
     * @return Integer the quick intake form ID, null if not configured
     * @since 2005-01-01
     */
    public Integer getIntakeQuick() {
        return intakeQuick;
    }

    /**
     * Sets the quick intake form configuration ID.
     *
     * Specifies which form to use for rapid client registration.
     * Set to null to disable quick intake for this agency.
     *
     * @param intakeQuick Integer the form ID for quick intake
     * @since 2005-01-01
     */
    public void setIntakeQuick(Integer intakeQuick) {
        this.intakeQuick = intakeQuick;
    }

    /**
     * Gets the workflow state for quick intake.
     *
     * Returns the workflow configuration that governs the
     * quick intake process, including states, transitions,
     * and business rules.
     *
     * @return String the workflow state, typically "HSC"
     * @since 2005-01-01
     */
    public String getIntakeQuickState() {
        return intakeQuickState;
    }

    /**
     * Sets the workflow state for quick intake.
     *
     * Configures which workflow rules apply to the quick
     * intake process. The state must match a defined
     * workflow in the system.
     *
     * @param intakeQuickState String the workflow state identifier
     * @since 2005-01-01
     */
    public void setIntakeQuickState(String intakeQuickState) {
        this.intakeQuickState = intakeQuickState;
    }

    /**
     * Gets the in-depth intake form configuration ID.
     *
     * Returns the form ID used for comprehensive client assessment.
     * In-depth intake collects detailed information for long-term
     * program enrollment and service planning.
     *
     * @return Integer the in-depth intake form ID, null if not configured
     * @since 2005-01-01
     */
    public Integer getIntakeIndepth() {
        return intakeIndepth;
    }

    /**
     * Sets the in-depth intake form configuration ID.
     *
     * Specifies which form to use for comprehensive assessment.
     * Set to null to disable in-depth intake for this agency.
     *
     * @param intakeIndepth Integer the form ID for in-depth intake
     * @since 2005-01-01
     */
    public void setIntakeIndepth(Integer intakeIndepth) {
        this.intakeIndepth = intakeIndepth;
    }

    /**
     * Gets the workflow state for in-depth intake.
     *
     * Returns the workflow configuration that governs the
     * comprehensive intake process, including assessment
     * stages, approvals, and service planning steps.
     *
     * @return String the workflow state, typically "HSC"
     * @since 2005-01-01
     */
    public String getIntakeIndepthState() {
        return intakeIndepthState;
    }

    /**
     * Sets the workflow state for in-depth intake.
     *
     * Configures which workflow rules apply to the comprehensive
     * intake process. The state must match a defined workflow
     * in the system.
     *
     * @param intakeIndepthState String the workflow state identifier
     * @since 2005-01-01
     */
    public void setIntakeIndepthState(String intakeIndepthState) {
        this.intakeIndepthState = intakeIndepthState;
    }


    /**
     * Determines equality based on the unique identifier.
     *
     * Two Agency objects are considered equal if they have
     * the same non-null ID. This is consistent with database
     * identity where the primary key determines uniqueness.
     *
     * @param obj Object the object to compare with
     * @return boolean true if objects have the same ID, false otherwise
     * @since 2005-01-01
     */
    @Override
    public boolean equals(Object obj) {
        if (null == obj) return false;
        if (!(obj instanceof Agency)) return false;
        else {
            Agency agency = (Agency) obj;
            if (null == this.getId() || null == agency.getId()) return false;
            else return (this.getId().equals(agency.getId()));
        }
    }

    /**
     * Generates a hash code based on the unique identifier.
     *
     * The hash code is cached for performance and recalculated
     * only when the ID changes. If ID is null, delegates to
     * superclass implementation.
     *
     * @return int the hash code for this agency
     * @since 2005-01-01
     */
    @Override
    public int hashCode() {
        if (Integer.MIN_VALUE == this.hashCode) {
            if (null == this.getId()) return super.hashCode();
            else {
                String hashStr = this.getClass().getName() + ":" + this.getId().hashCode();
                this.hashCode = hashStr.hashCode();
            }
        }
        return this.hashCode;
    }

    /**
     * Returns a string representation of this agency.
     *
     * Currently delegates to superclass implementation.
     * Could be enhanced to include agency name or other
     * identifying information for debugging.
     *
     * @return String the string representation
     * @since 2005-01-01
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
