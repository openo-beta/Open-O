//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.AppUser;

public interface AppUserDao extends AbstractDao<AppUser> {

    public AppUser findForProvider(int appId, String providerNo);
}
