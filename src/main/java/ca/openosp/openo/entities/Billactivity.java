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

package ca.openosp.openo.entities;

import java.util.Date;

/**
 * Healthcare billing activity entity representing provincial billing submission records and their processing status.
 * This entity encapsulates the data from the billactivity table, which tracks billing batch submissions
 * to provincial healthcare insurance providers (e.g., OHIP in Ontario, MSP in BC).
 *
 * Each Billactivity record represents a billing batch containing multiple claims that have been
 * formatted and prepared for submission to the provincial billing system. The entity tracks
 * both HTML formatted files for provider viewing and OHIP/provincial formatted files for
 * electronic submission.
 *
 * @see Billingmaster
 * @see Billingdetail
 * @see BillHistory
 * @since November 1, 2004
 */
public class Billactivity {
    /**
     * Month code identifying the billing period (format: YYYYMM)
     */
    private String monthCode;

    /**
     * Number of billing records in this batch
     */
    private int batchcount;

    /**
     * Filename of the HTML formatted report for provider viewing
     */
    private String htmlfilename;

    /**
     * Filename of the provincial billing system formatted file (e.g., OHIP format)
     */
    private String ohipfilename;

    /**
     * Provider's provincial health insurance number (e.g., OHIP number)
     */
    private String providerohipno;

    /**
     * Billing group number for multi-provider clinics
     */
    private String groupno;

    /**
     * Username of the user who created this billing batch
     */
    private String creator;

    /**
     * HTML content of the billing report
     */
    private String htmlcontext;

    /**
     * Provincial billing system formatted content (e.g., OHIP EDI format)
     */
    private String ohipcontext;

    /**
     * Claim record data in provincial format
     */
    private String claimrecord;

    /**
     * Date and time when this billing activity was last updated
     */
    private Date updatedatetime;

    /**
     * Status of the billing submission (SENT, NOTSENT, etc.)
     */
    private String status;

    /**
     * Total amount of all claims in this billing batch
     */
    private String total;

    /**
     * Unique identifier for this billing activity record
     */
    private int id;

    /**
     * Status constant indicating the billing batch has been sent to the provincial system
     */
    public static final String SENT = "S";

    /**
     * Status constant indicating the billing batch has not been sent (awaiting submission)
     */
    public static final String NOTSENT = "A";


    /**
     * Default constructor creating an empty Billactivity instance.
     * All fields will be initialized to their default values.
     */
    public Billactivity() {
    }

    /**
     * Full constructor creating a Billactivity instance with all field values.
     *
     * @param monthCode      String the billing period month code (format: YYYYMM)
     * @param batchcount     int the number of billing records in this batch
     * @param htmlfilename   String the filename of the HTML formatted report
     * @param ohipfilename   String the filename of the provincial billing file
     * @param providerohipno String the provider's provincial health insurance number
     * @param groupno        String the billing group number
     * @param creator        String the username of the batch creator
     * @param htmlcontext    String the HTML content of the billing report
     * @param ohipcontext    String the provincial billing system formatted content
     * @param claimrecord    String the claim record data in provincial format
     * @param updatedatetime Date the date and time of last update
     * @param status         String the submission status (SENT, NOTSENT)
     * @param total          String the total amount of all claims in the batch
     */
    public Billactivity(String monthCode, int batchcount, String htmlfilename,
                        String ohipfilename, String providerohipno,
                        String groupno, String creator, String htmlcontext,
                        String ohipcontext, String claimrecord,
                        Date updatedatetime, String status, String total) {
        this.setMonthCode(monthCode);
        this.setBatchcount(batchcount);
        this.setHtmlfilename(htmlfilename);
        this.setOhipfilename(ohipfilename);
        this.setProviderohipno(providerohipno);
        this.setGroupno(groupno);
        this.setCreator(creator);
        this.setHtmlcontext(htmlcontext);
        this.setOhipcontext(ohipcontext);
        this.setClaimrecord(claimrecord);
        this.setUpdatedatetime(updatedatetime);
        this.setStatus(status);
        this.setTotal(total);
    }

    /**
     * Gets the billing period month code.
     *
     * @return String the month code in YYYYMM format, never null (empty string if null)
     */
    public String getMonthCode() {
        return (monthCode != null ? monthCode : "");
    }

    /**
     * Gets the number of billing records in this batch.
     *
     * @return int the count of billing records included in this submission batch
     */
    public int getBatchcount() {
        return batchcount;
    }

    /**
     * Gets the filename of the HTML formatted billing report.
     *
     * @return String the HTML report filename, never null (empty string if null)
     */
    public String getHtmlfilename() {
        return (htmlfilename != null ? htmlfilename : "");
    }

    /**
     * Gets the filename of the provincial billing system formatted file.
     *
     * @return String the provincial billing file name (e.g., OHIP format), never null (empty string if null)
     */
    public String getOhipfilename() {
        return (ohipfilename != null ? ohipfilename : "");
    }

