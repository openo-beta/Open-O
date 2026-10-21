package ca.openosp.openo.documentManager.dao;

import ca.openosp.openo.commn.model.ConsultDocs;
import ca.openosp.openo.commn.model.ConsultResponseDoc;
import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.commn.model.EFormDocs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for DocumentDao attached-document queries. These return
 * (Document, AttachmentRow) tuples scoped by consult/eForm/response id. Ctl
 * classification is handled separately in {@code EDocUtil.enrichAttachedWithCtl}
 * and is not part of these queries' contract.
 */
@DisplayName("DocumentDao attached-document queries")
@Tag("integration")
@Tag("document")
@Tag("attached")
@Tag("read")
public class DocumentDaoAttachedFindIntegrationTest extends DocumentDaoBaseIntegrationTest {

    private static final Integer CONSULT_ID = 90001;
    private static final Integer EFORM_FDID = 80001;
    private static final Integer RESPONSE_ID = 70001;
    private static final Integer OWNER_PROVIDER_ID = 42;

    @Nested
    @DisplayName("findDocsAndConsultDocsByConsultId")
    class FindDocsAndConsultDocsByConsultId {

        @Test
        @DisplayName("returns the attached document")
        void shouldReturnAttachedDoc() {
            Integer docNo = persistDocument("Attached doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            persistConsultDocAttachment(CONSULT_ID, docNo);

            List<Object[]> result = documentDao.findDocsAndConsultDocsByConsultId(CONSULT_ID);

            assertThat(result).hasSize(1);
            assertThat(((Document) result.get(0)[0]).getDocumentNo()).isEqualTo(docNo);
        }

        @Test
        @DisplayName("returns attachments even when the doc has no ctl_document row")
        void shouldReturnOrphan_whenDocHasNoCtlRow() {
            Integer docNo = persistDocument("Orphan doc with no ctl row", 0, Document.STATUS_ACTIVE);
            persistConsultDocAttachment(CONSULT_ID, docNo);

            List<Object[]> result = documentDao.findDocsAndConsultDocsByConsultId(CONSULT_ID);

            assertThat(result)
                    .as("2-table query does not depend on CtlDocument, so orphan docs still surface")
                    .hasSize(1);
            assertThat(((Document) result.get(0)[0]).getDocumentNo()).isEqualTo(docNo);
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

        @Test
        @DisplayName("does not return attachments with a non-DOC docType")
        void shouldScopeByDocType() {
            Integer docNo = persistDocument("Lab-typed attachment", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            ConsultDocs cd = new ConsultDocs(CONSULT_ID, docNo, ConsultDocs.DOCTYPE_LAB, "999998");
            cd.setAttachDate(new Date());
            entityManager.persist(cd);
            entityManager.flush();

            List<Object[]> result = documentDao.findDocsAndConsultDocsByConsultId(CONSULT_ID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findDocsAndEFormDocsByFdid")
    class FindDocsAndEFormDocsByFdid {

        @Test
        @DisplayName("returns the attached document")
        void shouldReturnAttachedDoc() {
            Integer docNo = persistDocument("Attached eform doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            persistEFormDocAttachment(EFORM_FDID, docNo);

            List<Object[]> result = documentDao.findDocsAndEFormDocsByFdid(EFORM_FDID);

            assertThat(result).hasSize(1);
            assertThat(((Document) result.get(0)[0]).getDocumentNo()).isEqualTo(docNo);
        }

        @Test
        @DisplayName("returns attachments even when the doc has no ctl_document row")
        void shouldReturnOrphan_whenDocHasNoCtlRow() {
            Integer docNo = persistDocument("Orphan eform doc", 0, Document.STATUS_ACTIVE);
            persistEFormDocAttachment(EFORM_FDID, docNo);

            List<Object[]> result = documentDao.findDocsAndEFormDocsByFdid(EFORM_FDID);

            assertThat(result).hasSize(1);
            assertThat(((Document) result.get(0)[0]).getDocumentNo()).isEqualTo(docNo);
        }

        @Test
        @DisplayName("skips docs marked deleted on the eform_docs row")
        void shouldSkipSoftDeletedAttachments() {
            Integer docNo = persistDocument("Soft-deleted eform attachment", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);

            EFormDocs ed = new EFormDocs(EFORM_FDID, docNo, EFormDocs.DOCTYPE_DOC, "999998");
            ed.setDeleted(EFormDocs.DELETED);
            entityManager.persist(ed);
            entityManager.flush();

            List<Object[]> result = documentDao.findDocsAndEFormDocsByFdid(EFORM_FDID);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("does not return attachments for a different fdid")
        void shouldScopeByFdid() {
            Integer docNo = persistDocument("Other eform doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            persistEFormDocAttachment(EFORM_FDID, docNo);

            List<Object[]> result = documentDao.findDocsAndEFormDocsByFdid(EFORM_FDID + 1);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("does not return attachments with a non-DOC docType")
        void shouldScopeByDocType() {
            Integer docNo = persistDocument("Lab-typed eform attachment", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            EFormDocs ed = new EFormDocs(EFORM_FDID, docNo, EFormDocs.DOCTYPE_LAB, "999998");
            ed.setAttachDate(new Date());
            entityManager.persist(ed);
            entityManager.flush();

            List<Object[]> result = documentDao.findDocsAndEFormDocsByFdid(EFORM_FDID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findDocsAndConsultResponseDocsByConsultId")
    class FindDocsAndConsultResponseDocsByConsultId {

        @Test
        @DisplayName("returns the attached document")
        void shouldReturnAttachedDoc() {
            Integer docNo = persistDocument("Attached response doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            persistConsultResponseDocAttachment(RESPONSE_ID, docNo);

            List<Object[]> result = documentDao.findDocsAndConsultResponseDocsByConsultId(RESPONSE_ID);

            assertThat(result).hasSize(1);
            assertThat(((Document) result.get(0)[0]).getDocumentNo()).isEqualTo(docNo);
        }

        @Test
        @DisplayName("skips docs marked deleted on the consult_response_doc row")
        void shouldSkipSoftDeletedAttachments() {
            Integer docNo = persistDocument("Soft-deleted response attachment", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);

            ConsultResponseDoc crd = new ConsultResponseDoc(RESPONSE_ID, docNo, ConsultResponseDoc.DOCTYPE_DOC, "999998");
            crd.setDeleted(ConsultResponseDoc.DELETED);
            entityManager.persist(crd);
            entityManager.flush();

            List<Object[]> result = documentDao.findDocsAndConsultResponseDocsByConsultId(RESPONSE_ID);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("does not return attachments for a different response id")
        void shouldScopeByResponseId() {
            Integer docNo = persistDocument("Other response doc", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            persistConsultResponseDocAttachment(RESPONSE_ID, docNo);

            List<Object[]> result = documentDao.findDocsAndConsultResponseDocsByConsultId(RESPONSE_ID + 1);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("does not return attachments with a non-DOC docType")
        void shouldScopeByDocType() {
            Integer docNo = persistDocument("Lab-typed response attachment", 0, Document.STATUS_ACTIVE);
            persistCtlDocument(docNo, "provider", OWNER_PROVIDER_ID);
            ConsultResponseDoc crd = new ConsultResponseDoc(RESPONSE_ID, docNo, ConsultResponseDoc.DOCTYPE_LAB, "999998");
            crd.setAttachDate(new Date());
            entityManager.persist(crd);
            entityManager.flush();

            List<Object[]> result = documentDao.findDocsAndConsultResponseDocsByConsultId(RESPONSE_ID);

            assertThat(result).isEmpty();
        }
    }
}
