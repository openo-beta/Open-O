package ca.openosp.openo.documentManager;

import ca.openosp.openo.commn.dao.FaxConfigDao;
import ca.openosp.openo.commn.model.FaxConfig;
import ca.openosp.openo.managers.DocumentManager;
import ca.openosp.openo.managers.EmailComposeManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DocumentSendButtons#forDocument(LoggedInInfo, EDoc)}, the document view's Fax and Email buttons.
 *
 * <p>The contract: a greyed-out button shows one reason, checked in this order: the document is deleted; it is
 * neither a PDF nor an image; the clinic or the user can't send (no active fax account or no fax write right, or
 * email not set up or no email write right); it isn't filed to a patient. "Unlinked" comes last because saving a
 * patient on the view clears it. The patient's email address and consent are looked up only when Email can be
 * used. The JSON keeps every field, nulls included, under the names the view's script reads.</p>
 *
 * @since 2026-11-25
 */
@DisplayName("DocumentSendButtons: the document view's Fax and Email buttons")
@Tag("unit")
@Tag("fast")
@Tag("document")
class DocumentSendButtonsUnitTest extends OpenOUnitTestBase {

    private static final int DOCUMENT_NO = 15;
    private static final int DEMOGRAPHIC_NO = 3001;
    private static final String CONSENT_NAME = "FAKE email consent";
    private static final String OPTED_IN = "Explicit Opt-In";

    private SecurityInfoManager securityInfoManager;
    private EmailComposeManager emailComposeManager;
    private DocumentManager documentManager;
    private FaxConfigDao faxConfigDao;
    private LoggedInInfo loggedInInfo;

    @BeforeEach
    void setUp() {
        securityInfoManager = createAndRegisterMock(SecurityInfoManager.class);
        emailComposeManager = createAndRegisterMock(EmailComposeManager.class);
        documentManager = createAndRegisterMock(DocumentManager.class);
        faxConfigDao = createAndRegisterMock(FaxConfigDao.class);
        loggedInInfo = new LoggedInInfo();

        // Ready to send: filed to a patient, an active fax account, email set up, and both write rights
        when(documentManager.getLinkedDemographicNo(loggedInInfo, DOCUMENT_NO)).thenReturn(DEMOGRAPHIC_NO);
        givenActiveFaxAccount(true);
        when(emailComposeManager.isEmailEnabled()).thenReturn(true);
        givenWriteRight("_fax", true);
        givenWriteRight("_email", true);
        when(emailComposeManager.hasValidRecipient(loggedInInfo, DEMOGRAPHIC_NO)).thenReturn(true);
        when(emailComposeManager.getEmailConsentStatus(loggedInInfo, DEMOGRAPHIC_NO))
                .thenReturn(new String[]{CONSENT_NAME, OPTED_IN});
    }

    private void givenActiveFaxAccount(boolean active) {
        FaxConfig account = new FaxConfig();
        account.setActive(active);
        when(faxConfigDao.findAll(0, null)).thenReturn(List.of(account));
    }

    private void givenWriteRight(String securityObject, boolean granted) {
        when(securityInfoManager.hasPrivilege(eq(loggedInInfo), eq(securityObject), eq(SecurityInfoManager.WRITE),
                isNull(String.class))).thenReturn(granted);
    }

    private void givenUnlinked() {
        when(documentManager.getLinkedDemographicNo(loggedInInfo, DOCUMENT_NO)).thenReturn(null);
    }

    private static EDoc document(char status, String contentType) {
        EDoc document = new EDoc();
        document.setDocId(String.valueOf(DOCUMENT_NO));
        document.setStatus(status);
        document.setContentType(contentType);
        return document;
    }

    private DocumentSendButtons buttonsFor(EDoc document) {
        return DocumentSendButtons.forDocument(loggedInInfo, document);
    }

    private void verifyNoPatientEmailLookup() {
        verify(emailComposeManager, never()).hasValidRecipient(any(), anyInt());
        verify(emailComposeManager, never()).getEmailConsentStatus(any(), anyInt());
    }