    /**
     * Gets the provider's provincial health insurance number.
     *
     * @return String the provider's provincial number (e.g., OHIP number), never null (empty string if null)
     */
    public String getProviderohipno() {
        return (providerohipno != null ? providerohipno : "");
    }

    /**
     * Gets the billing group number for multi-provider clinics.
     *
     * @return String the billing group identifier, never null (empty string if null)
     */
    public String getGroupno() {
        return (groupno != null ? groupno : "");
    }

    /**
     * Gets the username of the user who created this billing batch.
     *
     * @return String the creator's username, never null (empty string if null)
     */
    public String getCreator() {
        return (creator != null ? creator : "");
    }

    /**
     * Gets the HTML content of the billing report.
     *
     * @return String the full HTML content for provider viewing, never null (empty string if null)
     */
    public String getHtmlcontext() {
        return (htmlcontext != null ? htmlcontext : "");
    }

    /**
     * Gets the provincial billing system formatted content.
     *
     * @return String the formatted content for provincial submission (e.g., OHIP EDI format), never null (empty string if null)
     */
    public String getOhipcontext() {
        return (ohipcontext != null ? ohipcontext : "");
    }

    /**
     * Gets the claim record data in provincial format.
     *
     * @return String the claim record data formatted for provincial billing system, never null (empty string if null)
     */
    public String getClaimrecord() {
        return (claimrecord != null ? claimrecord : "");
    }

    /**
     * Gets the date and time when this billing activity was last updated.
     *
     * @return Date the timestamp of the last update, may be null
     */
    public Date getUpdatedatetime() {
        return updatedatetime;
    }

    /**
     * Gets the submission status of this billing batch.
     *
     * @return String the status (SENT, NOTSENT), never null (empty string if null)
     * @see #SENT
     * @see #NOTSENT
     */
    public String getStatus() {
        return (status != null ? status : "");
    }

    /**
     * Gets the total amount of all claims in this billing batch.
     *
     * @return String the monetary total as a string, never null (empty string if null)
     */
    public String getTotal() {
        return (total != null ? total : "");
    }

    /**
     * Sets the billing period month code.
     *
     * @param monthCode String the month code in YYYYMM format
     */
    public void setMonthCode(String monthCode) {
        this.monthCode = monthCode;
    }

    /**
     * Sets the number of billing records in this batch.
     *
     * @param batchcount int the count of billing records in this submission batch
     */
    public void setBatchcount(int batchcount) {
        this.batchcount = batchcount;
    }

    /**
     * Sets the filename of the HTML formatted billing report.
     *
     * @param htmlfilename String the HTML report filename
     */
    public void setHtmlfilename(String htmlfilename) {
        this.htmlfilename = htmlfilename;
    }

    /**
     * Sets the filename of the provincial billing system formatted file.
     *
     * @param ohipfilename String the provincial billing file name (e.g., OHIP format)
     */
    public void setOhipfilename(String ohipfilename) {
        this.ohipfilename = ohipfilename;
    }

    /**
     * Sets the provider's provincial health insurance number.
     *
     * @param providerohipno String the provider's provincial number (e.g., OHIP number)
     */
    public void setProviderohipno(String providerohipno) {
        this.providerohipno = providerohipno;
    }

    /**
     * Sets the billing group number for multi-provider clinics.
     *
     * @param groupno String the billing group identifier
     */
    public void setGroupno(String groupno) {
        this.groupno = groupno;
    }

    /**
     * Sets the username of the user who created this billing batch.
     *
     * @param creator String the creator's username
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Sets the HTML content of the billing report.
     *
     * @param htmlcontext String the full HTML content for provider viewing
     */
    public void setHtmlcontext(String htmlcontext) {
        this.htmlcontext = htmlcontext;
    }

    /**
     * Sets the provincial billing system formatted content.
     *
     * @param ohipcontext String the formatted content for provincial submission (e.g., OHIP EDI format)
     */
    public void setOhipcontext(String ohipcontext) {
        this.ohipcontext = ohipcontext;
    }

    /**
     * Sets the claim record data in provincial format.
     *
     * @param claimrecord String the claim record data formatted for provincial billing system
     */
    public void setClaimrecord(String claimrecord) {
        this.claimrecord = claimrecord;
    }

    /**
     * Sets the date and time when this billing activity was last updated.
     *
     * @param updatedatetime Date the timestamp of the last update
     */
    public void setUpdatedatetime(Date updatedatetime) {
        this.updatedatetime = updatedatetime;
    }


    /**
     * Sets the submission status of this billing batch.
     *
     * @param status String the status (SENT, NOTSENT)
     * @see #SENT
     * @see #NOTSENT
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets the total amount of all claims in this billing batch.
     *
     * @param total String the monetary total as a string
     */
    public void setTotal(String total) {
        this.total = total;
    }

    /**
     * Gets the unique identifier for this billing activity record.
     *
     * @return int the unique billing activity ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this billing activity record.
     *
     * @param id int the unique billing activity ID
     */
    public void setId(int id) {
        this.id = id;
    }

}
