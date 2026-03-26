
package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.Property;

public interface PropertyDao extends AbstractDao<Property> {
    List<Property> findByName(String name);

    List<Property> findGlobalByName(String name);

    List<Property> findGlobalByName(Property.PROPERTY_KEY propertyName);

    List<Property> findByNameAndProvider(Property.PROPERTY_KEY propertyName, String providerNo);

    List<Property> findByNameAndProvider(String propertyName, String providerNo);

    List<Property> findByProvider(String providerNo);

    Property checkByName(String name);

    String getValueByNameAndDefault(String name, String defaultValue);

    List<Property> findByNameAndValue(String name, String value);

    void removeByName(String name);

    Boolean isActiveBooleanProperty(Property.PROPERTY_KEY name);

    Boolean isActiveBooleanProperty(Property.PROPERTY_KEY name, String providerNo);

    Boolean isActiveBooleanProperty(String name);

    Boolean isActiveBooleanProperty(String name, String providerNo);
}