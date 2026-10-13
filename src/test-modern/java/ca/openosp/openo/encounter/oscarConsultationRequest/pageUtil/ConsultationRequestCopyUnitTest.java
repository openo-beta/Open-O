package ca.openosp.openo.encounter.oscarConsultationRequest.pageUtil;

import ca.openosp.OscarProperties;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.ConsultationRequestDao;
import ca.openosp.openo.commn.dao.ConsultationRequestExtDao;
import ca.openosp.openo.commn.dao.ConsultationServiceDao;
import ca.openosp.openo.commn.dao.ContactDao;
import ca.openosp.openo.commn.dao.FaxClientLogDao;
import ca.openosp.openo.commn.dao.FaxJobDao;
import ca.openosp.openo.commn.model.ConsultationRequest;
import ca.openosp.openo.commn.model.ConsultationServices;
import ca.openosp.openo.commn.model.ProfessionalSpecialist;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.test.unit.OpenOUnitTestBase;
import ca.openosp.openo.utility.LocaleUtils;
import ca.openosp.openo.utility.LoggedInInfo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConsultationRequestCopy#prefill(LoggedInInfo, Integer, Locale, EctConsultationFormRequestUtil)},
 * which prefills a new consultation request from a saved one for the "Copy to New Consultation Request"
 * button.
 *
 * <p>The contract is a whitelist. The clinical text, urgency, team and (with multisites) site are copied.
 * The service, referring practitioner and letterhead are copied only while what they name is still
 * active. Everything else, such as the specialist, status, appointment, signature and eReferral data,
 * keeps its new-request default. The appointment notes get one line naming the source request.</p>
 */
@DisplayName("ConsultationRequestCopy prefill from a saved request")
@Tag("unit")
@Tag("fast")
@Tag("consultation")
class ConsultationRequestCopyUnitTest extends OpenOUnitTestBase {

    private static final Integer SOURCE_ID = 288;
    private static final Integer PATIENT = 1;
    private static final Integer SERVICE_ID = 3;
    private static final String REFERRING_PROVIDER = "10";
    private static final String LETTERHEAD_PROVIDER = "999998";
    private static final Date REFERRAL_DATE = new GregorianCalendar(2026, Calendar.JANUARY, 15).getTime();

    private static String originalBaseName;

    private ConsultationRequestDao consultationRequestDao;
    private ConsultationRequestExtDao consultationRequestExtDao;
    private ConsultationServiceDao consultationServiceDao;
    private ProviderDao providerDao;
    private SecurityInfoManager securityInfoManager;
    private LoggedInInfo loggedInInfo;
    private ConsultationRequest source;

    @BeforeAll
    static void useOscarResources() {
        // the web app points LocaleUtils at oscarResources on startup; a unit test has to do it itself
        originalBaseName = LocaleUtils.BASE_NAME;
        LocaleUtils.BASE_NAME = "oscarResources";
    }

    @AfterAll
    static void restoreBaseName() {
        LocaleUtils.BASE_NAME = originalBaseName;
    }

    @BeforeEach
    void setUp() throws Exception {
        consultationRequestDao = createAndRegisterMock(ConsultationRequestDao.class);
        consultationRequestExtDao = createAndRegisterMock(ConsultationRequestExtDao.class);
        consultationServiceDao = createAndRegisterMock(ConsultationServiceDao.class);
        providerDao = createAndRegisterMock(ProviderDao.class);
        securityInfoManager = createAndRegisterMock(SecurityInfoManager.class);
        createAndRegisterMock(DemographicManager.class);
        createAndRegisterMock(ContactDao.class);
        createAndRegisterMock(FaxJobDao.class);
        createAndRegisterMock(FaxClientLogDao.class);

        loggedInInfo = mock(LoggedInInfo.class);
        when(securityInfoManager.hasPrivilege(eq(loggedInInfo), eq("_con"), eq("r"), ArgumentMatchers.<String>isNull()))
                .thenReturn(true);

        source = savedRequest(SOURCE_ID);
        when(consultationRequestDao.find(SOURCE_ID)).thenReturn(source);
        when(consultationServiceDao.find(SERVICE_ID)).thenReturn(service(ConsultationServiceDao.ACTIVE));
        when(providerDao.getProvider(REFERRING_PROVIDER)).thenReturn(provider(REFERRING_PROVIDER, "1"));
        when(providerDao.getProvider(LETTERHEAD_PROVIDER)).thenReturn(provider(LETTERHEAD_PROVIDER, "1"));
        when(consultationRequestExtDao.getConsultationRequestExtsByKey(SOURCE_ID, "letterheadTitle")).thenReturn("Dr");
    }

