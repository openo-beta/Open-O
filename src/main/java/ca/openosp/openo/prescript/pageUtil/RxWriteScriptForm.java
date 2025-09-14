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


package ca.openosp.openo.prescript.pageUtil;

import ca.openosp.openo.utility.MiscUtils;

/**
 * Form bean for prescription writing data.
 *
 * This class serves as a data transfer object for prescription forms,
 * holding all the fields required to create or update a prescription.
 * It includes drug identification, dosing instructions, duration,
 * special flags, and provider information.
 *
 * The form handles both standard formulary drugs (identified by GCN)
 * and custom non-formulary medications. It supports various prescription
 * attributes including PRN status, substitution preferences, and
 * compliance tracking.
 *
 * @since 2003-12-10
 */
public final class RxWriteScriptForm {
    /**
     * Form action to perform (save, update, etc.).
     */
    String action = "";

    /**
     * Database ID of existing drug record.
     */
    int drugId = 0;

    /**
     * Patient demographic number.
     */
    int demographicNo = 0;

    /**
     * Prescription start date.
     */
    String rxDate = null;

    /**
     * Prescription end date.
     */
    String endDate = null;

    /**
     * Date prescription was written.
     */
    String writtenDate = null;

    /**
     * Generic drug name.
     */
    String GN = null;

    /**
     * Brand drug name.
     */
    String BN = null;

    /**
     * GCN sequence number for drug identification.
     */
    int GCN_SEQNO = 0;

    /**
     * Custom name for non-formulary drugs.
     */
    String customName = null;

    /**
     * Minimum dosage amount.
     */
    String takeMin = null;

    /**
     * Maximum dosage amount.
     */
    String takeMax = null;

    /**
     * Frequency code (e.g., "TID", "BID").
     */
    String frequencyCode = null;

    /**
     * Duration amount.
     */
    String duration = null;

    /**
     * Duration unit (D=days, W=weeks, M=months).
     */
    String durationUnit = null;

    /**
     * Quantity to dispense.
     */
    String quantity = null;

    /**
     * Number of refills allowed.
     */
    int repeat = 0;

    /**
     * Date of last refill.
     */
    String lastRefillDate = null;

    /**
     * No substitution flag.
     */
    boolean nosubs = false;

    /**
     * PRN (as needed) flag.
     */
    boolean prn = false;

    /**
     * Custom instructions flag.
     */
    boolean customInstr = false;

    /**
     * Long-term medication flag.
     */
    Boolean longTerm = null;

    /**
     * Short-term medication flag.
     */
    boolean shortTerm = false;

    /**
     * Past medication flag.
     */
    Boolean pastMed = null;

    /**
     * Internal dispensing flag.
     */
    boolean dispenseInternal = false;

    /**
     * Patient compliance indicator.
     */
    Boolean patientCompliance = null;

    /**
     * Special instructions text.
     */
    String special = null;

    /**
     * ATC classification code.
     */
    String atcCode = null;

    /**
     * Regional drug identifier.
     */
    String regionalIdentifier = null;

    /**
     * Administration method.
     */
    String method = null;

    /**
     * Dosage unit.
     */
    String unit = null;

    /**
     * Unit name for display.
     */
    String unitName = null;

    /**
     * Administration route.
     */
    String route = null;

    /**
     * Dosage strength.
     */
    String dosage = null;

    /**
     * External provider name.
     */
    String outsideProviderName = null;

    /**
     * External provider OHIP number.
     */
    String outsideProviderOhip = null;


    /**
     * Gets the form action.
     *
     * @return String action type
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Sets the form action.
     *
     * @param RHS String action type
     */
    public void setAction(String RHS) {
        this.action = RHS;
    }

    /**
     * Gets the drug ID.
     *
     * @return int drug database ID
     */
    public int getDrugId() {
        return this.drugId;
    }

    /**
     * Sets the drug ID.
     *
     * @param RHS int drug database ID
     */
    public void setDrugID(int RHS) {
        this.drugId = RHS;
    }

    /**
     * Gets the demographic number.
     *
     * @return int patient identifier
     */
    public int getDemographicNo() {
        return this.demographicNo;
    }

    /**
     * Sets the demographic number.
     *
     * @param RHS int patient identifier
     */
    public void setDemographicNo(int RHS) {
        this.demographicNo = RHS;
    }

