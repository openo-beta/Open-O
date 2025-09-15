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

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ca.openosp.openo.model.security.Secobjectname;

/**
 * Hibernate-based implementation of the SecObjectNameDao interface.
 * <p>
 * This class provides concrete implementation for managing security object names
 * using Hibernate ORM framework. It extends HibernateDaoSupport to leverage
 * Spring's Hibernate integration features including automatic session management
 * and exception translation.
 * </p>
 * <p>
 * The implementation uses HibernateTemplate for database operations, which provides
 * thread-safe access to Hibernate sessions and automatic resource cleanup. All
 * database operations are executed within Spring-managed transactions to ensure
 * data consistency.
 * </p>
 * <p>
 * This DAO is typically configured as a Spring bean and injected into service
 * classes that require security object name persistence functionality. The
 * SessionFactory is injected by Spring during bean initialization.
 * </p>
 *
 * @since 2001-08-01
 * @see SecObjectNameDao
 * @see ca.openosp.openo.model.security.Secobjectname
 * @see org.springframework.orm.hibernate5.support.HibernateDaoSupport
 */
public class SecObjectNameDaoImpl extends HibernateDaoSupport implements SecObjectNameDao {

    /**
     * {@inheritDoc}
     * <p>
     * Implementation uses Hibernate's saveOrUpdate method which intelligently determines
     * whether to perform an INSERT or UPDATE based on the entity's state. For new entities
     * (transient state), an INSERT is performed. For existing entities (persistent or
     * detached state), an UPDATE is executed.
     * </p>
     * <p>
     * The method relies on Hibernate's dirty checking mechanism to optimize updates -
     * only changed fields are included in the UPDATE statement. This improves performance
     * and reduces database load for large entities.
     * </p>
     * <p>
     * Any RuntimeException thrown by Hibernate is propagated to allow proper
     * transaction rollback by the Spring transaction manager. This ensures that
     * partial updates don't corrupt the security configuration.
     * </p>
     */
    @Override
    public void saveOrUpdate(Secobjectname t) {

        try {
            // Using HibernateTemplate's saveOrUpdate for automatic INSERT/UPDATE determination
            // This leverages Hibernate's session cache and dirty checking mechanisms
            this.getHibernateTemplate().saveOrUpdate(t);

        } catch (RuntimeException re) {
            // Re-throw to trigger transaction rollback
            // Spring's exception translation will convert this to DataAccessException
            throw re;
        }
    }
}
