//CHECKSTYLE:OFF
/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
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


package ca.openosp.openo.prescript.pageUtil;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import ca.openosp.openo.commn.dao.DrugReasonDao;
import ca.openosp.openo.commn.dao.Icd9Dao;
import ca.openosp.openo.commn.model.DrugReason;
import ca.openosp.openo.commn.model.Icd9;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * Struts2 action for managing drug prescription reasons and ICD codes.
 * <p>
 * This action handles the association of clinical reasons (ICD-9 codes)
 * with prescribed medications. It provides functionality to add new drug
 * reasons and archive existing ones. Drug reasons help establish clinical
 * justification for prescriptions and support clinical decision-making.
 * <p>
 * The action integrates with ICD-9 code validation to ensure proper
 * clinical coding and maintains audit logs for all reason management
 * operations.
 *
 * @since 2008
 */
public final class RxReason2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters and session */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for handling the response */
    HttpServletResponse response = ServletActionContext.getResponse();

    /** Security manager for validating user permissions */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    /**
     * Main execution method for drug reason management.
     * <p>
     * Routes to the appropriate method based on the "method" parameter:
     * - archiveReason: Archives an existing drug reason
     * - Default: Adds a new drug reason
     *
     * @return String the result status for the performed operation
     */
    public String execute() {
        if ("archiveReason".equals(request.getParameter("method"))) {
            return archiveReason();
        }
        return addDrugReason();
    }

    /*
     * Needed for a new Drug Reason
     *
    private Integer drugId = null;
    private String codingSystem = null;    // (icd9,icd10,etc...) OR protocol
    private String code = null;   // (250 (for icd9) or could be the protocol identifier )
    private String comments = null;
    private Boolean primaryReasonFlag;
    private String providerNo = null;
    private Integer demographicNo = null;
     */
    /**
     * Adds a new drug reason with ICD-9 code validation.
     * <p>
     * This method validates parameters, checks ICD-9 codes, and creates
     * new drug reason entries with proper audit logging.
     *
     * @return String "close" if successful, SUCCESS with error message if validation fails
     */
    public String addDrugReason() {
        // Validate user has permission to access prescription functionality
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_rx", "r", null)) {
            throw new RuntimeException("missing required sec object (_rx)");
        }

        DrugReasonDao drugReasonDao = (DrugReasonDao) SpringUtils.getBean(DrugReasonDao.class);
        Icd9Dao icd9Dao = (Icd9Dao) SpringUtils.getBean(Icd9Dao.class);

        String codingSystem = request.getParameter("codingSystem");
        String primaryReasonFlagStr = request.getParameter("primaryReasonFlag");
        String comments = request.getParameter("comments");
        String code = request.getParameter("code");

        String drugIdStr = request.getParameter("drugId");
        String demographicNo = request.getParameter("demographicNo");
        String providerNo = (String) request.getSession().getAttribute("user");

        request.setAttribute("drugId", Integer.parseInt(drugIdStr));
        request.setAttribute("demoNo", Integer.parseInt(demographicNo));

        if (code != null && code.trim().equals("")) {
            request.setAttribute("message", getText("SelectReason.error.codeEmpty"));
            return SUCCESS;
        }

        List<Icd9> list = icd9Dao.getIcd9Code(code);
        if (list.size() == 0) {
            request.setAttribute("message", getText("SelectReason.error.codeValid"));
            return SUCCESS;
        }

        if (drugReasonDao.hasReason(Integer.parseInt(drugIdStr), codingSystem, code, true)) {
            request.setAttribute("message", getText("SelectReason.error.duplicateCode"));
            return SUCCESS;
        }

        MiscUtils.getLogger().debug("addDrugReasonCalled codingSystem " + codingSystem + " code " + code + " drugIdStr " + drugIdStr);


        boolean primaryReasonFlag = true;
        if (!"true".equals(primaryReasonFlagStr)) {
            primaryReasonFlag = false;
        }

        DrugReason dr = new DrugReason();

        dr.setDrugId(Integer.parseInt(drugIdStr));
        dr.setProviderNo(providerNo);
        dr.setDemographicNo(Integer.parseInt(demographicNo));

        dr.setCodingSystem(codingSystem);
        dr.setCode(code);
        dr.setComments(comments);
        dr.setPrimaryReasonFlag(primaryReasonFlag);
        dr.setArchivedFlag(false);
        dr.setDateCoded(new Date());

        drugReasonDao.addNewDrugReason(dr);

        String ip = request.getRemoteAddr();
        LogAction.addLog((String) request.getSession().getAttribute("user"), LogConst.ADD, LogConst.CON_DRUGREASON, "" + dr.getId(), ip, demographicNo, dr.getAuditString());

        return "close";
    }


    /**
     * Archives an existing drug reason.
     * <p>
     * This method sets the archived flag and reason for an existing
     * drug reason entry and logs the operation for audit purposes.
     *
     * @return String SUCCESS with confirmation message
     */
    public String archiveReason() {
        // Validate user has permission to access prescription functionality
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_rx", "r", null)) {
            throw new RuntimeException("missing required sec object (_rx)");
        }

        DrugReasonDao drugReasonDao = (DrugReasonDao) SpringUtils.getBean(DrugReasonDao.class);
        String reasonId = request.getParameter("reasonId");
        String archiveReason = request.getParameter("archiveReason");

        DrugReason drugReason = drugReasonDao.find(Integer.parseInt(reasonId));

        drugReason.setArchivedFlag(true);
        drugReason.setArchivedReason(archiveReason);

        drugReasonDao.merge(drugReason);

        request.setAttribute("drugId", drugReason.getDrugId());
        request.setAttribute("demoNo", drugReason.getDemographicNo());

        String ip = request.getRemoteAddr();
        LogAction.addLog((String) request.getSession().getAttribute("user"), LogConst.ARCHIVE, LogConst.CON_DRUGREASON, "" + drugReason.getId(), ip, "" + drugReason.getDemographicNo(), drugReason.getAuditString());

        request.setAttribute("message", getText("SelectReason.msg.archived"));
        return SUCCESS;
    }
}
