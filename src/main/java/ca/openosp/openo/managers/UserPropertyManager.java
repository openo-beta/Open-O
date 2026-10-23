package ca.openosp.openo.managers;

import ca.openosp.openo.commn.dao.UserPropertyDAO;
import ca.openosp.openo.commn.model.UserProperty;
import ca.openosp.openo.commn.model.enumerator.UserPropertyKey;
import ca.openosp.openo.utility.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * Reads the settings of the logged-in provider from the property table.
 * It never reads another provider's settings.
 *
 * @since 2026-09-30
 */
@Service
public class UserPropertyManager {

    @Autowired
    private UserPropertyDAO userPropertyDao;

    /**
     * Gets one setting of the logged-in provider.
     *
     * @param loggedInInfo LoggedInInfo the logged-in user
     * @param property UserPropertyKey the setting to read
     * @return UserProperty the stored row, or null when the provider has not set it
     */
    public UserProperty getUserProperty(LoggedInInfo loggedInInfo, UserPropertyKey property) {
        String providerNumber = loggedInInfo.getLoggedInProviderNo();
        UserProperty userProperty = null;
        if (providerNumber != null) {
            userProperty = userPropertyDao.getProp(providerNumber, property);
        }
        return userProperty;
    }

    /**
     * Gets all settings of the logged-in provider.
     *
     * @param loggedInInfo LoggedInInfo the logged-in user
     * @return Map&lt;String, String&gt; setting name to value, empty when there is no logged-in provider
     */
    public Map<String, String> getAllUserProperties(LoggedInInfo loggedInInfo) {
        String providerNumber = loggedInInfo.getLoggedInProviderNo();
        Map<String, String> userProperties = Collections.emptyMap();
        if (providerNumber != null) {
            userProperties = userPropertyDao.getProviderPropertiesAsMap(providerNumber);
        }
        return userProperties;
    }
}
