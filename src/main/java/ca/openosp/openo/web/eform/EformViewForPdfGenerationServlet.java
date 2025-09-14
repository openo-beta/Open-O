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


package ca.openosp.openo.web.eform;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Electronic form PDF generation servlet for healthcare documentation processing.
 *
 * This servlet provides secure access to electronic form data specifically for PDF
 * generation purposes in the OpenO EMR system. It ensures that only local processes
 * can access healthcare forms for conversion to PDF format, maintaining PHI security
 * while enabling document generation workflows.
 *
 * The servlet acts as a security wrapper around the electronic form display JSP,
 * restricting access to localhost only and forwarding valid requests to the
 * underlying form rendering engine. This pattern is commonly used for automated
 * document generation where external PDF conversion tools need to access form
 * content without compromising system security.
 *
 * <p>Healthcare forms processed by this servlet may contain sensitive patient
 * information including medical history, test results, treatment plans, and
 * demographic data. The localhost-only restriction ensures these forms can
 * only be accessed by authorized server-side processes.</p>
 *
 * @since August 2010
 * @see ca.openosp.openo.eform
 */
public final class EformViewForPdfGenerationServlet extends HttpServlet {

    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Handles HTTP GET requests for electronic form PDF generation access.
     *
     * This method enforces strict localhost-only security by validating the
     * remote IP address before allowing access to healthcare form data. Valid
     * requests are forwarded to the electronic form display JSP for rendering.
     *
     * <p>The security check ensures that only local PDF generation processes
     * can access patient health information, maintaining HIPAA/PIPEDA compliance
     * by preventing external access to sensitive healthcare data.</p>
     *
     * @param request HttpServletRequest containing form ID and display parameters
     * @param response HttpServletResponse for form content delivery or error responses
     * @throws ServletException if the request cannot be processed
     * @throws IOException if an I/O error occurs during request forwarding
     *
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    @Override
    public final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Ensure it's a local machine request to protect PHI from external access
        String remoteAddress = request.getRemoteAddr();
        logger.debug("EformPdfServlet request from : " + remoteAddress);
        if (!"127.0.0.1".equals(remoteAddress)) {
            logger.warn("Unauthorised request made to EformPdfServlet from address : " + remoteAddress);
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Forward to electronic form display JSP for PDF generation
        // Example: https://127.0.0.1:8443/oscar/eform/efmshowform_data.jsp?fdid=2&parentAjaxId=eforms
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("/eform/efmshowform_data.jsp");
        requestDispatcher.forward(request, response);
    }
}
