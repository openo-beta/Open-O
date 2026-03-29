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
package ca.openosp.openo.commn.dao;

// --- Clinical direct-copy entities ---
import ca.openosp.openo.casemgmt.model.CaseManagementNoteLink;
import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.Consent;
import ca.openosp.openo.commn.model.CtlDocument;
import ca.openosp.openo.commn.model.CtlDocumentPK;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicArchive;
import ca.openosp.openo.commn.model.DemographicContact;
import ca.openosp.openo.commn.model.DemographicCust;
import ca.openosp.openo.commn.model.DemographicCustArchive;
import ca.openosp.openo.commn.model.DemographicExt;
import ca.openosp.openo.commn.model.DemographicPharmacy;
import ca.openosp.openo.commn.model.DigitalSignature;
import ca.openosp.openo.commn.model.Dxresearch;
import ca.openosp.openo.commn.model.Episode;
import ca.openosp.openo.commn.model.FaxJob;
import ca.openosp.openo.commn.model.FlowSheetDrug;
import ca.openosp.openo.commn.model.FlowSheetDx;
import ca.openosp.openo.commn.model.Immunizations;
import ca.openosp.openo.commn.model.MsgDemoMap;
import ca.openosp.openo.commn.model.MsgIntegratorDemoMap;
import ca.openosp.openo.commn.model.OLISQueryLog;
import ca.openosp.openo.commn.model.OLISResults;
import ca.openosp.openo.commn.model.OtherId;
import ca.openosp.openo.commn.model.PatientLabRouting;
import ca.openosp.openo.commn.model.Prescription;
import ca.openosp.openo.commn.model.Relationships;
import ca.openosp.openo.commn.model.TableModification;
import ca.openosp.openo.commn.model.WaitingList;

// --- Billing group ---
import ca.openosp.openo.commn.model.BillingONCHeader1;
import ca.openosp.openo.commn.model.BillingOnTransaction;

// --- Consultation group ---
import ca.openosp.openo.commn.model.ConsultationRequest;
import ca.openosp.openo.commn.model.ConsultationRequestArchive;
import ca.openosp.openo.commn.model.ProfessionalSpecialist;

// --- Drug group ---
import ca.openosp.openo.commn.model.Drug;

// --- EForm group ---
import ca.openosp.openo.commn.model.EFormData;

// --- Email group ---
import ca.openosp.openo.commn.model.EmailLog;

// --- ERefer group ---
import ca.openosp.openo.commn.model.EReferAttachment;

// --- BCAR 2020 group ---
import ca.openosp.openo.form.model.FormBCAR2020;

// --- HRM ---
import ca.openosp.openo.hospitalReportManager.model.HRMDocumentToDemographic;

// --- Measurements group ---
import ca.openosp.openo.commn.model.Measurement;

// --- Prevention group ---
import ca.openosp.openo.commn.model.Prevention;

// --- Tickler group ---
import ca.openosp.openo.commn.model.Tickler;

// --- CaseMgmt special entities ---
import ca.openosp.openo.commn.model.CaseMgmtCpp;
import ca.openosp.openo.commn.model.CaseMgmtIssue;
import ca.openosp.openo.commn.model.CaseMgmtIssueNotes;
import ca.openosp.openo.commn.model.CaseMgmtNote;

