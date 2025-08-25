//CHECKSTYLE:OFF
package org.oscarehr.common.interfaces;

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


import java.util.Date;


public interface Immunization {
    enum ImmunizationProperty {lot, location, route, dose, comments, neverReason, manufacture, name, expiryDate, providerName, brandSnomedId}

    int getImmunizationId();

    /**
     * Get an immunization data value by ImmunizationProperty key
     */
    String getImmunizationProperty(ImmunizationProperty immunizationProperty);

    boolean isImmunization();

    Integer getDemographicId();

    boolean isComplete();

    String getLotNo();

    void setLotNo(String lotNo);

    String getSite();

    void setSite(String site);

    String getRoute();

    void setRoute(String route);

    String getDose();

    void setDose(String dose);

    String getComment();

    void setComment(String comment);

    void setImmunizationRefused(boolean refused);

    boolean getImmunizationRefused();

    String getImmunizationRefusedReason();

    void setImmunizationRefusedReason(String reason);

    String getManufacture();

    void setManufacture(String manufacture);

    String getName();

    void setName(String name);

    String getImmunizationType();

    void setImmunizationType(String immunizationType);

    Date getImmunizationDate();

    void setImmunizationDate(Date immunizationDate);

    /**
     * The expire date of this immunization.
     * Returns NULL if no date is available.
     * Returns a Date object parsed from a string source.
     */
    Date getExpiryDate();

    /**
     * Set the expiry date of this immunization
     * Sets an empty string field if the parameter is null.
     */
    void setExpiryDate(Date expiryDate);

    /**
     * Fixed to SNOMED codes only
     */
    String getVaccineCode();

    /**
     * Use SNOMED codes only
     */
    void setVaccineCode(String vaccineCode);

    /**
     * True if the Immunization was administered by this clinician at this clinic.
     * For now this is hard coded to True as there is no way in Oscar to determine this.
     */
    boolean isPrimarySource();

    void setPrimarySource(boolean truefalse);

    /**
     * This is the name of an external provider that administered the immunization.
     */
    String getProviderName();

    void setProviderName(String providerName);

    /**
     * Fixed to SNOMED codes only
     */
    String getVaccineCode2();

    /**
     * Use SNOMED codes only
     */
    void setVaccineCode2(String vaccineCode);

    /**
     * This method subtracts the date of immunization from the current date and compares
     * the number of days given in the parameter.
     * <p>
     * In some cases it is assumed that an immunization being submitted to an authority after 14
     * days from the date of immunization is (was) completed externally.
     * <p>
     * ie: [submission date] – [immunization date] > 14 days (2 weeks)
     */
    boolean isHistorical(int days);
}
