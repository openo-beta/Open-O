//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.hospitalReportManager;

import ca.openosp.openo.lab.ca.all.util.Utilities;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HRMUploadLab2Action extends ActionSupport {
    private List<File> uploads;
    private List<String> uploadsContentType;
    private List<String> uploadsFileName;

    public void setUploads(List<File> uploads) {
        this.uploads = uploads;
    }

    public void setUploadsContentType(List<String> uploadsContentType) {
        this.uploadsContentType = uploadsContentType;
    }

    public void setUploadsFileName(List<String> uploadsFileName) {
        this.uploadsFileName = uploadsFileName;
    }

    public enum FileStatus {
        COMPLETED,
        FAILED,
        INVALID
    }

    @Override
    public String execute() {
        HttpServletRequest request = ServletActionContext.getRequest();
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        if (uploads != null && !uploads.isEmpty()) {
            Map<String, FileStatus> filesStatusMap = processFiles(loggedInInfo);
            request.setAttribute("filesStatusMap", filesStatusMap);
        } else {
            // No files uploaded - return empty map to preserve original flow
            request.setAttribute("filesStatusMap", new HashMap<>());
        }

        return SUCCESS;
    }

    private Map<String, FileStatus> processFiles(LoggedInInfo loggedInInfo) {
        Map<String, FileStatus> filesStatusMap = new HashMap<>();

        for (int i = 0; i < uploads.size(); i++) {
            File file = uploads.get(i);
            String fileName = uploadsFileName.get(i);

            try (InputStream inputStream = new FileInputStream(file)) {
                String filePath = Utilities.saveFile(inputStream, fileName);
                HRMReport report = HRMReportParser.parseReport(loggedInInfo, filePath);
                FileStatus fileStatus = handleHRMReport(loggedInInfo, report);
                filesStatusMap.put(fileName, fileStatus);
            } catch (IOException e) {
                MiscUtils.getLogger().error("Error occurred while processing file '{}': {}", fileName, e);
                filesStatusMap.put(fileName, FileStatus.INVALID);
            }
        }

        return filesStatusMap;
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
