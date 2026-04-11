//CHECKSTYLE:OFF


package ca.openosp.openo.daos.security;

import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ca.openosp.openo.model.security.Secobjectname;

/**
 * @author jackson
 */
public class SecObjectNameDaoImpl extends HibernateDaoSupport implements SecObjectNameDao {

    @Override
    public void saveOrUpdate(Secobjectname t) {

        try {

            this.getHibernateTemplate().saveOrUpdate(t);

        } catch (RuntimeException re) {

            throw re;
        }
    }
}
