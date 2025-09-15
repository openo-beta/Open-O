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
import java.util.Date;

import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.Provider;

/**
 * Entity representing service restrictions for clients in programs.
 *
 * ProgramClientRestriction manages temporary or permanent restrictions
 * placed on clients' access to specific programs or services. These
 * restrictions are used for behavioral management, compliance enforcement,
 * and safety protocols within healthcare and social service programs.
 *
 * Key features:
 * - Time-bound restrictions with start/end dates
 * - Provider-initiated and documented restrictions
 * - Early termination capability
 * - Audit trail with comments and reasons
 * - Enable/disable without deletion
 *
 * Common restriction scenarios:
 * - Behavioral incidents requiring temporary suspension
 * - Non-compliance with program rules
 * - Safety concerns for client or others
 * - Medical contraindications
 * - Administrative or legal requirements
 *
 * Restriction lifecycle:
 * - Creation: Provider documents reason and duration
 * - Active period: Client barred from program access
 * - Review: Periodic assessment of restriction necessity
 * - Termination: Natural expiry or early termination
 * - History: Maintained for compliance and reporting
 *
 * The restriction system enables:
 * - Client safety management
 * - Program integrity maintenance
 * - Regulatory compliance tracking
 * - Risk mitigation strategies
 * - Appeal and review processes
 *
 * Integration points:
 * - Admission screening checks
 * - Service eligibility validation
 * - Incident reporting systems
 * - Case management workflows
 * - Compliance reporting
 *
 * Database mapping:
 * - Table: program_client_restriction
 * - Primary key: id (auto-generated)
 * - Foreign keys: program_id, demographic_no, provider_no
 *
 * @since 2005-01-01
 * @see Program
 * @see Demographic
 * @see Provider
 * @see ServiceRestrictionException
 */
public class ProgramClientRestriction implements Serializable {

    /**
     * Primary key for the restriction record.
     * Auto-generated unique identifier.
     */
    private Integer id;

    /**
     * Foreign key reference to the program.
     * Identifies which program the restriction applies to.
     */
    private int programId;

    /**
     * Foreign key reference to the client.
     * Identifies the restricted client/patient.
     */
    private int demographicNo;

    /**
     * Provider who initiated the restriction.
     * Used for accountability and follow-up.
     */
    private String providerNo;

    /**
     * Optional reference to detailed comment record.
     * Links to extended documentation if needed.
     */
    private String commentId;

    /**
     * Restriction reason and details.
     * Documents the cause and terms of restriction.
     */
    private String comments;

    /**
     * Effective start date of the restriction.
     * Client is restricted from this date forward.
     */
    private Date startDate;

    /**
     * Scheduled end date of the restriction.
     * Restriction automatically expires on this date.
     */
    private Date endDate;

    /**
     * Active status flag.
     * True if restriction is currently enforced.
     */
    private boolean enabled;

    /**
     * Provider who terminated restriction early.
     * Null if restriction expired naturally.
     */
    private String earlyTerminationProvider;

    /**
     * Associated program entity.
     * The program from which client is restricted.
     */
    private Program program;

    /**
     * Associated client/patient entity.
     * The demographic record of restricted individual.
     */
    private Demographic client;

    /**
     * Associated provider entity.
     * The provider who created the restriction.
     */
    private Provider provider;

    /**
     * Default constructor for creating new ProgramClientRestriction.
     *
     * Initializes an empty restriction with ID set to 0.
     * All fields must be populated before persisting.
     *
     * @since 2005-01-01
     */
    public ProgramClientRestriction() {
        id = 0;
    }

