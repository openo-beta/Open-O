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

import java.util.Map;

/**
 * Copy engine for the demographic merge operation.
 * <p>
 * All methods operate directly at the database level. Source rows are never modified.
 * Rows are copied by inserting new records with updated demographic references.
 *
 * @since 2026-03-19
 */
public interface DemographicMergeOperationDao {

    /**
     * Copies all rows for {@code sourceDemographicNo} in the given table into new rows
     * for {@code targetDemographicNo}. The primary key is nulled so the database assigns
     * new auto-generated PKs. Returns a map of old PK to new PK for use by derived table copies.
     * <p>
     * Performs an {@code information_schema} table-exists check first; returns an empty map
     * silently if the table does not exist in this installation.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @param tableIndex          DemographicTableIndex descriptor for the table to copy
     * @return Map&lt;Long, Long&gt; mapping of old primary key to new primary key for copied rows
     */
    Map<Long, Long> copyDemographic(Integer sourceDemographicNo, Integer targetDemographicNo, DemographicTableIndex tableIndex);

    /**
     * Copies derived rows from a child table by remapping foreign keys using the provided parent PK map.
     * <p>
     * Performs an {@code information_schema} table-exists check first; skips silently if absent.
     *
     * @param parentPkMap              Map&lt;Long, Long&gt; mapping of old parent PK to new parent PK
     * @param childTableName           String the child table name
     * @param fkColumn                 String the foreign key column in the child table
     * @param childPkColumn            String the primary key column in the child table
     * @param isChildPkAutoIncrement   boolean true to null out the child PK (auto-increment); false to set it explicitly to the new parent PK
     * @param extraDemoColumn          String optional extra demographic column to update; pass null if not needed
     * @param targetDemographicNo      Integer the target demographic number for the extra demo column; pass null if not needed
     */
    void copyDerivedRows(Map<Long, Long> parentPkMap, String childTableName, String fkColumn, String childPkColumn, boolean isChildPkAutoIncrement, String extraDemoColumn, Integer targetDemographicNo);

    /**
     * Copies {@code casemgmt_issue} rows with deduplication.
     * <p>
     * Skips rows where the target already has the same issue code. Returns old-to-new PK map
     * for use by {@link #copyCaseMgmtIssueNotes}.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @return Map&lt;Long, Long&gt; mapping of old issue id to new issue id
     */
    Map<Long, Long> copyDemographicForCaseMgmtIssue(Integer sourceDemographicNo, Integer targetDemographicNo);

    /**
     * Copies {@code casemgmt_issue_notes} rows remapping both FK columns simultaneously.
     * <p>
     * Performs an {@code information_schema} table-exists check first; skips silently if absent.
     *
     * @param issuePkMap Map&lt;Long, Long&gt; mapping of old casemgmt_issue id to new casemgmt_issue id
     * @param notePkMap  Map&lt;Long, Long&gt; mapping of old casemgmt_note note_id to new note_id
     */
    void copyCaseMgmtIssueNotes(Map<Long, Long> issuePkMap, Map<Long, Long> notePkMap);

    /**
     * Copies {@code consultationRequestExtArchive} rows remapping both FK columns simultaneously.
     * <p>
     * Performs an {@code information_schema} table-exists check first; skips silently if absent.
     *
     * @param archivePkMap  Map&lt;Long, Long&gt; mapping of old consultationRequestsArchive id to new id
     * @param requestPkMap  Map&lt;Long, Long&gt; mapping of old consultationRequests requestId to new requestId
     */
    void copyConsultationRequestExtArchive(Map<Long, Long> archivePkMap, Map<Long, Long> requestPkMap);

    /**
     * Copies {@code erefer_attachment_data} rows using the provided attachment PK map.
     * Handles the composite PK {@code (erefer_attachment_id, lab_id, lab_type)}.
     * <p>
     * Performs an {@code information_schema} table-exists check first; skips silently if absent.
     *
     * @param pkMap Map&lt;Long, Long&gt; mapping of old erefer_attachment id to new id
     */
    void copyErereferAttachmentData(Map<Long, Long> pkMap);

    /**
     * Copies {@code form_boolean_value} rows scoped to a given form table using the provided PK map.
     * <p>
     * Performs an {@code information_schema} table-exists check first; skips silently if absent.
     *
     * @param formName String the form table name used as the form_name discriminator (e.g. "formRourke2017")
     * @param pkMap    Map&lt;Long, Long&gt; mapping of old form row id to new form row id
     */
    void copyFormBooleanValues(String formName, Map<Long, Long> pkMap);

    /**
     * Copies identity tables (demographicExt, demographiccust, other_id, and their archives).
     * <p>
     * When {@code isSecondary} is false (A pass), all rows are copied from source to target.
     * When {@code isSecondary} is true (secondary pass), only gap-fill / deduplication logic applies.
     *
     * @param sourceDemographicNo Integer the source patient demographic number
     * @param targetDemographicNo Integer the target patient demographic number
     * @param isSecondary         boolean false for the primary (A) pass; true for each secondary pass
     */
    void copyIdentityTables(Integer sourceDemographicNo, Integer targetDemographicNo, boolean isSecondary);
}
