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
package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.DemographicMerge;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * DAO interface for {@link DemographicMerge}.
 *
 * @since 2026-03-19
 */
public interface DemographicMergeDao extends AbstractDao<DemographicMerge> {

    /**
     * Loads the most recent MERGE event for the given merged demographic (C).
     * Used by the unmerge path to identify the primary and secondary demographics to reactivate.
     *
     * @param mergedDemographicNo Integer the demographic_no of the merged record (C)
     * @return DemographicMerge the latest MERGE event for the given demographic, or null if not found
     */
    DemographicMerge findLatestMergeEventByMergedDemographicNo(Integer mergedDemographicNo);

    /**
     * Given a small collection of demographic IDs, returns only those that are currently active
     * merge sources — i.e., appear as primary or secondary in a MERGE event that has not yet
     * been followed by an UNMERGE for the same merged record.
     *
     * @param demographicIds Collection&lt;Integer&gt; the candidate demographic IDs to check
     * @return Set&lt;Integer&gt; the subset of the input IDs that are currently merge sources
     */
    Set<Integer> findMergedSourcesAmong(Collection<Integer> demographicIds);

    /**
     * Returns the active MERGE event for the given merged record (C), or null if the record
     * is not currently a merged result (i.e., it was never merged or has since been unmerged).
     *
     * @param mergedDemographicNo Integer the demographic_no of the candidate merged record
     * @return DemographicMerge the active MERGE event, or null
     */
    DemographicMerge findActiveMergeEventForMergedRecord(Integer mergedDemographicNo);

    /**
     * Returns the active MERGE event in which the given demographic appears as a source
     * (primary or secondary), or null if this demographic is not currently a merge source.
     *
     * @param demographicNo Integer the demographic_no to check as a source
     * @return DemographicMerge the active MERGE event where this record is a source, or null
     */
    DemographicMerge findActiveMergeEventForSource(Integer demographicNo);

    /**
     * Returns the demographic_nos of all currently active merged records (C) for which the
     * given demographic was the primary source — i.e., MERGE events that have not been
     * followed by an UNMERGE for the same merged record.
     * <p>
     * Used by the lab chart view to include labs copied onto C in the primary patient's
     * chart while the merge is active, without leaking data after an unmerge.
     *
     * @param primaryDemographicNo Integer the demographic_no of the primary source (A)
     * @return List&lt;Integer&gt; the demographic_nos of active merged records, or empty list
     */
    List<Integer> findActiveMergedDemographicNosForPrimary(Integer primaryDemographicNo);
}
