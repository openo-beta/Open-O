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
public class OLISResultNomenclature extends AbstractModel<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String name;
    private String nameId;

    @Temporal(TemporalType.DATE)
    private Date effectiveDate;

    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(nullable = false)
    private String status = "ACTIVE";

    private String externalCodeVersion;
    private String successorCode;

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Date the date this result code becomes effective in OLIS, or {@code null} if unspecified
     * @since 2026-05-15
     */
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * @param effectiveDate Date the date this result code becomes effective in OLIS
     * @since 2026-05-15
     */
    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * @return Date the date this result code is retired, or {@code null} if it has no end date
     * @since 2026-05-15
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate Date the date this result code is retired ({@code null} for no end date)
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

    public OLISResultNomenclature() {
        super();
    }
}
