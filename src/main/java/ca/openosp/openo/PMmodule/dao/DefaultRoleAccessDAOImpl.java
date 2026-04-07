//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.DefaultRoleAccess;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

@SuppressWarnings("unchecked")
public class DefaultRoleAccessDAOImpl extends HibernateDaoSupport implements DefaultRoleAccessDAO {

    public void deleteDefaultRoleAccess(Long id) {
        this.getHibernateTemplate().delete(getDefaultRoleAccess(id));
    }

    public DefaultRoleAccess getDefaultRoleAccess(Long id) {
        return this.getHibernateTemplate().get(DefaultRoleAccess.class, id);
    }

    public List<DefaultRoleAccess> getDefaultRoleAccesses() {
        return (List<DefaultRoleAccess>) this.getHibernateTemplate().find("from DefaultRoleAccess dra ORDER BY role_id");
    }

    public List<DefaultRoleAccess> findAll() {
        return (List<DefaultRoleAccess>) this.getHibernateTemplate().find("from DefaultRoleAccess dra");
    }

    public void saveDefaultRoleAccess(DefaultRoleAccess dra) {
        this.getHibernateTemplate().saveOrUpdate(dra);
    }

    public DefaultRoleAccess find(Long roleId, Long accessTypeId) {
        String sSQL = "from DefaultRoleAccess dra where dra.roleId=?0 and dra.accessTypeId=?1";
        List results = this.getHibernateTemplate().find(sSQL, new Object[]{roleId, accessTypeId});

        if (!results.isEmpty()) {
            return (DefaultRoleAccess) results.get(0);
        }
        return null;
    }

    public List<Object[]> findAllRolesAndAccessTypes() {
        return (List<Object[]>) getHibernateTemplate().find("FROM DefaultRoleAccess a, AccessType b WHERE a.id = b.Id");
    }

}
