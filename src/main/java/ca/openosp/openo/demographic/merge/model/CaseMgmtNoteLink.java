//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 */
package ca.openosp.openo.demographic.merge.model;

import javax.persistence.*;

/**
 * JPA entity for the {@code casemgmt_note_link} table.
 * Links case management notes to other entities.
 *
 * @since 2026-03-19
 */
@Entity
@Table(name = "casemgmt_note_link")
public class CaseMgmtNoteLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "table_name", nullable = false)
    private Integer tableName;

    @Column(name = "table_id", nullable = false)
    private Integer tableId;

    @Column(name = "note_id", nullable = false)
    private Integer noteId;

    @Column(name = "other_id")
    private String otherId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTableName() { return tableName; }
    public void setTableName(Integer tableName) { this.tableName = tableName; }

    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }

    public Integer getNoteId() { return noteId; }
    public void setNoteId(Integer noteId) { this.noteId = noteId; }

    public String getOtherId() { return otherId; }
    public void setOtherId(String otherId) { this.otherId = otherId; }
}
