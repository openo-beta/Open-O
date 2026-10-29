package ca.openosp.openo.managers;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.casemgmt.model.ProviderExt;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.commn.dao.PrescriptionDao;
import ca.openosp.openo.commn.dao.ProviderExtDao;
import ca.openosp.openo.commn.dao.ProviderSignatureDao;
import ca.openosp.openo.commn.exception.ProviderSignatureException;
import ca.openosp.openo.commn.model.Prescription;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.ProviderSignature;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.managers.model.SignatureCard;
import ca.openosp.openo.managers.model.SignatureChoice;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LoggedInInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ProviderSignatureManagerImpl}: a provider's signature list and their default.
 *
 * <p>The contract: the default lives in {@code providerExt.signature}. A missing, empty or blank one
 * means the prescriber name ("Dr. " unless the first name has it, the first name, the last name),
 * which is what prescriptions printed before signatures could be chosen. The list is the prescriber
 * name first, then the saved signatures in the order they were added, then a default saved outside the
 * list; reading it saves nothing. Adding never changes the
 * default; editing or deleting the default carries the default with it; a provider can only change
 * their own signatures. A prescription saves its signature once, at its first print: the prescriber's
 * choice from that same list, or the prescriber's default when someone else prints it, and then never
 * changes. Only the prescriber gets the list to choose from in the print window.</p>
 *
 * @since 2026-10-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProviderSignatureManager")
@Tag("unit")
@Tag("fast")
@Tag("manager")
class ProviderSignatureManagerUnitTest extends OpenOUnitTestBase {

    private static final String PROVIDER_NO = "999998";
    private static final String OTHER_PROVIDER_NO = "999997";
    private static final String UNDER_ARMSTRONG = "Alex Openodoc, PA. Prescribed under the authority of Dr. Ty Armstrong";
    private static final String UNDER_WELCH = "Alex Openodoc, PA. Prescribed under the authority of Dr. Kirsten Welch";

    @Mock
    private ProviderExtDao providerExtDao;

    @Mock
    private ProviderSignatureDao providerSignatureDao;

    @Mock
    private ProviderDao providerDao;

    @Mock
    private PrescriptionDao prescriptionDao;

    @InjectMocks
    private ProviderSignatureManagerImpl providerSignatureManager;

    private LoggedInInfo loggedInInfo;

    private MockedStatic<LogAction> logActionMock;

    @BeforeEach
    void setUp() {
        // LogAction's static initializer gets OscarLogDao from SpringUtils, so register it before mocking LogAction
        registerMock(OscarLogDao.class, mock(OscarLogDao.class));
        logActionMock = mockStatic(LogAction.class);
        givenProvider("doctor", "openodoc");
        loggedInInfo = new LoggedInInfo();
        loggedInInfo.setLoggedInProvider(new Provider(PROVIDER_NO));
    }

    @AfterEach
    void tearDown() {
        logActionMock.close();
    }

    private void givenProvider(String firstName, String lastName) {
        Provider provider = new Provider(PROVIDER_NO);
        provider.setFirstName(firstName);
        provider.setLastName(lastName);
        when(providerDao.getProvider(PROVIDER_NO)).thenReturn(provider);
    }

    private ProviderExt givenSavedDefault(String signature) {
        ProviderExt providerExt = new ProviderExt();
        providerExt.setProviderNo(PROVIDER_NO);
        providerExt.setSignature(signature);
        when(providerExtDao.find(PROVIDER_NO)).thenReturn(providerExt);
        return providerExt;
    }

    private ProviderSignature signature(int id, String providerNo, String text) {
        ProviderSignature signature = new ProviderSignature();
        ReflectionTestUtils.setField(signature, "id", id);
        signature.setProviderNo(providerNo);
        signature.setSignature(text);
        when(providerSignatureDao.find(Integer.valueOf(id))).thenReturn(signature);
        return signature;
    }

    private void givenList(ProviderSignature... signatures) {
        when(providerSignatureDao.findByProviderNo(PROVIDER_NO)).thenReturn(Arrays.asList(signatures));
    }

    private ProviderSignature persistedSignature() {
        ArgumentCaptor<ProviderSignature> captor = ArgumentCaptor.forClass(ProviderSignature.class);
        verify(providerSignatureDao).persist(captor.capture());
        return captor.getValue();
    }

    /**
     * The signature printed when the prescriber doesn't choose another one.
     */
    @Nested
    @DisplayName("default signature")
    class DefaultSignature {

        @Test
        @DisplayName("should return the saved signature without surrounding spaces when one is saved")
        void shouldReturnSavedSignature_whenOneIsSaved() {
            givenSavedDefault("  " + UNDER_ARMSTRONG + " ");

            assertThat(providerSignatureManager.getDefaultSignature(PROVIDER_NO)).isEqualTo(UNDER_ARMSTRONG);
        }

