//CHECKSTYLE:OFF


package ca.openosp.openo.daos.security;

import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ca.openosp.openo.model.security.Secrole;

public class SecroleDaoImpl extends HibernateDaoSupport implements SecroleDao {

    private Logger logger = MiscUtils.getLogger();

    @Override
    public List<Secrole> getRoles() {
        @SuppressWarnings("unchecked")
        List<Secrole> results = (List<Secrole>) this.getHibernateTemplate().find("from Secrole r order by roleName");

        logger.debug("getRoles: # of results=" + results.size());

        return results;
    }

    @Override
    public Secrole getRole(Integer id) {
        if (id == null || id.intValue() <= 0) {
            throw new IllegalArgumentException();
        }

        Secrole result = this.getHibernateTemplate().get(Secrole.class, Long.valueOf(id));

        logger.debug("getRole: id=" + id + ",found=" + (result != null));

        return result;
    }

    @Override
    public Secrole getRoleByName(String roleName) {
        Secrole result = null;
        if (roleName == null || roleName.length() <= 0) {
            throw new IllegalArgumentException();
        }

        List lst = this.getHibernateTemplate().find("from Secrole r where r.roleName='" + roleName + "'");
        if (lst != null && lst.size() > 0)
            result = (Secrole) lst.get(0);

        logger.debug("getRoleByName: roleName=" + roleName + ",found=" + (result != null));

        return result;
    }

    @Override
    public List getDefaultRoles() {
        return this.getHibernateTemplate().find("from Secrole r where r.userDefined=0");
    }

    @Override
    public void save(Secrole secrole) {
        if (secrole == null) {
            throw new IllegalArgumentException();
        }

        getHibernateTemplate().saveOrUpdate(secrole);

    }

}
