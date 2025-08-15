/*
 *Copyright (c) 2023. Magenta Health Inc. All Rights Reserved.
 *
 *This software is published under the GPL GNU General Public License.
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 */
package org.oscarehr.inboxhub.display;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.oscarehr.inboxhub.inboxdata.LabDataController;
import org.oscarehr.inboxhub.query.InboxhubQuery;
import org.oscarehr.managers.SecurityInfoManager;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.SpringUtils;
import oscar.oscarLab.ca.on.LabResultData;
import oscar.oscarMDS.data.CategoryData;

public class ManageInboxhub2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();
    
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private InboxhubQuery query = new InboxhubQuery();

    public String execute() throws Exception {
        String method = request.getParameter("method");
        if ("displayInboxForm".equals(method)) {
            return displayInboxForm();
        } else if ("displayInboxList".equals(method)) {
            return displayInboxList();
        } else if ("displayInboxView".equals(method)) {
            return displayInboxView();
        }
        return displayInboxForm();
    }

    public String displayInboxForm() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", SecurityInfoManager.READ, null)) {
            return "unauthorized";
        }

        LabDataController labDataController = new LabDataController();
        String unclaimed = (String) request.getParameter("unclaimed");

        labDataController.setInboxFormQueryUnclaimed(query, unclaimed);

        labDataController.sanitizeInboxFormQuery(loggedInInfo, query, null, null);
        CategoryData categoryData = labDataController.getCategoryData(query);
        int[] totalCounts = labDataController.getTotalResultsCountBasedOnQuery(query, categoryData);

        request.setAttribute("totalDocsCount", totalCounts[0]);
        request.setAttribute("totalLabsCount", totalCounts[1]);
        request.setAttribute("totalHRMCount", totalCounts[2]);
        request.setAttribute("totalResultsCount", totalCounts[3]);
        request.setAttribute("categoryData", categoryData);
        return "success";
    }

    public String displayInboxList() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", SecurityInfoManager.READ, null)) {
            return "unauthorized";
        }

        fetchLabData(request);
        return "displayList";
    }

    public String displayInboxView() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", SecurityInfoManager.READ, null)) {
            return "unauthorized";
        }

        fetchLabData(request);
        return "displayView";
    }

    private void fetchLabData(HttpServletRequest request) {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        String page = request.getParameter("page");
        String pageSize = request.getParameter("pageSize");
        String demographicFilter = request.getParameter("demographicFilter");
        String typeFilter = request.getParameter("typeFilter");

        query.setPage(Integer.parseInt(page));
        query.setPageSize(Integer.parseInt(pageSize));

        LabDataController labDataController = new LabDataController();
        labDataController.sanitizeInboxFormQuery(loggedInInfo, query, demographicFilter, typeFilter);
        ArrayList<LabResultData> labDocs = labDataController.getLabData(loggedInInfo, query);
        if (labDocs.size() > 0) {
            String providerNo = request.getSession().getAttribute("user").toString();
            ArrayList<String> labLinks = labDataController.getLabLink(labDocs, query, request.getContextPath(), providerNo);
            request.setAttribute("labLinks", labLinks);
        }

        request.setAttribute("page", page);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("labDocs", labDocs);
        request.setAttribute("hasMoreData", labDocs.size() > 0);
        request.setAttribute("searchProviderNo", query.getSearchProviderNo());
    }

    public InboxhubQuery getQuery() {
        return query;
    }

    public void setQuery(InboxhubQuery query) {
        this.query = query;
    }
}