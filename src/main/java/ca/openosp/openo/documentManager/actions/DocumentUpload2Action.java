//CHECKSTYLE:OFF
/**
 * Copyright (c) 2008-2012 Indivica Inc.
 * <p>
 * This software is made available under the terms of the
 * GNU General Public License, Version 2, 1991 (GPLv2).
 * License details are available via "indivica.ca/gplv2"
 * and "gnu.org/licenses/gpl-2.0.html".
 */

package ca.openosp.openo.documentManager.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.OscarProperties;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.model.ProgramProvider;
import ca.openosp.openo.commn.dao.ProviderInboxRoutingDao;
import ca.openosp.openo.commn.dao.QueueDocumentLinkDao;
import ca.openosp.openo.commn.dao.UserPropertyDAO;
import ca.openosp.openo.commn.model.UserProperty;
import ca.openosp.openo.documentManager.EDoc;
import ca.openosp.openo.documentManager.EDocUtil;
import ca.openosp.openo.documentManager.IncomingDocUtil;
import ca.openosp.openo.managers.ProgramManager2;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class DocumentUpload2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static Logger logger = MiscUtils.getLogger();
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() throws Exception {
        return executeUpload();
    }

    public String executeUpload() throws Exception {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_edoc", "w", null)) {
            throw new SecurityException("missing required sec object (_edoc)");
        }

        HashMap<String, Object> map = new HashMap<String, Object>();
        File docFile = this.getFiledata();
        String destination = request.getParameter("destination");
        ResourceBundle props = ResourceBundle.getBundle("oscarResources");
        if (docFile == null) {
            map.put("error", 4);
        } else if (destination != null && destination.equals("incomingDocs")) {
            String fileName = docFile.getName();
            if (!fileName.toLowerCase().endsWith(".pdf")) {
                map.put("error", props.getString("dms.documentUpload.onlyPdf"));
            } else if (docFile.length() == 0) {
                map.put("error", 4);
                throw new FileNotFoundException();
            } else {
                String queueId = request.getParameter("queue");
                String destFolder = request.getParameter("destFolder");

                // Sanitize filename to prevent path traversal
                String sanitizedFileName = sanitizeFileNameForIncomingDocs(fileName);
                File f = new File(IncomingDocUtil.getAndCreateIncomingDocumentFilePathName(queueId, destFolder, sanitizedFileName));
                if (f.exists()) {
                    map.put("error", fileName + " " + props.getString("dms.documentUpload.alreadyExists"));
                } else {
                    boolean success = writeToIncomingDocs(docFile, queueId, destFolder, sanitizedFileName);
                    if (!success) {
                        map.put("error", "Failed to write file. Please contact administrator");
                        MiscUtils.getLogger().error("Failed to write file to " + destFolder);
                    } else {
                        map.put("name", docFile.getName());
                        map.put("size", docFile.length());
                    }
                }

                request.getSession().setAttribute("preferredQueue", queueId);
                if (docFile != null) {
                    docFile.delete();
                    docFile = null;
                }

            }
        } else {
            int numberOfPages = 0;
            String fileName = MiscUtils.sanitizeFileName(docFile.getName());
            String user = (String) request.getSession().getAttribute("user");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            EDoc newDoc = new EDoc("", "", fileName, "", user, user, this.getSource(), 'A',
                    simpleDateFormat.format(Calendar.getInstance().getTime()),
                    "", "", "demographic", "-1", 0);
            newDoc.setDocPublic("0");

            // if the document was added in the context of a program
            ProgramManager2 programManager = SpringUtils.getBean(ProgramManager2.class);
            LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
            ProgramProvider pp = programManager.getCurrentProgramInDomain(loggedInInfo, loggedInInfo.getLoggedInProviderNo());
            if (pp != null && pp.getProgramId() != null) {
                newDoc.setProgramId(pp.getProgramId().intValue());
            }

            fileName = newDoc.getFileName();
            // save local file;
            if (docFile.length() == 0) {
                map.put("error", 4);
                throw new FileNotFoundException();
            }

            // write file to local dir
            writeLocalFile(docFile, fileName);
            //newDoc.setContentType(docFile.getContentType());
            if (fileName.endsWith(".PDF") || fileName.endsWith(".pdf")) {
                newDoc.setContentType("application/pdf");
                // get number of pages when document is a PDF
                numberOfPages = countNumOfPages(fileName);
            }
            newDoc.setNumberOfPages(numberOfPages);
            String doc_no = EDocUtil.addDocumentSQL(newDoc);
            LogAction.addLog((String) request.getSession().getAttribute("user"), LogConst.ADD, LogConst.CON_DOCUMENT, doc_no, request.getRemoteAddr());

            String providerId = request.getParameter("providers");
            if (providerId != null) {
                WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
                ProviderInboxRoutingDao providerInboxRoutingDao = (ProviderInboxRoutingDao) ctx.getBean(ProviderInboxRoutingDao.class);
                providerInboxRoutingDao.addToProviderInbox(providerId, Integer.parseInt(doc_no), "DOC");
            }

            String queueId = request.getParameter("queue");
            if (queueId != null && !queueId.equals("-1")) {
                WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
                QueueDocumentLinkDao queueDocumentLinkDAO = (QueueDocumentLinkDao) ctx.getBean(QueueDocumentLinkDao.class);
                Integer qid = Integer.parseInt(queueId.trim());
                Integer did = Integer.parseInt(doc_no.trim());
                queueDocumentLinkDAO.addActiveQueueDocumentLink(qid, did);
                request.getSession().setAttribute("preferredQueue", queueId);
            }

            map.put("name", docFile.getName());
            map.put("size", docFile.length());

            if (docFile != null) {
                docFile.delete();
                docFile = null;
            }
        }
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = JSONObject.fromObject(map);
        jsonArray.add(jsonObject);
        response.getOutputStream().write(jsonArray.toString().getBytes());
        return null;
    }

    /**
     * Counts the number of pages in a local pdf file.
     *
     * @param fileName the name of the file
     * @return the number of pages in the file
     */
    public int countNumOfPages(String fileName) {
        return EDocUtil.getPDFPageCount(fileName);
    }

    /**
     * Writes an uploaded file to disk
     *
     * @param docFile  the uploaded file
     * @param fileName the name for the file on disk
     * @throws Exception when an error occurs
     */
    private void writeLocalFile(File docFile, String fileName) throws Exception {
        InputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = Files.newInputStream(docFile.toPath());
            String savePath = OscarProperties.getInstance().getProperty("DOCUMENT_DIR") + "/" + fileName;
            fos = new FileOutputStream(savePath);
            byte[] buf = new byte[128 * 1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        } finally {
            if (fis != null)
                fis.close();
            if (fos != null)
                fos.close();
        }
    }

    private boolean writeToIncomingDocs(File docFile, String queueId, String PdfDir, String fileName) {
        // Validate inputs to prevent path traversal attacks
        // Note: These validations are redundant as inputs are already validated before calling this method,
        // but we keep them for defense in depth
        if (queueId == null || PdfDir == null || fileName == null) {
            logger.error("Invalid parameters provided for writeToIncomingDocs");
            return false;
        }

        // The filename should already be sanitized by the caller, but validate again for defense in depth
        // Check that filename doesn't contain path traversal sequences
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            logger.error("Filename contains invalid path characters");
            return false;
        }
        
        String parentPath = IncomingDocUtil.getIncomingDocumentFilePath(queueId, PdfDir);
        if (!new File(parentPath).exists()) {
            return false;
        }

        String savePath = IncomingDocUtil.getAndCreateIncomingDocumentFilePathName(queueId, PdfDir, fileName);
            
        // Write the file
        try (InputStream fis = Files.newInputStream(docFile.toPath());
                FileOutputStream fos = new FileOutputStream(savePath)) {
            IOUtils.copy(fis, fos);
        } catch (IOException e) {
            logger.error("Error writing file to incoming docs", e);
            return false;
        }

        return true;
    }
    
    /**
     * Sanitizes a filename for use in incoming documents to prevent path traversal attacks.
     * 
     * @param fileName the original filename
     * @return sanitized filename safe for filesystem operations
     */
    private String sanitizeFileNameForIncomingDocs(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        
        // Get just the filename component (removes any path)
        File file = new File(fileName);
        String baseName = file.getName();
        
        // Remove any path traversal sequences and dangerous characters
        baseName = baseName.replaceAll("\\.\\.", "")  // Remove ..
                          .replaceAll("[\\\\/]", "")   // Remove any slashes
                          .replaceAll("\\$", "")        // Remove $
                          .replaceAll("~", "")          // Remove ~
                          .replaceAll(":", "")          // Remove colons
                          .replaceAll("\\|", "")        // Remove pipes
                          .replaceAll("<", "")          // Remove less than
                          .replaceAll(">", "")          // Remove greater than
                          .replaceAll("\"", "")         // Remove quotes
                          .replaceAll("\\*", "")        // Remove asterisks
                          .replaceAll("\\?", "");       // Remove question marks
        
        // Ensure the filename ends with .pdf (case insensitive)
        if (!baseName.toLowerCase().endsWith(".pdf")) {
            return null;
        }
        
        // Additional validation - ensure the filename is not empty after sanitization
        if (baseName.trim().isEmpty() || baseName.equals(".pdf")) {
            return null;
        }
        
        return baseName;
    }

    public String setUploadDestination() {

        String user_no = (String) request.getSession().getAttribute("user");
        String destination = request.getParameter("destination");
        UserPropertyDAO pref = (UserPropertyDAO) SpringUtils.getBean(UserPropertyDAO.class);
        UserProperty up = pref.getProp(user_no, UserProperty.UPLOAD_DOCUMENT_DESTINATION);

        if (up == null) {
            up = new UserProperty();
            up.setName(UserProperty.UPLOAD_DOCUMENT_DESTINATION);
            up.setProviderNo(user_no);
        }

        if (up.getValue() == null || !(up.getValue().equals(destination))) {
            up.setValue(destination);
            pref.saveProp(up);
        }
        return null;
    }

    public String setUploadIncomingDocumentFolder() {


        String user_no = (String) request.getSession().getAttribute("user");
        String destFolder = request.getParameter("destFolder");
        UserPropertyDAO pref = (UserPropertyDAO) SpringUtils.getBean(UserPropertyDAO.class);
        UserProperty up = pref.getProp(user_no, UserProperty.UPLOAD_INCOMING_DOCUMENT_FOLDER);

        if (up == null) {
            up = new UserProperty();
            up.setName(UserProperty.UPLOAD_INCOMING_DOCUMENT_FOLDER);
            up.setProviderNo(user_no);
        }

        if (up.getValue() == null || !(up.getValue().equals(destFolder))) {
            up.setValue(destFolder);
            pref.saveProp(up);
        }
        return null;
    }

    private String function = "";
    private String functionId = "";
    private String docType = "";
    private String docDesc = "";
    private String docCreator = "";
    private String responsibleId = "";
    private String source = "";
    private File docFile;

    private File filedata;

    private String docPublic = "";
    private String mode = "";
    private String observationDate = "";
    private String reviewerId = "";
    private String reviewDateTime = "";
    private boolean reviewDoc = false;
    private String html = "";

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocDesc() {
        return docDesc;
    }

    public void setDocDesc(String docDesc) {
        this.docDesc = docDesc;
    }


    public String getDocCreator() {
        return docCreator;
    }

    public void setDocCreator(String docCreator) {
        this.docCreator = docCreator;
    }

    public String getResponsibleId() {
        return responsibleId;
    }

    public void setResponsibleId(String responsibleId) {
        this.responsibleId = responsibleId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public File getDocFile() {
        return docFile;
    }

    public void setDocFile(File docFile) {
        this.docFile = docFile;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDocPublic() {
        return docPublic;
    }

    public void setDocPublic(String docPublic) {
        this.docPublic = docPublic;
    }

    public String getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(String observationDate) {
        this.observationDate = observationDate;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewDateTime() {
        return reviewDateTime;
    }

    public void setReviewDateTime(String reviewDateTime) {
        this.reviewDateTime = reviewDateTime;
    }

    public boolean getReviewDoc() {
        return reviewDoc;
    }

    public void setReviewDoc(boolean reviewDoc) {
        this.reviewDoc = reviewDoc;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public File getFiledata() {
        return filedata;
    }

    public void setFiledata(File Filedata) {
        this.filedata = Filedata;
    }
}
