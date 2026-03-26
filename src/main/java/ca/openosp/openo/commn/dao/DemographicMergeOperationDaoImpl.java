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
import ca.openosp.openo.commn.model.AppointmentArchive;
import ca.openosp.openo.commn.model.Consent;
import ca.openosp.openo.commn.model.CtlDocument;
import ca.openosp.openo.commn.model.CtlDocumentPK;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicArchive;
import ca.openosp.openo.commn.model.DemographicContact;
import ca.openosp.openo.commn.model.DemographicCust;
import ca.openosp.openo.commn.model.DemographicCustArchive;
import ca.openosp.openo.commn.model.DemographicExt;
import ca.openosp.openo.commn.model.DemographicExtArchive;
import ca.openosp.openo.commn.model.DemographicPharmacy;
import ca.openosp.openo.commn.model.DigitalSignature;
import ca.openosp.openo.commn.model.Dxresearch;
import ca.openosp.openo.commn.model.Episode;
import ca.openosp.openo.commn.model.FaxJob;
import ca.openosp.openo.commn.model.FlowSheetDrug;
import ca.openosp.openo.commn.model.FlowSheetDx;
import ca.openosp.openo.commn.model.Immunizations;
import ca.openosp.openo.commn.model.MeasurementsDeleted;
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
import ca.openosp.openo.billing.CA.ON.model.BillingONCHeader2;
import ca.openosp.openo.commn.model.BillingONCHeader1;
import ca.openosp.openo.commn.model.BillingONItem;
import ca.openosp.openo.commn.model.BillingOnTransaction;

// --- Consultation group ---
import ca.openosp.openo.commn.model.ConsultationRequest;
import ca.openosp.openo.commn.model.ConsultationRequestArchive;
import ca.openosp.openo.commn.model.ConsultationRequestExt;
import ca.openosp.openo.commn.model.ConsultationRequestExtArchive;

// --- Drug group ---
import ca.openosp.openo.commn.model.Drug;
import ca.openosp.openo.commn.model.DrugReason;

// --- EForm group ---
import ca.openosp.openo.commn.model.EFormData;
import ca.openosp.openo.commn.model.EFormValue;

// --- Email group ---
import ca.openosp.openo.commn.model.EmailAttachment;
import ca.openosp.openo.commn.model.EmailLog;

// --- ERefer group ---
import ca.openosp.openo.commn.model.EReferAttachment;
import ca.openosp.openo.commn.model.EReferAttachmentData;

// --- BCAR 2020 group ---
import ca.openosp.openo.form.model.FormBCAR2020;

// --- HRM ---
import ca.openosp.openo.hospitalReportManager.model.HRMDocumentToDemographic;

// --- Measurements group ---
import ca.openosp.openo.commn.model.Measurement;
import ca.openosp.openo.commn.model.MeasurementsExt;

// --- Prevention group ---
import ca.openosp.openo.commn.model.Prevention;
import ca.openosp.openo.commn.model.PreventionExt;

// --- Tickler group ---
import ca.openosp.openo.commn.model.Tickler;
import ca.openosp.openo.commn.model.TicklerLink;

