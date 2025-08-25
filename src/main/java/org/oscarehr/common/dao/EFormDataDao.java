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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.model.EFormData;
import org.oscarehr.util.MiscUtils;
import org.springframework.stereotype.Repository;

public interface EFormDataDao extends AbstractDao<EFormData> {

    String SORT_NAME = "form_name";
    String SORT_SUBJECT = "subject";

    List<EFormData> findByDemographicId(Integer demographicId);

    List<EFormData> findByDemographicIdSinceLastDate(Integer demographicId, Date lastDate);

    List<Integer> findDemographicIdSinceLastDate(Date lastDate);

    EFormData findByFormDataId(Integer formDataId);

    List<EFormData> findByDemographicIdCurrent(Integer demographicId, Boolean current);

    List<EFormData> findByDemographicIdCurrent(Integer demographicId, Boolean current, int startIndex,
                                               int numToReturn);

    List<EFormData> findByDemographicIdCurrentAttachedToConsult(String consultationId);

    List<EFormData> findByDemographicIdCurrentAttachedToEForm(String fdid);

    List<EFormData> findByDemographicIdCurrent(Integer demographicId, Boolean current, int startIndex,
                                               int numToReturn, String sortBy);

    List<Map<String, Object>> findByDemographicIdCurrentNoData(Integer demographicId, Boolean current);

    List<EFormData> findPatientIndependent(Boolean current);

    List<EFormData> findByFormId(Integer formId);

    List<Integer> findDemographicNosByFormId(Integer formId);

    List<Integer> findAllFdidByFormId(Integer formId);

    List<Object[]> findMetaFieldsByFormId(Integer formId);

    List<Integer> findAllCurrentFdidByFormId(Integer formId);

    List<EFormData> findByFormIdProviderNo(List<String> providerNo, Integer formId);

    List<EFormData> findByDemographicIdAndFormName(Integer demographicNo, String formName);

    List<EFormData> findByDemographicIdAndFormId(Integer demographicNo, Integer fid);

    List<EFormData> findByFidsAndDates(TreeSet<Integer> fids, Date dateStart, Date dateEnd);

    List<EFormData> findByFdids(List<Integer> ids);

    boolean isLatestShowLatestFormOnlyPatientForm(Integer fdid);

    List<EFormData> getFormsSameFidSamePatient(Integer fdid);

    List<Integer> findemographicIdSinceLastDate(Date lastDate);

    List<EFormData> findInGroups(Boolean status, int demographicNo, String groupName, String sortBy, int offset,
                                 int numToReturn, List<String> eformPerms);

    Integer getLatestFdid(Integer fid, Integer demographicNo);

    List<Integer> getDemographicNosMissingVarName(int fid, String varName);

    List<String> getProvidersForEforms(Collection<Integer> fdidList);

    Date getLatestFormDateAndTimeForEforms(Collection<Integer> fdidList);

}