        @ParameterizedTest(name = "saved signature [{0}]")
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("should return the prescriber name when the saved signature is missing or blank")
        void shouldReturnPrescriberName_whenSavedSignatureIsMissingOrBlank(String signature) {
            givenSavedDefault(signature);

            assertThat(providerSignatureManager.getDefaultSignature(PROVIDER_NO)).isEqualTo("Dr. doctor openodoc");
        }

        @Test
        @DisplayName("should return the prescriber name when the provider has no providerExt row")
        void shouldReturnPrescriberName_whenNoProviderExtRow() {
            assertThat(providerSignatureManager.getDefaultSignature(PROVIDER_NO)).isEqualTo("Dr. doctor openodoc");
        }
    }

    /**
     * The provider's name as prescriptions print it.
     */
    @Nested
    @DisplayName("prescriber name")
    class PrescriberName {

        @Test
        @DisplayName("should not add a second Dr. when the first name already has it")
        void shouldNotAddSecondDr_whenFirstNameHasIt() {
            givenProvider("Dr. Ty", "Armstrong");

            assertThat(providerSignatureManager.getDefaultSignature(PROVIDER_NO)).isEqualTo("Dr. Ty Armstrong");
        }

        @Test
        @DisplayName("should return an empty name when the provider doesn't exist")
        void shouldReturnEmptyName_whenProviderDoesNotExist() {
            when(providerDao.getProvider(PROVIDER_NO)).thenReturn(null);

            assertThat(providerSignatureManager.getDefaultSignature(PROVIDER_NO)).isEmpty();
        }
    }

    /**
     * The list shown in preferences and in the print window.
     */
    @Nested
    @DisplayName("choices")
    class Choices {

        @Test
        @DisplayName("should list the prescriber name first, then the signatures, with the saved default marked")
        void shouldListPrescriberNameFirstThenSignatures_whenProviderHasSignatures() {
            givenSavedDefault(UNDER_ARMSTRONG);
            givenList(signature(1, PROVIDER_NO, UNDER_ARMSTRONG), signature(2, PROVIDER_NO, UNDER_WELCH));

            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(choices).extracting(SignatureChoice::getSignatureId, SignatureChoice::getText, SignatureChoice::isDefaultChoice)
                    .containsExactly(
                            tuple(null, "Dr. doctor openodoc", false),
                            tuple(1, UNDER_ARMSTRONG, true),
                            tuple(2, UNDER_WELCH, false));
        }

        @Test
        @DisplayName("should mark the prescriber name as the default when no default is saved")
        void shouldMarkPrescriberNameDefault_whenNoDefaultSaved() {
            givenList(signature(1, PROVIDER_NO, UNDER_ARMSTRONG));

            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(choices).extracting(SignatureChoice::isDefaultChoice).containsExactly(true, false);
        }

        @Test
        @DisplayName("should show the saved default last, without saving it, when it isn't in the list")
        void shouldShowUnlistedDefaultWithoutSavingIt_whenItIsNotInTheList() {
            givenSavedDefault(UNDER_ARMSTRONG);
            givenList(signature(2, PROVIDER_NO, UNDER_WELCH));

            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(choices).extracting(SignatureChoice::getSignatureId, SignatureChoice::getText, SignatureChoice::isDefaultChoice)
                    .containsExactly(
                            tuple(null, "Dr. doctor openodoc", false),
                            tuple(2, UNDER_WELCH, false),
                            tuple(SignatureChoice.UNLISTED_DEFAULT_ID, UNDER_ARMSTRONG, true));
            assertThat(choices.get(2).isSavedSignature()).isFalse();
            verify(providerSignatureDao, never()).persist(any());
        }

        @Test
        @DisplayName("should mark a listed signature as the default, and show it trimmed, when only surrounding spaces differ")
        void shouldMarkListedSignatureDefault_whenOnlySurroundingSpacesDiffer() {
            givenSavedDefault(UNDER_ARMSTRONG);
            givenList(signature(1, PROVIDER_NO, UNDER_ARMSTRONG + "\t"));

            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(choices).extracting(SignatureChoice::getSignatureId, SignatureChoice::getText, SignatureChoice::isDefaultChoice)
                    .containsExactly(tuple(null, "Dr. doctor openodoc", false), tuple(1, UNDER_ARMSTRONG, true));
        }

        @Test
        @DisplayName("should list the prescriber name once, as the default, when the saved default is a copy of it")
        void shouldListPrescriberNameOnceAsDefault_whenSavedDefaultIsACopyOfIt() {
            givenSavedDefault("Dr. doctor openodoc");
            givenList(signature(1, PROVIDER_NO, "Dr. doctor openodoc"), signature(2, PROVIDER_NO, UNDER_WELCH));

            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(choices).extracting(SignatureChoice::getSignatureId, SignatureChoice::getText, SignatureChoice::isDefaultChoice)
                    .containsExactly(tuple(null, "Dr. doctor openodoc", true), tuple(2, UNDER_WELCH, false));
            assertThat(providerSignatureManager.findChoice(PROVIDER_NO, 1)).isEmpty();
        }

