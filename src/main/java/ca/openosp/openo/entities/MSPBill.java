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

import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.util.DateUtils;
import ca.openosp.openo.util.UtilDateUtilities;

import java.util.Date;
import java.util.Enumeration;

/**
 * Medical Services Plan (MSP) and general billing entity for British Columbia.
 *
 * This entity represents billing information for various payment types in the BC healthcare
 * system, including MSP, ICBC (Motor Vehicle Accidents), WCB (Workers' Compensation Board),
 * and private insurance billing. Originally designed for BC MSP billing, it has evolved
 * to support multiple billing scenarios.
 *
 * Key billing components include:
 * - Patient demographic and appointment information
 * - Service codes, dates, and billing amounts
 * - Diagnostic codes (dx1, dx2, dx3) for clinical justification
 * - Provider information and billing details
 * - Payment status and reconciliation data
 * - Explanation codes for claim rejections or adjustments
 *
 * Special features for BC healthcare:
 * - Integration with Teleplan (BC MSP billing system)
 * - WCB and ICBC injury claim support
 * - Aging analysis for accounts receivable management
 * - Multi-payee and payment method tracking
 *
 * Note: This class uses public fields for backward compatibility with legacy systems.
 * Future refactoring should implement proper encapsulation following JavaBean standards.
 *
 * @see <a href="https://www2.gov.bc.ca/gov/content/health/practitioner-professional-resources/msp">BC MSP Information</a>
 * @since June 11, 2005
 */
public class MSPBill {
    public String serviceDateRange = "";
    public String billing_no = "";
    public String apptDoctorNo = "";
    public String apptNo = "";
    public String demoNo = "";
    public String demoName = "";
    public String userno = "";
    public String apptDate = "";
    public String apptTime = "";
    public String reason = "";
    public String billMasterNo = "";
    public String billingtype = "";
    public String demoSex = "";
    public String code = "";
    public String amount = "";
    public String dx1 = "";
    public String dx2 = "";
    public String dx3 = "";
    public String providerFirstName = "";
    public String providerLastName = "";
    public String updateDate = "";
    public String billingUnit = "";
    public String serviceDate = "";
    public boolean WCB;
    private UtilDateUtilities ut;
    public String mvaCode;
    public String serviceLocation;
    public String demoDOB;
    public String demoPhone2;
    public String demoPhone;
    public String hin;
    public String serviceEndTime;
    public String serviceStartTime;
    public String serviceToDate;
    public String serviceStartDate;
    public String status;
    public String paymentDate;
    public String exp1 = "";
    public String exp2 = "";
    public String accountName;
    public String payeeName;
    public String provName;
    public String type;
    public String seqNum;
    public String acctInit;
    public String payeeNo;
    public String expString;
    public String expSum;
    public java.util.Hashtable explanations;
    public String accountNo;
    public String paymentMethod;
    public String paymentMethodName;
    public Date serviceDateDate;
    public String rejectionDate;
    public String adjustmentCode = "";
    public String adjustmentCodeDesc = "";
    public String amtOwing;
    public String adjustmentCodeAmt = "";

    /**
     * Default constructor for MSP billing entity.
     * Initializes the utility date handler for billing date calculations.
     */
    public MSPBill() {
        ut = new UtilDateUtilities();
    }

