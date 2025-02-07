//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package org.oscarehr.hospitalReportManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;

import oscar.oscarLab.ca.all.util.Utilities;

import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.ActionContext;

public class HRMUploadLab2Action extends ActionSupport {
    ActionContext context = ActionContext.getContext();
    HttpServletRequest request = (HttpServletRequest) context.get(ServletActionContext.HTTP_REQUEST);
    HttpServletResponse response = (HttpServletResponse) context.get(ServletActionContext.HTTP_RESPONSE);


    public enum FileStatus {
        COMPLETED,
        FAILED,
        INVALID
    }

    @Override
    public String execute() {
        String success = SUCCESS;
        if (!JakartaServletFileUpload.isMultipartContent(request)) {
            return success;
        }

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        List<FileItem> fileItems = getFiles(request);
        if (fileItems == null) {
            return success;
        }

        Map<String, FileStatus> filesStatusMap = processFiles(loggedInInfo, fileItems);
        request.setAttribute("filesStatusMap", filesStatusMap);
        return success;
    }

    private Map<String, FileStatus> processFiles(LoggedInInfo loggedInInfo, List<FileItem> fileItems) {
        Map<String, FileStatus> filesStatusMap = new HashMap<>();

        for (FileItem fileItem : fileItems) {
            String fileName = fileItem.getName();

            try (InputStream inputStream = fileItem.getInputStream()) {
                String filePath = Utilities.saveFile(inputStream, fileName);
                HRMReport report = HRMReportParser.parseReport(loggedInInfo, filePath);
                FileStatus fileStatus = handleHRMReport(loggedInInfo, report);
                filesStatusMap.put(fileName, fileStatus);
            } catch (IOException e) {
                MiscUtils.getLogger().error("Error occurred while processing file", e);
                filesStatusMap.put(fileName, FileStatus.INVALID);
            }
        }

        return filesStatusMap;
    }

    private List<FileItem> getFiles(HttpServletRequest request) {
        DiskFileItemFactory diskFileItemFactory = DiskFileItemFactory.builder().setPath(System.getProperty("java.io.tmpdir")).get();
        JakartaServletFileUpload servletFileUpload = new JakartaServletFileUpload(diskFileItemFactory);

        try {
            return servletFileUpload.parseRequest(request);
        } catch (FileUploadException e) {
            MiscUtils.getLogger().error("Error occurred while uploading files", e);
            return null;
        }
    }

    private FileStatus handleHRMReport(LoggedInInfo loggedInInfo, HRMReport report) {
        if (report == null) {
            return FileStatus.INVALID;
        }

        try {
            HRMReportParser.addReportToInbox(loggedInInfo, report);
            return FileStatus.COMPLETED;
        } catch (Exception e) {
            MiscUtils.getLogger().error("Couldn't handle uploaded HRM report", e);
            return FileStatus.FAILED;
        }
    }
}
