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

/**
 * Exception thrown when attempting to apply a service restriction to a client who already has that restriction.
 *
 * This exception is thrown when the system attempts to add a service restriction
 * that is already active for a client. Service restrictions are used to manage
 * client access to programs and services based on behavioral, medical, legal,
 * or administrative requirements.
 *
 * Types of service restrictions:
 * - Behavioral restrictions (violence, threats, non-compliance)
 * - Medical restrictions (infectious disease, medical instability)
 * - Administrative restrictions (documentation issues, eligibility)
 * - Legal restrictions (court orders, bail conditions)
 * - Facility restrictions (banned from specific locations)
 * - Time-based restrictions (curfews, limited hours)
 *
 * Common scenarios:
 * - Duplicate restriction entry attempts
 * - Overlapping restriction periods
 * - Re-applying expired restrictions without proper review
 * - Multiple staff applying same restriction
 * - System receiving concurrent restriction requests
 *
 * Business rules enforced:
 * - Prevents duplicate active restrictions
 * - Maintains restriction history integrity
 * - Ensures proper restriction documentation
 * - Prevents conflicting restriction types
 *
 * Handling recommendations:
 * - Display existing restriction details
 * - Show restriction start/end dates
 * - Offer to modify existing restriction
 * - Allow adding notes to current restriction
 * - Consider extending restriction period instead
 *
 * Implementation considerations:
 * - Check both active and pending restrictions
 * - Consider restriction hierarchies
 * - Account for temporary vs permanent restrictions
 * - Validate against restriction appeal status
 *
 * Related workflows:
 * - Restriction appeal process
 * - Restriction review schedules
 * - Automatic restriction expiration
 * - Restriction notification systems
 *
 * Security and privacy:
 * - Restriction reasons may contain sensitive information
 * - Access to restriction data should be role-based
 * - Audit trail required for all restriction changes
 * - Consider privacy laws for restriction sharing
 *
 * @since 2005-01-01
 * @see ServiceRestrictionException
 */
public class ClientAlreadyRestrictedException extends Exception {
    /**
     * Constructs a new ClientAlreadyRestrictedException with no detail message.
     *
     * The exception type itself indicates that the client already has
     * the restriction, which may be sufficient for basic error handling.
     *
     * @since 2005-01-01
     */
    public ClientAlreadyRestrictedException() {
        super();
    }

    /**
     * Constructs a new ClientAlreadyRestrictedException with the specified detail message.
     *
     * The message should provide context about the existing restriction:
     * - Type of restriction already in place
     * - Restriction start date and duration
     * - Service or program affected
     * - Restriction level or severity
     *
     * Example messages:
     * - "Client has active facility ban until 2024-12-31"
     * - "Service restriction already exists for Program XYZ"
     * - "Behavioral restriction in effect since 2024-01-15"
     * - "Duplicate restriction: client already restricted from overnight services"
     *
     * @param s String the detail message about the existing restriction
     * @since 2005-01-01
     */
    public ClientAlreadyRestrictedException(String s) {
        super(s);
    }

    /**
     * Constructs a new ClientAlreadyRestrictedException with the specified detail message and cause.
     *
     * Use this constructor when the duplicate restriction detection resulted
     * from another exception, such as a database constraint violation or
     * concurrent modification exception.
     *
     * @param s String the detail message about the existing restriction
     * @param throwable Throwable the underlying cause of the exception
     * @since 2005-01-01
     */
    public ClientAlreadyRestrictedException(String s, Throwable throwable) {
        super(s, throwable);
    }

    /**
     * Constructs a new ClientAlreadyRestrictedException with the specified cause.
     *
     * The detail message is set to the cause's toString() value.
     * Use when wrapping another exception without additional context,
     * typically when the cause provides sufficient information.
     *
     * @param throwable Throwable the underlying cause of the exception
     * @since 2005-01-01
     */
    public ClientAlreadyRestrictedException(Throwable throwable) {
        super(throwable);
    }
}
