//CHECKSTYLE:OFF


package ca.openosp.openo.daos.security;

import java.util.List;

import ca.openosp.openo.model.security.Secobjprivilege;

public interface SecobjprivilegeDao {

    public void save(Secobjprivilege secobjprivilege);

    public void saveAll(List list);

    public int update(Secobjprivilege instance);

    public int deleteByRoleName(String roleName);

    public void delete(Secobjprivilege persistentInstance);

    public String getFunctionDesc(String function_code);

    public String getAccessDesc(String accessType_code);

    public List getFunctions(String roleName);

    public List findByProperty(String propertyName, Object value);

    public List<Secobjprivilege> getByObjectNameAndRoles(String o, List<String> roles);

    public List<Secobjprivilege> getByRoles(List<String> roles);
}