    /**
     * Checks if this bill is for Workers' Compensation Board (WCB).
     * WCB bills are for workplace-related injuries and have special processing requirements.
     *
     * @return boolean true if this is a WCB bill, false otherwise
     */
    public boolean isWCB() {
        return billingtype.equals("WCB");
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getApptDate() {
        return apptDate;
    }

    public void setApptDate(String apptDate) {
        this.apptDate = apptDate;
    }

    public String getApptDoctorNo() {
        return apptDoctorNo;
    }

    public void setApptDoctorNo(String apptDoctorNo) {
        this.apptDoctorNo = apptDoctorNo;
    }

    public String getApptNo() {
        return apptNo;
    }

    public void setApptNo(String apptNo) {
        this.apptNo = apptNo;
    }

    public String getApptTime() {
        return apptTime;
    }

    public void setApptTime(String apptTime) {
        this.apptTime = apptTime;
    }

    public String getBilling_no() {
        return billing_no;
    }

    public void setBilling_no(String billing_no) {
        this.billing_no = billing_no;
    }

    public String getBillingtype() {
        return billingtype;
    }

    public void setBillingtype(String billingtype) {
        this.billingtype = billingtype;
    }

    public void setBillingUnit(String billingUnit) {
        this.billingUnit = billingUnit;
    }

    public void setBillMasterNo(String billMasterNo) {
        this.billMasterNo = billMasterNo;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDemoName(String demoName) {
        this.demoName = demoName;
    }

    public void setDemoNo(String demoNo) {
        this.demoNo = demoNo;
    }

    public void setDx1(String dx1) {
        this.dx1 = dx1;
    }

    public void setDx2(String dx2) {
        this.dx2 = dx2;
    }

    public void setDx3(String dx3) {
        this.dx3 = dx3;
    }

    public void setProviderLastName(String providerLastName) {
        this.providerLastName = providerLastName;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setServiceDate(String serviceDate) {
        this.serviceDate = serviceDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public void setUserno(String userno) {
        this.userno = userno;
    }

    public void setWCB(boolean WCB) {
        this.WCB = WCB;
    }

    public String getUserno() {
        return userno;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public String getServiceDate() {
        return serviceDate;
    }

    public String getReason() {
        return reason;
    }

    public String getProviderLastName() {
        return providerLastName;
    }

    public String getProviderFirstName() {
        return providerFirstName;
    }

    public String getDx3() {
        return dx3;
    }

    public String getDx2() {
        return dx2;
    }

    public String getDx1() {
        return dx1;
    }

    public String getDemoNo() {
        return demoNo;
    }

    public String getDemoName() {
        return demoName;
    }

    public String getCode() {
        return code;
    }

    public String getBillMasterNo() {
        return billMasterNo;
    }

    public String getBillingUnit() {
        return billingUnit;
    }

    public String getDemoSex() {
        return demoSex;
    }

    public void setDemoSex(String demoSex) {
        this.demoSex = demoSex;
    }

    public void setDemoPhone2(String demoPhone2) {
        this.demoPhone2 = demoPhone2;
    }

    public String getDemoPhone2() {
        return demoPhone2;
    }

    public String getDemoDOB() {
        return demoDOB;
    }

    public void setDemoDOB(String demoDOB) {
        this.demoDOB = demoDOB;
    }

    public void setExplanations(java.util.Hashtable explanations) {
        this.explanations = explanations;
    }

    public void setMvaCode(String mvaCode) {
        this.mvaCode = mvaCode;
    }

    public void setProviderFirstName(String providerFirstName) {
        this.providerFirstName = providerFirstName;
    }

    public String getMvaCode() {
        return mvaCode;
    }

    public java.util.Hashtable getExplanations() {
        return explanations;
    }

    public String getServiceLocation() {
        return serviceLocation;
    }

    public void setServiceLocation(String serviceLocation) {
        this.serviceLocation = serviceLocation;
    }

    public void setUt(UtilDateUtilities ut) {
        this.ut = ut;
    }

    public UtilDateUtilities getUt() {
        return ut;
    }

    public String getDemoPhone() {
        return demoPhone;
    }

    public void setDemoPhone(String demoPhone) {
        this.demoPhone = demoPhone;
    }

    public String getHin() {
        return hin;
    }

    public void setHin(String hin) {
        this.hin = hin;
    }

    public String getServiceEndTime() {
        return serviceEndTime;
    }

    public void setServiceEndTime(String serviceEndTime) {
        this.serviceEndTime = serviceEndTime;
    }

    public String getServiceStartTime() {
        return serviceStartTime;
    }

    public void setServiceStartTime(String serviceStartTime) {
        this.serviceStartTime = serviceStartTime;
    }

    public void setServiceToDate(String serviceToDate) {
        this.serviceToDate = serviceToDate;
    }

    public String getServiceToDate() {
        return serviceToDate;
    }

    public String getServiceStartDate() {
        return serviceStartDate;
    }

    public void setServiceStartDate(String serviceStartDate) {
        this.serviceStartDate = serviceStartDate;
    }

    /**
     * Calculates the aging category for accounts receivable management.
     * Returns a string code representing how many days old this bill is since service date:
     * "1" = 0-30 days, "2" = 31-60 days, "3" = 61-90 days, "4" = 91+ days.
     * This is essential for aging reports and collection management in healthcare billing.
     *
     * @return String the aging category code ("0", "1", "2", "3", or "4")
     */
    public String getServiceDateRange() {
        String ret = "0";
        java.util.Date dt = null;
        try {
            java.text.DateFormat formatter = new java.text.SimpleDateFormat(
                    "yyyyMMdd");
            dt = formatter.parse(this.serviceDate);
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        }
        DateUtils ut = new DateUtils();
        long daysOld = DateUtils.getDifDays(new Date(), dt);
        if (daysOld >= 0 && daysOld <= 30) {
            ret = "1";
        } else if (daysOld >= 31 && daysOld <= 60) {
            ret = "2";
        } else if (daysOld >= 61 && daysOld <= 90) {
            ret = "3";
        } else if (daysOld > 91) {
            ret = "4";
        }
        return ret;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setServiceDateRange(String serviceDateRange) {
        this.serviceDateRange = serviceDateRange;
    }

    public String getExp1() {
        return exp1;
    }

    public String getExp2() {
        return exp2;
    }

    public void setExp1(String exp1) {
        this.exp1 = exp1;
    }

    public void setExp2(String exp2) {
        this.exp2 = exp2;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public String getProvName() {
        return provName;
    }

    public void setProvName(String provName) {
        this.provName = provName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(String seqNum) {
        this.seqNum = seqNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAcctInit() {
        return acctInit;
    }

    public void setAcctInit(String acctInit) {
        this.acctInit = acctInit;
    }

    /**
     * Gets the concatenated explanation codes for this bill.
     * Explanation codes are used by MSP and other payers to indicate
     * reasons for claim adjustments, rejections, or special handling.
     *
     * @return String concatenated explanation codes
     */
    public String getExpString() {
        return this.expString;
    }

    public void setExpString(String expString) {
        this.expString = expString;
    }

    /**
     * Gets a formatted summary of explanation codes for rejected bills.
     * Provides human-readable descriptions of why claims were rejected or adjusted
     * by MSP or other payers. Each code is formatted as "code:description\n".
     *
     * Note: This method contains presentation logic that should be refactored
     * into a separate presentation layer class.
     *
     * @return String formatted explanation code summary with descriptions
     */
    public String getExpSum() {
        String summary = "";
        try {
            if (this.explanations != null) {
                Enumeration e = this.explanations.keys();
                while (e.hasMoreElements()) {
                    String code = (String) e.nextElement();
                    String desc = (String) this.explanations.get(code);
                    summary += code + ":" + desc + "\n";
                }
            } else {
                MiscUtils.getLogger().debug("null sum=" + this.hashCode());
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error", e);
        }
        return summary;
    }

    public void setExpSum(String expSum) {
        this.expSum = expSum;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public String getRejectionDate() {
        return rejectionDate;
    }

    public Date getServiceDateDate() {
        return serviceDateDate;
    }

    public String getPayeeNo() {
        return payeeNo;
    }

    public String getAdjustmentCode() {
        return adjustmentCode;
    }

    public String getAdjustmentCodeDesc() {
        return adjustmentCodeDesc;
    }

    public String getAmtOwing() {
        return amtOwing;
    }

    public String getAdjustmentCodeAmt() {
        return adjustmentCodeAmt;
    }


    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public void setRejectionDate(String rejectionDate) {
        this.rejectionDate = rejectionDate;
    }

    public void setServiceDateDate(Date serviceDateDate) {
        this.serviceDateDate = serviceDateDate;
    }

    public void setPayeeNo(String payeeNo) {
        this.payeeNo = payeeNo;
    }

    public void setAdjustmentCode(String adjustmentCode) {
        this.adjustmentCode = adjustmentCode;
    }

    public void setAdjustmentCodeDesc(String adjustmentCodeDesc) {
        this.adjustmentCodeDesc = adjustmentCodeDesc;
    }

    public void setAmtOwing(String amtOwing) {
        this.amtOwing = amtOwing;
    }

    public void setAdjustmentCodeAmt(String adjustmentCodeAmt) {
        this.adjustmentCodeAmt = adjustmentCodeAmt;
    }

}
