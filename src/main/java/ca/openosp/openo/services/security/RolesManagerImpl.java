//CHECKSTYLE:OFF



package ca.openosp.openo.services.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ca.openosp.openo.daos.security.SecobjprivilegeDao;
import ca.openosp.openo.daos.security.SecroleDao;
import ca.openosp.openo.model.security.Secobjprivilege;
import ca.openosp.openo.model.security.Secrole;

@Transactional
public class RolesManagerImpl implements RolesManager {

    private SecroleDao secroleDao;
    private SecobjprivilegeDao secobjprivilegeDao;

    public void setSecroleDao(SecroleDao dao) {
        this.secroleDao = dao;
    }

    public List<Secrole> getRoles() {
        return secroleDao.getRoles();
    }

    public Secrole getRole(String id) {
        return secroleDao.getRole(Integer.parseInt(id));
    }

    public Secrole getRole(int id) {
        return secroleDao.getRole(id);
    }

    public Secrole getRoleByRolename(String roleName) {
        return secroleDao.getRoleByName(roleName);
    }

    public void save(Secrole secrole) {
        secroleDao.save(secrole);
    }

    public void saveFunction(Secobjprivilege secobjprivilege) {
        secobjprivilegeDao.save(secobjprivilege);
    }

    public void saveFunctions(Secrole secrole, List newLst, String roleName) {
        if (secrole != null) secroleDao.save(secrole);
        List existLst = secobjprivilegeDao.getFunctions(roleName);

        ArrayList lstForDelete = new ArrayList();
        for (int i = 0; i < existLst.size(); i++) {
            boolean keepIt = false;
            Secobjprivilege sur1 = (Secobjprivilege) existLst.get(i);
            for (int j = 0; j < newLst.size(); j++) {
                Secobjprivilege sur2 = (Secobjprivilege) newLst.get(j);
                if (compare(sur1, sur2)) {
                    keepIt = true;
                    break;
                }
            }
            if (!keepIt) lstForDelete.add(sur1);
        }
        for (int i = 0; i < lstForDelete.size(); i++) {
            secobjprivilegeDao.delete((Secobjprivilege) lstForDelete.get(i));
        }

        secobjprivilegeDao.saveAll(newLst);
    }

    public boolean compare(Secobjprivilege sur1, Secobjprivilege sur2) {
        boolean isSame = false;
        if (sur1.getObjectname_code().equals(sur2.getObjectname_code()) &&
                sur1.getPrivilege_code().equals(sur2.getPrivilege_code()) &&
                sur1.getRoleusergroup().equals(sur2.getRoleusergroup()))
            isSame = true;
        return isSame;
    }

    public List getFunctions(String roleName) {
        return secobjprivilegeDao.getFunctions(roleName);
    }

    public String getFunctionDesc(String function_code) {
        return secobjprivilegeDao.getFunctionDesc(function_code);
    }

    public String getAccessDesc(String accessType_code) {
        return secobjprivilegeDao.getAccessDesc(accessType_code);
    }

    public void setSecobjprivilegeDao(SecobjprivilegeDao secobjprivilegeDao) {
        this.secobjprivilegeDao = secobjprivilegeDao;
    }

}
