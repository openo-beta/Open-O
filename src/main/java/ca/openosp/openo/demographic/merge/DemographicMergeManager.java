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
package ca.openosp.openo.demographic.merge;

import ca.openosp.openo.utility.LoggedInInfo;

import java.util.List;

/**
 * Orchestrates the demographic merge and unmerge operations.
 * <p>
 * A merge combines two or more patient records into a single new record (C):
 * all clinical data from the primary (A) and each secondary (B, D, ...) is
 * copied into C, then A and all secondaries are marked {@code MERGED} so they
 * no longer appear in active patient search results.
 * <p>
 * An unmerge reverses this: A and all secondaries are restored to {@code AC},
 * C is deactivated, and a new event row is written to the audit log.
 *
 * @since 2026-03-25
 */
public interface DemographicMergeManager {

    /**
     * Merges one or more secondary patients into a new patient record cloned
     * from the primary. All clinical data is copied; source records are marked
     * {@code MERGED} and left untouched otherwise.
     *
     * @param loggedInInfo           LoggedInInfo the authenticated provider performing the merge
     * @param primaryDemographicNo   Integer the demographic_no of the primary patient (A);
     *                               identity fields are cloned from this record to create C
     * @param secondaryDemographicNos List&lt;Integer&gt; one or more demographic_nos of secondary
     *                               patients whose clinical data is also copied into C
     * @throws IllegalArgumentException if secondaryDemographicNos is null or empty, or if
     *                                  any referenced demographic does not exist
     * @throws IllegalStateException    if the primary or any secondary is already {@code MERGED}
     */
    void merge(LoggedInInfo loggedInInfo, Integer primaryDemographicNo, List<Integer> secondaryDemographicNos);

    /**
     * Reverses a previous merge: reactivates the primary and all secondaries (back to
     * {@code AC}), deactivates the merged record C (set to {@code IN}), and writes
     * a new {@code UNMERGE} event row.
     *
     * @param loggedInInfo        LoggedInInfo the authenticated provider performing the unmerge
     * @param mergedDemographicNo Integer the demographic_no of the merged record (C) to reverse
     * @throws IllegalStateException if no MERGE event exists for the given demographic
     */
    void unmerge(LoggedInInfo loggedInInfo, Integer mergedDemographicNo);
}
