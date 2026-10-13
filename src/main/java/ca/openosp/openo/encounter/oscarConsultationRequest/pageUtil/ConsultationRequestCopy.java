package ca.openosp.openo.encounter.oscarConsultationRequest.pageUtil;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.IsPropertiesOn;
import ca.openosp.openo.commn.dao.ConsultationRequestDao;
import ca.openosp.openo.commn.dao.ConsultationRequestExtDao;
import ca.openosp.openo.commn.dao.ConsultationServiceDao;
import ca.openosp.openo.commn.model.ConsultationRequest;
import ca.openosp.openo.commn.model.ConsultationServices;
import ca.openosp.openo.commn.model.ProfessionalSpecialist;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.util.ConversionUtils;
import ca.openosp.openo.util.StringUtils;
import ca.openosp.openo.utility.LocaleUtils;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Prefills a new consultation request from a saved one, for the "Copy to New Consultation Request"
 * button on the consultation edit view.
 *
 * <p>The copy is a whitelist written into the page's {@link EctConsultationFormRequestUtil}. Anything
 * not listed keeps its new-request default, so the source's specialist, status, appointment, follow-up
 * date, signature and eReferral data never carry over.</p>
 * <ul>
 *   <li>reason, clinical information, concurrent problems, medications and allergies, as written on
 *       the source</li>
 *   <li>urgency, team (send to) and, with multisites, the site</li>
 *   <li>the service, while it is still active</li>
 *   <li>the referring practitioner, while that provider is still active</li>
 *   <li>the letterhead, unless it names a provider who is no longer active</li>
 * </ul>
 * <p>The appointment notes get one line naming the source request.</p>
 *
 * @since 2026-10-13
 */
public final class ConsultationRequestCopy {

    private static final String MSG_COPIED_FROM = "oscarEncounter.oscarConsultationRequest.ConsultationFormRequest.msgCopiedFrom";
    private static final String MSG_COPIED_FROM_DATED = "oscarEncounter.oscarConsultationRequest.ConsultationFormRequest.msgCopiedFromDated";
    private static final String MSG_COPIED_FROM_SPECIALIST = "oscarEncounter.oscarConsultationRequest.ConsultationFormRequest.msgCopiedFromSpecialist";

    // the team list's "no team" placeholder
    private static final String NO_TEAM = "-1";

    private ConsultationRequestCopy() {
    }

    /**
     * Writes the copied fields of a saved consultation request into a fresh page utility. The target's
     * {@code demoNo} is set to the source's patient, so the caller can check it against the patient the
     * copy was requested for.
     *
     * @param loggedInInfo LoggedInInfo the current user's session information
     * @param sourceRequestId Integer the id of the consultation request to copy from
     * @param locale Locale the locale for the copied-from note
     * @param target EctConsultationFormRequestUtil a page utility not yet established from any request
     * @return boolean true if the source request was found and copied, false if it does not exist
     * @throws SecurityException if the user lacks read access to consultations
     * @since 2026-10-13
     */
    public static boolean prefill(LoggedInInfo loggedInInfo, Integer sourceRequestId, Locale locale, EctConsultationFormRequestUtil target) {
        SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_con", "r", null)) {
            throw new SecurityException("missing required sec object (_con)");
        }

        ConsultationRequestDao consultationRequestDao = SpringUtils.getBean(ConsultationRequestDao.class);
        ConsultationRequest source = consultationRequestDao.find(sourceRequestId);
        if (source == null) {
            return false;
        }

        target.demoNo = String.valueOf(source.getDemographicId());
        target.reasonForConsultation = source.getReasonForReferral();
        target.clinicalInformation = source.getClinicalInfo();
        target.concurrentProblems = source.getConcurrentProblems();
        target.currentMedications = source.getCurrentMeds();
        target.allergies = source.getAllergies();
        target.urgency = source.getUrgency();
        // copying the placeholder would add it to the team list as a team of its own
        target.sendTo = NO_TEAM.equals(source.getSendTo()) ? null : source.getSendTo();
        if (IsPropertiesOn.isMultisitesEnable()) {
            target.siteName = source.getSiteName();
        }

        ConsultationServiceDao consultationServiceDao = SpringUtils.getBean(ConsultationServiceDao.class);
        ConsultationServices service = source.getServiceId() == null ? null : consultationServiceDao.find(source.getServiceId());
        if (service != null && ConsultationServiceDao.ACTIVE.equals(service.getActive())) {
            target.service = String.valueOf(source.getServiceId());
        }

        ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);
        if (isActive(providerDao.getProvider(source.getProviderNo()))) {
            target.providerNo = source.getProviderNo();
        }

        // A letterhead names a provider, the clinic or a program. Only a provider who has left resets it.
        String letterhead = source.getLetterheadName();
        if (!StringUtils.isNullOrEmpty(letterhead)) {
            Provider letterheadProvider = providerDao.getProvider(letterhead);
            if (letterheadProvider == null || isActive(letterheadProvider)) {
                target.letterheadName = letterhead;
                ConsultationRequestExtDao consultationRequestExtDao = SpringUtils.getBean(ConsultationRequestExtDao.class);
                target.letterheadTitle = StringUtils.noNull(consultationRequestExtDao.getConsultationRequestExtsByKey(source.getId(), "letterheadTitle"));
                target.letterheadAddress = StringUtils.noNull(source.getLetterheadAddress());
                target.letterheadPhone = StringUtils.noNull(source.getLetterheadPhone());
                target.letterheadFax = StringUtils.noNull(source.getLetterheadFax());
            }
        }

        target.appointmentNotes = copiedFromNote(source, locale);
        return true;
    }

    private static boolean isActive(Provider provider) {
        return provider != null && "1".equals(provider.getStatus());
    }

    private static String copiedFromNote(ConsultationRequest source, Locale locale) {
        String id = String.valueOf(source.getId());
        if (source.getReferralDate() == null) {
            return MessageFormat.format(LocaleUtils.getMessage(locale, MSG_COPIED_FROM), id);
        }

        String referralDate = ConversionUtils.toDateString(source.getReferralDate());
        ProfessionalSpecialist specialist = source.getProfessionalSpecialist();
        if (specialist == null) {
            return MessageFormat.format(LocaleUtils.getMessage(locale, MSG_COPIED_FROM_DATED), referralDate, id);
        }
        return MessageFormat.format(LocaleUtils.getMessage(locale, MSG_COPIED_FROM_SPECIALIST),
                specialist.getFormattedTitle(), referralDate, id);
    }
}
