//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.QuickListUser;

public interface QuickListUserDao extends AbstractDao<QuickListUser> {
    List<QuickListUser> findByNameAndProviderNo(String name, String providerNo);
}