        @Test
        @DisplayName("should not show the default again when it is the prescriber name and isn't in the list")
        void shouldNotShowDefaultAgain_whenItIsThePrescriberNameAndNotInTheList() {
            givenSavedDefault("Dr. doctor openodoc");
            givenList(signature(2, PROVIDER_NO, UNDER_WELCH));

            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(choices).extracting(SignatureChoice::getSignatureId, SignatureChoice::getText, SignatureChoice::isDefaultChoice)
                    .containsExactly(tuple(null, "Dr. doctor openodoc", true), tuple(2, UNDER_WELCH, false));
        }

        @Test
        @DisplayName("should leave out a saved copy of the prescriber name when another signature is the default")
        void shouldLeaveOutCopyOfPrescriberName_whenAnotherSignatureIsTheDefault() {
            givenSavedDefault(UNDER_WELCH);
            givenList(signature(1, PROVIDER_NO, " Dr. doctor openodoc"), signature(2, PROVIDER_NO, UNDER_WELCH));

            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(choices).extracting(SignatureChoice::getSignatureId, SignatureChoice::getText, SignatureChoice::isDefaultChoice)
                    .containsExactly(tuple(null, "Dr. doctor openodoc", false), tuple(2, UNDER_WELCH, true));
        }

        @Test
        @DisplayName("should list a copy of the old name as a signature once the prescriber's name changes")
        void shouldListCopyOfOldName_whenPrescriberNameChanges() {
            givenProvider("Jane", "openodoc");
            givenSavedDefault("Dr. doctor openodoc");
            givenList(signature(1, PROVIDER_NO, "Dr. doctor openodoc"));

            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(choices).extracting(SignatureChoice::getSignatureId, SignatureChoice::getText, SignatureChoice::isDefaultChoice)
                    .containsExactly(tuple(null, "Dr. Jane openodoc", false), tuple(1, "Dr. doctor openodoc", true));
        }

        @Test
        @DisplayName("should find an entry by its id, the prescriber name by no id, and nothing for an id not in the list")
        void shouldFindEntryById_whenItIsInTheList() {
            givenList(signature(2, PROVIDER_NO, UNDER_WELCH));

            assertThat(providerSignatureManager.findChoice(PROVIDER_NO, 2)).map(SignatureChoice::getText).contains(UNDER_WELCH);
            assertThat(providerSignatureManager.findChoice(PROVIDER_NO, null)).map(SignatureChoice::getText).contains("Dr. doctor openodoc");
            assertThat(providerSignatureManager.findChoice(PROVIDER_NO, 4)).isEmpty();
        }

        @Test
        @DisplayName("should post the prescriber name's id as empty and read it back as no id")
        void shouldPostPrescriberNameIdAsEmpty_andReadItBack() {
            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(choices.get(0).getPostedId()).isEmpty();
            assertThat(SignatureChoice.parsePostedId("")).isNull();
            assertThat(SignatureChoice.parsePostedId(null)).isNull();
            assertThat(SignatureChoice.parsePostedId("2")).isEqualTo(2);
            assertThatThrownBy(() -> SignatureChoice.parsePostedId("abc")).isInstanceOf(NumberFormatException.class);
        }
    }

    /**
     * What the print window's signature card shows.
     */
    @Nested
    @DisplayName("signature card")
    class Card {

        private static final int SCRIPT_NO = 287;
        private static final int DEMOGRAPHIC_NO = 1;

        private Prescription prescription;

        @BeforeEach
        void givenUnsavedPrescription() {
            prescription = new Prescription();
            prescription.setDemographicId(DEMOGRAPHIC_NO);
            when(prescriptionDao.find(Integer.valueOf(SCRIPT_NO))).thenReturn(prescription);
            givenSavedDefault(UNDER_ARMSTRONG);
            givenList(signature(1, PROVIDER_NO, UNDER_ARMSTRONG), signature(2, PROVIDER_NO, UNDER_WELCH));
        }

        @Test
        @DisplayName("should show the saved signature, locked, with nothing to choose, once the prescription has one")
        void shouldShowSavedSignatureLocked_whenPrescriptionHasOne() {
            prescription.setPrescriberSignature(UNDER_WELCH);

            SignatureCard card = providerSignatureManager.getSignatureCard(loggedInInfo, String.valueOf(SCRIPT_NO), DEMOGRAPHIC_NO, PROVIDER_NO);

            assertThat(card).extracting(SignatureCard::getScriptNo, SignatureCard::getSignature, SignatureCard::isLocked, SignatureCard::isEditable)
                    .containsExactly(String.valueOf(SCRIPT_NO), UNDER_WELCH, true, false);
            assertThat(card.getChoices()).isEmpty();
        }

        @Test
        @DisplayName("should not show another patient's saved signature, treating their prescription as not printed")
        void shouldNotShowSavedSignature_whenPrescriptionIsAnotherPatients() {
            prescription.setPrescriberSignature(UNDER_WELCH);

            SignatureCard card = providerSignatureManager.getSignatureCard(loggedInInfo, String.valueOf(SCRIPT_NO), DEMOGRAPHIC_NO + 1, PROVIDER_NO);

            assertThat(card).extracting(SignatureCard::getSignature, SignatureCard::isLocked)
                    .containsExactly(UNDER_ARMSTRONG, false);
        }

