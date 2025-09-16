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
 * S23 entity representing Teleplan individual claim adjustment records for British
 * Columbia MSP (Medical Services Plan) billing system. This entity captures the most
 * detailed level of billing information within the Teleplan payment hierarchy,
 * providing specific adjustment details, calculation methods, and payment breakdown
 * for individual healthcare service claims.
 *
 * <p>The S23 record type contains the granular adjustment information that explains
 * differences between amounts billed and amounts paid. These records are essential
 * for understanding MSP payment decisions, identifying billing issues, and ensuring
 * accurate financial reconciliation at the individual claim level.
 *
 * <p>Key Teleplan S23 features include:
 * <ul>
 * <li>Individual claim adjustment tracking with detailed calculation methods
 * <li>Payment breakdown by percentage and amount categories
 * <li>Adjustment codes (AJC, AJI, AJM) for different adjustment types
 * <li>Outstanding balance tracking and forward balance calculations
 * <li>Payment allocation across different funding sources or adjustment categories
 * <li>Integration with parent S21 payment summary records
 * </ul>
 *
 * <p>The S23 adjustment fields include:
 * <ul>
 * <li><strong>AJC, AJI, AJM:</strong> Adjustment codes for different adjustment categories
 * <li><strong>Calc Method:</strong> Calculation methodology used for payment determination
 * <li><strong>Percentages:</strong> Payment percentage calculations (rpercent, opercent)
 * <li><strong>Amounts:</strong> Financial amounts by category (gamount, ramount, oamount)
 * <li><strong>Balances:</strong> Forward balances and outstanding amounts
 * </ul>
 *
 * <p>The S23 record completes the Teleplan hierarchy as:
 * <ul>
 * <li><strong>S21:</strong> Payment summary (top-level record)
 * <li><strong>S22:</strong> Provider-level details (mid-level record)
 * <li><strong>S23:</strong> Individual claim adjustments (this entity - detail level)
 * </ul>
 *
 * <p>This entity is crucial for healthcare providers to understand the specific
 * reasons for payment adjustments, track claim-level financial performance, and
 * identify patterns in MSP payment decisions that may require billing practice
 * modifications or appeals.
 *
 * @see ca.openosp.openo.entities.S21
 * @see ca.openosp.openo.entities.S22
 * @since November 1, 2004
 */
public class S23 {

    private String s23Id;
    private String s21Id;
    private String fileName;
    private String s23Type;
    private String dataCentre;
    private String dataSeq;
    private String paymentDate;
    private String lineCode;
    private String payeeNo;
    private String payeeName;
    private String mspCTLno;
    private String ajc;
    private String aji;
    private String ajm;
    private String calcMethod;
    private String rpercent;
    private String opercent;
    private String gamount;
    private String ramount;
    private String oamount;
    private String balanceFwd;
    private String adjmade;
    private String adjOutstanding;
    private String filler;

    /**
     * Default constructor for S23 entity.
     * Creates a new S23 Teleplan individual claim adjustment record with all fields initialized to null.
     */
    public S23() {
    }

    /**
     * Gets the unique identifier for this S23 claim adjustment record.
     * This ID is used for database operations and tracking within the Teleplan hierarchy.
     *
     * @return String the S23 record ID
     */
    public String getS23Id() {
        return s23Id;
    }

    /**
     * Sets the unique identifier for this S23 claim adjustment record.
     *
     * @param s23Id String the S23 record ID
     */
    public void setS23Id(String s23Id) {
        this.s23Id = s23Id;
    }

    /**
     * Gets the parent S21 payment summary ID that this S23 adjustment record belongs to.
     * This foreign key relationship links individual claim adjustments to the payment summary.
     *
     * @return String the parent S21 record ID
     * @see ca.openosp.openo.entities.S21
     */
    public String getS21Id() {
        return s21Id;
    }

    /**
     * Sets the parent S21 payment summary ID that this S23 adjustment record belongs to.
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

    public String getS23Type() {
        return s23Type;
    }

    public void setS23Type(String s23Type) {
        this.s23Type = s23Type;
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
     * Gets the AJC (Adjustment Code C) for this claim adjustment.
     * This code represents one category of adjustment applied by MSP to the claim.
     *
     * @return String the AJC adjustment code
     */
    public String getAjc() {
        return ajc;
    }

