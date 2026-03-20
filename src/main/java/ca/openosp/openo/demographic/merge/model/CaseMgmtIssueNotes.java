//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 */
package ca.openosp.openo.demographic.merge.model;

import javax.persistence.*;

/**
 * JPA entity for the {@code casemgmt_issue_notes} junction table.
 * Links case management issues to their notes. Has no auto-increment PK —
 * the composite ({@code id}, {@code noteId}) uniquely identifies each row.
 * <p>
 * The {@code id} column references {@code casemgmt_issue.id} and
 * {@code noteId} references {@code casemgmt_note.note_id}. Both must be
 * remapped when copying (see {@code copyIssueNotesGroup()} in the merge DAO).
 *
 * @since 2026-03-19
 */
@Entity
@Table(name = "casemgmt_issue_notes")
@IdClass(CaseMgmtIssueNotesPK.class)
public class CaseMgmtIssueNotes {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Id
    @Column(name = "note_id", nullable = false)
    private Integer noteId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getNoteId() { return noteId; }
    public void setNoteId(Integer noteId) { this.noteId = noteId; }
}
