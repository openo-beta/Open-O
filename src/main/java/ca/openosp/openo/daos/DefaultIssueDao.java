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

import java.util.List;

import ca.openosp.openo.model.DefaultIssue;
import ca.openosp.openo.commn.dao.AbstractDao;

/**
 * Data Access Object interface for default issue management.
 *
 * This interface defines operations for managing default issues in the system.
 * Default issues are pre-configured medical issues or conditions that can be
 * quickly assigned to patients during clinical encounters. They serve as
 * templates for commonly encountered health concerns, streamlining documentation
 * and ensuring consistency in issue tracking across the EMR system.
 *
 * The DAO provides standard CRUD operations along with specialized methods
 * for retrieving the latest default issue and finding all issue identifiers.
 * These default issues are typically used in case management, encounter notes,
 * and problem lists to standardize medical documentation.
 *
 * @since 2012-01-01
 */
public interface DefaultIssueDao extends AbstractDao<DefaultIssue> {

    /**
     * Finds a default issue by its unique identifier.
     *
     * @param id Integer the unique identifier of the default issue
     * @return DefaultIssue the default issue entity, or null if not found
     */
    public DefaultIssue findDefaultIssue(Integer id);

    /**
     * Retrieves the most recently created default issue.
     *
     * This method is typically used to get the latest template or
     * to verify the most recent addition to the default issues list.
     *
     * @return DefaultIssue the most recently created default issue, or null if none exist
     */
    public DefaultIssue getLastestDefaultIssue();

    /**
     * Retrieves all default issues in the system.
     *
     * Returns a complete list of all configured default issues,
     * typically used for populating selection lists in the UI.
     *
     * @return List<DefaultIssue> list of all default issues, empty list if none exist
     */
    public List<DefaultIssue> findAll();

    /**
     * Saves or updates a default issue.
     *
     * Persists a new default issue or updates an existing one.
     * The method handles both insert and update operations based
     * on whether the issue has an existing identifier.
     *
     * @param issue DefaultIssue the default issue to save or update
     */
    public void saveDefaultIssue(DefaultIssue issue);

    /**
     * Retrieves all default issue identifiers.
     *
     * Returns an array of all default issue IDs as strings,
     * useful for quick lookups and validation without loading
     * full issue entities.
     *
     * @return String[] array of default issue identifiers, empty array if none exist
     */
    public String[] findAllDefaultIssueIds();
}
