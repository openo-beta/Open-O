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
 * Base exception class for program admission-related errors in the Program Management module.
 *
 * This exception serves as the parent class for all admission-related exceptions
 * in the PMmodule (Program Management module). It is thrown when general admission
 * errors occur that don't fit into more specific exception categories.
 *
 * The PMmodule manages client admissions to various healthcare and social service
 * programs. This exception hierarchy helps distinguish between different types
 * of admission failures, enabling appropriate error handling and user feedback.
 *
 * Common scenarios for this exception:
 * - Generic admission processing failures
 * - Validation errors during admission
 * - System errors preventing admission completion
 * - Data integrity issues during admission
 *
 * Subclasses provide more specific exception types:
 * - AlreadyAdmittedException: Client is already in the program
 * - ProgramFullException: Program has reached capacity
 * - ServiceRestrictionException: Client restricted from service
 * - AlreadyQueuedException: Client already in program queue
 *
 * Error handling best practices:
 * - Catch specific subclasses before this general exception
 * - Log the full exception details for debugging
 * - Provide user-friendly messages based on exception type
 * - Consider transaction rollback on admission failures
 *
 * @since 2005-01-01
 * @see AlreadyAdmittedException
 * @see ProgramFullException
 * @see ServiceRestrictionException
 */
public class AdmissionException extends Exception {

    /**
     * Constructs a new AdmissionException with no detail message.
     *
     * Use this constructor when the exception type itself provides
     * sufficient information about the error condition.
     *
     * @since 2005-01-01
     */
    public AdmissionException() {
        super();
    }

    /**
     * Constructs a new AdmissionException with the specified detail message.
     *
     * The detail message should provide specific information about why
     * the admission failed, including relevant context such as:
     * - Program name or ID
     * - Client identifier (avoid PHI in logs)
     * - Specific validation that failed
     * - Required conditions not met
     *
     * Example messages:
     * - "Admission failed: Program 123 requires completion of intake form"
     * - "Cannot admit client: Missing required health card information"
     * - "Admission validation failed: Age restriction not met"
     *
     * @param msg String the detail message explaining the admission failure
     * @since 2005-01-01
     */
    public AdmissionException(String msg) {
        super(msg);
    }
}
