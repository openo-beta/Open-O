package ca.openosp.openo.managers;

import java.util.List;
import java.util.stream.Stream;

import ca.openosp.openo.commn.dao.LookupListItemDao;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
 * Unit tests for the lookupListItem changes in {@link LookupListManager}:
 * {@code updateLookupListItemColour}, {@code updateLookupListItemIcon},
 * {@code removeLookupListItem}, {@code restoreLookupListItem} and {@code moveLookupListItem}.
 *
 * <p>A colour or an icon ends up in style and class attributes on the schedule, so a valid value
 * is stored, blank clears it, and anything ItemStyleColour or IconSet refuses is rejected before
 * the item is loaded; their own tests hold the full lists. Every change requires _admin update.</p>
 *
 * @since 2026-09-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LookupListManager item change unit tests")
@Tag("unit")
@Tag("fast")
@Tag("manager")
public class LookupListManagerUnitTest extends OpenOUnitTestBase {

    private static final int ITEM_ID = 7;

    @Mock
    private LookupListItemDao lookupListItemDao;

    @Mock
    private AppointmentLocationManager appointmentLocationManager;

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

    /** A change to one item, as the guard tests apply it. */
    @FunctionalInterface
    interface ItemChange {
        boolean apply(LookupListManager manager, LoggedInInfo loggedInInfo, int lookupListItemId);
    }

