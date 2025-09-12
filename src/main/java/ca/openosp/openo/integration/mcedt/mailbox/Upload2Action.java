/**
 * Copyright (c) 2014-2015. KAI Innovations Inc. All Rights Reserved.
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
package ca.openosp.openo.integration.mcedt.mailbox;

import ca.ontario.health.edt.*;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.io.FilenameUtils;
import org.apache.cxf.helpers.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import ca.openosp.openo.integration.mcedt.DelegateFactory;
import ca.openosp.openo.integration.mcedt.McedtMessageCreator;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.OscarProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Upload2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();


    private static Logger logger = MiscUtils.getLogger();

    @Override
    public String execute() throws Exception {
        String method = request.getParameter("method");
        if ("cancelUpload".equals(method)) {
            return cancelUpload();
        } else if ("addNew".equals(method)) {
            return addNew();
        } else if ("removeSelected".equals(method)) {
            return removeSelected();
        } else if ("uploadToMcedt".equals(method)) {
            return uploadToMcedt();
        } else if ("submitToMcedt".equals(method)) {
            return submitToMcedt();
        } else if ("uploadSubmitToMcedt".equals(method)) {
            return uploadSubmitToMcedt();
        } else if ("deleteUpload".equals(method)) {
            return deleteUpload();
        } else if ("addUpload".equals(method)) {
            return addUpload();
        }

        ActionUtils.removeSuccessfulUploads(request);
        ActionUtils.removeUploadResponseResults(request);
        ActionUtils.removeSubmitResponseResults(request);
        Date startDate = ActionUtils.getOutboxTimestamp();
        Date endDate = new Date();
        if (startDate != null && endDate != null) {
            ActionUtils.moveOhipToOutBox(startDate, endDate);
            ActionUtils.moveObecToOutBox(startDate, endDate);
            ActionUtils.setOutboxTimestamp(endDate);
        }
        ActionUtils.setUploadResourceId(request, new BigInteger("-1"));

        return SUCCESS;
    }

    public String cancelUpload() throws Exception {
        ActionUtils.removeUploadResourceId(request);
        ActionUtils.removeUploadFileName(request);
        List<File> files = ActionUtils.getSuccessfulUploads(request);

        try {
            // Get validated sent directory
            File sent = getValidatedSentDirectory();
            
            if (files != null && files.size() > 0) {
                for (File file : files) {
                    ActionUtils.moveFileToDirectory(file, sent, false, true);
                }
            }
        } catch (IOException e) {
            logger.error("A exception has occured while moving files at " + new Date());

            String errorMessage = McedtMessageCreator.exceptionToString(e);
            addActionError(getText("uploadAction.upload.faultException", new String[]{errorMessage}));
            return "failure";
        }
        ActionUtils.removeSuccessfulUploads(request);
        ActionUtils.removeUploadResponseResults(request);
        ActionUtils.removeSubmitResponseResults(request);

        return "cancel";
    }

    public String addNew() throws Exception {
        return "addNew";
    }

    public String removeSelected() throws Exception {
        return SUCCESS;
    }

    public String uploadToMcedt() {
        if (this.getResourceId().equals(new BigInteger("-1"))) {
            List<UploadData> uploads = new ArrayList<UploadData>();
            uploads.add(toUpload());

            try {
                EDTDelegate delegate = DelegateFactory.getEDTDelegateInstance(ActionUtils.getServiceId(this.getDescription()));
                ResourceResult result;

                try {
                    result = delegate.upload(uploads);
                } catch (Faultexception e) {
                    logger.error("A fault exception has occured while auto uploading MCEDT files at " + new Date());

                    String errorMessage = McedtMessageCreator.exceptionToString(e);
                    addActionError(getText("uploadAction.upload.failure", new String[]{errorMessage}));
                    return "failure";
                }

                if (result.getResponse().get(0).getResult().getCode().equals("IEDTS0001")) {
                    ActionUtils.setUploadResourceId(request, result.getResponse().get(0).getResourceID());
                    // Sanitize filename to prevent path traversal
                    String sanitizedFileName = FilenameUtils.getName(this.getFileName());
                    File outboxDir = getValidatedOutboxDirectory();
                    File file = new File(outboxDir, sanitizedFileName);
                    ActionUtils.setSuccessfulUploads(request, file);
                } else {
                    ActionUtils.setUploadResourceId(request, new BigInteger("-2"));
                    result.getResponse().get(0).setDescription(this.getFileName()); //this is done because error response has null description
                    ActionUtils.setSubmitResponseResults(request, result.getResponse().get(0));// if upload fails, submission is also assumed failed

                }
                ActionUtils.setUploadedFileName(request, this.getFileName());
                ActionUtils.setUploadResponseResults(request, result.getResponse().get(0));

                return SUCCESS;

            } catch (Exception e) {
                logger.error("Unable to upload to MCEDT", e);

                String errorMessage = McedtMessageCreator.exceptionToString(e);
                addActionError(getText("uploadAction.upload.failure", new String[]{errorMessage}));
                return "failure";
            }

        }
        return SUCCESS;
    }

    public String submitToMcedt() {
        if (!this.getResourceId().equals(new BigInteger("-2"))) {
            List<BigInteger> ids = new ArrayList<BigInteger>();
            ids.add(this.getResourceId());
            try {
                EDTDelegate delegate = DelegateFactory.getEDTDelegateInstance(ActionUtils.getServiceId(this.getFileName()));
                ResourceResult result;

                try {
                    result = delegate.submit(ids);
                } catch (Faultexception e) {
                    logger.error("A fault exception has occured while auto submitting MCEDT files at " + new Date());

                    String errorMessage = McedtMessageCreator.exceptionToString(e);
                    addActionError(getText("uploadAction.submit.failure", new String[]{errorMessage}));
                    return "failure";
                }

                if (!result.getResponse().get(0).getResult().getCode().equals("IEDTS0001")) {
                    result.getResponse().get(0).setDescription(this.getFileName());
                }
                ActionUtils.setSubmitResponseResults(request, result.getResponse().get(0));
                ActionUtils.setUploadResourceId(request, new BigInteger("-1"));
                return SUCCESS;
            } catch (Exception e) {
                logger.error("Unable to submit", e);

                String errorMessage = McedtMessageCreator.exceptionToString(e);
                addActionError(getText("uploadAction.submit.failure", new String[]{errorMessage}));
                return "failure";
            }

        } else {//if file has failed at upload level, no need to try submit
            ActionUtils.setUploadResourceId(request, new BigInteger("-1"));
            return SUCCESS;
        }
    }

    public String uploadSubmitToMcedt() {
        try {
            List<String> successUploads = new ArrayList<String>();
            List<String> failUploads = new ArrayList<String>();
            List<String> successSubmits = new ArrayList<String>();
            List<String> failSubmits = new ArrayList<String>();
            List<UploadData> uploads = toUploadMultipe();
            for (UploadData upload : uploads) {
                List<UploadData> uploadData = new ArrayList<UploadData>();
                uploadData.add(upload);
                EDTDelegate delegate = DelegateFactory.getEDTDelegateInstance(ActionUtils.getServiceId(upload.getDescription()));
                ResourceResult result;

                try {
                    result = delegate.upload(uploadData);
                } catch (Faultexception e) {
                    logger.error("A fault exception has occured while manually uploading MCEDT files at " + new Date());

                    String errorMessage = McedtMessageCreator.exceptionToString(e);
                    addActionError(getText("uploadAction.submit.failure", new String[]{errorMessage}));
                    return "failure";
                }

                List<BigInteger> ids = new ArrayList<BigInteger>();
                
                // Get validated directories
                File sent = getValidatedSentDirectory();
                File outboxDir = getValidatedOutboxDirectory();
                String canonicalOutboxPath = outboxDir.getCanonicalPath();
                
                for (ResponseResult edtResponse : result.getResponse()) {
                    if (edtResponse.getResult().getCode().equals("IEDTS0001")) {
                        ids.add(edtResponse.getResourceID());
                        // Sanitize filename to prevent path traversal
                        String sanitizedFileName = FilenameUtils.getName(edtResponse.getDescription());
                        File file = new File(outboxDir, sanitizedFileName);
                        
                        // Validate canonical path before moving file
                        try {
                            String canonicalFilePath = file.getCanonicalPath();
                            
                            if (!canonicalFilePath.startsWith(canonicalOutboxPath + File.separator)) {
                                logger.error("Attempted path traversal detected for file move: " + edtResponse.getDescription());
                                continue; // Skip this file and continue with others
                            }
                            
                            ActionUtils.moveFileToDirectory(file, sent, false, true);
                            successUploads.add(McedtMessageCreator.resourceResultToString(result));
                        } catch (IOException ex) {
                            logger.error("Error validating file path for move: " + sanitizedFileName, ex);
                            failUploads.add(edtResponse.getDescription() + ": Path validation error");
                        }
                    } else {
                        edtResponse.setDescription(upload.getDescription());
                        failUploads.add(edtResponse.getDescription() + ": " + edtResponse.getResult().getMsg());
                    }
                }
                if (ids.size() > 0) {

                    try {
                        result = delegate.submit(ids);
                    } catch (Faultexception e) {
                        logger.error("A fault exception has occured while manually submitting MCEDT files at " + new Date());

                        String errorMessage = McedtMessageCreator.exceptionToString(e);
                        addActionError(getText("uploadAction.submit.failure", new String[]{errorMessage}));
                        return "failure";
                    }

                    for (ResponseResult edtResponse : result.getResponse()) {
                        if (edtResponse.getResult().getCode().equals("IEDTS0001")) {
                            successSubmits.add(McedtMessageCreator.resourceResultToString(result));
                        } else {
                            edtResponse.setDescription(upload.getDescription());
                            failSubmits.add(edtResponse.getDescription() + ": " + edtResponse.getResult().getMsg());
                        }
                    }
                }
            }
            // Finally save all the messages/errors
            // we don't need to find out if upload is successful, we rather get info about submit status of that file
            //if ( successUploads!=null && successUploads.size()>0 ) messages = ActionUtils.addMoreMessage(messages, "uploadAction.upload.success", McedtMessageCreator.stringListToString(successUploads));
            if (successSubmits != null && successSubmits.size() > 0) {
                addActionMessage(getText("uploadAction.submit.success", new String[]{McedtMessageCreator.stringListToString(successSubmits)}));
            }

            String key = "";
            String val = "";
            if (failUploads != null && failUploads.size() > 0)
                addActionError(getText("uploadAction.upload.failure", new String[]{McedtMessageCreator.stringListToString(failUploads)}));
            if (failSubmits != null && failSubmits.size() > 0)
                addActionError(getText("uploadAction.submit.failure", new String[]{McedtMessageCreator.stringListToString(failSubmits)}));

        } catch (IOException e) {
            logger.error("An IO Exception has occured while moving the files to the sent folder at " + new Date(), e);

            String errorMessage = McedtMessageCreator.exceptionToString(e);
            addActionError(getText("uploadAction.upload.submit.failure", new String[]{errorMessage}));
        } catch (Exception e) {
            logger.error("Unable to Upload/Submit file", e);

            String errorMessage = McedtMessageCreator.exceptionToString(e);
            addActionError(getText("uploadAction.upload.submit.failure", new String[]{errorMessage}));
        }
        return SUCCESS;
    }

    public String deleteUpload() {
        try {
            List<String> fileNames = Arrays.asList(this.getFileName().trim().split(","));
            
            // Get validated outbox directory
            File outboxDir = getValidatedOutboxDirectory();
            String canonicalOutboxPath = outboxDir.getCanonicalPath();
            
            for (String fileName : fileNames) {
                // Sanitize filename to prevent path traversal
                String sanitizedFileName = FilenameUtils.getName(fileName);
                File file = new File(outboxDir, sanitizedFileName);
                
                // Validate canonical path to ensure file is within the outbox directory
                String canonicalFilePath = file.getCanonicalPath();
                
                if (!canonicalFilePath.startsWith(canonicalOutboxPath + File.separator)) {
                    logger.error("Attempted path traversal detected for file deletion: " + fileName);
                    throw new SecurityException("Invalid file path for deletion");
                }
                
                file.delete();
            }

        } catch (IOException e) {
            logger.error("IO error while deleting file", e);
            String errorMessage = McedtMessageCreator.exceptionToString(e);
            addActionError(getText("uploadAction.upload.submit.failure", new String[]{errorMessage}));
        } catch (SecurityException e) {
            logger.error("Security exception while deleting file", e);
            String errorMessage = McedtMessageCreator.exceptionToString(e);
            addActionError(getText("uploadAction.upload.submit.failure", new String[]{errorMessage}));
        } catch (Exception e) {
            logger.error("Unable to Delete file", e);
            String errorMessage = McedtMessageCreator.exceptionToString(e);
            addActionError(getText("uploadAction.upload.submit.failure", new String[]{errorMessage}));
        }
        return SUCCESS;
    }

    public String addUpload() {
        // Sanitize filename first to prevent path traversal
        String sanitizedFileName = FilenameUtils.getName(this.getFileName());
        
        if (!ActionUtils.isOBECFile(sanitizedFileName) && !ActionUtils.isOHIPFile(sanitizedFileName)) {
            addActionError(getText("uploadAction.upload.add.failure", new String[]{sanitizedFileName + " is not a supported file Name. Please upload only claim/OBEC files"}));
            return "failure";
        } else {
            try {
                // Get validated outbox directory
                File outboxDir = getValidatedOutboxDirectory();
                File myFile = new File(outboxDir, sanitizedFileName);
                
                // Validate canonical path to ensure file is within the outbox directory
                String canonicalFilePath = myFile.getCanonicalPath();
                String canonicalOutboxPath = outboxDir.getCanonicalPath();
                
                if (!canonicalFilePath.startsWith(canonicalOutboxPath + File.separator)) {
                    logger.error("Attempted path traversal detected for file upload: " + this.getFileName());
                    throw new SecurityException("Invalid file path for upload");
                }
                
                try (FileOutputStream outputStream = new FileOutputStream(myFile)) {
                    outputStream.write(Files.readAllBytes(this.getAddUploadFile().toPath()));
                    addActionMessage(getText("uploadAction.upload.add.success", new String[]{sanitizedFileName + " is succesfully added to the uploads list!"}));
                }
            } catch (IOException e) {
                logger.error("An error has occured with the addUpload file at " + new Date(), e);
                String errorMessage = McedtMessageCreator.exceptionToString(e);
                addActionError(getText("uploadAction.upload.add.failure", new String[]{errorMessage}));
                return "failure";
            } catch (SecurityException e) {
                logger.error("Security exception while adding upload file", e);
                String errorMessage = McedtMessageCreator.exceptionToString(e);
                addActionError(getText("uploadAction.upload.add.failure", new String[]{errorMessage}));
                return "failure";
            } catch (Exception e) {
                logger.error("Unable to Add file upload", e);
                String errorMessage = McedtMessageCreator.exceptionToString(e);
                addActionError(getText("uploadAction.upload.add.failure", new String[]{errorMessage}));
                return "failure";
            }
        }

        return SUCCESS;

    }

    public UploadData toUpload() {
        UploadData result = new UploadData();
        result.setDescription(this.getDescription());
        result.setResourceType(this.getResourceType());
        
        // Sanitize filename to prevent path traversal
        String sanitizedFileName = FilenameUtils.getName(this.getFileName());
        
        try {
            // Get validated outbox directory
            File outboxDir = getValidatedOutboxDirectory();
            File file = new File(outboxDir, sanitizedFileName);
            
            // Validate canonical path to ensure file is within the outbox directory
            String canonicalFilePath = file.getCanonicalPath();
            String canonicalOutboxPath = outboxDir.getCanonicalPath();
            
            if (!canonicalFilePath.startsWith(canonicalOutboxPath + File.separator)) {
                logger.error("Attempted path traversal detected for file: " + this.getFileName());
                throw new SecurityException("Invalid file path");
            }
            
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[fis.available()];
                fis.read(data);
                result.setContent(data);
            }
        } catch (IOException e) {
            logger.error("Unable to read upload file: " + sanitizedFileName, e);
            throw new RuntimeException("Unable to read upload file", e);
        } catch (SecurityException e) {
            logger.error("Security exception while processing file", e);
            throw new RuntimeException("Security error processing file", e);
        }
        return result;
    }

    public List<UploadData> toUploadMultipe() {
        List<UploadData> results = new ArrayList<UploadData>();
        List<String> fileNames = Arrays.asList(this.getFileName().trim().split(","));
        List<String> resourceTypes = Arrays.asList(this.getResourceType().trim().split(","));
        if (fileNames.size() == resourceTypes.size()) {
            try {
                // Get validated outbox directory
                File outboxDir = getValidatedOutboxDirectory();
                String canonicalOutboxPath = outboxDir.getCanonicalPath();
                
                for (int i = 0; i < fileNames.size(); i++) {
                    // Sanitize filename to prevent path traversal
                    String sanitizedFileName = FilenameUtils.getName(fileNames.get(i));
                    
                    // Construct file path safely
                    File file = new File(outboxDir, sanitizedFileName);
                    
                    // Validate canonical path to ensure file is within the outbox directory
                    String canonicalFilePath = file.getCanonicalPath();
                    
                    if (!canonicalFilePath.startsWith(canonicalOutboxPath + File.separator)) {
                        logger.error("Attempted path traversal detected for file: " + fileNames.get(i));
                        throw new SecurityException("Invalid file path");
                    }
                    
                    UploadData result = new UploadData();
                    result.setDescription(fileNames.get(i));
                    result.setResourceType(resourceTypes.get(i));
                    
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] data = new byte[fis.available()];
                        fis.read(data);
                        result.setContent(data);
                        results.add(result);
                    }
                }
            } catch (IOException e) {
                logger.error("Unable to process upload files", e);
                throw new RuntimeException("Unable to process upload files", e);
            } catch (SecurityException e) {
                logger.error("Security exception while processing files", e);
                throw new RuntimeException("Security error processing files", e);
            }
        }
        return results;
    }

    private String description;
    private String resourceType;
    private String fileName;
    private BigInteger resourceId;
    private File addUploadFile;
    private String addUploadFileFileName;
    private String addUploadFileContentType;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public BigInteger getResourceId() {
        return resourceId;
    }

    public void setResourceId(BigInteger resourceId) {
        this.resourceId = resourceId;
    }

    public File getAddUploadFile() {
        return addUploadFile;
    }

    public void setAddUploadFile(File addUploadFile) {
        this.addUploadFile = addUploadFile;
    }

    public String getAddUploadFileFileName() {
        return addUploadFileFileName;
    }
    public void setAddUploadFileFileName(String addUploadFileFileName) {
        this.addUploadFileFileName = addUploadFileFileName;
        this.setFileName(addUploadFileFileName); // set the file name to the upload file name
    }

    public String getAddUploadFileContentType() {
        return addUploadFileContentType;
    }
    public void setAddUploadFileContentType(String addUploadFileContentType) {
        this.addUploadFileContentType = addUploadFileContentType;
        this.setResourceType(addUploadFileContentType); // set the resource type to the upload file content type
    }
    
    /**
     * Validates and returns the canonical path of the ONEDT_OUTBOX directory.
     * This method ensures the configured path is valid and prevents path traversal attacks.
     * 
     * @return File object representing the validated outbox directory
     * @throws IOException if canonical path cannot be determined
     * @throws SecurityException if the path is invalid or potentially malicious
     */
    private File getValidatedOutboxDirectory() throws IOException {
        return getValidatedDirectory("ONEDT_OUTBOX");
    }
    
    /**
     * Validates and returns the canonical path of the ONEDT_SENT directory.
     * This method ensures the configured path is valid and prevents path traversal attacks.
     * 
     * @return File object representing the validated sent directory
     * @throws IOException if canonical path cannot be determined
     * @throws SecurityException if the path is invalid or potentially malicious
     */
    private File getValidatedSentDirectory() throws IOException {
        return getValidatedDirectory("ONEDT_SENT");
    }
    
    /**
     * Validates and returns the canonical path of a configured directory.
     * This method ensures the configured path is valid and prevents path traversal attacks.
     * 
     * @param propertyName The property name for the directory path
     * @return File object representing the validated directory
     * @throws IOException if canonical path cannot be determined
     * @throws SecurityException if the path is invalid or potentially malicious
     */
    private File getValidatedDirectory(String propertyName) throws IOException {
        OscarProperties props = OscarProperties.getInstance();
        String dirPath = props.getProperty(propertyName, "");
        
        // Validate the directory path configuration
        if (dirPath == null || dirPath.trim().isEmpty()) {
            throw new SecurityException(propertyName + " path not configured");
        }
        
        File dir = new File(dirPath);
        
        // Get canonical path to resolve any path traversal attempts in configuration
        String canonicalPath = dir.getCanonicalPath();
        File canonicalDir = new File(canonicalPath);
        
        // Ensure the directory exists and is actually a directory
        if (!canonicalDir.exists()) {
            // Try to create it if it doesn't exist
            if (!canonicalDir.mkdirs()) {
                throw new SecurityException("Unable to create " + propertyName + " directory: " + canonicalPath);
            }
        } else if (!canonicalDir.isDirectory()) {
            throw new SecurityException(propertyName + " path is not a directory: " + canonicalPath);
        }
        
        return canonicalDir;
    }
}