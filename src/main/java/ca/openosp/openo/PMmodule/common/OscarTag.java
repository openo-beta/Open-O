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
 * JSP custom tag for generating navigation links back to the main OSCAR EMR system.
 * <p>
 * This tag creates a hyperlink that returns users to the main OSCAR provider portal
 * from the PMmodule (Program Management module). It reconstructs the provider control
 * URL using session-stored information about the original OSCAR page and query parameters.
 * </p>
 * <p>
 * The tag is particularly useful in integrated deployments where users need to navigate
 * between the specialized PMmodule interface and the main EMR system. It preserves the
 * user's context by maintaining the original query parameters from their OSCAR session.
 * </p>
 * <p>
 * Session Dependencies:
 * <ul>
 *   <li>OscarPageURL - The original OSCAR page URL from which the user navigated</li>
 *   <li>OscarPageQuery - The query string parameters from the original page</li>
 * </ul>
 * </p>
 * <p>
 * Usage in JSP:
 * <pre>
 * &lt;pm:oscar name="optionalName"/&gt;
 * </pre>
 * </p>
 * <p>
 * If the required session attributes are not present (indicating the user accessed
 * PMmodule directly rather than from OSCAR), the tag outputs nothing, gracefully
 * handling the missing integration context.
 * </p>
 *
 * @since 2005-01-01
 * @see javax.servlet.jsp.tagext.Tag
 * @see OscarDemographicLinkTag
 */
public class OscarTag implements Tag {

    /** The JSP page context providing access to implicit objects */
    private PageContext pc = null;
    /** Reference to parent tag for nested tag scenarios */
    private Tag parent = null;
    /** Optional name attribute (currently unused but maintained for extensibility) */
    private String name = null;

    /**
     * Sets the page context for this tag.
     * <p>
     * Called by the JSP container to provide access to the page context
     * and its implicit objects (request, response, session, etc.).
     * </p>
     *
     * @param p PageContext the JSP page context
     */
    public void setPageContext(PageContext p) {
        pc = p;
    }

    /**
     * Sets the parent tag reference.
     * <p>
     * Used by the JSP container for nested tag support. Currently this tag
     * doesn't interact with parent tags but maintains the reference for
     * compliance with the Tag interface.
     * </p>
     *
     * @param t Tag the parent tag, or null if this is a root-level tag
     */
    public void setParent(Tag t) {
        parent = t;
    }

    /**
     * Returns the parent tag.
     *
     * @return Tag the parent tag reference, or null if none
     */
    public Tag getParent() {

        return parent;
    }

    /**
     * Sets the optional name attribute.
     * <p>
     * This attribute is currently not used in the tag implementation
     * but is maintained for potential future enhancements and backward
     * compatibility.
     * </p>
     *
     * @param s String the name value
     */
    public void setName(String s) {
        name = s;
    }

    /**
     * Gets the name attribute value.
     *
     * @return String the name value
     */
    public String getName() {
        return name;
    }

    /**
     * Generates the OSCAR system navigation link when the tag is processed.
     * <p>
     * This method reconstructs the provider control URL using session-stored
     * information about the user's original OSCAR context. The URL is built
     * by taking the directory path from the original URL and appending
     * 'providercontrol.jsp' with the original query parameters.
     * </p>
     * <p>
     * The link generation process:
     * <ol>
     *   <li>Retrieves OscarPageURL from session (the page user came from)</li>
     *   <li>Retrieves OscarPageQuery from session (original query parameters)</li>
     *   <li>Extracts the directory path from the URL</li>
     *   <li>Constructs new URL pointing to providercontrol.jsp</li>
     *   <li>Appends original query parameters to maintain context</li>
     * </ol>
     * </p>
     * <p>
     * If session attributes are missing (user didn't come from OSCAR),
     * the tag outputs nothing rather than generating a broken link.
     * </p>
     *
     * @return int SKIP_BODY as this tag has no body content to process
     * @throws JspException if an error occurs during output generation
     */
    public int doStartTag() throws JspException {
        try {
            HttpSession se = ((HttpServletRequest) pc.getRequest()).getSession();
            // Retrieve the original OSCAR page URL from session
            String p = (String) se.getAttribute("OscarPageURL");
            // Retrieve the original query string parameters
            String q = (String) se.getAttribute("OscarPageQuery");

            if (p == null) {
                // No OSCAR URL in session - user came directly to PMmodule
                pc.getOut().print("");
            } else if (p.equals("")) {
                // Empty URL - invalid state, output nothing
                pc.getOut().print("");
            } else {
                // Extract directory path and construct provider control URL
                // Takes everything up to and including the last '/'
                // then appends providercontrol.jsp with original query params
                p = p.substring(0, p.lastIndexOf("/") + 1) + "providercontrol.jsp?" + q;
                // Generate simple anchor tag with reconstructed URL
                String temps = "<a href='" + p + "'>Oscar Medical</a>";
                pc.getOut().print(temps);
            }

        } catch (Exception e) {
            // Wrap any exception in JspTagException
            throw new JspTagException("An IOException occurred.");
        }

        // No body content to process
        return SKIP_BODY;
    }

    /**
     * Completes tag processing.
     * <p>
     * Returns EVAL_PAGE to indicate that JSP processing should continue
     * with the rest of the page after this tag.
     * </p>
     *
     * @return int EVAL_PAGE to continue page evaluation
     * @throws JspException if an error occurs during tag completion
     */
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    /**
     * Releases all resources and resets the tag for reuse.
     * <p>
     * Called by the JSP container when returning the tag instance to the
     * tag pool. This method ensures all references are cleared to prevent
     * memory leaks and prepares the tag for reuse with different attributes.
     * </p>
     * <p>
     * This implementation follows the standard pattern of nullifying all
     * instance variables to ensure clean state for tag pooling.
     * </p>
     */
    public void release() {
        // Clear all instance variables for tag reuse
        pc = null;
        parent = null;
        name = null;
    }

}
