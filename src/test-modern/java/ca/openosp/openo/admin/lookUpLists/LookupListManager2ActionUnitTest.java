package ca.openosp.openo.admin.lookUpLists;

import java.util.HashMap;
import java.util.List;

import ca.openosp.openo.appt.LocationList;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.managers.LookupListManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;

import com.opensymphony.xwork2.ActionContext;
import org.apache.struts2.ServletActionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LookupListManager2Action#add()}: the Location List takes no location whose
 * name is longer than a booking's copy of it ({@link LocationList#LABEL_MAX_LENGTH}), while every
 * other list keeps taking any label.
 *
 * @since 2026-10-28
 */
@DisplayName("LookupListManager2Action unit tests")
@Tag("unit")
@Tag("fast")
@Tag("web")
public class LookupListManager2ActionUnitTest extends OpenOUnitTestBase {

    private static final int LOCATION_LIST_ID = 5;
    private static final int OTHER_LIST_ID = 1;

    /** The action keeps its manager in a static field, set when the class first loads, so the tests share one mock. */
    private static final LookupListManager lookupListManager = mock(LookupListManager.class);

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        reset(lookupListManager);
        registerMock(LookupListManager.class, lookupListManager);
        when(lookupListManager.findLookupListById(any(), eq(LOCATION_LIST_ID))).thenReturn(list(LOCATION_LIST_ID, "appointmentLocationCode"));
        when(lookupListManager.findLookupListById(any(), eq(OTHER_LIST_ID))).thenReturn(list(OTHER_LIST_ID, "reasonCode"));
        when(lookupListManager.findLookupListItemsByLookupListId(any(), anyInt())).thenReturn(List.of());

        request = new MockHttpServletRequest("POST", "/lookupListManagerAction.do");
        ActionContext context = new ActionContext(new HashMap<>());
        context.put(ServletActionContext.HTTP_REQUEST, request);
        context.put(ServletActionContext.HTTP_RESPONSE, new MockHttpServletResponse());
        ActionContext.setContext(context);
    }

    @AfterEach
    void clearActionContext() {
        ActionContext.setContext(null);
    }

    private static LookupList list(int id, String name) {
        LookupList list = new LookupList();
        list.setId(id);
        list.setName(name);
        return list;
    }

    /* Posts the Add box as the manager page does. */
    private void add(int listId, String label) {
        request.setParameter("method", "add");
        request.setParameter("lookupListId", String.valueOf(listId));
        request.setParameter("lookupListItemLabel", label);
        new LookupListManager2Action().add();
    }

    private LookupListItem added() {
        ArgumentCaptor<LookupListItem> item = ArgumentCaptor.forClass(LookupListItem.class);
        verify(lookupListManager).addLookupListItem(any(), item.capture());
        return item.getValue();
    }

    @Test
    @DisplayName("should add a location whose name fits a booking's copy of it")
    void shouldAddLocation_whenNameFitsBookingCopy() {
        String name = "x".repeat(LocationList.LABEL_MAX_LENGTH);

        add(LOCATION_LIST_ID, name);

        assertThat(added().getLabel()).isEqualTo(name);
        assertThat(added().getLookupListId()).isEqualTo(LOCATION_LIST_ID);
    }

    @Test
    @DisplayName("should not add a location whose name a booking would cut short")
    void shouldNotAddLocation_whenNameLongerThanBookingCopy() {
        add(LOCATION_LIST_ID, "x".repeat(LocationList.LABEL_MAX_LENGTH + 1));

        verify(lookupListManager, never()).addLookupListItem(any(), any());
    }

    @Test
    @DisplayName("should still add a long label to any other list")
    void shouldAddItem_whenOtherListLabelLong() {
        String label = "x".repeat(LocationList.LABEL_MAX_LENGTH + 1);

        add(OTHER_LIST_ID, label);

        assertThat(added().getLabel()).isEqualTo(label);
    }
}
