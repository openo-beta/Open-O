//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */


package ca.openosp.openo.prescript.util;

import java.util.Comparator;

import ca.openosp.openo.commn.model.Drug;

/**
 * Comparator for sorting Drug objects in a clinically meaningful order for medication lists.
 * This comparator implements a priority-based sorting system that organizes medications
 * by their clinical status and temporal relevance to patient care.
 *
 * <p>The sorting priority order is:</p>
 * <ol>
 * <li><b>Current medications</b> - Active prescriptions currently being taken</li>
 * <li><b>Expiring medications</b> - Current prescriptions that will expire within 30 days</li>
 * <li><b>Expired medications</b> - Prescriptions that have expired but may need renewal</li>
 * <li><b>Discontinued medications</b> - Medications that have been intentionally stopped</li>
 * </ol>
 *
 * <p>This ordering ensures that clinically active medications appear at the top of
 * medication lists, while historical medications are relegated to lower positions
 * but remain accessible for clinical reference.</p>
 *
 * @since 2007-04-16
 */
public class ShowAllSorter implements Comparator<Drug> {

    /**
     * Compares two Drug objects to establish their relative priority in medication lists.
     * The comparison is based on clinical status, expiration dates, and discontinuation status.
     *
     * @param a Drug the first drug to compare
     * @param b Drug the second drug to compare
     * @return int negative if a should come before b, positive if a should come after b, 0 if equal priority
     */
    public int compare(Drug a, Drug b) {

        long now = System.currentTimeMillis();
        // Define 30-day threshold for "expiring soon" classification
        long month = 1000L * 60L * 60L * 24L * 30L;

        // Priority 1: Current medications (not expired, not archived)
        if (!a.isExpired() && !a.isArchived()) {
            // Current medication 'a' has higher priority than expiring medication 'b'
            if (!b.isExpired() && b.getEndDate() != null && (b.getEndDate().getTime() - now <= month)) {
                return -1;
            }
            // Current medication 'a' has higher priority than expired/archived/discontinued 'b'
            if (b.isExpired() || b.isArchived() || b.isDiscontinued()) {
                return -1;
            }
        }

        // Priority 2: Current but will expire within 30 days
        if (!a.isExpired() && a.getEndDate() != null && (a.getEndDate().getTime() - now <= month)) {
            // Expiring medication 'a' has higher priority than expired/discontinued 'b'
            if (b.isExpired() || b.isDiscontinued()) {
                return -1;
            }
        }

        // Priority 3: Expired medications have higher priority than discontinued
        if (a.isExpired() && b.isDiscontinued()) {
            return -1;
        }

        // Reverse comparisons: when 'a' is lower priority than 'b'

        // Discontinued medications have lowest priority
        if (a.isDiscontinued() && !b.isDiscontinued()) {
            return 1;
        }

        // Expired 'a' has lower priority than current/expiring 'b'
        if (a.isExpired()) {
            if (!b.isExpired() && b.getEndDate() != null && (b.getEndDate().getTime() - now <= month)) {
                return 1;
            }
            if (!b.isExpired() && !b.isArchived()) {
                return 1;
            }
        }

        // Expiring 'a' has lower priority than current 'b'
        if (!a.isExpired() && a.getEndDate() != null && (a.getEndDate().getTime() - now <= month)) {
            if (!b.isExpired() && !b.isArchived()) {
                return 1;
            }
        }

        // Equal priority - maintain existing order
        return 0;
    }
}
