//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 */
package ca.openosp.openo.demographic.merge.model;

import javax.persistence.*;
import java.util.Date;

/**
 * JPA entity for the {@code casemgmt_note_ext} table.
 * Stores extended key-value attributes for case management notes.
 *
 * @since 2026-03-19
 */
@Entity
@Table(name = "casemgmt_note_ext")
public class CaseMgmtNoteExt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "note_id", nullable = false)
    private Integer noteId;

    @Column(name = "key_val", nullable = false)
    private String keyVal;

    @Lob
    @Column(name = "value")
    private String value;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_value")
    private Date dateValue;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getNoteId() { return noteId; }
    public void setNoteId(Integer noteId) { this.noteId = noteId; }

    public String getKeyVal() { return keyVal; }
    public void setKeyVal(String keyVal) { this.keyVal = keyVal; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Date getDateValue() { return dateValue; }
    public void setDateValue(Date dateValue) { this.dateValue = dateValue; }
}
