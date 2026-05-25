package ca.openosp.openo.hospitalReportManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.openo.hospitalReportManager.dao.HRMSendingFacilityDao;
import ca.openosp.openo.hospitalReportManager.model.HRMSendingFacility;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class HRMSendingFacility2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private HRMSendingFacilityDao hrmSendingFacilityDao = SpringUtils.getBean(HRMSendingFacilityDao.class);

    public String execute() {
        String method = request.getParameter("method");
        if ("save".equals(method)) {
            return save();
        }
        if ("delete".equals(method)) {
            return delete();
        }
        return list();
    }

    public String list() {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin", "r", null)) {
            throw new SecurityException("missing required sec object (_admin)");
        }
        request.setAttribute("facilities", hrmSendingFacilityDao.findAll());
        request.setAttribute("unregisteredFacilities", hrmSendingFacilityDao.findUnregisteredFacilityCounts());

        String editId = request.getParameter("id");
        if (editId != null && !editId.trim().isEmpty()) {
            try {
                HRMSendingFacility editing = hrmSendingFacilityDao.find(Integer.parseInt(editId));
                request.setAttribute("editing", editing);
            } catch (NumberFormatException ignore) {
            }
        }
        return SUCCESS;
    }

    public String save() {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin", "w", null)) {
            throw new SecurityException("missing required sec object (_admin)");
        }

        String idStr = request.getParameter("id");
        String sendingFacilityId = StringUtils.trimToNull(request.getParameter("sendingFacilityId"));
        String facilityName = StringUtils.trimToNull(request.getParameter("facilityName"));

        if (sendingFacilityId == null || facilityName == null) {
            request.setAttribute("errorMessage", "Sending Facility ID and Facility Name are both required.");
            return list();
        }

        HRMSendingFacility existingWithSameSfId = hrmSendingFacilityDao.findBySendingFacilityId(sendingFacilityId);
        try {
            HRMSendingFacility entity;
            if (idStr != null && !idStr.trim().isEmpty()) {
                entity = hrmSendingFacilityDao.find(Integer.parseInt(idStr));
                if (entity == null) {
                    request.setAttribute("errorMessage", "Facility not found.");
                    return list();
                }
                if (existingWithSameSfId != null && !existingWithSameSfId.getId().equals(entity.getId())) {
                    request.setAttribute("errorMessage", "Another facility is already registered with that Sending Facility ID.");
                    return list();
                }
            } else {
                if (existingWithSameSfId != null) {
                    request.setAttribute("errorMessage", "A facility with that Sending Facility ID already exists.");
                    return list();
                }
                entity = new HRMSendingFacility();
            }

            entity.setSendingFacilityId(sendingFacilityId);
            entity.setFacilityName(facilityName);

            if (entity.getId() == null) {
                hrmSendingFacilityDao.persist(entity);
            } else {
                hrmSendingFacilityDao.merge(entity);
            }
        } catch (Exception e) {
            MiscUtils.getLogger().error("Failed to save HRMSendingFacility", e);
            request.setAttribute("errorMessage", "Could not save facility — see server logs.");
        }
        return list();
    }

    public String delete() {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin", "w", null)) {
            throw new SecurityException("missing required sec object (_admin)");
        }

        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                hrmSendingFacilityDao.remove(Integer.parseInt(idStr));
            } catch (Exception e) {
                MiscUtils.getLogger().error("Failed to delete HRMSendingFacility id=" + idStr, e);
                request.setAttribute("errorMessage", "Could not delete facility — see server logs.");
            }
        }
        return list();
    }
}