        @Test
        @DisplayName("should give the prescriber their list, with the default shown, before the first print")
        void shouldGivePrescriberTheirList_whenNotPrintedYet() {
            SignatureCard card = providerSignatureManager.getSignatureCard(loggedInInfo, String.valueOf(SCRIPT_NO), DEMOGRAPHIC_NO, PROVIDER_NO);

            assertThat(card).extracting(SignatureCard::getSignature, SignatureCard::isLocked, SignatureCard::isEditable)
                    .containsExactly(UNDER_ARMSTRONG, false, true);
            assertThat(card.getChoices()).extracting(SignatureChoice::getSignatureId).containsExactly(null, 1, 2);
        }

        @Test
        @DisplayName("should show anyone else the prescriber's default with nothing to choose")
        void shouldShowSomeoneElseTheDefault_whenTheyAreNotThePrescriber() {
            LoggedInInfo someoneElse = new LoggedInInfo();
            someoneElse.setLoggedInProvider(new Provider(OTHER_PROVIDER_NO));

            SignatureCard card = providerSignatureManager.getSignatureCard(someoneElse, String.valueOf(SCRIPT_NO), DEMOGRAPHIC_NO, PROVIDER_NO);

            assertThat(card).extracting(SignatureCard::getSignature, SignatureCard::isLocked, SignatureCard::isEditable)
                    .containsExactly(UNDER_ARMSTRONG, false, false);
            assertThat(card.getChoices()).isEmpty();
        }

        @Test
        @DisplayName("should treat a window with no prescription yet as not printed")
        void shouldTreatAsNotPrinted_whenThereIsNoPrescription() {
            SignatureCard card = providerSignatureManager.getSignatureCard(loggedInInfo, null, DEMOGRAPHIC_NO, PROVIDER_NO);

            assertThat(card).extracting(SignatureCard::getScriptNo, SignatureCard::getSignature, SignatureCard::isLocked)
                    .containsExactly(null, UNDER_ARMSTRONG, false);
        }

        @Test
        @DisplayName("should treat a prescription number too big to exist as not printed")
        void shouldTreatAsNotPrinted_whenScriptNoIsTooBig() {
            SignatureCard card = providerSignatureManager.getSignatureCard(loggedInInfo, "99999999999", DEMOGRAPHIC_NO, PROVIDER_NO);

            assertThat(card).extracting(SignatureCard::getSignature, SignatureCard::isLocked)
                    .containsExactly(UNDER_ARMSTRONG, false);
        }
    }

    /**
     * Adding a signature to the list.
     */
    @Nested
    @DisplayName("add")
    class Add {

        @Test
        @DisplayName("should save the text without surrounding spaces and leave the default alone")
        void shouldSaveTextAndLeaveDefault_whenTextIsNew() {
            providerSignatureManager.addSignature(loggedInInfo, "  " + UNDER_WELCH + " ");

            assertThat(persistedSignature()).extracting(ProviderSignature::getProviderNo, ProviderSignature::getSignature, ProviderSignature::getLastUpdateUser)
                    .containsExactly(PROVIDER_NO, UNDER_WELCH, PROVIDER_NO);
            verify(providerExtDao, never()).merge(any());
            verify(providerExtDao, never()).persist(any());
        }

        @ParameterizedTest(name = "text [{0}]")
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("should refuse a signature with no text")
        void shouldRefuseSignature_whenTextIsBlank(String text) {
            assertThatThrownBy(() -> providerSignatureManager.addSignature(loggedInInfo, text))
                    .isInstanceOf(ProviderSignatureException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_BLANK);
        }

        @Test
        @DisplayName("should refuse a signature longer than the column")
        void shouldRefuseSignature_whenTextIsTooLong() {
            assertThatThrownBy(() -> providerSignatureManager.addSignature(loggedInInfo, "x".repeat(ProviderSignatureManager.MAX_LENGTH + 1)))
                    .isInstanceOf(ProviderSignatureException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_TOO_LONG);
        }

        @Test
        @DisplayName("should refuse a signature already in the list")
        void shouldRefuseSignature_whenAlreadyInList() {
            givenList(signature(1, PROVIDER_NO, UNDER_WELCH));

            assertThatThrownBy(() -> providerSignatureManager.addSignature(loggedInInfo, UNDER_WELCH))
                    .isInstanceOf(ProviderSignatureException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_DUPLICATE);
        }

        @Test
        @DisplayName("should refuse a signature that is the prescriber name, which is always in the list")
        void shouldRefuseSignature_whenSameAsPrescriberName() {
            assertThatThrownBy(() -> providerSignatureManager.addSignature(loggedInInfo, "Dr. doctor openodoc"))
                    .isInstanceOf(ProviderSignatureException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_DUPLICATE);
        }
    }

    /**
     * Changing a signature's text.
     */
    @Nested
    @DisplayName("edit")
    class Edit {

