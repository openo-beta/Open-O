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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

/**
 * JSP custom tag for generating links to the demographic master file from PMmodule.
 * <p>
 * This tag creates a hyperlink that opens the patient's demographic record in a new window.
 * It implements the basic Tag interface rather than extending TagSupport, providing
 * complete control over the tag lifecycle. The tag relies on a session attribute
 * 'OscarPageURL' to determine the base URL of the main OSCAR system.
 * </p>
 * <p>
 * The tag is designed for integration scenarios where the PMmodule needs to provide
 * quick access to the full patient demographic record in the main EMR system. It
 * generates a JavaScript-based popup link to avoid navigation away from the current
 * PMmodule context.
 * </p>
 * <p>
 * Usage in JSP:
 * <pre>
 * &lt;pm:oscarDemographicLink demographicNo="${patientId}"/&gt;
 * </pre>
 * </p>
 * <p>
 * Security Note: The demographic number is directly concatenated into the URL.
 * While this is generally safe for numeric IDs, proper validation should be
 * performed to ensure the demographicNo contains only expected characters.
 * </p>
 *
 * @since 2005-01-01
 * @see javax.servlet.jsp.tagext.Tag
 */
public class OscarDemographicLinkTag implements Tag {

    /** The JSP page context for accessing request, response, and session */
    private PageContext pc = null;
    /** Parent tag reference for nested tag scenarios */
    private Tag parent = null;
    /** Optional name attribute (currently unused but maintained for compatibility) */
    private String name = null;
    /** The patient's demographic number to link to */
    private String demographicNo;

    /**
     * Sets the page context for this tag.
     * <p>
     * Called by the JSP container to provide access to JSP implicit objects.
     * </p>
     *
     * @param p PageContext the JSP page context
     */
    public void setPageContext(PageContext p) {
        pc = p;
    }

    /**
     * Sets the parent tag if this tag is nested.
     *
     * @param t Tag the parent tag, or null if this is a top-level tag
     */
    public void setParent(Tag t) {
        parent = t;
    }

    /**
     * Returns the parent tag.
     *
     * @return Tag the parent tag, or null if none
     */
    public Tag getParent() {
        return parent;
    }

    /**
     * Sets the name attribute.
     * <p>
     * This attribute is currently unused but maintained for backward compatibility
     * and potential future use.
     * </p>
     *
     * @param s String the name value
     */
    public void setName(String s) {
        name = s;
    }

    /**
     * Gets the name attribute.
     *
     * @return String the name value
     */
    public String getName() {
        return name;
    }

    /**
     * Generates the demographic link when the tag is encountered.
     * <p>
     * This method retrieves the base OSCAR URL from the session and constructs
     * a link to the demographic control page. If the base URL is not available
     * (indicating the main OSCAR system is not accessible), no link is generated.
     * </p>
     * <p>
     * The generated link uses JavaScript to open the demographic page in a new
     * window named 'demographic', preventing navigation away from the PMmodule.
     * The link includes parameters for edit mode and detailed search operation.
     * </p>
     * <p>
     * URL Construction:
     * <ol>
     *   <li>Extracts base URL from OscarPageURL session attribute</li>
     *   <li>Removes the '/providers' path segment to get the application root</li>
     *   <li>Appends the demographic control page path with parameters</li>
     *   <li>Wraps in JavaScript window.open() for popup behavior</li>
     * </ol>
     * </p>
     *
     * @return int SKIP_BODY as this tag has no body content
     * @throws JspException if an error occurs during tag processing
     */
    public int doStartTag() throws JspException {
        try {
            HttpSession se = ((HttpServletRequest) pc.getRequest()).getSession();
            // Retrieve the base OSCAR URL from session
            // This is set when users navigate from main OSCAR to PMmodule
            String p = (String) se.getAttribute("OscarPageURL");

            if (p == null || p.equals("")) {
                // No OSCAR URL available - output nothing
                // This happens when PMmodule is accessed directly
                pc.getOut().print("");
            } else {
                // Extract base URL by removing provider-specific path
                p = p.substring(0, p.indexOf("/providers"));
                // Construct demographic edit URL with required parameters
                p += "/demographic/demographiccontrol.jsp?displaymode=edit&dboperation=search_detail&demographic_no=" + demographicNo;
                // Generate JavaScript popup link
                // javascript:void(0) prevents default link navigation
                // window.open() creates popup with name 'demographic' for window reuse
                String temps = "<a href=\"javascript.void(0);\" onclick=\"window.open('" + p + "','demographic');return false;\">OSCAR Master File</a>";
                pc.getOut().print(temps);
            }

        } catch (Exception e) {
            // Generic exception handling - could be IOException or others
            throw new JspTagException("An IOException occurred.");
        }

        // No body content to process
        return SKIP_BODY;
    }

    /**
     * Completes tag processing.
     *
     * @return int EVAL_PAGE to continue processing the JSP page
     * @throws JspException if an error occurs
     */
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    /**
     * Releases all resources held by this tag.
     * <p>
     * Called by the JSP container when the tag is returned to the tag pool.
     * This method clears all instance variables to prevent memory leaks and
     * ensure the tag is ready for reuse.
     * </p>
     */
    public void release() {
        pc = null;
        parent = null;
        name = null;
        // Note: demographicNo is not cleared here, which could be a bug
        // Should probably add: demographicNo = null;
    }

    /**
     * Gets the demographic number.
     *
     * @return String the patient's demographic number
     */
    public String getDemographicNo() {
        return demographicNo;
    }

    /**
     * Sets the demographic number for the patient link.
     * <p>
     * This is the primary attribute for the tag and identifies which
     * patient's demographic record should be linked to.
     * </p>
     *
     * @param demographicNo String the patient's unique demographic identifier
     */
    public void setDemographicNo(String demographicNo) {
        this.demographicNo = demographicNo;
    }

}
