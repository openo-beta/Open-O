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
import ca.openosp.openo.commn.dao.DemographicMergeOperationDao;
import ca.openosp.openo.commn.model.*;
import ca.openosp.openo.test.base.OpenOTestBase;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DemographicMergeOperationDao}.
 * <p>
 * Covers the 21 test cases defined in DEMOGRAPHIC_MERGE_DAO_TEST_PLAN.md.
 * All tests run against H2 in-memory (MODE=MySQL) with Hibernate auto-create,
 * and roll back automatically via {@code @Transactional} + {@code @Rollback}
 * inherited from {@link OpenOTestBase}.
 *
 * @since 2026-04-01
 */
@Tag("integration")
@Tag("dao")
@Tag("demographic-merge")
@Transactional
public class DemographicMergeOperationDaoIntegrationTest extends OpenOTestBase {

    @Autowired
    private DemographicMergeOperationDao operationDao;

    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    /**
     * Persists a minimal Demographic row and returns it.
     * Only the three NOT NULL columns required by Demographic.hbm.xml are set.
     */
    private Demographic createDemographic() {
        Demographic d = new Demographic();
        d.setFirstName("Test");
        d.setLastName("Patient");
        d.setSex("M");
        entityManager.persist(d);
        entityManager.flush();
        return d;
    }

    /**
     * Builds and persists a CaseMgmtIssue for the given demographic and issueId.
     */
    private CaseMgmtIssue issue(int demographicNo, int issueId) {
        CaseMgmtIssue i = new CaseMgmtIssue();
        i.setDemographicNo(demographicNo);
        i.setIssueId(issueId);
        i.setType("medical");
        i.setUpdateDate(new Date());
        entityManager.persist(i);
        return i;
    }

    /**
     * Builds and persists a CaseMgmtNote for the given demographic.
     */
    private CaseMgmtNote note(int demographicNo) {
        CaseMgmtNote n = new CaseMgmtNote();
        n.setDemographicNo(demographicNo);
        n.setProviderNo("999");
        n.setNote("test note");
        n.setSigningProviderNo("999");
        n.setEncounterType("face");
        n.setBillingCode("");
        n.setProgramNo("0");
        n.setReporterCaisiRole("");
        n.setReporterProgramTeam("");
        n.setHistory("");
        n.setUpdateDate(new Date());
        n.setObservationDate(new Date());
        entityManager.persist(n);
        return n;
    }

    /**
     * Builds and persists a CaseMgmtNoteLink for the given note.
     */
    private CaseMgmtNoteLink noteLink(int noteId, int tableName, int tableId) {
        CaseMgmtNoteLink l = new CaseMgmtNoteLink();
        l.setNoteId(noteId);
        l.setTableName(tableName);
        l.setTableId(tableId);
        entityManager.persist(l);
        return l;
    }

    /**
     * Builds and persists a CaseMgmtIssueNotes junction row.
     * Both id and noteId are the composite PK — no auto-generate.
     */
    private CaseMgmtIssueNotes issueNote(int issueId, int noteId) {
        CaseMgmtIssueNotes jn = new CaseMgmtIssueNotes();
        jn.setId(issueId);
        jn.setNoteId(noteId);
        entityManager.persist(jn);
        return jn;
    }

    /**
     * Builds and persists a minimal Tickler for the given demographic.
     */
    private Tickler tickler(int demographicNo) {
        Tickler t = new Tickler();
        t.setDemographicNo(demographicNo);
        t.setMessage("test tickler");
        t.setStatus(Tickler.STATUS.A);
        t.setPriority(Tickler.PRIORITY.Normal);
        t.setCreator("999");
        t.setTaskAssignedTo("999");
        t.setServiceDate(new Date());
        entityManager.persist(t);
        return t;
    }

    // -------------------------------------------------------------------------
    // Group 1 — copyCasemgmtIssueGroup
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Group 1: copyCasemgmtIssueGroup")
    class CopyCasemgmtIssueGroupTests {

        // Baseline path: two source issues with distinct issueIds are both inserted on the target.
        // Verifies that the returned PK map has two entries and that each mapped value is a
        // newly assigned PK (not the original source PK), and that source rows are left untouched.
        @Test
        @DisplayName("Should copy all issues when no conflict with target")
        void shouldCopyAllIssues_whenNoConflictWithTarget() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            CaseMgmtIssue i1 = issue(S, 10);
            CaseMgmtIssue i2 = issue(S, 20);
            entityManager.flush();

            Map<Long, Long> result = operationDao.copyCasemgmtIssueGroup(S, T);
            entityManager.flush();
            entityManager.clear();

            assertThat(result).hasSize(2);

            List<CaseMgmtIssue> targetIssues = entityManager
                    .createQuery("SELECT i FROM CaseMgmtIssue i WHERE i.demographicNo = :d", CaseMgmtIssue.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(targetIssues).hasSize(2);

            // Both mapped values must be new PKs (different from source PKs)
            assertThat(result.get((long) i1.getId())).isNotEqualTo((long) i1.getId());
            assertThat(result.get((long) i2.getId())).isNotEqualTo((long) i2.getId());

            // Source rows untouched
            List<CaseMgmtIssue> sourceIssues = entityManager
                    .createQuery("SELECT i FROM CaseMgmtIssue i WHERE i.demographicNo = :d", CaseMgmtIssue.class)
                    .setParameter("d", S)
                    .getResultList();
            assertThat(sourceIssues).hasSize(2);
        }

