package ca.openosp.openo.managers;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.casemgmt.model.ProviderExt;
import ca.openosp.openo.commn.dao.ProviderExtDao;
import ca.openosp.openo.commn.model.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ProviderSignatureManagerImpl}: which signature a prescription prints by default.
 *
 * <p>The contract: a saved signature prints as saved, without surrounding spaces. A missing, empty
 * or blank one prints the provider's prescriber name ("Dr. " unless the first name has it, the
 * first name, the last name), which is what prescriptions printed before signatures could be chosen.</p>
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

    @Mock
    private ProviderExtDao providerExtDao;

    @Mock
    private ProviderDao providerDao;

    @InjectMocks
    private ProviderSignatureManagerImpl providerSignatureManager;

    @BeforeEach
    void setUp() {
        givenProvider("doctor", "openodoc");
    }

    private void givenProvider(String firstName, String lastName) {
        Provider provider = new Provider(PROVIDER_NO);
        provider.setFirstName(firstName);
        provider.setLastName(lastName);
        when(providerDao.getProvider(PROVIDER_NO)).thenReturn(provider);
    }

    private void givenSavedSignature(String signature) {
        ProviderExt providerExt = new ProviderExt();
        providerExt.setProviderNo(PROVIDER_NO);
        providerExt.setSignature(signature);
        when(providerExtDao.find(PROVIDER_NO)).thenReturn(providerExt);
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
            givenSavedSignature("  Alex Openodoc, PA. Prescribed under the authority of Dr. Ty Armstrong ");

            assertThat(providerSignatureManager.getDefaultSignature(PROVIDER_NO))
                    .isEqualTo("Alex Openodoc, PA. Prescribed under the authority of Dr. Ty Armstrong");
        }

        @ParameterizedTest(name = "saved signature [{0}]")
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("should return the prescriber name when the saved signature is missing or blank")
        void shouldReturnPrescriberName_whenSavedSignatureIsMissingOrBlank(String signature) {
            givenSavedSignature(signature);

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
}
