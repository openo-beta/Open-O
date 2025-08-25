//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * <p>
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 * <p>
 * Modifications made by Magenta Health in 2024.
 */
package org.oscarehr.common.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.oscarehr.common.NativeSql;
import org.oscarehr.common.model.Drug;
import org.oscarehr.util.MiscUtils;

public interface DrugDao extends AbstractDao<Drug> {

    boolean addNewDrug(Drug d);

    List<Drug> findByPrescriptionId(Integer prescriptionId);

    List<Drug> findByDemographicId(Integer demographicId);

    List<Drug> findByDemographicId(Integer demographicId, Boolean archived);

    List<Drug> findByScriptNo(Integer scriptNo, Boolean archived);

    List<Drug> findByDemographicIdOrderByDate(Integer demographicId, Boolean archived);

    List<Drug> findByDemographicIdOrderByPositionForExport(Integer demographicId, Boolean archived);

    List<Drug> findByDemographicIdOrderByPosition(Integer demographicId, Boolean archived);

    List<Drug> findByDemographicIdSimilarDrugOrderByDate(Integer demographicId, String regionalIdentifier,
                                                         String customName);

    List<Drug> findByDemographicIdSimilarDrugOrderByDate(Integer demographicId, String regionalIdentifier,
                                                         String customName, String brandName);

    List<Drug> findByDemographicIdSimilarDrugOrderByDate(Integer demographicId, String regionalIdentifier,
                                                         String customName, String brandName, String atc);

    List<Drug> getUniquePrescriptions(String demographic_no);

    List<Drug> getPrescriptions(String demographic_no);

    List<Drug> getPrescriptions(String demographic_no, boolean all);

    int getNumberOfDemographicsWithRxForProvider(String providerNo, Date startDate, Date endDate,
                                                 boolean distinct);

    List<Drug> findByDemographicIdUpdatedAfterDate(Integer demographicId, Date updatedAfterThisDate);

    List<Drug> findByAtc(String atc);

    List<Drug> findByAtc(List<String> atc);

    List<Drug> findByDemographicIdAndAtc(int demographicNo, String atc);

    List<Drug> findByDemographicIdAndRegion(int demographicNo, String regionalIdentifier);

    List<Drug> findByDemographicIdAndDrugId(int demographicNo, Integer drugId);

    List<Object[]> findDrugsAndPrescriptions(int demographicNo);

    List<Object[]> findDrugsAndPrescriptionsByScriptNumber(int scriptNumber);

    int getMaxPosition(int demographicNo);

    Drug findByEverything(String providerNo, int demographicNo, Date rxDate, Date endDate, Date writtenDate,
                          String brandName, int gcn_SEQNO, String customName, float takeMin, float takeMax, String frequencyCode,
                          String duration, String durationUnit, String quantity, String unitName, int repeat, Date lastRefillDate,
                          boolean nosubs, boolean prn, String escapedSpecial, String outsideProviderName, String outsideProviderOhip,
                          boolean customInstr, Boolean longTerm, boolean customNote, Boolean pastMed,
                          Boolean patientCompliance, String specialInstruction, String comment, boolean startDateUnknown);

    List<Object[]> findByParameter(String parameter, String value);

    List<Drug> findByRegionBrandDemographicAndProvider(String regionalIdentifier, String brandName,
                                                       int demographicNo, String providerNo);

    Drug findByBrandNameDemographicAndProvider(String brandName, int demographicNo, String providerNo);

    Drug findByCustomNameDemographicIdAndProviderNo(String customName, int demographicNo, String providerNo);

    Integer findLastNotArchivedId(String brandName, String genericName, int demographicNo);

    Drug findByDemographicIdRegionalIdentifierAndAtcCode(String atcCode, String regionalIdentifier,
                                                         int demographicNo);

    List<String> findSpecialInstructions();

    List<Integer> findDemographicIdsUpdatedAfterDate(Date updatedAfterThisDate);

    List<Integer> findNewDrugsSinceDemoKey(String keyName);

    List<Drug> findLongTermDrugsByDemographic(Integer demographicId);

}
