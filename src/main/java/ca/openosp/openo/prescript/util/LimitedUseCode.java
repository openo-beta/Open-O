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


package ca.openosp.openo.prescript.util;

/**
 * Represents a Limited Use Code (LUC) for pharmaceutical coverage in Ontario.
 * Limited Use Codes are specific criteria that must be met for certain medications
 * to be covered under the Ontario Drug Benefit (ODB) formulary.
 *
 * <p>This class encapsulates the structure of limited use criteria including
 * the sequence, type, descriptive text, and usage identifier. These codes are
 * used in the prescription system to determine drug coverage eligibility and
 * provide clinical guidance for appropriate prescribing.</p>
 *
 * @since 2007-04-16
 */
public class LimitedUseCode {

    /**
     * The sequence number of this limited use code.
     * Used for ordering multiple criteria for the same medication.
     */
    private String seq;

    /**
     * The type classification of this limited use code.
     * Indicates the category or nature of the restriction.
     */
    private String type;

    /**
     * The descriptive text explaining the limited use criteria.
     * Contains the specific clinical or administrative requirements.
     */
    private String txt;

    /**
     * The unique identifier for the reason this limited use applies.
     * Links to specific indication or condition codes.
     */
    private String useId;

    /**
     * Gets the sequence number of this limited use code.
     *
     * @return String the sequence number, or null if not set
     */
    public String getSeq() {
        return seq;
    }

    /**
     * Sets the sequence number of this limited use code.
     *
     * @param seq String the sequence number to set
     */
    public void setSeq(String seq) {
        this.seq = seq;
    }

    /**
     * Gets the type classification of this limited use code.
     *
     * @return String the type classification, or null if not set
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type classification of this limited use code.
     *
     * @param type String the type classification to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the descriptive text explaining the limited use criteria.
     *
     * @return String the descriptive text, or null if not set
     */
    public String getTxt() {
        return txt;
    }

    /**
     * Sets the descriptive text explaining the limited use criteria.
     *
     * @param txt String the descriptive text to set
     */
    public void setTxt(String txt) {
        this.txt = txt;
    }

    /**
     * Gets the unique identifier for the reason this limited use applies.
     *
     * @return String the usage identifier, or null if not set
     */
    public String getUseId() {
        return useId;
    }

    /**
     * Sets the unique identifier for the reason this limited use applies.
     *
     * @param useId String the usage identifier to set
     */
    public void setUseId(String useId) {
        this.useId = useId;
    }

}
