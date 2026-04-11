//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import ca.openosp.openo.PMmodule.model.FunctionalUserType;
import ca.openosp.openo.PMmodule.model.ProgramFunctionalUser;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;

public class ProgramFunctionalUserDAOImpl extends HibernateDaoSupport implements ProgramFunctionalUserDAO {

    private static Logger log = MiscUtils.getLogger();
    public SessionFactory sessionFactory;

    @Autowired
    public void setSessionFactoryOverride(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    @Override
    public List<FunctionalUserType> getFunctionalUserTypes() {
        String sSQL = "from FunctionalUserType";
        List<FunctionalUserType> results = (List<FunctionalUserType>) this.getHibernateTemplate().find(sSQL);

        if (log.isDebugEnabled()) {
            log.debug("getFunctionalUserTypes: # of results=" + results.size());
        }
        return results;
    }

    @Override
    public FunctionalUserType getFunctionalUserType(Long id) {
        if (id == null || id.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        FunctionalUserType result = this.getHibernateTemplate().get(FunctionalUserType.class, id);

        if (log.isDebugEnabled()) {
            log.debug("getFunctionalUserType: id=" + id + ",found=" + (result != null));
        }

        return result;
    }

    @Override
    public void saveFunctionalUserType(FunctionalUserType fut) {
        if (fut == null) {
            throw new IllegalArgumentException();
        }

        this.getHibernateTemplate().saveOrUpdate(fut);

        if (log.isDebugEnabled()) {
            log.debug("saveFunctionalUserType:" + fut.getId());
        }
    }

    @Override
    public void deleteFunctionalUserType(Long id) {
        if (id == null || id.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        this.getHibernateTemplate().delete(getFunctionalUserType(id));

        if (log.isDebugEnabled()) {
            log.debug("deleteFunctionalUserType:" + id);
        }
    }

    @Override
    public List<FunctionalUserType> getFunctionalUsers(Long programId) {
        if (programId == null || programId.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        String sSQL = "from ProgramFunctionalUser pfu where pfu.ProgramId = ?0";
        List<FunctionalUserType> results = (List<FunctionalUserType>) this.getHibernateTemplate().find(sSQL, programId);

        if (log.isDebugEnabled()) {
            log.debug("getFunctionalUsers: programId=" + programId + ",# of results=" + results.size());
        }
        return results;
    }

    @Override
    public ProgramFunctionalUser getFunctionalUser(Long id) {
        if (id == null || id.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        ProgramFunctionalUser result = this.getHibernateTemplate().get(ProgramFunctionalUser.class, id);

        if (log.isDebugEnabled()) {
            log.debug("getFunctionalUser: id=" + id + ",found=" + (result != null));
        }

        return result;
    }

    @Override
    public void saveFunctionalUser(ProgramFunctionalUser pfu) {
        if (pfu == null) {
            throw new IllegalArgumentException();
        }

        this.getHibernateTemplate().saveOrUpdate(pfu);

        if (log.isDebugEnabled()) {
            log.debug("saveFunctionalUser:" + pfu.getId());
        }
    }

    @Override
    public void deleteFunctionalUser(Long id) {
        if (id == null || id.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        this.getHibernateTemplate().delete(getFunctionalUser(id));

        if (log.isDebugEnabled()) {
            log.debug("deleteFunctionalUser:" + id);
        }
    }

    @Override
    public Long getFunctionalUserByUserType(Long programId, Long userTypeId) {
        if (programId == null || programId.intValue() <= 0) {
            throw new IllegalArgumentException();
        }
        if (userTypeId == null || userTypeId.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        Long result = null;

        String query = "select pfu.ProgramId from ProgramFunctionalUser pfu where pfu.ProgramId = ?0 and pfu.UserTypeId = ?1";
        Session session = sessionFactory.getCurrentSession();
        Query q = session.createQuery(query);
        q.setLong(1, programId.longValue());
        q.setLong(2, userTypeId.longValue());
        List results = new ArrayList();
        try {
            results = q.list();
        } finally {
            // releaseSession(session);
            session.close();
        }
        if (results.size() > 0) {
            result = (Long) results.get(0);
        }

        if (log.isDebugEnabled()) {
            log.debug("getFunctionalUserByUserType: programId=" + programId + ",userTypeId=" + userTypeId + ",result="
                    + result);
        }

        return result;
    }
}