import ca.openosp.openo.utility.MiscUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Hibernate-based implementation of {@link DemographicMergeOperationDao}.
 * <p>
 * Uses the load → detach → null-PK → set-demo → persist pattern for all parent tables so that
 * {@code GenerationType.IDENTITY} assigns fresh PKs. The old→new PK map is captured from the
 * returned entity after {@code flush()}, which is safe under concurrent inserts.
 * <p>
 * JDBC is used only for two targeted exceptions:
 * <ul>
 *   <li>{@code formBCAR2020Text} — composite {@code @IdClass} (formId + pageNo + field)</li>
 *   <li>{@code formONAREnhancedRecord*} — 100+ column tables with no entity classes</li>
 * </ul>
 * Optional-module tables ({@code [BC]}, {@code [ON]}) are guarded by {@link #tableExists(String)}.
 *
 * @since 2026-03-19
 */
@Repository
@Transactional
public class DemographicMergeOperationDaoImpl implements DemographicMergeOperationDao {

    private static final Logger logger = MiscUtils.getLogger();

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // -------------------------------------------------------------------------
    // Schema cache — DatabaseMetaData lookups cached in-process
    // -------------------------------------------------------------------------

    private final ConcurrentHashMap<String, Boolean> tableExistsCache = new ConcurrentHashMap<>();

    /**
     * Returns true if the named table exists in the current database.
     * Result is cached in-process so repeated calls across a single merge are cheap.
     *
     * @param tableName String the table name to check (case-sensitive as stored in DB metadata)
     * @return boolean true if the table exists
     */
    private boolean tableExists(String tableName) {
        return tableExistsCache.computeIfAbsent(tableName, t ->
            Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) con -> {
                DatabaseMetaData meta = con.getMetaData();
                try (ResultSet rs = meta.getTables(null, null, t, new String[]{"TABLE"})) {
                    return rs.next();
                }
            }))
        );
    }

    // -------------------------------------------------------------------------
    // Core Hibernate copy helpers
    // -------------------------------------------------------------------------

    /**
     * Generic Hibernate copy for parent tables (auto-increment PK).
     * <p>
     * Loads all rows for {@code sourceDemoNo} using the given JPQL entity name and demo field,
     * detaches each, clears the PK, updates the demographic FK, re-persists, and flushes.
     * After flush the entity contains the database-assigned new PK.
     * Returns a map of old PK → new PK.
     *
     * @param <T>           the entity type
     * @param entityClass   Class&lt;T&gt; entity class token
     * @param jpqlName      String JPQL entity name (simple class name unless @Entity(name) is set)
     * @param demoField     String the JPQL field name for the demographic FK
     * @param sourceDemoNo  Integer source demographic number
     * @param targetDemoNo  Integer target demographic number
     * @param getPk         Function to read the current PK as Long
     * @param clearPk       Consumer that nulls/resets the PK (e.g. {@code e -> e.setId(null)})
     * @param setDemoNo     BiConsumer that sets the demographic FK to the target value
     * @return Map&lt;Long, Long&gt; mapping old PK to new PK for every copied row
     */
    private <T> Map<Long, Long> copyEntityRows(Class<T> entityClass, String jpqlName, String demoField, Integer sourceDemoNo, Integer targetDemoNo, Function<T, Long> getPk, Consumer<T> clearPk, BiConsumer<T, Integer> setDemoNo) {

        List<T> rows = entityManager.createQuery("SELECT e FROM " + jpqlName + " e WHERE e." + demoField + " = :demo", entityClass)
            .setParameter("demo", sourceDemoNo)
            .getResultList();

        if (rows.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Long> pkMap = new HashMap<>();
        long t0 = System.currentTimeMillis();
        for (T row : rows) {
            long oldPk = getPk.apply(row);
            entityManager.detach(row);
            clearPk.accept(row);
            setDemoNo.accept(row, targetDemoNo);
            entityManager.persist(row);
            entityManager.flush();
            pkMap.put(oldPk, getPk.apply(row));
        }

        System.out.println("    [" + jpqlName + "] copied " + pkMap.size() + " rows  (source=" + sourceDemoNo + " -> target=" + targetDemoNo + ")  [" + (System.currentTimeMillis() - t0) + "ms]");
        logger.debug("copyEntityRows: entity='{}', source={}, target={}, rows={}", jpqlName, sourceDemoNo, targetDemoNo, pkMap.size());
        return pkMap;
    }

    /**
     * Issues one bulk JPQL UPDATE per entry in {@code pkMap}, remapping
     * {@code casemgmt_note_link.table_id} from the old entity PK to the new entity PK
     * for all copied note rows ({@code note_id IN newNoteIds}) of the given {@code linkType}.
     * <p>
     * O(entities of that type) — one UPDATE per entity, filtering by the full set of copied
     * note PKs via {@code IN} clause. No entities are loaded into memory.
     *
     * @param newNoteIds list of new note PKs (the target patient's copied notes)
     * @param linkType   Integer the {@code CaseManagementNoteLink} integer constant
     * @param pkMap      Map&lt;Long, Long&gt; old entity PK → new entity PK
     */
    private void remapNoteLinkTableIds(List<Integer> newNoteIds, Integer linkType, Map<Long, Long> pkMap) {
        for (Map.Entry<Long, Long> entry : pkMap.entrySet()) {
            entityManager.createQuery("UPDATE CaseMgmtNoteLink e SET e.tableId = :newId WHERE e.noteId IN :noteIds AND e.tableName = :type AND e.tableId = :oldId")
                .setParameter("newId",   entry.getValue().intValue())
                .setParameter("noteIds", newNoteIds)
                .setParameter("type",    linkType)
                .setParameter("oldId",   entry.getKey().intValue())
                .executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // ── Appointments + clinical direct-copy records
    // -------------------------------------------------------------------------

    @Override
    public Map<Long, Long> copyAppointments(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY APPOINTMENTS: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // appointment
        Map<Long, Long> apptPkMap = copyEntityRows(Appointment.class, "Appointment", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // appointmentArchive — HQL bulk INSERT: archive PKs are not linked from casemgmt_note_link;
        // leaf table, PK never captured. One SQL statement replaces N × IDENTITY-flush round-trips.
        entityManager.createQuery(
            "INSERT INTO AppointmentArchive (appointmentNo, providerNo, appointmentDate, startTime, endTime, " +
            "name, demographicNo, programId, notes, reason, location, resources, type, style, billing, status, " +
            "createDateTime, updateDateTime, creator, lastUpdateUser, remarks, importedStatus, urgency, " +
            "creatorSecurityId, bookingSource) " +
            "SELECT a.appointmentNo, a.providerNo, a.appointmentDate, a.startTime, a.endTime, " +
            "a.name, :targetDemo, a.programId, a.notes, a.reason, a.location, a.resources, a.type, a.style, " +
            "a.billing, a.status, a.createDateTime, a.updateDateTime, a.creator, a.lastUpdateUser, a.remarks, " +
            "a.importedStatus, a.urgency, a.creatorSecurityId, a.bookingSource " +
            "FROM AppointmentArchive a WHERE a.demographicNo = :sourceDemo")
            .setParameter("targetDemo", targetDemoNo)
            .setParameter("sourceDemo", sourceDemoNo)
            .executeUpdate();

        logger.debug("copyAppointments: source={}, target={}, appt rows={}", sourceDemoNo, targetDemoNo, apptPkMap.size());
        System.out.println("=== COPY APPOINTMENTS DONE: " + apptPkMap.size() + " appointment(s) copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return apptPkMap;
    }

    @Override
    public Map<Long, Long> copyAllergies(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY ALLERGIES: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        Map<Long, Long> allergyPkMap = copyEntityRows(Allergy.class, "Allergy", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));
        logger.debug("copyAllergies: source={}, target={}, allergy rows={}", sourceDemoNo, targetDemoNo, allergyPkMap.size());
        System.out.println("=== COPY ALLERGIES DONE: " + allergyPkMap.size() + " allergy record(s) copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return allergyPkMap;
    }

    @Override
    public void copyClinicalDirectRecords(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY CLINICAL DIRECT RECORDS: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // allergies — copied by copyAllergies(); the manager calls that separately so the
        // PK map is available for casemgmt_note_link tableId remap (table_name = 3)

        // appointment and appointmentArchive are handled by copyAppointments() so the PK map
        // can be passed to copyCasemgmtNoteGroup for note-link remap

        // casemgmt_cpp — demo field is String (varchar in DB); handled inline
        long tCpp = System.currentTimeMillis();
        List<CaseMgmtCpp> cppRows = entityManager.createQuery("SELECT e FROM CaseMgmtCpp e WHERE e.demographicNo = :demo", CaseMgmtCpp.class)
            .setParameter("demo", String.valueOf(sourceDemoNo))
            .getResultList();
        for (CaseMgmtCpp cpp : cppRows) {
            entityManager.detach(cpp);
            cpp.setId(null);
            cpp.setDemographicNo(String.valueOf(targetDemoNo));
            entityManager.persist(cpp);
            entityManager.flush();
        }
        System.out.println("    [CaseMgmtCpp] copied " + cppRows.size() + " row(s)  [" + (System.currentTimeMillis() - tCpp) + "ms]");
        logger.debug("copyClinicalDirectRecords: CaseMgmtCpp rows={}", cppRows.size());

        // Consent
        copyEntityRows(Consent.class, "Consent", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // ctl_document — composite @EmbeddedId (module + documentNo); module_id = demographicNo.
        // New-object pattern: construct a fresh transient CtlDocument so Hibernate issues a direct INSERT.
        // Dedup: if both the primary and a secondary patient link to the same documentNo, the primary
        // pass already inserted [documentNo,demographic,targetDemoNo]. Skip duplicates to avoid
        // EntityExistsException ("different object with same identifier already in session").
        long tCtl = System.currentTimeMillis();
        List<CtlDocument> sourceDocs = entityManager.createQuery("SELECT d FROM CtlDocument d WHERE d.id.module = 'demographic' AND d.id.moduleId = :mid", CtlDocument.class)
            .setParameter("mid", sourceDemoNo)
            .getResultList();
        int ctlDocCopied = 0;
        for (CtlDocument src : sourceDocs) {
            CtlDocumentPK newPk = new CtlDocumentPK("demographic", targetDemoNo, src.getId().getDocumentNo());
            if (entityManager.find(CtlDocument.class, newPk) != null) {
                logger.debug("copyClinicalDirectRecords: CtlDocument documentNo={} already linked to target={}, skipping", src.getId().getDocumentNo(), targetDemoNo);
                continue;
            }
            CtlDocument copy = new CtlDocument();
            copy.setId(newPk);
            copy.setStatus(src.getStatus());
            entityManager.persist(copy);
            ctlDocCopied++;
        }
        if (ctlDocCopied > 0) {
            entityManager.flush();
            System.out.println("    [CtlDocument] copied " + ctlDocCopied + " row(s)  [" + (System.currentTimeMillis() - tCtl) + "ms]");
        }

        // demographicArchive — Long PK
        copyEntityRows(DemographicArchive.class, "DemographicArchive", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // DemographicContact
        copyEntityRows(DemographicContact.class, "DemographicContact", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // demographicPharmacy
        copyEntityRows(DemographicPharmacy.class, "DemographicPharmacy", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // DigitalSignature — demo field is "demographicId"
        copyEntityRows(DigitalSignature.class, "DigitalSignature", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        // dxresearch — PK java field is "dxresearchNo"
        copyEntityRows(Dxresearch.class, "Dxresearch", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getDxresearchNo(), e -> e.setDxresearchNo(null),
                (e, d) -> e.setDemographicNo(d));

        // Episode
        copyEntityRows(Episode.class, "Episode", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // faxes
        copyEntityRows(FaxJob.class, "FaxJob", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // flowsheet_drug
        copyEntityRows(FlowSheetDrug.class, "FlowSheetDrug", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // flowsheet_dx
        copyEntityRows(FlowSheetDx.class, "FlowSheetDx", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // HRMDocumentToDemographic
        copyEntityRows(HRMDocumentToDemographic.class, "HRMDocumentToDemographic", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // immunizations
        copyEntityRows(Immunizations.class, "Immunizations", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // measurementsDeleted — HQL bulk INSERT: leaf table, PK never captured, no child FK.
        // Replaces N × IDENTITY-flush round-trips with one server-side SQL statement.
        // Expected: ~40,000 rows across both passes → ~19.7 min → < 1 sec.
        long tMd = System.currentTimeMillis();
        int mdCount = entityManager.createQuery(
            "INSERT INTO MeasurementsDeleted (demographicNo, type, providerNo, dataField, measuringInstruction, " +
            "comments, dateObserved, dateEntered, dateDeleted, originalId) " +
            "SELECT :targetDemo, m.type, m.providerNo, m.dataField, m.measuringInstruction, " +
            "m.comments, m.dateObserved, m.dateEntered, m.dateDeleted, m.originalId " +
            "FROM MeasurementsDeleted m WHERE m.demographicNo = :sourceDemo")
            .setParameter("targetDemo", targetDemoNo)
            .setParameter("sourceDemo", sourceDemoNo)
            .executeUpdate();
        System.out.println("    [MeasurementsDeleted] copied " + mdCount + " rows  (source=" + sourceDemoNo + " -> target=" + targetDemoNo + ")  [HQL bulk]  [" + (System.currentTimeMillis() - tMd) + "ms]");
        logger.debug("copyClinicalDirectRecords: MeasurementsDeleted rows={} [HQL bulk]", mdCount);

        // msgDemoMap — Long PK, demo field is "demographic_no"
        copyEntityRows(MsgDemoMap.class, "MsgDemoMap", "demographic_no",
                sourceDemoNo, targetDemoNo,
                e -> e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographic_no(d));

        // msgIntegratorDemoMap — demo field is "sourceDemographicNo"
        copyEntityRows(MsgIntegratorDemoMap.class, "MsgIntegratorDemoMap", "sourceDemographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setSourceDemographicNo(d));

        // OLISQueryLog — [ON] optional module
        if (tableExists("OLISQueryLog")) {
            copyEntityRows(OLISQueryLog.class, "OLISQueryLog", "demographicNo",
                    sourceDemoNo, targetDemoNo,
                    e -> (long) e.getId(), e -> e.setId(null),
                    (e, d) -> e.setDemographicNo(d));
        }

        // OLISResults — [ON] optional module
        if (tableExists("OLISResults")) {
            copyEntityRows(OLISResults.class, "OLISResults", "demographicNo",
                    sourceDemoNo, targetDemoNo,
                    e -> (long) e.getId(), e -> e.setId(null),
                    (e, d) -> e.setDemographicNo(d));
        }

        // patientLabRouting
        copyEntityRows(PatientLabRouting.class, "PatientLabRouting", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // prescription — demo field is "demographicId"
        copyEntityRows(Prescription.class, "Prescription", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        // relationships
        copyEntityRows(Relationships.class, "Relationships", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // table_modification
        copyEntityRows(TableModification.class, "TableModification", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // waitingList
        copyEntityRows(WaitingList.class, "WaitingList", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        logger.debug("copyClinicalDirectRecords: source={}, target={} — complete", sourceDemoNo, targetDemoNo);
        System.out.println("=== COPY CLINICAL DIRECT RECORDS DONE (source=" + sourceDemoNo + " -> target=" + targetDemoNo + ")  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }

    // -------------------------------------------------------------------------
    // ── Form tables
    // -------------------------------------------------------------------------

    @Override
    public void copyAllForms(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY ALL FORMS: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // Tables covered: form, form2MinWalk, formAdf, formAdfV2, formAlpha, formAnnual,
        //   formAnnualV2, formAR, formBCAR[BC], formBCAR2007[BC], formBCAR2012[BC],
        //   formBCBirthSumMo[BC], formBCBirthSumMo2008[BC], formBCClientChartChecklist[BC],
        //   formBCHP[BC], formBCINR[BC], formBCNewBorn[BC], formBCNewBorn2008[BC], formBPMH[BC],
        //   formCaregiver, formCESD, formchf, formConsult, formCostQuestionnaire, formCounseling,
        //   formDischargeSummary (bigint PK), formECARES[BC], formFalls, formfollowup (bigint PK),
        //   formGripStrength, formGrowth0_36, formGrowthChart, formHomeFalls, formImmunAllergy,
        //   formIntakeHx, formIntakeInfo, formintakea, formintakeb, formintakec,
        //   formInternetAccess, formLabReq, formLabReq07[ON], formLabReq10[ON],
        //   formLateLifeFDIDisability, formLateLifeFDIFunction, formMentalHealth,
        //   formMentalHealthForm1 (bigint), formMentalHealthForm14 (bigint), formMentalHealthForm42 (bigint),
        //   formMMSE, formNoShowPolicy, formONAR[ON], formONAREnhanced[ON], formovulation[ON] (bigint),
        //   formPalliativeCare, formPeriMenopausal, formPositionHazard[ON],
        //   formreceptionassessment, formRhImmuneGlobulin, formRourke, formRourke2006,
        //   formRourke2009[BC], formRourke2017, formRourke2020, formSatisfactionScale,
        //   formSelfAdministered, formSelfAssessment, formSelfEfficacy, formSelfManagement,
        //   formSF36, formSF36Caregiver, formTreatmentPref, formType2Diabetes, formVTForm
        // Universal form tables — all use JDBC row-by-row INSERT with GeneratedKeyHolder.
        // tableExists() guards every call so missing optional tables are silently skipped.
        // formBCAR2020 is omitted here — handled by copyFormBCAR2020Group() which also copies formBCAR2020Text.
        // formONAREnhancedRecord/Ext1/Ext2 are omitted — handled by copyFormONAREnhancedGroup().

        // --- core / generic forms ---
        copyFormJdbc("form", "form_no", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("form2MinWalk", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formAdf", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formAdfV2", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formAlpha", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formAnnual", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formAnnualV2", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formAR", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formCaregiver", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formCESD", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formchf", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formConsult", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formCostQuestionnaire", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formCounseling", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formDischargeSummary", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formFalls", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formfollowup", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formGripStrength", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formGrowth0_36", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formGrowthChart", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formHomeFalls", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formImmunAllergy", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formIntakeHx", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formIntakeInfo", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formintakea", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formintakeb", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formintakec", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formInternetAccess", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formLabReq", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formLateLifeFDIDisability", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formLateLifeFDIFunction", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formMentalHealth", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formMentalHealthForm1", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formMentalHealthForm14", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formMentalHealthForm42", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formMMSE", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formNoShowPolicy", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formPalliativeCare", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formPeriMenopausal", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formreceptionassessment", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formRhImmuneGlobulin", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formRourke", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formRourke2006", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formRourke2017", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formRourke2020", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formSatisfactionScale", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formSelfAdministered", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formSelfAssessment", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formSelfEfficacy", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formSelfManagement", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formSF36", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formSF36Caregiver", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formTreatmentPref", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formType2Diabetes", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formVTForm", "ID", sourceDemoNo, targetDemoNo, true);

        // --- BC optional forms ---
        copyFormJdbc("formBCAR", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBCAR2007", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBCAR2012", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBCBirthSumMo", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBCBirthSumMo2008", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBCClientChartChecklist", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBCHP", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBCINR", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBCNewBorn", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBCNewBorn2008", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formBPMH", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formECARES", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formRourke2009", "ID", sourceDemoNo, targetDemoNo, true);

        // --- ON optional forms ---
        copyFormJdbc("formLabReq07", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formLabReq10", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formONAR", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formONAREnhanced", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formovulation", "ID", sourceDemoNo, targetDemoNo, true);
        copyFormJdbc("formPositionHazard", "ID", sourceDemoNo, targetDemoNo, true);

        logger.debug("copyAllForms: source={}, target={} — complete", sourceDemoNo, targetDemoNo);
        System.out.println("=== COPY ALL FORMS DONE (source=" + sourceDemoNo + " -> target=" + targetDemoNo + ")  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }

    /**
     * Copies all rows for the given form table belonging to {@code sourceDemoNo} into new rows
     * for {@code targetDemoNo} using JDBC row-by-row INSERT with {@link GeneratedKeyHolder}.
     * Silently skips if the table does not exist in this installation.
     * If {@code copyBooleanValues} is true, copies corresponding {@code form_boolean_value} rows.
     *
     * @param tableName         String form table name
     * @param pkCol             String primary key column name (usually "ID"; "form_no" for the {@code form} table)
     * @param sourceDemoNo      Integer source demographic number
     * @param targetDemoNo      Integer target demographic number
     * @param copyBooleanValues boolean true to also copy form_boolean_value rows after the form copy
     */
    private void copyFormJdbc(String tableName, String pkCol,
            Integer sourceDemoNo, Integer targetDemoNo, boolean copyBooleanValues) {
        long t0 = System.currentTimeMillis();
        Map<Long, Long> pkMap = copyFormJdbcWithMap(tableName, pkCol, sourceDemoNo, targetDemoNo);
        if (!pkMap.isEmpty() && copyBooleanValues) {
            copyFormBooleanValues(pkMap, tableName);
        }
        if (!pkMap.isEmpty()) {
            System.out.println("    [form: " + tableName + "] copied " + pkMap.size() + " row(s)  (source=" + sourceDemoNo + " -> target=" + targetDemoNo + ")  [" + (System.currentTimeMillis() - t0) + "ms]");
            logger.debug("copyFormJdbc: table='{}', source={}, target={}, rows={}", tableName, sourceDemoNo, targetDemoNo, pkMap.size());
        }
    }

    /**
     * Variant of {@link #copyFormJdbc} that returns the old→new PK map without copying
     * {@code form_boolean_value} rows. Used by {@link #copyFormONAREnhancedGroup} which needs
     * the PK map to remap child Ext1/Ext2 table rows.
     *
     * @param tableName    String form table name
     * @param pkCol        String primary key column name
     * @param sourceDemoNo Integer source demographic number
     * @param targetDemoNo Integer target demographic number
     * @return Map&lt;Long, Long&gt; old PK → new PK for every copied row
     */
    private Map<Long, Long> copyFormJdbcWithMap(String tableName, String pkCol,
            Integer sourceDemoNo, Integer targetDemoNo) {
        if (!tableExists(tableName)) return Collections.emptyMap();

        List<Map<String, Object>> sourceRows = jdbcTemplate.queryForList(
            "SELECT * FROM `" + tableName + "` WHERE `demographic_no` = ?", sourceDemoNo);

        if (sourceRows.isEmpty()) return Collections.emptyMap();

        Map<Long, Long> pkMap = new HashMap<>();
        for (Map<String, Object> row : sourceRows) {
            long oldId = ((Number) row.get(pkCol)).longValue();
            List<String> columns = new ArrayList<>(row.keySet());

            StringBuilder ins = new StringBuilder("INSERT INTO `").append(tableName).append("` (");
            StringBuilder vals = new StringBuilder(" VALUES (");
            Object[] params = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                String col = columns.get(i);
                if (i > 0) { ins.append(", "); vals.append(", "); }
                ins.append("`").append(col).append("`");
                vals.append("?");
                if (col.equalsIgnoreCase(pkCol)) {
                    params[i] = null;
                } else if (col.equalsIgnoreCase("demographic_no")) {
                    params[i] = targetDemoNo;
                } else {
                    params[i] = row.get(col);
                }
            }
            ins.append(")"); vals.append(")");
            String sql = ins.toString() + vals.toString();

            KeyHolder keyHolder = new GeneratedKeyHolder();
            final Object[] finalParams = params;
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < finalParams.length; i++) ps.setObject(i + 1, finalParams[i]);
                return ps;
            }, keyHolder);
            pkMap.put(oldId, keyHolder.getKey().longValue());
        }
        return pkMap;
    }

    /**
     * Copies {@code form_boolean_value} rows for the given form using the provided old-to-new PK map.
     *
     * @param pkMap    Map&lt;Long, Long&gt; old form row id → new form row id
     * @param formName String the form table name used as the form_name discriminator
     */
    private void copyFormBooleanValues(Map<Long, Long> pkMap, String formName) {
        if (pkMap.isEmpty()) return;
        String sql = "INSERT INTO form_boolean_value (form_name, form_id, field_name, value) " +
                     "SELECT form_name, ?, field_name, value " +
                     "FROM form_boolean_value WHERE form_name = ? AND form_id = ?";
        List<Object[]> batchArgs = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : pkMap.entrySet()) {
            batchArgs.add(new Object[]{entry.getValue(), formName, entry.getKey()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    // -------------------------------------------------------------------------
    // ── Parent + derived group copy methods
    // -------------------------------------------------------------------------

    @Override
    public void copyBillingGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY BILLING GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        if (!tableExists("billing_on_cheader1")) {
            System.out.println("=== COPY BILLING GROUP DONE: billing_on_cheader1 table not present, skipped  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        // billing_on_cheader1 (parent)
        // Clear billingItems before persist: the @OneToMany(cascade=ALL) collection holds old detached
        // BillingONItem entities that would trigger "detached entity passed to persist" on cascade.
        // Items are copied separately below via HQL bulk INSERT.
        Map<Long, Long> ch1PkMap = copyEntityRows(BillingONCHeader1.class, "BillingONCHeader1", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> { e.setId(null); e.getBillingItems().clear(); },
                (e, d) -> e.setDemographicNo(d));

        if (ch1PkMap.isEmpty()) {
            System.out.println("=== COPY BILLING GROUP DONE: no billing header rows found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        // billing_on_cheader2 — per-parent HQL bulk INSERT: child PKs not needed downstream.
        for (Map.Entry<Long, Long> entry : ch1PkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO BillingONCHeader2 (ch1Id, transactionId, recordId, hin, lastName, firstName, sex, province, timestamp) " +
                "SELECT :newId, e.transactionId, e.recordId, e.hin, e.lastName, e.firstName, e.sex, e.province, e.timestamp " +
                "FROM BillingONCHeader2 e WHERE e.ch1Id = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        // billing_on_item — per-parent HQL bulk INSERT: child PKs not needed downstream.
        // @PrePersist lifecycle callback does not fire on HQL bulk operations — lastEditDT copied as-is.
        for (Map.Entry<Long, Long> entry : ch1PkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO BillingONItem (ch1Id, transcId, recId, serviceCode, fee, serviceCount, serviceDate, dx, dx1, dx2, status, lastEditDT) " +
                "SELECT :newId, e.transcId, e.recId, e.serviceCode, e.fee, e.serviceCount, e.serviceDate, e.dx, e.dx1, e.dx2, e.status, e.lastEditDT " +
                "FROM BillingONItem e WHERE e.ch1Id = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        // billing_on_transaction — child of cheader1 via ch1Id; also has demographicNo
        // Copy rows first, then remap ch1Id to target cheader1 PKs via JPQL bulk UPDATE.
        copyEntityRows(BillingOnTransaction.class, "BillingOnTransaction", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(0),
                (e, d) -> e.setDemographicNo(d));

        // Remap ch1Id in newly copied transaction rows using JPQL bulk UPDATE
        for (Map.Entry<Long, Long> ch1 : ch1PkMap.entrySet()) {
            entityManager.createQuery("UPDATE BillingOnTransaction e SET e.ch1Id = :newCh1Id WHERE e.demographicNo = :demo AND e.ch1Id = :oldCh1Id")
                .setParameter("newCh1Id", ch1.getValue().intValue())
                .setParameter("demo", targetDemoNo)
                .setParameter("oldCh1Id", ch1.getKey().intValue())
                .executeUpdate();
        }

        logger.debug("copyBillingGroup: source={}, target={}, ch1 rows={}", sourceDemoNo, targetDemoNo, ch1PkMap.size());
        System.out.println("=== COPY BILLING GROUP DONE: " + ch1PkMap.size() + " billing header(s) + children copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }

    @Override
    public Map<Long, Long> copyConsultationsGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY CONSULTATIONS GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // consultationRequests (parent) — demo field is "demographicId"
        Map<Long, Long> requestPkMap = copyEntityRows(ConsultationRequest.class, "ConsultationRequest", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (requestPkMap.isEmpty()) {
            System.out.println("=== COPY CONSULTATIONS GROUP DONE: no consultation requests found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return requestPkMap;
        }

        // consultationRequestExt — per-parent HQL bulk INSERT: child PKs not needed downstream.
        // Only parent requestPkMap flows to copyConsultationArchiveGroup — not these child PKs.
        // Expected: ~267 rows across both passes → ~3.9 sec → < 1 sec.
        for (Map.Entry<Long, Long> entry : requestPkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO ConsultationRequestExt (requestId, key, value, dateCreated) " +
                "SELECT :newId, e.key, e.value, e.dateCreated " +
                "FROM ConsultationRequestExt e WHERE e.requestId = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        logger.debug("copyConsultationsGroup: source={}, target={}, request rows={}", sourceDemoNo, targetDemoNo, requestPkMap.size());
        System.out.println("=== COPY CONSULTATIONS GROUP DONE: " + requestPkMap.size() + " consultation request(s) + ext rows copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return requestPkMap;
    }

    @Override
    public Map<Long, Long> copyDrugsGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY DRUGS GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // drugs (parent) — demo field is "demographicId"
        Map<Long, Long> drugPkMap = copyEntityRows(Drug.class, "Drug", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (drugPkMap.isEmpty()) {
            System.out.println("=== COPY DRUGS GROUP DONE: no drug records found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return drugPkMap;
        }

        // drugReason — per-parent HQL bulk INSERT: child PKs not needed downstream.
        // demographicNo also remapped to targetDemoNo in the same statement.
        // Expected: ~9 rows → minor, but eliminates flush overhead.
        for (Map.Entry<Long, Long> entry : drugPkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO DrugReason (drugId, codingSystem, code, comments, primaryReasonFlag, archivedFlag, archivedReason, providerNo, demographicNo, dateCoded) " +
                "SELECT :newId, e.codingSystem, e.code, e.comments, e.primaryReasonFlag, e.archivedFlag, e.archivedReason, e.providerNo, :targetDemo, e.dateCoded " +
                "FROM DrugReason e WHERE e.drugId = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("targetDemo", targetDemoNo)
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        logger.debug("copyDrugsGroup: source={}, target={}, drug rows={}", sourceDemoNo, targetDemoNo, drugPkMap.size());
        System.out.println("=== COPY DRUGS GROUP DONE: " + drugPkMap.size() + " drug(s) + drug reasons copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return drugPkMap;
    }

    @Override
    public Map<Long, Long> copyEformGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY EFORM GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // eform_data (parent) — demo field is "demographicId"
        Map<Long, Long> eformPkMap = copyEntityRows(EFormData.class, "EFormData", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (eformPkMap.isEmpty()) {
            System.out.println("=== COPY EFORM GROUP DONE: no eform data found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return eformPkMap;
        }

        // eform_values — per-parent HQL bulk INSERT: child PKs not needed downstream.
        // demographicId is also remapped to targetDemoNo in the same statement.
        // Expected: ~15,559 rows across both passes → ~195 sec → < 5 sec.
        for (Map.Entry<Long, Long> entry : eformPkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO EFormValue (formDataId, formId, demographicId, varName, varValue) " +
                "SELECT :newId, e.formId, :targetDemo, e.varName, e.varValue " +
                "FROM EFormValue e WHERE e.formDataId = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("targetDemo", targetDemoNo)
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        logger.debug("copyEformGroup: source={}, target={}, eform rows={}", sourceDemoNo, targetDemoNo, eformPkMap.size());
        System.out.println("=== COPY EFORM GROUP DONE: " + eformPkMap.size() + " eform record(s) + values copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return eformPkMap;
    }

    @Override
    public Map<Long, Long> copyEmailGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY EMAIL GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // emailLog — @ManyToOne Demographic; query by demographic.id
        List<EmailLog> logs = entityManager.createQuery("SELECT e FROM EmailLog e WHERE e.demographic.id = :demo", EmailLog.class)
            .setParameter("demo", sourceDemoNo)
            .getResultList();

        if (logs.isEmpty()) {
            System.out.println("=== COPY EMAIL GROUP DONE: no email logs found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return Collections.emptyMap();
        }

        Map<Long, Long> emailPkMap = new HashMap<>();
        Demographic targetRef = entityManager.getReference(Demographic.class, targetDemoNo);
        int totalAttachmentsCopied = 0;
        long tLogCopy = System.currentTimeMillis();

        for (EmailLog log : logs) {
            int oldLogId = log.getId();

            entityManager.detach(log);
            log.setId(null);
            log.setDemographic(targetRef);
            // Clear the @OneToMany list to prevent CascadeType.PERSIST on old attachments
            log.setEmailAttachments(null);
            entityManager.persist(log);
            entityManager.flush();

            int newLogId = log.getId();
            emailPkMap.put((long) oldLogId, (long) newLogId);

            // EmailAttachment — native INSERT-SELECT: @ManyToOne FK (logId) is a plain integer
            // column; fileSize is @Transient so it has no DB column. Child PKs not needed downstream.
            totalAttachmentsCopied += entityManager.createNativeQuery(
                "INSERT INTO emailAttachment (logId, fileName, filePath, documentType, documentId) " +
                "SELECT :newLogId, fileName, filePath, documentType, documentId " +
                "FROM emailAttachment WHERE logId = :oldLogId")
                .setParameter("newLogId", newLogId)
                .setParameter("oldLogId", oldLogId)
                .executeUpdate();
        }

        System.out.println("    [EmailLog] copied " + logs.size() + " rows  [" + (System.currentTimeMillis() - tLogCopy) + "ms]");
        System.out.println("    [EmailAttachment (child)] copied " + totalAttachmentsCopied + " rows  [native bulk]");
        logger.debug("copyEmailGroup: source={}, target={}, log rows={}, attachment rows={}", sourceDemoNo, targetDemoNo, logs.size(), totalAttachmentsCopied);
        System.out.println("=== COPY EMAIL GROUP DONE: " + logs.size() + " email log(s) + " + totalAttachmentsCopied + " attachment(s) copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return emailPkMap;
    }

    @Override
    public void copyEreferGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY EREFER GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // erefer_attachment (parent)
        // Replace attachments with a fresh empty list before persist: the @OneToMany(cascade=PERSIST) collection
        // is LAZY — calling clear() on the uninitialized PersistentBag after detach throws LazyInitializationException.
        // Setting a new ArrayList avoids touching the PersistentBag entirely and prevents cascade persist of old rows.
        // Data rows are copied separately below via the new-object pattern.
        Map<Long, Long> attachPkMap = copyEntityRows(EReferAttachment.class, "EReferAttachment", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> { e.setId(null); e.setAttachments(new ArrayList<>()); },
                (e, d) -> e.setDemographicNo(d));

        if (attachPkMap.isEmpty()) {
            System.out.println("=== COPY EREFER GROUP DONE: no eRefer attachments found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        // erefer_attachment_data — composite PK (erefer_attachment_id, lab_id, lab_type); no auto-increment.
        // Native INSERT-SELECT per parent: bypasses per-row persist+flush round-trips.
        // Native query used because the @ManyToOne FK on the composite PK makes HQL INSERT complex.
        for (Map.Entry<Long, Long> entry : attachPkMap.entrySet()) {
            entityManager.createNativeQuery(
                "INSERT INTO erefer_attachment_data (erefer_attachment_id, lab_id, lab_type) " +
                "SELECT :newId, e.lab_id, e.lab_type " +
                "FROM erefer_attachment_data e WHERE e.erefer_attachment_id = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        logger.debug("copyEreferGroup: source={}, target={}, attachment rows={}", sourceDemoNo, targetDemoNo, attachPkMap.size());
        System.out.println("=== COPY EREFER GROUP DONE: " + attachPkMap.size() + " eRefer attachment(s) + data rows copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }

    @Override
    public void copyFormBCAR2020Group(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY FORM BCAR2020 GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        if (!tableExists("formBCAR2020")) {
            System.out.println("=== COPY FORM BCAR2020 GROUP DONE: table not present, skipped  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        // formBCAR2020 (parent) — Hibernate copy; PK java field is "formId"
        Map<Long, Long> formPkMap = copyEntityRows(FormBCAR2020.class, "FormBCAR2020", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        if (formPkMap.isEmpty()) {
            System.out.println("=== COPY FORM BCAR2020 GROUP DONE: no rows found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        // formBCAR2020Text — composite IdClass (formId + pageNo + field); JDBC INSERT...SELECT
        for (Map.Entry<Long, Long> entry : formPkMap.entrySet()) {
            jdbcTemplate.update(
                "INSERT INTO formBCAR2020Text (form_id, page_no, field, value, last_modified) " +
                "SELECT ?, page_no, field, value, last_modified " +
                "FROM formBCAR2020Text WHERE form_id = ?",
                entry.getValue().intValue(), entry.getKey().intValue());
        }

        logger.debug("copyFormBCAR2020Group: source={}, target={}, form rows={}", sourceDemoNo, targetDemoNo, formPkMap.size());
        System.out.println("=== COPY FORM BCAR2020 GROUP DONE: " + formPkMap.size() + " form(s) + text rows copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }

    @Override
    public void copyFormONAREnhancedGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY FORM ONAR ENHANCED GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // All three tables have 400–1400 columns — no entity classes; reuse copyFormJdbc for parent.
        // Ext1/Ext2 are child tables keyed by the parent id (not demographic_no), so they need
        // a separate inline copy using the parent PK map.
        if (!tableExists("formONAREnhancedRecord")) {
            System.out.println("=== COPY FORM ONAR ENHANCED GROUP DONE: table not present, skipped  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        // Copy main table via shared helper; capture old→new PK map for child remapping
        Map<Long, Long> onarPkMap = copyFormJdbcWithMap("formONAREnhancedRecord", "ID", sourceDemoNo, targetDemoNo);
        if (onarPkMap.isEmpty()) {
            System.out.println("=== COPY FORM ONAR ENHANCED GROUP DONE: no rows found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        // formONAREnhancedRecordExt1 and Ext2 — child rows keyed by parent id
        for (String extTable : new String[]{"formONAREnhancedRecordExt1", "formONAREnhancedRecordExt2"}) {
            if (!tableExists(extTable)) continue;
            int extRowsCopied = 0;
            for (Map.Entry<Long, Long> entry : onarPkMap.entrySet()) {
                List<Map<String, Object>> extRows = jdbcTemplate.queryForList(
                    "SELECT * FROM `" + extTable + "` WHERE id = ?", entry.getKey().intValue());
                for (Map<String, Object> extRow : extRows) {
                    List<String> extCols = new ArrayList<>(extRow.keySet());
                    StringBuilder ei = new StringBuilder("INSERT INTO `").append(extTable).append("` (");
                    StringBuilder ev = new StringBuilder(" VALUES (");
                    Object[] ep = new Object[extCols.size()];
                    for (int i = 0; i < extCols.size(); i++) {
                        String col = extCols.get(i);
                        if (i > 0) { ei.append(", "); ev.append(", "); }
                        ei.append("`").append(col).append("`"); ev.append("?");
                        // id = new parent PK; all other columns unchanged
                        ep[i] = col.equalsIgnoreCase("id") ? entry.getValue().intValue() : extRow.get(col);
                    }
                    ei.append(")"); ev.append(")");
                    jdbcTemplate.update(ei.toString() + ev.toString(), ep);
                    extRowsCopied++;
                }
            }
            if (extRowsCopied > 0) {
                System.out.println("    [" + extTable + "] copied " + extRowsCopied + " child row(s)  [" + (System.currentTimeMillis() - t0) + "ms]");
            }
        }

        logger.debug("copyFormONAREnhancedGroup: source={}, target={}, record rows={}", sourceDemoNo, targetDemoNo, onarPkMap.size());
        System.out.println("=== COPY FORM ONAR ENHANCED GROUP DONE: " + onarPkMap.size() + " record(s) + Ext1/Ext2 rows copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }

    @Override
    public void copyMeasurementsGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY MEASUREMENTS GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // measurements (parent) — demo field is "demographicId"
        Map<Long, Long> measurePkMap = copyEntityRows(Measurement.class, "Measurement", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (measurePkMap.isEmpty()) {
            System.out.println("=== COPY MEASUREMENTS GROUP DONE: no measurement records found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        // measurementsExt — per-parent HQL bulk INSERT: child PKs not needed downstream.
        // One SQL statement per parent replaces N × IDENTITY-flush round-trips per child row.
        // Expected: ~17,730 rows across both passes → ~465 sec → < 5 sec.
        for (Map.Entry<Long, Long> entry : measurePkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO MeasurementsExt (measurementId, keyVal, val) " +
                "SELECT :newId, e.keyVal, e.val " +
                "FROM MeasurementsExt e WHERE e.measurementId = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        logger.debug("copyMeasurementsGroup: source={}, target={}, measure rows={}", sourceDemoNo, targetDemoNo, measurePkMap.size());
        System.out.println("=== COPY MEASUREMENTS GROUP DONE: " + measurePkMap.size() + " measurement(s) + ext rows copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }

    @Override
    public Map<Long, Long> copyPreventionsGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY PREVENTIONS GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // preventions (parent) — demo field is "demographicId"
        Map<Long, Long> prevPkMap = copyEntityRows(Prevention.class, "Prevention", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (prevPkMap.isEmpty()) {
            System.out.println("=== COPY PREVENTIONS GROUP DONE: no prevention records found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return prevPkMap;
        }

        // preventionsExt — per-parent HQL bulk INSERT: child PKs not needed downstream.
        // The @ManyToOne prevention field is insertable=false — only preventionId is written.
        // Expected: ~508 rows across both passes → ~14.7 sec → < 1 sec.
        for (Map.Entry<Long, Long> entry : prevPkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO PreventionExt (preventionId, keyval, val) " +
                "SELECT :newId, e.keyval, e.val " +
                "FROM PreventionExt e WHERE e.preventionId = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        logger.debug("copyPreventionsGroup: source={}, target={}, prevention rows={}", sourceDemoNo, targetDemoNo, prevPkMap.size());
        System.out.println("=== COPY PREVENTIONS GROUP DONE: " + prevPkMap.size() + " prevention(s) + ext rows copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return prevPkMap;
    }

    @Override
    public Map<Long, Long> copyTicklerGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY TICKLER GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // tickler (parent)
        Map<Long, Long> ticklerPkMap = copyEntityRows(Tickler.class, "Tickler", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        if (ticklerPkMap.isEmpty()) {
            System.out.println("=== COPY TICKLER GROUP DONE: no ticklers found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return ticklerPkMap;
        }

        // tickler_link — per-parent HQL bulk INSERT: child PKs not needed downstream.
        for (Map.Entry<Long, Long> entry : ticklerPkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO TicklerLink (ticklerNo, tableName, tableId) " +
                "SELECT :newId, e.tableName, e.tableId " +
                "FROM TicklerLink e WHERE e.ticklerNo = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        logger.debug("copyTicklerGroup: source={}, target={}, tickler rows={}", sourceDemoNo, targetDemoNo, ticklerPkMap.size());
        System.out.println("=== COPY TICKLER GROUP DONE: " + ticklerPkMap.size() + " tickler(s) + links copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return ticklerPkMap;
    }

    // -------------------------------------------------------------------------
    // ── Special-case group methods
    // -------------------------------------------------------------------------

    @Override
    public void copyConsultationArchiveGroup(Integer sourceDemoNo, Integer targetDemoNo, Map<Long, Long> requestPkMap) {
        System.out.println("\n=== COPY CONSULTATION ARCHIVE GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // consultationRequestsArchive (parent) — demo field is "demographicId"
        // ProfessionalSpecialist has @ManyToOne(cascade=ALL). The query below loads all archive
        // rows as managed entities. If any of them remain managed during a subsequent flush(),
        // Hibernate will cascade PERSIST_ON_FLUSH from each managed row to its PS field.
        // If PS was detached by the cascade from an earlier detach(a) inside the copy loop,
        // the flush throws "detached entity passed to persist: ProfessionalSpecialist".
        // Fix: detach every archive row immediately after loading, before the copy loop.
        // This ensures only newly-persisted copies (with managed PS from find()) are in session.
        List<ConsultationRequestArchive> archiveRows = entityManager.createQuery(
                "SELECT e FROM ConsultationRequestArchive e WHERE e.demographicId = :demo",
                ConsultationRequestArchive.class)
            .setParameter("demo", sourceDemoNo)
            .getResultList();

        // Upfront batch-detach: removes every loaded archive row from the session.
        // cascade=ALL on professionalSpecialist means detach(a) also cascade-detaches
        // the PS instance held by the first archive row that references each unique PS.
        // Subsequent detach(a) calls for the same PS are no-ops (already absent).
        for (ConsultationRequestArchive a : archiveRows) {
            entityManager.detach(a);
        }

        // Cache managed ProfessionalSpecialist instances to avoid a SELECT per row.
        // find() guarantees a managed entity (L1 cache hit if already loaded, DB SELECT otherwise).
        Map<Integer, ProfessionalSpecialist> managedPsCache = new HashMap<>();

        Map<Long, Long> archivePkMap = new HashMap<>();
        for (ConsultationRequestArchive a : archiveRows) {
            long oldPk = (long) a.getId();
            Integer psId = a.getSpecialistId();
            a.setId(null);
            a.setDemographicId(targetDemoNo);
            if (psId != null) {
                ProfessionalSpecialist managedPs = managedPsCache.computeIfAbsent(
                    psId, id -> entityManager.find(ProfessionalSpecialist.class, id));
                a.setProfessionalSpecialist(managedPs);
            }
            entityManager.persist(a);
            entityManager.flush();
            archivePkMap.put(oldPk, (long) a.getId());
        }
        System.out.println("    [ConsultationRequestArchive] copied " + archivePkMap.size() + " rows  (source=" + sourceDemoNo + " -> target=" + targetDemoNo + ")  [" + (System.currentTimeMillis() - t0) + "ms]");

        if (archivePkMap.isEmpty()) {
            System.out.println("=== COPY CONSULTATION ARCHIVE GROUP DONE: no archive rows found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        // consultationRequestExtArchive — dual FK: consultationRequestArchiveId + requestId.
        // Per-archive-entry HQL bulk INSERT with CASE WHEN for the requestId remap.
        // Rows whose requestId has no mapping in requestPkMap are excluded by the IN clause
        // (same skip semantics as the previous per-row warn-and-continue).
        // O(archivePkMap.size) queries instead of O(archivePkMap × rows) persist+flush calls.
        if (!requestPkMap.isEmpty()) {
            // Build CASE WHEN for requestId remap — values are controlled Long integers, not user input.
            StringBuilder caseExpr = new StringBuilder("CASE e.requestId");
            List<Integer> oldReqIds = new ArrayList<>();
            for (Map.Entry<Long, Long> reqEntry : requestPkMap.entrySet()) {
                caseExpr.append(" WHEN ").append(reqEntry.getKey().intValue())
                        .append(" THEN ").append(reqEntry.getValue().intValue());
                oldReqIds.add(reqEntry.getKey().intValue());
            }
            caseExpr.append(" END");

            for (Map.Entry<Long, Long> archEntry : archivePkMap.entrySet()) {
                entityManager.createQuery(
                    "INSERT INTO ConsultationRequestExtArchive " +
                    "(consultationRequestArchiveId, originalId, requestId, key, value, dateCreated) " +
                    "SELECT :newArchId, e.originalId, " + caseExpr + ", e.key, e.value, e.dateCreated " +
                    "FROM ConsultationRequestExtArchive e " +
                    "WHERE e.consultationRequestArchiveId = :oldArchId AND e.requestId IN :oldReqIds")
                    .setParameter("newArchId", archEntry.getValue().intValue())
                    .setParameter("oldArchId", archEntry.getKey().intValue())
                    .setParameter("oldReqIds", oldReqIds)
                    .executeUpdate();
            }
        }

        logger.debug("copyConsultationArchiveGroup: source={}, target={}, archive rows={}", sourceDemoNo, targetDemoNo, archivePkMap.size());
        System.out.println("=== COPY CONSULTATION ARCHIVE GROUP DONE: " + archivePkMap.size() + " archive row(s) + ext archives copied  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }

    @Override
    public Map<Long, Long> copyCasemgmtNoteGroup(Integer sourceDemoNo, Integer targetDemoNo, Map<Long, Long> appointmentPkMap, Map<Integer, Map<Long, Long>> linkedEntityPkMaps) {
        System.out.println("\n=== COPY CASEMGMT NOTE GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        // casemgmt_note (parent) — demo field is "demographicNo"
        Map<Long, Long> notePkMap = copyEntityRows(CaseMgmtNote.class, "CaseMgmtNote", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setNoteId(null),
                (e, d) -> e.setDemographicNo(d));

        if (notePkMap.isEmpty()) {
            System.out.println("=== COPY CASEMGMT NOTE GROUP DONE: no notes found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return notePkMap;
        }

        // casemgmt_note_ext — per-parent HQL bulk INSERT: child PKs not needed downstream.
        for (Map.Entry<Long, Long> entry : notePkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO CaseMgmtNoteExt (noteId, keyVal, value, dateValue) " +
                "SELECT :newId, e.keyVal, e.value, e.dateValue " +
                "FROM CaseMgmtNoteExt e WHERE e.noteId = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        // casemgmt_note_link — per-parent HQL bulk INSERT: child PKs not needed downstream.
        // tableId values are copied as-is from source; remapNoteLinkTableIds corrects them below.
        for (Map.Entry<Long, Long> entry : notePkMap.entrySet()) {
            entityManager.createQuery(
                "INSERT INTO CaseMgmtNoteLink (noteId, tableName, tableId, otherId) " +
                "SELECT :newId, e.tableName, e.tableId, e.otherId " +
                "FROM CaseMgmtNoteLink e WHERE e.noteId = :oldId")
                .setParameter("newId", entry.getValue().intValue())
                .setParameter("oldId", entry.getKey().intValue())
                .executeUpdate();
        }

        // Build the list of new note PKs once — reused by every remap call below.
        // One UPDATE per entity entry (outer loop), matching all copied note PKs via IN clause — O(entities).
        List<Integer> newNoteIds = new ArrayList<>();
        for (Long v : notePkMap.values()) {
            newNoteIds.add(v.intValue());
        }

        // Remap CASEMGMTNOTE (1) self-links: a note that references another note on the same
        // source patient. Both notes are now on the target with new PKs; fix tableId using notePkMap.
        remapNoteLinkTableIds(newNoteIds, CaseManagementNoteLink.CASEMGMTNOTE, notePkMap);

        // Remap APPOINTMENT (11) links to point to the target patient's new appointment PKs.
        if (appointmentPkMap != null && !appointmentPkMap.isEmpty()) {
            remapNoteLinkTableIds(newNoteIds, CaseManagementNoteLink.APPOINTMENT, appointmentPkMap);
        }

        // Remap all other copied-entity link types supplied by the manager
        // (ALLERGIES=3, DRUGS=2, EFORMDATA=6, EMAIL=12, PREVENTIONS=8, TICKLER=10).
        if (linkedEntityPkMaps != null) {
            for (Map.Entry<Integer, Map<Long, Long>> entry : linkedEntityPkMaps.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    remapNoteLinkTableIds(newNoteIds, entry.getKey(), entry.getValue());
                }
            }
        }

        logger.debug("copyCasemgmtNoteGroup: source={}, target={}, note rows={}", sourceDemoNo, targetDemoNo, notePkMap.size());
        System.out.println("=== COPY CASEMGMT NOTE GROUP DONE: " + notePkMap.size() + " note(s) + ext/link rows copied and remapped  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return notePkMap;
    }

    @Override
    public Map<Long, Long> copyCasemgmtIssueGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        System.out.println("\n=== COPY CASEMGMT ISSUE GROUP: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " ===");
        long t0 = System.currentTimeMillis();
        List<CaseMgmtIssue> sourceIssues = entityManager.createQuery("SELECT e FROM CaseMgmtIssue e WHERE e.demographicNo = :demo", CaseMgmtIssue.class)
            .setParameter("demo", sourceDemoNo)
            .getResultList();

        if (sourceIssues.isEmpty()) {
            System.out.println("=== COPY CASEMGMT ISSUE GROUP DONE: no issues found for source=" + sourceDemoNo + "  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return Collections.emptyMap();
        }
        System.out.println("    Found " + sourceIssues.size() + " issue(s) for source=" + sourceDemoNo);

        // Dedup: collect issue_id values already on the target to avoid duplicates
        List<Integer> existingIssueIds = entityManager.createQuery("SELECT e.issueId FROM CaseMgmtIssue e WHERE e.demographicNo = :demo", Integer.class)
            .setParameter("demo", targetDemoNo)
            .getResultList();

        Map<Long, Long> issuePkMap = new HashMap<>();
        for (CaseMgmtIssue issue : sourceIssues) {
            if (existingIssueIds.contains(issue.getIssueId())) {
                // Duplicate: target already has this issue. Map the source PK → existing target PK
                // so copyIssueNotesGroup can still link any copied notes to the correct issue row.
                Integer existingPk = entityManager.createQuery("SELECT e.id FROM CaseMgmtIssue e WHERE e.demographicNo = :demo AND e.issueId = :iid", Integer.class)
                    .setParameter("demo", targetDemoNo)
                    .setParameter("iid", issue.getIssueId())
                    .getSingleResult();
                issuePkMap.put((long) issue.getId(), (long) existingPk);
                logger.debug("copyCasemgmtIssueGroup: duplicate issueId={} mapped source pk={} → existing target pk={}", issue.getIssueId(), issue.getId(), existingPk);
                continue;
            }
            long oldPk = (long) issue.getId();
            entityManager.detach(issue);
            issue.setId(null);
            issue.setDemographicNo(targetDemoNo);
            entityManager.persist(issue);
            entityManager.flush();
            issuePkMap.put(oldPk, (long) issue.getId());
        }

        logger.debug("copyCasemgmtIssueGroup: source={}, target={}, issue rows copied={}", sourceDemoNo, targetDemoNo, issuePkMap.size());
        System.out.println("=== COPY CASEMGMT ISSUE GROUP DONE: " + issuePkMap.size() + " issue mapping(s) resolved (new + deduped)  [" + (System.currentTimeMillis() - t0) + "ms] ===");
        return issuePkMap;
    }

    @Override
    public void copyIssueNotesGroup(Map<Long, Long> issuePkMap, Map<Long, Long> notePkMap) {
        System.out.println("\n=== COPY ISSUE-NOTES JUNCTION (casemgmt_issue_notes): " + issuePkMap.size() + " issue(s), " + notePkMap.size() + " note(s) ===");
        long t0 = System.currentTimeMillis();
        if (issuePkMap.isEmpty() || notePkMap.isEmpty()) {
            System.out.println("=== COPY ISSUE-NOTES DONE: empty maps, nothing to copy  [" + (System.currentTimeMillis() - t0) + "ms] ===");
            return;
        }

        for (Long oldIssueId : issuePkMap.keySet()) {
            List<CaseMgmtIssueNotes> junctionRows = entityManager.createQuery("SELECT e FROM CaseMgmtIssueNotes e WHERE e.id = :issueId", CaseMgmtIssueNotes.class)
                .setParameter("issueId", oldIssueId.intValue())
                .getResultList();

            Long newIssueId = issuePkMap.get(oldIssueId);

            for (CaseMgmtIssueNotes row : junctionRows) {
                Long newNoteId = notePkMap.get((long) row.getNoteId());
                if (newNoteId == null) {
                    logger.warn("copyIssueNotesGroup: no note mapping for old noteId={}; skipping row", row.getNoteId());
                    continue;
                }
                entityManager.detach(row);
                row.setId(newIssueId.intValue());
                row.setNoteId(newNoteId.intValue());
                entityManager.persist(row);
                entityManager.flush();
            }
        }

        logger.debug("copyIssueNotesGroup: issue entries={}", issuePkMap.size());
        System.out.println("=== COPY ISSUE-NOTES DONE  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }

    // -------------------------------------------------------------------------
    // ── Identity tables
    // -------------------------------------------------------------------------

    @Override
    public void copyIdentityTables(Integer sourceDemoNo, Integer targetDemoNo, boolean isSecondary) {
        System.out.println("\n=== COPY IDENTITY TABLES: source=" + sourceDemoNo + " -> target=" + targetDemoNo + " (isSecondary=" + isSecondary + ") ===");
        long t0 = System.currentTimeMillis();
        // demographicExt
        if (!isSecondary) {
            // Primary pass: copy all rows
            copyEntityRows(DemographicExt.class, "DemographicExt", "demographicNo",
                    sourceDemoNo, targetDemoNo,
                    e -> (long) e.getId(), e -> e.setId(null),
                    (e, d) -> e.setDemographicNo(d));
        } else {
            // Secondary pass: skip rows whose key_val already exists for the target
            List<String> existingKeys = entityManager.createQuery("SELECT e.key FROM DemographicExt e WHERE e.demographicNo = :demo", String.class)
                .setParameter("demo", targetDemoNo)
                .getResultList();

            List<DemographicExt> sourceExts = entityManager.createQuery("SELECT e FROM DemographicExt e WHERE e.demographicNo = :demo", DemographicExt.class)
                .setParameter("demo", sourceDemoNo)
                .getResultList();

            for (DemographicExt ext : sourceExts) {
                if (existingKeys.contains(ext.getKey())) {
                    logger.debug("copyIdentityTables: skipping DemographicExt key='{}' already on target={}", ext.getKey(), targetDemoNo);
                    continue;
                }
                entityManager.detach(ext);
                ext.setId(null);
                ext.setDemographicNo(targetDemoNo);
                entityManager.persist(ext);
                entityManager.flush();
            }
        }

        // demographiccust — one row per patient; PK IS the demographicNo
        List<DemographicCust> sourceCust = entityManager.createQuery("SELECT e FROM DemographicCust e WHERE e.id = :demo", DemographicCust.class)
            .setParameter("demo", sourceDemoNo)
            .getResultList();

        if (!sourceCust.isEmpty()) {
            List<DemographicCust> targetCustList = entityManager.createQuery("SELECT e FROM DemographicCust e WHERE e.id = :demo", DemographicCust.class)
                .setParameter("demo", targetDemoNo)
                .getResultList();

            if (targetCustList.isEmpty()) {
                // Primary pass (or secondary where target has no cust row): insert a fresh copy
                DemographicCust cust = sourceCust.get(0);
                entityManager.detach(cust);
                cust.setId(targetDemoNo);
                entityManager.persist(cust);
                entityManager.flush();
            } else if (isSecondary) {
                // Secondary pass: field-level merge — fill NULL/empty fields from this secondary
                DemographicCust src = sourceCust.get(0);
                DemographicCust target = targetCustList.get(0);
                boolean dirty = false;
                if ((target.getNurse() == null || target.getNurse().isEmpty()) && src.getNurse() != null && !src.getNurse().isEmpty()) {
                    target.setNurse(src.getNurse()); dirty = true;
                }
                if ((target.getResident() == null || target.getResident().isEmpty()) && src.getResident() != null && !src.getResident().isEmpty()) {
                    target.setResident(src.getResident()); dirty = true;
                }
                if ((target.getAlert() == null || target.getAlert().isEmpty()) && src.getAlert() != null && !src.getAlert().isEmpty()) {
                    target.setAlert(src.getAlert()); dirty = true;
                }
                if ((target.getMidwife() == null || target.getMidwife().isEmpty()) && src.getMidwife() != null && !src.getMidwife().isEmpty()) {
                    target.setMidwife(src.getMidwife()); dirty = true;
                }
                if ((target.getNotes() == null || target.getNotes().isEmpty()) && src.getNotes() != null && !src.getNotes().isEmpty()) {
                    target.setNotes(src.getNotes()); dirty = true;
                }
                if (dirty) {
                    entityManager.flush();
                    logger.debug("copyIdentityTables: DemographicCust field-level merge applied from source={} to target={}", sourceDemoNo, targetDemoNo);
                }
            } else {
                logger.debug("copyIdentityTables: target={} already has DemographicCust row, skipping primary insert", targetDemoNo);
            }
        }

        // other_id — FK is tableId (String) representing demographicNo
        if (!isSecondary) {
            // Primary pass: copy all rows
            List<OtherId> otherIds = entityManager.createQuery("SELECT e FROM OtherId e WHERE e.tableId = :tid", OtherId.class)
                .setParameter("tid", String.valueOf(sourceDemoNo))
                .getResultList();
            for (OtherId oid : otherIds) {
                entityManager.detach(oid);
                oid.setId(null);
                oid.setTableId(String.valueOf(targetDemoNo));
                entityManager.persist(oid);
                entityManager.flush();
            }
        } else {
            // Secondary pass: skip rows whose otherKey already exists for the target
            List<String> existingOtherKeys = entityManager.createQuery("SELECT e.otherKey FROM OtherId e WHERE e.tableId = :tid", String.class)
                .setParameter("tid", String.valueOf(targetDemoNo))
                .getResultList();

            List<OtherId> sourceOtherIds = entityManager.createQuery("SELECT e FROM OtherId e WHERE e.tableId = :tid", OtherId.class)
                .setParameter("tid", String.valueOf(sourceDemoNo))
                .getResultList();

            for (OtherId oid : sourceOtherIds) {
                if (existingOtherKeys.contains(oid.getOtherKey())) {
                    logger.debug("copyIdentityTables: skipping OtherId otherKey='{}' already on target={}", oid.getOtherKey(), targetDemoNo);
                    continue;
                }
                entityManager.detach(oid);
                oid.setId(null);
                oid.setTableId(String.valueOf(targetDemoNo));
                entityManager.persist(oid);
                entityManager.flush();
            }
        }

        // demographicExtArchive — HQL bulk INSERT: leaf table, PK never captured, no child FK.
        // Expected: ~825 rows across both passes → ~1.6 sec → < 50 ms.
        long tDea = System.currentTimeMillis();
        int deaCount = entityManager.createQuery(
            "INSERT INTO DemographicExtArchive (archiveId, demographicNo, providerNo, key, value, dateCreated, hidden) " +
            "SELECT e.archiveId, :targetDemo, e.providerNo, e.key, e.value, e.dateCreated, e.hidden " +
            "FROM DemographicExtArchive e WHERE e.demographicNo = :sourceDemo")
            .setParameter("targetDemo", targetDemoNo)
            .setParameter("sourceDemo", sourceDemoNo)
            .executeUpdate();
        System.out.println("    [DemographicExtArchive] copied " + deaCount + " rows  (source=" + sourceDemoNo + " -> target=" + targetDemoNo + ")  [HQL bulk]  [" + (System.currentTimeMillis() - tDea) + "ms]");
        logger.debug("copyIdentityTables: DemographicExtArchive rows={} [HQL bulk]", deaCount);

        // demographiccustArchive — copy all rows regardless of pass
        copyEntityRows(DemographicCustArchive.class, "DemographicCustArchive", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        logger.debug("copyIdentityTables: source={}, target={}, isSecondary={} — complete", sourceDemoNo, targetDemoNo, isSecondary);
        System.out.println("=== COPY IDENTITY TABLES DONE (source=" + sourceDemoNo + " -> target=" + targetDemoNo + ", isSecondary=" + isSecondary + ")  [" + (System.currentTimeMillis() - t0) + "ms] ===");
    }
}
