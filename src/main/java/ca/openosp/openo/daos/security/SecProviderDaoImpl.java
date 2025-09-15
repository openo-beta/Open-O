//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
 * Contributors:
 * <Quatro Group Software Systems inc.>  <OSCAR Team>
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package ca.openosp.openo.daos.security;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ca.openosp.openo.model.security.SecProvider;

/**
 * Hibernate-based implementation of the SecProviderDao interface.
 * <p>
 * This class provides concrete implementation for managing security provider records
 * using Hibernate ORM. It handles all database operations for provider entities including
 * CRUD operations and various search queries. The implementation uses both HibernateTemplate
 * for simpler operations and direct Session access for more complex queries.
 * </p>
 * <p>
 * The class extends HibernateDaoSupport to leverage Spring's Hibernate integration,
 * providing automatic session management, exception translation, and transaction support.
 * All methods log their operations for debugging and audit purposes.
 * </p>
 * <p>
 * This DAO implementation includes error handling that logs exceptions before re-throwing
 * them, which aids in troubleshooting database-related issues in production environments.
 * The logging uses Log4j2 for structured logging capabilities.
 * </p>
 *
 * @since 2005-01-01
 * @see SecProviderDao
 * @see ca.openosp.openo.model.security.SecProvider
 * @see org.springframework.orm.hibernate5.support.HibernateDaoSupport
 */
public class SecProviderDaoImpl extends HibernateDaoSupport implements SecProviderDao {
    /** Logger instance for debugging and error tracking */
    private static final Logger logger = MiscUtils.getLogger();