    private EctConsultationFormRequestUtil copy() {
        EctConsultationFormRequestUtil util = new EctConsultationFormRequestUtil();
        assertThat(ConsultationRequestCopy.prefill(loggedInInfo, SOURCE_ID, Locale.ENGLISH, util)).isTrue();
        return util;
    }

    @Nested
    @DisplayName("copied fields")
    class CopiedFields {

        @Test
        @DisplayName("should copy the clinical text, urgency and team as written on the source")
        void shouldCopyClinicalTextUrgencyAndTeam_whenSourceExists() {
            EctConsultationFormRequestUtil util = copy();

            assertThat(util.reasonForConsultation).isEqualTo("reason");
            assertThat(util.clinicalInformation).isEqualTo("clinical");
            assertThat(util.concurrentProblems).isEqualTo("concurrent");
            assertThat(util.currentMedications).isEqualTo("meds as written");
            assertThat(util.allergies).isEqualTo("allergies as written");
            assertThat(util.urgency).isEqualTo("1");
            assertThat(util.sendTo).isEqualTo("KKR");
        }

        @Test
        @DisplayName("should leave the team unset when the source has none")
        void shouldLeaveTeamUnset_whenSourceHasNoTeam() {
            source.setSendTo("-1");

            assertThat(copy().sendTo).isNull();
        }

        @Test
        @DisplayName("should take the patient from the source")
        void shouldTakePatientFromSource_whenSourceExists() {
            assertThat(copy().demoNo).isEqualTo(String.valueOf(PATIENT));
        }

        @Test
        @DisplayName("should not copy the site when multisites is off")
        void shouldNotCopySite_whenMultisitesDisabled() {
            assertThat(copy().siteName).isNull();
        }

        @Test
        @DisplayName("should copy the site when multisites is on")
        void shouldCopySite_whenMultisitesEnabled() {
            OscarProperties properties = OscarProperties.getInstance();
            String original = properties.getProperty("multisites");
            properties.setProperty("multisites", "on");
            try {
                assertThat(copy().siteName).isEqualTo("North Site");
            } finally {
                if (original == null) {
                    properties.remove("multisites");
                } else {
                    properties.setProperty("multisites", original);
                }
            }
        }
    }

    @Nested
    @DisplayName("fields left at their new-request default")
    class DefaultFields {

        @Test
        @DisplayName("should leave the specialist, status, dates, signature and eReferral data unset")
        void shouldLeaveSpecialistStatusDatesSignatureAndEReferralUnset_whenSourceHasThem() {
            EctConsultationFormRequestUtil util = copy();

            assertThat(util.specialist).isNull();
            assertThat(util.status).isNull();
            assertThat(util.referalDate).isNull();
            assertThat(util.appointmentDate).isNull();
            assertThat(util.appointmentHour).isNull();
            assertThat(util.signatureImg).isNull();
            assertThat(util.pwb).isNull();
            assertThat(util.getAppointmentInstructions()).isNull();
            assertThat(util.fdid).isNull();
            assertThat(util.isEReferral).isFalse();
        }
    }

    @Nested
    @DisplayName("service")
    class Service {

        @Test
        @DisplayName("should copy the service while it is active")
        void shouldCopyService_whenServiceIsActive() {
            assertThat(copy().service).isEqualTo(String.valueOf(SERVICE_ID));
        }

