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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;

import ca.openosp.openo.model.security.Secobjprivilege;

/**
 * Hibernate-based implementation of the SecobjprivilegeDao interface.
 * <p>
 * This class manages the persistence of security object privileges, which define
 * the access rights that roles have on specific security objects. It handles
 * complex queries involving multiple tables and provides batch operations for
 * efficient privilege management.
 * </p>
 * <p>
 * The implementation uses both HibernateTemplate for simple operations and
 * direct Session access for complex queries and batch updates. Session management
 * is handled through Spring's SessionFactory injection.
 * </p>
 * <p>
 * Special attention is given to preventing duplicate privilege assignments through
 * the update-then-insert pattern in saveAll method. This ensures data integrity
 * in the security configuration.
 * </p>
 *
 * @since 2005-01-01
 * @see SecobjprivilegeDao
 * @see ca.openosp.openo.model.security.Secobjprivilege
 */
public class SecobjprivilegeDaoImpl extends HibernateDaoSupport implements SecobjprivilegeDao {

    /** Logger for debugging and error tracking */
    private Logger logger = MiscUtils.getLogger();
    /** Direct SessionFactory reference for complex queries */
    public SessionFactory sessionFactory;

    /**
     * Sets the SessionFactory for this DAO.
     * <p>
     * Uses @Autowired to receive Spring-injected SessionFactory.
     * Overrides the parent class setter to maintain local reference.
     * </p>
     *
     * @param sessionFactory SessionFactory the Hibernate session factory
     */
    @Autowired
    public void setSessionFactoryOverride(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses saveOrUpdate to handle both new and existing privilege assignments.
     * Logs the operation for audit purposes when debug logging is enabled.
     * </p>
     */
    @Override
    public void save(Secobjprivilege secobjprivilege) {
        if (secobjprivilege == null) {
            throw new IllegalArgumentException();
        }

        // SaveOrUpdate intelligently chooses INSERT or UPDATE
        getHibernateTemplate().saveOrUpdate(secobjprivilege);

        if (logger.isDebugEnabled()) {
            // Log role and object for security audit trail
            logger.debug("SecobjprivilegeDao : save: " + secobjprivilege.getRoleusergroup() + ":"
                    + secobjprivilege.getObjectname_desc());
        }

    }

    /**
     * {@inheritDoc}
     * <p>
     * Implements an update-then-insert pattern to prevent duplicates.
     * Each privilege is first attempted as an update; if no rows are affected
     * (indicating the record doesn't exist), it's inserted as new.
     * </p>
     */
    @Override
    public void saveAll(List list) {
        logger.debug("saving ALL Secobjprivilege instances");
        try {
            for (int i = 0; i < list.size(); i++) {
                Secobjprivilege obj = (Secobjprivilege) list.get(i);

                // Try update first to avoid duplicates
                int rowcount = update(obj);

                if (rowcount <= 0) {
                    // No existing record found, insert new
                    save(obj);
                }

            }

            logger.debug("save ALL successful");
        } catch (RuntimeException re) {
            logger.error("save ALL failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Updates the provider number for an existing privilege assignment.
     * Uses direct HQL with string concatenation - should be refactored to use
     * parameterized queries for better security.
     * </p>
     * <p>
     * WARNING: This method uses string concatenation for query building which
     * could be vulnerable to SQL injection if input is not properly validated.
     * Consider refactoring to use parameterized queries.
     * </p>
     */
    @Override
    public int update(Secobjprivilege instance) {
        logger.debug("update Secobjprivilege instance");
        Session session = sessionFactory.getCurrentSession();
        try {
            // WARNING: String concatenation in query - potential SQL injection risk
            // This should be refactored to use parameterized queries
            String queryString = "update Secobjprivilege as model set model.providerNo ='" + instance.getProviderNo()
                    + "'"
                    + " where model.objectname_code ='" + instance.getObjectname_code() + "'"
                    + " and model.privilege_code ='" + instance.getPrivilege_code() + "'"
                    + " and model.roleusergroup ='" + instance.getRoleusergroup() + "'";

            Query queryObject = session.createQuery(queryString);

            return queryObject.executeUpdate();

        } catch (RuntimeException re) {
            logger.error("Update failed", re);
            throw re;
        } finally {
            // Manual session management required here
            session.close();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Performs bulk deletion of all privileges for a role using HQL.
     * This is more efficient than deleting records individually.
     * </p>
     */
    @Override
    public int deleteByRoleName(String roleName) {
        logger.debug("deleting Secobjprivilege by roleName");
        try {
            // Bulk delete using parameterized HQL for security
            return getHibernateTemplate().bulkUpdate("delete Secobjprivilege as model where model.roleusergroup =?0",
                    roleName);

        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Deletes a single privilege assignment from the database.
     * </p>
     */
    @Override
    public void delete(Secobjprivilege persistentInstance) {
        logger.debug("deleting Secobjprivilege instance");
        try {
            getHibernateTemplate().delete(persistentInstance);
            logger.debug("delete successful");
        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Queries the Secobjectname table to retrieve the human-readable description
     * of a security object/function code.
     * </p>
     * <p>
     * WARNING: Uses string concatenation in query - potential SQL injection risk.
     * Should be refactored to use parameterized queries.
     * </p>
     */
    @Override
    public String getFunctionDesc(String function_code) {
        try {
            // WARNING: String concatenation - SQL injection risk
            String queryString = "select description from Secobjectname obj where obj.objectname='" + function_code
                    + "'";

            List lst = getHibernateTemplate().find(queryString);
            if (lst.size() > 0 && lst.get(0) != null)
                return lst.get(0).toString();
            else
                return "";
        } catch (RuntimeException re) {
            logger.error("find by property name failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Queries the Secprivilege table to retrieve the description of an access
     * type/privilege level (e.g., 'r' for read, 'w' for write).
     * </p>
     * <p>
     * WARNING: Uses string concatenation in query - potential SQL injection risk.
     * Should be refactored to use parameterized queries.
     * </p>
     */
    @Override
    public String getAccessDesc(String accessType_code) {
        try {
            // WARNING: String concatenation - SQL injection risk
            String queryString = "select description from Secprivilege obj where obj.privilege='" + accessType_code
                    + "'";

            List lst = getHibernateTemplate().find(queryString);
            if (lst.size() > 0 && lst.get(0) != null)
                return lst.get(0).toString();
            else
                return "";
        } catch (RuntimeException re) {
            logger.error("find by property name failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Retrieves all privilege assignments for a role by delegating to
     * findByProperty with the roleusergroup field.
     * </p>
     */
    @Override
    public List getFunctions(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException();
        }

        // Delegate to property-based search
        List result = findByProperty("roleusergroup", roleName);
        if (logger.isDebugEnabled()) {
            logger.debug("SecobjprivilegeDao : getFunctions: ");
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Generic property search with results ordered by object name code.
     * Uses parameterized queries for the value but concatenates property name,
     * which should only come from internal code, not user input.
     * </p>
     */
    @Override
    public List findByProperty(String propertyName, Object value) {
        logger.debug("finding Secobjprivilege instance with property: " + propertyName
                + ", value: " + value);
        Session session = sessionFactory.getCurrentSession();
        try {
            // Property name concatenation is safe if only called internally
            String queryString = "from Secobjprivilege as model where model."
                    + propertyName + "= ?1 order by objectname_code";
            Query queryObject = session.createQuery(queryString);
            // Value is parameterized for security
            queryObject.setParameter(1, value);
            return queryObject.list();
        } catch (RuntimeException re) {
            logger.error("find by property name failed", re);
            throw re;
        } finally {
            // Manual session management
            session.close();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Retrieves privileges for a specific object, then filters by roles in memory.
     * This approach fetches all privileges for the object and filters client-side,
     * which may be inefficient for objects with many privilege assignments.
     * </p>
     * <p>
     * WARNING: Uses string concatenation for object name - SQL injection risk.
     * Should be refactored to use parameterized queries.
     * </p>
     */
    @Override
    public List<Secobjprivilege> getByObjectNameAndRoles(String o, List<String> roles) {
        // WARNING: String concatenation - SQL injection risk
        String queryString = "from Secobjprivilege obj where obj.objectname_code='" + o + "'";
        List<Secobjprivilege> results = new ArrayList<Secobjprivilege>();

        @SuppressWarnings("unchecked")
        List<Secobjprivilege> lst = (List<Secobjprivilege>) getHibernateTemplate().find(queryString);

        // In-memory filtering by roles - consider moving to database query
        for (Secobjprivilege p : lst) {
            if (roles.contains(p.getRoleusergroup())) {
                results.add(p);
            }
        }
        return results;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Efficiently retrieves all privileges for multiple roles using HQL's IN clause.
     * This method properly uses parameterized queries for security.
     * </p>
     */
    @Override
    public List<Secobjprivilege> getByRoles(List<String> roles) {
        String queryString = "from Secobjprivilege obj where obj.roleusergroup IN (?1)";
        List<Secobjprivilege> results = new ArrayList<Secobjprivilege>();

        Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery(queryString);

        // Properly parameterized list for IN clause
        q.setParameterList(1, roles);

        results = q.list();

        return results;
    }
}
