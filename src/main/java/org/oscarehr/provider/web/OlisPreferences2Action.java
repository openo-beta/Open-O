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


package org.oscarehr.provider.web;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.oscarehr.common.dao.UserPropertyDAO;
import org.oscarehr.common.model.UserProperty;
import org.oscarehr.olis.dao.OLISProviderPreferencesDao;
import org.oscarehr.olis.model.OLISProviderPreferences;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.SpringUtils;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.ActionContext;

public class OlisPreferences2Action extends ActionSupport {
    ActionContext context = ActionContext.getContext();
    HttpServletRequest request = (HttpServletRequest) context.get(ServletActionContext.HTTP_REQUEST);
    HttpServletResponse response = (HttpServletResponse) context.get(ServletActionContext.HTTP_RESPONSE);

    private UserPropertyDAO dao = (UserPropertyDAO) SpringUtils.getBean(UserPropertyDAO.class);
    private OLISProviderPreferencesDao olisProviderPreferencesDao = SpringUtils.getBean(OLISProviderPreferencesDao.class);

    @Override
    public String execute() {
        return view();
    }

    public String view() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        UserProperty prop = dao.getProp(providerNo, "olis_reportingLab");
        if (prop != null)
            request.setAttribute("reportingLaboratory", prop.getValue());

        prop = dao.getProp(providerNo, "olis_exreportingLab");
        if (prop != null)
            request.setAttribute("excludeReportingLaboratory", prop.getValue());

        prop = dao.getProp(providerNo, "olis_polling_frequency");
        if (prop != null)
            request.setAttribute("pollingFrequency", prop.getValue());

        OLISProviderPreferences olisPref = olisProviderPreferencesDao.findById(providerNo);
        if (olisPref != null) {
            request.setAttribute("olis_provider_start_time", olisPref.getStartTime());
        }

        return "form";
    }

    public String save() {
        String reportingLab = request.getParameter("reportingLaboratory");
        String excludeReportingLab = request.getParameter("excludeReportingLaboratory");
        String pollingFrequency = request.getParameter("pollingFrequency");
        String providerStartTime = request.getParameter("providerStartTime");
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();

        if (reportingLab != null) {
            UserProperty prop = dao.getProp(providerNo, "olis_reportingLab");
            if (prop == null) {
                prop = new UserProperty();
                prop.setName("olis_reportingLab");
                prop.setProviderNo(providerNo);
            }
            prop.setValue(reportingLab);
            dao.saveProp(prop);
        }

        if (excludeReportingLab != null) {
            UserProperty prop = dao.getProp(providerNo, "olis_exreportingLab");
            if (prop == null) {
                prop = new UserProperty();
                prop.setName("olis_exreportingLab");
                prop.setProviderNo(providerNo);
            }
            prop.setValue(excludeReportingLab);
            dao.saveProp(prop);
        }

        if (pollingFrequency != null) {
            UserProperty prop = dao.getProp(providerNo, "olis_polling_frequency");
            if (prop == null) {
                prop = new UserProperty();
                prop.setName("olis_polling_frequency");
                prop.setProviderNo(providerNo);
            }
            prop.setValue(pollingFrequency);
            dao.saveProp(prop);
        }

        if (providerStartTime != null) {
            //validate the time
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssZ");
            try {
                ZonedDateTime date = ZonedDateTime.parse(providerStartTime, inputFormatter);
                providerStartTime = date.format(outputFormatter);
                OLISProviderPreferences pref = olisProviderPreferencesDao.findById(providerNo);
                if (pref == null) {
                    pref = new OLISProviderPreferences();
                    pref.setProviderId(providerNo);
                    pref.setStartTime(providerStartTime);
                    olisProviderPreferencesDao.persist(pref);
                } else {
                    pref.setStartTime(providerStartTime);
                    olisProviderPreferencesDao.merge(pref);
                }

            } catch (RuntimeException e) {
                
            }
        }

        return view();
    }
}
