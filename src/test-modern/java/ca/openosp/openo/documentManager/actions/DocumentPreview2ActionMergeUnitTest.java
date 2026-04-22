package ca.openosp.openo.documentManager.actions;

import ca.openosp.openo.documentManager.EDoc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DocumentPreview2Action#mergeSingleAttachedDoc} — the
 * classification function that decides which section (patient / provider public /
 * provider private) an attached EDoc lands in when the attachment manager reopens
 * a saved consult or eForm. No Spring context; the method is a pure classifier.
 *
 * Covers the full matrix of (module type, public flag, ownership, deleted state)
 * combinations that drive the UI's section placement and "(other provider)" label.
 */
@DisplayName("DocumentPreview2Action.mergeSingleAttachedDoc")
@Tag("unit")
@Tag("document")
class DocumentPreview2ActionMergeUnitTest {

    private static final String CURRENT_PROVIDER = "42";
    private static final String OTHER_PROVIDER = "99";

    private List<EDoc> allDocuments;
    private List<EDoc> providerPrivateDocs;
    private List<EDoc> providerPublicDocs;
    private Set<String> foreignPrivateDocIds;

    @BeforeEach
    void resetSinks() {
        allDocuments = new ArrayList<>();
        providerPrivateDocs = new ArrayList<>();
        providerPublicDocs = new ArrayList<>();
        foreignPrivateDocIds = new HashSet<>();
    }

    @Nested
    @DisplayName("patient docs (module = demographic)")
    class PatientDocs {

        @Test
        @DisplayName("active patient doc is a no-op — already listed in allDocuments")
        void shouldBeNoOp_whenActivePatientDoc() {
            merge(patientDoc("1", 'A'));
            assertAllEmpty();
        }

        @Test
        @DisplayName("deleted patient doc is re-injected into allDocuments so the section still renders it")
        void shouldReInject_whenDeletedPatientDoc() {
            EDoc doc = patientDoc("2", 'D');
            merge(doc);
            assertThat(allDocuments).containsExactly(doc);
            assertThat(providerPrivateDocs).isEmpty();
            assertThat(providerPublicDocs).isEmpty();
            assertThat(foreignPrivateDocIds).isEmpty();
        }
    }

    @Nested
    @DisplayName("public provider docs (module = providers, docPublic = 1)")
    class PublicProviderDocs {

        @Test
        @DisplayName("active public provider doc is a no-op — already listed in providerPublicDocs")
        void shouldBeNoOp_whenActivePublicProviderDoc() {
            merge(providerDoc("3", 'A', true, OTHER_PROVIDER));
            assertAllEmpty();
        }

        @Test
        @DisplayName("deleted public provider doc is re-injected into providerPublicDocs")
        void shouldReInject_whenDeletedPublicProviderDoc() {
            EDoc doc = providerDoc("4", 'D', true, OTHER_PROVIDER);
            merge(doc);
            assertThat(providerPublicDocs).containsExactly(doc);
            assertThat(allDocuments).isEmpty();
            assertThat(providerPrivateDocs).isEmpty();
            assertThat(foreignPrivateDocIds).isEmpty();
        }
    }

    @Nested
    @DisplayName("private provider docs owned by the current provider")
    class OwnPrivateProviderDocs {

        @Test
        @DisplayName("active own private doc is a no-op — already listed in providerPrivateDocs")
        void shouldBeNoOp_whenActiveOwnPrivateDoc() {
            merge(providerDoc("5", 'A', false, CURRENT_PROVIDER));
            assertAllEmpty();
        }

        @Test
        @DisplayName("deleted own private doc is re-injected into providerPrivateDocs (not marked foreign)")
        void shouldReInject_whenDeletedOwnPrivateDoc() {
            EDoc doc = providerDoc("6", 'D', false, CURRENT_PROVIDER);
            merge(doc);
            assertThat(providerPrivateDocs).containsExactly(doc);
            assertThat(foreignPrivateDocIds)
                    .as("doc belongs to current provider, should not be flagged foreign")
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("private provider docs owned by a different provider (cross-provider)")
    class ForeignPrivateProviderDocs {

        @Test
        @DisplayName("active foreign private doc is merged into providerPrivateDocs AND flagged foreign")
        void shouldMergeAndFlag_whenActiveForeignPrivateDoc() {
            EDoc doc = providerDoc("7", 'A', false, OTHER_PROVIDER);
            merge(doc);
            assertThat(providerPrivateDocs).containsExactly(doc);
            assertThat(foreignPrivateDocIds).containsExactly("7");
            assertThat(allDocuments).isEmpty();
            assertThat(providerPublicDocs).isEmpty();
        }

        @Test
        @DisplayName("deleted foreign private doc is merged into providerPrivateDocs AND flagged foreign")
        void shouldMergeAndFlag_whenDeletedForeignPrivateDoc() {
            EDoc doc = providerDoc("8", 'D', false, OTHER_PROVIDER);
            merge(doc);
            assertThat(providerPrivateDocs).containsExactly(doc);
            assertThat(foreignPrivateDocIds).containsExactly("8");
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("no current provider means foreign private docs aren't misclassified as owned")
        void shouldFlagForeign_whenCurrentProviderIsNull() {
            EDoc doc = providerDoc("9", 'A', false, OTHER_PROVIDER);
            DocumentPreview2Action.mergeSingleAttachedDoc(
                    doc, null, allDocuments, providerPrivateDocs, providerPublicDocs, foreignPrivateDocIds);
            assertThat(providerPrivateDocs).containsExactly(doc);
            assertThat(foreignPrivateDocIds).containsExactly("9");
        }

        @Test
        @DisplayName("legacy 'provider' module spelling is treated the same as 'providers'")
        void shouldTreatProviderAndProvidersIdentically() {
            EDoc legacy = new EDoc();
            legacy.setDocId("10");
            legacy.setStatus('A');
            legacy.setModule("provider");
            legacy.setModuleId(OTHER_PROVIDER);
            legacy.setDocPublic("0");

            merge(legacy);

            assertThat(providerPrivateDocs).containsExactly(legacy);
            assertThat(foreignPrivateDocIds).containsExactly("10");
        }
    }

    // --- helpers ---------------------------------------------------------

    private void merge(EDoc doc) {
        DocumentPreview2Action.mergeSingleAttachedDoc(
                doc, CURRENT_PROVIDER, allDocuments, providerPrivateDocs, providerPublicDocs, foreignPrivateDocIds);
    }

    private void assertAllEmpty() {
        assertThat(allDocuments).isEmpty();
        assertThat(providerPrivateDocs).isEmpty();
        assertThat(providerPublicDocs).isEmpty();
        assertThat(foreignPrivateDocIds).isEmpty();
    }

    private static EDoc patientDoc(String docId, char status) {
        EDoc d = new EDoc();
        d.setDocId(docId);
        d.setStatus(status);
        d.setModule("demographic");
        d.setModuleId("2001");
        d.setDocPublic("0");
        return d;
    }

    private static EDoc providerDoc(String docId, char status, boolean isPublic, String ownerProviderNo) {
        EDoc d = new EDoc();
        d.setDocId(docId);
        d.setStatus(status);
        d.setModule("providers");
        d.setModuleId(ownerProviderNo);
        d.setDocPublic(isPublic ? "1" : "0");
        return d;
    }
}
