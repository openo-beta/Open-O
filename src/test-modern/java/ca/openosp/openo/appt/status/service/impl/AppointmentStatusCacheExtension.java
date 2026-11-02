package ca.openosp.openo.appt.status.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.util.ReflectionTestUtils;

import ca.openosp.openo.commn.dao.AppointmentStatusDao;
import ca.openosp.openo.commn.model.AppointmentStatus;

/**
 * Lets a test change {@link AppointmentStatusMgrImpl}'s static state, the DAO it looks up on class
 * load and the schedule's status cache, and puts both back once the test ends. Register it as a
 * field with {@code @RegisterExtension}. The state is saved on the first change, because touching
 * the class loads it, which needs the test's AppointmentStatusDao mock registered first.
 *
 * @since 2026-10-23
 */
public final class AppointmentStatusCacheExtension implements AfterEachCallback {

    private static final List<String> FIELDS = List.of("appointStatusDao", "cachedStatuses", "cacheIsDirty");

    private Object[] saved;

    /**
     * Makes the manager use a DAO until the test ends.
     *
     * @param dao AppointmentStatusDao the DAO, usually a mock
     */
    public void useDao(AppointmentStatusDao dao) {
        set("appointStatusDao", dao);
    }

    /**
     * Fills the cache with statuses, as freshly loaded, until the test ends.
     *
     * @param statuses List&lt;AppointmentStatus&gt; the statuses the schedule should see
     */
    public void fill(List<AppointmentStatus> statuses) {
        save();
        AppointmentStatusMgrImpl.setCachedStatuses(new ArrayList<>(statuses));
        set("cacheIsDirty", false);
    }

    /**
     * Empties the cache, so the next lookup loads the statuses from the DAO.
     */
    public void empty() {
        set("cachedStatuses", null);
        set("cacheIsDirty", false);
    }

    private void set(String field, Object value) {
        save();
        ReflectionTestUtils.setField(AppointmentStatusMgrImpl.class, field, value);
    }

    private void save() {
        if (saved == null) {
            saved = FIELDS.stream().map(field -> ReflectionTestUtils.getField(AppointmentStatusMgrImpl.class, field)).toArray();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (saved != null) {
            for (int i = 0; i < FIELDS.size(); i++) {
                ReflectionTestUtils.setField(AppointmentStatusMgrImpl.class, FIELDS.get(i), saved[i]);
            }
            saved = null;
        }
    }
}
