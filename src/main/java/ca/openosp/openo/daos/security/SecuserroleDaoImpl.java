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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import ca.openosp.openo.PMmodule.web.formbean.StaffForm;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;

import ca.openosp.openo.model.security.Secuserrole;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate-based implementation of the SecuserroleDao interface.
 * <p>
 * This class manages user-role assignments with support for complex organizational
 * hierarchies and multi-facility deployments. It implements batch operations with
 * update-then-insert logic to prevent duplicates and maintains audit trails through
 * automatic lastUpdateDate tracking.
 * </p>
 * <p>
 * The implementation uses both HibernateTemplate and direct Session access depending
 * on the complexity of the operation. Session management includes manual closing in
 * some methods due to legacy code patterns that should be refactored.
 * </p>
 * <p>
 * Several methods contain SQL injection vulnerabilities due to string concatenation
 * in query building. These are marked with warnings and should be refactored to use
 * parameterized queries.
 * </p>
 *
 * @since 2005-01-01
 * @see SecuserroleDao
 * @see ca.openosp.openo.model.security.Secuserrole
 */
@Transactional
public class SecuserroleDaoImpl extends HibernateDaoSupport implements SecuserroleDao {
    /** Logger for debugging and audit */
    private static final Logger logger = MiscUtils.getLogger();
    /** Direct SessionFactory reference for complex queries */
    public SessionFactory sessionFactory;

    /**
     * Sets the SessionFactory with Spring autowiring.
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
     * Implements update-then-insert pattern to prevent duplicates.
     * Automatically sets lastUpdateDate for audit trail.
     * Note: Contains commented-out session management code from legacy migration.
     * </p>
     */
    @Override
    public void saveAll(List list) {
        logger.debug("saving ALL Secuserrole instances");
        Session session = currentSession();
        // Semicolon on separate line is likely a typo from refactoring
        ;
        try {
            for (int i = 0; i < list.size(); i++) {
                Secuserrole obj = (Secuserrole) list.get(i);
                // Set audit timestamp
                obj.setLastUpdateDate(new Date());
                // Try update first
                int rowcount = update(obj);

                if (rowcount <= 0) {
                    // No existing record, insert new
                    session.save(obj);
                }

            }
            logger.debug("save ALL successful");
        } catch (RuntimeException re) {
            logger.error("save ALL failed", re);
            throw re;
        }
    }

    @Override
    public void save(Secuserrole transientInstance) {
        logger.debug("saving Secuserrole instance");
        // Session session = getSession();
        Session session = currentSession();
        ;
        try {
            transientInstance.setLastUpdateDate(new Date());
            session.saveOrUpdate(transientInstance);
            logger.debug("save successful");
        } catch (RuntimeException re) {
            logger.error("save failed", re);
            throw re;
        }
        // finally {
        // this.releaseSession(session);
        // }
    }

    @Override
    public void updateRoleName(Integer id, String roleName) {
        Secuserrole sur = this.getHibernateTemplate().get(Secuserrole.class, id);
        if (sur != null) {
            sur.setRoleName(roleName);
            sur.setLastUpdateDate(new Date());
            this.getHibernateTemplate().update(sur);
        }
    }

