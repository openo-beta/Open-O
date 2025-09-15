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
 * Exception thrown when attempting to admit a client to a program that has reached its capacity.
 *
 * This exception is thrown when a program cannot accept new admissions because
 * it has reached its maximum capacity limit. Programs in healthcare and social
 * services often have capacity constraints based on funding, staffing, physical
 * space, or regulatory requirements.
 *
 * Types of capacity limits:
 * - Bed capacity (residential programs, shelters, hospitals)
 * - Staff-to-client ratios (mandated by regulations)
 * - Funding limits (government-funded slots)
 * - Physical space constraints (room capacity, fire codes)
 * - Program-specific limits (group therapy size, class size)
 * - Service intensity limits (case management caseloads)
 *
 * Common scenarios:
 * - Shelter beds are fully occupied
 * - Treatment program cohort is complete
 * - Counselor caseload at maximum
 * - Residential facility at licensed capacity
 * - Day program reached enrollment limit
 * - Emergency overflow capacity exceeded
 *
 * Business implications:
 * - Triggers waitlist/queue management
 * - May initiate overflow protocols
 * - Requires capacity monitoring
 * - Affects funding and reporting
 * - Impacts service delivery metrics
 *
 * Handling recommendations:
 * - Offer to add client to waitlist
 * - Check for upcoming discharges
 * - Suggest alternative programs
 * - Display current capacity status
 * - Provide estimated wait times
 * - Consider emergency/priority override
 *
 * Capacity management features:
 * - Real-time capacity tracking
 * - Automatic waitlist progression
 * - Capacity forecasting
 * - Overflow routing
 * - Emergency capacity protocols
 * - Reserved beds for specific populations
 *
 * Implementation considerations:
 * - Check both regular and emergency capacity
 * - Consider temporary vs permanent beds
 * - Account for gender-specific capacity
 * - Validate against funding restrictions
 * - Check for pending discharges
 * - Consider virtual/remote capacity
 *
 * Related systems:
 * - Waitlist management
 * - Bed management
 * - Discharge planning
 * - Transfer coordination
 * - Capacity reporting
 *
 * @since 2005-01-01
 * @see AdmissionException
 * @see AlreadyQueuedException
 */
public class ProgramFullException extends Exception {
    /**
     * Constructs a new ProgramFullException with no detail message.
     *
     * The exception type itself indicates that the program is at capacity,
     * which may be sufficient for basic error handling.
     *
     * @since 2005-01-01
     */
    public ProgramFullException() {
        super();
    }

    /**
     * Constructs a new ProgramFullException with the specified detail message.
     *
     * The message should provide specific information about the capacity issue:
     * - Program name and current capacity
     * - Type of capacity limit reached
     * - Number of people on waitlist
     * - Expected availability timeframe
     *
     * Example messages:
     * - "Shelter at capacity: 50/50 beds occupied"
     * - "Treatment program full: 12/12 participants enrolled"
     * - "Cannot admit: Staff-to-client ratio limit reached (1:8)"
     * - "Program XYZ at capacity with 5 on waitlist"
     * - "Residential facility at licensed maximum of 30 residents"
     *
     * @param s String the detail message describing the capacity situation
     * @since 2005-01-01
     */
    public ProgramFullException(String s) {
        super(s);
    }

    /**
     * Constructs a new ProgramFullException with the specified detail message and cause.
     *
     * Use this constructor when the capacity check resulted from another
     * exception, such as a database error while checking capacity or
     * a calculation error in capacity determination.
     *
     * @param s String the detail message describing the capacity situation
     * @param throwable Throwable the underlying cause of the exception
     * @since 2005-01-01
     */
    public ProgramFullException(String s, Throwable throwable) {
        super(s, throwable);
    }

    /**
     * Constructs a new ProgramFullException with the specified cause.
     *
     * The detail message is set to the cause's toString() value.
     * Use when wrapping another exception without additional context,
     * typically when the underlying exception provides sufficient information.
     *
     * @param throwable Throwable the underlying cause of the exception
     * @since 2005-01-01
     */
    public ProgramFullException(Throwable throwable) {
        super(throwable);
    }
}
