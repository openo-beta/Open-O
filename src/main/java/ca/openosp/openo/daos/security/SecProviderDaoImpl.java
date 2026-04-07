//CHECKSTYLE:OFF


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
 * @author JZhang
 */

public class SecProviderDaoImpl extends HibernateDaoSupport implements SecProviderDao {
    private static final Logger logger = MiscUtils.getLogger();
    // property constants

    @Override
    public void save(SecProvider transientInstance) {
        logger.debug("saving Provider instance");
        try {
            this.getHibernateTemplate().save(transientInstance);
            logger.debug("save successful");
        } catch (RuntimeException re) {
            logger.error("save failed", re);
            throw re;
        }
    }

    @Override
    public void saveOrUpdate(SecProvider transientInstance) {
        logger.debug("saving Provider instance");
        try {
            this.getHibernateTemplate().saveOrUpdate(transientInstance);
            logger.debug("save successful");
        } catch (RuntimeException re) {
            logger.error("save failed", re);
            throw re;
        }
    }

    @Override
    public void delete(SecProvider persistentInstance) {
        logger.debug("deleting Provider instance");
        try {
            this.getHibernateTemplate().delete(persistentInstance);
            logger.debug("delete successful");
        } catch (RuntimeException re) {
            logger.error("delete failed", re);
            throw re;
        }
    }

    @Override
    public SecProvider findById(java.lang.String id) {
        logger.debug("getting Provider instance with id: " + id);
        try {
            SecProvider instance = (SecProvider) this.getHibernateTemplate().get(
                    SecProvider.class, id);
            return instance;
        } catch (RuntimeException re) {
            logger.error("get failed", re);
            throw re;
        }
    }

    @Override
    public SecProvider findById(java.lang.String id, String status) {
        logger.debug("getting Provider instance with id: " + id);
        try {
            String sql = "from SecProvider where id=?0 and status=?1";
            List lst = this.getHibernateTemplate().find(sql, new Object[]{id, status});
            if (lst.size() == 0)
                return null;
            else
                return (SecProvider) lst.get(0);

        } catch (RuntimeException re) {
            logger.error("get failed", re);
            throw re;
        }
    }

    @Override
    public List findByExample(SecProviderDao instance) {
        logger.debug("finding Provider instance by example");
        Session session = currentSession();
        try {
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

    @Override
    public List findByProperty(String propertyName, Object value) {
        logger.debug("finding Provider instance with property: " + propertyName
                + ", value: " + value);
        Session session = currentSession();
        try {
            String queryString = "from Provider as model where model."
                    + propertyName + "= ?1";
            Query queryObject = session.createQuery(queryString);
            queryObject.setParameter(1, value);
            return queryObject.list();
        } catch (RuntimeException re) {
            logger.error("find by property name failed", re);
            throw re;
        }
    }

    @Override
    public List findByLastName(Object lastName) {
        return findByProperty(LAST_NAME, lastName);
    }

    @Override
    public List findByFirstName(Object firstName) {
        return findByProperty(FIRST_NAME, firstName);
    }

    @Override
    public List findByProviderType(Object providerType) {
        return findByProperty(PROVIDER_TYPE, providerType);
    }

    @Override
    public List findBySpecialty(Object specialty) {
        return findByProperty(SPECIALTY, specialty);
    }

    @Override
    public List findByTeam(Object team) {
        return findByProperty(TEAM, team);
    }

    @Override
    public List findBySex(Object sex) {
        return findByProperty(SEX, sex);
    }

    @Override
    public List findByAddress(Object address) {
        return findByProperty(ADDRESS, address);
    }

    @Override
    public List findByPhone(Object phone) {
        return findByProperty(PHONE, phone);
    }

    @Override
    public List findByWorkPhone(Object workPhone) {
        return findByProperty(WORK_PHONE, workPhone);
    }

    @Override
    public List findByOhipNo(Object ohipNo) {
        return findByProperty(OHIP_NO, ohipNo);
    }

    @Override
    public List findByRmaNo(Object rmaNo) {
        return findByProperty(RMA_NO, rmaNo);
    }

    @Override
    public List findByBillingNo(Object billingNo) {
        return findByProperty(BILLING_NO, billingNo);
    }

    @Override
    public List findByHsoNo(Object hsoNo) {
        return findByProperty(HSO_NO, hsoNo);
    }

    @Override
    public List findByStatus(Object status) {
        return findByProperty(STATUS, status);
    }

    @Override
    public List findByComments(Object comments) {
        return findByProperty(COMMENTS, comments);
    }

    @Override
    public List findByProviderActivity(Object providerActivity) {
        return findByProperty(PROVIDER_ACTIVITY, providerActivity);
    }

    @Override
    public List findAll() {
        logger.debug("finding all Provider instances");
        Session session = currentSession();
        try {
            String queryString = "from Provider";
            Query queryObject = session.createQuery(queryString);
            return queryObject.list();
        } catch (RuntimeException re) {
            logger.error("find all failed", re);
            throw re;
        }
    }

    @Override
    public SecProviderDao merge(SecProviderDao detachedInstance) {
        logger.debug("merging Provider instance");
        Session session = currentSession();
        try {
            SecProviderDao result = (SecProviderDao) session.merge(detachedInstance);
            logger.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            logger.error("merge failed", re);
            throw re;
        }
    }

    @Override
    public void attachDirty(SecProviderDao instance) {
        logger.debug("attaching dirty Provider instance");
        Session session = currentSession();
        try {
            session.saveOrUpdate(instance);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }

    @Override
    public void attachClean(SecProviderDao instance) {
        logger.debug("attaching clean Provider instance");
        Session session = currentSession();
        try {
            session.lock(instance, LockMode.NONE);
            logger.debug("attach successful");
        } catch (RuntimeException re) {
            logger.error("attach failed", re);
            throw re;
        }
    }
}