    /**
     * {@inheritDoc}
     * <p>
     * Persists a new provider record using Hibernate's save method.
     * This method should only be used for new providers (transient instances).
     * For existing providers, use saveOrUpdate instead.
     * </p>
     */
    @Override
    public void save(SecProvider transientInstance) {
        logger.debug("saving Provider instance");
        try {
            // Direct save operation - assumes new entity
            this.getHibernateTemplate().save(transientInstance);
            logger.debug("save successful");
        } catch (RuntimeException re) {
            // Log the error with full stack trace for debugging
            logger.error("save failed", re);
            // Re-throw to trigger transaction rollback
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses Hibernate's intelligent saveOrUpdate to handle both new and existing providers.
     * The method determines whether to INSERT or UPDATE based on the entity's identifier
     * and persistence state.
     * </p>
     */
    @Override
    public void saveOrUpdate(SecProvider transientInstance) {
        logger.debug("saving Provider instance");
        try {
            // Hibernate will check if entity exists and choose INSERT or UPDATE
            this.getHibernateTemplate().saveOrUpdate(transientInstance);
            logger.debug("save successful");
        } catch (RuntimeException re) {
            logger.error("save failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Physically removes the provider record from the database.
     * Consider the implications for referential integrity and audit trails
     * before using this method. Status-based deactivation is often preferred.
     * </p>
     */
    @Override
    public void delete(SecProvider persistentInstance) {
        logger.debug("deleting Provider instance");
        try {
            // Physical deletion - consider using status field instead for audit trail
            this.getHibernateTemplate().delete(persistentInstance);
            logger.debug("delete successful");
        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses Hibernate's get method which returns null if the provider is not found.
     * This method leverages the session cache for performance optimization.
     * </p>
     */
    @Override
    public SecProvider findById(java.lang.String id) {
        logger.debug("getting Provider instance with id: " + id);
        try {
            // get() returns null if not found, doesn't throw exception
            SecProvider instance = (SecProvider) this.getHibernateTemplate().get(
                    SecProvider.class, id);
            return instance;
        } catch (RuntimeException re) {
            logger.error("get failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Queries for a provider with specific ID and status combination.
     * Commonly used to find only active providers (status="1").
     * Uses HQL with positional parameters for security.
     * </p>
     */
    @Override
    public SecProvider findById(java.lang.String id, String status) {
        logger.debug("getting Provider instance with id: " + id);
        try {
            // HQL query with positional parameters to prevent SQL injection
            String sql = "from SecProvider where id=?0 and status=?1";
            List lst = this.getHibernateTemplate().find(sql, new Object[]{id, status});
            // Return null if no match found
            if (lst.size() == 0)
                return null;
            else
                // Return first match (should be unique due to ID)
                return (SecProvider) lst.get(0);

        } catch (RuntimeException re) {
            logger.error("get failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses Hibernate's Example API for query-by-example searches.
     * Null properties are ignored, and string matches are exact by default.
     * This method requires direct session access for Criteria API.
     * </p>
     */
    @Override
    public List findByExample(SecProviderDao instance) {
        logger.debug("finding Provider instance by example");
        // Direct session access needed for Criteria API
        Session session = currentSession();
        try {
            // Query-by-example ignores null properties
            List results = session.createCriteria(
                            SecProvider.class).add(
                            Example.create(instance))
                    .list();
            logger.debug("find by example successful, result size: "
                    + results.size());
            return results;
        } catch (RuntimeException re) {
            logger.error("find by example failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Generic property search method that constructs HQL dynamically.
     * Note: The HQL references "Provider" but should map to SecProvider entity.
     * This appears to be a legacy naming convention that works due to entity mapping.
     * </p>
     */
    @Override
    public List findByProperty(String propertyName, Object value) {
        logger.debug("finding Provider instance with property: " + propertyName
                + ", value: " + value);
        Session session = currentSession();
        try {
            // Dynamic HQL construction - property name is concatenated but not user input
            // Using "Provider" entity name (mapped to SecProvider)
            String queryString = "from Provider as model where model."
                    + propertyName + "= ?1";
            Query queryObject = session.createQuery(queryString);
            // Parameterized value prevents SQL injection
            queryObject.setParameter(1, value);
            return queryObject.list();
        } catch (RuntimeException re) {
            logger.error("find by property name failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the LAST_NAME constant.
     */
    @Override
    public List findByLastName(Object lastName) {
        return findByProperty(LAST_NAME, lastName);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the FIRST_NAME constant.
     */
    @Override
    public List findByFirstName(Object firstName) {
        return findByProperty(FIRST_NAME, firstName);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the PROVIDER_TYPE constant.
     */
    @Override
    public List findByProviderType(Object providerType) {
        return findByProperty(PROVIDER_TYPE, providerType);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the SPECIALTY constant.
     */
    @Override
    public List findBySpecialty(Object specialty) {
        return findByProperty(SPECIALTY, specialty);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the TEAM constant.
     */
    @Override
    public List findByTeam(Object team) {
        return findByProperty(TEAM, team);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the SEX constant.
     */
    @Override
    public List findBySex(Object sex) {
        return findByProperty(SEX, sex);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the ADDRESS constant.
     */
    @Override
    public List findByAddress(Object address) {
        return findByProperty(ADDRESS, address);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the PHONE constant.
     */
    @Override
    public List findByPhone(Object phone) {
        return findByProperty(PHONE, phone);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the WORK_PHONE constant.
     */
    @Override
    public List findByWorkPhone(Object workPhone) {
        return findByProperty(WORK_PHONE, workPhone);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the OHIP_NO constant.
     */
    @Override
    public List findByOhipNo(Object ohipNo) {
        return findByProperty(OHIP_NO, ohipNo);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the RMA_NO constant.
     */
    @Override
    public List findByRmaNo(Object rmaNo) {
        return findByProperty(RMA_NO, rmaNo);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the BILLING_NO constant.
     */
    @Override
    public List findByBillingNo(Object billingNo) {
        return findByProperty(BILLING_NO, billingNo);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the HSO_NO constant.
     */
    @Override
    public List findByHsoNo(Object hsoNo) {
        return findByProperty(HSO_NO, hsoNo);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the STATUS constant.
     */
    @Override
    public List findByStatus(Object status) {
        return findByProperty(STATUS, status);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the COMMENTS constant.
     */
    @Override
    public List findByComments(Object comments) {
        return findByProperty(COMMENTS, comments);
    }

    /**
     * {@inheritDoc}
     * Delegates to findByProperty with the PROVIDER_ACTIVITY constant.
     */
    @Override
    public List findByProviderActivity(Object providerActivity) {
        return findByProperty(PROVIDER_ACTIVITY, providerActivity);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Retrieves all provider records without filtering.
     * Use with caution in production as this may return a large dataset.
     * Consider implementing pagination for better performance.
     * </p>
     */
    @Override
    public List findAll() {
        logger.debug("finding all Provider instances");
        Session session = currentSession();
        try {
            // Simple HQL to fetch all providers - consider pagination for large datasets
            String queryString = "from Provider";
            Query queryObject = session.createQuery(queryString);
            return queryObject.list();
        } catch (RuntimeException re) {
            logger.error("find all failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Merges a detached provider instance with the current persistence context.
     * This is useful when updating entities that were modified outside of the
     * current Hibernate session.
     * </p>
     */
    @Override
    public SecProviderDao merge(SecProviderDao detachedInstance) {
        logger.debug("merging Provider instance");
        Session session = currentSession();
        try {
            // Merge returns a managed copy of the entity
            SecProviderDao result = (SecProviderDao) session.merge(detachedInstance);
            logger.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            logger.error("merge failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Re-attaches a modified (dirty) provider instance to the session.
     * Hibernate will detect changes and synchronize with the database
     * at flush time.
     * </p>
     */
    @Override
    public void attachDirty(SecProviderDao instance) {
        logger.debug("attaching dirty Provider instance");
        Session session = currentSession();
        try {
            // saveOrUpdate will reattach and mark for update
            session.saveOrUpdate(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Re-attaches an unmodified (clean) provider instance to the session.
     * Uses lock with NONE mode to avoid version checking. The entity
     * is assumed to be unchanged since detachment.
     * </p>
     */
    @Override
    public void attachClean(SecProviderDao instance) {
        logger.debug("attaching clean Provider instance");
        Session session = currentSession();
        try {
            // Lock with NONE mode - no version check, assumes unchanged
            session.lock(instance, LockMode.NONE);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }
}
