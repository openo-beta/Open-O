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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.oscarehr.PMmodule.dao.ProgramAccessDAO;
import org.oscarehr.PMmodule.dao.ProgramProviderDAO;
import org.oscarehr.PMmodule.dao.ProviderDao;
import org.oscarehr.PMmodule.model.ProgramAccess;
import org.oscarehr.PMmodule.model.ProgramProvider;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.casemgmt.service.CaseManagementManager;
import org.oscarehr.common.dao.ClinicDAO;
import org.oscarehr.common.dao.CustomFilterDao;
import org.oscarehr.common.dao.TicklerCategoryDao;
import org.oscarehr.common.dao.TicklerCommentDao;
import org.oscarehr.common.dao.TicklerDao;
import org.oscarehr.common.dao.TicklerLinkDao;
import org.oscarehr.common.dao.TicklerTextSuggestDao;
import org.oscarehr.common.dao.TicklerUpdateDao;
import org.oscarehr.common.model.Clinic;
import org.oscarehr.common.model.CustomFilter;
import org.oscarehr.common.model.Tickler;
import org.oscarehr.common.model.TicklerCategory;
import org.oscarehr.common.model.TicklerComment;
import org.oscarehr.common.model.TicklerLink;
import org.oscarehr.common.model.TicklerTextSuggest;
import org.oscarehr.common.model.TicklerUpdate;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;
import org.oscarehr.util.VelocityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

import oscar.OscarProperties;
import oscar.log.LogAction;

import com.quatro.model.security.Secrole;

public interface TicklerManager {

    String DEMOGRAPHIC_NAME = "demographic_name";
    String CREATOR = "creator";
    String SERVICE_DATE = "service_date";
    String CREATION_DATE = "creation_date";
    String PRIORITY = "priority";
    String TASK_ASSIGNED_TO = "task_assigned_to";
    String STATUS = "status";
    String SORT_ASC = "asc";
    String SORT_DESC = "desc";

    List<TicklerCategory> getActiveTicklerCategories(LoggedInInfo loggedInInfo);

    boolean validateTicklerIsValid(Tickler tickler);

    boolean addTicklerLink(LoggedInInfo loggedInInfo, TicklerLink ticklerLink);

    boolean addTickler(LoggedInInfo loggedInInfo, Tickler tickler);

    boolean updateTickler(LoggedInInfo loggedInInfo, Tickler tickler);

    List<Tickler> getTicklers(LoggedInInfo loggedInInfo, CustomFilter filter, String providerNo,
                              String programId);

    List<Tickler> getTicklers(LoggedInInfo loggedInInfo, CustomFilter filter);

    List<Tickler> getTicklers(LoggedInInfo loggedInInfo, CustomFilter filter, int offset, int limit);

    List<Tickler> getTicklerByLabId(LoggedInInfo loggedInInfo, int labId, Integer demoNo);

    List<Tickler> getTicklerByLabIdAnyProvider(LoggedInInfo loggedInInfo, int labId, Integer demoNo);

    List<Tickler> ticklerFacilityFiltering(LoggedInInfo loggedInInfo, List<Tickler> ticklers);

    List<Tickler> filterTicklersByAccess(List<Tickler> ticklers, String providerNo, String programNo);

    int getActiveTicklerCount(LoggedInInfo loggedInInfo, String providerNo);

    int getActiveTicklerByDemoCount(LoggedInInfo loggedInInfo, Integer demographicNo);

    int getNumTicklers(LoggedInInfo loggedInInfo, CustomFilter filter);

    Tickler getTickler(LoggedInInfo loggedInInfo, String tickler_no);

    Tickler getTickler(LoggedInInfo loggedInInfo, Integer id);

    void addComment(LoggedInInfo loggedInInfo, Integer tickler_id, String provider, String message);

    void reassign(LoggedInInfo loggedInInfo, Integer tickler_id, String provider, String task_assigned_to);

    void updateStatus(LoggedInInfo loggedInInfo, Integer tickler_id, String provider, Tickler.STATUS status);

    void sendNotification(LoggedInInfo loggedInInfo, Tickler t) throws IOException;

    void completeTickler(LoggedInInfo loggedInInfo, Integer tickler_id, String provider);

    void deleteTickler(LoggedInInfo loggedInInfo, Integer tickler_id, String provider);

    void activateTickler(LoggedInInfo loggedInInfo, Integer tickler_id, String provider);

    void resolveTicklersBySubstring(LoggedInInfo loggedInInfo, String providerNo, List<String> demographicIds,
                                    String remString);

    List<CustomFilter> getCustomFilters();

    List<CustomFilter> getCustomFilters(String provider_no);

    List<CustomFilter> getCustomFilterWithShortCut(String providerNo);

    CustomFilter getCustomFilter(String name);

    CustomFilter getCustomFilter(String name, String providerNo);

    CustomFilter getCustomFilterById(Integer id);

    void saveCustomFilter(CustomFilter filter);

    void deleteCustomFilter(String name);

    void deleteCustomFilterById(Integer id);

    void addTickler(String demographic_no, String message, Tickler.STATUS status, String service_date,
                    String creator, Tickler.PRIORITY priority, String task_assigned_to);

    boolean hasTickler(String demographic, String task_assigned_to, String message);

    void createTickler(String demoNo, String provNo, String message, String assignedTo);

    void resolveTicklers(LoggedInInfo loggedInInfo, String providerNo, List<String> cdmPatientNos,
                         String remString);

    List<Tickler> listTicklers(LoggedInInfo loggedInInfo, Integer demographicNo, Date beginDate, Date endDate);

    List<Tickler> findActiveByDemographicNo(LoggedInInfo loggedInInfo, Integer demographicNo);

    List<Tickler> search_tickler_bydemo(LoggedInInfo loggedInInfo, Integer demographicNo, String status,
                                        Date beginDate, Date endDate);

    List<Tickler> search_tickler(LoggedInInfo loggedInInfo, Integer demographicNo, Date endDate);

    List<TicklerTextSuggest> getActiveTextSuggestions(LoggedInInfo loggedInInfo);

    List<TicklerTextSuggest> getAllTextSuggestions(LoggedInInfo loggedInInfo, int offset, int itemsToReturn);

    List<Tickler> sortTicklerList(Boolean isSortAscending, String sortColumn, List<Tickler> ticklers);
}
