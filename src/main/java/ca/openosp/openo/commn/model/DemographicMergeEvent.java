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
package ca.openosp.openo.commn.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single merge or unmerge event in the demographic merge audit log.
 * <p>
 * One row is inserted per merge or unmerge operation. The unmerge path reads this
 * table to identify the primary and all secondary demographics to reactivate.
 *
 * @since 2026-03-19
 */
@Entity
@Table(name = "demographic_merge_event")
public class DemographicMergeEvent extends AbstractModel<Integer> {

    /**
     * Discriminator for the {@code event_type} column.
     * Stored as a VARCHAR using the enum name (e.g. {@code "MERGE"}, {@code "UNMERGE"}).
     */
    public enum EventType {
        MERGE,
        UNMERGE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "primary_demographic_no")
    private int primaryDemographicNo;

    @Column(name = "secondary_demographic_no")
    private String secondaryDemographicNo;

    @Column(name = "merged_demographic_no")
    private int mergedDemographicNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    @Column(name = "provider_no")
    private String providerNo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "event_date")
    private Date eventDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getPrimaryDemographicNo() {
        return primaryDemographicNo;
    }

    public void setPrimaryDemographicNo(int primaryDemographicNo) {
        this.primaryDemographicNo = primaryDemographicNo;
    }

    public String getSecondaryDemographicNo() {
        return secondaryDemographicNo;
    }

    public void setSecondaryDemographicNo(String secondaryDemographicNo) {
        this.secondaryDemographicNo = secondaryDemographicNo;
    }

    /**
     * Parses the comma-separated secondary demographic IDs into a list of integers.
     * Returns an empty list if the field is null or blank.
     *
     * @return List&lt;Integer&gt; list of secondary demographic numbers
     */
    public List<Integer> getSecondaryDemographicNos() {
        if (secondaryDemographicNo == null || secondaryDemographicNo.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(secondaryDemographicNo.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public int getMergedDemographicNo() {
        return mergedDemographicNo;
    }

    public void setMergedDemographicNo(int mergedDemographicNo) {
        this.mergedDemographicNo = mergedDemographicNo;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getProviderNo() {
        return providerNo;
    }

    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
}
