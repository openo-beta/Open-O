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

package org.oscarehr.managers;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oscarehr.common.dao.PreventionDao;
import org.oscarehr.common.dao.PreventionExtDao;
import org.oscarehr.common.dao.PropertyDao;
import org.oscarehr.common.interfaces.Immunization.ImmunizationProperty;
import org.oscarehr.common.model.Prevention;
import org.oscarehr.common.model.PreventionExt;
import org.oscarehr.common.model.Property;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oscar.log.LogAction;
import oscar.oscarPrevention.PreventionDS;
import oscar.oscarPrevention.PreventionData;
import oscar.oscarPrevention.PreventionDisplayConfig;
import oscar.util.StringUtils;

public interface PreventionManager {

    List<Prevention> getUpdatedAfterDate(LoggedInInfo loggedInInfo, Date updatedAfterThisDateExclusive,
                                         int itemsToReturn);

    List<Prevention> getByDemographicIdUpdatedAfterDate(LoggedInInfo loggedInInfo, Integer demographicId,
                                                        Date updatedAfterThisDateExclusive);

    Prevention getPrevention(LoggedInInfo loggedInInfo, Integer id);

    List<PreventionExt> getPreventionExtByPrevention(LoggedInInfo loggedInInfo, Integer preventionId);

    ArrayList<String> getPreventionTypeList();

    ArrayList<HashMap<String, String>> getPreventionTypeDescList();

    boolean hideItem(String item);

    void addCustomPreventionItems(String items);

    void addPreventionWithExts(Prevention prevention, HashMap<String, String> exts);

    List<Prevention> getPreventionsByProgramProviderDemographicDate(LoggedInInfo loggedInInfo, Integer programId,
                                                                    String providerNo, Integer demographicId, Calendar updatedAfterThisDateExclusive, int itemsToReturn);

    List<Prevention> getPreventionsByDemographicNo(LoggedInInfo loggedInInfo, Integer demographicNo);

    String getWarnings(LoggedInInfo loggedInInfo, String demo);

    String checkNames(String k);

    boolean isDisabled();

    boolean isCreated();

    Set<String> getPreventionStopSigns();

    boolean isPrevDisabled(String name);

    List<String> getDisabledPreventions();

    boolean isHidePrevItemExist();

    boolean setDisabledPreventions(List<String> newDisabledPreventions);

    List<Prevention> getImmunizationsByDemographic(LoggedInInfo loggedInInfo, Integer demographicNo);

    String getCustomPreventionItems();

}
