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

import ca.openosp.openo.commn.model.DemographicMergeEvent;

/**
 * DAO interface for {@link DemographicMergeEvent}.
 *
 * @since 2026-03-19
 */
public interface DemographicMergeEventDao extends AbstractDao<DemographicMergeEvent> {

    /**
     * Loads the most recent MERGE event for the given merged demographic (C).
     * Used by the unmerge path to identify the primary and secondary demographics to reactivate.
     *
     * @param mergedDemographicNo Integer the demographic_no of the merged record (C)
     * @return DemographicMergeEvent the latest MERGE event for the given demographic, or null if not found
     */
    DemographicMergeEvent findLatestMergeEventByMergedDemographicNo(Integer mergedDemographicNo);
}
