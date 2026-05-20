//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 */
package ca.openosp.openo.commn.model;

import javax.persistence.*;
import java.util.Date;

/**
 * JPA entity for the {@code casemgmt_issue} table.
 * Represents a patient's medical issue/problem in case management.
 *
 * @since 2026-03-19
 */
@Entity
@Table(name = "casemgmt_issue")
public class CaseMgmtIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "demographic_no", nullable = false)
    private Integer demographicNo;

    @Column(name = "issue_id", nullable = false)
    private Integer issueId;

    @Column(name = "acute", nullable = false)
    private boolean acute;

    @Column(name = "certain", nullable = false)
    private boolean certain;

    @Column(name = "major", nullable = false)
    private boolean major;

    @Column(name = "resolved", nullable = false)
    private boolean resolved;

    @Column(name = "program_id")
    private Integer programId;

    @Column(name = "type", nullable = false)
    private String type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_date", nullable = false)
    private Date updateDate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getDemographicNo() { return demographicNo; }
    public void setDemographicNo(Integer demographicNo) { this.demographicNo = demographicNo; }

    public Integer getIssueId() { return issueId; }
    public void setIssueId(Integer issueId) { this.issueId = issueId; }

    public boolean isAcute() { return acute; }
    public void setAcute(boolean acute) { this.acute = acute; }

    public boolean isCertain() { return certain; }
    public void setCertain(boolean certain) { this.certain = certain; }

    public boolean isMajor() { return major; }
    public void setMajor(boolean major) { this.major = major; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public Integer getProgramId() { return programId; }
    public void setProgramId(Integer programId) { this.programId = programId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getUpdateDate() { return updateDate; }
    public void setUpdateDate(Date updateDate) { this.updateDate = updateDate; }
}
