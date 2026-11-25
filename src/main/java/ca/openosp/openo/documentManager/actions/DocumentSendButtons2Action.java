package ca.openosp.openo.documentManager.actions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.openo.documentManager.DocumentSendButtons;
import ca.openosp.openo.documentManager.EDoc;
import ca.openosp.openo.documentManager.EDocUtil;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Returns the document view's Fax and Email buttons for one document as JSON ({@link DocumentSendButtons}).
 *
 * <p>The document view builds the buttons when it loads. It calls this after a save, which may have linked the
 * document to a patient, so the buttons and the Email button's checks follow the saved link.</p>
 *
 * <p>Request: {@code GET documentManager/documentSendButtons.do?documentNo=<number>}. Responds 400 for a
 * missing or malformed number and 404 for an unknown document.</p>
 *
 * @since 2026-11-25
 */
public class DocumentSendButtons2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Writes the buttons for the requested document.
     *
     * @return String null, since the JSON is written to the response
     * @throws SecurityException if the user may not read documents, the right that opens the document view
     * @throws IOException if the response can't be written
     */
    @Override
    public String execute() throws IOException {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_edoc", SecurityInfoManager.READ, null)) {
            throw new SecurityException("missing required sec object (_edoc)");
        }

        String documentNo = request.getParameter("documentNo");
        if (documentNo == null || !documentNo.matches("\\d{1,9}")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        EDoc document = EDocUtil.getDoc(documentNo);
        // getDoc returns an empty EDoc for an unknown number
        if (document.getDocId() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.getOutputStream().write(DocumentSendButtons.forDocument(loggedInInfo, document).toJson()
                .getBytes(StandardCharsets.UTF_8));
        return null;
    }
}
