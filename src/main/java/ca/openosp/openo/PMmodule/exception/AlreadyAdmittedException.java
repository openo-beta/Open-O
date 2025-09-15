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
 * Exception thrown when attempting to admit a client who is already admitted to a program.
 *
 * This exception is thrown when the system detects that a client is already
 * actively enrolled in a program and a duplicate admission is attempted.
 * This prevents duplicate records and maintains data integrity in the
 * Program Management module.
 *
 * Typical scenarios:
 * - Client attempts to register for a program they're already in
 * - Staff accidentally tries to re-admit an active client
 * - System receives duplicate admission requests
 * - Client re-registration without proper discharge
 *
 * The exception helps enforce business rules:
 * - One active admission per client per program
 * - Prevents duplicate service allocations
 * - Maintains accurate program capacity counts
 * - Ensures proper admission/discharge workflow
 *
 * Handling recommendations:
 * - Check if client needs discharge before re-admission
 * - Verify if this is a transfer between programs
 * - Display current admission status to user
 * - Offer to view/edit existing admission instead
 *
 * Implementation notes:
 * - Check should occur before any database transactions
 * - Consider concurrent admission attempts
 * - May need to check admission date ranges
 * - Should account for different program types
 *
 * @since 2005-01-01
 * @see AdmissionException
 */
public class AlreadyAdmittedException extends Exception {

    /**
     * Constructs a new AlreadyAdmittedException with no detail message.
     *
     * The exception type itself indicates that the client is already
     * admitted, which may be sufficient information for the handler.
     *
     * @since 2005-01-01
     */
    public AlreadyAdmittedException() {
        super();
    }

    /**
     * Constructs a new AlreadyAdmittedException with the specified detail message.
     *
     * The message should provide context about the existing admission:
     * - Program name or identifier
     * - Admission date if relevant
     * - Current admission status
     *
     * Example messages:
     * - "Client already admitted to Program XYZ on 2024-01-15"
     * - "Active admission found for this program"
     * - "Client must be discharged before re-admission"
     *
     * @param message String the detail message about the existing admission
     * @since 2005-01-01
     */
    public AlreadyAdmittedException(String message) {
        super(message);
    }

    /**
     * Constructs a new AlreadyAdmittedException with the specified detail message and cause.
     *
     * Use this constructor when the duplicate admission detection resulted
     * from another exception, such as a database constraint violation.
     *
     * @param message String the detail message about the existing admission
     * @param cause Throwable the underlying cause of the exception
     * @since 2005-01-01
     */
    public AlreadyAdmittedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AlreadyAdmittedException with the specified cause.
     *
     * The detail message is set to the cause's toString() value.
     * Use when wrapping another exception without additional context.
     *
     * @param cause Throwable the underlying cause of the exception
     * @since 2005-01-01
     */
    public AlreadyAdmittedException(Throwable cause) {
        super(cause);
    }
}
