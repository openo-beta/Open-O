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
 * Exception thrown when attempting to add a client to a program queue when they are already queued.
 *
 * This exception is thrown when a client is already on the waiting list or queue
 * for a program and another queue registration is attempted. It maintains the
 * integrity of program waitlists and prevents duplicate queue entries.
 *
 * Program queues are used when:
 * - Programs are at full capacity
 * - Clients are waiting for bed availability
 * - Services require pre-screening or assessment
 * - Programs have intake schedules or cohort starts
 *
 * Typical scenarios for this exception:
 * - Client re-applies while already on waitlist
 * - Duplicate queue registration from multiple sources
 * - Staff accidentally re-queues an already waiting client
 * - System receives concurrent queue requests
 *
 * Queue management business rules:
 * - One queue position per client per program
 * - Maintains fair waitlist ordering
 * - Prevents queue position manipulation
 * - Ensures accurate waitlist counts
 *
 * Handling recommendations:
 * - Display current queue position to user
 * - Show estimated wait time if available
 * - Offer to update queue priority if applicable
 * - Allow viewing of queue status instead
 *
 * Implementation considerations:
 * - Queue position should be preserved
 * - May have different queue types (urgent, regular)
 * - Consider queue expiration dates
 * - Account for multiple program queues
 *
 * Related workflows:
 * - Queue to admission transition
 * - Queue priority management
 * - Waitlist notification systems
 * - Queue removal/cancellation
 *
 * @since 2005-01-01
 * @see AdmissionException
 * @see AlreadyAdmittedException
 */
public class AlreadyQueuedException extends Exception {

    /**
     * Constructs a new AlreadyQueuedException with no detail message.
     *
     * The exception type itself indicates that the client is already
     * in the queue, which may be sufficient for basic error handling.
     *
     * @since 2005-01-01
     */
    public AlreadyQueuedException() {
        super();
    }

    /**
     * Constructs a new AlreadyQueuedException with the specified detail message.
     *
     * The message should provide context about the existing queue entry:
     * - Program name or identifier
     * - Queue position if available
     * - Date added to queue
     * - Queue type or priority
     *
     * Example messages:
     * - "Client already queued for Program ABC at position 5"
     * - "Existing waitlist entry found from 2024-01-10"
     * - "Client is on urgent queue for this service"
     *
     * @param message String the detail message about the existing queue entry
     * @since 2005-01-01
     */
    public AlreadyQueuedException(String message) {
        super(message);
    }

    /**
     * Constructs a new AlreadyQueuedException with the specified detail message and cause.
     *
     * Use this constructor when the duplicate queue detection resulted
     * from another exception, such as a database constraint violation.
     *
     * @param message String the detail message about the existing queue entry
     * @param cause Throwable the underlying cause of the exception
     * @since 2005-01-01
     */
    public AlreadyQueuedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AlreadyQueuedException with the specified cause.
     *
     * The detail message is set to the cause's toString() value.
     * Use when wrapping another exception without additional context.
     *
     * @param cause Throwable the underlying cause of the exception
     * @since 2005-01-01
     */
    public AlreadyQueuedException(Throwable cause) {
        super(cause);
    }
}