    /**
     * Gets the prescription date.
     *
     * @return String start date
     */
    public String getRxDate() {
        return this.rxDate;
    }

    /**
     * Sets the prescription date.
     *
     * @param RHS String start date
     */
    public void setRxDate(String RHS) {
        this.rxDate = RHS;
    }

    /**
     * Gets the end date.
     *
     * @return String prescription end date
     */
    public String getEndDate() {
        return this.endDate;
    }

    /**
     * Sets the end date.
     *
     * @param RHS String prescription end date
     */
    public void setEndDate(String RHS) {
        this.endDate = RHS;
    }

    /**
     * Gets the written date.
     *
     * @return String date prescription was written
     */
    public String getWrittenDate() {
        return this.writtenDate;
    }

    /**
     * Sets the written date.
     *
     * @param RHS String date prescription was written
     */
    public void setWrittenDate(String RHS) {
        this.writtenDate = RHS;
    }

    /**
     * Gets the generic name.
     *
     * @return String generic drug name
     */
    public String getGenericName() {
        return this.GN;
    }

    /**
     * Sets the generic name.
     *
     * @param RHS String generic drug name
     */
    public void setGenericName(String RHS) {
        this.GN = RHS;
    }

    /**
     * Gets the brand name.
     *
     * @return String brand drug name
     */
    public String getBrandName() {
        return this.BN;
    }

    /**
     * Sets the brand name.
     *
     * @param RHS String brand drug name
     */
    public void setBrandName(String RHS) {
        this.BN = RHS;
    }

    /**
     * Gets the GCN sequence number.
     *
     * @return int GCN identifier
     */
    public int getGCN_SEQNO() {
        return this.GCN_SEQNO;
    }

    /**
     * Sets the GCN sequence number.
     *
     * @param RHS int GCN identifier
     */
    public void setGCN_SEQNO(int RHS) {
        this.GCN_SEQNO = RHS;
    }

    /**
     * Gets the custom drug name.
     *
     * @return String custom name for non-formulary drugs
     */
    public String getCustomName() {
        return this.customName;
    }

    /**
     * Sets the custom drug name.
     *
     * @param RHS String custom name
     */
    public void setCustomName(String RHS) {
        this.customName = RHS;
    }

    /**
     * Gets the minimum dosage.
     *
     * @return String minimum dosage amount
     */
    public String getTakeMin() {
        return this.takeMin;
    }

    /**
     * Sets the minimum dosage.
     *
     * @param RHS String minimum dosage amount
     */
    public void setTakeMin(String RHS) {
        this.takeMin = RHS;
    }

    /**
     * Gets minimum dosage as float.
     *
     * Converts string dosage to float for calculations.
     *
     * @return float minimum dosage or -1 if invalid
     */
    public float getTakeMinFloat() {
        float i = -1;
        try {
            i = Float.parseFloat(this.takeMin);
        } catch (Exception e) {
        }
        return i;
    }

    /**
     * Gets the maximum dosage.
     *
     * @return String maximum dosage amount
     */
    public String getTakeMax() {
        return this.takeMax;
    }

    /**
     * Sets the maximum dosage.
     *
     * @param RHS String maximum dosage amount
     */
    public void setTakeMax(String RHS) {
        this.takeMax = RHS;
    }

    /**
     * Gets maximum dosage as float.
     *
     * Converts string dosage to float for calculations.
     *
     * @return float maximum dosage or -1 if invalid
     */
    public float getTakeMaxFloat() {
        float i = -1;
        try {
            i = Float.parseFloat(this.takeMax);
        } catch (Exception e) {
        }
        return i;
    }

    /**
     * Gets the frequency code.
     *
     * @return String frequency code (e.g., "TID")
     */
    public String getFrequencyCode() {
        return this.frequencyCode;
    }

    /**
     * Sets the frequency code.
     *
     * @param RHS String frequency code
     */
    public void setFrequencyCode(String RHS) {
        this.frequencyCode = RHS;
    }

    /**
     * Gets the duration amount.
     *
     * @return String duration value
     */
    public String getDuration() {
        return this.duration;
    }

    /**
     * Sets the duration amount.
     *
     * @param RHS String duration value
     */
    public void setDuration(String RHS) {
        this.duration = RHS;
    }

    public String getDurationUnit() {
        return this.durationUnit;
    }

    public void setDurationUnit(String RHS) {
        this.durationUnit = RHS;
    }

