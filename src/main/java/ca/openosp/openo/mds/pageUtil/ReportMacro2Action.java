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
package ca.openosp.openo.mds.pageUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.TicklerDao;
import ca.openosp.openo.commn.dao.TicklerLinkDao;
import ca.openosp.openo.commn.dao.UserPropertyDAO;
import ca.openosp.openo.commn.model.Tickler;
import ca.openosp.openo.commn.model.TicklerLink;
import ca.openosp.openo.commn.model.UserProperty;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import ca.openosp.openo.lab.ca.on.CommonLabResultData;
import ca.openosp.openo.lab.ca.on.LabResultData;


import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for executing user-defined macros on laboratory reports in MDS system.
 * <p>
 * This action enables healthcare providers to create and execute custom macros that perform
 * multiple actions on laboratory reports simultaneously, improving workflow efficiency.
 * Macros can include automated acknowledgment with comments and tickler creation for
 * follow-up tasks, allowing providers to streamline repetitive laboratory review tasks.
 * <p>
 * Key functionality includes:
 * <ul>
 * <li>Security validation requiring lab write privileges</li>
 * <li>JSON-based macro definition processing</li>
 * <li>Automatic laboratory result acknowledgment with provider comments</li>
 * <li>Tickler creation for patient follow-up and task management</li>
 * <li>Integration with provider preference system</li>
 * <li>Support for provider-specific macro configurations</li>
 * </ul>
 * This action significantly enhances provider productivity by automating common
 * laboratory result processing workflows while maintaining audit trails.
 *
 * @since July 5, 2019
 */
public class ReportMacro2Action extends ActionSupport {
    /** HTTP request object for accessing parameters and session information */
    HttpServletRequest request = ServletActionContext.getRequest();
    /** HTTP response object for JSON output and response handling */
    HttpServletResponse response = ServletActionContext.getResponse();

    /** Logger for debugging and error tracking */
    private static Logger logger = MiscUtils.getLogger();

    /** Security manager for validating user access to laboratory functions */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    /** DAO for creating and managing tickler tasks */
    private TicklerDao ticklerDao = SpringUtils.getBean(TicklerDao.class);
    /** DAO for linking ticklers to laboratory results */
    private TicklerLinkDao ticklerLinkDao = SpringUtils.getBean(TicklerLinkDao.class);

    /**
     * Executes a user-defined macro on a laboratory report.
     * <p>
     * This method processes macro execution requests by:
     * <ul>
     * <li>Validating security privileges for lab write access</li>
     * <li>Retrieving the macro definition from user properties</li>
     * <li>Executing the macro actions (acknowledge, create ticklers)</li>
     * <li>Returning JSON response indicating success or failure</li>
     * </ul>
     *
     * @return String null (no view redirect, JSON response written directly)
     * @throws ServletException if servlet processing fails
     * @throws IOException if JSON response writing fails
     */
    public String execute() throws ServletException, IOException {
        JSONObject result = new JSONObject();

        // Validate user has laboratory write privileges for macro execution
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_lab", "w", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        // Extract macro name from request parameters
        String name = request.getParameter("name");

        logger.info("RunMacro called with name = " + name);

        // Validate that macro name was provided
        if (name == null) {
            result.put("success", false);
            result.put("error", "No macro name provided");
            result.write(response.getWriter());
            return null;
        }

        // Retrieve provider's macro definitions from user properties
        UserPropertyDAO upDao = SpringUtils.getBean(UserPropertyDAO.class);
        UserProperty up = upDao.getProp(LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo(), UserProperty.LAB_MACRO_JSON);

        boolean success = false;

        // Find and execute the specified macro from provider's definitions
        if (up != null && !StringUtils.isEmpty(up.getValue())) {
            JSONArray macros = (JSONArray) JSONSerializer.toJSON(up.getValue());
            if (macros != null) {
                // Search through provider's macro definitions for matching name
                for (int x = 0; x < macros.size(); x++) {
                    JSONObject macro = macros.getJSONObject(x);
                    if (name.equals(macro.getString("name"))) {
                        success = runMacro(macro, request);
                    }
                }
            }
        } else {
            // Handle case where no macros are defined for this provider
            result.put("success", false);
            result.put("error", "No macros defined in provider preferences");
            result.write(response.getWriter());
            return null;
        }


        // Return JSON response indicating macro execution result
        result.put("success", success);
        result.write(response.getWriter());
        return null;
    }

