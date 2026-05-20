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
 * Requires both {@code _demographic} and {@code _admin} write privileges.
 * Throws {@link SecurityException} if the logged-in provider lacks either.
 *
 * @since 2026-03-25
 */
public class DemographicMergeAction extends ActionSupport {

    private static final Logger logger = MiscUtils.getLogger();

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_OFFSET = 0;

    /** Demographic number of the newly created merged record — used in the success redirect. */
    private Integer mergedDemoNo;

    public Integer getMergedDemoNo() { return mergedDemoNo; }

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

        checkPrivilege(loggedInInfo);

        String mtd = request.getParameter("method");

        if ("merge".equals(mtd)) {
            return doMerge(loggedInInfo);
        } else if ("unmerge".equals(mtd)) {
            return doUnmerge(loggedInInfo);
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
        String mergedDemoNoParam = request.getParameter("mergedDemoNo");

        if (searchMode == null) searchMode = "search_name";
        if (mode == null) mode = "merge";

        int offset = DEFAULT_OFFSET;
        int limit = DEFAULT_LIMIT;
        try {
            String p1 = request.getParameter("limit1");
            String p2 = request.getParameter("limit2");
            if (p1 != null) offset = Integer.parseInt(p1);
            if (p2 != null) limit  = Integer.parseInt(p2);
        } catch (NumberFormatException ignored) {}

        boolean unmerge = "unmerge".equals(mode);

        // Search whenever the form has been submitted (keyword may be blank = search all)
        List<Demographic> demoList = null;
        if (keyword != null) {
            try {
                String kw = keyword.trim();
                if (unmerge) {
                    demoList = demographicManager.searchMergedDemographicsForUnmerge(loggedInInfo, kw, searchMode, limit, offset);
                } else {
                    demoList = demographicManager.searchDemographicsForMerge(loggedInInfo, kw, searchMode, limit, offset);
                }
            } catch (Exception e) {
                logger.error("DemographicMergeAction.doSearch: search failed", e);
            }
        }

        // In unmerge mode, load merge events and source demographics for each result
        // so the JSP accordion can show which records were merged into each C record.
        if (unmerge && demoList != null && !demoList.isEmpty()) {
            List<Integer> mergedNos = new ArrayList<>();
            for (Demographic demo : demoList) mergedNos.add(demo.getDemographicNo());
            request.setAttribute("mergeEventMap", mergeManager.findMergeEventsForDemographics(loggedInInfo, mergedNos));
            request.setAttribute("mergeSourcesMap", mergeManager.findMergeSourcesForDemographics(loggedInInfo, mergedNos));
        }

        request.setAttribute("demoList", demoList);
        request.setAttribute("keyword", keyword);
        request.setAttribute("searchMode", searchMode);
        request.setAttribute("mode", mode);
        request.setAttribute("outcome", outcome);
        request.setAttribute("mergedDemoNo", mergedDemoNoParam);
        request.setAttribute("offset", offset);
        request.setAttribute("limit", limit);
        request.setAttribute("unmergeMode", unmerge);
        request.setAttribute("resultCount", demoList != null ? demoList.size() : 0);

        return "search";
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

            mergedDemoNo = mergeManager.merge(loggedInInfo, primaryNo, secondaryNos);
            return "success";
        } catch (Exception e) {
            logger.error("DemographicMergeAction.doMerge: merge failed", e);
            return "failure";
        }
    }

    /**
     * Enforces that the logged-in provider holds both {@code _demographic w} and
     * {@code _admin w} privileges, mirroring the gate applied by the JSP.
     *
     * @param loggedInInfo LoggedInInfo the authenticated provider
     * @throws SecurityException if either privilege is missing
     */
    private void checkPrivilege(LoggedInInfo loggedInInfo) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_demographic", "w", null)) {
            throw new SecurityException("missing required sec object (_demographic)");
        }
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", "w", null)) {
            throw new SecurityException("missing required sec object (_admin)");
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
            return "failureUnMerge";
        }
    }

}
