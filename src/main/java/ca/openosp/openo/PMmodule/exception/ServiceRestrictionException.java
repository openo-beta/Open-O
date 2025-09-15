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

package ca.openosp.openo.PMmodule.exception;

import ca.openosp.openo.PMmodule.model.ProgramClientRestriction;

/**
 * Exception thrown when a client is restricted from accessing a service or program.
 *
 * This exception is thrown when a client attempts to access a program or service
 * from which they have an active restriction. Unlike other admission exceptions,
 * this one carries the actual ProgramClientRestriction object, providing detailed
 * information about the nature and scope of the restriction.
 *
 * Service restrictions are enforced for:
 * - Client safety (medical conditions, vulnerability)
 * - Staff safety (violence history, threats)
 * - Other client safety (predatory behavior, conflicts)
 * - Legal requirements (court orders, bail conditions)
 * - Administrative reasons (non-payment, documentation)
 * - Behavioral issues (property damage, substance use)
 *
 * Restriction characteristics:
 * - Can be facility-wide or program-specific
 * - May be temporary or permanent
 * - Can have conditions for lifting
 * - May require appeal process
 * - Often have review dates
 * - May allow exceptions for emergencies
 *
 * Common restriction scenarios:
 * - Banned from facility due to violence
 * - Restricted from overnight services
 * - Cannot access when intoxicated
 * - Barred pending investigation
 * - Restricted to specific programs only
 * - Limited to daytime services
 *
 * The included ProgramClientRestriction object contains:
 * - Client and program identifiers
 * - Restriction type and severity
 * - Start and end dates
 * - Reason for restriction
 * - Conditions for removal
 * - Review requirements
 * - Staff who applied restriction
 *
 * Handling recommendations:
 * - Display restriction details clearly
 * - Explain appeal process if available
 * - Suggest alternative services
 * - Document access attempt
 * - Notify appropriate staff
 * - Consider emergency overrides
 *
 * Legal and ethical considerations:
 * - Must balance safety with duty of care
 * - Restrictions must be proportionate
 * - Regular review required
 * - Appeal process must be available
 * - Cannot discriminate illegally
 * - Emergency services may override
 *
 * Implementation notes:
 * - Check restrictions before any service access
 * - Consider restriction inheritance (facility → program)
 * - Account for restriction suspensions
 * - Validate restriction dates
 * - Log all restriction enforcements
 *
 * @since 2005-01-01
 * @see ClientAlreadyRestrictedException
 * @see ProgramClientRestriction
 */
public class ServiceRestrictionException extends Exception {

    /**
     * The actual restriction object that prevented service access.
     * Contains detailed information about the restriction including
     * type, duration, reason, and conditions.
     */
    private ProgramClientRestriction restriction;

    /**
     * Constructs a new ServiceRestrictionException with the restriction details.
     *
     * Creates an exception with no message but includes the restriction
     * object for detailed handling. The restriction object provides all
     * necessary context about why access was denied.
     *
     * @param restriction ProgramClientRestriction the active restriction preventing access
     * @since 2005-01-01
     */
    public ServiceRestrictionException(ProgramClientRestriction restriction) {
        super();
        this.restriction = restriction;
    }

    /**
     * Constructs a new ServiceRestrictionException with a message and restriction details.
     *
     * Provides both a human-readable message and the restriction object.
     * The message can provide context beyond what's in the restriction.
     *
     * Example messages:
     * - "Access denied: Active safety restriction until 2024-12-31"
     * - "Service restricted: Client banned from overnight programs"
     * - "Cannot admit: Facility-wide restriction in effect"
     * - "Restriction enforced: Review required before re-admission"
     *
     * @param s String the detail message explaining the restriction enforcement
     * @param restriction ProgramClientRestriction the active restriction preventing access
     * @since 2005-01-01
     */
    public ServiceRestrictionException(String s, ProgramClientRestriction restriction) {
        super(s);
        this.restriction = restriction;
    }

    /**
     * Constructs a new ServiceRestrictionException with message, cause, and restriction.
     *
     * Use when the restriction check encountered an error, such as a
     * database exception while validating restrictions. Provides complete
     * context including the error that occurred and the restriction found.
     *
     * @param s String the detail message explaining the restriction enforcement
     * @param throwable Throwable the underlying cause of the exception
     * @param restriction ProgramClientRestriction the active restriction preventing access
     * @since 2005-01-01
     */
    public ServiceRestrictionException(String s, Throwable throwable, ProgramClientRestriction restriction) {
        super(s, throwable);
        this.restriction = restriction;
    }

    /**
     * Constructs a new ServiceRestrictionException with cause and restriction.
     *
     * Use when wrapping another exception that occurred during restriction
     * checking. The cause's message becomes the exception message.
     *
     * @param throwable Throwable the underlying cause of the exception
     * @param restriction ProgramClientRestriction the active restriction preventing access
     * @since 2005-01-01
     */
    public ServiceRestrictionException(Throwable throwable, ProgramClientRestriction restriction) {
        super(throwable);
        this.restriction = restriction;
    }

    /**
     * Gets the restriction that prevented service access.
     *
     * Returns the ProgramClientRestriction object containing full details
     * about the restriction including:
     * - Restriction type and severity level
     * - Start and end dates
     * - Reason for restriction
     * - Program or facility affected
     * - Conditions for removal
     * - Review requirements
     *
     * This information can be used to:
     * - Display specific restriction details to staff
     * - Determine if override is possible
     * - Check if restriction has expired
     * - Initiate appeal process
     * - Log access attempts
     * - Suggest alternatives
     *
     * @return ProgramClientRestriction the restriction that blocked access
     * @since 2005-01-01
     */
    public ProgramClientRestriction getRestriction() {
        return restriction;
    }
}