    /** Every item change that needs _admin update, named for the test report. */
    static Stream<Named<ItemChange>> updates() {
        return Stream.of(
                Named.of("updateLookupListItemColour", (ItemChange) (m, info, id) -> m.updateLookupListItemColour(info, id, "#445566")),
                Named.of("updateLookupListItemIcon", (ItemChange) (m, info, id) -> m.updateLookupListItemIcon(info, id, "glyphicon-star")),
                Named.of("restoreLookupListItem", (ItemChange) (m, info, id) -> m.restoreLookupListItem(info, id)),
                Named.of("moveLookupListItem", (ItemChange) (m, info, id) -> m.moveLookupListItem(info, id, true)));
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

        @Test
        @DisplayName("should reject a colour ItemStyleColour refuses, such as one breaking out of its style attribute")
        void shouldRejectColour_whenNotHexColour() {
            assertThatThrownBy(() -> manager.updateLookupListItemColour(loggedInInfo, ITEM_ID, "#112233;background:url(x)"))
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

        @ParameterizedTest
        @ValueSource(strings = {"glyphicon-map-marker", "here.gif"})
        @DisplayName("should store the icon when it is in the Icon Set, a glyph or an image")
        void shouldStoreIcon_whenInIconSet(String icon) {
            assertThat(manager.updateLookupListItemIcon(loggedInInfo, ITEM_ID, icon)).isTrue();

            assertThat(item.getIcon()).isEqualTo(icon);
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

        @Test
        @DisplayName("should reject an icon outside the Icon Set, such as one breaking out of its class attribute")
        void shouldRejectIcon_whenNotInIconSet() {
            assertThatThrownBy(() -> manager.updateLookupListItemIcon(loggedInInfo, ITEM_ID, "glyphicon-home\" onclick=\"x"))
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

        @ParameterizedTest(name = "{0}")
        @MethodSource("ca.openosp.openo.managers.LookupListManagerUnitTest#updates")
        @DisplayName("should deny the change and touch nothing without _admin update")
        void shouldDeny_whenUserLacksAdminUpdate(ItemChange change) {
            when(securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.UPDATE, null))
                    .thenReturn(false);

            assertThatThrownBy(() -> change.apply(manager, loggedInInfo, ITEM_ID)).hasMessage("Access Denied");

            verify(lookupListItemDao, never()).find(anyInt());
            verify(lookupListItemDao, never()).merge(any());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("ca.openosp.openo.managers.LookupListManagerUnitTest#updates")
        @DisplayName("should return false and save nothing when the item does not exist")
        void shouldReturnFalse_whenItemMissing(ItemChange change) {
            assertThat(change.apply(manager, loggedInInfo, 99)).isFalse();

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

    @Nested
    @DisplayName("removeLookupListItem")
    class Remove {

        @BeforeEach
        void allowDelete() {
            item.setActive(true);
            when(securityInfoManager.hasPrivilege(loggedInInfo, "_admin", SecurityInfoManager.DELETE, null))
                    .thenReturn(true);
        }

        @Test
        @DisplayName("should disable the item and stop it being the Default Location, if it is")
        void shouldRemoveDefault_whenDisabled() {
            assertThat(manager.removeLookupListItem(loggedInInfo, ITEM_ID)).isTrue();

            assertThat(item.isActive()).isFalse();
            verify(lookupListItemDao).merge(item);
            verify(appointmentLocationManager).removeDefault(loggedInInfo, ITEM_ID);
        }
    }

    @Nested
    @DisplayName("restoreLookupListItem")
    class Restore {

        @BeforeEach
        void deactivateItem() {
            item.setActive(false);
            item.setDisplayOrder(2);
        }

        @Test
        @DisplayName("should put the item back at the end of the list")
        void shouldAppend_whenRestored() {
            item.setLookupListId(5);
            when(lookupListItemDao.findActiveByLookupListId(5)).thenReturn(List.of(active(1, 1), active(2, 4)));

            assertThat(manager.restoreLookupListItem(loggedInInfo, ITEM_ID)).isTrue();

            assertThat(item.isActive()).isTrue();
            assertThat(item.getDisplayOrder()).isEqualTo(5);
            verify(lookupListItemDao).merge(item);
        }

        @Test
        @DisplayName("should be the first in order when no other item is in use")
        void shouldBeFirst_whenNoneActive() {
            item.setLookupListId(5);
            when(lookupListItemDao.findActiveByLookupListId(5)).thenReturn(List.of());

            assertThat(manager.restoreLookupListItem(loggedInInfo, ITEM_ID)).isTrue();

            assertThat(item.getDisplayOrder()).isEqualTo(1);
        }

        @Test
        @DisplayName("should leave an item that is already in use where it is")
        void shouldLeaveOrder_whenAlreadyActive() {
            item.setActive(true);

            assertThat(manager.restoreLookupListItem(loggedInInfo, ITEM_ID)).isTrue();

            assertThat(item.getDisplayOrder()).isEqualTo(2);
            verify(lookupListItemDao, never()).merge(any());
        }
    }

    @Nested
    @DisplayName("moveLookupListItem")
    class Move {

        private LookupListItem first;
        private LookupListItem last;

        @BeforeEach
        void threeItemList() {
            item.setActive(true);
            item.setLookupListId(5);
            item.setDisplayOrder(2);
            first = active(101, 1);
            last = active(102, 3);
            lenient().when(lookupListItemDao.findActiveByLookupListId(5)).thenReturn(List.of(first, item, last));
        }

        @Test
        @DisplayName("should move the item one place towards the start")
        void shouldMoveUp_whenNotFirst() {
            assertThat(manager.moveLookupListItem(loggedInInfo, ITEM_ID, true)).isTrue();

            assertThat(item.getDisplayOrder()).isEqualTo(1);
            assertThat(first.getDisplayOrder()).isEqualTo(2);
            assertThat(last.getDisplayOrder()).isEqualTo(3);
        }

        @Test
        @DisplayName("should move the item one place towards the end")
        void shouldMoveDown_whenNotLast() {
            assertThat(manager.moveLookupListItem(loggedInInfo, ITEM_ID, false)).isTrue();

            assertThat(first.getDisplayOrder()).isEqualTo(1);
            assertThat(last.getDisplayOrder()).isEqualTo(2);
            assertThat(item.getDisplayOrder()).isEqualTo(3);
        }

        @Test
        @DisplayName("should write only the items whose place changed")
        void shouldWriteMovedItemsOnly_whenMoved() {
            manager.moveLookupListItem(loggedInInfo, ITEM_ID, true);

            verify(lookupListItemDao).merge(item);
            verify(lookupListItemDao).merge(first);
            verify(lookupListItemDao, never()).merge(last);
        }

        @Test
        @DisplayName("should reorder even when two items share a display order")
        void shouldReorder_whenOrdersRepeat() {
            // An item added while another was inactive holds the same order as the one before it.
            first.setDisplayOrder(2);

            assertThat(manager.moveLookupListItem(loggedInInfo, ITEM_ID, true)).isTrue();

            assertThat(item.getDisplayOrder()).isEqualTo(1);
            assertThat(first.getDisplayOrder()).isEqualTo(2);
        }

        @Test
        @DisplayName("should do nothing at either end of the list")
        void shouldReturnFalse_whenAtTheEnd() {
            when(lookupListItemDao.find(101)).thenReturn(first);
            when(lookupListItemDao.find(102)).thenReturn(last);
            first.setLookupListId(5);
            last.setLookupListId(5);

            assertThat(manager.moveLookupListItem(loggedInInfo, 101, true)).isFalse();
            assertThat(manager.moveLookupListItem(loggedInInfo, 102, false)).isFalse();

            verify(lookupListItemDao, never()).merge(any());
        }

        @Test
        @DisplayName("should not move an inactive item")
        void shouldReturnFalse_whenItemInactive() {
            item.setActive(false);

            assertThat(manager.moveLookupListItem(loggedInInfo, ITEM_ID, true)).isFalse();

            verify(lookupListItemDao, never()).merge(any());
        }
    }

    private static LookupListItem active(int id, int displayOrder) {
        LookupListItem other = new LookupListItem();
        other.setId(id);
        other.setActive(true);
        other.setDisplayOrder(displayOrder);
        return other;
    }
}