    /**
     * Sets the AJC (Adjustment Code C) for this claim adjustment.
     *
     * @param ajc String the AJC adjustment code
     */
    public void setAjc(String ajc) {
        this.ajc = ajc;
    }

    /**
     * Gets the AJI (Adjustment Code I) for this claim adjustment.
     * This code represents another category of adjustment applied by MSP to the claim.
     *
     * @return String the AJI adjustment code
     */
    public String getAji() {
        return aji;
    }

    /**
     * Sets the AJI (Adjustment Code I) for this claim adjustment.
     *
     * @param aji String the AJI adjustment code
     */
    public void setAji(String aji) {
        this.aji = aji;
    }

    /**
     * Gets the AJM (Adjustment Code M) for this claim adjustment.
     * This code represents a third category of adjustment applied by MSP to the claim.
     *
     * @return String the AJM adjustment code
     */
    public String getAjm() {
        return ajm;
    }

    /**
     * Sets the AJM (Adjustment Code M) for this claim adjustment.
     *
     * @param ajm String the AJM adjustment code
     */
    public void setAjm(String ajm) {
        this.ajm = ajm;
    }

    /**
     * Gets the calculation method used by MSP for this claim payment determination.
     * This indicates the methodology applied for calculating the final payment amount.
     *
     * @return String the calculation method code
     */
    public String getCalcMethod() {
        return calcMethod;
    }

    /**
     * Sets the calculation method used by MSP for this claim payment determination.
     *
     * @param calcMethod String the calculation method code
     */
    public void setCalcMethod(String calcMethod) {
        this.calcMethod = calcMethod;
    }

    public String getRpercent() {
        return rpercent;
    }

    public void setRpercent(String rpercent) {
        this.rpercent = rpercent;
    }

    public String getOpercent() {
        return opercent;
    }

    public void setOpercent(String opercent) {
        this.opercent = opercent;
    }

    /**
     * Gets the G amount (gross amount) for this claim adjustment.
     * This represents one component of the financial breakdown for the claim.
     *
     * @return String the G amount
     */
    public String getGamount() {
        return gamount;
    }

    /**
     * Sets the G amount (gross amount) for this claim adjustment.
     *
     * @param gamount String the G amount
     */
    public void setGamount(String gamount) {
        this.gamount = gamount;
    }

    public String getRamount() {
        return ramount;
    }

    public void setRamount(String ramount) {
        this.ramount = ramount;
    }

    public String getOamount() {
        return oamount;
    }

    public void setOamount(String oamount) {
        this.oamount = oamount;
    }

    /**
     * Gets the balance forward amount for this claim adjustment.
     * This represents any outstanding balance carried forward from previous processing.
     *
     * @return String the balance forward amount
     */
    public String getBalanceFwd() {
        return balanceFwd;
    }

    /**
     * Sets the balance forward amount for this claim adjustment.
     *
     * @param balanceFwd String the balance forward amount
     */
    public void setBalanceFwd(String balanceFwd) {
        this.balanceFwd = balanceFwd;
    }

    public String getAdjmade() {
        return adjmade;
    }

    public void setAdjmade(String adjmade) {
        this.adjmade = adjmade;
    }

    /**
     * Gets the outstanding adjustment amount for this claim.
     * This represents any remaining adjustment amount that is still outstanding
     * and may require further processing or resolution.
     *
     * @return String the outstanding adjustment amount
     */
    public String getAdjOutstanding() {
        return adjOutstanding;
    }

    /**
     * Sets the outstanding adjustment amount for this claim.
     *
     * @param adjOutstanding String the outstanding adjustment amount
     */
    public void setAdjOutstanding(String adjOutstanding) {
        this.adjOutstanding = adjOutstanding;
    }

    public String getFiller() {
        return filler;
    }

    public void setFiller(String filler) {
        this.filler = filler;
    }
}
