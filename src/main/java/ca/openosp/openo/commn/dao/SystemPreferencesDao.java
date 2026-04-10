//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import ca.openosp.openo.commn.model.SystemPreferences;

import java.util.*;

public interface SystemPreferencesDao extends AbstractDao<SystemPreferences> {

    <T extends Enum<T>> SystemPreferences findPreferenceByName(Enum<T> name);

    <E extends Enum<E>> List<SystemPreferences> findPreferencesByNames(Class<E> clazz);

    <E extends Enum<E>> Map<String, Boolean> findByKeysAsMap(Class<E> clazz);

    Map<String, SystemPreferences> findByKeysAsPreferenceMap(List<String> keys);

    <T extends Enum<T>> boolean isReadBooleanPreference(Enum<T> name);

    <T extends Enum<T>> boolean isPreferenceValueEquals(Enum<T> preferenceName, String trueValueStr);
}
