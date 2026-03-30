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


package ca.openosp.openo.commn.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "relationships")
public class Relationships extends AbstractModel<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "facility_id")
    private Integer facilityId;

    @Column(name = "demographic_no")
    private int demographicNo;

    @Column(name = "relation_demographic_no")
    private int relationDemographicNo;

    private String relation;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name = "sub_decision_maker")
    private String subDecisionMaker;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    private String notes;

    // TEMP FIX: Changed from primitive boolean to Boolean wrapper to handle legacy NULL values in the
    // 'deleted' DB column. Hibernate uses field-level access and cannot assign NULL to a primitive boolean,
    // causing a PropertyAccessException during entity load in DemographicMergeOperationDaoImpl.
    // TODO: Revert this back to primitive boolean once the root cause is fixed.
    // TODO: Add a NOT NULL DEFAULT 0 constraint on the 'deleted' column (via a DB migration) so that
    //       NULL values can no longer be inserted, then revert this field back to primitive boolean.
    private Boolean deleted;

    private String creator;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public int getDemographicNo() {
        return demographicNo;
    }

    public void setDemographicNo(int demographicNo) {
        this.demographicNo = demographicNo;
    }

    public int getRelationDemographicNo() {
        return relationDemographicNo;
    }

    public void setRelationDemographicNo(int relationDemographicNo) {
        this.relationDemographicNo = relationDemographicNo;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getSubDecisionMaker() {
        return subDecisionMaker;
    }

    public void setSubDecisionMaker(String subDecisionMaker) {
        this.subDecisionMaker = subDecisionMaker;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }


}