        @Test
        @DisplayName("should change the default too when the edited signature is the default")
        void shouldChangeDefaultToo_whenEditedSignatureIsTheDefault() {
            ProviderExt providerExt = givenSavedDefault(UNDER_ARMSTRONG);
            ProviderSignature armstrong = signature(1, PROVIDER_NO, UNDER_ARMSTRONG);
            givenList(armstrong);

            providerSignatureManager.editSignature(loggedInInfo, 1, UNDER_WELCH);

            assertThat(armstrong.getSignature()).isEqualTo(UNDER_WELCH);
            assertThat(providerExt.getSignature()).isEqualTo(UNDER_WELCH);
            verify(providerSignatureDao).merge(armstrong);
            verify(providerExtDao).merge(providerExt);
        }

        @Test
        @DisplayName("should leave the default alone when the edited signature isn't the default")
        void shouldLeaveDefault_whenEditedSignatureIsNotTheDefault() {
            ProviderExt providerExt = givenSavedDefault(UNDER_ARMSTRONG);
            ProviderSignature welch = signature(2, PROVIDER_NO, UNDER_WELCH);
            givenList(signature(1, PROVIDER_NO, UNDER_ARMSTRONG), welch);

            providerSignatureManager.editSignature(loggedInInfo, 2, UNDER_WELCH + ", MD");

            assertThat(welch.getSignature()).isEqualTo(UNDER_WELCH + ", MD");
            assertThat(providerExt.getSignature()).isEqualTo(UNDER_ARMSTRONG);
            verify(providerExtDao, never()).merge(any());
        }

        @Test
        @DisplayName("should allow saving a signature with its own text unchanged")
        void shouldAllowOwnText_whenEditingTheSignatureItself() {
            ProviderSignature welch = signature(2, PROVIDER_NO, UNDER_WELCH);
            givenList(welch);

            providerSignatureManager.editSignature(loggedInInfo, 2, UNDER_WELCH);

            verify(providerSignatureDao).merge(welch);
        }

        @Test
        @DisplayName("should refuse to change another provider's signature")
        void shouldRefuseEdit_whenSignatureBelongsToAnotherProvider() {
            ProviderSignature theirs = signature(3, OTHER_PROVIDER_NO, UNDER_WELCH);

            assertThatThrownBy(() -> providerSignatureManager.editSignature(loggedInInfo, 3, UNDER_ARMSTRONG))
                    .isInstanceOf(SecurityException.class);
            assertThat(theirs.getSignature()).isEqualTo(UNDER_WELCH);
        }

        @Test
        @DisplayName("should refuse the change with the deleted message when the signature was deleted in another window")
        void shouldRefuseEdit_whenSignatureWasDeleted() {
            assertThatThrownBy(() -> providerSignatureManager.editSignature(loggedInInfo, 4, UNDER_WELCH))
                    .isInstanceOf(ProviderSignatureException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_DELETED);
            verify(providerSignatureDao, never()).merge(any());
        }
    }

    /**
     * Deleting a signature from the list.
     */
    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should make the prescriber name the default when the deleted signature is the default")
        void shouldMakePrescriberNameDefault_whenDeletedSignatureIsTheDefault() {
            ProviderExt providerExt = givenSavedDefault(UNDER_ARMSTRONG);
            ProviderSignature armstrong = signature(1, PROVIDER_NO, UNDER_ARMSTRONG);

            providerSignatureManager.deleteSignature(loggedInInfo, 1);

            assertThat(providerExt.getSignature()).isNull();
            verify(providerSignatureDao).remove(armstrong);
        }

        @Test
        @DisplayName("should leave the default alone when the deleted signature isn't the default")
        void shouldLeaveDefault_whenDeletedSignatureIsNotTheDefault() {
            ProviderExt providerExt = givenSavedDefault(UNDER_ARMSTRONG);
            ProviderSignature welch = signature(2, PROVIDER_NO, UNDER_WELCH);

            providerSignatureManager.deleteSignature(loggedInInfo, 2);

            assertThat(providerExt.getSignature()).isEqualTo(UNDER_ARMSTRONG);
            verify(providerSignatureDao).remove(welch);
        }

        @Test
        @DisplayName("should refuse to delete another provider's signature")
        void shouldRefuseDelete_whenSignatureBelongsToAnotherProvider() {
            signature(3, OTHER_PROVIDER_NO, UNDER_WELCH);

            assertThatThrownBy(() -> providerSignatureManager.deleteSignature(loggedInInfo, 3))
                    .isInstanceOf(SecurityException.class);
            verify(providerSignatureDao, never()).remove(any(ProviderSignature.class));
        }

