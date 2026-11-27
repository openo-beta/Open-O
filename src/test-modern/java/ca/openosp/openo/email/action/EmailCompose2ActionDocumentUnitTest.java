package ca.openosp.openo.email.action;

import ca.openosp.openo.commn.exception.DocumentNotSendableException;
import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.commn.model.EmailAttachment;
import ca.openosp.openo.commn.model.EmailLog.TransactionType;
import ca.openosp.openo.commn.model.enumerator.DocumentType;
import ca.openosp.openo.email.core.EmailComposeStore;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.managers.DocumentManager;
import ca.openosp.openo.managers.EmailComposeManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.PDFGenerationException;
import org.apache.struts2.ServletActionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link EmailCompose2Action#prepareComposeDocumentMailer()}: the OSCAR Email window opened from the
 * document view's Email button.
 *
 * <p>The contract: only a user who may send emails gets the page. The patient comes from the document itself,
 * through the document manager's rule for sending a document, so the email can only go to the patient the document
 * is filed to. The page shows the document as its one attachment, starts with both kinds of encryption on and the
 * patient's default password, and stores its own compose key for Send. When the document can't be emailed, the
 * page shows a fixed reason, never the document's description, and nothing is stored.</p>
 *
 * @since 2026-11-26
 */
@DisplayName("EmailCompose2Action document email")
@Tag("unit")
@Tag("fast")
@Tag("email")
class EmailCompose2ActionDocumentUnitTest extends OpenOUnitTestBase {

    private static final int DOCUMENT_NO = 15;
    private static final int DEMOGRAPHIC_NO = 3001;
    private static final String NOT_EMAILED = "This document cannot be emailed.\\n\\n";

    private SecurityInfoManager securityInfoManager;
    private DocumentManager documentManager;
    private EmailComposeManager emailComposeManager;
    private DemographicManager demographicManager;
    private MockedStatic<ServletActionContext> servletActionContext;
    private MockHttpServletRequest request;
    private MockHttpSession session;
    private LoggedInInfo loggedInInfo;
    private Document document;
    private List<EmailAttachment> attachments;

    @BeforeEach
    void setUp() throws Exception {
        securityInfoManager = createAndRegisterMock(SecurityInfoManager.class);
        documentManager = createAndRegisterMock(DocumentManager.class);
        emailComposeManager = createAndRegisterMock(EmailComposeManager.class);
        demographicManager = createAndRegisterMock(DemographicManager.class);

        loggedInInfo = new LoggedInInfo();
        session = new MockHttpSession();
        session.setAttribute(loggedInInfo.LOGGED_IN_INFO_KEY, loggedInInfo);
        request = new MockHttpServletRequest();
        request.setSession(session);
        request.setParameter("method", "prepareComposeDocumentMailer");
        request.setParameter("documentNo", String.valueOf(DOCUMENT_NO));
        servletActionContext = mockStatic(ServletActionContext.class);
        servletActionContext.when(ServletActionContext::getRequest).thenReturn(request);
        servletActionContext.when(ServletActionContext::getResponse).thenReturn(new MockHttpServletResponse());

        // Ready to email: the user may send emails, and the document is filed to a patient they may open
        when(securityInfoManager.hasPrivilege(eq(loggedInInfo), eq("_email"), eq(SecurityInfoManager.WRITE), isNull(String.class))).thenReturn(true);
        document = new Document(DOCUMENT_NO);
        when(documentManager.getDocument(loggedInInfo, DOCUMENT_NO)).thenReturn(document);
        when(documentManager.getSendableDemographicNo(loggedInInfo, document)).thenReturn(DEMOGRAPHIC_NO);
        attachments = List.of(new EmailAttachment("fake_mri_head.pdf", "/var/lib/OscarDocument/oscar/document/fake_mri_head.pdf",
                DocumentType.DOC, DOCUMENT_NO, 1024L));
        when(emailComposeManager.prepareEDocAttachments(loggedInInfo, new String[]{String.valueOf(DOCUMENT_NO)})).thenReturn(attachments);
        when(emailComposeManager.getEmailConsentStatus(loggedInInfo, DEMOGRAPHIC_NO)).thenReturn(new String[]{"FAKE email consent", "Unknown"});
        when(emailComposeManager.getRecipients(loggedInInfo, DEMOGRAPHIC_NO)).thenReturn(new List<?>[]{List.of("fake@example.com"), List.of()});
        when(emailComposeManager.getAllSenderAccounts()).thenReturn(List.of());
        when(emailComposeManager.createEmailPDFPassword(loggedInInfo, DEMOGRAPHIC_NO)).thenReturn("19600101");
        when(demographicManager.getDemographicFormattedName(loggedInInfo, DEMOGRAPHIC_NO)).thenReturn("FAKE-Ava, FAKE-Walker");
    }

