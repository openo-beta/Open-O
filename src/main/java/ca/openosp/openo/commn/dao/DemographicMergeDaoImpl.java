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
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JPA implementation of {@link DemographicMergeDao}.
 *
 * @since 2026-03-19
 */
@Repository
public class DemographicMergeDaoImpl extends AbstractDaoImpl<DemographicMerge> implements DemographicMergeDao {

    public DemographicMergeDaoImpl() {
        super(DemographicMerge.class);
    }

    @Override
    public DemographicMerge findLatestMergeEventByMergedDemographicNo(Integer mergedDemographicNo) {
        Query q = entityManager.createQuery(
                "select e from DemographicMerge e " +
                "where e.mergedDemographicNo = ?1 and e.eventType = ?2 " +
                "order by e.eventDate desc");
        q.setParameter(1, mergedDemographicNo);
        q.setParameter(2, DemographicMerge.EventType.MERGE);
        q.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<DemographicMerge> results = q.getResultList();

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public Set<Integer> findMergedSourcesAmong(Collection<Integer> demographicIds) {
        if (demographicIds == null || demographicIds.isEmpty()) return Collections.emptySet();

        // Primary check — indexed on primary_demographic_no
        Query primaryQ = entityManager.createQuery(
                "select e.primaryDemographicNo from DemographicMerge e where e.primaryDemographicNo in :ids and e.eventType = :mergeType and e.mergedDemographicNo not in (select u.mergedDemographicNo from DemographicMerge u where u.eventType = :unmergeType)");
        primaryQ.setParameter("ids", demographicIds);
        primaryQ.setParameter("mergeType", DemographicMerge.EventType.MERGE);
        primaryQ.setParameter("unmergeType", DemographicMerge.EventType.UNMERGE);

        @SuppressWarnings("unchecked")
        Set<Integer> result = new HashSet<>(primaryQ.getResultList());

        // Secondary check — secondary_demographic_no is comma-separated so use FIND_IN_SET per remaining ID
        for (Integer id : demographicIds) {
            if (result.contains(id)) continue;
            Query secQ = entityManager.createNativeQuery("select count(*) from demographic_merged_event where event_type = 'MERGE' and find_in_set(:id, replace(secondary_demographic_no, ' ', '')) > 0 and merged_demographic_no not in (select merged_demographic_no from demographic_merged_event where event_type = 'UNMERGE')");
            secQ.setParameter("id", id);
            Number count = (Number) secQ.getSingleResult();
            if (count.intValue() > 0) result.add(id);
        }

        return result;
    }

    @Override
    public DemographicMerge findActiveMergeEventForMergedRecord(Integer mergedDemographicNo) {
        Query q = entityManager.createQuery(
                "select e from DemographicMerge e where e.mergedDemographicNo = :demoNo and e.eventType = :mergeType and e.mergedDemographicNo not in (select u.mergedDemographicNo from DemographicMerge u where u.eventType = :unmergeType) order by e.eventDate desc");
        q.setParameter("demoNo", mergedDemographicNo);
        q.setParameter("mergeType", DemographicMerge.EventType.MERGE);
        q.setParameter("unmergeType", DemographicMerge.EventType.UNMERGE);
        q.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<DemographicMerge> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public DemographicMerge findActiveMergeEventForSource(Integer demographicNo) {
        // Primary check — indexed on primary_demographic_no
        Query primaryQ = entityManager.createQuery(
                "select e from DemographicMerge e where e.primaryDemographicNo = :demoNo and e.eventType = :mergeType and e.mergedDemographicNo not in (select u.mergedDemographicNo from DemographicMerge u where u.eventType = :unmergeType)");
        primaryQ.setParameter("demoNo", demographicNo);
        primaryQ.setParameter("mergeType", DemographicMerge.EventType.MERGE);
        primaryQ.setParameter("unmergeType", DemographicMerge.EventType.UNMERGE);
        primaryQ.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<DemographicMerge> primaryResults = primaryQ.getResultList();
        if (!primaryResults.isEmpty()) return primaryResults.get(0);

        // Secondary check — secondary_demographic_no is comma-separated so use FIND_IN_SET
        Query secQ = entityManager.createNativeQuery("select id from demographic_merged_event where event_type = 'MERGE' and find_in_set(:id, replace(secondary_demographic_no, ' ', '')) > 0 and merged_demographic_no not in (select merged_demographic_no from demographic_merged_event where event_type = 'UNMERGE') limit 1");
        secQ.setParameter("id", demographicNo);

        @SuppressWarnings("unchecked")
        List<Number> secResults = secQ.getResultList();
        if (!secResults.isEmpty()) {
            return entityManager.find(DemographicMerge.class, secResults.get(0).intValue());
        }
        return null;
    }
}
