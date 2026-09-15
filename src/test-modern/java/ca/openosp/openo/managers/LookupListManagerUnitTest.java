package ca.openosp.openo.managers;

import ca.openosp.openo.commn.dao.LookupListDao;
import ca.openosp.openo.commn.dao.LookupListItemDao;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the lookupListItem style updates in {@link LookupListManager}:
 * {@code updateLookupListItemColour} and {@code updateLookupListItemIcon}; and for
 * {@code findAppointmentLocationList}, the one place the Location List is resolved.
 *
 * <p>Both values end up in class and style attributes on the schedule, so the tests pin the
 * whitelist: a valid value is stored, blank clears it, anything else is rejected before the
 * item is loaded, and every path requires _admin update.</p>
 *
 * @since 2026-09-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LookupListManager style update and Location List unit tests")
@Tag("unit")
@Tag("fast")
@Tag("manager")
@Tag("update")
public class LookupListManagerUnitTest extends OpenOUnitTestBase {

    private static final int ITEM_ID = 7;

    @Mock
    private LookupListDao lookupListDao;

    @Mock
    private LookupListItemDao lookupListItemDao;

    @Mock
    private SecurityInfoManager securityInfoManager;

    @InjectMocks
    private LookupListManager manager;

    private final LoggedInInfo loggedInInfo = new LoggedInInfo();
    private MockedStatic<LogAction> logActionMock;
    private LookupListItem item;

    @BeforeEach
    void setUp() {
        // LogAction's static initializer looks up OscarLogDao, so register it before mocking LogAction
        registerMock(OscarLogDao.class, mock(OscarLogDao.class));
        logActionMock = mockStatic(LogAction.class);

        lenient().when(securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null))
                .thenReturn(true);

        item = new LookupListItem();
        item.setId(ITEM_ID);
        item.setIcon("glyphicon-home");
        item.setColour("#112233");
        lenient().when(lookupListItemDao.find(ITEM_ID)).thenReturn(item);
    }

    @AfterEach
    void tearDown() {
        logActionMock.close();
    }

    @Nested
    @DisplayName("findAppointmentLocationList")
    class LocationListResolver {

        @Test
        @DisplayName("should resolve the appointmentLocationCode list without a privilege check")
        void shouldFindLocationList_whenAnyUser() {
            LookupList locationList = new LookupList();
            when(lookupListDao.findByName("appointmentLocationCode")).thenReturn(locationList);

            assertThat(manager.findAppointmentLocationList(loggedInInfo)).isSameAs(locationList);

            verify(securityInfoManager, never()).hasPrivilege(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("updateLookupListItemColour")
    class Colour {

        @ParameterizedTest
        @ValueSource(strings = {"#1a2B3c", "  #1a2b3c  "})
        @DisplayName("should store the trimmed colour when it is #rrggbb")
        void shouldStoreColour_whenHexColour(String colour) {
            assertThat(manager.updateLookupListItemColour(loggedInInfo, ITEM_ID, colour)).isTrue();

            assertThat(item.getColour()).isEqualTo(colour.trim());
            verify(lookupListItemDao).merge(item);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("should clear the colour when the value is blank")
        void shouldClearColour_whenBlank(String colour) {
            assertThat(manager.updateLookupListItemColour(loggedInInfo, ITEM_ID, colour)).isTrue();

            assertThat(item.getColour()).isNull();
            verify(lookupListItemDao).merge(item);
        }

        @ParameterizedTest
        @ValueSource(strings = {"red", "#fff", "112233", "#12345g", "#1122334", "#112233;background:url(x)"})
        @DisplayName("should reject the colour and save nothing when it is not #rrggbb")
        void shouldRejectColour_whenNotHexColour(String colour) {
            assertThatThrownBy(() -> manager.updateLookupListItemColour(loggedInInfo, ITEM_ID, colour))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(item.getColour()).isEqualTo("#112233");
            verify(lookupListItemDao, never()).merge(any());
        }

        @Test
        @DisplayName("should leave the icon alone when the colour changes")
        void shouldKeepIcon_whenColourUpdated() {
            manager.updateLookupListItemColour(loggedInInfo, ITEM_ID, "#445566");

            assertThat(item.getIcon()).isEqualTo("glyphicon-home");
        }
    }

    @Nested
    @DisplayName("updateLookupListItemIcon")
    class Icon {

        @Test
        @DisplayName("should store the icon when it is a glyphicon class name")
        void shouldStoreIcon_whenGlyphiconClass() {
            assertThat(manager.updateLookupListItemIcon(loggedInInfo, ITEM_ID, "glyphicon-map-marker")).isTrue();

            assertThat(item.getIcon()).isEqualTo("glyphicon-map-marker");
            verify(lookupListItemDao).merge(item);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("should clear the icon when the value is blank")
        void shouldClearIcon_whenBlank(String icon) {
            assertThat(manager.updateLookupListItemIcon(loggedInInfo, ITEM_ID, icon)).isTrue();

            assertThat(item.getIcon()).isNull();
            verify(lookupListItemDao).merge(item);
        }

        @ParameterizedTest
        @ValueSource(strings = {"home", "glyphicon-", "glyphicon-Home", "fa-home", "glyphicon home",
                "glyphicon-home\" onclick=\"x"})
        @DisplayName("should reject the icon and save nothing when it is not a glyphicon class name")
        void shouldRejectIcon_whenNotGlyphiconClass(String icon) {
            assertThatThrownBy(() -> manager.updateLookupListItemIcon(loggedInInfo, ITEM_ID, icon))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(item.getIcon()).isEqualTo("glyphicon-home");
            verify(lookupListItemDao, never()).merge(any());
        }

        @Test
        @DisplayName("should leave the colour alone when the icon changes")
        void shouldKeepColour_whenIconUpdated() {
            manager.updateLookupListItemIcon(loggedInInfo, ITEM_ID, "glyphicon-star");

            assertThat(item.getColour()).isEqualTo("#112233");
        }
    }

    @Nested
    @DisplayName("shared rules")
    class SharedRules {

        @Test
        @DisplayName("should deny both updates and touch nothing without _admin update")
        void shouldDenyUpdate_whenUserLacksAdminUpdate() {
            when(securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null))
                    .thenReturn(false);

            assertThatThrownBy(() -> manager.updateLookupListItemColour(loggedInInfo, ITEM_ID, "#445566"))
                    .hasMessage("Access Denied");
            assertThatThrownBy(() -> manager.updateLookupListItemIcon(loggedInInfo, ITEM_ID, "bad value"))
                    .hasMessage("Access Denied");

            verify(lookupListItemDao, never()).find(anyInt());
            verify(lookupListItemDao, never()).merge(any());
        }

        @Test
        @DisplayName("should return false and save nothing when the item does not exist")
        void shouldReturnFalse_whenItemMissing() {
            assertThat(manager.updateLookupListItemColour(loggedInInfo, 99, "#445566")).isFalse();
            assertThat(manager.updateLookupListItemIcon(loggedInInfo, 0, "glyphicon-star")).isFalse();

            verify(lookupListItemDao, never()).merge(any());
        }

        @Test
        @DisplayName("should write the audit log through updateLookupListItem")
        void shouldLogMerge_whenStyleUpdated() {
            manager.updateLookupListItemIcon(loggedInInfo, ITEM_ID, "glyphicon-star");

            logActionMock.verify(() -> LogAction.addLogSynchronous(eq(loggedInInfo),
                    eq("LookupListManager.updateLookupListItem"), contains(String.valueOf(ITEM_ID))));
        }
    }
}
