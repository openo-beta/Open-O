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

import java.util.Map;

/**
 * Hibernate-based copy engine for the demographic merge operation.
 * <p>
 * All methods use the load → detach → null-PK → set-demo → persist pattern so that
 * the database assigns new auto-generated PKs via {@code GenerationType.IDENTITY}.
 * This eliminates the MAX(pk) race condition present in JDBC INSERT...SELECT approaches.
 * <p>
 * Methods are grouped by clinical domain. Optional-module methods ({@code [BC]}, {@code [ON]})
 * perform a runtime {@code tableExists()} check and skip silently when the table is absent.
 * Source rows are never modified.
 *
 * @since 2026-03-19
 */
public interface DemographicMergeOperationDao {

    // -------------------------------------------------------------------------
    // Identity / demographic extension tables
    // -------------------------------------------------------------------------

    /**
     * Copies identity and demographic extension tables
     * ({@code demographicExt}, {@code demographiccust}, {@code other_id},
     * {@code demographicExtArchive}, {@code demographiccustArchive}).
     * <p>
     * When {@code isSecondary} is {@code false} (primary A pass), all rows are copied.
     * When {@code isSecondary} is {@code true} (secondary pass), only gap-fill logic applies.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @param isSecondary         boolean false for the primary (A) pass; true for each secondary pass
     */
    void copyIdentityTables(Integer sourceDemographicNo, Integer targetDemographicNo, boolean isSecondary);

    // -------------------------------------------------------------------------
    // Clinical direct-copy tables
    // -------------------------------------------------------------------------

