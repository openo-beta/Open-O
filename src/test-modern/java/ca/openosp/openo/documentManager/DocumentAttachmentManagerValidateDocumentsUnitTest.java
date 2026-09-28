package ca.openosp.openo.documentManager;

import ca.openosp.openo.commn.dao.DocumentDao;
import ca.openosp.openo.commn.dao.EFormDataDao;
import ca.openosp.openo.commn.dao.PatientLabRoutingDao;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test matrix for {@link DocumentAttachmentManagerImpl#validateDocumentsBelongToPatient}.
 *
 * <p>Policy exercised here: a "D" (document) attachment is valid when it has
 * an active {@code ctl_document} row that is either (a) demographic-scoped to
 * this patient, or (b) provider-library scoped
 * ({@code module IN ('provider','providers')}) — i.e. the doc lives in some
 * provider's library (public or private eDoc). Provider-library docs are
 * attachable by reference to any chart per PR #2295's model, so demographic-only
 * validation (PR #2405's original shape) rejected them incorrectly on Ocean
 * submission. This matrix pins down the fix. Both branches share the
 * {@code c.status != 'D'} filter, enforced at the DAO level.
 */
@DisplayName("DocumentAttachmentManagerImpl.validateDocumentsBelongToPatient")
@Tag("unit")
@Tag("fast")
@Tag("document")
@Tag("manager")
class DocumentAttachmentManagerValidateDocumentsUnitTest extends OpenOUnitTestBase {

    private static final Integer PATIENT_DEMO_NO = 42;

    // IDs are spread across distinct ranges so a single scenario's stubbing
    // never accidentally satisfies another scenario's expectation.
    private static final Integer PATIENT_DOC_ID = 100;
    private static final Integer PUBLIC_EDOC_ID = 200;
    private static final Integer OWN_PRIVATE_EDOC_ID = 300;
    private static final Integer FOREIGN_PRIVATE_EDOC_ID = 400;
    private static final Integer CROSS_PATIENT_DOC_ID = 500;

    private DocumentDao documentDao;
    private PatientLabRoutingDao patientLabRoutingDao;
    private EFormDataDao eFormDataDao;
    private LoggedInInfo loggedInInfo;
    private DocumentAttachmentManagerImpl manager;

    @BeforeEach
    void setUp() throws Exception {
        documentDao = mock(DocumentDao.class);
        patientLabRoutingDao = mock(PatientLabRoutingDao.class);
        eFormDataDao = mock(EFormDataDao.class);
        loggedInInfo = mock(LoggedInInfo.class);

        manager = new DocumentAttachmentManagerImpl();
        inject("documentDao", documentDao);
        inject("patientLabRoutingDao", patientLabRoutingDao);
        inject("eFormDataDao", eFormDataDao);
    }

    private void inject(String field, Object value) throws Exception {
        Field f = DocumentAttachmentManagerImpl.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(manager, value);
    }

    private boolean validate(String... docs) {
        return manager.validateDocumentsBelongToPatient(loggedInInfo, PATIENT_DEMO_NO, docs);
    }

    // Stubs documentDao to recognize `recognized` as attachable to PATIENT_DEMO_NO
    // — either demographic-scoped for this patient or provider-library scoped.
    // Any doc id outside `recognized` is treated as not found.
    private void attachableDocs(Integer... recognized) {
        when(documentDao.findValidAttachmentDocNos(eq(PATIENT_DEMO_NO), any()))
                .thenAnswer(inv -> filter(inv.getArgument(1), recognized));
    }

    private static List<Integer> filter(List<Integer> requested, Integer[] allowed) {
        List<Integer> allowedList = java.util.Arrays.asList(allowed);
        return requested.stream().filter(allowedList::contains).collect(java.util.stream.Collectors.toList());
    }

    @Nested
    @DisplayName("patient-scoped documents (module = 'demographic')")
    class PatientDocs {

        @Test
        @DisplayName("returns true for a doc whose ctl row is demographic-scoped to this patient")
        void shouldReturnTrue_whenDocBelongsToDemographic() {
            attachableDocs(PATIENT_DOC_ID);
            assertThat(validate("D" + PATIENT_DOC_ID)).isTrue();
        }

        @Test
        @DisplayName("returns false for a doc whose ctl row belongs to a different demographic")
        void shouldReturnFalse_whenDocBelongsToDifferentDemographic() {
            // No recognized rows — doc exists in DB but isn't linked to this demographic
            // and isn't in any provider library either.
            attachableDocs();
            assertThat(validate("D" + CROSS_PATIENT_DOC_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("provider-library documents (module in 'provider','providers')")
    class ProviderDocs {

        @Test
        @DisplayName("returns true for a public provider eDoc attached to the consult")
        void shouldReturnTrue_whenDocIsPublicProviderEDoc() {
            attachableDocs(PUBLIC_EDOC_ID);
            assertThat(validate("D" + PUBLIC_EDOC_ID)).isTrue();
        }

        @Test
        @DisplayName("returns true for a private eDoc from the current provider's library")
        void shouldReturnTrue_whenDocIsOwnPrivateProviderEDoc() {
            attachableDocs(OWN_PRIVATE_EDOC_ID);
            assertThat(validate("D" + OWN_PRIVATE_EDOC_ID)).isTrue();
        }

        @Test
        @DisplayName("returns true for a foreign private eDoc (another provider's library)")
        void shouldReturnTrue_whenDocIsForeignPrivateProviderEDoc() {
            // Policy: provider-library membership is sufficient regardless of which provider owns
            // the doc. Matches PR #2295's reference model — shared private eDocs (clinic-wide forms,
            // etc.) are a supported workflow, and the doc is already viewable on the consult once
            // attached, so downstream Ocean submission doesn't add new exposure. Tighten at this
            // boundary if that policy changes.
            attachableDocs(FOREIGN_PRIVATE_EDOC_ID);
            assertThat(validate("D" + FOREIGN_PRIVATE_EDOC_ID)).isTrue();
        }
    }

    @Nested
    @DisplayName("mixed attachment sets")
    class Mixed {

        @Test
        @DisplayName("returns true when a legitimate mix of patient doc + public eDoc is submitted")
        void shouldReturnTrue_whenMixOfPatientAndProviderDocs() {
            attachableDocs(PATIENT_DOC_ID, PUBLIC_EDOC_ID);
            assertThat(validate("D" + PATIENT_DOC_ID, "D" + PUBLIC_EDOC_ID)).isTrue();
        }

        @Test
        @DisplayName("returns false when any doc is genuinely cross-patient")
        void shouldReturnFalse_whenAnyDocIsCrossPatient() {
            attachableDocs(PATIENT_DOC_ID);
            assertThat(validate("D" + PATIENT_DOC_ID, "D" + CROSS_PATIENT_DOC_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("malformed and empty input")
    class EdgeCases {

        @Test
        @DisplayName("returns false when a typed id has a non-numeric suffix")
        void shouldReturnFalse_whenTypedIdHasNonNumericSuffix() {
            assertThat(validate("DXYZ")).isFalse();
        }

        @Test
        @DisplayName("returns true when the document list is empty (nothing to validate)")
        void shouldReturnTrue_whenNoDocumentsProvided() {
            assertThat(validate()).isTrue();
        }

        @Test
        @DisplayName("skips entries shorter than two characters without failing the whole submission")
        void shouldReturnTrue_whenEntryIsShorterThanTwoCharacters() {
            // A stray "D" with no id should be skipped (length < 2), not treated as malformed.
            assertThat(validate("D")).isTrue();
        }
    }
}
