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


package ca.openosp.openo.prescript.pageUtil;

import ca.openosp.openo.commn.model.Allergy;

/**
 * Display model for allergy information in prescription views.
 *
 * This class serves as a data transfer object for presenting allergy
 * information in the prescription module's user interface. It provides
 * a simplified view of the Allergy entity with formatted display methods
 * for severity, onset, and type descriptions.
 *
 * The class handles both local and remote facility allergies, supporting
 * multi-facility integration through the CAISI system. It includes
 * archival status to distinguish between active and historical allergies.
 *
 * @since 2004-09-10
 */
public final class AllergyDisplay {
    /**
     * Unique identifier for the allergy.
     */
    private Integer id;

    /**
     * Remote facility ID for multi-facility integration.
     */
    private Integer remoteFacilityId;

    /**
     * Date when allergy was entered into the system.
     */
    private String entryDate;

    /**
     * Allergy description or allergen name.
     */
    private String description;

    /**
     * Type code for allergy classification.
     */
    private int typeCode;

    /**
     * Severity code (mild, moderate, severe).
     */
    private String severityCode;

    /**
     * Onset code indicating when reaction began.
     */
    private String onSetCode;

    /**
     * Description of allergic reaction.
     */
    private String reaction;

    /**
     * Date when allergy first occurred.
     */
    private String startDate;

    /**
     * Archive status ("1" if archived, null if active).
     */
    private String archived;

    /**
     * Gets the allergy ID.
     *
     * @return Integer unique identifier
     */
    public Integer getId() {
        return (id);
    }

    /**
     * Sets the allergy ID.
     *
     * @param id Integer unique identifier
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the remote facility ID.
     *
     * @return Integer facility identifier or null if local
     */
    public Integer getRemoteFacilityId() {
        return (remoteFacilityId);
    }

    /**
     * Sets the remote facility ID.
     *
     * @param remoteFacilityId Integer facility identifier
     */
    public void setRemoteFacilityId(Integer remoteFacilityId) {
        this.remoteFacilityId = remoteFacilityId;
    }

    /**
     * Gets the entry date.
     *
     * @return String date when allergy was recorded
     */
    public String getEntryDate() {
        return (entryDate);
    }

    /**
     * Sets the entry date.
     *
     * @param entryDate String date when allergy was recorded
     */
    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    /**
     * Gets the allergy description.
     *
     * @return String allergen name or description
     */
    public String getDescription() {
        return (description);
    }

    /**
     * Sets the allergy description.
     *
     * @param description String allergen name or description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the allergy type code.
     *
     * @return int type classification code
     */
    public int getTypeCode() {
        return (typeCode);
    }

    /**
     * Sets the allergy type code.
     *
     * @param typeCode int type classification code
     */
    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    /**
     * Gets the severity code.
     *
     * @return String severity level code
     */
    public String getSeverityCode() {
        return (severityCode);
    }

    /**
     * Sets the severity code.
     *
     * @param severityCode String severity level code
     */
    public void setSeverityCode(String severityCode) {
        this.severityCode = severityCode;
    }

    /**
     * Gets the onset code.
     *
     * @return String onset timing code
     */
    public String getOnSetCode() {
        return (onSetCode);
    }

    /**
     * Sets the onset code.
     *
     * @param onSetCode String onset timing code
     */
    public void setOnSetCode(String onSetCode) {
        this.onSetCode = onSetCode;
    }

    /**
     * Gets the reaction description.
     *
     * @return String allergic reaction details
     */
    public String getReaction() {
        return (reaction);
    }

    /**
     * Sets the reaction description.
     *
     * @param reaction String allergic reaction details
     */
    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    /**
     * Gets the allergy start date.
     *
     * @return String date when allergy first occurred
     */
    public String getStartDate() {
        return (startDate);
    }

    /**
     * Sets the allergy start date.
     *
     * @param startDate String date when allergy first occurred
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets human-readable onset description.
     *
     * Converts onset code to descriptive text using the
     * Allergy model's lookup method.
     *
     * @return String onset description
     */
    public String getOnSetDesc() {
        return (Allergy.getOnSetOfReactionDesc(onSetCode));
    }

    /**
     * Gets human-readable severity description.
     *
     * Converts severity code to descriptive text using the
     * Allergy model's lookup method.
     *
     * @return String severity description
     */
    public String getSeverityDesc() {
        return (Allergy.getSeverityOfReactionDesc(severityCode));
    }

    /**
     * Gets human-readable type description.
     *
     * Converts type code to descriptive text using the
     * Allergy model's lookup method.
     *
     * @return String allergy type description
     */
    public String getTypeDesc() {
        return (Allergy.getTypeDesc(typeCode));
    }

    /**
     * Gets the archive status.
     *
     * @return String "1" if archived, null if active
     */
    public String getArchived() {
        return archived;
    }

    /**
     * Sets the archive status.
     *
     * @param archived String "1" to archive, null for active
     */
    public void setArchived(String archived) {
        this.archived = archived;
    }

}
