package ca.openosp.openo.managers;

import ca.openosp.openo.commn.dao.CtlDocumentDao;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.commn.exception.DocumentNotSendableException;
import ca.openosp.openo.commn.model.CtlDocument;
import ca.openosp.openo.commn.model.CtlDocumentPK;
import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.commn.model.Provider;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests for the document manager's rule for sending a document from the document view:
 * {@link DocumentManagerImpl#getLinkedDemographicNo(LoggedInInfo, Integer)} and
 * {@link DocumentManagerImpl#getSendableDemographicNo(LoggedInInfo, Document)}.
 *
 * <p>The contract: a document has one {@code ctl_document} link, and counts as filed to a patient only when
 * that link is a demographic link to a real patient. An uploaded document is linked to patient -1 until staff
 * save a patient, and a document in a provider's inbox is linked to that provider. A document can be faxed or
 * emailed only for the patient it is filed to, by a user who may read that patient's documents and open the
 * patient's record, and only when it isn't deleted. The deleted check comes after the access checks.</p>
 *
 * @since 2026-11-25
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DocumentManager rule for sending a document")
@Tag("unit")
@Tag("fast")
@Tag("manager")
@Tag("document")
class DocumentManagerSendableDocumentUnitTest extends OpenOUnitTestBase {

    private static final int DOCUMENT_NO = 15;
    private static final int DEMOGRAPHIC_NO = 3001;

    @Mock
    private SecurityInfoManager securityInfoManager;

    @Mock
    private CtlDocumentDao ctlDocumentDao;

    @InjectMocks
    private DocumentManagerImpl documentManager;

    private LoggedInInfo loggedInInfo;

    private MockedStatic<LogAction> logActionMock;

    @BeforeEach
    void setUp() {
        // LogAction's static initializer gets OscarLogDao from SpringUtils, so register it before mocking LogAction
        registerMock(OscarLogDao.class, mock(OscarLogDao.class));
        logActionMock = mockStatic(LogAction.class);
        loggedInInfo = new LoggedInInfo();
        loggedInInfo.setLoggedInProvider(new Provider("999998"));
        when(securityInfoManager.hasPrivilege(any(), eq("_edoc"), eq("r"), anyString())).thenReturn(true);
        when(securityInfoManager.hasPrivilege(any(), eq("_edoc"), eq(SecurityInfoManager.READ), anyInt())).thenReturn(true);
        when(securityInfoManager.isAllowedAccessToPatientRecord(any(), eq(DEMOGRAPHIC_NO))).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        logActionMock.close();
    }

    private void givenLink(String module, int moduleId) {
        CtlDocument link = new CtlDocument();
        link.setId(new CtlDocumentPK(module, moduleId, DOCUMENT_NO));
        when(ctlDocumentDao.getCtrlDocument(DOCUMENT_NO)).thenReturn(link);
    }

    private static Document document(char status) {
        Document document = new Document(DOCUMENT_NO);
        document.setStatus(status);
        return document;
    }

    @Nested
    @DisplayName("getLinkedDemographicNo")
    class GetLinkedDemographicNo {

        @Test
        @DisplayName("should return the patient when the document is filed to a patient")
        void shouldReturnPatient_whenDocumentIsFiledToPatient() {
            givenLink("demographic", DEMOGRAPHIC_NO);

            assertThat(documentManager.getLinkedDemographicNo(loggedInInfo, DOCUMENT_NO)).isEqualTo(DEMOGRAPHIC_NO);
        }

        @Test
        @DisplayName("should return null when an uploaded document has no patient saved yet")
        void shouldReturnNull_whenDocumentIsLinkedToNoPatient() {
            givenLink("demographic", -1);

            assertThat(documentManager.getLinkedDemographicNo(loggedInInfo, DOCUMENT_NO)).isNull();
        }

        @Test
        @DisplayName("should return null when the document is in a provider's inbox")
        void shouldReturnNull_whenDocumentIsInProviderInbox() {
            givenLink("providers", 999998);

            assertThat(documentManager.getLinkedDemographicNo(loggedInInfo, DOCUMENT_NO)).isNull();
        }

        @Test
        @DisplayName("should return null when the document has no link")
        void shouldReturnNull_whenDocumentHasNoLink() {
            when(ctlDocumentDao.getCtrlDocument(DOCUMENT_NO)).thenReturn(null);

            assertThat(documentManager.getLinkedDemographicNo(loggedInInfo, DOCUMENT_NO)).isNull();
        }

        @Test
        @DisplayName("should refuse when the user can't read documents")
        void shouldThrow_whenUserCannotReadDocuments() {
            when(securityInfoManager.hasPrivilege(any(), eq("_edoc"), eq("r"), anyString())).thenReturn(false);
            givenLink("demographic", DEMOGRAPHIC_NO);

            assertThatThrownBy(() -> documentManager.getLinkedDemographicNo(loggedInInfo, DOCUMENT_NO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("_edoc");
        }

        @Test
        @DisplayName("should not write an audit row, since the document view looks the link up on every load")
        void shouldNotWriteAuditRow_whenLookingUpLink() {
            givenLink("demographic", DEMOGRAPHIC_NO);

            documentManager.getLinkedDemographicNo(loggedInInfo, DOCUMENT_NO);

            logActionMock.verifyNoInteractions();
        }
    }

    @Nested
    @DisplayName("getSendableDemographicNo")
    class GetSendableDemographicNo {

        @Test
        @DisplayName("should return the patient when an active document is filed to a patient the user may open")
        void shouldReturnPatient_whenDocumentIsSendable() throws Exception {
            givenLink("demographic", DEMOGRAPHIC_NO);

            assertThat(documentManager.getSendableDemographicNo(loggedInInfo, document('A'))).isEqualTo(DEMOGRAPHIC_NO);
        }

        @Test
        @DisplayName("should refuse as deleted when the document wasn't found")
        void shouldRefuseAsDeleted_whenDocumentIsMissing() {
            assertThatThrownBy(() -> documentManager.getSendableDemographicNo(loggedInInfo, null))
                    .isInstanceOf(DocumentNotSendableException.class)
                    .hasMessage("It has been deleted.");
        }

        @Test
        @DisplayName("should refuse as deleted when the document is deleted")
        void shouldRefuseAsDeleted_whenDocumentIsDeleted() {
            givenLink("demographic", DEMOGRAPHIC_NO);

            assertThatThrownBy(() -> documentManager.getSendableDemographicNo(loggedInInfo, document('D')))
                    .isInstanceOf(DocumentNotSendableException.class)
                    .hasMessage("It has been deleted.");
        }

        @Test
        @DisplayName("should refuse as unlinked when an uploaded document has no patient saved yet")
        void shouldRefuseAsUnlinked_whenDocumentIsLinkedToNoPatient() {
            givenLink("demographic", -1);

            assertThatThrownBy(() -> documentManager.getSendableDemographicNo(loggedInInfo, document('A')))
                    .isInstanceOf(DocumentNotSendableException.class)
                    .hasMessage("It isn't linked to a patient.");
        }

        @Test
        @DisplayName("should refuse as unlinked when the document is in a provider's inbox")
        void shouldRefuseAsUnlinked_whenDocumentIsInProviderInbox() {
            givenLink("providers", 999998);

            assertThatThrownBy(() -> documentManager.getSendableDemographicNo(loggedInInfo, document('A')))
                    .isInstanceOf(DocumentNotSendableException.class)
                    .hasMessage("It isn't linked to a patient.");
        }

        @Test
        @DisplayName("should refuse when the user can't read this patient's documents")
        void shouldThrowSecurityException_whenUserCannotReadPatientDocuments() {
            givenLink("demographic", DEMOGRAPHIC_NO);
            when(securityInfoManager.hasPrivilege(any(), eq("_edoc"), eq(SecurityInfoManager.READ), anyInt())).thenReturn(false);

            assertThatThrownBy(() -> documentManager.getSendableDemographicNo(loggedInInfo, document('A')))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("_edoc");
        }

        @Test
        @DisplayName("should refuse when the user can't open the patient's record")
        void shouldThrowSecurityException_whenUserCannotOpenPatientRecord() {
            givenLink("demographic", DEMOGRAPHIC_NO);
            when(securityInfoManager.isAllowedAccessToPatientRecord(any(), eq(DEMOGRAPHIC_NO))).thenReturn(false);

            assertThatThrownBy(() -> documentManager.getSendableDemographicNo(loggedInInfo, document('A')))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("should not say a document is deleted to a user who can't open its patient")
        void shouldThrowSecurityException_beforeSayingDeleted() {
            givenLink("demographic", DEMOGRAPHIC_NO);
            when(securityInfoManager.isAllowedAccessToPatientRecord(any(), eq(DEMOGRAPHIC_NO))).thenReturn(false);

            assertThatThrownBy(() -> documentManager.getSendableDemographicNo(loggedInInfo, document('D')))
                    .isInstanceOf(SecurityException.class);
        }
    }
}