    public String getQuantity() {
        return this.quantity;
    }

    public void setQuantity(String RHS) {
        this.quantity = RHS;
    }

    public int getRepeat() {
        return this.repeat;
    }

    public void setRepeat(int RHS) {
        this.repeat = RHS;
    }

    public String getLastRefillDate() {
        return this.lastRefillDate;
    }

    public void setLastRefillDate(String RHS) {
        this.lastRefillDate = RHS;
    }

    public boolean getNosubs() {
        return this.nosubs;
    }

    public void setNosubs(boolean RHS) {
        this.nosubs = RHS;
    }

    public boolean getPrn() {
        return this.prn;
    }

    public void setPrn(boolean RHS) {
        this.prn = RHS;
    }

    public String getSpecial() {
        return this.special;
    }

    public void setSpecial(String RHS) {

        if (RHS == null || RHS.length() < 6)
            MiscUtils.getLogger().error("drug special is either null or empty : " + RHS, new IllegalArgumentException("special is null or empty"));

        this.special = RHS;
    }

    public boolean getCustomInstr() {
        return this.customInstr;
    }

    public void setCustomInstr(boolean c) {
        this.customInstr = c;
    }

    public Boolean getLongTerm() {
        return this.longTerm;
    }

    public void setLongTerm(Boolean trueFalseNull) {
        this.longTerm = trueFalseNull;
    }

    public boolean getShortTerm() {
        return this.shortTerm;
    }

    public void setShortTerm(boolean st) {
        this.shortTerm = st;
    }

    public Boolean getPastMed() {
        return this.pastMed;
    }

    public void setPastMed(Boolean trueFalseNull) {
        this.pastMed = trueFalseNull;
    }

    public boolean getDispenseInternal() {
        return dispenseInternal;
    }

    public boolean isDispenseInternal() {
        return dispenseInternal;
    }

    public void setDispenseInternal(boolean dispenseInternal) {
        this.dispenseInternal = dispenseInternal;
    }

    public Boolean getPatientCompliance() {
        return this.patientCompliance;
    }

    public void setPatientCompliance(Boolean trueFalseNull) {
        this.patientCompliance = trueFalseNull;
    }

    /**
     * Getter for property atcCode.
     *
     * @return Value of property atcCode.
     */
    public java.lang.String getAtcCode() {
        return atcCode;
    }

    /**
     * Setter for property atcCode.
     *
     * @param atcCode New value of property atcCode.
     */
    public void setAtcCode(java.lang.String atcCode) {
        this.atcCode = atcCode;
    }

    /**
     * Getter for property regionalIdentifier.
     *
     * @return Value of property regionalIdentifier.
     */
    public java.lang.String getRegionalIdentifier() {
        return regionalIdentifier;
    }

    /**
     * Setter for property regionalIdentifier.
     *
     * @param regionalIdentifier New value of property regionalIdentifier.
     */
    public void setRegionalIdentifier(java.lang.String regionalIdentifier) {
        this.regionalIdentifier = regionalIdentifier;
    }

    /**
     * Getter for property method.
     *
     * @return Value of property method.
     */
    public java.lang.String getMethod() {
        return method;
    }

    /**
     * Setter for property method.
     *
     * @param method New value of property method.
     */
    public void setMethod(java.lang.String method) {
        this.method = method;
    }

    /**
     * Getter for property unit.
     *
     * @return Value of property unit.
     */
    public java.lang.String getUnit() {
        return unit;
    }

    /**
     * Setter for property unit.
     *
     * @param unit New value of property unit.
     */
    public void setUnit(java.lang.String unit) {
        this.unit = unit;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    /**
     * Getter for property route.
     *
     * @return Value of property route.
     */
    public java.lang.String getRoute() {
        return route;
    }

    /**
     * Setter for property route.
     *
     * @param route New value of property route.
     */
    public void setRoute(java.lang.String route) {
        this.route = route;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getOutsideProviderName() {
        return outsideProviderName;
    }

    public void setOutsideProviderName(String outsideProviderName) {
        this.outsideProviderName = outsideProviderName;
    }

    public String getOutsideProviderOhip() {
        return outsideProviderOhip;
    }

    public void setOutsideProviderOhip(String outsideProviderOhip) {
        this.outsideProviderOhip = outsideProviderOhip;
    }
}
