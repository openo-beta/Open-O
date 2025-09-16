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

import ca.openosp.openo.util.UtilMisc;

/**
 * Healthcare billing history entity representing audit trail records for billing modifications.
 * This entity maintains an archive of all modification events performed on specific billing records
 * (BillingMaster entries), providing a complete audit trail for billing activities.
 *
 * Each BillHistory record captures the state of a billing record at the time of modification,
 * including the practitioner who made the change, the billing status, amounts, and payment details.
 * This ensures regulatory compliance and enables tracking of all billing-related activities.
 *
 * @see Billingmaster
 * @see Billactivity
 * @since November 1, 2004
 */
public class BillHistory {

    /**
     * Unique identifier for this billing history record
     */
    private int id;

    /**
     * Reference to the billing master record that was modified
     */
    private int billingMasterNo;

    /**
     * Practitioner number of the provider who made the billing modification
     */
    private String practitioner_no = "";

    /**
     * Status of the billing record at the time of the modification
     */
    private String billingStatus = "";

    /**
     * Date and time when this archive entry was created
     */
    private Date archiveDate;

    /**
     * Type of billing (e.g., OHIP, private, WCB)
     */
    private String billingtype = "";

    /**
     * Sequence number for the billing record
     */
    private String seqNum = "";

    /**
     * Monetary amount of the billing claim
     */
    private double amount;

    /**
     * Amount received/paid for this billing claim
     */
    private double amountReceived;

    /**
     * Identifier for the payment type
     */
    private String paymentTypeId;

    /**
     * Descriptive text for the payment type
     */
    private String paymentTypeDesc;

    /**
     * Default constructor creating an empty BillHistory instance.
     * All fields will be initialized to their default values.
     */
    public BillHistory() {
    }

    /**
     * Sets the unique identifier for this billing history record.
     *
     * @param id int the unique billing history ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the reference to the billing master record that was modified.
     *
     * @param billingMasterNo int the billing master record number being tracked
     */
    public void setBillingMasterNo(int billingMasterNo) {
        this.billingMasterNo = billingMasterNo;
    }

    /**
     * Sets the practitioner number of the provider who made the billing modification.
     *
     * @param practitioner_no String the provider number responsible for the modification
     */
    public void setPractitioner_no(String practitioner_no) {

        this.practitioner_no = practitioner_no;
    }

    /**
     * Sets the status of the billing record at the time of the modification.
     *
     * @param billingStatus String the billing status at the time of archiving
     */
    public void setBillingStatus(String billingStatus) {
        this.billingStatus = billingStatus;
    }

    /**
     * Sets the date and time when this archive entry was created.
     *
     * @param archiveDate Date the timestamp when this billing modification occurred
     */
    public void setArchiveDate(Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    /**
     * Sets the type of billing for this record.
     *
     * @param billingtype String the billing type (e.g., OHIP, private, WCB)
     */
    public void setBillingtype(String billingtype) {
        this.billingtype = billingtype;
    }

    /**
     * Sets the sequence number for the billing record.
     *
     * @param seqNum String the sequence number
     */
    public void setSeqNum(String seqNum) {
        this.seqNum = seqNum;
    }

    /**
     * Sets the monetary amount of the billing claim.
     * The amount is automatically formatted to currency precision.
     *
     * @param amount double the billing claim amount
     */
    public void setAmount(double amount) {
        this.amount = UtilMisc.toCurrencyDouble(amount);
    }

    /**
     * Sets the amount received/paid for this billing claim.
     * The amount is automatically formatted to currency precision.
     *
     * @param amountReceived double the amount received or paid
     */
    public void setAmountReceived(double amountReceived) {
        this.amountReceived = UtilMisc.toCurrencyDouble(amountReceived);
    }

    /**
     * Sets the identifier for the payment type.
     *
     * @param paymentTypeId String the payment type identifier
     */
    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    /**
     * Sets the descriptive text for the payment type.
     *
     * @param paymentTypeDesc String the payment type description
     */
    public void setPaymentTypeDesc(String paymentTypeDesc) {
        this.paymentTypeDesc = paymentTypeDesc;
    }

    /**
     * Gets the unique identifier for this billing history record.
     *
     * @return int the unique billing history ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the reference to the billing master record that was modified.
     *
     * @return int the billing master record number being tracked
     */
    public int getBillingMasterNo() {
        return billingMasterNo;
    }

    /**
     * Gets the practitioner number of the provider who made the billing modification.
     *
     * @return String the provider number responsible for the modification
     */
    public String getPractitioner_no() {
        return practitioner_no;
    }

    /**
     * Gets the status of the billing record at the time of the modification.
     *
     * @return String the billing status at the time of archiving
     */
    public String getBillingStatus() {
        return billingStatus;
    }

    /**
     * Gets the date and time when this archive entry was created.
     *
     * @return Date the timestamp when this billing modification occurred
     */
    public Date getArchiveDate() {
        return archiveDate;
    }

    /**
     * Gets the type of billing for this record.
     *
     * @return String the billing type (e.g., OHIP, private, WCB)
     */
    public String getBillingtype() {
        return billingtype;
    }

    /**
     * Gets the sequence number for the billing record.
     *
     * @return String the sequence number
     */
    public String getSeqNum() {
        return seqNum;
    }

    /**
     * Gets the monetary amount of the billing claim.
     *
     * @return double the billing claim amount formatted to currency precision
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Gets the amount received/paid for this billing claim.
     *
     * @return double the amount received or paid formatted to currency precision
     */
    public double getAmountReceived() {
        return amountReceived;
    }

    /**
     * Gets the identifier for the payment type.
     *
     * @return String the payment type identifier
     */
    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    /**
     * Gets the descriptive text for the payment type.
     *
     * @return String the payment type description
     */
    public String getPaymentTypeDesc() {
        return paymentTypeDesc;
    }

}
