//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.demographic.merge;

import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.dao.DemographicMergeEventDao;
import ca.openosp.openo.commn.dao.DemographicMergeOperationDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicMergeEvent;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring-managed implementation of {@link DemographicMergeManager}.
 * <p>
 * Each public method is fully transactional: the entire operation either
 * commits or rolls back together. The DAO layer handles all row-level copies
 * and PK remapping; this class is responsible only for sequencing those calls
 * and managing the patient-status transitions on the {@code demographic} table.
 *
 * @since 2026-03-25
 */
@Service
public class DemographicMergeManagerImpl implements DemographicMergeManager {

    private static final Logger logger = MiscUtils.getLogger();

    private static final String STATUS_MERGED   = Demographic.PatientStatus.MERGED.name();
    private static final String STATUS_INACTIVE = Demographic.PatientStatus.IN.name();
    private static final String STATUS_ACTIVE   = Demographic.PatientStatus.AC.name();

    @Autowired
    private DemographicDao demographicDao;

    @Autowired
    private DemographicMergeEventDao mergeEventDao;

    @Autowired
    private DemographicMergeOperationDao operationDao;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>
     * Execution order for each source demographic (primary first, then secondaries):
     * <ol>
     *   <li>Identity tables (Part 1) — gap-fill on secondary passes</li>
     *   <li>Appointments — extracted so the PK map is available for note-link remap</li>
     *   <li>All remaining clinical direct-copy records (Part 2a)</li>
     *   <li>All form tables (Part 2 form section)</li>
     *   <li>All parent + derived group tables (Part 2b)</li>
     *   <li>Special-case tables requiring cross-group PK maps (Part 2c)</li>
     * </ol>
     */
    @Override
    @Transactional
    public void merge(LoggedInInfo loggedInInfo, Integer primaryDemographicNo, List<Integer> secondaryDemographicNos) {
        validateMergeInputs(primaryDemographicNo, secondaryDemographicNos);

        Demographic demographicA = loadAndValidateNotMerged(primaryDemographicNo, "Primary");
        List<Demographic> secondaries = loadAndValidateSecondaries(secondaryDemographicNos);

        // Clone A → C and obtain C's auto-generated demographic_no
        Demographic demographicC = cloneDemographic(demographicA, loggedInInfo.getLoggedInProviderNo());
        demographicDao.save(demographicC);
        Integer targetDemographicNo = demographicC.getDemographicNo();

        logger.debug("DemographicMergeManager.merge: created merged demographic C={} from primary A={}",
                targetDemographicNo, primaryDemographicNo);

        // Record the merge event before copying data so the audit row is always
        // written as part of the same transaction; it rolls back with everything
        // else if an error occurs.
        saveMergeEvent(DemographicMergeEvent.EventType.MERGE, primaryDemographicNo, secondaryDemographicNos,
                targetDemographicNo, loggedInInfo.getLoggedInProviderNo());

        // Primary (A → C): full copy + mark merged
        copyAllDataForSource(primaryDemographicNo, targetDemographicNo, false);
        markMerged(demographicA);

        // Each secondary (S → C): gap-fill identity + full clinical copy + mark merged
        for (Demographic secondary : secondaries) {
            copyAllDataForSource(secondary.getDemographicNo(), targetDemographicNo, true);
            markMerged(secondary);
        }

        // Audit
        writeAuditEntriesForMerge(loggedInInfo, primaryDemographicNo, secondaries, targetDemographicNo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void unmerge(LoggedInInfo loggedInInfo, Integer mergedDemographicNo) {
        DemographicMergeEvent event = mergeEventDao.findLatestMergeEventByMergedDemographicNo(mergedDemographicNo);
        if (event == null) {
            throw new IllegalStateException(
                    "No MERGE event found for demographic " + mergedDemographicNo + " — cannot unmerge");
        }

        // Restore primary A → AC
        Demographic demographicA = loadAndValidateExists(event.getPrimaryDemographicNo(), "Primary");
        markActive(demographicA);

        // Restore each secondary → AC
        for (Integer secondaryNo : event.getSecondaryDemographicNos()) {
            Demographic secondary = loadAndValidateExists(secondaryNo, "Secondary");
            markActive(secondary);
        }

        // Deactivate the merged record C (guard against double-unmerge)
        Demographic demographicC = loadAndValidateExists(mergedDemographicNo, "Merged");
        if (!STATUS_ACTIVE.equals(demographicC.getPatientStatus())) {
            throw new IllegalStateException(
                    "Merged demographic " + mergedDemographicNo + " is not active — already unmerged?");
        }
        demographicC.setPatientStatus(STATUS_INACTIVE);
        demographicC.setPatientStatusDate(new Date());
        demographicDao.save(demographicC);

        // Record the unmerge event
        saveMergeEvent(DemographicMergeEvent.EventType.UNMERGE, event.getPrimaryDemographicNo(),
                event.getSecondaryDemographicNos(), mergedDemographicNo,
                loggedInInfo.getLoggedInProviderNo());

        logger.debug("DemographicMergeManager.unmerge: restored primary={} and {} secondaries; deactivated C={}",
                event.getPrimaryDemographicNo(), event.getSecondaryDemographicNos().size(), mergedDemographicNo);

        // Audit
        writeAuditEntriesForUnmerge(loggedInInfo, event, mergedDemographicNo);
    }

    // -------------------------------------------------------------------------
    // Data copy orchestration
    // -------------------------------------------------------------------------

    /**
     * Copies all clinical data from {@code sourceNo} into {@code targetNo}.
     * This method encapsulates the full Part 1 + Part 2 copy sequence for one source
     * demographic. It is called once for the primary and once per secondary.
     *
     * @param sourceNo    Integer the source patient demographic_no
     * @param targetNo    Integer the target patient demographic_no (merged record C)
     * @param isSecondary boolean false for primary A pass; true for each secondary pass
     */
    private void copyAllDataForSource(Integer sourceNo, Integer targetNo, boolean isSecondary) {
        logger.debug("DemographicMergeManager: copying data source={} → target={} isSecondary={}",
                sourceNo, targetNo, isSecondary);

        // Part 1 — identity / demographic extension tables
        operationDao.copyIdentityTables(sourceNo, targetNo, isSecondary);

        // Appointments are extracted so the PK map is available for casemgmt_note_link remap
        Map<Long, Long> appointmentPkMap = operationDao.copyAppointments(sourceNo, targetNo);

        // Part 2a — remaining clinical direct-copy records (appointments excluded)
        operationDao.copyClinicalDirectRecords(sourceNo, targetNo);

        // Form tables
        operationDao.copyAllForms(sourceNo, targetNo);

        // Part 2b — parent + derived group tables
        operationDao.copyBillingGroup(sourceNo, targetNo);
        Map<Long, Long> requestPkMap = operationDao.copyConsultationsGroup(sourceNo, targetNo);
        operationDao.copyDrugsGroup(sourceNo, targetNo);
        operationDao.copyEformGroup(sourceNo, targetNo);
        operationDao.copyEmailGroup(sourceNo, targetNo);
        operationDao.copyEreferGroup(sourceNo, targetNo);
        operationDao.copyFormBCAR2020Group(sourceNo, targetNo);
        operationDao.copyFormONAREnhancedGroup(sourceNo, targetNo);
        operationDao.copyMeasurementsGroup(sourceNo, targetNo);
        operationDao.copyPreventionsGroup(sourceNo, targetNo);
        operationDao.copyTicklerGroup(sourceNo, targetNo);

        // Part 2c — special-case tables requiring cross-group PK maps
        operationDao.copyConsultationArchiveGroup(sourceNo, targetNo, requestPkMap);
        Map<Long, Long> notePkMap = operationDao.copyCasemgmtNoteGroup(sourceNo, targetNo, appointmentPkMap);
        Map<Long, Long> issuePkMap = operationDao.copyCasemgmtIssueGroup(sourceNo, targetNo);
        operationDao.copyIssueNotesGroup(issuePkMap, notePkMap);

        logger.debug("DemographicMergeManager: finished copying source={} → target={}", sourceNo, targetNo);
    }

    // -------------------------------------------------------------------------
    // Demographic cloning
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@link Demographic} whose persistent fields are copied from
     * {@code source}. The new record has no {@code demographicNo} (so the database
     * assigns one on persist), {@code patientStatus = "AC"}, and {@code patientStatusDate}
     * set to now. Extension rows ({@code demographicExt}, {@code demographiccust},
     * {@code other_id}) are handled separately by {@link DemographicMergeOperationDao#copyIdentityTables}.
     *
     * @param source     Demographic the primary demographic whose identity fields are cloned
     * @param providerNo String the provider performing the merge (written to lastUpdateUser)
     * @return Demographic a new transient Demographic ready to be persisted
     */
    private Demographic cloneDemographic(Demographic source, String providerNo) {
        Demographic c = new Demographic();

        // Name
        c.setFirstName(source.getFirstName());
        c.setLastName(source.getLastName());
        c.setMiddleNames(source.getMiddleNames());
        c.setPrefName(source.getPrefName());
        c.setAlias(source.getAlias());
        c.setTitle(source.getTitle());

        // Sex / gender / pronoun
        c.setSex(source.getSex());
        c.setSexDesc(source.getSexDesc());
        c.setGender(source.getGender());
        c.setPronoun(source.getPronoun());
        c.setGenderId(source.getGenderId());
        c.setPronounId(source.getPronounId());

        // Date of birth
        c.setYearOfBirth(source.getYearOfBirth());
        c.setMonthOfBirth(source.getMonthOfBirth());
        c.setDateOfBirth(source.getDateOfBirth());

        // Health card
        c.setHin(source.getHin());
        c.setVer(source.getVer());
        c.setHcType(source.getHcType());
        c.setHcRenewDate(source.getHcRenewDate());

        // Contact
        c.setPhone(source.getPhone());
        c.setPhone2(source.getPhone2());
        c.setCellPhone(source.getCellPhone());
        c.setPhoneComment(source.getPhoneComment());
        c.setEmail(source.getEmail());
        c.setConsentToUseEmailForCare(source.getConsentToUseEmailForCare());

        // Address
        c.setAddress(source.getAddress());
        c.setCity(source.getCity());
        c.setProvince(source.getProvince());
        c.setPostal(source.getPostal());
        c.setPreviousAddress(source.getPreviousAddress());
        c.setResidentialAddress(source.getResidentialAddress());
        c.setResidentialCity(source.getResidentialCity());
        c.setResidentialProvince(source.getResidentialProvince());
        c.setResidentialPostal(source.getResidentialPostal());

        // Provider / roster
        c.setProviderNo(source.getProviderNo());
        c.setRosterStatus(source.getRosterStatus());
        c.setRosterDate(source.getRosterDate());
        c.setRosterTerminationDate(source.getRosterTerminationDate());
        c.setRosterTerminationReason(source.getRosterTerminationReason());
        c.setRosterEnrolledTo(source.getRosterEnrolledTo());
        c.setPcnIndicator(source.getPcnIndicator());
        c.setFamilyDoctor(source.getFamilyDoctor());
        c.setFamilyPhysician(source.getFamilyPhysician());

        // Dates
        c.setDateJoined(source.getDateJoined());
        c.setEffDate(source.getEffDate());
        c.setEndDate(source.getEndDate());

        // Administrative
        c.setChartNo(source.getChartNo());
        c.setLinks(source.getLinks());
        c.setPatientType(source.getPatientType());

        // Socioeconomic / personal
        c.setChildren(source.getChildren());
        c.setSourceOfIncome(source.getSourceOfIncome());
        c.setCitizenship(source.getCitizenship());
        c.setSin(source.getSin());
        c.setSpokenLanguage(source.getSpokenLanguage());
        c.setOfficialLanguage(source.getOfficialLanguage());
        c.setCountryOfOrigin(source.getCountryOfOrigin());
        c.setNewsletter(source.getNewsletter());

        // New merged record is always active; extension rows copied separately by copyIdentityTables.
        // anonymous / headRecord / subRecord are intentionally omitted — C is a fresh active record.
        c.setPatientStatus(STATUS_ACTIVE);
        c.setPatientStatusDate(new Date());

        // Audit
        c.setLastUpdateUser(providerNo);
        c.setLastUpdateDate(new Date());

        return c;
    }

    // -------------------------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------------------------

    /**
     * Validates that the merge inputs are structurally sound before any data is loaded.
     *
     * @param primaryDemographicNo    Integer the primary demographic number
     * @param secondaryDemographicNos List&lt;Integer&gt; the secondary demographic numbers
     */
    private void validateMergeInputs(Integer primaryDemographicNo, List<Integer> secondaryDemographicNos) {
        if (primaryDemographicNo == null) {
            throw new IllegalArgumentException("primaryDemographicNo must not be null");
        }
        if (secondaryDemographicNos == null || secondaryDemographicNos.isEmpty()) {
            throw new IllegalArgumentException("At least one secondary demographic is required");
        }
        if (secondaryDemographicNos.contains(primaryDemographicNo)) {
            throw new IllegalArgumentException(
                    "Primary demographic " + primaryDemographicNo + " cannot also be a secondary");
        }
        long distinctCount = secondaryDemographicNos.stream().distinct().count();
        if (distinctCount != secondaryDemographicNos.size()) {
            throw new IllegalArgumentException("secondaryDemographicNos contains duplicate entries");
        }
    }

    /**
     * Loads a demographic and verifies it exists and has not already been merged.
     * Patients with status MERGED or IN cannot participate in a new merge.
     *
     * @param demographicNo Integer the demographic to load
     * @param label         String a human-readable label used in exception messages
     * @return Demographic the loaded demographic
     */
    private Demographic loadAndValidateNotMerged(Integer demographicNo, String label) {
        Demographic demo = loadAndValidateExists(demographicNo, label);
        if (STATUS_MERGED.equals(demo.getPatientStatus()) || STATUS_INACTIVE.equals(demo.getPatientStatus())) {
            throw new IllegalStateException(
                    label + " demographic " + demographicNo + " is not active and cannot be merged");
        }
        return demo;
    }

    /**
     * Loads a demographic and verifies it exists.
     *
     * @param demographicNo Integer the demographic to load
     * @param label         String a human-readable label used in exception messages
     * @return Demographic the loaded demographic
     */
    private Demographic loadAndValidateExists(Integer demographicNo, String label) {
        Demographic demo = demographicDao.getClientByDemographicNo(demographicNo);
        if (demo == null) {
            throw new IllegalArgumentException(label + " demographic not found: " + demographicNo);
        }
        return demo;
    }

    /**
     * Loads and validates all secondary demographics.
     *
     * @param secondaryDemographicNos List&lt;Integer&gt; the secondary demographic numbers
     * @return List&lt;Demographic&gt; the loaded, validated secondary demographics in input order
     */
    private List<Demographic> loadAndValidateSecondaries(List<Integer> secondaryDemographicNos) {
        List<Demographic> secondaries = new ArrayList<>();
        for (Integer secondaryNo : secondaryDemographicNos) {
            secondaries.add(loadAndValidateNotMerged(secondaryNo, "Secondary"));
        }
        return secondaries;
    }

    // -------------------------------------------------------------------------
    // Status helpers
    // -------------------------------------------------------------------------

    /**
     * Marks a demographic as MERGED and persists it.
     *
     * @param demographic Demographic the demographic to mark as merged
     */
    private void markMerged(Demographic demographic) {
        demographic.setPatientStatus(STATUS_MERGED);
        demographic.setPatientStatusDate(new Date());
        demographicDao.save(demographic);
    }

    /**
     * Marks a demographic as active (AC) and persists it.
     *
     * @param demographic Demographic the demographic to reactivate
     */
    private void markActive(Demographic demographic) {
        demographic.setPatientStatus(STATUS_ACTIVE);
        demographic.setPatientStatusDate(new Date());
        demographicDao.save(demographic);
    }

    // -------------------------------------------------------------------------
    // Event persistence
    // -------------------------------------------------------------------------

    /**
     * Saves a {@link DemographicMergeEvent} row recording a merge or unmerge action.
     *
     * @param eventType              String "MERGE" or "UNMERGE"
     * @param primaryDemographicNo   Integer the primary demographic number
     * @param secondaryDemographicNos List&lt;Integer&gt; the secondary demographic numbers
     * @param mergedDemographicNo    Integer the merged record's demographic number
     * @param providerNo             String the provider performing the operation
     */
    private void saveMergeEvent(DemographicMergeEvent.EventType eventType, Integer primaryDemographicNo,
                                List<Integer> secondaryDemographicNos,
                                Integer mergedDemographicNo, String providerNo) {
        DemographicMergeEvent event = new DemographicMergeEvent();
        event.setEventType(eventType);
        event.setPrimaryDemographicNo(primaryDemographicNo);
        event.setSecondaryDemographicNo(
                secondaryDemographicNos.stream().map(String::valueOf).collect(Collectors.joining(",")));
        event.setMergedDemographicNo(mergedDemographicNo);
        event.setProviderNo(providerNo);
        event.setEventDate(new Date());
        mergeEventDao.persist(event);
    }

    // -------------------------------------------------------------------------
    // Audit logging
    // -------------------------------------------------------------------------

    /**
     * Writes LogAction audit entries for a completed merge operation:
     * one entry for the primary A, one per secondary, and one for the new merged record C.
     *
     * @param loggedInInfo         LoggedInInfo the authenticated provider
     * @param primaryDemographicNo Integer the primary demographic number
     * @param secondaries          List&lt;Demographic&gt; the secondary demographics
     * @param targetDemographicNo  Integer the new merged record's demographic number
     */
    private void writeAuditEntriesForMerge(LoggedInInfo loggedInInfo, Integer primaryDemographicNo,
                                           List<Demographic> secondaries, Integer targetDemographicNo) {
        String mergedTo = "mergedTo=" + targetDemographicNo;
        LogAction.addLogSynchronous(loggedInInfo, "DemographicMerge",
                "primary=" + primaryDemographicNo + " " + mergedTo);

        for (Demographic secondary : secondaries) {
            LogAction.addLog(loggedInInfo, "DemographicMerge", "demographic",
                    String.valueOf(secondary.getDemographicNo()),
                    String.valueOf(secondary.getDemographicNo()),
                    mergedTo);
        }

        LogAction.addLog(loggedInInfo, "DemographicMerge", "demographic",
                String.valueOf(targetDemographicNo),
                String.valueOf(targetDemographicNo),
                "mergedFrom=" + primaryDemographicNo);
    }

    /**
     * Writes LogAction audit entries for a completed unmerge operation:
     * one entry for the primary A, one per secondary, and one for the deactivated merged record C.
     *
     * @param loggedInInfo        LoggedInInfo the authenticated provider
     * @param event               DemographicMergeEvent the original MERGE event being reversed
     * @param mergedDemographicNo Integer the demographic number of the deactivated merged record C
     */
    private void writeAuditEntriesForUnmerge(LoggedInInfo loggedInInfo, DemographicMergeEvent event,
                                             Integer mergedDemographicNo) {
        String unmergedFrom = "unmergedFrom=" + mergedDemographicNo;
        LogAction.addLogSynchronous(loggedInInfo, "DemographicUnmerge",
                "primary=" + event.getPrimaryDemographicNo() + " " + unmergedFrom);

        for (Integer secondaryNo : event.getSecondaryDemographicNos()) {
            LogAction.addLog(loggedInInfo, "DemographicUnmerge", "demographic",
                    String.valueOf(secondaryNo),
                    String.valueOf(secondaryNo),
                    unmergedFrom);
        }

        LogAction.addLog(loggedInInfo, "DemographicUnmerge", "demographic",
                String.valueOf(mergedDemographicNo),
                String.valueOf(mergedDemographicNo),
                "deactivated; primary=" + event.getPrimaryDemographicNo());
    }
}
