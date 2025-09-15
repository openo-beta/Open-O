//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.PMmodule.common;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * JSP custom tag for generating case management encounter links in the PMmodule.
 * <p>
 * This tag generates a complete URL to the encounter system (IncomingEncounter.do) with
 * all necessary parameters pre-populated. It's designed to facilitate seamless navigation
 * from the Program Management module to the patient encounter interface, automatically
 * setting up the encounter context with current date/time and provider information.
 * </p>
 * <p>
 * The tag extends TagSupport for simplified tag implementation and generates the URL
 * dynamically based on the current server configuration, ensuring portability across
 * different deployment environments (development, staging, production).
 * </p>
 * <p>
 * Usage in JSP:
 * <pre>
 * &lt;pm:caseManagementLink demographicNo="${demographicId}"
 *                         providerNo="${providerId}"
 *                         providerName="${providerName}"/&gt;
 * </pre>
 * </p>
 * <p>
 * The generated URL includes automatic timestamp generation for encounter tracking
 * and sets the status to 'T' (temporary) by default, indicating a new encounter.
 * </p>
 *
 * @since 2005-01-01
 * @see javax.servlet.jsp.tagext.TagSupport
 */
public class CaseManagementLinkTag extends TagSupport {

    /** The patient's demographic number - primary identifier for the patient */
    private Integer demographicNo;
    /** The provider's unique identifier who will be conducting the encounter */
    private String providerNo;
    /** The provider's display name for UI presentation */
    private String providerName;

    /**
     * Sets the demographic number for the patient.
     * <p>
     * This is a required attribute that identifies which patient
     * the encounter link should be created for.
     * </p>
     *
     * @param demographicNo Integer the patient's unique demographic identifier
     */
    public void setDemographicNo(Integer demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Sets the provider number for the encounter.
     * <p>
     * This identifies the healthcare provider who will be conducting
     * the encounter. Used for both providerNo and curProviderNo parameters.
     * </p>
     *
     * @param providerNo String the provider's unique identifier
     */
    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * Sets the provider's display name.
     * <p>
     * This name is passed as the userName parameter and is used
     * for display purposes in the encounter interface.
     * </p>
     *
     * @param providerName String the provider's full name for display
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * Generates and writes the encounter URL when the tag is processed.
     * <p>
     * This method constructs a complete URL to the IncomingEncounter.do action
     * with all necessary parameters for creating a new encounter. It automatically
     * generates the current date and time for the encounter timestamps.
     * </p>
     * <p>
     * The URL is built dynamically using the current request's scheme, server name,
     * and port to ensure it works correctly across different deployment environments.
     * This approach handles both HTTP and HTTPS protocols and non-standard ports.
     * </p>
     * <p>
     * Parameters included in the generated URL:
     * <ul>
     *   <li>providerNo - The provider conducting the encounter</li>
     *   <li>appointmentNo - Set to 0 for walk-in/unscheduled encounters</li>
     *   <li>demographicNo - The patient identifier</li>
     *   <li>curProviderNo - Current provider (same as providerNo)</li>
     *   <li>reason - Empty for case management initiated encounters</li>
     *   <li>userName - Provider's display name</li>
     *   <li>curDate - Today's date in yyyy-M-d format</li>
     *   <li>appointmentDate - Same as curDate</li>
     *   <li>startTime - Current time in HH:mm format</li>
     *   <li>status - 'T' for temporary/new encounter</li>
     * </ul>
     * </p>
     *
     * @return int SKIP_BODY to indicate tag has no body content
     * @throws JspException if an IOException occurs while writing to the output stream
     */
    @Override
    public int doStartTag() throws JspException {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();

        // Generate current timestamp for encounter
        Date date = new Date();

        // Format date and time according to encounter system requirements
        // Date format: yyyy-M-d (single digit months/days not zero-padded)
        Format formatterDate = new SimpleDateFormat("yyyy-M-d");
        // Time format: 24-hour format
        Format formatterTime = new SimpleDateFormat("HH:mm");

        String placeDate = formatterDate.format(date);
        String placeTime = formatterTime.format(date);


        try {
            StringBuilder builder = new StringBuilder();

            // Build base URL from current request context
            // This ensures the URL works regardless of deployment configuration
            builder.append(req.getScheme()).append("://");
            builder.append(req.getServerName()).append(":");
            builder.append(req.getServerPort());
            builder.append(req.getContextPath()).append("/");

            // Append encounter endpoint and parameters
            builder.append("oscarEncounter/IncomingEncounter.do").append("?");
            builder.append("providerNo=").append(providerNo).append("&");
            // appointmentNo=0 indicates walk-in/unscheduled encounter
            builder.append("appointmentNo=").append(0).append("&");
            builder.append("demographicNo=").append(demographicNo).append("&");
            // curProviderNo duplicates providerNo for session tracking
            builder.append("curProviderNo=").append(providerNo).append("&");
            // reason left empty for case management initiated encounters
            builder.append("reason=").append("&");
            builder.append("userName=").append(providerName).append("&");
            builder.append("curDate=").append(placeDate).append("&");
            builder.append("appointmentDate=").append(placeDate).append("&");
            builder.append("startTime=").append(placeTime).append("&");
            // status='T' indicates temporary/new encounter
            builder.append("status=").append("T");

            // Write the complete URL to JSP output
            pageContext.getOut().write(builder.toString());
        } catch (IOException e) {
            // Wrap IOException in JspTagException as required by tag contract
            throw new JspTagException("An IOException occurred.");
        }

        // No body content to process
        return SKIP_BODY;
    }

    /**
     * Completes tag processing.
     * <p>
     * Returns EVAL_PAGE to continue processing the rest of the JSP page
     * after this tag completes.
     * </p>
     *
     * @return int EVAL_PAGE to continue page evaluation
     * @throws JspException if an error occurs during tag completion
     */
    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

}
