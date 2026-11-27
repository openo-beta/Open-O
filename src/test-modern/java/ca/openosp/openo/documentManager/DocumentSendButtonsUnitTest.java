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
 * neither a PDF nor an image; the clinic hasn't set up sending (no active fax account, or email not set up); the
 * user doesn't have the write right to send; it isn't filed to a patient; the user can't open that patient's record
 * (document read for the patient and chart access, as the fax and email windows require; Email also needs the
 * patient read right). "Unlinked" comes after the reasons that saving a patient won't fix. The patient's email
 * address and consent are looked up only when Email can be used. The JSON keeps every field, nulls included,
 * under the names the view's script reads, and when the buttons can't be worked out both are greyed out so the
 * view still loads.</p>
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

        // Ready to send: filed to a patient the user may open, an active fax account, email set up, both write rights
        when(documentManager.getLinkedDemographicNo(loggedInInfo, DOCUMENT_NO)).thenReturn(DEMOGRAPHIC_NO);
        givenActiveFaxAccount(true);
        when(emailComposeManager.isEmailEnabled()).thenReturn(true);
        givenWriteRight("_fax", true);
        givenWriteRight("_email", true);
        givenPatientRight("_edoc", true);
        givenPatientRight("_demographic", true);
        when(securityInfoManager.isAllowedAccessToPatientRecord(loggedInInfo, DEMOGRAPHIC_NO)).thenReturn(true);
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

    private void givenPatientRight(String securityObject, boolean granted) {
        when(securityInfoManager.hasPrivilege(eq(loggedInInfo), eq(securityObject), eq(SecurityInfoManager.READ),
                eq(DEMOGRAPHIC_NO))).thenReturn(granted);
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
        @DisplayName("should grey out Fax with a permission tip when the user may not send faxes")
        void shouldGreyOutFaxWithPermissionTip_whenUserCannotWriteFaxes() {
            givenWriteRight("_fax", false);

            assertThat(buttonsFor(document('A', "application/pdf")).faxBlockedReason())
                    .isEqualTo(DocumentSendButtons.NO_FAX_RIGHT);
        }

        @Test
        @DisplayName("should grey out Fax when the user can't open the patient's record")
        void shouldGreyOutFax_whenUserCannotOpenPatient() {
            when(securityInfoManager.isAllowedAccessToPatientRecord(loggedInInfo, DEMOGRAPHIC_NO)).thenReturn(false);

            assertThat(buttonsFor(document('A', "application/pdf")).faxBlockedReason())
                    .isEqualTo(DocumentSendButtons.NO_PATIENT_ACCESS);
        }

        @Test
        @DisplayName("should grey out Fax when the user may not read the patient's documents")
        void shouldGreyOutFax_whenUserCannotReadPatientDocuments() {
            givenPatientRight("_edoc", false);

            assertThat(buttonsFor(document('A', "application/pdf")).faxBlockedReason())
                    .isEqualTo(DocumentSendButtons.NO_PATIENT_ACCESS);
        }

        @Test
        @DisplayName("should keep Fax on when only the patient read right is missing, since the fax window doesn't use it")
        void shouldKeepFaxOn_whenOnlyPatientReadRightIsMissing() {
            givenPatientRight("_demographic", false);

            assertThat(buttonsFor(document('A', "application/pdf")).faxBlockedReason()).isNull();
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
        @DisplayName("should grey out Email with a permission tip, without looking up the patient, when the user may not send emails")
        void shouldGreyOutEmailWithPermissionTip_whenUserCannotWriteEmails() {
            givenWriteRight("_email", false);

            assertThat(buttonsFor(document('A', "application/pdf")).emailBlockedReason())
                    .isEqualTo(DocumentSendButtons.NO_EMAIL_RIGHT);
            verifyNoPatientEmailLookup();
        }

        @Test
        @DisplayName("should grey out Email without looking up the patient when the user can't open the patient's record")
        void shouldGreyOutEmail_whenUserCannotOpenPatient() {
            when(securityInfoManager.isAllowedAccessToPatientRecord(loggedInInfo, DEMOGRAPHIC_NO)).thenReturn(false);

            assertThat(buttonsFor(document('A', "application/pdf")).emailBlockedReason())
                    .isEqualTo(DocumentSendButtons.NO_PATIENT_ACCESS);
            verifyNoPatientEmailLookup();
        }

        @Test
        @DisplayName("should grey out Email without looking up the patient when the user may not read the patient's details")
        void shouldGreyOutEmail_whenUserCannotReadPatientDetails() {
            givenPatientRight("_demographic", false);

            assertThat(buttonsFor(document('A', "application/pdf")).emailBlockedReason())
                    .isEqualTo(DocumentSendButtons.NO_PATIENT_ACCESS);
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

        @Test
        @DisplayName("should name the clinic's setup before the user's permission")
        void shouldNameSetup_beforePermission() {
            givenActiveFaxAccount(false);
            when(emailComposeManager.isEmailEnabled()).thenReturn(false);
            givenWriteRight("_fax", false);
            givenWriteRight("_email", false);

            DocumentSendButtons buttons = buttonsFor(document('A', "application/pdf"));

            assertThat(buttons.faxBlockedReason()).isEqualTo(DocumentSendButtons.NO_FAX_SENDERS);
            assertThat(buttons.emailBlockedReason()).isEqualTo(DocumentSendButtons.EMAIL_NOT_SET_UP);
        }

        @Test
        @DisplayName("should name the user's permission before the link")
        void shouldNamePermission_beforeUnlinked() {
            givenUnlinked();
            givenWriteRight("_fax", false);
            givenWriteRight("_email", false);

            DocumentSendButtons buttons = buttonsFor(document('A', "application/pdf"));

            assertThat(buttons.faxBlockedReason()).isEqualTo(DocumentSendButtons.NO_FAX_RIGHT);
            assertThat(buttons.emailBlockedReason()).isEqualTo(DocumentSendButtons.NO_EMAIL_RIGHT);
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
            assertThat(json.get("emailBlockedReason").asText()).isEqualTo(DocumentSendButtons.NO_EMAIL_RIGHT);
        }

        @Test
        @DisplayName("should write the buttons for the document view")
        void shouldWriteButtons_forDocumentView() throws Exception {
            JsonNode json = new ObjectMapper().readTree(DocumentSendButtons.jsonFor(loggedInInfo, document('A', "application/pdf")));

            assertThat(json.get("faxBlockedReason").isNull()).isTrue();
            assertThat(json.get("emailBlockedReason").isNull()).isTrue();
            assertThat(json.get("demographicNo").asInt()).isEqualTo(DEMOGRAPHIC_NO);
        }

        @Test
        @DisplayName("should grey out both buttons, so the document view still loads, when a lookup fails")
        void shouldGreyOutBoth_whenLookupFails() throws Exception {
            when(emailComposeManager.getEmailConsentStatus(loggedInInfo, DEMOGRAPHIC_NO))
                    .thenThrow(new RuntimeException("Unauthorised Access. Object[_demographic]"));

            JsonNode json = new ObjectMapper().readTree(DocumentSendButtons.jsonFor(loggedInInfo, document('A', "application/pdf")));

            assertThat(json.get("faxBlockedReason").asText()).isEqualTo(DocumentSendButtons.UNAVAILABLE);
            assertThat(json.get("emailBlockedReason").asText()).isEqualTo(DocumentSendButtons.UNAVAILABLE);
            assertThat(json.get("demographicNo").isNull()).isTrue();
        }
    }
}