    /**
     * Copies {@code appointment} and {@code appointmentArchive} rows and returns the
     * old-to-new appointment PK map. This is extracted from {@link #copyClinicalDirectRecords}
     * so the manager can pass the map to {@link #copyCasemgmtNoteGroup} for remapping
     * {@code casemgmt_note_link} rows that reference appointment PKs ({@code table_name = 11}).
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old {@code appointment.appointment_no} to new value
     */
    Map<Long, Long> copyAppointments(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies {@code allergies} rows for the source patient to the target patient.
     * Extracted from {@link #copyClinicalDirectRecords} so the returned PK map can be
     * threaded into {@link #copyCasemgmtNoteGroup} for {@code casemgmt_note_link}
     * {@code table_name = 3} (ALLERGIES) {@code tableId} remap.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old {@code allergies.id} to new {@code allergies.id}
     */
    Map<Long, Long> copyAllergies(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies all remaining clinical tables that map directly to a single {@code demographicNo}
     * column with no derived child tables. {@code appointment}, {@code appointmentArchive}, and
     * {@code allergies} are excluded — call {@link #copyAppointments} and {@link #copyAllergies}
     * first so their PK maps are available for {@link #copyCasemgmtNoteGroup}:
     * {@code casemgmt_cpp}, {@code Consent}, {@code ctl_document} (JDBC),
     * {@code demographicArchive}, {@code DemographicContact}, {@code demographicPharmacy},
     * {@code DigitalSignature}, {@code dxresearch}, {@code Episode}, {@code faxes},
     * {@code flowsheet_drug}, {@code flowsheet_dx}, {@code HRMDocumentToDemographic},
     * {@code immunizations}, {@code measurementsDeleted}, {@code msgDemoMap},
     * {@code msgIntegratorDemoMap}, {@code OLISQueryLog} {@code [ON]}, {@code OLISResults} {@code [ON]},
     * {@code patientLabRouting}, {@code prescription}, {@code relationships},
     * {@code table_modification}, {@code waitingList}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     */
    void copyClinicalDirectRecords(Integer sourceDemographicNo, Integer targetDemographicNo);

    // -------------------------------------------------------------------------
    // Form tables
    // -------------------------------------------------------------------------

    /**
     * Copies all form tables for the patient. Each form row is copied via Hibernate;
     * corresponding {@code form_boolean_value} rows are copied inline via JDBC.
     * Optional forms ({@code [BC]}, {@code [ON]}) are guarded by a runtime table-exists check.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     */
    void copyAllForms(Integer sourceDemographicNo, Integer targetDemographicNo);

    // -------------------------------------------------------------------------
    // Part 2b — parent + derived group tables
    // -------------------------------------------------------------------------

    /**
     * Copies Ontario billing tables {@code [ON]}:
     * {@code billing_on_cheader1} (parent) → {@code billing_on_cheader2},
     * {@code billing_on_item}, {@code billing_on_transaction} (children).
     * A JDBC UPDATE remaps {@code ch1_id} in {@code billing_on_transaction} after copy.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     */
    void copyBillingGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies consultation tables:
     * {@code consultationRequests} (parent) → {@code consultationRequestExt} (child).
     * Returns the old-to-new request PK map needed by {@link #copyConsultationArchiveGroup}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old {@code requestId} to new {@code requestId}
     */
    Map<Long, Long> copyConsultationsGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies drug tables:
     * {@code drugs} (parent) → {@code drugReason} (child with extra {@code demographicNo} column).
     * Returns the old-to-new drug PK map needed for {@code casemgmt_note_link}
     * {@code table_name = 2} (DRUGS) {@code tableId} remap in {@link #copyCasemgmtNoteGroup}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old {@code drugs.drugid} to new {@code drugs.drugid}
     */
    Map<Long, Long> copyDrugsGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies eForm tables:
     * {@code eform_data} (parent) → {@code eform_values} (child with extra {@code demographicId} column).
     * Returns the old-to-new eform PK map needed for {@code casemgmt_note_link}
     * {@code table_name = 6} (EFORMDATA) {@code tableId} remap in {@link #copyCasemgmtNoteGroup}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old {@code eform_data.id} to new {@code eform_data.id}
     */
    Map<Long, Long> copyEformGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies email tables:
     * {@code emailLog} (parent, {@code @ManyToOne Demographic}) →
     * {@code emailAttachment} (child, {@code @ManyToOne EmailLog}).
     * Returns the old-to-new email log PK map needed for {@code casemgmt_note_link}
     * {@code table_name = 12} (EMAIL) {@code tableId} remap in {@link #copyCasemgmtNoteGroup}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old {@code emailLog.id} to new {@code emailLog.id}
     */
    Map<Long, Long> copyEmailGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies eReferral tables:
     * {@code erefer_attachment} (parent) — no child tables currently mapped.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     */
    void copyEreferGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies BC BCAR 2020 form tables {@code [BC]}:
     * {@code formBCAR2020} (parent, Hibernate) →
     * {@code formBCAR2020Text} (child, JDBC INSERT...SELECT — composite IdClass).
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     */
    void copyFormBCAR2020Group(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies Ontario ONAR Enhanced form tables {@code [ON]} via JDBC INSERT...SELECT
     * (three 100+-column tables have no entity classes):
     * {@code formONAREnhancedRecord}, {@code formONAREnhancedRecordExt1},
     * {@code formONAREnhancedRecordExt2}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     */
    void copyFormONAREnhancedGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies measurement tables:
     * {@code measurements} (parent) → {@code measurementsExt} (child).
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     */
    void copyMeasurementsGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies prevention tables:
     * {@code preventions} (parent) → {@code preventionsExt} (child).
     * Returns the old-to-new prevention PK map needed for {@code casemgmt_note_link}
     * {@code table_name = 8} (PREVENTIONS) {@code tableId} remap in {@link #copyCasemgmtNoteGroup}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old {@code preventions.id} to new {@code preventions.id}
     */
    Map<Long, Long> copyPreventionsGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies tickler tables:
     * {@code tickler} (parent) → {@code tickler_link} (child).
     * Returns the old-to-new tickler PK map needed for {@code casemgmt_note_link}
     * {@code table_name = 10} (TICKLER) {@code tableId} remap in {@link #copyCasemgmtNoteGroup}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old {@code tickler.id} to new {@code tickler.id}
     */
    Map<Long, Long> copyTicklerGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    // -------------------------------------------------------------------------
    // Part 2c — special-case tables
    // -------------------------------------------------------------------------

    /**
     * Copies consultation archive tables using the existing live-request PK map:
     * {@code consultationRequestsArchive} (parent) →
     * {@code consultationRequestExtArchive} (child with dual FK remap).
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @param requestPkMap        Map&lt;Long, Long&gt; mapping of old {@code requestId} to new {@code requestId}
     *                            (returned by {@link #copyConsultationsGroup})
     */
    void copyConsultationArchiveGroup(Integer sourceDemographicNo, Integer targetDemographicNo, Map<Long, Long> requestPkMap);

    /**
     * Copies {@code casemgmt_note} rows with their child tables
     * ({@code casemgmt_note_ext}, {@code casemgmt_note_link}).
     * After copying note links, remaps {@code table_id} for all link types whose entities
     * were also copied during merge, so associations point to the target patient's new records:
     * <ul>
     *   <li>{@code table_name = 1} (CASEMGMTNOTE) — remapped using the note PK map produced
     *       by this method itself (self-links between notes)</li>
     *   <li>{@code table_name = 11} (APPOINTMENT) — remapped using {@code appointmentPkMap}</li>
     *   <li>All types present in {@code linkedEntityPkMaps} — remapped using the supplied maps</li>
     * </ul>
     * Types not copied during merge (LABTEST=4, DOCUMENT=5, DEMOGRAPHIC=7, LABTEST2=9) are
     * intentionally excluded — their {@code table_id} values remain valid after merge.
     * Returns the old-to-new note PK map (note_id) needed by {@link #copyIssueNotesGroup}.
     *
     * @param sourceDemographicNo  Integer the source patient demographic number
     * @param targetDemographicNo  Integer the target patient demographic number
     * @param appointmentPkMap     Map&lt;Long, Long&gt; old appointment PK → new appointment PK
     *                             (returned by {@link #copyAppointments})
     * @param linkedEntityPkMaps   Map&lt;Integer, Map&lt;Long, Long&gt;&gt; keyed by
     *                             {@code CaseManagementNoteLink} integer constant; each value is
     *                             the old→new PK map for that entity type. Only non-empty maps
     *                             need to be included. Pass an empty map if none apply.
     * @return Map&lt;Long, Long&gt; mapping of old {@code note_id} to new {@code note_id}
     */
    Map<Long, Long> copyCasemgmtNoteGroup(Integer sourceDemographicNo, Integer targetDemographicNo, Map<Long, Long> appointmentPkMap, Map<Integer, Map<Long, Long>> linkedEntityPkMaps);

    /**
     * Copies {@code casemgmt_issue} rows with deduplication (skips rows where the target
     * already has the same {@code issue_id}). Returns the old-to-new issue PK map
     * needed by {@link #copyIssueNotesGroup}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old issue {@code id} to new issue {@code id}
     */
    Map<Long, Long> copyCasemgmtIssueGroup(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies {@code casemgmt_issue_notes} junction rows remapping both FK columns:
     * {@code id} (FK to {@code casemgmt_issue}) and {@code note_id}
     * (FK to {@code casemgmt_note}) simultaneously.
     *
     * @param issuePkMap Map&lt;Long, Long&gt; mapping of old {@code casemgmt_issue.id} to new id
     * @param notePkMap  Map&lt;Long, Long&gt; mapping of old {@code casemgmt_note.note_id} to new note_id
     */
    void copyIssueNotesGroup(Map<Long, Long> issuePkMap, Map<Long, Long> notePkMap);
}
