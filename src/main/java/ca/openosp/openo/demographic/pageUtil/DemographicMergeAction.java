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
package ca.openosp.openo.demographic.pageUtil;

import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.demographic.merge.DemographicMergeManager;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Struts2 action for the demographic merge/unmerge workflow.
 * <p>
 * Handles all operations — search display, primary selection, merge, and unmerge —
 * routing via the {@code method} request parameter. Display methods populate request
 * attributes consumed by the JSPs (no server-side logic in JSPs).
 * Delegates all business logic to {@link DemographicMergeManager} and
 * {@link DemographicManager}.
 * <p>
 * Requires {@code _demographic} write privilege. Throws {@link SecurityException}
 * if the logged-in provider lacks this privilege.
 *
 * @since 2026-03-25
 */
public class DemographicMergeAction extends ActionSupport {

    private static final Logger logger = MiscUtils.getLogger();

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_OFFSET = 0;

    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private DemographicMergeManager mergeManager = SpringUtils.getBean(DemographicMergeManager.class);
    private DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);

    /**
     * Entry point — routes by {@code method} parameter; defaults to the search/display page.
     *
     * @return String Struts2 result name
     */
    @Override
    public String execute() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_demographic", "w", null)) {
            throw new SecurityException("missing required sec object (_demographic)");
        }

        String mtd = request.getParameter("method");

        if ("merge".equals(mtd)) {
            return doMerge(loggedInInfo);
        } else if ("unmerge".equals(mtd)) {
            return doUnmerge(loggedInInfo);
        } else if ("selectPrimary".equals(mtd)) {
            return doSelectPrimary(loggedInInfo);
        }

        return doSearch(loggedInInfo);
    }

    // -------------------------------------------------------------------------
    // Display methods
    // -------------------------------------------------------------------------

    /**
     * Runs the patient search and populates request attributes for
     * {@code demographicMergeRecord.jsp}.
     *
     * @param loggedInInfo LoggedInInfo the authenticated provider
     * @return String {@code "search"}
     */
    private String doSearch(LoggedInInfo loggedInInfo) {
        String keyword = request.getParameter("keyword");
        String searchMode = request.getParameter("search_mode");
        String mode = request.getParameter("mode");
        String outcome = request.getParameter("outcome");

        if (searchMode == null) searchMode = "search_name";
        if (mode == null) mode = "merge";

        int offset = DEFAULT_OFFSET;
        int limit = DEFAULT_LIMIT;
        boolean unmerge = "unmerge".equals(mode);

        List<Demographic> demoList = null;
        if (keyword != null && !keyword.trim().isEmpty()) {
            try {
                if (unmerge) {
                    demoList = demographicManager.searchMergedDemographicsForUnmerge(loggedInInfo, keyword, searchMode, limit, offset);
                } else {
                    demoList = demographicManager.searchDemographicsForMerge(loggedInInfo, keyword, searchMode, limit, offset);
                }
            } catch (Exception e) {
                logger.error("DemographicMergeAction.doSearch: search failed", e);
            }
        }

        request.setAttribute("demoList", demoList);
        request.setAttribute("keyword", keyword);
        request.setAttribute("searchMode", searchMode);
        request.setAttribute("mode", mode);
        request.setAttribute("outcome", outcome);
        request.setAttribute("offset", offset);
        request.setAttribute("limit", limit);
        request.setAttribute("unmergeMode", unmerge);
        request.setAttribute("resultCount", demoList != null ? demoList.size() : 0);

        return "search";
    }

    /**
     * Loads the selected demographics and populates request attributes for
     * {@code demographicMergePrimarySelect.jsp}.
     *
     * @param loggedInInfo LoggedInInfo the authenticated provider
     * @return String {@code "selectPrimary"} on success, {@code "search"} if fewer than 2 IDs supplied
     */
    private String doSelectPrimary(LoggedInInfo loggedInInfo) {
        String[] selectedParams = request.getParameterValues("demographicNo");

        if (selectedParams == null || selectedParams.length < 2) {
            logger.warn("DemographicMergeAction.doSelectPrimary: fewer than 2 demographicNo values received");
            return doSearch(loggedInInfo);
        }

        List<Integer> selectedIds = new ArrayList<>();
        for (String s : selectedParams) {
            try { selectedIds.add(Integer.parseInt(s)); } catch (NumberFormatException ignored) {}
        }

        if (selectedIds.size() < 2) {
            return doSearch(loggedInInfo);
        }

        List<Demographic> demographics = demographicManager.getDemographics(loggedInInfo, selectedIds);
        request.setAttribute("demographics", demographics);
        request.setAttribute("selectedIds", selectedIds);

        return "selectPrimary";
    }

    // -------------------------------------------------------------------------
    // Operation methods
    // -------------------------------------------------------------------------

    /**
     * Performs the merge and returns a result for redirect.
     *
     * @param loggedInInfo LoggedInInfo the authenticated provider
     * @return String {@code "success"} or {@code "failure"}
     */
    private String doMerge(LoggedInInfo loggedInInfo) {
        try {
            Integer primaryNo = Integer.parseInt(request.getParameter("primaryDemographicNo"));
            String[] secondaryParams = request.getParameterValues("secondaryDemographicNo");

            if (secondaryParams == null || secondaryParams.length == 0) {
                logger.warn("DemographicMergeAction.doMerge: secondaryDemographicNo is missing or empty");
                return "failure";
            }

            List<Integer> secondaryNos = new ArrayList<>();
            for (String s : secondaryParams) {
                secondaryNos.add(Integer.parseInt(s));
            }

            mergeManager.merge(loggedInInfo, primaryNo, secondaryNos);
            return "success";
        } catch (Exception e) {
            logger.error("DemographicMergeAction.doMerge: merge failed", e);
            return "failure";
        }
    }

    /**
     * Performs the unmerge and returns a result for redirect.
     *
     * @param loggedInInfo LoggedInInfo the authenticated provider
     * @return String {@code "successUnMerge"} or {@code "failure"}
     */
    private String doUnmerge(LoggedInInfo loggedInInfo) {
        try {
            Integer mergedNo = Integer.parseInt(request.getParameter("mergedDemographicNo"));
            mergeManager.unmerge(loggedInInfo, mergedNo);
            return "successUnMerge";
        } catch (Exception e) {
            logger.error("DemographicMergeAction.doUnmerge: unmerge failed", e);
            return "failure";
        }
    }

}