        // Dedup path: when the target already has an issue with the same issueId, no new row is
        // inserted. Critically, the PK map must still contain an entry for the source PK pointing
        // to the existing target PK so that copyIssueNotesGroup can remap junction rows correctly.
        @Test
        @DisplayName("Should not insert duplicate when issue id already exists on target")
        void shouldNotInsertDuplicate_whenIssueIdAlreadyExistsOnTarget() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            CaseMgmtIssue existing = issue(T, 42);
            CaseMgmtIssue srcIssue = issue(S, 42);
            entityManager.flush();

            long existingTargetPk = existing.getId();
            long sourcePk = srcIssue.getId();

            Map<Long, Long> result = operationDao.copyCasemgmtIssueGroup(S, T);
            entityManager.flush();
            entityManager.clear();

            // No new row inserted for target
            List<CaseMgmtIssue> targetIssues = entityManager
                    .createQuery("SELECT i FROM CaseMgmtIssue i WHERE i.demographicNo = :d", CaseMgmtIssue.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(targetIssues).hasSize(1);

            assertThat(result).hasSize(1);
            // Critical: source PK must map to the EXISTING target PK, not null
            assertThat(result.get(sourcePk)).isEqualTo(existingTargetPk);
        }

        // Both dedup and insert branches fire in a single invocation. Confirms that the unique
        // issue is inserted and mapped to a new PK while the duplicate is correctly mapped to the
        // existing target PK — ensuring neither branch accidentally swallows the other's map entry.
        @Test
        @DisplayName("Should handle mixed unique and duplicate issues returning correct pk map")
        void shouldHandleMixedUniqueAndDuplicate_returningCorrectPkMap() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            CaseMgmtIssue existing = issue(T, 42);
            CaseMgmtIssue srcDup = issue(S, 42);
            CaseMgmtIssue srcNew = issue(S, 55);
            entityManager.flush();

            long existingTargetPk = existing.getId();
            long sourcePkDup = srcDup.getId();
            long sourcePkNew = srcNew.getId();

            Map<Long, Long> result = operationDao.copyCasemgmtIssueGroup(S, T);
            entityManager.flush();
            entityManager.clear();

            // Target now has 2 rows: original + new for issueId=55
            List<CaseMgmtIssue> targetIssues = entityManager
                    .createQuery("SELECT i FROM CaseMgmtIssue i WHERE i.demographicNo = :d", CaseMgmtIssue.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(targetIssues).hasSize(2);

            assertThat(result).hasSize(2);
            assertThat(result.get(sourcePkDup)).isEqualTo(existingTargetPk);
            assertThat(result.get(sourcePkNew)).isNotNull();
            assertThat(result.get(sourcePkNew)).isNotEqualTo(sourcePkNew);
        }
    }

    // -------------------------------------------------------------------------
    // Group 2 — copyIdentityTables
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Group 2: copyIdentityTables")
    class CopyIdentityTablesTests {

        // A-wins gap-fill logic for demographicExt on a secondary pass. A key that already exists
        // on the target must keep its current value (primary wins), while a key present only on the
        // source must be inserted — verifying the skip-if-present and copy-if-absent branches.
        @Test
        @DisplayName("Should gap fill demographic ext when secondary pass and key already exists on target")
        void shouldGapFillDemographicExt_whenSecondaryPassAndKeyAlreadyExists() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            // Target already has "lang" — primary wins
            DemographicExt tgtLang = new DemographicExt("", T, "lang", "en");
            entityManager.persist(tgtLang);
            // Source has conflicting "lang" and new "height"
            DemographicExt srcLang = new DemographicExt("", S, "lang", "fr");
            entityManager.persist(srcLang);
            DemographicExt srcHeight = new DemographicExt("", S, "height", "170");
            entityManager.persist(srcHeight);
            entityManager.flush();

            operationDao.copyIdentityTables(S, T, true);
            entityManager.flush();
            entityManager.clear();

            List<DemographicExt> rows = entityManager
                    .createQuery("SELECT e FROM DemographicExt e WHERE e.demographicNo = :d", DemographicExt.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(rows).hasSize(2);

            DemographicExt lang = rows.stream().filter(r -> "lang".equals(r.getKey())).findFirst().orElse(null);
            DemographicExt height = rows.stream().filter(r -> "height".equals(r.getKey())).findFirst().orElse(null);
            assertThat(lang).isNotNull();
            assertThat(lang.getValue()).isEqualTo("en");   // primary wins
            assertThat(height).isNotNull();
            assertThat(height.getValue()).isEqualTo("170"); // gap-fill
        }

        // Field-level merge for demographiccust when the target already has a row. Fields that are
        // already populated on the target must not be overwritten, while null or blank fields must
        // be filled from the source — confirming the per-field conditional update logic.
        @Test
        @DisplayName("Should field merge demographic cust when target row already exists")
        void shouldFieldMergeDemographicCust_whenTargetRowAlreadyExists() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            DemographicCust tgtCust = new DemographicCust();
            tgtCust.setId(T);
            tgtCust.setNurse("NurseA");
            // midwife and notes left null
            entityManager.persist(tgtCust);

            DemographicCust srcCust = new DemographicCust();
            srcCust.setId(S);
            srcCust.setNurse("NurseB");
            srcCust.setMidwife("MidwifeB");
            srcCust.setNotes("some note");
            entityManager.persist(srcCust);
            entityManager.flush();