    /**
     * Full constructor with all core restriction fields.
     *
     * Creates a complete restriction record with all required
     * attributes for immediate persistence.
     *
     * @param id Integer the restriction ID
     * @param programId int the program identifier
     * @param demographicNo int the client identifier
     * @param providerNo String the provider identifier
     * @param comments String the restriction reason
     * @param startDate Date the restriction start
     * @param endDate Date the restriction end
     * @param enabled boolean the active status
     * @param program Program the program entity
     * @param client Demographic the client entity
     * @since 2005-01-01
     */
    public ProgramClientRestriction(Integer id, int programId, int demographicNo, String providerNo, String comments, Date startDate, Date endDate, boolean enabled, Program program, Demographic client) {
        this.id = id;
        this.programId = programId;
        this.demographicNo = demographicNo;
        this.providerNo = providerNo;
        this.comments = comments;
        this.startDate = startDate;
        this.endDate = endDate;
        this.enabled = enabled;
        this.program = program;
        this.client = client;
    }

    /**
     * Gets the provider number who created the restriction.
     *
     * Returns the identifier of the healthcare provider
     * who initiated this service restriction.
     *
     * @return String the provider number
     * @since 2005-01-01
     */
    public String getProviderNo() {
        return providerNo;
    }

    /**
     * Calculates days remaining in the restriction period.
     *
     * Computes the number of days between start and end dates.
     * Returns negative values if restriction has expired.
     *
     * @return long the number of days remaining
     * @since 2005-01-01
     */
    public long getDaysRemaining() {
        return (this.getEndDate().getTime() - this.getStartDate().getTime()) / 1000 / 60 / 60 / 24;
    }

    /**
     * Sets the provider number who created the restriction.
     *
     * Updates the provider responsible for initiating
     * this service restriction.
     *
     * @param providerNo String the provider identifier
     * @since 2005-01-01
     */
    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * Gets the unique identifier of this restriction.
     *
     * Returns the primary key from the database.
     * Zero indicates an unsaved restriction.
     *
     * @return Integer the restriction ID
     * @since 2005-01-01
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the unique identifier of this restriction.
     *
     * Updates the primary key value. Typically set by
     * persistence framework during save operations.
     *
     * @param id Integer the restriction ID
     * @since 2005-01-01
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the program identifier for this restriction.
     *
     * Returns the ID of the program from which the
     * client is restricted.
     *
     * @return int the program ID
     * @since 2005-01-01
     */
    public int getProgramId() {
        return programId;
    }

    /**
     * Sets the program identifier for this restriction.
     *
     * Specifies which program the client is restricted from.
     * Must reference a valid program record.
     *
     * @param programId int the program ID
     * @since 2005-01-01
     */
    public void setProgramId(int programId) {
        this.programId = programId;
    }

    /**
     * Gets the client demographic number.
     *
     * Returns the identifier of the client who is
     * subject to this service restriction.
     *
     * @return int the demographic number
     * @since 2005-01-01
     */
    public int getDemographicNo() {
        return demographicNo;
    }

