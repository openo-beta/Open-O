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
 * Struts2 action for adding allergic reactions in the prescription module.
 * <p>
 * This action handles the processing of allergy reaction addition requests
 * from the prescription interface. It extracts allergy information from
 * request parameters and forwards them to the appropriate view for
 * displaying the allergy addition form.
 * <p>
 * The action processes parameters such as drug/allergen ID, name, type,
 * and handles archiving of existing allergies when adding new ones.
 * This is part of the comprehensive allergy management system that helps
 * prevent adverse drug reactions.
 *
 * @since 2008
 */
public final class RxAddReaction2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters and attributes */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for handling the response */
    HttpServletResponse response = ServletActionContext.getResponse();


    /**
     * Main execution method for processing allergy reaction addition requests.
     * <p>
     * Extracts allergy-related parameters from the request including:
     * - ID: Drug or allergen identifier
     * - name: Name of the drug or allergen
     * - type: Type of allergy (drug/non-drug)
     * - allergyToArchive: ID of existing allergy to archive
     * - nkdaId: No Known Drug Allergies identifier
     * <p>
     * These parameters are set as request attributes to be used by the
     * target JSP page for displaying the allergy addition form.
     *
     * @return String the result status (SUCCESS) to forward to the appropriate view
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    public String execute()
            throws IOException, ServletException {

        // Extract allergy-related parameters from the request
        String id = request.getParameter("ID");
        String name = request.getParameter("name");
        String type = request.getParameter("type");
        String allergyToArchive = request.getParameter("allergyToArchive");
        String nkdaId = request.getParameter("nkdaId");


        // Set extracted parameters as request attributes for the target JSP
        request.setAttribute("drugrefId", id);
        request.setAttribute("name", name);
        request.setAttribute("type", type);
        request.setAttribute("allergyToArchive", allergyToArchive);
        request.setAttribute("nkdaId", nkdaId);


        return SUCCESS;
    }
}
