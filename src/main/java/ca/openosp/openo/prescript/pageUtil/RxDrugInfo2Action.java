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
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.prescript.data.RxDrugData;


import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for retrieving drug information from external resources.
 * <p>
 * This action provides access to external drug information resources by
 * redirecting to the OSCAR drug information search system. It handles
 * both generic name (GN) and brand name (BN) drug lookups and redirects
 * to the appropriate external resource with the drug information.
 * <p>
 * The action integrates with the RxDrugData system to resolve brand names
 * to generic names when necessary and constructs appropriate URLs for
 * external drug information lookups.
 *
 * @since 2008
 */
public final class RxDrugInfo2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for handling redirects */
    HttpServletResponse response = ServletActionContext.getResponse();

    /** Security manager for validating user permissions */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);


    /**
     * Main execution method for drug information lookup and redirect.
     * <p>
     * This method:
     * 1. Validates user permissions for prescription access
     * 2. Processes generic name (GN) or brand name (BN) parameters
     * 3. Resolves brand names to generic names if necessary
     * 4. Redirects to external drug information resource with search terms
     * <p>
     * The method handles two types of drug lookups:
     * - Generic Name (GN): Direct redirect with the generic name
     * - Brand Name (BN): Resolves to generic name first, then redirects
     *
     * @return String null (no forward needed as response is redirected)
     * @throws IOException if an input/output error occurs during redirect
     * @throws ServletException if a servlet error occurs
     * @throws RuntimeException if user lacks required prescription permissions
     */
    public String execute()
            throws IOException, ServletException {

        // Validate user has permission to access prescription information
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_rx", "r", null)) {
            throw new RuntimeException("missing required sec object (_rx)");
        }


        // Extract drug name parameters from request
        String GN = null;
        String BN = null;

        // Handle generic name lookup
        if (request.getParameter("GN") != null) {
            if (!request.getParameter("GN").equals("null")) {
                GN = request.getParameter("GN");
                response.sendRedirect("http://resource.oscarmcmaster.org/oscarResource/OSCAR_search/OSCAR_search_results?title=" + URLEncoder.encode(GN, "UTF-8"));
            }
        }

        // Handle brand name lookup - resolve to generic name first
        if (request.getParameter("BN") != null) {
            if (!request.getParameter("BN").equals("null")) {
                BN = request.getParameter("BN");
                RxDrugData drugData = new RxDrugData();
                String genName = null;
                try {
                    // Try to resolve brand name to generic name
                    genName = drugData.getGenericName(BN);
                } catch (Exception e) {
                    // Fall back to brand name if resolution fails
                    genName = BN;
                }
                response.sendRedirect("http://resource.oscarmcmaster.org/oscarResource/OSCAR_search/OSCAR_search_results?title=" + URLEncoder.encode(genName, "UTF-8"));
            }
        }

        // If no valid drug names provided, could redirect to general search page
        if (GN == null && BN == null) {
            // Future enhancement: redirect to general drug search page
        }

        return null;
    }
}
