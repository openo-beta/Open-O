package ca.openosp.openo.test.unit;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import ca.openosp.OscarProperties;

/**
 * Sets or removes {@link OscarProperties} entries for one test, and puts back what was there
 * before once it ends, so a setting can't leak into the next test. Register it as a field with
 * {@code @RegisterExtension}.
 *
 * @since 2026-10-23
 */
public final class OscarPropertiesOverride implements AfterEachCallback {

    /** Each changed key's stored value before the test, or null when it was unset. */
    private final Map<String, String> originals = new HashMap<>();

    /**
     * Sets a property until the test ends.
     *
     * @param key String the property name
     * @param value String the value to use
     * @return OscarPropertiesOverride this, to chain calls
     */
    public OscarPropertiesOverride set(String key, String value) {
        remember(key);
        OscarProperties.getInstance().setProperty(key, value);
        return this;
    }

    /**
     * Unsets properties until the test ends.
     *
     * @param keys String... the property names
     * @return OscarPropertiesOverride this, to chain calls
     */
    public OscarPropertiesOverride remove(String... keys) {
        for (String key : keys) {
            remember(key);
            OscarProperties.getInstance().remove(key);
        }
        return this;
    }

    private void remember(String key) {
        // The stored entry, not getProperty's, which fills in a default or an environment value
        if (!originals.containsKey(key)) {
            originals.put(key, (String) OscarProperties.getInstance().get(key));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        OscarProperties properties = OscarProperties.getInstance();
        originals.forEach((key, value) -> {
            if (value == null) {
                properties.remove(key);
            } else {
                properties.setProperty(key, value);
            }
        });
        originals.clear();
    }
}
