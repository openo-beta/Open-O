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

package ca.openosp.openo.match;

/**
 * Exception thrown when healthcare patient-provider matching operations fail.
 *
 * <p>This exception is used throughout the OpenO EMR matching system to indicate
 * failures in patient-provider matching operations. It provides specific error
 * messaging for healthcare workflow issues that may occur during the matching process.</p>
 *
 * <p>Common Healthcare Matching Scenarios That Trigger This Exception:
 * <ul>
 *   <li>Invalid or unsupported event types in match processing</li>
 *   <li>Data integrity issues with patient or provider information</li>
 *   <li>System errors during match calculation or persistence</li>
 *   <li>Configuration problems with matching templates or criteria</li>
 * </ul>
 * </p>
 *
 * <p>Healthcare Context: This exception helps maintain system reliability
 * by providing clear error handling for critical patient-provider matching
 * operations, ensuring healthcare workflows can handle and recover from
 * matching system failures appropriately.</p>
 *
 * @see MatchManager
 * @see IMatchManager
 * @since 2005-12-16
 */
public class MatchManagerException extends Exception {

    /**
     * Constructs a new MatchManagerException with the specified error message.
     *
     * <p>The error message should clearly describe the healthcare matching
     * operation that failed and provide context for troubleshooting the issue.</p>
     *
     * @param message String descriptive error message explaining the matching failure
     */
    public MatchManagerException(String message) {
        super(message);
    }
}