            operationDao.copyIdentityTables(S, T, true);
            entityManager.flush();
            entityManager.clear();

            DemographicCust merged = entityManager.find(DemographicCust.class, T);
            assertThat(merged).isNotNull();
            assertThat(merged.getNurse()).isEqualTo("NurseA");      // unchanged
            assertThat(merged.getMidwife()).isEqualTo("MidwifeB");  // gap-filled
            assertThat(merged.getNotes()).isEqualTo("some note");   // gap-filled

            Long count = entityManager
                    .createQuery("SELECT COUNT(c) FROM DemographicCust c WHERE c.id = :d", Long.class)
                    .setParameter("d", T)
                    .getSingleResult();
            assertThat(count).isEqualTo(1L);
        }

        // Third branch of the demographiccust logic: target has no row at all during a secondary
        // pass. The source row must be inserted with PK set to the target demographicNo — verifying
        // the DAO does not crash on a missing target row and correctly handles the manual PK set.
        @Test
        @DisplayName("Should copy demographic cust when target has no row on secondary pass")
        void shouldCopyDemographicCust_whenTargetHasNoRowOnSecondaryPass() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            DemographicCust srcCust = new DemographicCust();
            srcCust.setId(S);
            srcCust.setNurse("NurseB");
            srcCust.setAlert("Allergy");
            entityManager.persist(srcCust);
            entityManager.flush();

            operationDao.copyIdentityTables(S, T, true);
            entityManager.flush();
            entityManager.clear();