        @Test
        @DisplayName("should leave the service unset once it is inactive")
        void shouldLeaveServiceUnset_whenServiceIsInactive() {
            when(consultationServiceDao.find(SERVICE_ID)).thenReturn(service(ConsultationServiceDao.INACTIVE));

            assertThat(copy().service).isNull();
        }

        @Test
        @DisplayName("should leave the service unset when it no longer exists")
        void shouldLeaveServiceUnset_whenServiceIsMissing() {
            when(consultationServiceDao.find(SERVICE_ID)).thenReturn(null);

            assertThat(copy().service).isNull();
        }
    }

    @Nested
    @DisplayName("referring practitioner")
    class ReferringPractitioner {

        @Test
        @DisplayName("should copy the referring practitioner while they are active")
        void shouldCopyReferringPractitioner_whenProviderIsActive() {
            assertThat(copy().providerNo).isEqualTo(REFERRING_PROVIDER);
        }

        @Test
        @DisplayName("should leave the referring practitioner unset once they are inactive")
        void shouldLeaveReferringPractitionerUnset_whenProviderIsInactive() {
            when(providerDao.getProvider(REFERRING_PROVIDER)).thenReturn(provider(REFERRING_PROVIDER, "0"));

            assertThat(copy().providerNo).isNull();
        }
    }

    @Nested
    @DisplayName("letterhead")
    class Letterhead {

        @Test
        @DisplayName("should copy the letterhead and title while its provider is active")
        void shouldCopyLetterhead_whenLetterheadProviderIsActive() {
            EctConsultationFormRequestUtil util = copy();

            assertThat(util.letterheadName).isEqualTo(LETTERHEAD_PROVIDER);
            assertThat(util.letterheadTitle).isEqualTo("Dr");
            assertThat(util.letterheadAddress).isEqualTo("1 Main St");
            assertThat(util.letterheadPhone).isEqualTo("555-555-5555");
            assertThat(util.letterheadFax).isEqualTo("555-555-0000");
        }

        @Test
        @DisplayName("should leave the letterhead unset once its provider is inactive")
        void shouldLeaveLetterheadUnset_whenLetterheadProviderIsInactive() {
            when(providerDao.getProvider(LETTERHEAD_PROVIDER)).thenReturn(provider(LETTERHEAD_PROVIDER, "0"));

            EctConsultationFormRequestUtil util = copy();

            assertThat(util.letterheadName).isNull();
            assertThat(util.letterheadTitle).isNull();
            assertThat(util.letterheadAddress).isNull();
            assertThat(util.letterheadPhone).isNull();
            assertThat(util.letterheadFax).isNull();
        }

        @Test
        @DisplayName("should copy a clinic letterhead, which names no provider")
        void shouldCopyLetterhead_whenLetterheadIsTheClinic() {
            source.setLetterheadName("Hamilton Clinic");

            assertThat(copy().letterheadName).isEqualTo("Hamilton Clinic");
        }

        @Test
        @DisplayName("should leave the letterhead unset when the source has none")
        void shouldLeaveLetterheadUnset_whenSourceHasNone() {
            source.setLetterheadName(null);

            EctConsultationFormRequestUtil util = copy();

            assertThat(util.letterheadName).isNull();
            assertThat(util.letterheadAddress).isNull();
        }
    }

    @Nested
    @DisplayName("copied-from note")
    class CopiedFromNote {

        @Test
        @DisplayName("should name the specialist, referral date and source id")
        void shouldNameSpecialistDateAndId_whenSourceHasSpecialist() {
            assertThat(copy().appointmentNotes)
                    .isEqualTo("Copied from the referral to Dr. C, John dated 2026-01-15 (#288).");
        }

        @Test
        @DisplayName("should name the referral date and source id when the source has no specialist")
        void shouldNameDateAndId_whenSourceHasNoSpecialist() {
            source.setProfessionalSpecialist(null);

            assertThat(copy().appointmentNotes).isEqualTo("Copied from the referral dated 2026-01-15 (#288).");
        }

        @Test
        @DisplayName("should name only the source id when the source has no referral date")
        void shouldNameIdOnly_whenSourceHasNoReferralDate() {
            source.setReferralDate(null);

            assertThat(copy().appointmentNotes).isEqualTo("Copied from referral #288.");
        }

