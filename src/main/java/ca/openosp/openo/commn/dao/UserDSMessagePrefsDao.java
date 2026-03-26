//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.Hashtable;
import java.util.List;

import ca.openosp.openo.commn.model.UserDSMessagePrefs;

public interface UserDSMessagePrefsDao extends AbstractDao<UserDSMessagePrefs> {
    void saveProp(UserDSMessagePrefs prop);

    void updateProp(UserDSMessagePrefs prop);

    UserDSMessagePrefs getMessagePrefsOnType(String prov, String name);

    Hashtable<String, Long> getHashofMessages(String providerNo, String name);

    List<UserDSMessagePrefs> findMessages(String providerNo, String resourceType, String resourceId, boolean archived);

    UserDSMessagePrefs getDsMessage(String providerNo, String resourceType, String resourceId, boolean archived);

    List<UserDSMessagePrefs> findAllByResourceId(String resourceId);
}
