//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Collection;
import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.SecObjPrivilege;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unchecked")
public class SecObjPrivilegeDaoImpl extends AbstractDaoImpl<SecObjPrivilege> implements SecObjPrivilegeDao {

    public SecObjPrivilegeDaoImpl() {
        super(SecObjPrivilege.class);
    }

    @Override
    public List<SecObjPrivilege> findByRoleUserGroupAndObjectName(String roleUserGroup, String objectName) {
        String sql = "select s FROM SecObjPrivilege s WHERE s.id.roleUserGroup = ?1 AND  s.id.objectName = ?2";

        Query query = entityManager.createQuery(sql);
        query.setParameter(1, roleUserGroup);
        query.setParameter(2, objectName);


        List<SecObjPrivilege> result = query.getResultList();

        return result;
    }

    @Override
    public List<SecObjPrivilege> findByObjectNames(Collection<String> objectNames) {
        String sql = "select s FROM SecObjPrivilege s WHERE s.id.objectName IN (?1) order by s.priority desc";

        Query query = entityManager.createQuery(sql);
        query.setParameter(1, objectNames);


        List<SecObjPrivilege> result = query.getResultList();

        return result;
    }

    @Override
    public List<SecObjPrivilege> findByRoleUserGroup(String roleUserGroup) {
        String sql = "select s FROM SecObjPrivilege s WHERE s.id.roleUserGroup like ?1 order by s.id.roleUserGroup, s.id.objectName";

        Query query = entityManager.createQuery(sql);
        query.setParameter(1, roleUserGroup);

        List<SecObjPrivilege> result = query.getResultList();

        return result;
    }

    @Override
    public List<SecObjPrivilege> findByObjectName(String objectName) {
        String sql = "select s FROM SecObjPrivilege s WHERE s.id.objectName like ?1 order by s.id.objectName, s.id.roleUserGroup";

        Query query = entityManager.createQuery(sql);
        query.setParameter(1, objectName);

        List<SecObjPrivilege> result = query.getResultList();

        return result;
    }

    @Override
    public int countObjectsByName(String objName) {
        String sql = "SELECT COUNT(*) FROM SecObjPrivilege p WHERE p.id.objectName = ?1";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, objName);
        List<Object> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return 0;
        }
        return (((Long) resultList.get(0))).intValue();
    }

    @Override
    public List<Object[]> findByFormNamePrivilegeAndProviderNo(String formName, String privilege, String providerNo) {
        String sql = "FROM SecObjPrivilege p, SecUserRole r " +
                "WHERE p.id.roleUserGroup = r.RoleName " +
                "AND p.id.objectName = ?1 " +
                "AND p.privilege = ?2 " +
                "AND r.ProviderNo = ?3";
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, formName);
        query.setParameter(2, privilege);
        query.setParameter(3, providerNo);
        return query.getResultList();
    }
}