    /**
     * Sets the client demographic number.
     *
     * Identifies which client is subject to this restriction.
     * Must reference a valid demographic record.
     *
     * @param demographicNo int the demographic number
     * @since 2005-01-01
     */
    public void setDemographicNo(int demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Gets the restriction start date.
     *
     * Returns when the restriction becomes effective.
     * Client is barred from service starting this date.
     *
     * @return Date the start date
     * @since 2005-01-01
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the restriction start date.
     *
     * Defines when the restriction takes effect.
     * Should not be null for active restrictions.
     *
     * @param startDate Date the effective date
     * @since 2005-01-01
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the restriction end date.
     *
     * Returns when the restriction is scheduled to expire.
     * Restriction is automatically lifted on this date.
     *
     * @return Date the end date
     * @since 2005-01-01
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the restriction end date.
     *
     * Defines when the restriction expires.
     * Can be modified for early termination.
     *
     * @param endDate Date the expiration date
     * @since 2005-01-01
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Checks if the restriction is currently enabled.
     *
     * Returns true if the restriction is actively enforced,
     * false if it has been disabled but not deleted.
     *
     * @return boolean the enabled status
     * @since 2005-01-01
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the enabled status of the restriction.
     *
     * Activates or deactivates the restriction without
     * deleting the record for audit purposes.
     *
     * @param enabled boolean true to enable
     * @since 2005-01-01
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the restriction comments.
     *
     * Returns the documented reason and details for
     * imposing this service restriction.
     *
     * @return String the restriction comments
     * @since 2005-01-01
     */
    public String getComments() {
        return comments;
    }

    /**
     * Sets the restriction comments.
     *
     * Documents the reason for the restriction.
     * Should include incident details and terms.
     *
     * @param comments String the restriction reason
     * @since 2005-01-01
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Gets the associated program entity.
     *
     * Returns the Program object from which the client
     * is restricted. May be lazy-loaded.
     *
     * @return Program the program entity
     * @since 2005-01-01
     */
    public Program getProgram() {
        return program;
    }

    /**
     * Sets the associated program entity.
     *
     * Links this restriction to a Program object.
     * Updates the program_id foreign key relationship.
     *
     * @param program Program the program entity
     * @since 2005-01-01
     */
    public void setProgram(Program program) {
        this.program = program;
    }

    /**
     * Gets the associated client entity.
     *
     * Returns the Demographic object representing the
     * restricted client. May be lazy-loaded.
     *
     * @return Demographic the client entity
     * @since 2005-01-01
     */
    public Demographic getClient() {
        return client;
    }

    /**
     * Sets the associated client entity.
     *
     * Links this restriction to a Demographic object.
     * Updates the demographic_no foreign key relationship.
     *
     * @param client Demographic the client entity
     * @since 2005-01-01
     */
    public void setClient(Demographic client) {
        this.client = client;
    }

    /**
     * Gets the associated provider entity.
     *
     * Returns the Provider object who created this
     * restriction. May be lazy-loaded.
     *
     * @return Provider the provider entity
     * @since 2005-01-01
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * Sets the associated provider entity.
     *
     * Links this restriction to a Provider object.
     * Updates the provider_no foreign key relationship.
     *
     * @param provider Provider the provider entity
     * @since 2005-01-01
     */
    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    /**
     * Compares this restriction with another object for equality.
     *
     * Two ProgramClientRestriction objects are considered equal
     * if they have the same ID value.
     *
     * @param o Object the object to compare
     * @return boolean true if equal, false otherwise
     * @since 2005-01-01
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProgramClientRestriction that = (ProgramClientRestriction) o;

        if (id != that.id) return false;

        return true;
    }

    /**
     * Generates a hash code for this restriction.
     *
     * Hash code is based on the restriction ID.
     * Used for hash-based collections.
     *
     * @return int the hash code
     * @since 2005-01-01
     */
    public int hashCode() {
        return (id ^ (id >>> 32));
    }

    /**
     * Gets the comment record identifier.
     *
     * Returns optional reference to extended documentation
     * stored in a separate comment table.
     *
     * @return String the comment ID
     * @since 2005-01-01
     */
    public String getCommentId() {
        return commentId;
    }

    /**
     * Sets the comment record identifier.
     *
     * Links to extended documentation if the comments
     * field is insufficient for full details.
     *
     * @param commentId String the comment record ID
     * @since 2005-01-01
     */
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    /**
     * Gets the early termination provider.
     *
     * Returns the provider who ended the restriction
     * before its scheduled expiration date.
     *
     * @return String the provider number, null if not terminated early
     * @since 2005-01-01
     */
    public String getEarlyTerminationProvider() {
        return earlyTerminationProvider;
    }

    /**
     * Sets the early termination provider.
     *
     * Records which provider authorized early lifting
     * of the restriction for audit purposes.
     *
     * @param earlyTerminationProvider String the provider number
     * @since 2005-01-01
     */
    public void setEarlyTerminationProvider(String earlyTerminationProvider) {
        this.earlyTerminationProvider = earlyTerminationProvider;
    }


}
