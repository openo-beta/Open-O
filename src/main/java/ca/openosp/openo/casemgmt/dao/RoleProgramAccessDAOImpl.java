//CHECKSTYLE:OFF


package ca.openosp.openo.casemgmt.dao;

import java.util.List;

import ca.openosp.openo.PMmodule.model.DefaultRoleAccess;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

public class RoleProgramAccessDAOImpl extends HibernateDaoSupport implements RoleProgramAccessDAO {

    @SuppressWarnings("unchecked")
    @Override
    public List<DefaultRoleAccess> getDefaultAccessRightByRole(Long roleId) {
        String q = "from DefaultRoleAccess da where da.caisi_role.id=?0";
        return (List<DefaultRoleAccess>) getHibernateTemplate().find(q, roleId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DefaultRoleAccess> getDefaultSpecificAccessRightByRole(Long roleId, String accessType) {
        String q = "from DefaultRoleAccess da where da.caisi_role.id=?0 and da.access_type.Name like ?1";
        return (List<DefaultRoleAccess>) getHibernateTemplate().find(q, new Object[]{roleId, accessType});
    }

    @Override
    public boolean hasAccess(String accessName, Long roleId) {
        String q = "from DefaultRoleAccess da where da.caisi_role.id=" + roleId + " and da.access_type.Name= ?0";
        return getHibernateTemplate().find(q, accessName).isEmpty() ? false : true;
    }
}
