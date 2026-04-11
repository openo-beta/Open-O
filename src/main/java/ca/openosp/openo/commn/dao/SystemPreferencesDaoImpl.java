//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.SystemPreferences;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class SystemPreferencesDaoImpl extends AbstractDaoImpl<SystemPreferences> implements SystemPreferencesDao {
    public SystemPreferencesDaoImpl() {
        super(SystemPreferences.class);
    }

    @Override
    public <T extends Enum<T>> SystemPreferences findPreferenceByName(Enum<T> name) {
        return findPreferenceByName(name.name());
    }

    /**
     * DEPRECATED: use enumerator
     */
    private SystemPreferences findPreferenceByName(String name) {
        Query query = entityManager.createQuery("FROM SystemPreferences sp WHERE sp.name = ?1");
        query.setParameter(1, name);

        List<SystemPreferences> results = query.getResultList();
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }

    private List<SystemPreferences> findPreferencesByNames(List<String> names) {
        Query query = entityManager.createQuery("FROM SystemPreferences sp WHERE sp.name IN (?1)");
        query.setParameter(1, names);

        List<SystemPreferences> results = query.getResultList();
        return results;
    }

    @Override
    public <E extends Enum<E>> List<SystemPreferences> findPreferencesByNames(Class<E> clazz) {
        List<String> parameters = new ArrayList<>();
        for (Enum<E> enumValue : EnumSet.allOf(clazz)) {
            parameters.add(enumValue.name());
        }

        return findPreferencesByNames(parameters);
    }

    @Override
    public <E extends Enum<E>> Map<String, Boolean> findByKeysAsMap(Class<E> clazz) {
        List<String> keyList = new ArrayList<>();
        for (Enum<E> enumValue : EnumSet.allOf(clazz)) {
            keyList.add(enumValue.name());
        }
        return findByKeysAsMap(keyList);
    }

    /**
     * Gets a map of system preference values
     *
     * @param keys List of preference keys to search for in the database
     * @return Map of preference keys with their associated boolean value
     */
    private Map<String, Boolean> findByKeysAsMap(List<String> keys) {
        List<SystemPreferences> preferences = findPreferencesByNames(keys);
        Map<String, Boolean> preferenceMap = new HashMap<String, Boolean>();

        for (SystemPreferences preference : preferences) {
            preferenceMap.put(preference.getName(), preference.getValueAsBoolean());
        }

        return preferenceMap;
    }

    /**
     * Gets a map of system preferences with the preference name as the key
     *
     * @param keys List of keys to get the preferences for
     * @return A map of SystemPreferences with the preference name as the key
     */
    @Override
    public Map<String, SystemPreferences> findByKeysAsPreferenceMap(List<String> keys) {
        Map<String, SystemPreferences> preferenceMap = new HashMap<>();

        List<SystemPreferences> preferences = findPreferencesByNames(keys);

        for (SystemPreferences preference : preferences) {
            preferenceMap.put(preference.getName(), preference);
        }

        return preferenceMap;
    }

    @Override
    public <T extends Enum<T>> boolean isReadBooleanPreference(Enum<T> name) {
        return isReadBooleanPreference(name.name());
    }

    private boolean isReadBooleanPreference(String name) {
        SystemPreferences preference = findPreferenceByName(name);
        return (preference != null && Boolean.parseBoolean(preference.getValue()));
    }

    @Override
    public <T extends Enum<T>> boolean isPreferenceValueEquals(Enum<T> preferenceName, String trueValueStr) {
        return isPreferenceValueEquals(preferenceName.name(), trueValueStr);
    }

    private boolean isPreferenceValueEquals(String preferenceName, String trueValueStr) {
        SystemPreferences preference = findPreferenceByName(preferenceName);
        return (preference != null && trueValueStr.equals(preference.getValue()));
    }
}
