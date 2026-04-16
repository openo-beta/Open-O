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

import ca.openosp.openo.casemgmt.model.CaseManagementNoteLink;
import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.dao.DemographicMergeDao;
import ca.openosp.openo.commn.dao.DemographicMergeOperationDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicMerge;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

    private static final String STATUS_INACTIVE = Demographic.PatientStatus.IN.name();
    private static final String STATUS_ACTIVE = Demographic.PatientStatus.AC.name();

    @Autowired
    private SecurityInfoManager securityInfoManager;

    @Autowired
    private DemographicDao demographicDao;

    @Autowired
    private DemographicMergeDao mergeDao;

    @Autowired
    private DemographicMergeOperationDao operationDao;

    /**
     * {@inheritDoc}
     * <p>
     * Execution order for each source demographic (primary first, then secondaries):
     * <ol>
     *   <li>Identity tables — gap-fill on secondary passes</li>
     *   <li>All remaining clinical direct-copy records</li>
     *   <li>All form tables</li>
     *   <li>All parent + derived group tables</li>
     *   <li>Special-case tables requiring cross-group PK maps</li>
     * </ol>
     */
    @Override
    @Transactional
    public Integer merge(LoggedInInfo loggedInInfo, Integer primaryDemographicNo, List<Integer> secondaryDemographicNos) {
        checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║            DEMOGRAPHIC MERGE STARTED                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("  Primary:     " + primaryDemographicNo);
        System.out.println("  Secondaries: " + secondaryDemographicNos);
        System.out.println("  Provider:    " + loggedInInfo.getLoggedInProviderNo());

        validateMergeInputs(primaryDemographicNo, secondaryDemographicNos);
        System.out.println("  [OK] Inputs validated");

        Demographic demographicA = loadAndValidateNotMerged(primaryDemographicNo, "Primary");
        List<Demographic> secondaries = loadAndValidateSecondaries(secondaryDemographicNos);
        System.out.println("  [OK] Loaded primary (A=" + primaryDemographicNo + ") and " + secondaries.size() + " secondary(s) — all active");

        // Clone A → C and obtain C's auto-generated demographic_no
        Demographic demographicC = cloneDemographic(demographicA, loggedInInfo.getLoggedInProviderNo());
        demographicDao.save(demographicC);
        Integer targetDemographicNo = demographicC.getDemographicNo();

        System.out.println("  [OK] Created merged demographic C=" + targetDemographicNo + " (cloned from A=" + primaryDemographicNo + ")");
        logger.debug("DemographicMergeManager.merge: created merged demographic C={} from primary A={}", targetDemographicNo, primaryDemographicNo);

        // Record the merge event before copying data so the audit row is always
        // written as part of the same transaction; it rolls back with everything
        // else if an error occurs.
        saveMergeEvent(DemographicMerge.EventType.MERGE, primaryDemographicNo, secondaryDemographicNos, targetDemographicNo, loggedInInfo.getLoggedInProviderNo());
        System.out.println("  [OK] Merge event recorded in audit table");

        // Primary (A → C): full copy — status update deferred to applyMergeStatuses()
        System.out.println("\n--- COPYING PRIMARY: A=" + primaryDemographicNo + " -> C=" + targetDemographicNo + " ---");
        copyAllDataForSource(primaryDemographicNo, targetDemographicNo, false);
        System.out.println("--- PRIMARY A=" + primaryDemographicNo + " data copied ---");

        // Each secondary (S → C): gap-fill identity + full clinical copy
        int secIdx = 1;
        for (Demographic secondary : secondaries) {
            System.out.println("\n--- COPYING SECONDARY " + secIdx + "/" + secondaries.size() + ": S=" + secondary.getDemographicNo() + " -> C=" + targetDemographicNo + " ---");
            copyAllDataForSource(secondary.getDemographicNo(), targetDemographicNo, true);
            System.out.println("--- SECONDARY S=" + secondary.getDemographicNo() + " data copied ---");
            secIdx++;
        }

        // Audit
        writeAuditEntriesForMerge(loggedInInfo, primaryDemographicNo, secondaries, targetDemographicNo);
        System.out.println("\n  [OK] Audit log entries written");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  DEMOGRAPHIC MERGE DATA COPY COMPLETE                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("  Result: A=" + primaryDemographicNo + " + " + secondaries.size() + " secondary(s) -> C=" + targetDemographicNo);
        System.out.println("  (Status updates will be applied in a separate transaction)");
        return targetDemographicNo;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This runs in its own short transaction so it does not participate in the
     * long-running data-copy transaction. The legacy Hibernate session connection
     * used by {@code DemographicDaoImpl} is acquired fresh here, used for milliseconds,
     * and released — no risk of MySQL {@code wait_timeout} expiry.
     */
    @Override
    @Transactional
    public void applyMergeStatuses(LoggedInInfo loggedInInfo, Integer primaryDemographicNo, List<Integer> secondaryDemographicNos) {
        checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
        System.out.println("\n--- APPLYING MERGE STATUSES ---");
        Demographic demographicA = loadAndValidateExists(primaryDemographicNo, "Primary");
        markInactive(demographicA);
        System.out.println("--- PRIMARY A=" + primaryDemographicNo + " marked as IN ---");

        for (Integer secNo : secondaryDemographicNos) {
            Demographic secondary = loadAndValidateExists(secNo, "Secondary");
            markInactive(secondary);
            System.out.println("--- SECONDARY S=" + secNo + " marked as IN ---");
        }
        System.out.println("--- MERGE STATUSES APPLIED ---");
        logger.debug("applyMergeStatuses: primary={}, secondaries={}", primaryDemographicNo, secondaryDemographicNos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void unmerge(LoggedInInfo loggedInInfo, Integer mergedDemographicNo) {
        checkPrivilege(loggedInInfo, SecurityInfoManager.WRITE);
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║            DEMOGRAPHIC UNMERGE STARTED                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("  Merged demographic C: " + mergedDemographicNo);
        System.out.println("  Provider:             " + loggedInInfo.getLoggedInProviderNo());

        DemographicMerge event = mergeDao.findLatestMergeEventByMergedDemographicNo(mergedDemographicNo);
        if (event == null) {
            System.out.println("  [ERROR] No MERGE event found for C=" + mergedDemographicNo + " — aborting");
            throw new IllegalStateException(
                    "No MERGE event found for demographic " + mergedDemographicNo + " — cannot unmerge");
        }
        System.out.println("  [OK] Found original MERGE event: primary=A=" + event.getPrimaryDemographicNo()
                + ", secondaries=" + event.getSecondaryDemographicNos());

        // Guard against double-unmerge before making any status changes
        Demographic demographicC = loadAndValidateExists(mergedDemographicNo, "Merged");
        if (!STATUS_ACTIVE.equals(demographicC.getPatientStatus())) {
            System.out.println("  [ERROR] C=" + mergedDemographicNo + " is not AC (status=" + demographicC.getPatientStatus() + ") — already unmerged?");
            throw new IllegalStateException(
                    "Merged demographic " + mergedDemographicNo + " is not active — already unmerged?");
        }
        System.out.println("  [OK] Merged demographic C=" + mergedDemographicNo + " is active — proceeding");

        // Restore primary A → AC
        Demographic demographicA = loadAndValidateExists(event.getPrimaryDemographicNo(), "Primary");
        markActive(demographicA);
        System.out.println("  [OK] Restored primary A=" + event.getPrimaryDemographicNo() + " -> AC");

        // Restore each secondary → AC
        for (Integer secondaryNo : event.getSecondaryDemographicNos()) {
            Demographic secondary = loadAndValidateExists(secondaryNo, "Secondary");
            markActive(secondary);
            System.out.println("  [OK] Restored secondary S=" + secondaryNo + " -> AC");
        }

        // Deactivate the merged record C
        demographicC.setPatientStatus(STATUS_INACTIVE);
        demographicC.setPatientStatusDate(new Date());
        demographicDao.save(demographicC);
        System.out.println("  [OK] Deactivated merged demographic C=" + mergedDemographicNo + " -> IN");

        // Record the unmerge event
        saveMergeEvent(DemographicMerge.EventType.UNMERGE, event.getPrimaryDemographicNo(),
                event.getSecondaryDemographicNos(), mergedDemographicNo,
                loggedInInfo.getLoggedInProviderNo());
        System.out.println("  [OK] Unmerge event recorded in audit table");

        logger.debug("DemographicMergeManager.unmerge: restored primary={} and {} secondaries; deactivated C={}",
                event.getPrimaryDemographicNo(), event.getSecondaryDemographicNos().size(), mergedDemographicNo);

        // Audit
        writeAuditEntriesForUnmerge(loggedInInfo, event, mergedDemographicNo);
        System.out.println("  [OK] Audit log entries written");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  DEMOGRAPHIC UNMERGE COMPLETE                               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("  Result: C=" + mergedDemographicNo + " deactivated; A=" + event.getPrimaryDemographicNo()
                + " + " + event.getSecondaryDemographicNos().size() + " secondary(s) restored");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, DemographicMerge> findMergeEventsForDemographics(LoggedInInfo loggedInInfo, List<Integer> mergedDemographicNos) {
        checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
        Map<Integer, DemographicMerge> result = new HashMap<>();
        for (Integer no : mergedDemographicNos) {
            DemographicMerge event = mergeDao.findLatestMergeEventByMergedDemographicNo(no);
            if (event != null) {
                result.put(no, event);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, List<Demographic>> findMergeSourcesForDemographics(LoggedInInfo loggedInInfo, List<Integer> mergedDemographicNos) {
        checkPrivilege(loggedInInfo, SecurityInfoManager.READ);
        Map<Integer, List<Demographic>> result = new HashMap<>();
        for (Integer no : mergedDemographicNos) {
            DemographicMerge event = mergeDao.findLatestMergeEventByMergedDemographicNo(no);
            if (event == null) continue;
            List<Demographic> sources = new ArrayList<>();
            Demographic primary = demographicDao.getClientByDemographicNo(event.getPrimaryDemographicNo());
            if (primary != null) sources.add(primary);
            for (Integer secNo : event.getSecondaryDemographicNos()) {
                Demographic sec = demographicDao.getClientByDemographicNo(secNo);
                if (sec != null) sources.add(sec);
            }
            result.put(no, sources);
        }
        return result;
    }

    private void checkPrivilege(LoggedInInfo loggedInInfo, String privilege) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_demographic", privilege, null)) {
            throw new RuntimeException("missing required sec object (_demographic)");
        }
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", privilege, null)) {
            throw new RuntimeException("missing required sec object (_admin)");
        }
    }

    // -------------------------------------------------------------------------
    // Data copy orchestration
    // -------------------------------------------------------------------------

    /**
     * Copies all clinical data from {@code sourceNo} into {@code targetNo}.
     * This method encapsulates the full copy sequence for one source
     * demographic. It is called once for the primary and once per secondary.
     *
     * @param sourceNo    Integer the source patient demographic_no
     * @param targetNo    Integer the target patient demographic_no (merged record C)
     * @param isSecondary boolean false for primary A pass; true for each secondary pass
     */
    private void copyAllDataForSource(Integer sourceNo, Integer targetNo, boolean isSecondary) {
        System.out.println("\n┌──────────────────────────────────────────────────────────────");
        System.out.println("│ BEGIN DATA COPY: source=" + sourceNo + " -> target=" + targetNo
                + "  [" + (isSecondary ? "SECONDARY pass" : "PRIMARY pass") + "]");
        System.out.println("└──────────────────────────────────────────────────────────────");
        logger.debug("DemographicMergeManager: copying data source={} → target={} isSecondary={}", sourceNo, targetNo, isSecondary);

        // Identity / demographic extension tables
        operationDao.copyIdentityTables(sourceNo, targetNo, isSecondary);

        // Appointments extracted so the PK map is available for casemgmt_note_link remap
        Map<Long, Long> appointmentPkMap = operationDao.copyAppointments(sourceNo, targetNo);

        // Allergies extracted so the PK map is available for casemgmt_note_link remap (table_name = 3)
        Map<Long, Long> allergyPkMap = operationDao.copyAllergies(sourceNo, targetNo);

        // Remaining clinical direct-copy records (appointments and allergies excluded)
        operationDao.copyClinicalDirectRecords(sourceNo, targetNo);

        // Form tables
        operationDao.copyAllForms(sourceNo, targetNo);

        // Parent + derived group tables — capture PK maps for casemgmt_note_link tableId remap
        System.out.println("\n  [GROUP TABLES]");
        operationDao.copyBillingGroup(sourceNo, targetNo);
        Map<Long, Long> requestPkMap = operationDao.copyConsultationsGroup(sourceNo, targetNo);
        Map<Long, Long> drugPkMap = operationDao.copyDrugsGroup(sourceNo, targetNo);
        Map<Long, Long> eformPkMap = operationDao.copyEformGroup(sourceNo, targetNo);
        Map<Long, Long> emailPkMap = operationDao.copyEmailGroup(sourceNo, targetNo);
        operationDao.copyEreferGroup(sourceNo, targetNo);
        operationDao.copyFormBCAR2020Group(sourceNo, targetNo);
        operationDao.copyFormONAREnhancedGroup(sourceNo, targetNo);
        operationDao.copyMeasurementsGroup(sourceNo, targetNo);
        Map<Long, Long> prevPkMap = operationDao.copyPreventionsGroup(sourceNo, targetNo);
        Map<Long, Long> ticklerPkMap = operationDao.copyTicklerGroup(sourceNo, targetNo);

        // Build combined map of copied-entity PK maps for casemgmt_note_link tableId remap.
        // Only non-empty maps are added — the remap helper skips empty maps, but excluding
        // them here avoids allocating unnecessary map entries.
        Map<Integer, Map<Long, Long>> linkedEntityPkMaps = new HashMap<>();
        if (!allergyPkMap.isEmpty()) linkedEntityPkMaps.put(CaseManagementNoteLink.ALLERGIES, allergyPkMap);
        if (!drugPkMap.isEmpty()) linkedEntityPkMaps.put(CaseManagementNoteLink.DRUGS, drugPkMap);
        if (!eformPkMap.isEmpty()) linkedEntityPkMaps.put(CaseManagementNoteLink.EFORMDATA, eformPkMap);
        if (!emailPkMap.isEmpty()) linkedEntityPkMaps.put(CaseManagementNoteLink.EMAIL, emailPkMap);
        if (!prevPkMap.isEmpty()) linkedEntityPkMaps.put(CaseManagementNoteLink.PREVENTIONS, prevPkMap);
        if (!ticklerPkMap.isEmpty()) linkedEntityPkMaps.put(CaseManagementNoteLink.TICKLER, ticklerPkMap);
        System.out.println("  Note-link remap will cover " + linkedEntityPkMaps.size() + " entity type(s) with data");

        // Special-case tables requiring cross-group PK maps
        System.out.println("\n  [SPECIAL-CASE / NOTE TABLES]");
        operationDao.copyConsultationArchiveGroup(sourceNo, targetNo, requestPkMap);
        Map<Long, Long> notePkMap = operationDao.copyCasemgmtNoteGroup(sourceNo, targetNo, appointmentPkMap, linkedEntityPkMaps);
        Map<Long, Long> issuePkMap = operationDao.copyCasemgmtIssueGroup(sourceNo, targetNo);
        operationDao.copyIssueNotesGroup(issuePkMap, notePkMap);

        logger.debug("DemographicMergeManager: finished copying source={} → target={}", sourceNo, targetNo);
        System.out.println("\n┌──────────────────────────────────────────────────────────────");
        System.out.println("│ END DATA COPY: source=" + sourceNo + " -> target=" + targetNo + " COMPLETE");
        System.out.println("└──────────────────────────────────────────────────────────────");
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
        c.setFirstName(source.getFirstName());
        c.setLastName(source.getLastName());
        c.setMiddleNames(source.getMiddleNames());
        c.setPrefName(source.getPrefName());
        c.setAlias(source.getAlias());
        c.setTitle(source.getTitle());
        c.setSex(source.getSex());
        c.setSexDesc(source.getSexDesc());
        c.setGender(source.getGender());
        c.setPronoun(source.getPronoun());
        c.setGenderId(source.getGenderId());
        c.setPronounId(source.getPronounId());
        c.setYearOfBirth(source.getYearOfBirth());
        c.setMonthOfBirth(source.getMonthOfBirth());
        c.setDateOfBirth(source.getDateOfBirth());
        c.setHin(source.getHin());
        c.setVer(source.getVer());
        c.setHcType(source.getHcType());
        c.setHcRenewDate(source.getHcRenewDate());
        c.setPhone(source.getPhone());
        c.setPhone2(source.getPhone2());
        c.setCellPhone(source.getCellPhone());
        c.setPhoneComment(source.getPhoneComment());
        c.setEmail(source.getEmail());
        c.setConsentToUseEmailForCare(source.getConsentToUseEmailForCare());
        c.setAddress(source.getAddress());
        c.setCity(source.getCity());
        c.setProvince(source.getProvince());
        c.setPostal(source.getPostal());
        c.setPreviousAddress(source.getPreviousAddress());
        c.setResidentialAddress(source.getResidentialAddress());
        c.setResidentialCity(source.getResidentialCity());
        c.setResidentialProvince(source.getResidentialProvince());
        c.setResidentialPostal(source.getResidentialPostal());
        c.setProviderNo(source.getProviderNo());
        c.setRosterStatus(source.getRosterStatus());
        c.setRosterDate(source.getRosterDate());
        c.setRosterTerminationDate(source.getRosterTerminationDate());
        c.setRosterTerminationReason(source.getRosterTerminationReason());
        c.setRosterEnrolledTo(source.getRosterEnrolledTo());
        c.setPcnIndicator(source.getPcnIndicator());
        c.setFamilyDoctor(source.getFamilyDoctor());
        c.setFamilyPhysician(source.getFamilyPhysician());
        c.setDateJoined(source.getDateJoined());
        c.setEffDate(source.getEffDate());
        c.setEndDate(source.getEndDate());
        c.setChartNo(source.getChartNo());
        c.setLinks(source.getLinks());
        c.setPatientType(source.getPatientType());
        c.setChildren(source.getChildren());
        c.setSourceOfIncome(source.getSourceOfIncome());
        c.setCitizenship(source.getCitizenship());
        c.setSin(source.getSin());
        c.setSpokenLanguage(source.getSpokenLanguage());
        c.setOfficialLanguage(source.getOfficialLanguage());
        c.setCountryOfOrigin(source.getCountryOfOrigin());
        c.setNewsletter(source.getNewsletter());
        c.setAnonymous(source.getAnonymous());

        // New merged record is always active; extension rows copied separately by copyIdentityTables.
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
     * Loads a demographic and verifies it exists and is active.
     * Patients with status IN cannot participate in a new merge.
     *
     * @param demographicNo Integer the demographic to load
     * @param label         String a human-readable label used in exception messages
     * @return Demographic the loaded demographic
     */
    private Demographic loadAndValidateNotMerged(Integer demographicNo, String label) {
        Demographic demo = loadAndValidateExists(demographicNo, label);
        if (STATUS_INACTIVE.equals(demo.getPatientStatus())) {
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
     * Marks a demographic as inactive (IN) and persists it.
     * Used when deactivating source demographics (A and B) after a merge.
     *
     * @param demographic Demographic the demographic to deactivate
     */
    private void markInactive(Demographic demographic) {
        demographic.setPatientStatus(STATUS_INACTIVE);
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
     * Saves a {@link DemographicMerge} row recording a merge or unmerge action.
     *
     * @param eventType              String "MERGE" or "UNMERGE"
     * @param primaryDemographicNo   Integer the primary demographic number
     * @param secondaryDemographicNos List&lt;Integer&gt; the secondary demographic numbers
     * @param mergedDemographicNo    Integer the merged record's demographic number
     * @param providerNo             String the provider performing the operation
     */
    private void saveMergeEvent(DemographicMerge.EventType eventType, Integer primaryDemographicNo,
                                List<Integer> secondaryDemographicNos,
                                Integer mergedDemographicNo, String providerNo) {
        DemographicMerge event = new DemographicMerge();
        event.setEventType(eventType);
        event.setPrimaryDemographicNo(primaryDemographicNo);
        event.setSecondaryDemographicNo(
                secondaryDemographicNos.stream().map(String::valueOf).collect(Collectors.joining(",")));
        event.setMergedDemographicNo(mergedDemographicNo);
        event.setProviderNo(providerNo);
        event.setEventDate(new Date());
        mergeDao.persist(event);
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

        // Entry on the primary's own audit trail
        LogAction.addLog(loggedInInfo, "DemographicMerge", "demographic",
                String.valueOf(primaryDemographicNo),
                String.valueOf(primaryDemographicNo),
                mergedTo);
        // Entry on C's audit trail showing the primary source
        LogAction.addLog(loggedInInfo, "DemographicMerge", "demographic",
                String.valueOf(targetDemographicNo),
                String.valueOf(targetDemographicNo),
                "mergedFrom=" + primaryDemographicNo);

        for (Demographic secondary : secondaries) {
            // Entry on each secondary's own audit trail
            LogAction.addLog(loggedInInfo, "DemographicMerge", "demographic",
                    String.valueOf(secondary.getDemographicNo()),
                    String.valueOf(secondary.getDemographicNo()),
                    mergedTo);
            // Entry on C's audit trail showing this secondary source
            LogAction.addLog(loggedInInfo, "DemographicMerge", "demographic",
                    String.valueOf(targetDemographicNo),
                    String.valueOf(targetDemographicNo),
                    "mergedFrom=" + secondary.getDemographicNo());
        }
    }

    /**
     * Writes LogAction audit entries for a completed unmerge operation:
     * one entry for the primary A, one per secondary, and one for the deactivated merged record C.
     *
     * @param loggedInInfo        LoggedInInfo the authenticated provider
     * @param event               DemographicMerge the original MERGE event being reversed
     * @param mergedDemographicNo Integer the demographic number of the deactivated merged record C
     */
    private void writeAuditEntriesForUnmerge(LoggedInInfo loggedInInfo, DemographicMerge event,
                                             Integer mergedDemographicNo) {
        String unmergedFrom = "unmergedFrom=" + mergedDemographicNo;

        // Entry on the primary's own audit trail
        LogAction.addLog(loggedInInfo, "DemographicUnmerge", "demographic",
                String.valueOf(event.getPrimaryDemographicNo()),
                String.valueOf(event.getPrimaryDemographicNo()),
                unmergedFrom);
        // Entry on C's audit trail showing the primary was restored
        LogAction.addLog(loggedInInfo, "DemographicUnmerge", "demographic",
                String.valueOf(mergedDemographicNo),
                String.valueOf(mergedDemographicNo),
                "restoredTo=" + event.getPrimaryDemographicNo());

        for (Integer secondaryNo : event.getSecondaryDemographicNos()) {
            // Entry on each secondary's own audit trail
            LogAction.addLog(loggedInInfo, "DemographicUnmerge", "demographic",
                    String.valueOf(secondaryNo),
                    String.valueOf(secondaryNo),
                    unmergedFrom);
            // Entry on C's audit trail showing this secondary was restored
            LogAction.addLog(loggedInInfo, "DemographicUnmerge", "demographic",
                    String.valueOf(mergedDemographicNo),
                    String.valueOf(mergedDemographicNo),
                    "restoredTo=" + secondaryNo);
        }
    }
}
