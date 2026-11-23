package ca.openosp.openo.rx;

import ca.openosp.openo.commn.exception.ProviderSignatureException;
import ca.openosp.openo.managers.ProviderSignatureManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.managers.model.SignatureChoice;
import ca.openosp.openo.prescript.pageUtil.RxSessionBean;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Answers the prescription print window's signature card, as JSON.
 *
 * <p>A POST saves the signature on a prescription when the print window first prints, faxes or
 * makes a PDF of it, and answers with the saved signature ({@code {"signature": "..."}}), which the
 * window then shows and prints. Parameters: {@code scriptNo}, the prescription; {@code signatureId},
 * the prescriber's choice as {@link SignatureChoice#getPostedId()} writes it; {@code demographicNo},
 * the patient of the prescription window. A user who isn't the prescriber saves the prescriber's
 * default, whatever they send. The signature is null when there was none to save. A choice that is no
 * longer in the prescriber's list (deleted in another window) answers 409, so the window can ask for
 * another pick.
 *
 * <p>{@code method=choices} saves nothing: it answers with the list the card's dropdown shows now
 * ({@code {"choices": [{"id": "", "text": "...", "default": true}, ...]}}), so the window can update
 * the dropdown after the prescriber changes their signatures in Signature Preference. Parameters:
 * {@code scriptNo} and {@code demographicNo}. The list is empty when the user can't choose: the
 * signature is saved already, or they aren't the prescriber.
 *
 * @since 2026-10-26
 */
public class RxPrescriberSignature2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final ProviderSignatureManager providerSignatureManager = SpringUtils.getBean(ProviderSignatureManager.class);

    /**
     * Saves the prescription's signature, unless it has one already, and writes it to the response;
     * or, for {@code method=choices}, writes the card's current list.
     *
     * @return String null, the response is written here
     * @throws IOException when the response can't be written
     * @throws SecurityException when the user can't read prescriptions, or the prescription doesn't
     *                           belong to this prescription window
     */
    @Override
    public String execute() throws IOException {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        // Read, like Reprint: a receptionist's first reprint saves the prescriber's default
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_rx", SecurityInfoManager.READ, null)) {
            throw new SecurityException("missing required sec object (_rx)");
        }

        boolean listChoices = "choices".equals(request.getParameter("method"));
        // Saving is POST only, so CSRFGuard checks the token; listing the choices saves nothing
        if (!listChoices && !"POST".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }

        // RxSessionFilter picks this window's patient from the demographicNo parameter
        RxSessionBean sessionBean = (RxSessionBean) request.getSession().getAttribute("RxSessionBean");
        Integer scriptNo;
        Integer signatureId;
        try {
            scriptNo = Integer.valueOf(request.getParameter("scriptNo"));
            signatureId = SignatureChoice.parsePostedId(request.getParameter("signatureId"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        if (sessionBean == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }

        ObjectNode json = new ObjectMapper().createObjectNode();
        if (listChoices) {
            ArrayNode choices = json.putArray("choices");
            for (SignatureChoice choice : providerSignatureManager.getChoicesForPrescription(loggedInInfo, scriptNo, sessionBean.getDemographicNo())) {
                choices.addObject()
                        .put("id", choice.getPostedId())
                        .put("text", choice.getText())
                        .put("default", choice.isDefaultChoice());
            }
            // The list changes whenever the prescriber edits it, so the browser must not reuse an old answer
            response.setHeader("Cache-Control", "no-store");
        } else {
            try {
                json.put("signature", providerSignatureManager.lockForPrescription(loggedInInfo, scriptNo, sessionBean.getDemographicNo(), signatureId));
            } catch (ProviderSignatureException e) {
                // The choice isn't in the prescriber's list any more: deleted in another window after this one opened
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return null;
            }
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json.toString());
        return null;
    }
}