            DemographicCust created = entityManager.find(DemographicCust.class, T);
            assertThat(created).isNotNull();
            assertThat(created.getId()).isEqualTo(T);
            assertThat(created.getNurse()).isEqualTo("NurseB");
            assertThat(created.getAlert()).isEqualTo("Allergy");
        }

        // Unlike demographicExt, archive rows are never deduplicated — every source archive row
        // must be copied regardless of whether the target already has a row with the same key.
        // Confirms the intentional design: both the primary and secondary archive rows coexist.
        @Test
        @DisplayName("Should always copy demographic ext archive on secondary pass without dedup")
        void shouldAlwaysCopyDemographicExtArchive_onSecondaryPass() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            // Simulate primary pass already ran — target has one archive row
            DemographicExtArchive tgtArchive = new DemographicExtArchive();
            tgtArchive.setDemographicNo(T);
            tgtArchive.setKey("lang");
            tgtArchive.setValue("en");
            entityManager.persist(tgtArchive);

            // Secondary has same key — archive is NOT deduplicated
            DemographicExtArchive srcArchive = new DemographicExtArchive();
            srcArchive.setDemographicNo(S);
            srcArchive.setKey("lang");
            srcArchive.setValue("fr");
            entityManager.persist(srcArchive);
            entityManager.flush();

            operationDao.copyIdentityTables(S, T, true);
            entityManager.flush();
            entityManager.clear();

            List<DemographicExtArchive> rows = entityManager
                    .createQuery("SELECT a FROM DemographicExtArchive a WHERE a.demographicNo = :d",
                            DemographicExtArchive.class)
                    .setParameter("d", T)
                    .getResultList();
            // Both rows must exist — no dedup on archive
            assertThat(rows).hasSize(2);
        }
    }

    // -------------------------------------------------------------------------
    // Group 3 — copyCasemgmtNoteGroup
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Group 3: copyCasemgmtNoteGroup")
    class CopyCasemgmtNoteGroupTests {

        // APPOINTMENT link remap (tableName=11): after notes are copied, any casemgmt_note_link
        // row that references an old appointment PK must have its tableId updated to the new
        // appointment PK supplied in appointmentPkMap — the most common link type in production.
        @Test
        @DisplayName("Should remap appointment link table id after note group copy")
        void shouldRemapAppointmentLinkTableId_afterNoteGroupCopy() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            // Create old appointment for source
            Appointment oldAppt = new Appointment();
            oldAppt.setDemographicNo(S);
            entityManager.persist(oldAppt);

            // Create new appointment for target (simulates already-copied appt)
            Appointment newAppt = new Appointment();
            newAppt.setDemographicNo(T);
            entityManager.persist(newAppt);

            CaseMgmtNote srcNote = note(S);
            entityManager.flush();

            long oldApptPk = oldAppt.getId();
            long newApptPk = newAppt.getId();

            noteLink(srcNote.getNoteId(), CaseManagementNoteLink.APPOINTMENT, (int) oldApptPk);
            entityManager.flush();

            Map<Long, Long> appointmentPkMap = Map.of(oldApptPk, newApptPk);

            Map<Long, Long> notePkMap = operationDao.copyCasemgmtNoteGroup(
                    S, T, appointmentPkMap, Collections.emptyMap());
            entityManager.flush();
            entityManager.clear();

            assertThat(notePkMap).hasSize(1);
            long newNotePk = notePkMap.values().iterator().next();

            List<CaseMgmtNoteLink> links = entityManager
                    .createQuery("SELECT l FROM CaseMgmtNoteLink l WHERE l.noteId = :nid AND l.tableName = 11",
                            CaseMgmtNoteLink.class)
                    .setParameter("nid", (int) newNotePk)
                    .getResultList();
            assertThat(links).hasSize(1);
            assertThat(links.get(0).getTableId()).isEqualTo((int) newApptPk);
        }

        // DOCUMENT (tableName=5) and LABTEST (tableName=4) link types are intentionally excluded
        // from remapping because their referenced rows are not copied during merge. Verifies that
        // a "remap everything" bug cannot silently corrupt these tableId values.
        @Test
        @DisplayName("Should not remap document or labtest links after note group copy")
        void shouldNotRemapDocumentOrLabtestLinks_afterNoteGroupCopy() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            CaseMgmtNote srcNote = note(S);
            noteLink(srcNote.getNoteId(), CaseManagementNoteLink.DOCUMENT, 300);  // DOCUMENT
            noteLink(srcNote.getNoteId(), CaseManagementNoteLink.LABTEST, 400);  // LABTEST
            entityManager.flush();

            operationDao.copyCasemgmtNoteGroup(S, T, Collections.emptyMap(), Collections.emptyMap());
            entityManager.flush();
            entityManager.clear();

            CaseMgmtNote newNote = entityManager
                    .createQuery("SELECT n FROM CaseMgmtNote n WHERE n.demographicNo = :d", CaseMgmtNote.class)
                    .setParameter("d", T)
                    .getSingleResult();

            List<CaseMgmtNoteLink> docLinks = entityManager
                    .createQuery("SELECT l FROM CaseMgmtNoteLink l WHERE l.noteId = :nid AND l.tableName = 5",
                            CaseMgmtNoteLink.class)
                    .setParameter("nid", newNote.getNoteId())
                    .getResultList();
            assertThat(docLinks).hasSize(1);
            assertThat(docLinks.get(0).getTableId()).isEqualTo(300);

            List<CaseMgmtNoteLink> labLinks = entityManager
                    .createQuery("SELECT l FROM CaseMgmtNoteLink l WHERE l.noteId = :nid AND l.tableName = 4",
                            CaseMgmtNoteLink.class)
                    .setParameter("nid", newNote.getNoteId())
                    .getResultList();
            assertThat(labLinks).hasSize(1);
            assertThat(labLinks.get(0).getTableId()).isEqualTo(400);
        }

        // CASEMGMTNOTE self-link (tableName=1): one note referencing another note on the same
        // patient. Both notes are copied, so the link's tableId must be remapped from the old
        // note PK to the newly assigned note PK using the map produced by this method itself.
        @Test
        @DisplayName("Should remap self link when note links to another note on same patient")
        void shouldRemapSelfLink_whenNoteLinksToAnotherNoteOnSamePatient() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            CaseMgmtNote noteA = note(S);
            CaseMgmtNote noteB = note(S);
            // noteA links to noteB (tableName=1, CASEMGMTNOTE)
            noteLink(noteA.getNoteId(), CaseManagementNoteLink.CASEMGMTNOTE, noteB.getNoteId());
            entityManager.flush();

            long noteA_pk = noteA.getNoteId();
            long noteB_pk = noteB.getNoteId();

            Map<Long, Long> notePkMap = operationDao.copyCasemgmtNoteGroup(
                    S, T, Collections.emptyMap(), Collections.emptyMap());
            entityManager.flush();
            entityManager.clear();

            assertThat(notePkMap).hasSize(2);
            long newNoteA = notePkMap.get(noteA_pk);
            long newNoteB = notePkMap.get(noteB_pk);

            List<CaseMgmtNoteLink> selfLinks = entityManager
                    .createQuery("SELECT l FROM CaseMgmtNoteLink l WHERE l.noteId = :nid AND l.tableName = 1",
                            CaseMgmtNoteLink.class)
                    .setParameter("nid", (int) newNoteA)
                    .getResultList();
            assertThat(selfLinks).hasSize(1);
            assertThat(selfLinks.get(0).getTableId()).isEqualTo((int) newNoteB);
        }
    }

    // -------------------------------------------------------------------------
    // Group 4 — copyIssueNotesGroup
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Group 4: copyIssueNotesGroup")
    class CopyIssueNotesGroupTests {

        // casemgmt_issue_notes is a junction table with a composite PK (issue id + note id).
        // Both FK columns must be remapped simultaneously: the issue PK to the new issue PK and
        // the note PK to the new note PK. Verifies the dual-remap INSERT and that the original row survives.
        @Test
        @DisplayName("Should insert junction row with both foreign keys remapped")
        void shouldInsertJunctionRowWithBothFKsRemapped() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            CaseMgmtIssue srcIssue = issue(S, 100);
            CaseMgmtIssue tgtIssue = issue(T, 200);
            CaseMgmtNote srcNote = note(S);
            CaseMgmtNote tgtNote = note(T);
            entityManager.flush();

            long oldIssuePk = srcIssue.getId();
            long newIssuePk = tgtIssue.getId();
            long oldNotePk = srcNote.getNoteId();
            long newNotePk = tgtNote.getNoteId();

            issueNote(srcIssue.getId(), srcNote.getNoteId());
            entityManager.flush();

            Map<Long, Long> issuePkMap = Map.of(oldIssuePk, newIssuePk);
            Map<Long, Long> notePkMap = Map.of(oldNotePk, newNotePk);

            operationDao.copyIssueNotesGroup(issuePkMap, notePkMap);
            entityManager.flush();
            entityManager.clear();

            // New junction row must exist
            Long count = entityManager
                    .createQuery("SELECT COUNT(n) FROM CaseMgmtIssueNotes n WHERE n.id = :i AND n.noteId = :n",
                            Long.class)
                    .setParameter("i", (int) newIssuePk)
                    .setParameter("n", (int) newNotePk)
                    .getSingleResult();
            assertThat(count).isEqualTo(1L);

            // Original row still exists (INSERT, not UPDATE)
            Long origCount = entityManager
                    .createQuery("SELECT COUNT(n) FROM CaseMgmtIssueNotes n WHERE n.id = :i AND n.noteId = :n",
                            Long.class)
                    .setParameter("i", (int) oldIssuePk)
                    .setParameter("n", (int) oldNotePk)
                    .getSingleResult();
            assertThat(origCount).isEqualTo(1L);
        }

        // The HQL WHERE note_id IN :oldNoteIds clause filters out junction rows whose note was not
        // in the copy set. Without this guard a stale note_id would be inserted, creating a broken
        // FK. Verifies only the mapped note produces a new junction row; the unmapped one is skipped.
        @Test
        @DisplayName("Should skip junction row when note is not in note pk map")
        void shouldSkipJunctionRow_whenNoteIsNotInNotePkMap() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            CaseMgmtIssue srcIssue = issue(S, 100);
            CaseMgmtIssue tgtIssue = issue(T, 200);
            CaseMgmtNote srcNote = note(S);
            CaseMgmtNote tgtNote = note(T);
            CaseMgmtNote unmappedNote = note(S);
            entityManager.flush();

            long oldIssuePk = srcIssue.getId();
            long newIssuePk = tgtIssue.getId();
            long oldNotePk = srcNote.getNoteId();
            long newNotePk = tgtNote.getNoteId();
            long unmappedNotePk = unmappedNote.getNoteId();

            issueNote(srcIssue.getId(), srcNote.getNoteId());
            issueNote(srcIssue.getId(), unmappedNote.getNoteId());
            entityManager.flush();

            Map<Long, Long> issuePkMap = Map.of(oldIssuePk, newIssuePk);
            // unmappedNotePk intentionally excluded from notePkMap
            Map<Long, Long> notePkMap = Map.of(oldNotePk, newNotePk);

            operationDao.copyIssueNotesGroup(issuePkMap, notePkMap);
            entityManager.flush();
            entityManager.clear();

            // Only the mapped note should appear in junction for newIssuePk
            Long count = entityManager
                    .createQuery("SELECT COUNT(n) FROM CaseMgmtIssueNotes n WHERE n.id = :i",
                            Long.class)
                    .setParameter("i", (int) newIssuePk)
                    .getSingleResult();
            assertThat(count).isEqualTo(1L);

            // The unmapped note must not have been inserted
            Long unmappedCount = entityManager
                    .createQuery("SELECT COUNT(n) FROM CaseMgmtIssueNotes n WHERE n.id = :i AND n.noteId = :n",
                            Long.class)
                    .setParameter("i", (int) newIssuePk)
                    .setParameter("n", (int) unmappedNotePk)
                    .getSingleResult();
            assertThat(unmappedCount).isEqualTo(0L);
        }
    }

    // -------------------------------------------------------------------------
    // Group 5 — copyConsultationArchiveGroup
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Group 5: copyConsultationArchiveGroup")
    class CopyConsultationArchiveGroupTests {

        // consultationRequestExtArchive carries two FK columns that must both be remapped: the
        // archive parent PK (to the newly inserted archive row) and the requestId (via requestPkMap
        // passed from copyConsultationsGroup). Confirms data is preserved and both FKs are correct.
        @Test
        @DisplayName("Should remap both foreign keys in consultation request ext archive")
        void shouldRemapBothFKs_inConsultationRequestExtArchive() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            ProfessionalSpecialist specialist = new ProfessionalSpecialist();
            entityManager.persist(specialist);

            ConsultationRequestArchive archive = new ConsultationRequestArchive();
            archive.setDemographicId(S);
            archive.setProfessionalSpecialist(specialist);
            entityManager.persist(archive);
            entityManager.flush();

            int oldArchivePk = archive.getId();

            // Insert ext archive row manually
            entityManager.createNativeQuery(
                    "INSERT INTO consultationRequestExtArchive "
                    + "(consultationRequestArchiveId, requestId, name, value) "
                    + "VALUES (:archId, :reqId, :k, :v)")
                    .setParameter("archId", oldArchivePk)
                    .setParameter("reqId", 50)
                    .setParameter("k", "k")
                    .setParameter("v", "v")
                    .executeUpdate();
            entityManager.flush();

            Map<Long, Long> requestPkMap = Map.of(50L, 9999L);

            operationDao.copyConsultationArchiveGroup(S, T, requestPkMap);
            entityManager.flush();
            entityManager.clear();

            List<ConsultationRequestArchive> targetArchives = entityManager
                    .createQuery("SELECT a FROM ConsultationRequestArchive a WHERE a.demographicId = :d",
                            ConsultationRequestArchive.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(targetArchives).hasSize(1);
            int newArchivePk = targetArchives.get(0).getId();

            List<ConsultationRequestExtArchive> extRows = entityManager
                    .createQuery("SELECT e FROM ConsultationRequestExtArchive e "
                            + "WHERE e.consultationRequestArchiveId = :aid",
                            ConsultationRequestExtArchive.class)
                    .setParameter("aid", newArchivePk)
                    .getResultList();
            assertThat(extRows).hasSize(1);
            assertThat(extRows.get(0).getRequestId()).isEqualTo(9999);
            assertThat(extRows.get(0).getKey()).isEqualTo("k");
            assertThat(extRows.get(0).getValue()).isEqualTo("v");
        }

        // When requestPkMap is empty there are no live consultation requests to remap against, so
        // copying the ext archive rows with a stale requestId would corrupt data. Verifies the
        // if (!requestPkMap.isEmpty()) guard: the parent archive row is copied but ext rows are not.
        @Test
        @DisplayName("Should copy archive row only when request pk map is empty")
        void shouldCopyArchiveRowOnly_whenRequestPkMapIsEmpty() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            ProfessionalSpecialist specialist = new ProfessionalSpecialist();
            entityManager.persist(specialist);

            ConsultationRequestArchive archive = new ConsultationRequestArchive();
            archive.setDemographicId(S);
            archive.setProfessionalSpecialist(specialist);
            entityManager.persist(archive);
            entityManager.flush();

            int oldArchivePk = archive.getId();

            entityManager.createNativeQuery(
                    "INSERT INTO consultationRequestExtArchive "
                    + "(consultationRequestArchiveId, requestId, name, value) "
                    + "VALUES (:archId, :reqId, :k, :v)")
                    .setParameter("archId", oldArchivePk)
                    .setParameter("reqId", 50)
                    .setParameter("k", "k")
                    .setParameter("v", "v")
                    .executeUpdate();
            entityManager.flush();

            operationDao.copyConsultationArchiveGroup(S, T, Collections.emptyMap());
            entityManager.flush();
            entityManager.clear();

            // Parent copied
            List<ConsultationRequestArchive> targetArchives = entityManager
                    .createQuery("SELECT a FROM ConsultationRequestArchive a WHERE a.demographicId = :d",
                            ConsultationRequestArchive.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(targetArchives).hasSize(1);
            int newArchivePk = targetArchives.get(0).getId();

            // Ext rows NOT copied because requestPkMap was empty
            List<ConsultationRequestExtArchive> extRows = entityManager
                    .createQuery("SELECT e FROM ConsultationRequestExtArchive e "
                            + "WHERE e.consultationRequestArchiveId = :aid",
                            ConsultationRequestExtArchive.class)
                    .setParameter("aid", newArchivePk)
                    .getResultList();
            assertThat(extRows).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // Group 6 — copyTicklerGroup, copyBillingGroup, copyClinicalDirectRecords,
    //           copyDrugsGroup
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Group 6: copyTicklerGroup / copyBillingGroup / copyClinicalDirectRecords / copyDrugsGroup")
    class Group6Tests {

        // Regression guard for the fix "copy tickler_comments rows in copyTicklerGroup". The
        // tickler_comments copy block sits at the end of copyTicklerGroup and has no demographicNo
        // column — it is linked only through ticklerNo. Ensures it is not accidentally removed.
        @Test
        @DisplayName("Should copy tickler comments when tickler group is copied")
        void shouldCopyTicklerComments_whenTicklerGroupCopied() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            Tickler t = tickler(S);
            entityManager.flush();
            int oldTicklerPk = t.getId();

            TicklerComment comment = new TicklerComment();
            comment.setTicklerNo(oldTicklerPk);
            comment.setMessage("follow up");
            comment.setProviderNo("999");
            entityManager.persist(comment);
            entityManager.flush();

            Map<Long, Long> pkMap = operationDao.copyTicklerGroup(S, T);
            entityManager.flush();
            entityManager.clear();

            Long newTicklerPk = pkMap.get((long) oldTicklerPk);
            assertThat(newTicklerPk).isNotNull();

            List<TicklerComment> newComments = entityManager
                    .createQuery("SELECT c FROM TicklerComment c WHERE c.ticklerNo = :tid",
                            TicklerComment.class)
                    .setParameter("tid", newTicklerPk.intValue())
                    .getResultList();
            assertThat(newComments).hasSize(1);
            assertThat(newComments.get(0).getMessage()).isEqualTo("follow up");
            assertThat(newComments.get(0).getProviderNo()).isEqualTo("999");

            // Original comment untouched
            List<TicklerComment> origComments = entityManager
                    .createQuery("SELECT c FROM TicklerComment c WHERE c.ticklerNo = :tid",
                            TicklerComment.class)
                    .setParameter("tid", oldTicklerPk)
                    .getResultList();
            assertThat(origComments).hasSize(1);
        }

        // Two-step billing copy: transactions are first inserted pointing to the target demographic,
        // then a separate JPQL bulk UPDATE remaps ch1Id from the old header PK to the new one.
        // If the UPDATE has a wrong parameter or WHERE clause, ch1Id silently points to the wrong header.
        @Test
        @DisplayName("Should remap ch1 id on billing transaction after copy")
        void shouldRemapCh1Id_onBillingTransactionAfterCopy() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            BillingONCHeader1 header = new BillingONCHeader1();
            header.setHeaderId(1);
            header.setDemographicNo(S);
            entityManager.persist(header);
            entityManager.flush();
            int oldCh1Pk = header.getId();

            BillingOnTransaction txn = new BillingOnTransaction();
            txn.setId(0); // primitive int — 0 signals auto-assign via IDENTITY
            txn.setDemographicNo(S);
            txn.setCh1Id(oldCh1Pk);
            txn.setUpdateProviderNo("999"); // NOT NULL column with no default
            entityManager.persist(txn);
            entityManager.flush();

            operationDao.copyBillingGroup(S, T);
            entityManager.flush();
            entityManager.clear();

            List<BillingONCHeader1> newHeaders = entityManager
                    .createQuery("SELECT h FROM BillingONCHeader1 h WHERE h.demographicNo = :d",
                            BillingONCHeader1.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(newHeaders).hasSize(1);
            int newCh1Pk = newHeaders.get(0).getId();

            List<BillingOnTransaction> newTxns = entityManager
                    .createQuery("SELECT t FROM BillingOnTransaction t WHERE t.demographicNo = :d",
                            BillingOnTransaction.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(newTxns).hasSize(1);
            assertThat(newTxns.get(0).getCh1Id()).isEqualTo(newCh1Pk);
        }

        // CtlDocument has a composite PK (module, moduleId, documentNo). When both primary and
        // secondary demographics reference the same documentNo, a secondary pass must not attempt
        // a second INSERT — the entityManager.find() guard must prevent EntityExistsException.
        @Test
        @DisplayName("Should not insert duplicate ctl document when same document already linked to target")
        void shouldNotInsertDuplicateCtlDocument_whenSameDocumentAlreadyLinkedToTarget() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            // Primary pass already ran — target already linked to document 100
            CtlDocument tgtDoc = new CtlDocument();
            tgtDoc.setId(new CtlDocumentPK("demographic", T, 100));
            entityManager.persist(tgtDoc);

            // Secondary also references document 100
            CtlDocument srcDoc = new CtlDocument();
            srcDoc.setId(new CtlDocumentPK("demographic", S, 100));
            entityManager.persist(srcDoc);
            entityManager.flush();

            // Should not throw EntityExistsException
            operationDao.copyClinicalDirectRecords(S, T);
            entityManager.flush();
            entityManager.clear();

            Long count = entityManager
                    .createQuery("SELECT COUNT(c) FROM CtlDocument c "
                            + "WHERE c.id.module = 'demographic' AND c.id.moduleId = :mid AND c.id.documentNo = 100",
                            Long.class)
                    .setParameter("mid", T)
                    .getSingleResult();
            assertThat(count).isEqualTo(1L);
        }

        // DrugReason HQL INSERT remaps two columns at once: drugId (FK to new drug PK) and
        // demographicNo (the patient column on the child row). If the :targetDemo binding is
        // dropped or mis-bound, DrugReason rows end up with the source demographicNo — silent corruption.
        @Test
        @DisplayName("Should remap drug reason demographic number to target not source")
        void shouldRemapDrugReasonDemographicNo_toTarget_notSource() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            Drug drug = new Drug();
            drug.setDemographicId(S);
            entityManager.persist(drug);
            entityManager.flush();
            int oldDrugPk = drug.getId();

            DrugReason reason = new DrugReason();
            reason.setDrugId(oldDrugPk);
            reason.setDemographicNo(S);
            reason.setCode("ABC");
            reason.setPrimaryReasonFlag(true);
            reason.setArchivedFlag(false);
            reason.setProviderNo("999");
            entityManager.persist(reason);
            entityManager.flush();

            Map<Long, Long> pkMap = operationDao.copyDrugsGroup(S, T);
            entityManager.flush();
            entityManager.clear();

            Long newDrugPk = pkMap.get((long) oldDrugPk);
            assertThat(newDrugPk).isNotNull();

            List<DrugReason> newReasons = entityManager
                    .createQuery("SELECT r FROM DrugReason r WHERE r.drugId = :did",
                            DrugReason.class)
                    .setParameter("did", newDrugPk.intValue())
                    .getResultList();
            assertThat(newReasons).hasSize(1);
            assertThat(newReasons.get(0).getDemographicNo()).isEqualTo(T);
            assertThat(newReasons.get(0).getCode()).isEqualTo("ABC");

            // Original row untouched
            List<DrugReason> origReasons = entityManager
                    .createQuery("SELECT r FROM DrugReason r WHERE r.drugId = :did",
                            DrugReason.class)
                    .setParameter("did", oldDrugPk)
                    .getResultList();
            assertThat(origReasons).hasSize(1);
            assertThat(origReasons.get(0).getDemographicNo()).isEqualTo(S);
        }
    }

    // -------------------------------------------------------------------------
    // Group 7 — copyClinicalDirectRecords (CaseMgmtCpp), copyEmailGroup,
    //           copyEreferGroup
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Group 7: CaseMgmtCpp / copyEmailGroup / copyEreferGroup")
    class Group7Tests {

        // CaseMgmtCpp stores demographicNo as a String column, not an int. The DAO must use
        // String.valueOf() for both the source query parameter and the target setter. If either
        // conversion is omitted the row is not found or is written with the wrong demographic number.
        @Test
        @DisplayName("Should remap case management cpp demographic number stored as string to target")
        void shouldRemapCaseMgmtCppDemographicNo_asString_toTarget() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            CaseMgmtCpp cpp = new CaseMgmtCpp();
            // demographicNo stored as String in this entity
            cpp.setDemographicNo(String.valueOf(S));
            cpp.setProviderNo("999");
            entityManager.persist(cpp);
            entityManager.flush();

            operationDao.copyClinicalDirectRecords(S, T);
            entityManager.flush();
            entityManager.clear();

            List<CaseMgmtCpp> targetRows = entityManager
                    .createQuery("SELECT c FROM CaseMgmtCpp c WHERE c.demographicNo = :d",
                            CaseMgmtCpp.class)
                    .setParameter("d", String.valueOf(T))
                    .getResultList();
            assertThat(targetRows).hasSize(1);
            assertThat(targetRows.get(0).getDemographicNo()).isEqualTo(String.valueOf(T));

            // Source row untouched
            List<CaseMgmtCpp> sourceRows = entityManager
                    .createQuery("SELECT c FROM CaseMgmtCpp c WHERE c.demographicNo = :d",
                            CaseMgmtCpp.class)
                    .setParameter("d", String.valueOf(S))
                    .getResultList();
            assertThat(sourceRows).hasSize(1);
        }

        // Two special steps protect the email attachment copy: (1) emailAttachments collection is
        // nulled out before persist to prevent CascadeType.PERSIST from re-inserting old children,
        // and (2) attachments are re-inserted via a native INSERT-SELECT. Both steps must remain intact.
        @Test
        @DisplayName("Should copy email attachments via email group")
        void shouldCopyEmailAttachments_viaEmailGroup() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            // EmailLog requires a real Demographic FK (@ManyToOne)
            EmailLog log = new EmailLog();
            log.setDemographic(src);
            entityManager.persist(log);
            entityManager.flush();
            int oldLogPk = log.getId();

            // Insert attachment via native SQL using actual column names (logId, fileName, etc.)
            entityManager.createNativeQuery(
                    "INSERT INTO emailAttachment (logId, fileName, filePath, documentType, documentId) "
                    + "VALUES (:logId, :name, '', 'EFORM', 0)")
                    .setParameter("logId", oldLogPk)
                    .setParameter("name", "referral.pdf")
                    .executeUpdate();
            entityManager.flush();

            operationDao.copyEmailGroup(S, T);
            entityManager.flush();
            entityManager.clear();

            List<EmailLog> newLogs = entityManager
                    .createQuery("SELECT e FROM EmailLog e WHERE e.demographic.DemographicNo = :d",
                            EmailLog.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(newLogs).hasSize(1);
            int newLogPk = newLogs.get(0).getId();

            // Verify attachment was copied (DAO uses logId column for the FK)
            List<?> newAttachments = entityManager
                    .createNativeQuery("SELECT * FROM emailAttachment WHERE logId = :logId")
                    .setParameter("logId", newLogPk)
                    .getResultList();
            assertThat(newAttachments).hasSize(1);

            // Source attachment still exists
            List<?> srcAttachments = entityManager
                    .createNativeQuery("SELECT * FROM emailAttachment WHERE logId = :logId")
                    .setParameter("logId", oldLogPk)
                    .getResultList();
            assertThat(srcAttachments).hasSize(1);
        }

        // EReferAttachment has a LAZY @OneToMany collection. After detach the collection is an
        // uninitialized PersistentBag; calling setAttachments(new ArrayList<>()) before persist
        // replaces it so no lazy-load is triggered. Removing that line would throw LazyInitializationException.
        @Test
        @DisplayName("Should copy erefer attachment data without lazy initialization exception")
        void shouldCopyEreferAttachmentData_withoutLazyInitializationException() {
            Demographic src = createDemographic();
            Demographic tgt = createDemographic();
            int S = src.getDemographicNo();
            int T = tgt.getDemographicNo();

            // EReferAttachment holds demographicNo directly (no EReferral parent)
            EReferAttachment attachment = new EReferAttachment(S);
            entityManager.persist(attachment);
            entityManager.flush();
            int oldAttachmentPk = attachment.getId();

            // Insert attachment data row via native SQL using actual composite columns
            // (erefer_attachment_id, lab_id, lab_type) — no auto-increment PK
            entityManager.createNativeQuery(
                    "INSERT INTO erefer_attachment_data (erefer_attachment_id, lab_id, lab_type) "
                    + "VALUES (:id, :labId, :labType)")
                    .setParameter("id", oldAttachmentPk)
                    .setParameter("labId", 1)
                    .setParameter("labType", "HL7")
                    .executeUpdate();
            entityManager.flush();

            // Should not throw LazyInitializationException
            operationDao.copyEreferGroup(S, T);
            entityManager.flush();
            entityManager.clear();

            List<EReferAttachment> newAttachments = entityManager
                    .createQuery("SELECT a FROM EReferAttachment a WHERE a.demographicNo = :d",
                            EReferAttachment.class)
                    .setParameter("d", T)
                    .getResultList();
            assertThat(newAttachments).hasSize(1);
            int newAttachmentPk = newAttachments.get(0).getId();

            // Verify attachment data was copied
            List<?> newData = entityManager
                    .createNativeQuery(
                            "SELECT * FROM erefer_attachment_data WHERE erefer_attachment_id = :id")
                    .setParameter("id", newAttachmentPk)
                    .getResultList();
            assertThat(newData).hasSize(1);

            // Source data still exists
            List<?> srcData = entityManager
                    .createNativeQuery(
                            "SELECT * FROM erefer_attachment_data WHERE erefer_attachment_id = :id")
                    .setParameter("id", oldAttachmentPk)
                    .getResultList();
            assertThat(srcData).hasSize(1);
        }
    }
}