        @Test
        @DisplayName("should change nothing when the signature was deleted already")
        void shouldChangeNothing_whenSignatureWasDeletedAlready() {
            ProviderExt providerExt = givenSavedDefault(UNDER_ARMSTRONG);

            providerSignatureManager.deleteSignature(loggedInInfo, 4);

            assertThat(providerExt.getSignature()).isEqualTo(UNDER_ARMSTRONG);
            verify(providerSignatureDao, never()).remove(any(ProviderSignature.class));
            verify(providerExtDao, never()).merge(any());
        }
    }

    /**
     * Picking the default.
     */
    @Nested
    @DisplayName("make default")
    class MakeDefault {

        @Test
        @DisplayName("should save the signature's text as the default")
        void shouldSaveSignatureText_whenMakingSignatureTheDefault() {
            ProviderExt providerExt = givenSavedDefault(UNDER_ARMSTRONG);
            signature(2, PROVIDER_NO, UNDER_WELCH);

            providerSignatureManager.makeDefault(loggedInInfo, 2);

            assertThat(providerExt.getSignature()).isEqualTo(UNDER_WELCH);
            verify(providerExtDao).merge(providerExt);
        }

        @Test
        @DisplayName("should clear the saved default when the prescriber name becomes the default")
        void shouldClearSavedDefault_whenMakingPrescriberNameTheDefault() {
            ProviderExt providerExt = givenSavedDefault(UNDER_ARMSTRONG);

            providerSignatureManager.makeDefault(loggedInInfo, null);

            assertThat(providerExt.getSignature()).isNull();
            verify(providerExtDao).merge(providerExt);
        }

        @Test
        @DisplayName("should add the provider's providerExt row when they have none")
        void shouldAddProviderExtRow_whenProviderHasNone() {
            signature(2, PROVIDER_NO, UNDER_WELCH);

            providerSignatureManager.makeDefault(loggedInInfo, 2);

            ArgumentCaptor<ProviderExt> captor = ArgumentCaptor.forClass(ProviderExt.class);
            verify(providerExtDao).persist(captor.capture());
            assertThat(captor.getValue()).extracting(ProviderExt::getProviderNo, ProviderExt::getSignature)
                    .containsExactly(PROVIDER_NO, UNDER_WELCH);
        }

        @Test
        @DisplayName("should refuse another provider's signature as the default")
        void shouldRefuseDefault_whenSignatureBelongsToAnotherProvider() {
            signature(3, OTHER_PROVIDER_NO, UNDER_WELCH);

            assertThatThrownBy(() -> providerSignatureManager.makeDefault(loggedInInfo, 3))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("should leave the default alone when the signature was deleted in another window")
        void shouldLeaveDefault_whenSignatureWasDeleted() {
            ProviderExt providerExt = givenSavedDefault(UNDER_ARMSTRONG);

            providerSignatureManager.makeDefault(loggedInInfo, 4);

            assertThat(providerExt.getSignature()).isEqualTo(UNDER_ARMSTRONG);
            verify(providerExtDao, never()).merge(any());
        }
    }

    /**
     * Saving the signature on a prescription at its first print, fax or PDF.
     */
    @Nested
    @DisplayName("lock on a prescription")
    class LockForPrescription {

        private static final int SCRIPT_NO = 287;
        private static final int DEMOGRAPHIC_NO = 1;
        private static final String TEXT_VIEW = "Alex\nMHI Org\nHamilton\nAMOXICILLIN 125MG";

        private Prescription prescription;

        @BeforeEach
        void givenUnsavedPrescription() {
            prescription = new Prescription();
            ReflectionTestUtils.setField(prescription, "id", SCRIPT_NO);
            prescription.setProviderNo(PROVIDER_NO);
            prescription.setDemographicId(DEMOGRAPHIC_NO);
            prescription.setTextView(TEXT_VIEW);
            when(prescriptionDao.find(Integer.valueOf(SCRIPT_NO))).thenReturn(prescription);
            when(prescriptionDao.lockPrescriberSignature(any(), any(), any())).thenReturn(1);
        }

        private LoggedInInfo loggedInAs(String providerNo) {
            LoggedInInfo info = new LoggedInInfo();
            info.setLoggedInProvider(new Provider(providerNo));
            return info;
        }

        @Test
        @DisplayName("should save the prescriber's chosen signature and put it on the first line of the text copy")
        void shouldSaveChosenSignature_whenPrescriberPrints() {
            givenList(signature(2, PROVIDER_NO, UNDER_WELCH));

            String saved = providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, 2);

            assertThat(saved).isEqualTo(UNDER_WELCH);
            verify(prescriptionDao).lockPrescriberSignature(SCRIPT_NO, UNDER_WELCH, UNDER_WELCH + "\nMHI Org\nHamilton\nAMOXICILLIN 125MG");
        }

        @Test
        @DisplayName("should save the prescriber name when the prescriber picks it")
        void shouldSavePrescriberName_whenPrescriberPicksIt() {
            givenSavedDefault(UNDER_ARMSTRONG);

            assertThat(providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, null))
                    .isEqualTo("Dr. doctor openodoc");
        }

        @Test
        @DisplayName("should save the prescriber's default whatever someone else picks")
        void shouldSavePrescriberDefault_whenSomeoneElsePrints() {
            givenSavedDefault(UNDER_ARMSTRONG);
            signature(2, PROVIDER_NO, UNDER_WELCH);

            String saved = providerSignatureManager.lockForPrescription(loggedInAs(OTHER_PROVIDER_NO), SCRIPT_NO, DEMOGRAPHIC_NO, 2);

            assertThat(saved).isEqualTo(UNDER_ARMSTRONG);
            verify(prescriptionDao).lockPrescriberSignature(SCRIPT_NO, UNDER_ARMSTRONG, UNDER_ARMSTRONG + "\nMHI Org\nHamilton\nAMOXICILLIN 125MG");
        }

        @Test
        @DisplayName("should keep the saved signature when the prescription has one already")
        void shouldKeepSavedSignature_whenAlreadySaved() {
            prescription.setPrescriberSignature(UNDER_ARMSTRONG);
            signature(2, PROVIDER_NO, UNDER_WELCH);

            assertThat(providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, 2)).isEqualTo(UNDER_ARMSTRONG);
            verify(prescriptionDao, never()).lockPrescriberSignature(any(), any(), any());
        }

        @Test
        @DisplayName("should return the other window's signature when another window saved one first")
        void shouldReturnOtherWindowsSignature_whenItSavedFirst() {
            givenList(signature(2, PROVIDER_NO, UNDER_WELCH));
            when(prescriptionDao.lockPrescriberSignature(any(), any(), any())).thenReturn(0);
            doAnswer(invocation -> {
                prescription.setPrescriberSignature(UNDER_ARMSTRONG);
                return null;
            }).when(prescriptionDao).refresh(prescription);

            assertThat(providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, 2)).isEqualTo(UNDER_ARMSTRONG);
        }

        @Test
        @DisplayName("should refuse a prescription for another patient")
        void shouldRefuse_whenPrescriptionIsForAnotherPatient() {
            assertThatThrownBy(() -> providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, 2, null))
                    .isInstanceOf(SecurityException.class);
            verify(prescriptionDao, never()).lockPrescriberSignature(any(), any(), any());
        }

        @Test
        @DisplayName("should refuse a signature from another provider's list, which isn't in the prescriber's")
        void shouldRefuse_whenSignatureBelongsToAnotherProvider() {
            signature(3, OTHER_PROVIDER_NO, UNDER_WELCH);

            assertThatThrownBy(() -> providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, 3))
                    .isInstanceOf(ProviderSignatureException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_DELETED);
            verify(prescriptionDao, never()).lockPrescriberSignature(any(), any(), any());
        }

        @Test
        @DisplayName("should save a listed signature without the surrounding spaces it was copied in with")
        void shouldSaveTrimmedText_whenListedSignatureHasSurroundingSpaces() {
            givenList(signature(2, PROVIDER_NO, " " + UNDER_WELCH + "\t"));

            assertThat(providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, 2)).isEqualTo(UNDER_WELCH);
        }

        @Test
        @DisplayName("should refuse with the deleted message when the chosen signature was deleted in another window")
        void shouldRefuseWithDeletedMessage_whenChosenSignatureWasDeleted() {
            assertThatThrownBy(() -> providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, 4))
                    .isInstanceOf(ProviderSignatureException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_DELETED);
            verify(prescriptionDao, never()).lockPrescriberSignature(any(), any(), any());
        }

        @Test
        @DisplayName("should save the prescriber's default when they pick a default that isn't in their list")
        void shouldSaveDefault_whenPrescriberPicksUnlistedDefault() {
            givenSavedDefault(UNDER_ARMSTRONG);

            String saved = providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, SignatureChoice.UNLISTED_DEFAULT_ID);

            assertThat(saved).isEqualTo(UNDER_ARMSTRONG);
            verify(prescriptionDao).lockPrescriberSignature(SCRIPT_NO, UNDER_ARMSTRONG, UNDER_ARMSTRONG + "\nMHI Org\nHamilton\nAMOXICILLIN 125MG");
        }

        @Test
        @DisplayName("should save nothing when the prescriber has no name or default on file")
        void shouldSaveNothing_whenPrescriberHasNoNameOrDefault() {
            when(providerDao.getProvider(PROVIDER_NO)).thenReturn(null);

            assertThat(providerSignatureManager.lockForPrescription(loggedInAs(OTHER_PROVIDER_NO), SCRIPT_NO, DEMOGRAPHIC_NO, null)).isNull();
            verify(prescriptionDao, never()).lockPrescriberSignature(any(), any(), any());
        }

        @Test
        @DisplayName("should write an audit entry when it saves the signature")
        void shouldWriteAuditEntry_whenItSavesTheSignature() {
            givenList(signature(2, PROVIDER_NO, UNDER_WELCH));

            providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, 2);

            logActionMock.verify(() -> LogAction.addLog(loggedInInfo, LogConst.UPDATE, LogConst.CON_PRESCRIPTION,
                    String.valueOf(SCRIPT_NO), String.valueOf(DEMOGRAPHIC_NO), "prescriber signature"));
        }

        @Test
        @DisplayName("should write no audit entry when the prescription has a signature already")
        void shouldWriteNoAuditEntry_whenAlreadySaved() {
            prescription.setPrescriberSignature(UNDER_ARMSTRONG);

            providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, null);

            logActionMock.verifyNoInteractions();
        }

        @Test
        @DisplayName("should write no audit entry when another window saved first")
        void shouldWriteNoAuditEntry_whenAnotherWindowSavedFirst() {
            givenList(signature(2, PROVIDER_NO, UNDER_WELCH));
            when(prescriptionDao.lockPrescriberSignature(any(), any(), any())).thenReturn(0);

            providerSignatureManager.lockForPrescription(loggedInInfo, SCRIPT_NO, DEMOGRAPHIC_NO, 2);

            logActionMock.verifyNoInteractions();
        }

        @Test
        @DisplayName("should write no audit entry when there is nothing to save")
        void shouldWriteNoAuditEntry_whenNothingToSave() {
            when(providerDao.getProvider(PROVIDER_NO)).thenReturn(null);

            providerSignatureManager.lockForPrescription(loggedInAs(OTHER_PROVIDER_NO), SCRIPT_NO, DEMOGRAPHIC_NO, null);

            logActionMock.verifyNoInteractions();
        }

        @Test
        @DisplayName("should print the saved signature once there is one, and the default before then")
        void shouldPrintSavedSignatureOnceSaved_andDefaultBefore() {
            givenSavedDefault(UNDER_ARMSTRONG);

            assertThat(providerSignatureManager.getPrintedSignature(String.valueOf(SCRIPT_NO), PROVIDER_NO)).isEqualTo(UNDER_ARMSTRONG);
            prescription.setPrescriberSignature(UNDER_WELCH);
            assertThat(providerSignatureManager.getPrintedSignature(String.valueOf(SCRIPT_NO), PROVIDER_NO)).isEqualTo(UNDER_WELCH);
            assertThat(providerSignatureManager.getPrintedSignature(null, PROVIDER_NO)).isEqualTo(UNDER_ARMSTRONG);
        }
    }

    /**
     * Writing the signature on the first line of a prescription's text copy.
     */
    @Nested
    @DisplayName("text copy first line")
    class TextCopyFirstLine {

        @Test
        @DisplayName("should replace only the first line")
        void shouldReplaceOnlyFirstLine_whenTextHasSeveralLines() {
            assertThat(ProviderSignatureManagerImpl.withFirstLine("Alex\nMHI Org\n\nDrug", UNDER_WELCH))
                    .isEqualTo(UNDER_WELCH + "\nMHI Org\n\nDrug");
        }

        @Test
        @DisplayName("should replace the whole text when it is one line")
        void shouldReplaceWholeText_whenTextIsOneLine() {
            assertThat(ProviderSignatureManagerImpl.withFirstLine("Alex", UNDER_WELCH)).isEqualTo(UNDER_WELCH);
        }

        @ParameterizedTest(name = "text copy [{0}]")
        @NullSource
        @ValueSource(strings = {""})
        @DisplayName("should leave a missing or empty text copy as it is")
        void shouldLeaveTextCopy_whenMissingOrEmpty(String textView) {
            assertThat(ProviderSignatureManagerImpl.withFirstLine(textView, UNDER_WELCH)).isEqualTo(textView);
        }
    }

    /**
     * The default saved through the provider settings web service.
     */
    @Nested
    @DisplayName("default saved by text")
    class DefaultSavedByText {

        @Test
        @DisplayName("should add the text to the list and make it the default when it isn't in the list")
        void shouldAddTextAndMakeItDefault_whenNotInList() {
            ProviderExt providerExt = givenSavedDefault(null);

            providerSignatureManager.saveDefaultSignatureText(loggedInInfo, PROVIDER_NO, UNDER_WELCH);

            assertThat(persistedSignature().getSignature()).isEqualTo(UNDER_WELCH);
            assertThat(providerExt.getSignature()).isEqualTo(UNDER_WELCH);
        }

        @Test
        @DisplayName("should not add the text again when it is already in the list")
        void shouldNotAddText_whenAlreadyInList() {
            ProviderExt providerExt = givenSavedDefault(null);
            givenList(signature(2, PROVIDER_NO, UNDER_WELCH));

            providerSignatureManager.saveDefaultSignatureText(loggedInInfo, PROVIDER_NO, UNDER_WELCH);

            verify(providerSignatureDao, never()).persist(any());
            assertThat(providerExt.getSignature()).isEqualTo(UNDER_WELCH);
        }

        @Test
        @DisplayName("should make the prescriber name the default when the text is blank")
        void shouldClearSavedDefault_whenTextIsBlank() {
            ProviderExt providerExt = givenSavedDefault(UNDER_ARMSTRONG);

            providerSignatureManager.saveDefaultSignatureText(loggedInInfo, PROVIDER_NO, " ");

            assertThat(providerExt.getSignature()).isNull();
            verify(providerSignatureDao, never()).persist(any());
        }
    }
}
