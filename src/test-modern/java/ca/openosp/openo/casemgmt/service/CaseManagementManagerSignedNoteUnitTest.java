package ca.openosp.openo.casemgmt.service;

import ca.openosp.openo.PMmodule.model.ProgramProvider;
import ca.openosp.openo.PMmodule.service.ProgramManager;
import ca.openosp.openo.casemgmt.dao.CaseManagementNoteDAO;
import ca.openosp.openo.casemgmt.dao.CaseManagementNoteLinkDAO;
import ca.openosp.openo.casemgmt.model.CaseManagementNote;
import ca.openosp.openo.casemgmt.model.ProviderExt;
import ca.openosp.openo.commn.dao.ProviderExtDao;
import ca.openosp.openo.commn.dao.SecRoleDao;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.SecRole;
import ca.openosp.openo.encounter.data.EctProgram;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the notes OpenO writes on a provider's behalf, such as the email and fax notes:
 * {@link CaseManagementManagerImpl#saveSignedNote(LoggedInInfo, int, String)} and
 * {@link CaseManagementManagerImpl#getSignatureName(Provider)}.
 *
 * <p>The contract: the note goes in the given patient's chart, written and signed by the logged-in provider, in
 * their current program and their role there, or the doctor role when they have none; its history is its text and
 * its team is "0". A provider signs with their signature when they set one, otherwise their full name.</p>
 *
 * @since 2026-11-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CaseManagementManager signed notes")
@Tag("unit")
@Tag("fast")
@Tag("manager")
@Tag("casemgmt")
class CaseManagementManagerSignedNoteUnitTest extends OpenOUnitTestBase {

    private static final int DEMOGRAPHIC_NO = 3001;
    private static final String PROVIDER_NO = "999998";
    private static final String PROGRAM_ID = "10034";

    @Mock
    private CaseManagementNoteDAO caseManagementNoteDAO;

    @Mock
    private ProgramManager programManager;

    @Mock
    private SecRoleDao secRoleDao;

    @Mock
    private ProviderExtDao providerExtDao;

    @InjectMocks
    private CaseManagementManagerImpl caseManagementManager;

    private Provider provider;

    private LoggedInInfo loggedInInfo;

    private MockedConstruction<EctProgram> ectProgram;

    @BeforeEach
    void setUp() {
        provider = new Provider(PROVIDER_NO);
        provider.setFirstName("FAKE-Sam");
        provider.setLastName("Example");
        loggedInInfo = new LoggedInInfo();
        loggedInInfo.setLoggedInProvider(provider);

        // A new CaseManagementNote gets its link DAO from SpringUtils
        registerMock(CaseManagementNoteLinkDAO.class, mock(CaseManagementNoteLinkDAO.class));
        // EctProgram reads the provider's programs from the web context, which a unit test doesn't have
        ectProgram = mockConstruction(EctProgram.class, (program, context) -> when(program.getProgram(PROVIDER_NO)).thenReturn(PROGRAM_ID));

        ProgramProvider programProvider = new ProgramProvider();
        programProvider.setRoleId(7L);
        when(programManager.getProgramProvider(PROVIDER_NO, PROGRAM_ID)).thenReturn(programProvider);
        when(caseManagementNoteDAO.saveAndReturn(any())).thenReturn(774L);
    }

    @AfterEach
    void tearDown() {
        ectProgram.close();
    }

    private CaseManagementNote savedNote() {
        ArgumentCaptor<CaseManagementNote> note = ArgumentCaptor.forClass(CaseManagementNote.class);
        verify(caseManagementNoteDAO).saveAndReturn(note.capture());
        return note.getValue();
    }

    @Nested
    @DisplayName("saveSignedNote")
    class SaveSignedNote {

        @Test
        @DisplayName("should save the text as a note signed by the provider in their program and role")
        void shouldSaveSignedNote_inProviderProgramAndRole() {
            Long noteId = caseManagementManager.saveSignedNote(loggedInInfo, DEMOGRAPHIC_NO, "FAKE note text");

            assertThat(noteId).isEqualTo(774L);
            CaseManagementNote note = savedNote();
            assertThat(note.getDemographic_no()).isEqualTo(String.valueOf(DEMOGRAPHIC_NO));
            assertThat(note.getNote()).isEqualTo("FAKE note text");
            assertThat(note.getHistory()).isEqualTo("FAKE note text");
            assertThat(note.getProviderNo()).isEqualTo(PROVIDER_NO);
            assertThat(note.isSigned()).isTrue();
            assertThat(note.getSigning_provider_no()).isEqualTo(PROVIDER_NO);
            assertThat(note.getProgram_no()).isEqualTo(PROGRAM_ID);
            assertThat(note.getReporter_caisi_role()).isEqualTo("7");
            assertThat(note.getReporter_program_team()).isEqualTo("0");
            assertThat(note.getUpdate_date()).isNotNull().isEqualTo(note.getObservation_date());
        }

        @Test
        @DisplayName("should use the doctor role when the provider has no role in the program")
        void shouldUseDoctorRole_whenProviderHasNoProgramRole() {
            when(programManager.getProgramProvider(PROVIDER_NO, PROGRAM_ID)).thenReturn(null);
            SecRole doctor = mock(SecRole.class);
            when(doctor.getId()).thenReturn(2);
            when(secRoleDao.findByName("doctor")).thenReturn(doctor);

            caseManagementManager.saveSignedNote(loggedInInfo, DEMOGRAPHIC_NO, "FAKE note text");

            assertThat(savedNote().getReporter_caisi_role()).isEqualTo("2");
        }
    }

    @Nested
    @DisplayName("getSignatureName")
    class GetSignatureName {

        @Test
        @DisplayName("should return the signature, trimmed, when the provider set one")
        void shouldReturnSignature_whenSet() {
            ProviderExt providerExt = new ProviderExt();
            providerExt.setSignature("  Dr. FAKE-Sam Example, MD  ");
            when(providerExtDao.find(PROVIDER_NO)).thenReturn(providerExt);

            assertThat(caseManagementManager.getSignatureName(provider)).isEqualTo("Dr. FAKE-Sam Example, MD");
        }

        @Test
        @DisplayName("should return the full name when the signature is blank")
        void shouldReturnFullName_whenSignatureIsBlank() {
            ProviderExt providerExt = new ProviderExt();
            providerExt.setSignature("   ");
            when(providerExtDao.find(PROVIDER_NO)).thenReturn(providerExt);

            assertThat(caseManagementManager.getSignatureName(provider)).isEqualTo("FAKE-Sam Example");
        }

        @Test
        @DisplayName("should return the full name when the provider has no settings row")
        void shouldReturnFullName_whenNoSettings() {
            assertThat(caseManagementManager.getSignatureName(provider)).isEqualTo("FAKE-Sam Example");
        }

        @Test
        @DisplayName("should return an empty name when there is no provider")
        void shouldReturnEmpty_whenNoProvider() {
            assertThat(caseManagementManager.getSignatureName(null)).isEmpty();
        }
    }
}
