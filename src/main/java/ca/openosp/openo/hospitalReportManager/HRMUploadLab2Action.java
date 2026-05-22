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

import ca.openosp.openo.hospitalReportManager.UploadResult.FileStatus;
import ca.openosp.openo.lab.ca.all.util.Utilities;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.PathValidationUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.action.UploadedFilesAware;
import org.apache.struts2.dispatcher.multipart.UploadedFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HRMUploadLab2Action extends ActionSupport implements UploadedFilesAware {
    private List<UploadedFile> uploads;

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    // Allowed file types for HRM uploads
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "text/plain",
        "text/xml",
        "application/xml",
        "application/pdf",
        "image/jpeg",
        "image/png",
        "image/gif"
    );

    @Override
    public void withUploadedFiles(List<UploadedFile> uploadedFiles) {
        this.uploads = new ArrayList<>(uploadedFiles);
    }

    @Override
    public String execute() {
        HttpServletRequest request = ServletActionContext.getRequest();
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        // MANDATORY security check
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_hrm", "w", null)) {
            throw new SecurityException("missing required sec object (_hrm)");
        }

        if (uploads != null && !uploads.isEmpty()) {
            request.setAttribute("uploadResults", processFiles(loggedInInfo));
        } else {
            request.setAttribute("uploadResults", new HashMap<>());
        }

        return SUCCESS;
    }

    private Map<String, UploadResult> processFiles(LoggedInInfo loggedInInfo) {
        Map<String, UploadResult> resultsMap = new HashMap<>();

        for (int i = 0; i < uploads.size(); i++) {
            UploadedFile uf = uploads.get(i);
            File file = PathValidationUtils.toFile(uf);
            String fileName = uf.getOriginalName();
            String contentType = uf.getContentType();

            if (!isValidContentType(contentType)) {
                MiscUtils.getLogger().warn("Invalid content type '{}' for file '{}'", contentType, fileName);
                resultsMap.put(fileName, new UploadResult(FileStatus.INVALID, "Invalid content type: " + contentType));
                continue;
            }

            String sanitizedFileName = sanitizeFileName(fileName);
            if (sanitizedFileName == null || sanitizedFileName.isEmpty()) {
                MiscUtils.getLogger().error("Invalid filename provided: '{}'", fileName);
                resultsMap.put(fileName, new UploadResult(FileStatus.INVALID, "Invalid filename"));
                continue;
            }

            try (InputStream inputStream = new FileInputStream(file)) {
                String filePath = Utilities.saveFile(inputStream, sanitizedFileName);
                List<Throwable> parseErrors = new ArrayList<>();
                HRMReport report = HRMReportParser.parseReport(loggedInInfo, filePath, parseErrors);

                if (report == null) {
                    String errMsg = parseErrors.isEmpty() ? "Failed to parse HRM report" : parseErrors.get(0).getMessage();
                    resultsMap.put(fileName, new UploadResult(FileStatus.INVALID, errMsg));
                } else {
                    try {
                        List<String> warnings = new ArrayList<>();
                        HRMReportParser.addReportToInbox(loggedInInfo, report, warnings);
                        resultsMap.put(fileName, new UploadResult(FileStatus.COMPLETED, null, warnings));
                    } catch (Exception e) {
                        MiscUtils.getLogger().error("Couldn't handle uploaded HRM report", e);
                        resultsMap.put(fileName, new UploadResult(FileStatus.FAILED, e.getMessage()));
                    }
                }
            } catch (IOException e) {
                MiscUtils.getLogger().error("Error reading file '{}': {}", fileName, e);
                resultsMap.put(fileName, new UploadResult(FileStatus.INVALID, e.getMessage()));
            } catch (SecurityException e) {
                MiscUtils.getLogger().error("Security violation for file '{}': {}", fileName, e);
                resultsMap.put(fileName, new UploadResult(FileStatus.INVALID, e.getMessage()));
            }
        }

        return resultsMap;
    }

    /**
     * Validates that the content type is allowed for HRM uploads.
     * @param contentType the MIME type to validate
     * @return true if content type is allowed, false otherwise
     */
    private boolean isValidContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            return false;
        }

        // Normalize content type (remove charset, etc.)
        String normalizedType = contentType.toLowerCase().split(";")[0].trim();
        return ALLOWED_CONTENT_TYPES.contains(normalizedType);
    }

    /**
     * Sanitizes filename to prevent path traversal attacks and other security issues.
     * Based on the pattern from BillingDocumentErrorReportUpload2Action.
     * @param fileName the original filename from user upload
     * @return sanitized filename or null if invalid
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }

        // First normalize Unicode to prevent homoglyph attacks
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFKC);

        // URL decode to catch encoded traversal attempts like %2e%2e
        String decoded = normalized;
        try {
            // Decode multiple times to catch double-encoding
            for (int i = 0; i < 3; i++) {
                String temp = URLDecoder.decode(decoded, "UTF-8");
                if (temp.equals(decoded)) {
                    break; // No more encoding layers
                }
                decoded = temp;
            }
        } catch (Exception e) {
            // If decoding fails, reject the filename
            return null;
        }

        // Extract just the filename (remove any path components)
        String baseName = new File(decoded).getName();

        // Remove dangerous characters and sequences
        String sanitized = baseName
            .replaceAll("[\\\\/:*?\"<>|]", "") // Windows illegal chars
            .replaceAll("\\.{2,}", ".")        // Multiple dots (../)
            .replaceAll("^\\.", "")            // Leading dot
            .replaceAll("\\.$", "")            // Trailing dot
            .trim();

        // Additional security checks
        if (sanitized.isEmpty() ||
            sanitized.contains("..") ||
            sanitized.startsWith("/") ||
            sanitized.startsWith("\\") ||
            sanitized.length() > 255) {
            return null;
        }

        // Ensure we have a reasonable filename
        if (sanitized.length() < 1) {
            return null;
        }

        return sanitized;
    }
}
