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
 * S22 entity representing Teleplan provider-level detail records for British Columbia
 * MSP (Medical Services Plan) billing system. This entity captures detailed billing
 * information at the individual practitioner level within the Teleplan payment
 * hierarchy, providing specific financial reconciliation data for each healthcare
 * provider associated with a payment summary.
 *
 * <p>The S22 record type contains provider-specific payment details that sit between
 * the high-level S21 payment summary and the detailed S23 adjustment records. These
 * records enable healthcare organizations to allocate payments to specific practitioners
 * and understand billing performance at the individual provider level.
 *
 * <p>Key Teleplan S22 features include:
 * <ul>
 * <li>Provider-specific billing and payment amounts within a payment batch
 * <li>Practitioner identification linking to provider master records
 * <li>MSP control number inheritance from parent S21 record
 * <li>Payment allocation for multi-provider healthcare organizations
 * <li>S22 type classification for different record variations
 * <li>Data integrity tracking with data centre and sequence information
 * </ul>
 *
 * <p>The S22 record fits within the Teleplan hierarchy as:
 * <ul>
 * <li><strong>S21:</strong> Payment summary (parent record)
 * <li><strong>S22:</strong> Provider-level details (this entity)
 * <li><strong>S23:</strong> Individual claim adjustments (child records)
 * </ul>
 *
 * <p>This entity is essential for healthcare organizations with multiple providers
 * to properly allocate MSP payments, track individual provider billing performance,
 * and identify provider-specific payment issues or adjustments within the BC
 * healthcare billing system.
 *
 * @see ca.openosp.openo.entities.S21
 * @see ca.openosp.openo.entities.S23
 * @see ca.openosp.openo.entities.Provider
 * @since November 1, 2004
 */
public class S22 {

    private String s22Id;
    private String s21Id;
    private String fileName;
    private String s22Type;
    private String dataCentre;
    private String dataSeq;
    private String paymentDate;
    private String lineCode;
    private String payeeNo;
    private String payeeName;
    private String mspCTLno;
    private String practitionerNo;
    private String practitionerName;
    private String amtBilled;
    private String amtPaid;
    private String filler;

    /**
     * Default constructor for S22 entity.
     * Creates a new S22 Teleplan provider-level detail record with all fields initialized to null.
     */
    public S22() {
    }

    /**
     * Gets the unique identifier for this S22 provider detail record.
     * This ID is used for database operations and tracking within the Teleplan hierarchy.
     *
     * @return String the S22 record ID
     */
    public String getS22Id() {
        return s22Id;
    }

    /**
     * Sets the unique identifier for this S22 provider detail record.
     *
     * @param s22Id String the S22 record ID
     */
    public void setS22Id(String s22Id) {
        this.s22Id = s22Id;
    }

    /**
     * Gets the parent S21 payment summary ID that this S22 record belongs to.
     * This foreign key relationship links provider details to the payment summary.
     *
     * @return String the parent S21 record ID
     * @see ca.openosp.openo.entities.S21
     */
    public String getS21Id() {
        return s21Id;
    }

    /**
     * Sets the parent S21 payment summary ID that this S22 record belongs to.
     *
     * @param s21Id String the parent S21 record ID
     */
    public void setS21Id(String s21Id) {
        this.s21Id = s21Id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the S22 record type classification.
     * Different S22 types may contain varying data elements or represent
     * different categories of provider-level payment information.
     *
     * @return String the S22 record type
     */
    public String getS22Type() {
        return s22Type;
    }

    /**
     * Sets the S22 record type classification.
     *
     * @param s22Type String the S22 record type
     */
    public void setS22Type(String s22Type) {
        this.s22Type = s22Type;
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

    public String getPayeeNo() {
        return payeeNo;
    }

    public void setPayeeNo(String payeeNo) {
        this.payeeNo = payeeNo;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getMspCTLno() {
        return mspCTLno;
    }

    public void setMspCTLno(String mspCTLno) {
        this.mspCTLno = mspCTLno;
    }

    /**
     * Gets the MSP practitioner number for the healthcare provider.
     * This identifier links the S22 record to a specific healthcare practitioner
     * for payment allocation and billing reconciliation.
     *
     * @return String the MSP practitioner number
     * @see ca.openosp.openo.entities.Provider
     */
    public String getPractitionerNo() {
        return practitionerNo;
    }

    /**
     * Sets the MSP practitioner number for the healthcare provider.
     *
     * @param practitionerNo String the MSP practitioner number
     */
    public void setPractitionerNo(String practitionerNo) {
        this.practitionerNo = practitionerNo;
    }

    /**
     * Gets the practitioner's name as recorded in the Teleplan response.
     * This name provides human-readable identification of the healthcare provider
     * for payment reconciliation and reporting purposes.
     *
     * @return String the practitioner's name
     */
    public String getPractitionerName() {
        return practitionerName;
    }

    /**
     * Sets the practitioner's name as recorded in the Teleplan response.
     *
     * @param practitionerName String the practitioner's name
     */
    public void setPractitionerName(String practitionerName) {
        this.practitionerName = practitionerName;
    }

    /**
     * Gets the total amount billed by this specific practitioner.
     * This represents the portion of the total S21 billing amount attributable
     * to this individual healthcare provider.
     *
     * @return String the amount billed by this practitioner
     */
    public String getAmtBilled() {
        return amtBilled;
    }

    /**
     * Sets the total amount billed by this specific practitioner.
     *
     * @param amtBilled String the amount billed by this practitioner
     */
    public void setAmtBilled(String amtBilled) {
        this.amtBilled = amtBilled;
    }

    /**
     * Gets the total amount paid to this specific practitioner.
     * This may differ from the amount billed due to adjustments, reductions,
     * or claim rejections at the practitioner level.
     *
     * @return String the amount paid to this practitioner
     */
    public String getAmtPaid() {
        return amtPaid;
    }

    /**
     * Sets the total amount paid to this specific practitioner.
     *
     * @param amtPaid String the amount paid to this practitioner
     */
    public void setAmtPaid(String amtPaid) {
        this.amtPaid = amtPaid;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
