//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
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
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package ca.openosp.openo.daos.security;

import ca.openosp.openo.model.security.Secobjectname;

/**
 * Data Access Object interface for managing security object names in the OpenO EMR system.
 * <p>
 * This interface defines the contract for persisting and managing security object names
 * which represent protected resources within the application. Security object names are
 * fundamental components of the role-based access control (RBAC) system, identifying
 * specific functions, modules, or features that require authorization checks.
 * </p>
 * <p>
 * Security object names work in conjunction with privileges and roles to form the complete
 * authorization framework. Each object name represents a distinct protected resource that
 * can have different access levels (read, write, delete) assigned through privileges.
 * </p>
 * <p>
 * Implementation classes should ensure thread-safety and proper transaction management
 * when persisting security object data, as these are critical for maintaining system
 * security integrity.
 * </p>
 *
 * @since 2001-08-01
 * @see ca.openosp.openo.model.security.Secobjectname
 * @see ca.openosp.openo.daos.security.SecobjprivilegeDao
 */
public interface SecObjectNameDao {
    /**
     * Persists or updates a security object name entity in the database.
     * <p>
     * This method performs an "upsert" operation - if the security object name already exists
     * in the database (based on its primary key), it will be updated with the new values.
     * If it doesn't exist, a new record will be created.
     * </p>
     * <p>
     * This operation is typically used during system configuration or when adding new
     * protected resources to the application. The method should be called within a
     * transaction context to ensure data consistency.
     * </p>
     *
     * @param t Secobjectname the security object name entity to save or update.
     *          Must not be null and should contain valid object name and description.
     * @throws IllegalArgumentException if the parameter is null
     * @throws org.hibernate.HibernateException if there is a database access error
     * @throws org.springframework.dao.DataAccessException if there is a Spring data access error
     */
    public void saveOrUpdate(Secobjectname t);
}
