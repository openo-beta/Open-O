//CHECKSTYLE:OFF
package ca.openosp.openo.encounter.oceanEReferal.pageUtil;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.EReferAttachmentDao;
import ca.openosp.openo.commn.model.EReferAttachment;
import ca.openosp.openo.commn.model.EReferAttachmentData;
import ca.openosp.openo.commn.model.enumerator.DocumentType;
import ca.openosp.openo.documentManager.DocumentAttachmentManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class ERefer2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private static final Logger logger = MiscUtils.getLogger();
    private final DocumentAttachmentManager documentAttachmentManager = SpringUtils.getBean(DocumentAttachmentManager.class);
    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public String execute() {
        String method = request.getParameter("method");

        if (method != null) {
            if (method.equalsIgnoreCase("attachOceanEReferralConsult"))
                attachOceanEReferralConsult();
            else if (method.equalsIgnoreCase("editOceanEReferralConsult"))
                editOceanEReferralConsult();
            else if (method.equalsIgnoreCase("validateDocuments"))
                validateDocuments();
        }

        return SUCCESS;
    }

    private void writeResponse(boolean valid) {
        try (PrintWriter writer = response.getWriter()) {
            response.setContentType("application/json");
            writer.write(valid ? "{\"valid\":true}" : "{\"valid\":false}");
        } catch (IOException e) {
            logger.error("Failed to write validation response", e);
        }
    }

    public void validateDocuments() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_con", SecurityInfoManager.READ, null)) {
            writeResponse(false);
            return;
        }
        String demographicNo = request.getParameter("demographicNo");
        String[] documents = request.getParameterValues("documents");
        if (StringUtils.isNullOrEmpty(demographicNo) || documents == null || documents.length == 0) {
            // Nothing to validate - return true
            writeResponse(true);
            return;
        }
        boolean valid = documentAttachmentManager.validateDocumentsBelongToPatient(
                loggedInInfo, Integer.parseInt(demographicNo), documents);
        writeResponse(valid);
    }

    // Documents (attachments) originate from the consult request window.
    // Users can attach these documents using the attachment GUI on the consult request form.
    // All documents are internal to Oscar.
    public void attachOceanEReferralConsult() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_con", SecurityInfoManager.READ, null)) {
            throw new SecurityException("missing required sec object (_con)");
        }
        String demographicNo = StringUtils.isNullOrEmpty(request.getParameter("demographicNo")) ? "" : request.getParameter("demographicNo");
        String documents = StringUtils.isNullOrEmpty(request.getParameter("documents")) ? "" : request.getParameter("documents");
        if (documents.isEmpty() || demographicNo.isEmpty()) {
            return;
        }

        EReferAttachment eReferAttachment = new EReferAttachment(Integer.parseInt(demographicNo));
        List<EReferAttachmentData> attachments = new ArrayList<>();

        for (String document : documents.split("\\|")) {
            String type = document.replaceAll("\\d", "");
            Integer id = Integer.parseInt(document.substring(type.length()));
            EReferAttachmentData attachmentData = new EReferAttachmentData(eReferAttachment, id, type);
            attachments.add(attachmentData);
        }
        eReferAttachment.setAttachments(attachments);
        EReferAttachmentDao eReferAttachmentDao = SpringUtils.getBean(EReferAttachmentDao.class);
        eReferAttachmentDao.persist(eReferAttachment);

        try (PrintWriter writer = response.getWriter()) {
            writer.write(eReferAttachment.getId().toString());
        } catch (IOException e) {
            logger.error("Failed to write the eReferAttachment ID to the response", e);
        }
    }

    // Documents (attachments) originate from the consult request window.
    // Users can attach these documents using the attachment GUI on the consult request form.
    // All documents are internal to Oscar.
    public void editOceanEReferralConsult() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        String demographicNo = StringUtils.isNullOrEmpty(request.getParameter("demographicNo")) ? "" : request.getParameter("demographicNo");
        String requestId = StringUtils.isNullOrEmpty(request.getParameter("requestId")) ? "" : request.getParameter("requestId");
        String documents = StringUtils.isNullOrEmpty(request.getParameter("documents")) ? "" : request.getParameter("documents");
        if (documents.isEmpty() || demographicNo.isEmpty() || requestId.isEmpty()) {
            return;
        }

        List<String> docs = new ArrayList<>();
        List<String> labs = new ArrayList<>();
        List<String> eforms = new ArrayList<>();
        List<String> hrms = new ArrayList<>();

        for (String document : documents.split("\\|")) {
            String type = document.replaceAll("\\d", "");
            switch (type) {
                case "D":
                    docs.add(document.substring(type.length()));
                    break;
                case "L":
                    labs.add(document.substring(type.length()));
                    break;
                case "E":
                    eforms.add(document.substring(type.length()));
                    break;
                case "H":
                    hrms.add(document.substring(type.length()));
                    break;
            }
        }

        documentAttachmentManager.attachToConsult(loggedInInfo, DocumentType.DOC, docs.toArray(new String[0]), providerNo, Integer.parseInt(requestId), Integer.parseInt(demographicNo), Boolean.TRUE);
        documentAttachmentManager.attachToConsult(loggedInInfo, DocumentType.LAB, labs.toArray(new String[0]), providerNo, Integer.parseInt(requestId), Integer.parseInt(demographicNo), Boolean.TRUE);
        documentAttachmentManager.attachToConsult(loggedInInfo, DocumentType.EFORM, eforms.toArray(new String[0]), providerNo, Integer.parseInt(requestId), Integer.parseInt(demographicNo), Boolean.TRUE);
        documentAttachmentManager.attachToConsult(loggedInInfo, DocumentType.HRM, hrms.toArray(new String[0]), providerNo, Integer.parseInt(requestId), Integer.parseInt(demographicNo), Boolean.TRUE);
    }
}
