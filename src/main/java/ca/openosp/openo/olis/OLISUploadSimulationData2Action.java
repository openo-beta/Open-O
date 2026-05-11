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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PathValidationUtils;

import ca.openosp.openo.olis1.Driver;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;

public class OLISUploadSimulationData2Action extends ActionSupport implements UploadedFilesAware {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    @Override
    public String execute() {

        Logger logger = MiscUtils.getLogger();

        if (simulateFileOnDisk == null) {
            request.setAttribute("result", "Please select a file to upload before submitting.");
            return SUCCESS;
        }

        String simulationData = null;
        try (InputStream is = new FileInputStream(simulateFileOnDisk)) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, "UTF-8");
            simulationData = writer.toString();

            if (simulationData != null && simulationData.length() > 0) {
                if (simulateError) {
                    Driver.readResponseFromXML(LoggedInInfo.getLoggedInInfoFromSession(request), request, simulationData);
                    simulationData = (String) request.getAttribute("olisResponseContent");
                    request.getSession().setAttribute("errors", request.getAttribute("errors"));
                }
                request.getSession().setAttribute("olisResponseContent", simulationData);
                request.setAttribute("result", "File successfully uploaded");
            } else {
                request.setAttribute("result", "Uploaded file was empty");
            }
        } catch (Exception e) {
            logger.error("Error reading uploaded OLIS simulation file", e);
            request.setAttribute("result", "Error reading uploaded file");
        }

        return SUCCESS;
    }

    private UploadedFile simulateFile;
    private File simulateFileOnDisk;
    private boolean simulateError;


    @Override
    public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
        if (!uploadedFiles.isEmpty()) {
            this.simulateFile = uploadedFiles.get(0);
            this.simulateFileOnDisk = PathValidationUtils.toFile(simulateFile);
        }
    }

    public void setSimulateError(String simulateErrorParam) {
        // HTML checkbox sends "on" when checked, no param when unchecked.
        // Treat any non-empty / non-"false" presence as true.
        this.simulateError = simulateErrorParam != null
                && !"false".equalsIgnoreCase(simulateErrorParam)
                && !simulateErrorParam.isEmpty();
    }
}
