//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * <p>
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
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package ca.openosp.openo.daos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import ca.openosp.openo.model.DefaultIssue;
import ca.openosp.openo.commn.dao.AbstractDaoImpl;

/**
 * Implementation of the DefaultIssueDao interface.
 *
 * This class provides the concrete implementation for managing default issues
 * in the database using JPA. It extends AbstractDaoImpl to inherit common
 * DAO functionality and implements DefaultIssueDao to fulfill the contract
 * for default issue-specific operations.
 *
 * The implementation handles persistence operations for default medical issues,
 * which are templates for commonly encountered health concerns in clinical
 * practice. These templates streamline the documentation process by providing
 * pre-configured issue definitions that can be quickly applied to patient records.
 *
 * Key features include:
 * - JPQL queries for efficient data retrieval
 * - Automatic handling of insert vs update operations
 * - Parsing and deduplication of comma-separated issue ID lists
 * - Ordering by assignment time for chronological consistency
 *
 * @since 2012-01-01
 */
public class DefaultIssueDaoImpl extends AbstractDaoImpl<DefaultIssue> implements DefaultIssueDao {

    /**
     * Constructs a new DefaultIssueDaoImpl.
     *
     * Initializes the DAO with the DefaultIssue entity class
     * for type-safe JPA operations.
     */
    public DefaultIssueDaoImpl() {
        super(DefaultIssue.class);
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves a default issue by its primary key using JPQL.
     */
    public DefaultIssue findDefaultIssue(Integer id) {
        Query query = entityManager.createQuery("select x from DefaultIssue x where x.id = ?1");
        query.setParameter(1, id);
        return getSingleResultOrNull(query);
    }

    /**
     * {@inheritDoc}
     *
     * Retrieves the most recently assigned default issue by ordering
     * results by assigned time in descending order and limiting to one result.
     */
    @SuppressWarnings("unchecked")
    public DefaultIssue getLastestDefaultIssue() {
        Query query = entityManager.createQuery("select x from DefaultIssue x order by x.assignedtime desc");
        query.setMaxResults(1);
        List<DefaultIssue> issueList = query.getResultList();
        if (issueList == null || issueList.size() == 0) {
            return null;
        }
        return issueList.get(0);
    }

    /**
     * {@inheritDoc}
     *
     * Returns all default issues ordered by assigned time in descending order,
     * showing the most recently assigned issues first.
     */
    @SuppressWarnings("unchecked")
    public List<DefaultIssue> findAll() {
        Query query = entityManager.createQuery("select x from DefaultIssue x order by x.assignedtime desc");
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     *
     * Intelligently handles both insert and update operations based on
     * the presence of an ID. New issues (null or zero ID) are persisted,
     * while existing issues are merged.
     */
    public void saveDefaultIssue(DefaultIssue issue) {
        if (issue.getId() != null && issue.getId() > 0) {
            // Update existing issue
            merge(issue);
        } else {
            // Insert new issue
            persist(issue);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Extracts and deduplicates issue IDs from comma-separated strings
     * stored in the database. Uses a Set to ensure uniqueness and filters
     * out empty strings.
     */
    @SuppressWarnings("unchecked")
    public String[] findAllDefaultIssueIds() {
        Query query = entityManager.createQuery("select x.issueIds from DefaultIssue x order by x.assignedtime");
        query.setMaxResults(1);
        List<String> issueIdsList = query.getResultList();
        if (issueIdsList.size() == 0) {
            return new String[0];
        }

        // Use Set to automatically deduplicate issue IDs
        Set<String> issueIdsSet = new HashSet<String>();
        for (String ids : issueIdsList) {
            // Split comma-separated issue IDs
            String[] idsArr = ids.split(",");
            for (String id : idsArr) {
                // Filter out empty strings
                if (id.length() > 0) {
                    issueIdsSet.add(id);
                }
            }
        }

        return issueIdsSet.toArray(new String[issueIdsSet.size()]);
    }
}