    @AfterEach
    void tearDown() {
        servletActionContext.close();
    }

    private String openEmailWindow() {
        return new EmailCompose2Action().execute();
    }

    private void assertErrorPage(String result, String message) {
        assertThat(result).isEqualTo("compose");
        assertThat(request.getAttribute("isEmailError")).isEqualTo(true);
        assertThat(request.getAttribute("emailErrorMessage")).isEqualTo(NOT_EMAILED + message);
        assertThat(request.getAttribute("composeKey")).isNull();
    }

    @Nested
    @DisplayName("when the document can be emailed")
    class WhenEmailable {

        @Test
        @DisplayName("should address the page to the patient the document is filed to, with the document attached")
        void shouldPreparePage_forPatientDocumentIsFiledTo() throws Exception {
            String result = openEmailWindow();

            assertThat(result).isEqualTo("compose");
            assertThat(request.getAttribute("isEmailError")).isNull();
            assertThat(request.getAttribute("transactionType")).isEqualTo(TransactionType.DOCUMENT);
            assertThat(request.getAttribute("demographicId")).isEqualTo(String.valueOf(DEMOGRAPHIC_NO));
            assertThat(request.getAttribute("receiverEmailList")).isEqualTo(List.of("fake@example.com"));
            assertThat(request.getAttribute("emailAttachmentList")).isEqualTo(attachments);
            verify(emailComposeManager).sanitizeAttachments(attachments);
        }

        @Test
        @DisplayName("should start with both kinds of encryption on and the patient's default password")
        void shouldStartEncrypted_withDefaultPassword() {
            openEmailWindow();

            assertThat(request.getAttribute("isEmailEncrypted")).isEqualTo(true);
            assertThat(request.getAttribute("isEmailAttachmentEncrypted")).isEqualTo(true);
            assertThat(request.getAttribute("emailPDFPassword")).isEqualTo("19600101");
            assertThat(request.getAttribute("emailPDFPasswordClue")).isEqualTo(EmailComposeManager.DEFAULT_PDF_PASSWORD_CLUE);
        }

        @Test
        @DisplayName("should store its own compose key with the patient and the document for Send")
        void shouldStoreComposeKey_withPatientAndDocument() {
            openEmailWindow();

            EmailComposeStore.Entry entry = EmailComposeStore.get(session, (String) request.getAttribute("composeKey"));
            assertThat(entry).isNotNull();
            assertThat(entry.transactionType()).isEqualTo(TransactionType.DOCUMENT);
            assertThat(entry.demographicNo()).isEqualTo(String.valueOf(DEMOGRAPHIC_NO));
            assertThat(entry.attachments()).isEqualTo(attachments);
        }
    }

    @Nested
    @DisplayName("when the document can't be emailed")
    class WhenRefused {

        @Test
        @DisplayName("should refuse before looking at the document when the user may not send emails")
        void shouldRefuse_whenUserCannotSendEmails() {
            when(securityInfoManager.hasPrivilege(eq(loggedInInfo), eq("_email"), eq(SecurityInfoManager.WRITE), isNull(String.class))).thenReturn(false);

            assertThatThrownBy(EmailCompose2ActionDocumentUnitTest.this::openEmailWindow)
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("_email");
            verifyNoInteractions(documentManager);
        }

        @Test
        @DisplayName("should say no document was given when the number is malformed")
        void shouldShowError_whenDocumentNumberIsMalformed() {
            request.setParameter("documentNo", "15abc");

            assertErrorPage(openEmailWindow(), "No document was given.");
            verifyNoInteractions(documentManager);
        }

        @Test
        @DisplayName("should show the send rule's reason, without attaching anything, when the document can't be sent")
        void shouldShowReason_whenDocumentIsNotSendable() throws Exception {
            when(documentManager.getSendableDemographicNo(loggedInInfo, document))
                    .thenThrow(new DocumentNotSendableException("It isn't linked to a patient."));

            assertErrorPage(openEmailWindow(), "It isn't linked to a patient.");
            verify(emailComposeManager, never()).prepareEDocAttachments(any(), any());
        }

        @Test
        @DisplayName("should show a fixed reason, never the document's description, when it can't become a PDF")
        void shouldShowFixedReason_whenDocumentCannotBeRendered() throws Exception {
            when(emailComposeManager.prepareEDocAttachments(loggedInInfo, new String[]{String.valueOf(DOCUMENT_NO)}))
                    .thenThrow(new PDFGenerationException("Error Details: Document [FAKE MRI head report] could not be converted into a PDF"));

            String result = openEmailWindow();

            assertErrorPage(result, "It couldn't be turned into a PDF attachment. Only PDFs and images can be emailed.");
            assertThat((String) request.getAttribute("emailErrorMessage")).doesNotContain("FAKE MRI");
        }
    }
}
