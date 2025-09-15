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

package ca.openosp.openo.daos.security;

import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ca.openosp.openo.model.security.Secrole;

/**
 * Hibernate-based implementation of the SecroleDao interface.
 * <p>
 * Manages persistence of security role definitions using HibernateTemplate.
 * All queries use HQL for database independence and leverage Hibernate's
 * caching mechanisms for performance optimization.
 * </p>
 *
 * @since 2005-01-01
 * @see SecroleDao
 * @see ca.openosp.openo.model.security.Secrole
 */
public class SecroleDaoImpl extends HibernateDaoSupport implements SecroleDao {

    /** Logger for debugging and audit */
    private Logger logger = MiscUtils.getLogger();

    /**
     * {@inheritDoc}
     * Retrieves all roles ordered alphabetically by name for consistent display.
     */
    @Override
    public List<Secrole> getRoles() {
        @SuppressWarnings("unchecked")
        List<Secrole> results = (List<Secrole>) this.getHibernateTemplate().find("from Secrole r order by roleName");

        logger.debug("getRoles: # of results=" + results.size());

        return results;
    }

    /**
     * {@inheritDoc}
     * Uses Hibernate's get() method for efficient retrieval by primary key.
     */
    @Override
    public Secrole getRole(Integer id) {
        if (id == null || id.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        // Convert Integer to Long for entity ID type
        Secrole result = this.getHibernateTemplate().get(Secrole.class, Long.valueOf(id));

        logger.debug("getRole: id=" + id + ",found=" + (result != null));

        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * WARNING: Uses string concatenation in HQL - potential SQL injection risk.
     * Should be refactored to use parameterized queries.
     * </p>
     */
    @Override
    public Secrole getRoleByName(String roleName) {
        Secrole result = null;
        if (roleName == null || roleName.length() <= 0) {
            throw new IllegalArgumentException();
        }

        // WARNING: String concatenation - SQL injection risk
        List lst = this.getHibernateTemplate().find("from Secrole r where r.roleName='" + roleName + "'");
        if (lst != null && lst.size() > 0)
            result = (Secrole) lst.get(0);

        logger.debug("getRoleByName: roleName=" + roleName + ",found=" + (result != null));

        return result;
    }

    /**
     * {@inheritDoc}
     * Filters for system-defined roles using userDefined flag.
     */
    @Override
    public List getDefaultRoles() {
        return this.getHibernateTemplate().find("from Secrole r where r.userDefined=0");
    }

    /**
     * {@inheritDoc}
     * Uses saveOrUpdate for intelligent INSERT/UPDATE determination.
     */
    @Override
    public void save(Secrole secrole) {
        if (secrole == null) {
            throw new IllegalArgumentException();
        }

        getHibernateTemplate().saveOrUpdate(secrole);

    }

}