    /**
     * Executes the specified macro definition with its configured actions.
     * <p>
     * This method processes macro actions including:
     * <ul>
     * <li>Laboratory result acknowledgment with provider comments</li>
     * <li>Tickler creation for patient follow-up tasks</li>
     * <li>Integration with provider preference settings</li>
     * </ul>
     * The method supports multiple actions within a single macro execution,
     * allowing providers to streamline complex workflows.
     *
     * @param macro JSONObject the macro definition containing actions to execute
     * @param request HttpServletRequest the request containing laboratory parameters
     * @return boolean true if macro executed successfully, false otherwise
     */
    protected boolean runMacro(JSONObject macro, HttpServletRequest request) {
        logger.info("running macro " + macro.getString("name"));
        // Extract laboratory identification parameters from request
        String segmentID = request.getParameter("segmentID");
        String providerNo = request.getParameter("providerNo");
        String labType = request.getParameter("labType");
        String demographicNo = request.getParameter("demographicNo");

        // Process acknowledgment action if defined in macro
        if (macro.has("acknowledge")) {
            logger.info("Acknowledging lab " + labType + ":" + segmentID);
            JSONObject jAck = macro.getJSONObject("acknowledge");
            String comment = jAck.getString("comment");
            // Update lab report status to acknowledged with provider comment
            CommonLabResultData.updateReportStatus(Integer.parseInt(segmentID), providerNo, 'A', comment, labType, skipComment(LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo()));
        }
        // Process tickler creation action if defined in macro and demographic available
        if (macro.has("tickler") && !StringUtils.isEmpty(demographicNo)) {
            JSONObject jTickler = macro.getJSONObject("tickler");

            // Validate required tickler fields are present
            if (jTickler.has("taskAssignedTo") && jTickler.has("message")) {
                logger.info("Sending Tickler");
                // Create new tickler task for patient follow-up
                Tickler t = new Tickler();
                t.setTaskAssignedTo(jTickler.getString("taskAssignedTo"));
                t.setDemographicNo(Integer.parseInt(demographicNo));
                t.setMessage(jTickler.getString("message"));
                t.setCreator(LoggedInInfo.getLoggedInInfoFromSession(request).getLoggedInProviderNo());
                ticklerDao.persist(t);

                // Link tickler to laboratory result for traceability
                TicklerLink tl = new TicklerLink();
                tl.setTableId(Long.valueOf(segmentID));
                tl.setTableName(LabResultData.HL7TEXT);
                tl.setTicklerNo(t.getId());
                ticklerLinkDao.persist(tl);
            } else {
                logger.info("Cannot sent tickler. Not enough information in macro definition. Requires taskAssignedTo and message");
            }

        }

        // Return success - macro execution completed
        return true;
    }

    /**
     * Determines whether to skip comment prompts for laboratory acknowledgment.
     * <p>
     * This method checks provider preferences to determine if the provider has
     * configured automatic comment handling for laboratory acknowledgments.
     *
     * @param providerNo String the provider number to check preferences for
     * @return boolean true if comment prompts should be skipped, false otherwise
     */
    private boolean skipComment(String providerNo) {
        UserPropertyDAO userPropertyDAO = (UserPropertyDAO) SpringUtils.getBean(UserPropertyDAO.class);
        UserProperty uProp = userPropertyDAO.getProp(providerNo, UserProperty.LAB_ACK_COMMENT);
        boolean skipComment = false;
        if (uProp != null && uProp.getValue().equalsIgnoreCase("yes")) {
            skipComment = true;
        }
        return skipComment;
    }
}
