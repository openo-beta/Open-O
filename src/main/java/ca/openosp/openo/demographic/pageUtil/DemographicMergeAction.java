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

import ca.openosp.openo.demographic.merge.DemographicMergeManager;
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
 * Routes both merge and unmerge operations through a single {@code execute()} method,
 * using the {@code method} request parameter to distinguish them. Delegates all
 * business logic to {@link DemographicMergeManager}.
 * <p>
 * Requires {@code _demographic} write privilege. Throws {@link SecurityException}
 * if the logged-in provider lacks this privilege.
 *
 * @since 2026-03-25
 */
public class DemographicMergeAction extends ActionSupport {

    private static final Logger logger = MiscUtils.getLogger();

    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private DemographicMergeManager demographicMergeManager = SpringUtils.getBean(DemographicMergeManager.class);

    /**
     * Routes to merge or unmerge based on the {@code method} request parameter.
     *
     * @return String Struts2 result name: {@code "success"}, {@code "successUnMerge"}, or {@code "failure"}
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
        }

        return "failure";
    }

    /**
     * Handles the merge operation.
     * <p>
     * Reads {@code primaryDemographicNo} and {@code secondaryDemographicNo[]} from the
     * request, validates both, then delegates to {@link DemographicMergeManager#merge}.
     *
     * @param loggedInInfo LoggedInInfo the authenticated provider
     * @return String {@code "success"} on success, {@code "failure"} on validation error or exception
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

            demographicMergeManager.merge(loggedInInfo, primaryNo, secondaryNos);
            return "success";
        } catch (Exception e) {
            logger.error("DemographicMergeAction.doMerge: merge failed", e);
            return "failure";
        }
    }

    /**
     * Handles the unmerge operation.
     * <p>
     * Reads {@code mergedDemographicNo} from the request, validates it, then delegates
     * to {@link DemographicMergeManager#unmerge}.
     *
     * @param loggedInInfo LoggedInInfo the authenticated provider
     * @return String {@code "successUnMerge"} on success, {@code "failure"} on validation error or exception
     */
    private String doUnmerge(LoggedInInfo loggedInInfo) {
        try {
            Integer mergedNo = Integer.parseInt(request.getParameter("mergedDemographicNo"));
            demographicMergeManager.unmerge(loggedInInfo, mergedNo);
            return "successUnMerge";
        } catch (Exception e) {
            logger.error("DemographicMergeAction.doUnmerge: unmerge failed", e);
            return "failure";
        }
    }
}
