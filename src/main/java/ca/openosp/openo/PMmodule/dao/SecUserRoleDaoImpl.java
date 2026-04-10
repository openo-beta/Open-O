//CHECKSTYLE:OFF


package ca.openosp.openo.PMmodule.dao;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.model.SecUserRole;
import ca.openosp.openo.utility.MiscUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class SecUserRoleDaoImpl extends HibernateDaoSupport implements SecUserRoleDao {

    private static Logger log = MiscUtils.getLogger();

    @Override
    public List<SecUserRole> getUserRoles(String providerNo) {
        if (providerNo == null) {
            throw new IllegalArgumentException();
        }

        String sSQL = "from SecUserRole s where s.ProviderNo = ?0";
        @SuppressWarnings("unchecked")
        List<SecUserRole> results = (List<SecUserRole>) getHibernateTemplate().find(sSQL, providerNo);

        if (log.isDebugEnabled()) {
            log.debug("getUserRoles: providerNo=" + providerNo + ",# of results=" + results.size());
        }

        return results;
    }

    @Override
    public List<SecUserRole> getSecUserRolesByRoleName(String roleName) {
        String sSQL = "from SecUserRole s where s.RoleName = ?0";
        @SuppressWarnings("unchecked")
        List<SecUserRole> results = (List<SecUserRole>) getHibernateTemplate().find(sSQL, roleName);

        return results;
    }

    @Override
    public List<SecUserRole> findByRoleNameAndProviderNo(String roleName, String providerNo) {
        String sSQL = "from SecUserRole s where s.RoleName = ?0 and s.ProviderNo=?1";
        @SuppressWarnings("unchecked")
        List<SecUserRole> results = (List<SecUserRole>) getHibernateTemplate().find(sSQL, new Object[]{roleName, providerNo});

        return results;
    }

    @Override
    public boolean hasAdminRole(String providerNo) {
        if (providerNo == null) {
            throw new IllegalArgumentException();
        }

        boolean result = false;
        String sSQL = "from SecUserRole s where s.ProviderNo = ?0 and s.RoleName = 'admin'";
        @SuppressWarnings("unchecked")
        List<SecUserRole> results = (List<SecUserRole>) this.getHibernateTemplate().find(sSQL, providerNo);
        if (!results.isEmpty()) {
            result = true;
        }

        if (log.isDebugEnabled()) {
            log.debug("hasAdminRole: providerNo=" + providerNo + ",result=" + result);
        }

        return result;
    }

    @Override
    public SecUserRole find(Long id) {
        return this.getHibernateTemplate().get(SecUserRole.class, id);
    }

    @Override
    public void save(SecUserRole sur) {
        sur.setLastUpdateDate(new Date());
        this.getHibernateTemplate().save(sur);
    }

    @Override
    public List<String> getRecordsAddedAndUpdatedSinceTime(Date date) {
        String sSQL = "select p.ProviderNo From SecUserRole p WHERE p.lastUpdateDate > ?0";
        @SuppressWarnings("unchecked")
        List<String> records = (List<String>) getHibernateTemplate().find(sSQL, date);

        return records;
    }

}
