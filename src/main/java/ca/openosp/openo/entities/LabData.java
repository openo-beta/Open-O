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
 * Laboratory data entity for diabetes and cardiovascular risk assessment.
 *
 * This entity represents key laboratory values commonly used in diabetes management
 * and cardiovascular risk assessment. It focuses on essential metabolic indicators
 * that are critical for monitoring patient health and guiding clinical decisions.
 *
 * Key laboratory parameters include:
 * - Glycemic control markers (A1c, glucose levels)
 * - Lipid profile components (LDL, triglycerides, ratios)
 * - Renal function indicators (kidney function, microalbumin)
 * - Hypoglycemia tracking for safety monitoring
 *
 * This entity supports:
 * - Chronic disease management (diabetes, cardiovascular disease)
 * - Clinical decision support and care planning
 * - Quality improvement initiatives and outcome tracking
 * - Integration with clinical guidelines and protocols
 *
 * @since November 1, 2004
 */
public class LabData {
    private String a1c;
    private String ldl;
    private String ratio;
    private String triglycerides;
    private String hypoglycemia;
    private String glucLab;
    private String microalbumin;
    private String kidneyfunction;

    /**
     * Default constructor for laboratory data entity.
     * Initializes all laboratory values to their default states.
     */
    public LabData() {

    }

    /**
     * Gets the hemoglobin A1c value.
     * A1c reflects average blood glucose levels over the past 2-3 months
     * and is the gold standard for diabetes management. Target is typically <7%.
     *
     * @return String the A1c value as a percentage
     */
    public String getA1c() {
        return a1c;
    }

    /**
     * Sets the hemoglobin A1c value.
     * A1c reflects average blood glucose levels over the past 2-3 months.
     *
     * @param a1c String the A1c value as a percentage
     */
    public void setA1c(String a1c) {
        this.a1c = a1c;
    }

    /**
     * Gets the LDL (Low-Density Lipoprotein) cholesterol value.
     * LDL is "bad" cholesterol and a key risk factor for cardiovascular disease.
     * Target levels vary based on patient risk factors.
     *
     * @return String the LDL cholesterol value
     */
    public String getLdl() {
        return ldl;
    }

    /**
     * Sets the LDL (Low-Density Lipoprotein) cholesterol value.
     * LDL is "bad" cholesterol and a key cardiovascular risk factor.
     *
     * @param ldl String the LDL cholesterol value
     */
    public void setLdl(String ldl) {
        this.ldl = ldl;
    }

    /**
     * Gets the cholesterol ratio (typically Total Cholesterol/HDL ratio).
     * The ratio provides additional cardiovascular risk assessment beyond individual values.
     * Lower ratios indicate better cardiovascular health.
     *
     * @return String the cholesterol ratio value
     */
    public String getRatio() {
        return ratio;
    }

    /**
     * Sets the cholesterol ratio (typically Total Cholesterol/HDL ratio).
     * The ratio provides cardiovascular risk assessment information.
     *
     * @param ratio String the cholesterol ratio value
     */
    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    /**
     * Gets the triglycerides value.
     * Triglycerides are a type of fat found in blood and are part of the lipid profile.
     * Elevated levels increase cardiovascular and pancreatitis risk.
     *
     * @return String the triglycerides value
     */
    public String getTriglycerides() {
        return triglycerides;
    }

    /**
     * Sets the triglycerides value.
     * Triglycerides are part of the lipid profile and cardiovascular risk assessment.
     *
     * @param triglycerides String the triglycerides value
     */
    public void setTriglycerides(String triglycerides) {
        this.triglycerides = triglycerides;
    }

    /**
     * Gets the hypoglycemia indicator or value.
     * Tracks episodes of low blood glucose, which is critical for diabetes safety monitoring.
     * Hypoglycemia can be life-threatening and requires careful tracking.
     *
     * @return String the hypoglycemia indicator or count
     */
    public String getHypoglycemia() {
        return hypoglycemia;
    }

    /**
     * Sets the hypoglycemia indicator or value.
     * Tracks episodes of low blood glucose for diabetes safety monitoring.
     *
     * @param hypoglycemia String the hypoglycemia indicator or count
     */
    public void setHypoglycemia(String hypoglycemia) {
        this.hypoglycemia = hypoglycemia;
    }

    /**
     * Gets the glucose laboratory value.
     * Represents blood glucose measurements from laboratory tests,
     * used for diabetes diagnosis and monitoring.
     *
     * @return String the glucose laboratory value
     */
    public String getGlucLab() {
        return glucLab;
    }

    /**
     * Sets the glucose laboratory value.
     * Represents blood glucose measurements from laboratory tests.
     *
     * @param glucLab String the glucose laboratory value
     */
    public void setGlucLab(String glucLab) {
        this.glucLab = glucLab;
    }

    /**
     * Gets the microalbumin value.
     * Microalbumin is an early indicator of kidney damage in diabetic patients.
     * It detects small amounts of protein in urine, indicating diabetic nephropathy.
     *
     * @return String the microalbumin value
     */
    public String getMicroalbumin() {
        return microalbumin;
    }

    /**
     * Sets the microalbumin value.
     * Microalbumin is an early marker of diabetic kidney disease.
     *
     * @param microalbumin String the microalbumin value
     */
    public void setMicroalbumin(String microalbumin) {
        this.microalbumin = microalbumin;
    }

    /**
     * Gets the kidney function assessment.
     * Represents overall kidney function status, often including eGFR (estimated
     * Glomerular Filtration Rate) and other renal function indicators.
     *
     * @return String the kidney function assessment
     */
    public String getKidneyfunction() {
        return kidneyfunction;
    }

    /**
     * Sets the kidney function assessment.
     * Represents overall kidney function status and renal health indicators.
     *
     * @param kidneyfunction String the kidney function assessment
     */
    public void setKidneyfunction(String kidneyfunction) {
        this.kidneyfunction = kidneyfunction;
    }
}
