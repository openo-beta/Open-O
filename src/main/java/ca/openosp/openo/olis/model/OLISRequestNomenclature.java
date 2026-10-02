//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.olis.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ca.openosp.openo.commn.model.AbstractModel;

@Entity
public class OLISRequestNomenclature extends AbstractModel<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nameId;
    private String name;
    private String category;

    @Temporal(TemporalType.DATE)
    private Date effectiveDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(nullable = false)
    private String status = "ACTIVE";

    private String externalCodeVersion;
    private String successorCode;
    private String sortKey;

    public OLISRequestNomenclature() {
        super();
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return Date the date this request code becomes effective in OLIS, or {@code null} if unspecified
     * @since 2026-05-15
     */
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * @param effectiveDate Date the date this request code becomes effective in OLIS
     * @since 2026-05-15
     */
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * @return Date the date this request code is retired, or {@code null} if it has no end date
     * @since 2026-05-15
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate Date the date this request code is retired ({@code null} for no end date)
     * @since 2026-05-15
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return String the lifecycle status, e.g. {@code "ACTIVE"} or {@code "INACTIVE"}
     * @since 2026-05-15
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status String the lifecycle status, e.g. {@code "ACTIVE"} or {@code "INACTIVE"}
     * @since 2026-05-15
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return String the version identifier of the external code set this entry belongs to
     * @since 2026-05-15
     */
    public String getExternalCodeVersion() {
        return externalCodeVersion;
    }

    /**
     * @param externalCodeVersion String the version identifier of the external code set
     * @since 2026-05-15
     */
    public void setExternalCodeVersion(String externalCodeVersion) {
        this.externalCodeVersion = externalCodeVersion;
    }

    /**
     * @return String the code that supersedes this one when it is deprecated, or {@code null} if none
     * @since 2026-05-15
     */
    public String getSuccessorCode() {
        return successorCode;
    }

    /**
     * @param successorCode String the code that supersedes this one when it is deprecated
     * @since 2026-05-15
     */
    public void setSuccessorCode(String successorCode) {
        this.successorCode = successorCode;
    }

    /**
     * @return String the OLIS catalog request sort key (from the "Sort Key" column),
     *         used as a fallback ordering key when a request carries no ZBR.11 sort
     *         key; {@code null} if the catalog row predates the sort-key column
     * @since 2026-06-17
     */
    public String getSortKey() {
        return sortKey;
    }

    /**
     * @param sortKey String the OLIS catalog request sort key
     * @since 2026-06-17
     */
    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

}
