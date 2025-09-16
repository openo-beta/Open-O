//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.hospitalReportManager;

import javax.servlet.http.HttpServletRequest;

import ca.openosp.openo.hospitalReportManager.dao.HRMDocumentToProviderDao;
import ca.openosp.openo.hospitalReportManager.model.HRMDocumentToProvider;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class HRMDisplayReport2Action extends ActionSupport {

    // --- Request params (must have getters/setters for Struts to inject) ---
    private Integer id;               // reportId / document id
    private Integer segmentID;        // segment id
    private String providerNo;        // provider number
    private String searchProviderNo;  // search provider number
    private String status;            // report status
    private String demoName;          // patient / demo name
    private String duplicateLabIds;   // duplicate ids if any
    private boolean listView;       // flag if rendering list view

    // getters/setters
    public void setId(Integer id) { this.id = id; }
    public Integer getId() { return id; }

    public void setSegmentID(Integer segmentID) { this.segmentID = segmentID; }
    public Integer getSegmentID() { return segmentID; }

    public void setProviderNo(String providerNo) { this.providerNo = providerNo; }
    public String getProviderNo() { return providerNo; }

    public void setSearchProviderNo(String searchProviderNo) { this.searchProviderNo = searchProviderNo; }
    public String getSearchProviderNo() { return searchProviderNo; }

    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }

    public void setDemoName(String demoName) { this.demoName = demoName; }
    public String getDemoName() { return demoName; }

    public void setDuplicateLabIds(String duplicateLabIds) { this.duplicateLabIds = duplicateLabIds; }
    public String getDuplicateLabIds() { return duplicateLabIds; }

    public void setListView(boolean listView) { this.listView = listView; }
    public boolean isListView() { return listView; }

    // --- Existing service/dao wiring ---
    private static HRMDocumentToProviderDao hrmDocumentToProviderDao =
            (HRMDocumentToProviderDao) SpringUtils.getBean(HRMDocumentToProviderDao.class);
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    @Override
    public String execute() {
        HttpServletRequest request = ServletActionContext.getRequest();

        // check privilege
        if (!securityInfoManager.hasPrivilege(
                LoggedInInfo.getLoggedInInfoFromSession(request),
                "_hrm", "r", null)) {
            throw new SecurityException("missing required sec object (_hrm)");
        }

        // At this point, all fields (id, providerNo, etc.) are already populated by Struts
        // You could pass them as request attributes for the JSP if needed
        request.setAttribute("id", id);
        request.setAttribute("segmentID", segmentID);
        request.setAttribute("providerNo", providerNo);
        request.setAttribute("searchProviderNo", searchProviderNo);
        request.setAttribute("status", status);
        request.setAttribute("demoName", demoName);
        request.setAttribute("duplicateLabIds", duplicateLabIds);
        request.setAttribute("isListView", listView);

        return "display";
    }

    public static HRMDocumentToProvider getHRMDocumentFromProvider(String providerNo, Integer hrmDocumentId) {
        return hrmDocumentToProviderDao.findByHrmDocumentIdAndProviderNo(hrmDocumentId, providerNo);
    }
}