// --- CaseMgmt special entities ---
import ca.openosp.openo.commn.model.CaseMgmtCpp;
import ca.openosp.openo.commn.model.CaseMgmtIssue;
import ca.openosp.openo.commn.model.CaseMgmtIssueNotes;
import ca.openosp.openo.commn.model.CaseMgmtNote;
import ca.openosp.openo.commn.model.CaseMgmtNoteExt;
import ca.openosp.openo.commn.model.CaseMgmtNoteLink;

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
        for (T row : rows) {
            long oldPk = getPk.apply(row);
            entityManager.detach(row);
            clearPk.accept(row);
            setDemoNo.accept(row, targetDemoNo);
            entityManager.persist(row);
            entityManager.flush();
            pkMap.put(oldPk, getPk.apply(row));
        }

        logger.debug("copyEntityRows: entity='{}', source={}, target={}, rows={}", jpqlName, sourceDemoNo, targetDemoNo, pkMap.size());
        return pkMap;
    }

    /**
     * Generic Hibernate copy for child/derived tables (FK mapped to a parent PK map).
     * <p>
     * For each old parent PK in {@code parentPkMap}, loads all child rows by FK value,
     * detaches, optionally clears the child PK, sets the FK to the new parent PK,
     * optionally sets an extra demographic field, re-persists, and flushes.
     *
     * @param <T>            the entity type
     * @param entityClass    Class&lt;T&gt; entity class token
     * @param jpqlName       String JPQL entity name
     * @param fkField        String the JPQL field name for the FK to the parent
     * @param parentPkMap    Map&lt;Long, Long&gt; mapping old parent PK → new parent PK
     * @param clearPk        Consumer that nulls the child PK; pass {@code null} if PK is not auto-increment
     * @param setFk          BiConsumer to set the FK to the new parent PK (Integer)
     * @param extraDemoField BiConsumer to set an extra demographic column; pass {@code null} if unused
     * @param targetDemoNo   Integer target demographic number for the extra demo field; ignored if null
     */
    private <T> void copyChildRows(Class<T> entityClass, String jpqlName, String fkField, Map<Long, Long> parentPkMap, Consumer<T> clearPk, BiConsumer<T, Integer> setFk, BiConsumer<T, Integer> extraDemoField, Integer targetDemoNo) {

        if (parentPkMap == null || parentPkMap.isEmpty()) return;

        for (Map.Entry<Long, Long> entry : parentPkMap.entrySet()) {
            List<T> children = entityManager.createQuery("SELECT e FROM " + jpqlName + " e WHERE e." + fkField + " = :fk", entityClass)
                .setParameter("fk", entry.getKey().intValue())
                .getResultList();

            for (T child : children) {
                entityManager.detach(child);
                if (clearPk != null) clearPk.accept(child);
                setFk.accept(child, entry.getValue().intValue());
                if (extraDemoField != null) extraDemoField.accept(child, targetDemoNo);
                entityManager.persist(child);
                entityManager.flush();
            }
        }

        logger.debug("copyChildRows: entity='{}', parent entries={}", jpqlName, parentPkMap.size());
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
        // appointment
        Map<Long, Long> apptPkMap = copyEntityRows(Appointment.class, "Appointment", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // appointmentArchive — archive PKs are not linked from casemgmt_note_link but copy here
        // for completeness; return value is ignored
        copyEntityRows(AppointmentArchive.class, "AppointmentArchive", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        logger.debug("copyAppointments: source={}, target={}, appt rows={}", sourceDemoNo, targetDemoNo, apptPkMap.size());
        return apptPkMap;
    }

    @Override
    public Map<Long, Long> copyAllergies(Integer sourceDemoNo, Integer targetDemoNo) {
        Map<Long, Long> allergyPkMap = copyEntityRows(Allergy.class, "Allergy", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));
        logger.debug("copyAllergies: source={}, target={}, allergy rows={}", sourceDemoNo, targetDemoNo, allergyPkMap.size());
        return allergyPkMap;
    }

    @Override
    public void copyClinicalDirectRecords(Integer sourceDemoNo, Integer targetDemoNo) {
        // allergies — copied by copyAllergies(); the manager calls that separately so the
        // PK map is available for casemgmt_note_link tableId remap (table_name = 3)

        // appointment and appointmentArchive are handled by copyAppointments() so the PK map
        // can be passed to copyCasemgmtNoteGroup for note-link remap

        // casemgmt_cpp — demo field is String (varchar in DB); handled inline
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
        logger.debug("copyClinicalDirectRecords: CaseMgmtCpp rows={}", cppRows.size());

        // Consent
        copyEntityRows(Consent.class, "Consent", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // ctl_document — composite @EmbeddedId (module + documentNo); module_id = demographicNo.
        // New-object pattern: construct a fresh transient CtlDocument so Hibernate issues a direct INSERT.
        List<CtlDocument> sourceDocs = entityManager.createQuery("SELECT d FROM CtlDocument d WHERE d.id.module = 'demographic' AND d.id.moduleId = :mid", CtlDocument.class)
            .setParameter("mid", sourceDemoNo)
            .getResultList();
        for (CtlDocument src : sourceDocs) {
            CtlDocument copy = new CtlDocument();
            copy.setId(new CtlDocumentPK("demographic", targetDemoNo, src.getId().getDocumentNo()));
            copy.setStatus(src.getStatus());
            entityManager.persist(copy);
        }
        if (!sourceDocs.isEmpty()) {
            entityManager.flush();
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

        // measurementsDeleted
        copyEntityRows(MeasurementsDeleted.class, "MeasurementsDeleted", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

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
    }

    // -------------------------------------------------------------------------
    // ── Form tables
    // -------------------------------------------------------------------------

    @Override
    public void copyAllForms(Integer sourceDemoNo, Integer targetDemoNo) {
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
        Map<Long, Long> pkMap = copyFormJdbcWithMap(tableName, pkCol, sourceDemoNo, targetDemoNo);
        if (!pkMap.isEmpty() && copyBooleanValues) {
            copyFormBooleanValues(pkMap, tableName);
        }
        if (!pkMap.isEmpty()) {
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
        String sql = "INSERT INTO form_boolean_value (form_name, form_id, field_name, field_value) " +
                     "SELECT form_name, ?, field_name, field_value " +
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
        if (!tableExists("billing_on_cheader1")) return;

        // billing_on_cheader1 (parent)
        Map<Long, Long> ch1PkMap = copyEntityRows(BillingONCHeader1.class, "BillingONCHeader1", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        if (ch1PkMap.isEmpty()) return;

        // billing_on_cheader2 — child of cheader1, FK is ch1Id
        copyChildRows(BillingONCHeader2.class, "BillingONCHeader2", "ch1Id",
                ch1PkMap,
                e -> e.setId(null),
                (e, fk) -> e.setCh1Id(fk),
                null, null);

        // billing_on_item — child of cheader1, FK is ch1Id
        copyChildRows(BillingONItem.class, "BillingONItem", "ch1Id",
                ch1PkMap,
                e -> e.setId(null),
                (e, fk) -> e.setCh1Id(fk),
                null, null);

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
    }

    @Override
    public Map<Long, Long> copyConsultationsGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        // consultationRequests (parent) — demo field is "demographicId"
        Map<Long, Long> requestPkMap = copyEntityRows(ConsultationRequest.class, "ConsultationRequest", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (requestPkMap.isEmpty()) return requestPkMap;

        // consultationRequestExt — child, FK is requestId
        copyChildRows(ConsultationRequestExt.class, "ConsultationRequestExt", "requestId",
                requestPkMap,
                e -> e.setId(null),
                (e, fk) -> e.setRequestId(fk),
                null, null);

        logger.debug("copyConsultationsGroup: source={}, target={}, request rows={}", sourceDemoNo, targetDemoNo, requestPkMap.size());
        return requestPkMap;
    }

    @Override
    public Map<Long, Long> copyDrugsGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        // drugs (parent) — demo field is "demographicId"
        Map<Long, Long> drugPkMap = copyEntityRows(Drug.class, "Drug", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (drugPkMap.isEmpty()) return drugPkMap;

        // drugReason — child, FK is drugId; also has a extra demographicNo column
        copyChildRows(DrugReason.class, "DrugReason", "drugId",
                drugPkMap,
                e -> e.setId(null),
                (e, fk) -> e.setDrugId(fk),
                (e, d) -> e.setDemographicNo(d),
                targetDemoNo);

        logger.debug("copyDrugsGroup: source={}, target={}, drug rows={}", sourceDemoNo, targetDemoNo, drugPkMap.size());
        return drugPkMap;
    }

    @Override
    public Map<Long, Long> copyEformGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        // eform_data (parent) — demo field is "demographicId"
        Map<Long, Long> eformPkMap = copyEntityRows(EFormData.class, "EFormData", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (eformPkMap.isEmpty()) return eformPkMap;

        // eform_values — child, FK is formDataId; also has extra demographicId column
        copyChildRows(EFormValue.class, "EFormValue", "formDataId",
                eformPkMap,
                e -> e.setId(null),
                (e, fk) -> e.setFormDataId(fk),
                (e, d) -> e.setDemographicId(d),
                targetDemoNo);

        logger.debug("copyEformGroup: source={}, target={}, eform rows={}", sourceDemoNo, targetDemoNo, eformPkMap.size());
        return eformPkMap;
    }

    @Override
    public Map<Long, Long> copyEmailGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        // emailLog — @ManyToOne Demographic; query by demographic.id
        List<EmailLog> logs = entityManager.createQuery("SELECT e FROM EmailLog e WHERE e.demographic.id = :demo", EmailLog.class)
            .setParameter("demo", sourceDemoNo)
            .getResultList();

        if (logs.isEmpty()) return Collections.emptyMap();

        Map<Long, Long> emailPkMap = new HashMap<>();
        Demographic targetRef = entityManager.getReference(Demographic.class, targetDemoNo);

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

            // Copy attachments for this log
            List<EmailAttachment> attachments = entityManager.createQuery("SELECT a FROM EmailAttachment a WHERE a.emailLog.id = :lid", EmailAttachment.class)
                .setParameter("lid", oldLogId)
                .getResultList();

            for (EmailAttachment att : attachments) {
                entityManager.detach(att);
                att.setId(null);
                att.setEmailLog(log); // log now carries the new id
                entityManager.persist(att);
                entityManager.flush();
            }
        }

        logger.debug("copyEmailGroup: source={}, target={}, log rows={}", sourceDemoNo, targetDemoNo, logs.size());
        return emailPkMap;
    }

    @Override
    public void copyEreferGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        // erefer_attachment (parent)
        Map<Long, Long> attachPkMap = copyEntityRows(EReferAttachment.class, "EReferAttachment", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        if (attachPkMap.isEmpty()) return;

        // erefer_attachment_data — composite PK (erefer_attachment_id, lab_id, lab_type); no auto-increment.
        // Use the new-object Hibernate pattern: construct a brand-new transient instance per row so
        // Hibernate classifies it as TRANSIENT immediately (no DB snapshot SELECT) and issues a direct INSERT.
        for (Map.Entry<Long, Long> entry : attachPkMap.entrySet()) {
            List<EReferAttachmentData> sourceRows = entityManager.createQuery("SELECT e FROM EReferAttachmentData e WHERE e.eReferAttachment.id = :pid", EReferAttachmentData.class)
                .setParameter("pid", entry.getKey().intValue())
                .getResultList();

            EReferAttachment newParentRef = entityManager.getReference(EReferAttachment.class, entry.getValue().intValue());

            for (EReferAttachmentData src : sourceRows) {
                // Brand-new object — never been in any session, so always TRANSIENT; persist() → direct INSERT
                EReferAttachmentData copy = new EReferAttachmentData(newParentRef, src.getLabId(), src.getLabType());
                entityManager.persist(copy);
                entityManager.flush();
            }
        }

        logger.debug("copyEreferGroup: source={}, target={}, attachment rows={}", sourceDemoNo, targetDemoNo, attachPkMap.size());
    }

    @Override
    public void copyFormBCAR2020Group(Integer sourceDemoNo, Integer targetDemoNo) {
        if (!tableExists("formBCAR2020")) return;

        // formBCAR2020 (parent) — Hibernate copy; PK java field is "formId"
        Map<Long, Long> formPkMap = copyEntityRows(FormBCAR2020.class, "FormBCAR2020", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        if (formPkMap.isEmpty()) return;

        // formBCAR2020Text — composite IdClass (formId + pageNo + field); JDBC INSERT...SELECT
        for (Map.Entry<Long, Long> entry : formPkMap.entrySet()) {
            jdbcTemplate.update(
                "INSERT INTO formBCAR2020Text (form_id, page_no, field, value, last_modified) " +
                "SELECT ?, page_no, field, value, last_modified " +
                "FROM formBCAR2020Text WHERE form_id = ?",
                entry.getValue().intValue(), entry.getKey().intValue());
        }

        logger.debug("copyFormBCAR2020Group: source={}, target={}, form rows={}", sourceDemoNo, targetDemoNo, formPkMap.size());
    }

    @Override
    public void copyFormONAREnhancedGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        // All three tables have 400–1400 columns — no entity classes; reuse copyFormJdbc for parent.
        // Ext1/Ext2 are child tables keyed by the parent id (not demographic_no), so they need
        // a separate inline copy using the parent PK map.
        if (!tableExists("formONAREnhancedRecord")) return;

        // Copy main table via shared helper; capture old→new PK map for child remapping
        Map<Long, Long> onarPkMap = copyFormJdbcWithMap("formONAREnhancedRecord", "ID", sourceDemoNo, targetDemoNo);
        if (onarPkMap.isEmpty()) return;

        // formONAREnhancedRecordExt1 and Ext2 — child rows keyed by parent id
        for (String extTable : new String[]{"formONAREnhancedRecordExt1", "formONAREnhancedRecordExt2"}) {
            if (!tableExists(extTable)) continue;
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
                }
            }
        }

        logger.debug("copyFormONAREnhancedGroup: source={}, target={}, record rows={}", sourceDemoNo, targetDemoNo, onarPkMap.size());
    }

    @Override
    public void copyMeasurementsGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        // measurements (parent) — demo field is "demographicId"
        Map<Long, Long> measurePkMap = copyEntityRows(Measurement.class, "Measurement", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (measurePkMap.isEmpty()) return;

        // measurementsExt — child, FK is measurementId
        copyChildRows(MeasurementsExt.class, "MeasurementsExt", "measurementId",
                measurePkMap,
                e -> e.setId(null),
                (e, fk) -> e.setMeasurementId(fk),
                null, null);

        logger.debug("copyMeasurementsGroup: source={}, target={}, measure rows={}", sourceDemoNo, targetDemoNo, measurePkMap.size());
    }

    @Override
    public Map<Long, Long> copyPreventionsGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        // preventions (parent) — demo field is "demographicId"
        Map<Long, Long> prevPkMap = copyEntityRows(Prevention.class, "Prevention", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (prevPkMap.isEmpty()) return prevPkMap;

        // preventionsExt — child, FK is preventionId
        copyChildRows(PreventionExt.class, "PreventionExt", "preventionId",
                prevPkMap,
                e -> e.setId(null),
                (e, fk) -> e.setPreventionId(fk),
                null, null);

        logger.debug("copyPreventionsGroup: source={}, target={}, prevention rows={}", sourceDemoNo, targetDemoNo, prevPkMap.size());
        return prevPkMap;
    }

    @Override
    public Map<Long, Long> copyTicklerGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        // tickler (parent)
        Map<Long, Long> ticklerPkMap = copyEntityRows(Tickler.class, "Tickler", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        if (ticklerPkMap.isEmpty()) return ticklerPkMap;

        // tickler_link — child, FK is ticklerNo
        copyChildRows(TicklerLink.class, "TicklerLink", "ticklerNo",
                ticklerPkMap,
                e -> e.setId(null),
                (e, fk) -> e.setTicklerNo(fk),
                null, null);

        logger.debug("copyTicklerGroup: source={}, target={}, tickler rows={}", sourceDemoNo, targetDemoNo, ticklerPkMap.size());
        return ticklerPkMap;
    }

    // -------------------------------------------------------------------------
    // ── Special-case group methods
    // -------------------------------------------------------------------------

    @Override
    public void copyConsultationArchiveGroup(Integer sourceDemoNo, Integer targetDemoNo, Map<Long, Long> requestPkMap) {
        // consultationRequestsArchive (parent) — demo field is "demographicId"
        Map<Long, Long> archivePkMap = copyEntityRows(ConsultationRequestArchive.class, "ConsultationRequestArchive", "demographicId",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicId(d));

        if (archivePkMap.isEmpty()) return;

        // consultationRequestExtArchive — dual FK: consultationRequestArchiveId + requestId
        // Query by consultationRequestArchiveId (the FK to the archive parent), not by id (the PK)
        for (Map.Entry<Long, Long> archEntry : archivePkMap.entrySet()) {
            List<ConsultationRequestExtArchive> extArchives = entityManager.createQuery("SELECT e FROM ConsultationRequestExtArchive e WHERE e.consultationRequestArchiveId = :aid", ConsultationRequestExtArchive.class)
                .setParameter("aid", archEntry.getKey().intValue())
                .getResultList();

            for (ConsultationRequestExtArchive ext : extArchives) {
                long oldRequestId = ext.getRequestId();
                Long newRequestId = requestPkMap.get(oldRequestId);
                if (newRequestId == null) {
                    // No mapping means the live request was never copied (e.g. it belonged to a
                    // different demographic). Persisting the stale source requestId would create a
                    // cross-patient FK. Skip and warn instead.
                    logger.warn("copyConsultationArchiveGroup: no requestId mapping for oldRequestId={}; skipping ext archive row for archiveId={}", oldRequestId, archEntry.getKey());
                    continue;
                }
                entityManager.detach(ext);
                ext.setId(null);
                // Remap archive FK to the new archive row
                ext.setConsultationRequestArchiveId(archEntry.getValue().intValue());
                ext.setRequestId(newRequestId.intValue());
                entityManager.persist(ext);
                entityManager.flush();
            }
        }

        logger.debug("copyConsultationArchiveGroup: source={}, target={}, archive rows={}", sourceDemoNo, targetDemoNo, archivePkMap.size());
    }

    @Override
    public Map<Long, Long> copyCasemgmtNoteGroup(Integer sourceDemoNo, Integer targetDemoNo, Map<Long, Long> appointmentPkMap, Map<Integer, Map<Long, Long>> linkedEntityPkMaps) {
        // casemgmt_note (parent) — demo field is "demographicNo"
        Map<Long, Long> notePkMap = copyEntityRows(CaseMgmtNote.class, "CaseMgmtNote", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setNoteId(null),
                (e, d) -> e.setDemographicNo(d));

        if (notePkMap.isEmpty()) return notePkMap;

        // casemgmt_note_ext — child, FK is noteId
        copyChildRows(CaseMgmtNoteExt.class, "CaseMgmtNoteExt", "noteId",
                notePkMap,
                e -> e.setId(null),
                (e, fk) -> e.setNoteId(fk),
                null, null);

        // casemgmt_note_link — child, FK is noteId; tableId remapped below for all copied entity types
        copyChildRows(CaseMgmtNoteLink.class, "CaseMgmtNoteLink", "noteId",
                notePkMap,
                e -> e.setId(null),
                (e, fk) -> e.setNoteId(fk),
                null, null);

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
        return notePkMap;
    }

    @Override
    public Map<Long, Long> copyCasemgmtIssueGroup(Integer sourceDemoNo, Integer targetDemoNo) {
        List<CaseMgmtIssue> sourceIssues = entityManager.createQuery("SELECT e FROM CaseMgmtIssue e WHERE e.demographicNo = :demo", CaseMgmtIssue.class)
            .setParameter("demo", sourceDemoNo)
            .getResultList();

        if (sourceIssues.isEmpty()) {
            return Collections.emptyMap();
        }

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
        return issuePkMap;
    }

    @Override
    public void copyIssueNotesGroup(Map<Long, Long> issuePkMap, Map<Long, Long> notePkMap) {
        if (issuePkMap.isEmpty() || notePkMap.isEmpty()) return;

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
    }

    // -------------------------------------------------------------------------
    // ── Identity tables
    // -------------------------------------------------------------------------

    @Override
    public void copyIdentityTables(Integer sourceDemoNo, Integer targetDemoNo, boolean isSecondary) {
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

        // demographicExtArchive — copy all rows regardless of pass
        copyEntityRows(DemographicExtArchive.class, "DemographicExtArchive", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        // demographiccustArchive — copy all rows regardless of pass
        copyEntityRows(DemographicCustArchive.class, "DemographicCustArchive", "demographicNo",
                sourceDemoNo, targetDemoNo,
                e -> (long) e.getId(), e -> e.setId(null),
                (e, d) -> e.setDemographicNo(d));

        logger.debug("copyIdentityTables: source={}, target={}, isSecondary={} — complete", sourceDemoNo, targetDemoNo, isSecondary);
    }
}