        @Test
        @DisplayName("should not group the digits of a large source id")
        void shouldNotGroupDigits_whenSourceIdIsLarge() throws Exception {
            Integer largeId = 12345;
            when(consultationRequestDao.find(largeId)).thenReturn(savedRequest(largeId));

            EctConsultationFormRequestUtil util = new EctConsultationFormRequestUtil();
            ConsultationRequestCopy.prefill(loggedInInfo, largeId, Locale.ENGLISH, util);

            assertThat(util.appointmentNotes).endsWith("(#12345).");
        }
    }

    @Nested
    @DisplayName("guards")
    class Guards {

        @Test
        @DisplayName("should return false and copy nothing when the source does not exist")
        void shouldReturnFalse_whenSourceDoesNotExist() {
            when(consultationRequestDao.find(SOURCE_ID)).thenReturn(null);

            EctConsultationFormRequestUtil util = new EctConsultationFormRequestUtil();

            assertThat(ConsultationRequestCopy.prefill(loggedInInfo, SOURCE_ID, Locale.ENGLISH, util)).isFalse();
            assertThat(util.demoNo).isNull();
            assertThat(util.reasonForConsultation).isNull();
        }

        @Test
        @DisplayName("should refuse when the user cannot read consultations")
        void shouldThrowSecurityException_whenUserCannotReadConsultations() {
            when(securityInfoManager.hasPrivilege(eq(loggedInInfo), eq("_con"), eq("r"), ArgumentMatchers.<String>isNull()))
                    .thenReturn(false);

            EctConsultationFormRequestUtil util = new EctConsultationFormRequestUtil();

            assertThatThrownBy(() -> ConsultationRequestCopy.prefill(loggedInInfo, SOURCE_ID, Locale.ENGLISH, util))
                    .isInstanceOf(SecurityException.class);
            assertThat(util.reasonForConsultation).isNull();
        }
    }

    /**
     * A saved request with every field set, so each test can show what is and is not copied.
     */
    private static ConsultationRequest savedRequest(Integer id) throws Exception {
        ConsultationRequest request = new ConsultationRequest();
        Field idField = ConsultationRequest.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(request, id);

        request.setDemographicId(PATIENT);
        request.setReasonForReferral("reason");
        request.setClinicalInfo("clinical");
        request.setConcurrentProblems("concurrent");
        request.setCurrentMeds("meds as written");
        request.setAllergies("allergies as written");
        request.setUrgency("1");
        request.setSendTo("KKR");
        request.setSiteName("North Site");
        request.setServiceId(SERVICE_ID);
        request.setProviderNo(REFERRING_PROVIDER);
        request.setReferralDate(REFERRAL_DATE);

        request.setLetterheadName(LETTERHEAD_PROVIDER);
        request.setLetterheadAddress("1 Main St");
        request.setLetterheadPhone("555-555-5555");
        request.setLetterheadFax("555-555-0000");

        ProfessionalSpecialist specialist = new ProfessionalSpecialist();
        specialist.setSalutation("Dr.");
        specialist.setLastName("C");
        specialist.setFirstName("John");
        request.setProfessionalSpecialist(specialist);

        request.setStatus("2");
        request.setStatusText("appointment notes on the source");
        request.setAppointmentDate(new GregorianCalendar(2026, Calendar.NOVEMBER, 2).getTime());
        request.setFollowUpDate(new GregorianCalendar(2026, Calendar.NOVEMBER, 20).getTime());
        request.setSignatureImg("127");
        request.setAppointmentInstructions("instruction-uuid");
        request.setPatientWillBook(true);
        request.setFdid(46);
        request.setSource("eform");
        return request;
    }

    private static ConsultationServices service(String active) {
        ConsultationServices service = new ConsultationServices("Acarology");
        service.setServiceId(SERVICE_ID);
        service.setActive(active);
        return service;
    }

    private static Provider provider(String providerNo, String status) {
        Provider provider = new Provider(providerNo);
        provider.setStatus(status);
        return provider;
    }
}
