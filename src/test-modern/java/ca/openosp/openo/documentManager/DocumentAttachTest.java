package ca.openosp.openo.documentManager;

import ca.openosp.openo.commn.dao.ConsultDocsDao;
import ca.openosp.openo.commn.dao.EFormDocsDao;
import ca.openosp.openo.commn.model.ConsultDocs;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentAttachTest extends OpenOUnitTestBase {
    private ConsultDocsDao consultDocsDao;

    @BeforeEach
    void setUp() {
        consultDocsDao = mock(ConsultDocsDao.class);
        registerMock(ConsultDocsDao.class, consultDocsDao);
        registerMock(EFormDocsDao.class, mock(EFormDocsDao.class));
    }

    @Test
    void distinguishesSameNumericIdFromDifferentFormTables() {
        ConsultDocs annual = new ConsultDocs(11, 5, ConsultDocs.DOCTYPE_FORM, "999998");
        annual.setFormTable("formAnnual");
        when(consultDocsDao.findByRequestIdDocType(11, ConsultDocs.DOCTYPE_FORM))
                .thenReturn(List.of(annual));

        new DocumentAttach().attachFormsToConsult(List.of(
                EncounterFormAttachmentKey.of("formAnnual", 5),
                EncounterFormAttachmentKey.of("formGrowthChart", 5)), "999998", 11);

        verify(consultDocsDao, never()).merge(annual);
        assertNull(annual.getDeleted());

        ArgumentCaptor<ConsultDocs> inserted = ArgumentCaptor.forClass(ConsultDocs.class);
        verify(consultDocsDao).persist(inserted.capture());
        assertEquals(5, inserted.getValue().getDocumentNo());
        assertEquals("formGrowthChart", inserted.getValue().getFormTable());
        assertEquals(ConsultDocs.DOCTYPE_FORM, inserted.getValue().getDocType());
    }
}
