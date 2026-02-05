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
package ca.openosp.openo.tickler.pageUtil;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opensymphony.xwork2.ActionSupport;

import ca.openosp.OscarProperties;
import ca.openosp.openo.commn.model.CustomFilter;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.managers.TicklerManager;
import ca.openosp.openo.tickler.dto.TicklerCommentDTO;
import ca.openosp.openo.tickler.dto.TicklerLinkDTO;
import ca.openosp.openo.tickler.dto.TicklerListDTO;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Struts2 action that returns paginated tickler data as JSON for DataTables
 * server-side processing. Returns raw data objects; HTML rendering is handled
 * by client-side DataTables column render functions.
 *
 * @since 2026-02-05
 */
public class TicklerList2Action extends ActionSupport {

    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TicklerManager ticklerManager = SpringUtils.getBean(TicklerManager.class);
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Handles DataTables server-side processing requests. Accepts standard
     * DataTables parameters (draw, start, length) plus tickler filter parameters.
     * Returns JSON with raw data fields for client-side rendering.
     *
     * @return null since the response is written directly
     * @throws IOException if writing the JSON response fails
     */
    public String execute() throws IOException {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_tickler", "r", null)) {
            throw new SecurityException("missing required sec object (_tickler)");
        }

        int draw = parseIntParam("draw", 1);
        int start = parseIntParam("start", 0);
        int length = parseIntParam("length", 50);
        boolean showAll = (length <= 0);

        Locale locale = request.getLocale();

        CustomFilter filter = buildFilterFromRequest();

        int totalRecords = ticklerManager.getNumTicklers(loggedInInfo, filter);
        List<TicklerListDTO> ticklers;
        if (showAll) {
            ticklers = ticklerManager.getTicklerDTOs(loggedInInfo, filter);
        } else {
            ticklers = ticklerManager.getTicklerDTOs(loggedInInfo, filter, start, length);
        }

        long ticklerWarnDays = getTicklerWarnDays();
        boolean ignoreWarning = (ticklerWarnDays <= 0);

        DateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        DateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", locale);
        DateFormat timeOnlyFormat = new SimpleDateFormat("HH:mm:ss", locale);

        ArrayNode dataArray = objectMapper.createArrayNode();

        for (TicklerListDTO tickler : ticklers) {
            LocalDateTime serviceDate = tickler.getServiceDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            long daysDifference = Duration.between(serviceDate, LocalDateTime.now()).toDays();
            boolean warning = !ignoreWarning && (daysDifference >= ticklerWarnDays);

            ObjectNode row = objectMapper.createObjectNode();
            row.put("id", tickler.getId());
            row.put("demoNo", tickler.getDemographicNo());
            row.put("demoLastName", tickler.getDemographicLastName());
            row.put("demoFirstName", tickler.getDemographicFirstName());
            row.put("creator", tickler.getCreatorFormattedName());
            row.put("serviceDate", dateOnlyFormat.format(tickler.getServiceDate()));
            row.put("createDate", datetimeFormat.format(tickler.getCreateDate()));
            row.put("priority", String.valueOf(tickler.getPriority()));
            row.put("assignee", tickler.getAssigneeFormattedName());
            row.put("status", tickler.getStatusDesc(locale));
            row.put("message", tickler.getMessage());
            row.put("warning", warning);
            row.put("rowType", "tickler");

            ArrayNode linksArray = objectMapper.createArrayNode();
            List<TicklerLinkDTO> linkList = tickler.getLinks();
            if (linkList != null) {
                for (TicklerLinkDTO tl : linkList) {
                    ObjectNode linkNode = objectMapper.createObjectNode();
                    linkNode.put("tableName", tl.getTableName());
                    linkNode.put("tableId", tl.getTableId());
                    linksArray.add(linkNode);
                }
            }
            row.set("links", linksArray);

            dataArray.add(row);

            List<TicklerCommentDTO> tcomments = tickler.getComments();
            if (tcomments != null) {
                for (TicklerCommentDTO tc : tcomments) {
                    ObjectNode commentRow = objectMapper.createObjectNode();
                    commentRow.put("id", tickler.getId());
                    commentRow.put("demoNo", tickler.getDemographicNo());
                    commentRow.put("demoLastName", tickler.getDemographicLastName());
                    commentRow.put("demoFirstName", tickler.getDemographicFirstName());
                    commentRow.put("creator", tc.getProviderFormattedName());
                    commentRow.put("serviceDate", dateOnlyFormat.format(tickler.getServiceDate()));
                    if (tc.isUpdateDateToday()) {
                        commentRow.put("createDate", timeOnlyFormat.format(tc.getUpdateDate()));
                    } else {
                        commentRow.put("createDate", datetimeFormat.format(tc.getUpdateDate()));
                    }
                    commentRow.put("priority", String.valueOf(tickler.getPriority()));
                    commentRow.put("assignee", "");
                    commentRow.put("status", "");
                    commentRow.put("message", tc.getMessage());
                    commentRow.put("warning", false);
                    commentRow.put("rowType", "comment");

                    dataArray.add(commentRow);
                }
            }
        }

