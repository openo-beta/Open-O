//CHECKSTYLE:OFF

package ca.openosp.openo.daos.security;

import java.util.List;

public interface UserAccessDao {
    public List GetUserAccessList(String providerNo, Integer shelterId);

    public List GetUserOrgAccessList(String providerNo, Integer shelterId);

}
