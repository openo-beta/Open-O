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

/**
 * Healthcare billing detail entity representing individual line items within billing transactions.
 * This entity encapsulates the data from the billingdetail table, which stores detailed information
 * about specific healthcare services billed to provincial insurance systems or private payers.
 *
 * Each Billingdetail record represents a single service or procedure that was performed,
 * including the service code, description, amount, diagnostic codes, and billing status.
 * These details are associated with a parent billing record and are used to generate
 * claims for submission to insurance providers.
 *
 * @see Billingmaster
 * @see Billactivity
 * @see BillHistory
 * @since November 1, 2004
 */
public class Billingdetail {
    /**
     * Auto-increment unique identifier for this billing detail record
     */
    private int billingDtNo;

    /**
     * Reference to the parent billing record (billingmaster)
     */
    private int billingNo;

    /**
     * Provincial service code identifying the healthcare service performed
     */
    private String serviceCode;

    /**
     * Descriptive text of the healthcare service provided
     */
    private String serviceDesc;

    /**
     * Monetary amount to be billed for this service
     */
    private String billingAmount;

    /**
     * Diagnostic code (ICD-9/ICD-10) associated with this service
     */
    private String diagnosticCode;

    /**
     * Date when the appointment/service was provided
     */
    private String appointmentDate;

    /**
     * Current status of this billing detail (e.g., submitted, paid, rejected)
     */
    private String status;

    /**
     * Billing units or quantity for the service provided
     */
    private String billingunit;

    /**
     * Default constructor creating an empty Billingdetail instance.
     * All fields will be initialized to their default values.
     */
    public Billingdetail() {
    }

    /**
     * Full constructor creating a Billingdetail instance with all field values.
     *
     * @param billingDtNo     int the unique billing detail ID
     * @param billingNo       int the parent billing record ID
     * @param serviceCode     String the provincial service code
     * @param serviceDesc     String the healthcare service description
     * @param billingAmount   String the monetary amount for this service
     * @param diagnosticCode  String the diagnostic code (ICD-9/ICD-10)
     * @param appointmentDate String the date when service was provided
     * @param status          String the billing status
     * @param billingunit     String the billing units or quantity
     */
    public Billingdetail(int billingDtNo, int billingNo, String serviceCode,
                         String serviceDesc, String billingAmount,
                         String diagnosticCode, String appointmentDate,
                         String status, String billingunit) {
        this.billingDtNo = billingDtNo;
        this.billingNo = billingNo;
        this.serviceCode = serviceCode;
        this.serviceDesc = serviceDesc;
        this.billingAmount = billingAmount;
        this.diagnosticCode = diagnosticCode;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.billingunit = billingunit;
    }

    /**
     * Gets the unique identifier for this billing detail record.
     *
     * @return int the auto-increment billing detail number
     */
    public int getBillingDtNo() {
        return billingDtNo;
    }

    /**
     * Gets the reference to the parent billing record.
     *
     * @return int the billing master record number
     */
    public int getBillingNo() {
        return billingNo;
    }

    /**
     * Gets the provincial service code identifying the healthcare service.
     *
     * @return String the service code, never null (empty string if null)
     */
    public String getServiceCode() {
        return (serviceCode != null ? serviceCode : "");
    }

    /**
     * Gets the descriptive text of the healthcare service provided.
     *
     * @return String the service description, never null (empty string if null)
     */
    public String getServiceDesc() {
        return (serviceDesc != null ? serviceDesc : "");
    }

    /**
     * Gets the monetary amount to be billed for this service.
     *
     * @return String the billing amount as a string, never null (empty string if null)
     */
    public String getBillingAmount() {
        return (billingAmount != null ? billingAmount : "");
    }

    /**
     * Gets the diagnostic code associated with this service.
     *
     * @return String the diagnostic code (ICD-9/ICD-10), never null (empty string if null)
     */
    public String getDiagnosticCode() {
        return (diagnosticCode != null ? diagnosticCode : "");
    }

    /**
     * Gets the date when the appointment/service was provided.
     *
     * @return String the appointment date, may be null
     */
    public String getAppointmentDate() {
        return appointmentDate;
    }

    /**
     * Gets the current status of this billing detail.
     *
     * @return String the billing status (e.g., submitted, paid, rejected), never null (empty string if null)
     */
    public String getStatus() {
        return (status != null ? status : "");
    }

    /**
     * Gets the billing units or quantity for the service provided.
     *
     * @return String the billing units, never null (empty string if null)
     */
    public String getBillingunit() {
        return (billingunit != null ? billingunit : "");
    }

    /**
     * Sets the unique identifier for this billing detail record.
     *
     * @param billingDtNo int the auto-increment billing detail number
     */
    public void setBillingDtNo(int billingDtNo) {
        this.billingDtNo = billingDtNo;
    }

    /**
     * Sets the reference to the parent billing record.
     *
     * @param billingNo int the billing master record number
     */
    public void setBillingNo(int billingNo) {
        this.billingNo = billingNo;
    }

    /**
     * Sets the provincial service code identifying the healthcare service.
     *
     * @param serviceCode String the service code
     */
    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    /**
     * Sets the descriptive text of the healthcare service provided.
     *
     * @param serviceDesc String the service description
     */
    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    /**
     * Sets the monetary amount to be billed for this service.
     *
     * @param billingAmount String the billing amount as a string
     */
    public void setBillingAmount(String billingAmount) {
        this.billingAmount = billingAmount;
    }

    /**
     * Sets the diagnostic code associated with this service.
     *
     * @param diagnosticCode String the diagnostic code (ICD-9/ICD-10)
     */
    public void setDiagnosticCode(String diagnosticCode) {
        this.diagnosticCode = diagnosticCode;
    }

    /**
     * Sets the date when the appointment/service was provided.
     *
     * @param appointmentDate String the appointment date
     */
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    /**
     * Sets the current status of this billing detail.
     *
     * @param status String the billing status (e.g., submitted, paid, rejected)
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets the billing units or quantity for the service provided.
     *
     * @param billingunit String the billing units
     */
    public void setBillingunit(String billingunit) {
        this.billingunit = billingunit;
    }
}