    @Nested
    @DisplayName("Fax")
    class Fax {

        @Test
        @DisplayName("should turn Fax on for a PDF filed to a patient, with the patient to fax for")
        void shouldTurnFaxOn_whenPdfIsFiledToPatient() {
            DocumentSendButtons buttons = buttonsFor(document('A', "application/pdf"));

            assertThat(buttons.faxBlockedReason()).isNull();
            assertThat(buttons.demographicNo()).isEqualTo(DEMOGRAPHIC_NO);
        }

        @Test
        @DisplayName("should turn Fax on for an image, which the fax window wraps in a PDF")
        void shouldTurnFaxOn_whenDocumentIsImage() {
            assertThat(buttonsFor(document('A', "image/png")).faxBlockedReason()).isNull();
        }

        @Test
        @DisplayName("should grey out Fax when the clinic has no active fax account")
        void shouldGreyOutFax_whenNoActiveFaxAccount() {
            givenActiveFaxAccount(false);

            assertThat(buttonsFor(document('A', "application/pdf")).faxBlockedReason())
                    .isEqualTo(DocumentSendButtons.NO_FAX_SENDERS);
        }

        @Test
        @DisplayName("should grey out Fax when the user may not send faxes")
        void shouldGreyOutFax_whenUserCannotWriteFaxes() {
            givenWriteRight("_fax", false);

            assertThat(buttonsFor(document('A', "application/pdf")).faxBlockedReason())
                    .isEqualTo(DocumentSendButtons.NO_FAX_SENDERS);
        }

        @Test
        @DisplayName("should grey out Fax when the document isn't filed to a patient")
        void shouldGreyOutFax_whenDocumentIsUnlinked() {
            givenUnlinked();

            DocumentSendButtons buttons = buttonsFor(document('A', "application/pdf"));

            assertThat(buttons.faxBlockedReason()).isEqualTo(DocumentSendButtons.UNLINKED);
            assertThat(buttons.demographicNo()).isNull();
        }
    }

    @Nested
    @DisplayName("Email")
    class Email {

        @Test
        @DisplayName("should turn Email on with the patient's address check and consent")
        void shouldTurnEmailOn_withPatientEmailDetails() {
            DocumentSendButtons buttons = buttonsFor(document('A', "application/pdf"));

            assertThat(buttons.emailBlockedReason()).isNull();
            assertThat(buttons.hasValidEmail()).isTrue();
            assertThat(buttons.emailConsentName()).isEqualTo(CONSENT_NAME);
            assertThat(buttons.emailConsentStatus()).isEqualTo(OPTED_IN);
        }

        @Test
        @DisplayName("should keep Email on when the patient has no valid address, so the click can say so")
        void shouldKeepEmailOn_whenPatientHasNoValidAddress() {
            when(emailComposeManager.hasValidRecipient(loggedInInfo, DEMOGRAPHIC_NO)).thenReturn(false);

            DocumentSendButtons buttons = buttonsFor(document('A', "application/pdf"));

            assertThat(buttons.emailBlockedReason()).isNull();
            assertThat(buttons.hasValidEmail()).isFalse();
        }

        @Test
        @DisplayName("should grey out Email without looking up the patient when email isn't set up")
        void shouldGreyOutEmail_whenEmailIsNotSetUp() {
            when(emailComposeManager.isEmailEnabled()).thenReturn(false);

            DocumentSendButtons buttons = buttonsFor(document('A', "application/pdf"));

            assertThat(buttons.emailBlockedReason()).isEqualTo(DocumentSendButtons.EMAIL_NOT_SET_UP);
            assertThat(buttons.hasValidEmail()).isFalse();
            assertThat(buttons.emailConsentName()).isEmpty();
            assertThat(buttons.emailConsentStatus()).isEmpty();
            verifyNoPatientEmailLookup();
        }

