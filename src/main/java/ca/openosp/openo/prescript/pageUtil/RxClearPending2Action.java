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


package ca.openosp.openo.prescript.pageUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for clearing pending prescriptions from the prescription stash.
 * <p>
 * This action handles the clearing of pending prescriptions from the user's
 * prescription stash. The stash is used to temporarily store prescriptions
 * during the prescription workflow before they are finalized or printed.
 * This action provides functionality to clear all pending items and optionally
 * close the prescription interface.
 * <p>
 * The action integrates with RxSessionBean to manage the prescription stash
 * state and supports both clearing the stash and returning to different
 * workflow states.
 *
 * @since 2008
 */
public final class RxClearPending2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters and session */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for handling the response */
    HttpServletResponse response = ServletActionContext.getResponse();

    /**
     * Main execution method for clearing pending prescriptions from the stash.
     * <p>
     * This method:
     * 1. Retrieves the RxSessionBean from the session
     * 2. Clears all pending prescription items from the stash
     * 3. Determines the appropriate return action based on the action parameter
     * <p>
     * If the action parameter is "close", the method returns "close" to
     * close the prescription interface. Otherwise, it returns SUCCESS to
     * continue with the prescription workflow.
     *
     * @return String the result status ("close" or SUCCESS) depending on action parameter
     * @throws IOException if an input/output error occurs during redirect
     * @throws ServletException if a servlet error occurs
     */
    public String execute()
            throws IOException, ServletException {


        // Retrieve the prescription session bean from the session
        RxSessionBean bean = (RxSessionBean) request.getSession().getAttribute("RxSessionBean");
        if (bean == null) {
            response.sendRedirect("error.html");
            return null;
        }


        // Clear all pending prescriptions from the stash
        bean.clearStash();

        // Check if we should close the prescription interface
        if (action.equals("close")) {
            return "close";
        }

        return SUCCESS;
    }

    /** Action parameter to determine workflow behavior */
    private String action = null;

    /**
     * Gets the action parameter that determines the workflow behavior.
     *
     * @return String the action parameter ("close" to close interface, null for normal flow)
     */
    public String getAction() {
        return this.action;
    }

    /**
     * Sets the action parameter that determines the workflow behavior.
     *
     * @param RHS String the action parameter to set
     */
    public void setAction(String RHS) {
        this.action = RHS;
    }
}
