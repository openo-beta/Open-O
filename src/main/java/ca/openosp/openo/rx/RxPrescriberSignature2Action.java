package ca.openosp.openo.rx;

import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.managers.ProviderSignatureManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.prescript.pageUtil.RxSessionBean;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Saves the signature on a prescription when the print window first prints, faxes or makes a PDF of
 * it, and answers with the saved signature as JSON ({@code {"signature": "..."}}), which the window
 * then shows and prints.
 *
 * <p>Parameters: {@code scriptNo}, the prescription; {@code signatureId}, the prescriber's chosen
 * signature, or empty for their prescriber name; {@code demographicNo}, the patient of the
 * prescription window. A user who isn't the prescriber saves the prescriber's default, whatever they send.
 * The signature is null when there was none to save. A chosen signature deleted in another window
 * answers 409, so the window can ask for another pick.
 *
 * @since 2026-10-26
 */
public class RxPrescriberSignature2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final ProviderSignatureManager providerSignatureManager = SpringUtils.getBean(ProviderSignatureManager.class);

    /**
     * Saves the prescription's signature, unless it has one already, and writes it to the response.
     *
     * @return String null, the response is written here
     * @throws IOException when the response can't be written
     * @throws SecurityException when the user can't read prescriptions, or the prescription or
     *                           signature doesn't belong to this prescription window
     */
    @Override
    public String execute() throws IOException {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        // Read, like Reprint: a receptionist's first reprint saves the prescriber's default
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_rx", SecurityInfoManager.READ, null)) {
            throw new SecurityException("missing required sec object (_rx)");
        }

        // POST only, so CSRFGuard checks the token
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }

        // RxSessionFilter picks this window's patient from the demographicNo parameter
        RxSessionBean sessionBean = (RxSessionBean) request.getSession().getAttribute("RxSessionBean");
        Integer scriptNo;
        Integer signatureId;
        try {
            scriptNo = Integer.valueOf(request.getParameter("scriptNo"));
            String signatureIdParam = request.getParameter("signatureId");
            signatureId = StringUtils.isEmpty(signatureIdParam) ? null : Integer.valueOf(signatureIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        if (sessionBean == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        String signature;
        try {
            signature = providerSignatureManager.lockForPrescription(loggedInInfo, scriptNo, sessionBean.getDemographicNo(), signatureId);
        } catch (IllegalArgumentException e) {
            if (!ProviderSignatureManager.ERROR_DELETED.equals(e.getMessage())) {
                throw e;
            }
            // Deleted in another window after this one opened
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return null;
        }
        LogAction.addLog(loggedInInfo.getLoggedInProviderNo(), LogConst.UPDATE, LogConst.CON_PRESCRIPTION, String.valueOf(scriptNo),
                request.getRemoteAddr(), String.valueOf(sessionBean.getDemographicNo()), "prescriber signature");

        ObjectNode json = new ObjectMapper().createObjectNode();
        json.put("signature", signature);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.toString());
        return null;
    }
}
