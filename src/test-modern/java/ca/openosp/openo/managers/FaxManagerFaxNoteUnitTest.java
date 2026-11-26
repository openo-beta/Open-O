package ca.openosp.openo.managers;

import ca.openosp.openo.casemgmt.service.CaseManagementManager;
import ca.openosp.openo.commn.model.Document;
import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.commn.model.FaxJob;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.managers.FaxManager.TransactionType;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link FaxManagerImpl#addFaxNote(LoggedInInfo, TransactionType, int, int, List, boolean, String, String)}:
 * the optional chart note when a consultation request, eForm or document is faxed from the OSCAR Fax window.
 *
 * <p>The contract: the note is saved as a signed note in the patient's chart through
 * {@link CaseManagementManager#saveSignedNote(LoggedInInfo, int, String)}, and isn't linked to what was faxed, since
 * OpenO reads the newest note linked to a document as the annotation. Laid out like the email window's note, it names
 * what was faxed, the recipient and each copy (names as typed), whether a cover page went with its comments, and an
 * internal comment when one was typed, and says the fax was queued, not sent, by the sender's signature name. Only a
 * user who may send faxes and open the patient's record can add it, and only for an eForm or document that still
 * exists.</p>
 *
 * @since 2026-11-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FaxManager fax chart note")
@Tag("unit")
@Tag("fast")
@Tag("manager")
@Tag("fax")
class FaxManagerFaxNoteUnitTest extends OpenOUnitTestBase {

    private static final int DOCUMENT_NO = 15;
    private static final int EFORM_DATA_ID = 55;
    private static final int CONSULTATION_ID = 12;
    private static final int DEMOGRAPHIC_NO = 3001;
    private static final String QUEUED_LINE = "\\[Queued for sending on \\d{2}-[A-Z][a-z]{2}-\\d{4} \\d{1,2}:\\d{2}";

    @Mock
    private SecurityInfoManager securityInfoManager;

    @Mock
    private DocumentManager documentManager;

    @Mock
    private EformDataManager eformDataManager;

    @Mock
    private CaseManagementManager caseManagementManager;

    @InjectMocks
    private FaxManagerImpl faxManager;

    private LoggedInInfo loggedInInfo;

    @BeforeEach
    void setUp() {
        loggedInInfo = new LoggedInInfo();
        loggedInInfo.setLoggedInProvider(new Provider("999998"));

        when(securityInfoManager.hasPrivilege(any(), eq("_fax"), eq(SecurityInfoManager.WRITE), isNull(String.class))).thenReturn(true);
        when(securityInfoManager.isAllowedAccessToPatientRecord(any(), eq(DEMOGRAPHIC_NO))).thenReturn(true);
        Document document = new Document(DOCUMENT_NO);
        document.setDocdesc("FAKE MRI head report");
        when(documentManager.getDocument(any(LoggedInInfo.class), eq(DOCUMENT_NO))).thenReturn(document);
        EFormData eForm = new EFormData();
        eForm.setId(EFORM_DATA_ID);
        eForm.setFormName("FAKE Referral letter");
        when(eformDataManager.findByFdid(any(LoggedInInfo.class), eq(EFORM_DATA_ID))).thenReturn(eForm);
        when(caseManagementManager.getSignatureName(any())).thenReturn("FAKE-Sam Example");
    }

    private static FaxJob job(String recipient, String fax) {
        FaxJob faxJob = new FaxJob();
        faxJob.setRecipient(recipient);
        faxJob.setDestination(fax);
        return faxJob;
    }

    private static List<FaxJob> oneRecipient() {
        return List.of(job("Dr. FAKE Neuro", "5550100144"));
    }

    private String addFaxNote(TransactionType transactionType, int transactionId, List<FaxJob> faxJobs,
                              boolean coverPage, String coverPageComments, String internalComment) {
        faxManager.addFaxNote(loggedInInfo, transactionType, transactionId, DEMOGRAPHIC_NO, faxJobs, coverPage,
                coverPageComments, internalComment);
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(caseManagementManager).saveSignedNote(eq(loggedInInfo), eq(DEMOGRAPHIC_NO), text.capture());
        return text.getValue();
    }

    private String addFaxNote(List<FaxJob> faxJobs, boolean coverPage, String coverPageComments, String internalComment) {
        return addFaxNote(TransactionType.DOCUMENT, DOCUMENT_NO, faxJobs, coverPage, coverPageComments, internalComment);
    }

    private String addFaxNote(boolean coverPage, String coverPageComments, String internalComment) {
        return addFaxNote(oneRecipient(), coverPage, coverPageComments, internalComment);
    }

    @Nested
    @DisplayName("saving")
    class Saving {

        @Test
        @DisplayName("should save a signed note in the patient's chart, not linked to what was faxed")
        void shouldSaveSignedNoteWithoutLink_whenFaxIsQueued() {
            addFaxNote(false, null, null);

            verify(caseManagementManager, never()).saveNoteLink(any());
        }

        @Test
        @DisplayName("should refuse without saving when the user may not send faxes")
        void shouldRefuse_whenUserCannotSendFaxes() {
            when(securityInfoManager.hasPrivilege(any(), eq("_fax"), eq(SecurityInfoManager.WRITE), isNull(String.class))).thenReturn(false);

            assertThatThrownBy(() -> faxManager.addFaxNote(loggedInInfo, TransactionType.DOCUMENT, DOCUMENT_NO,
                    DEMOGRAPHIC_NO, oneRecipient(), false, null, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("_fax");
            verify(caseManagementManager, never()).saveSignedNote(any(), anyInt(), anyString());
        }

        @Test
        @DisplayName("should refuse without saving when the user may not open the patient's record")
        void shouldRefuse_whenUserCannotOpenPatient() {
            when(securityInfoManager.isAllowedAccessToPatientRecord(any(), eq(DEMOGRAPHIC_NO))).thenReturn(false);

            assertThatThrownBy(() -> faxManager.addFaxNote(loggedInInfo, TransactionType.CONSULTATION, CONSULTATION_ID,
                    DEMOGRAPHIC_NO, oneRecipient(), false, null, null))
                    .isInstanceOf(SecurityException.class);
            verify(caseManagementManager, never()).saveSignedNote(any(), anyInt(), anyString());
        }

        @Test
        @DisplayName("should refuse without saving when the document no longer exists")
        void shouldRefuse_whenDocumentIsGone() {
            when(documentManager.getDocument(any(LoggedInInfo.class), eq(DOCUMENT_NO))).thenReturn(null);

            assertThatThrownBy(() -> faxManager.addFaxNote(loggedInInfo, TransactionType.DOCUMENT, DOCUMENT_NO,
                    DEMOGRAPHIC_NO, oneRecipient(), false, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("15");
            verify(caseManagementManager, never()).saveSignedNote(any(), anyInt(), anyString());
        }

        @Test
        @DisplayName("should refuse without saving when the eForm no longer exists")
        void shouldRefuse_whenEFormIsGone() {
            when(eformDataManager.findByFdid(any(LoggedInInfo.class), eq(EFORM_DATA_ID))).thenReturn(null);

            assertThatThrownBy(() -> faxManager.addFaxNote(loggedInInfo, TransactionType.EFORM, EFORM_DATA_ID,
                    DEMOGRAPHIC_NO, oneRecipient(), false, null, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("55");
            verify(caseManagementManager, never()).saveSignedNote(any(), anyInt(), anyString());
        }

        @ParameterizedTest
        @EnumSource(value = TransactionType.class, names = {"FORM", "RX"})
        @DisplayName("should refuse without saving a fax the fax window doesn't send")
        void shouldRefuse_whenFaxWindowDoesNotSendType(TransactionType transactionType) {
            assertThatThrownBy(() -> faxManager.addFaxNote(loggedInInfo, transactionType, 1, DEMOGRAPHIC_NO,
                    oneRecipient(), false, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(caseManagementManager, never()).saveSignedNote(any(), anyInt(), anyString());
        }
    }

    @Nested
    @DisplayName("note text")
    class NoteText {

        @Test
        @DisplayName("should name the document, the recipient then each copy, and say it was queued by the sender")
        void shouldListRecipientThenCopies_whenFaxHasCopies() {
            String text = addFaxNote(List.of(job("Dr. FAKE Neuro", "5550100144"), job("FAKE Clinic", "5550100155"),
                    job("", "5550100166")), false, null, null);

            assertThat(text).matches(Pattern.quote("Faxed document: FAKE MRI head report (ID: 15)\n\n"
                    + "To: Dr. FAKE Neuro (5550100144)\n"
                    + "Copy to: FAKE Clinic (5550100155)\n"
                    + "Copy to: 5550100166\n"
                    + "Cover page: No\n"
                    + "\n") + QUEUED_LINE + " by FAKE-Sam Example\\]");
        }

        @Test
        @DisplayName("should name the eForm by its form name")
        void shouldNameEForm_whenEFormIsFaxed() {
            assertThat(addFaxNote(TransactionType.EFORM, EFORM_DATA_ID, oneRecipient(), false, null, null))
                    .startsWith("Faxed eForm: FAKE Referral letter (ID: 55)\n\nTo: Dr. FAKE Neuro (5550100144)\n");
        }

        @Test
        @DisplayName("should name the consultation request")
        void shouldNameConsultationRequest_whenConsultationIsFaxed() {
            assertThat(addFaxNote(TransactionType.CONSULTATION, CONSULTATION_ID, oneRecipient(), false, null, null))
                    .startsWith("Faxed consultation request (ID: 12)\n\nTo: Dr. FAKE Neuro (5550100144)\n");
        }

        @Test
        @DisplayName("should show a recipient name as typed, since queue stores it HTML-encoded")
        void shouldShowNameAsTyped_whenStoredHtmlEncoded() {
            assertThat(addFaxNote(List.of(job("Dr. O&#39;FAKE &amp; Partners", "5550100144")), false, null, null))
                    .contains("To: Dr. O'FAKE & Partners (5550100144)");
        }

        @Test
        @DisplayName("should include the cover page comments when a cover page went")
        void shouldIncludeCoverPageComments_whenCoverPageWent() {
            assertThat(addFaxNote(true, "  Please call the patient.  ", null))
                    .contains("Cover page: Yes\nCover page comments:\nPlease call the patient.\n");
        }

        @Test
        @DisplayName("should leave out the comments when no cover page went")
        void shouldLeaveOutComments_whenNoCoverPage() {
            assertThat(addFaxNote(false, "Please call the patient.", null))
                    .contains("Cover page: No\n").doesNotContain("Please call");
        }

        @Test
        @DisplayName("should leave out blank cover page comments")
        void shouldLeaveOutComments_whenBlank() {
            assertThat(addFaxNote(true, "   ", null)).contains("Cover page: Yes\n").doesNotContain("Cover page comments");
        }

        @Test
        @DisplayName("should include the internal comment when one was typed")
        void shouldIncludeInternalComment_whenTyped() {
            assertThat(addFaxNote(false, null, " Sent at the patient's request. "))
                    .contains("\n***Internal Comment***\n\nSent at the patient's request.\n");
        }

        @Test
        @DisplayName("should leave out a blank internal comment")
        void shouldLeaveOutInternalComment_whenBlank() {
            assertThat(addFaxNote(false, null, "  ")).doesNotContain("Internal Comment");
        }

        @Test
        @DisplayName("should leave out 'by' when the sender has no name")
        void shouldLeaveOutBy_whenSenderHasNoName() {
            when(caseManagementManager.getSignatureName(any())).thenReturn("");

            assertThat(addFaxNote(false, null, null)).matches("(?s).*\n" + QUEUED_LINE + "\\]");
        }
    }
}