    @Override
    public void delete(Secuserrole persistentInstance) {
        logger.debug("deleting Secuserrole instance");
        // Session session = getSession();
        Session session = currentSession();
        ;
        try {
            session.delete(persistentInstance);
            logger.debug("delete successful");
        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
        // finally {
        // this.releaseSession(session);
        // }
    }

    @Override
    public int deleteByOrgcd(String orgcd) {
        logger.debug("deleting Secuserrole by orgcd");
        try {

            return getHibernateTemplate().bulkUpdate("delete Secuserrole as model where model.orgcd =?0", orgcd);

        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    @Override
    public int deleteByProviderNo(String providerNo) {
        logger.debug("deleting Secuserrole by providerNo");
        try {

            return getHibernateTemplate().bulkUpdate("delete Secuserrole as model where model.providerNo =?0",
                    providerNo);

        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    @Override
    public int deleteById(Integer id) {
        logger.debug("deleting Secuserrole by ID");
        try {

            return getHibernateTemplate().bulkUpdate("delete Secuserrole as model where model.id =?0", id);

        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Updates active status and lastUpdateDate for an existing assignment.
     * WARNING: Uses string concatenation - SQL injection vulnerability.
     * This method should be refactored to use parameterized queries.
     * </p>
     */
    @Override
    public int update(Secuserrole instance) {
        logger.debug("Update Secuserrole instance");
        Session session = currentSession();
        // Semicolon on separate line is likely a typo
        ;
        try {
            // WARNING: String concatenation creates SQL injection risk
            // Should use parameterized queries instead
            String queryString = "update Secuserrole as model set model.activeyn ='" + instance.getActiveyn()
                    + "' , lastUpdateDate=now() "
                    + " where model.providerNo ='" + instance.getProviderNo() + "'"
                    + " and model.roleName ='" + instance.getRoleName() + "'"
                    + " and model.orgcd ='" + instance.getOrgcd() + "'";

            Query queryObject = session.createQuery(queryString);

            return queryObject.executeUpdate();

        } catch (RuntimeException re) {
            logger.error("Update failed", re);
            throw re;
        }
    }

    @Override
    public Secuserrole findById(java.lang.Integer id) {
        logger.debug("getting Secuserrole instance with id: " + id);
        // Session session = getSession();
        Session session = currentSession();
        ;
        try {
            Secuserrole instance = (Secuserrole) session.get(
                    Secuserrole.class, id);
            return instance;
        } catch (RuntimeException re) {
            logger.error("get failed", re);
            throw re;
        }
        // finally {
        // this.releaseSession(session);
        // }
    }

    @Override
    public List findByExample(Secuserrole instance) {
        // Session session = getSession();
        Session session = currentSession();
        ;
        logger.debug("finding Secuserrole instance by example");
        try {
            List results = session.createCriteria(
                            Secuserrole.class).add(
                            Example.create(instance))
                    .list();
            logger.debug("find by example successful, result size: "
                    + results.size());
            return results;
        } catch (RuntimeException re) {
            logger.error("find by example failed", re);
            throw re;
        }
        // finally {
        // this.releaseSession(session);
        // }
    }

    @Override
    public List findByProperty(String propertyName, Object value) {
        logger.debug("finding Secuserrole instance with property: " + propertyName
                + ", value: " + value);
        // Session session = getSession();
        Session session = currentSession();
        ;
        try {
            String queryString = "from Secuserrole as model where model."
                    + propertyName + "= ?1";
            Query queryObject = session.createQuery(queryString);
            queryObject.setParameter(1, value);
            return queryObject.list();
        } catch (RuntimeException re) {
            logger.error("find by property name failed", re);
            throw re;
        }
        // finally {
        // this.releaseSession(session);
        // }
    }

    @Override
    public List findByProviderNo(Object providerNo) {
        return findByProperty(PROVIDER_NO, providerNo);
    }

    @Override
    public List findByRoleName(Object roleName) {
        return findByProperty(ROLE_NAME, roleName);
    }

    @Override
    public List findByOrgcd(Object orgcd, boolean activeOnly) {
        // return findByProperty(ORGCD, orgcd);
        /*
         * SQL:
         * select * from secUserRole s,
         * (select codecsv from lst_orgcd where code = 'P200011') b
         * where b.codecsv like '%' || s.orgcd || ',%'
         * and not (s.orgcd like 'R%' or s.orgcd like 'O%')
         *
         */
        logger.debug("Find staff instance .");
        try {

            String queryString = "select a from Secuserrole a, LstOrgcd b, SecProvider p"
                    + " where a.providerNo=p.providerNo and b.code ='" + orgcd + "'";
            if (activeOnly)
                queryString += " and p.status='1'";

            queryString = queryString
                    + " and b.codecsv like '%' || a.orgcd || ',%'"
                    + " and not (a.orgcd like 'R%' or a.orgcd like 'O%')";

            return this.getHibernateTemplate().find(queryString);

        } catch (RuntimeException re) {
            logger.error("Find staff failed", re);
            throw re;
        }

    }

    /**
     * {@inheritDoc}
     * <p>
     * Searches for staff assignments using complex organizational hierarchy queries.
     * Uses StringEscapeUtils.escapeSql for basic SQL injection protection, but this
     * is not sufficient - should use parameterized queries instead.
     * </p>
     * <p>
     * The query excludes organization codes starting with 'R' or 'O' which appear
     * to be special system codes. Uses LIKE with concatenation for CSV field matching.
     * </p>
     */
    @Override
    public List searchByCriteria(StaffForm staffForm) {

        logger.debug("Search staff instance .");
        try {

            String AND = " and ";

            String orgcd = staffForm.getOrgcd();

            // Complex join with organization hierarchy
            // WARNING: String concatenation with orgcd - injection risk
            String queryString = "select a from Secuserrole a, LstOrgcd b"
                    + " where b.code ='" + orgcd + "'"
                    + " and b.codecsv like '%' || a.orgcd || ',%'"
                    + " and not (a.orgcd like 'R%' or a.orgcd like 'O%')";

            String fname = staffForm.getFirstName();
            String lname = staffForm.getLastName();

            if (fname != null && fname.length() > 0) {
                // Basic SQL escaping - not sufficient for security
                fname = StringEscapeUtils.escapeSql(fname);
                fname = fname.toLowerCase();
                queryString = queryString + AND + "lower(a.providerFName) like '%" + fname + "%'";
            }
            if (lname != null && lname.length() > 0) {
                // Basic SQL escaping - not sufficient for security
                lname = StringEscapeUtils.escapeSql(lname);
                lname = lname.toLowerCase();
                queryString = queryString + AND + "lower(a.providerLName) like '%" + lname + "%'";
            }

            return this.getHibernateTemplate().find(queryString);

        } catch (RuntimeException re) {
            logger.error("Search staff failed", re);
            throw re;
        }
    }

    @Override
    public List findByActiveyn(Object activeyn) {
        return findByProperty(ACTIVEYN, activeyn);
    }

    @Override
    public List findAll() {
        // Session session = getSession();
        Session session = currentSession();
        ;
        logger.debug("finding all Secuserrole instances");
        try {
            String queryString = "from Secuserrole";
            Query queryObject = session.createQuery(queryString);
            return queryObject.list();
        } catch (RuntimeException re) {
            logger.error("find all failed", re);
            throw re;
        }
        // finally {
        // this.releaseSession(session);
        // }
    }

    @Override
    public Secuserrole merge(Secuserrole detachedInstance) {
        logger.debug("merging Secuserrole instance");
        // Session session = getSession();
        Session session = currentSession();
        ;
        try {
            detachedInstance.setLastUpdateDate(new Date());
            Secuserrole result = (Secuserrole) session.merge(
                    detachedInstance);
            logger.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            logger.error("merge failed", re);
            throw re;
        }
        // finally {
        // this.releaseSession(session);
        // }
    }

    @Override
    public void attachDirty(Secuserrole instance) {
        logger.debug("attaching dirty Secuserrole instance");
        // Session session = getSession();
        Session session = currentSession();
        ;
        try {
            instance.setLastUpdateDate(new Date());
            session.saveOrUpdate(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
        // finally {
        // this.releaseSession(session);
        // }
    }

    @Override
    public void attachClean(Secuserrole instance) {
        logger.debug("attaching clean Secuserrole instance");
        // Session session = getSession();
        Session session = currentSession();
        ;
        try {
            session.lock(instance, LockMode.NONE);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
        // finally {
        // this.releaseSession(session);
        // }
    }
}
