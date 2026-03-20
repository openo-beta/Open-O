//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 */
package ca.openosp.openo.demographic.merge.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for {@link CaseMgmtIssueNotes}.
 *
 * @since 2026-03-19
 */
public class CaseMgmtIssueNotesPK implements Serializable {

    private Integer id;
    private Integer noteId;

    public CaseMgmtIssueNotesPK() {}

    public CaseMgmtIssueNotesPK(Integer id, Integer noteId) {
        this.id = id;
        this.noteId = noteId;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getNoteId() { return noteId; }
    public void setNoteId(Integer noteId) { this.noteId = noteId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CaseMgmtIssueNotesPK)) return false;
        CaseMgmtIssueNotesPK that = (CaseMgmtIssueNotesPK) o;
        return Objects.equals(id, that.id) && Objects.equals(noteId, that.noteId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, noteId);
    }
}
