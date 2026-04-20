//CHECKSTYLE:OFF

package ca.openosp.openo.daos.security;

import java.util.List;

import ca.openosp.openo.model.security.Secrole;

public interface SecroleDao {

    public List<Secrole> getRoles();

    public Secrole getRole(Integer id);

    public Secrole getRoleByName(String roleName);

    public List getDefaultRoles();

    public void save(Secrole secrole);

}
