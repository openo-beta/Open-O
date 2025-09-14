//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.olis;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;

import ca.openosp.openo.olis1.Driver;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * OLIS Upload Simulation Data Action for testing OLIS response processing with simulated data.
 * <p>
 * This action allows developers and testers to upload XML files containing simulated OLIS
 * responses for testing the system's ability to process laboratory results without connecting
 * to the live OLIS system. This is essential for development, testing, and debugging scenarios.
 * <p>
 * Key Features:
 * - File upload handling for XML simulation data
 * - Optional error simulation mode for testing error handling
 * - Integration with OLIS Driver for response processing
 * - Session-based result storage for display
 * - Comprehensive error handling and logging
 * <p>
 * The action supports two modes:
 * 1. Normal simulation: Processes uploaded XML as successful OLIS response
 * 2. Error simulation: Processes uploaded XML through error handling pathways
 *
 * @since 2008
 */
public class OLISUploadSimulationData2Action extends ActionSupport {
    /** HTTP servlet request for file upload processing */
    HttpServletRequest request = ServletActionContext.getRequest();
    /** HTTP servlet response for result handling */
    HttpServletResponse response = ServletActionContext.getResponse();


    /**
     * Processes uploaded OLIS simulation data files for testing purposes.
     * <p>
     * This method handles multipart file uploads containing XML simulation data and processes
     * them through the OLIS response handling system. The simulation can run in normal or
     * error mode to test different scenarios.
     * <p>
     * Processing steps:
     * 1. Parse multipart request to extract uploaded files and form parameters
     * 2. Read simulation data file content as UTF-8 text
     * 3. Check for error simulation mode flag
     * 4. Process simulation data through OLIS Driver
     * 5. Store results in session for display
     * 6. Set success message for user feedback
     *
     * @return String SUCCESS constant indicating successful processing
     */
    @Override
    public String execute() {

        Logger logger = MiscUtils.getLogger();

        String simulationData = null;
        boolean simulationError = false;

        try {
            FileUpload upload = new FileUpload(new DefaultFileItemFactory());
            @SuppressWarnings("unchecked")
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    // Process form fields for simulation configuration
                    String name = item.getFieldName();
                    if (name.equals("simulateError")) {
                        simulationError = true;
                    }
                } else {
                    // Process uploaded simulation data file
                    if (item.getFieldName().equals("simulateFile")) {
                        InputStream is = item.getInputStream();
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(is, writer, "UTF-8");
                        simulationData = writer.toString();
                    }
                }
            }

            // Process simulation data if file was successfully uploaded
            if (simulationData != null && simulationData.length() > 0) {
                if (simulationError) {
                    // Process simulation data through error handling pathway
                    Driver.readResponseFromXML(LoggedInInfo.getLoggedInInfoFromSession(request), request, simulationData);
                    simulationData = (String) request.getAttribute("olisResponseContent");
                    request.getSession().setAttribute("errors", request.getAttribute("errors"));
                }
                // Store simulation results in session for display
                request.getSession().setAttribute("olisResponseContent", simulationData);
                request.setAttribute("result", "File successfully uploaded");
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Error processing OLIS simulation data upload", e);
        }

        return SUCCESS;
    }
}
