//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
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
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package org.oscarehr.common.dao;

import java.util.*;

import javax.persistence.Query;

import org.oscarehr.common.model.DemographicExt;
import org.oscarehr.common.model.enumerator.DemographicExtKey;
import org.springframework.stereotype.Repository;

public interface DemographicExtDao extends AbstractDao<DemographicExt> {

    DemographicExt getDemographicExt(Integer id);

    List<DemographicExt> getDemographicExtByDemographicNo(Integer demographicNo);

    DemographicExt getDemographicExt(Integer demographicNo, DemographicExtKey demographicExtKey);

    DemographicExt getDemographicExt(Integer demographicNo, String key);

    List<DemographicExt> getDemographicExtByKeyAndValue(DemographicExtKey demographicExtKey, String value);

    List<DemographicExt> getDemographicExtByKeyAndValue(String key, String value);

    DemographicExt getLatestDemographicExt(Integer demographicNo, String key);

    void updateDemographicExt(DemographicExt de);

    void saveDemographicExt(Integer demographicNo, String key, String value);

    void removeDemographicExt(Integer id);

    void removeDemographicExt(Integer demographicNo, String key);

    Map<String, String> getAllValuesForDemo(Integer demo);

    void addKey(String providerNo, Integer demo, String key, String value);

    void addKey(String providerNo, Integer demo, String key, String newValue, String oldValue);

    List<String[]> getListOfValuesForDemo(Integer demo);

    String getValueForDemoKey(Integer demo, String key);

    List<Integer> findDemographicIdsByKeyVal(DemographicExtKey demographicExtKey, String val);

    List<Integer> findDemographicIdsByKeyVal(String key, String val);

    List<DemographicExt> getMultipleDemographicExtKeyForDemographicNumbersByProviderNumber(
        final DemographicExtKey demographicExtKey,
        final Collection<Integer> demographicNumbers,
        final String midwifeNumber);

    List<Integer> getDemographicNumbersByDemographicExtKeyAndProviderNumberAndDemographicLastNameRegex(
        final DemographicExtKey key,
        final String providerNumber,
        final String lastNameRegex);

}