        @Test
        @DisplayName("should grey out Email without looking up the patient when the user may not send emails")
        void shouldGreyOutEmail_whenUserCannotWriteEmails() {
            givenWriteRight("_email", false);

            assertThat(buttonsFor(document('A', "application/pdf")).emailBlockedReason())
                    .isEqualTo(DocumentSendButtons.EMAIL_NOT_SET_UP);
            verifyNoPatientEmailLookup();
        }

        @Test
        @DisplayName("should grey out Email without looking up a patient when the document isn't filed to one")
        void shouldGreyOutEmail_whenDocumentIsUnlinked() {
            givenUnlinked();

            assertThat(buttonsFor(document('A', "application/pdf")).emailBlockedReason())
                    .isEqualTo(DocumentSendButtons.UNLINKED);
            verifyNoPatientEmailLookup();
        }
    }

    @Nested
    @DisplayName("Order of reasons")
    class OrderOfReasons {

        @Test
        @DisplayName("should say deleted before any other reason")
        void shouldSayDeleted_beforeAnyOtherReason() {
            givenUnlinked();
            givenActiveFaxAccount(false);
            when(emailComposeManager.isEmailEnabled()).thenReturn(false);

            DocumentSendButtons buttons = buttonsFor(document('D', "text/html"));

            assertThat(buttons.faxBlockedReason()).isEqualTo(DocumentSendButtons.DELETED);
            assertThat(buttons.emailBlockedReason()).isEqualTo(DocumentSendButtons.DELETED);
        }

        @Test
        @DisplayName("should say only PDFs and images before the sender and the link")
        void shouldSayNotPdfOrImage_beforeSenderAndLink() {
            givenUnlinked();
            givenActiveFaxAccount(false);
            when(emailComposeManager.isEmailEnabled()).thenReturn(false);

            DocumentSendButtons buttons = buttonsFor(document('H', "text/html"));

            assertThat(buttons.faxBlockedReason()).isEqualTo(DocumentSendButtons.NOT_PDF_OR_IMAGE);
            assertThat(buttons.emailBlockedReason()).isEqualTo(DocumentSendButtons.NOT_PDF_OR_IMAGE);
        }

        @Test
        @DisplayName("should name the sender problem before the link, since saving a patient won't fix it")
        void shouldNameSenderProblem_beforeUnlinked() {
            givenUnlinked();
            givenActiveFaxAccount(false);
            when(emailComposeManager.isEmailEnabled()).thenReturn(false);

            DocumentSendButtons buttons = buttonsFor(document('A', "application/pdf"));

            assertThat(buttons.faxBlockedReason()).isEqualTo(DocumentSendButtons.NO_FAX_SENDERS);
            assertThat(buttons.emailBlockedReason()).isEqualTo(DocumentSendButtons.EMAIL_NOT_SET_UP);
        }
    }

    @Nested
    @DisplayName("JSON for the view's script")
    class Json {

        @Test
        @DisplayName("should write every field under the names the script reads")
        void shouldWriteFieldNamesTheScriptReads() throws Exception {
            JsonNode json = new ObjectMapper().readTree(buttonsFor(document('A', "application/pdf")).toJson());

            assertThat(json.fieldNames()).toIterable().containsExactlyInAnyOrder("faxBlockedReason",
                    "emailBlockedReason", "demographicNo", "hasValidEmail", "emailConsentName", "emailConsentStatus");
            assertThat(json.get("demographicNo").asInt()).isEqualTo(DEMOGRAPHIC_NO);
            assertThat(json.get("hasValidEmail").asBoolean()).isTrue();
            assertThat(json.get("emailConsentStatus").asText()).isEqualTo(OPTED_IN);
        }

        @Test
        @DisplayName("should write a usable button's reason as null, which the script reads as turned on")
        void shouldWriteNullReason_whenButtonIsUsable() throws Exception {
            givenWriteRight("_email", false);

            JsonNode json = new ObjectMapper().readTree(buttonsFor(document('A', "application/pdf")).toJson());

            assertThat(json.get("faxBlockedReason").isNull()).isTrue();
            assertThat(json.get("emailBlockedReason").asText()).isEqualTo(DocumentSendButtons.EMAIL_NOT_SET_UP);
        }
    }
}
