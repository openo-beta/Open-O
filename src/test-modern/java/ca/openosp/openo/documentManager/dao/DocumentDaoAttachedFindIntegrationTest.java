package ca.openosp.openo.documentManager.dao;

import ca.openosp.openo.commn.model.ConsultDocs;
import ca.openosp.openo.commn.model.CtlDocument;
import ca.openosp.openo.commn.model.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DocumentDao attached-document queries that rely on
 * the (Document, ConsultDocs|EFormDocs|ConsultResponseDoc, CtlDocument) join.
 *
 * The key scenario under test: a single document_no bound to multiple
 * ctl_document rows (e.g., provider-owned doc that has also been attached
 * to a patient document record). The join fans out per ctl row and the
 * caller has to reconcile which (module, moduleId) "wins" when building
 * the EDoc view model.
 */
@DisplayName("DocumentDao attached-document queries")
@Tag("integration")
@Tag("document")
@Tag("attached")
@Tag("read")
public class DocumentDaoAttachedFindIntegrationTest extends DocumentDaoBaseIntegrationTest {

    private static final Integer CONSULT_ID = 90001;
    private static final Integer OWNER_PROVIDER_ID = 42;
    private static final Integer PATIENT_DEMO_ID = 2001;

    @Nested
    @DisplayName("findDocsAndConsultDocsByConsultId")
    class FindDocsAndConsultDocsByConsultId {

        @Test
        @DisplayName("returns one tuple when a document has a single ctl_document row")
        void shouldReturnSingleTuple_whenDocHasOneCtlRow() {
            Integer docNo = persistDocument("Provider-only doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            persistConsultDocAttachment(CONSULT_ID, docNo);

            List<Object[]> result = documentDao.findDocsAndConsultDocsByConsultId(CONSULT_ID);

            assertThat(result).hasSize(1);
            CtlDocument ctl = (CtlDocument) result.get(0)[2];
            assertThat(ctl.getId().getModule()).isEqualTo("provider");
            assertThat(ctl.getId().getModuleId()).isEqualTo(OWNER_PROVIDER_ID);
        }

        @Test
        @DisplayName("fans out into multiple tuples when a document has multiple ctl_document rows")
        void shouldReturnMultipleTuples_whenDocHasMultipleCtlRows() {
            Integer docNo = persistDocument("Shared provider doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            persistCtlDocument(docNo, "demographic", PATIENT_DEMO_ID);
            persistConsultDocAttachment(CONSULT_ID, docNo);

            List<Object[]> result = documentDao.findDocsAndConsultDocsByConsultId(CONSULT_ID);

            assertThat(result)
                    .as("Expected one tuple per ctl_document row for the same documentNo")
                    .hasSize(2);

            Set<String> modules = result.stream()
                    .map(row -> ((CtlDocument) row[2]).getId().getModule())
                    .collect(Collectors.toSet());
            assertThat(modules).containsExactlyInAnyOrder("provider", "demographic");
        }

        @Test
        @DisplayName("first-tuple is always the 'demographic' ctl row when both demographic and provider bindings exist")
        void shouldAlwaysReturnDemographicFirst_whenDocHasProviderAndDemographicCtlRows() {
            Integer docA = persistDocument("Provider-inserted-first doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docA, "provider", OWNER_PROVIDER_ID);
            persistCtlDocument(docA, "demographic", PATIENT_DEMO_ID);
            persistConsultDocAttachment(CONSULT_ID, docA);

            Integer otherConsult = CONSULT_ID + 1000;
            Integer docB = persistDocument("Demographic-inserted-first doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docB, "demographic", PATIENT_DEMO_ID + 1);
            persistCtlDocument(docB, "provider", OWNER_PROVIDER_ID + 1);
            persistConsultDocAttachment(otherConsult, docB);

            String firstModuleA = ((CtlDocument) documentDao.findDocsAndConsultDocsByConsultId(CONSULT_ID).get(0)[2])
                    .getId().getModule();
            String firstModuleB = ((CtlDocument) documentDao.findDocsAndConsultDocsByConsultId(otherConsult).get(0)[2])
                    .getId().getModule();

            // H2 orders by composite PK (module, module_id, document_no), so 'demographic'
            // always precedes 'provider' alphabetically. A "first-wins" dedup in the caller
            // therefore classifies provider-owned docs as patient docs whenever the doc
            // also has a demographic ctl_document binding. This is the misclassification
            // bug the EDocUtil.listDocs dedup needs to solve.
            assertThat(firstModuleA).isEqualTo("demographic");
            assertThat(firstModuleB).isEqualTo("demographic");
        }

        @Test
        @DisplayName("skips docs marked deleted on the consultdocs row")
        void shouldSkipSoftDeletedAttachments() {
            Integer docNo = persistDocument("Soft-deleted attachment", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);

            ConsultDocs cd = new ConsultDocs(CONSULT_ID, docNo, ConsultDocs.DOCTYPE_DOC, "999998");
            cd.setDeleted(ConsultDocs.DELETED);
            entityManager.persist(cd);
            entityManager.flush();

            List<Object[]> result = documentDao.findDocsAndConsultDocsByConsultId(CONSULT_ID);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("does not return attachments for a different consult id")
        void shouldScopeByRequestId() {
            Integer docNo = persistDocument("Other consult doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            persistConsultDocAttachment(CONSULT_ID, docNo);

            List<Object[]> result = documentDao.findDocsAndConsultDocsByConsultId(CONSULT_ID + 1);

            assertThat(result).isEmpty();
        }
    }
}
