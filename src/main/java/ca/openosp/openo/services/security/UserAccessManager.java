//CHECKSTYLE:OFF

package ca.openosp.openo.services.security;

import java.util.List;

import ca.openosp.openo.daos.security.UserAccessDao;
import ca.openosp.openo.services.LookupManager;

public interface UserAccessManager {
    SecurityManager getUserSecurityManager(String providerNo, Integer shelterId, LookupManager lkManager);

    List getAccessListForFunction(List list, int startIdx);

    void setUserAccessDao(UserAccessDao dao);
}
