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
 * S21 entity representing Teleplan payment summary records for British Columbia MSP
 * (Medical Services Plan) billing system. This entity captures the main payment
 * summary information from Teleplan response files, providing essential financial
 * reconciliation data for healthcare providers billing through BC's provincial
 * healthcare system.
 *
 * <p>Teleplan is BC's electronic billing and payment system that processes medical
 * service claims submitted by healthcare providers. The S21 record type contains
 * high-level payment summary information including total amounts billed, amounts
 * paid, outstanding balances, and cheque details for provider remittance.
 *
 * <p>Key Teleplan S21 features include:
 * <ul>
 * <li>Payment summary data linking to detailed S22 and S23 records
 * <li>MSP control number tracking for payment reconciliation
 * <li>Provider payee information and remittance details
 * <li>Financial totals including billed amounts, payments, and balances
 * <li>Data centre and sequence tracking for file processing integrity
 * <li>Status tracking for payment processing workflow
 * </ul>
 *
 * <p>The S21 record serves as the header record in the Teleplan hierarchy:
 * <ul>
 * <li><strong>S21:</strong> Payment summary (this entity)
 * <li><strong>S22:</strong> Provider-level detail records
 * <li><strong>S23:</strong> Individual claim adjustment details
 * </ul>
 *
 * <p>This entity is critical for BC healthcare providers to reconcile payments
 * received from MSP against claims submitted, ensuring accurate financial
 * management and identifying payment discrepancies or adjustments.
 *
 * @see ca.openosp.openo.entities.S22
 * @see ca.openosp.openo.entities.S23
 * @since November 1, 2004
 */
public class S21 {

    private String s21Id;
    private String fileName;
    private String dataCentre;
    private String dataSeq;
    private String paymentDate;
    private String lineCode;
    private String payeeNo;
    private String mspCTLno;
    private String payeeName;
    private String amtBilled;
    private String amtPaid;
    private String balanceFwd;
    private String cheque;
    private String newBalance;
    private String filler;
    private String status;

    /**
     * Default constructor for S21 entity.
     * Creates a new S21 Teleplan payment summary record with all fields initialized to null.
     */
    public S21() {
    }

    /**
     * Gets the unique identifier for this S21 payment summary record.
     * This ID is used for database operations and linking to related S22 and S23 records.
     *
     * @return String the S21 record ID
     */
    public String getS21Id() {
        return s21Id;
    }

    /**
     * Sets the unique identifier for this S21 payment summary record.
     *
     * @param s21Id String the S21 record ID
     */
    public void setS21Id(String s21Id) {
        this.s21Id = s21Id;
    }

    /**
     * Gets the Teleplan response file name from which this S21 record was extracted.
     * This provides traceability back to the original MSP payment file for audit purposes.
     *
     * @return String the Teleplan response file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the Teleplan response file name from which this S21 record was extracted.
     *
     * @param fileName String the Teleplan response file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDataCentre() {
        return dataCentre;
    }

    public void setDataCentre(String dataCentre) {
        this.dataCentre = dataCentre;
    }

    public String getDataSeq() {
        return dataSeq;
    }

    public void setDataSeq(String dataSeq) {
        this.dataSeq = dataSeq;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    /**
     * Gets the MSP payee number identifying the healthcare provider receiving payment.
     * This number corresponds to the provider's MSP billing identifier.
     *
     * @return String the MSP payee number
     */
    public String getPayeeNo() {
        return payeeNo;
    }

    /**
     * Sets the MSP payee number identifying the healthcare provider receiving payment.
     *
     * @param payeeNo String the MSP payee number
     */
    public void setPayeeNo(String payeeNo) {
        this.payeeNo = payeeNo;
    }

    /**
     * Gets the MSP control number for this payment batch.
     * This number links the payment to the specific submission batch processed by MSP.
     *
     * @return String the MSP control number
     */
    public String getMspCTLno() {
        return mspCTLno;
    }

    /**
     * Sets the MSP control number for this payment batch.
     *
     * @param mspCTLno String the MSP control number
     */
    public void setMspCTLno(String mspCTLno) {
        this.mspCTLno = mspCTLno;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    /**
     * Gets the total amount billed to MSP for this payment summary.
     * This represents the sum of all medical services claimed in this batch.
     *
     * @return String the total amount billed to MSP
     */
    public String getAmtBilled() {
        return amtBilled;
    }

    /**
     * Sets the total amount billed to MSP for this payment summary.
     *
     * @param amtBilled String the total amount billed to MSP
     */
    public void setAmtBilled(String amtBilled) {
        this.amtBilled = amtBilled;
    }

    /**
     * Gets the total amount paid by MSP for this payment summary.
     * This may differ from the amount billed due to adjustments, reductions, or rejections.
     *
     * @return String the total amount paid by MSP
     */
    public String getAmtPaid() {
        return amtPaid;
    }

    /**
     * Sets the total amount paid by MSP for this payment summary.
     *
     * @param amtPaid String the total amount paid by MSP
     */
    public void setAmtPaid(String amtPaid) {
        this.amtPaid = amtPaid;
    }

    public String getBalanceFwd() {
        return balanceFwd;
    }

    public void setBalanceFwd(String balanceFwd) {
        this.balanceFwd = balanceFwd;
    }

    /**
     * Gets the cheque number or electronic payment reference for this remittance.
     * This identifier links the payment summary to the actual payment instrument.
     *
     * @return String the cheque number or payment reference
     */
    public String getCheque() {
        return cheque;
    }

    /**
     * Sets the cheque number or electronic payment reference for this remittance.
     *
     * @param cheque String the cheque number or payment reference
     */
    public void setCheque(String cheque) {
        this.cheque = cheque;
    }

    public String getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(String newBalance) {
        this.newBalance = newBalance;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }

    /**
     * Gets the processing status of this S21 payment summary record.
     * Status values track the payment processing workflow and reconciliation state.
     *
     * @return String the processing status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the processing status of this S21 payment summary record.
     *
     * @param status String the processing status
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