        ObjectNode result = objectMapper.createObjectNode();
        result.put("draw", draw);
        result.put("recordsTotal", totalRecords);
        result.put("recordsFiltered", totalRecords);
        result.set("data", dataArray);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(result.toString());

        return null;
    }

    /**
     * Parses request parameters and constructs a CustomFilter for the tickler query.
     * Handles both general and demographic-specific filtering through a single path.
     *
     * @return CustomFilter populated from request parameters
     */
    private CustomFilter buildFilterFromRequest() {
        String ticklerview = getStringParam("ticklerview", "A");
        String providerview = getStringParam("providerview", "all");
        String assignedTo = getStringParam("assignedTo", "all");
        String mrpview = getStringParam("mrpview", "all");
        String dateBegin = getStringParam("xml_vdate", "1950-01-01");
        String dateEnd = getStringParam("xml_appointment_date", "");
        int targetDemographic = parseIntParam("demographic_no", 0);

        CustomFilter filter = new CustomFilter();
        filter.setPriority(null);
        filter.setStatus(ticklerview);
        filter.setStartDateWeb(dateBegin);
        filter.setEndDateWeb(dateEnd);

        if (targetDemographic > 0) {
            filter.setDemographicNo(String.valueOf(targetDemographic));
            // When viewing a specific demographic's ticklers, only filter by
            // demographic, status, and date range (matches old search_tickler_bydemo behavior)
            filter.setMrp(null);
            filter.setProvider(null);
            filter.setAssignee(null);
            if (dateEnd.isEmpty()) {
                filter.setEndDateWeb("8888-12-31");
            }
        } else {
            if (!mrpview.isEmpty() && !"all".equals(mrpview)) {
                filter.setMrp(mrpview);
            }
            if (!providerview.isEmpty() && !"all".equals(providerview)) {
                filter.setProvider(providerview);
            }
            if (!assignedTo.isEmpty() && !"all".equals(assignedTo)) {
                filter.setAssignee(assignedTo);
            }
        }

        filter.setSort_order("desc");
        return filter;
    }

    /**
     * Reads the tickler warning period from application properties.
     *
     * @return the number of days after which a tickler triggers a warning, or 0 if not configured
     */
    private long getTicklerWarnDays() {
        String numDaysUntilWarn = OscarProperties.getInstance().getProperty("tickler_warn_period");
        if (numDaysUntilWarn == null || numDaysUntilWarn.isEmpty()) {
            numDaysUntilWarn = "0";
        }
        return Long.parseLong(numDaysUntilWarn);
    }

    private int parseIntParam(String name, int defaultValue) {
        String val = request.getParameter(name);
        if (val == null || val.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String getStringParam(String name, String defaultValue) {
        String val = request.getParameter(name);
        if (val == null || val.isEmpty()) {
            return defaultValue;
        }
        return val;
    }
}
