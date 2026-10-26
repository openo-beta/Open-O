package ca.openosp.openo.managers;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.casemgmt.model.ProviderExt;
import ca.openosp.openo.commn.dao.ProviderExtDao;
import ca.openosp.openo.commn.dao.ProviderSignatureDao;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.ProviderSignature;
import ca.openosp.openo.managers.model.SignatureChoice;
import ca.openosp.openo.utility.LoggedInInfo;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ProviderSignatureManagerImpl}: a provider's signature list and their default.
 *
 * <p>The contract: the default lives in {@code providerExt.signature}. A missing, empty or blank one
 * means the prescriber name ("Dr. " unless the first name has it, the first name, the last name),
 * which is what prescriptions printed before signatures could be chosen. The list is the prescriber
 * name first, then the saved signatures in the order they were added. Adding never changes the
 * default; editing or deleting the default carries the default with it; a provider can only change
 * their own signatures.</p>
 *
 * @since 2026-10-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProviderSignatureManager")
@Tag("unit")
@Tag("fast")
@Tag("manager")
class ProviderSignatureManagerUnitTest {

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

    @InjectMocks
    private ProviderSignatureManagerImpl providerSignatureManager;

    private LoggedInInfo loggedInInfo;

    @BeforeEach
    void setUp() {
        givenProvider("doctor", "openodoc");
        loggedInInfo = new LoggedInInfo();
        loggedInInfo.setLoggedInProvider(new Provider(PROVIDER_NO));
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

            assertThat(providerSignatureManager.getPrescriberName(PROVIDER_NO)).isEqualTo("Dr. Ty Armstrong");
        }

        @Test
        @DisplayName("should return an empty name when the provider doesn't exist")
        void shouldReturnEmptyName_whenProviderDoesNotExist() {
            when(providerDao.getProvider(PROVIDER_NO)).thenReturn(null);

            assertThat(providerSignatureManager.getPrescriberName(PROVIDER_NO)).isEmpty();
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
        @DisplayName("should add the saved default to the list when it isn't in it, so it can be pre-selected")
        void shouldAddSavedDefaultToList_whenItIsNotInTheList() {
            givenSavedDefault(UNDER_ARMSTRONG);

            List<SignatureChoice> choices = providerSignatureManager.getChoices(PROVIDER_NO);

            assertThat(persistedSignature()).extracting(ProviderSignature::getProviderNo, ProviderSignature::getSignature)
                    .containsExactly(PROVIDER_NO, UNDER_ARMSTRONG);
            assertThat(choices).extracting(SignatureChoice::getText, SignatureChoice::isDefaultChoice)
                    .containsExactly(tuple("Dr. doctor openodoc", false), tuple(UNDER_ARMSTRONG, true));
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
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_BLANK);
        }

        @Test
        @DisplayName("should refuse a signature longer than the column")
        void shouldRefuseSignature_whenTextIsTooLong() {
            assertThatThrownBy(() -> providerSignatureManager.addSignature(loggedInInfo, "x".repeat(ProviderSignatureManager.MAX_LENGTH + 1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_TOO_LONG);
        }

        @Test
        @DisplayName("should refuse a signature already in the list")
        void shouldRefuseSignature_whenAlreadyInList() {
            givenList(signature(1, PROVIDER_NO, UNDER_WELCH));

            assertThatThrownBy(() -> providerSignatureManager.addSignature(loggedInInfo, UNDER_WELCH))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(ProviderSignatureManager.ERROR_DUPLICATE);
        }

        @Test
        @DisplayName("should refuse a signature that is the prescriber name, which is always in the list")
        void shouldRefuseSignature_whenSameAsPrescriberName() {
            assertThatThrownBy(() -> providerSignatureManager.addSignature(loggedInInfo, "Dr. doctor openodoc"))
                    .isInstanceOf(IllegalArgumentException.class)
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
