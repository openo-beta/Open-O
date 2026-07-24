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

/**
 * Struts2 action for managing the HRM Sending Facility registry (list, create/update, delete).
 * All operations require the {@code _admin} security object; the state-changing operations
 * (save and delete) are additionally restricted to POST so they cannot be triggered by a GET.
 *
 * @since 2026-05-22
 */
public class HRMSendingFacility2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private HRMSendingFacilityDao hrmSendingFacilityDao = SpringUtils.getBean(HRMSendingFacilityDao.class);

    /**
     * Routes the request to {@link #save()}, {@link #delete()}, or {@link #list()} based on the
     * {@code method} request parameter. Save and delete are rejected unless the request is a POST.
     *
     * @return String the Struts result name
     */
    public String execute() {
        String method = request.getParameter("method");
        // State-changing operations must be POST so they cannot be triggered by a GET
        // (link prefetch, cross-site request, etc.). CSRF tokens are injected on POST forms.
        if (("save".equals(method) || "delete".equals(method))
                && !"POST".equalsIgnoreCase(request.getMethod())) {
            throw new SecurityException("HRM sending facility " + method + " must use POST");
        }
        if ("save".equals(method)) {
            return save();
        }
        if ("delete".equals(method)) {
            return delete();
        }
        return list();
    }

    /**
     * Lists all registered sending facilities plus the unregistered facilities seen on HRM
     * documents. When a valid numeric {@code id} parameter is supplied, loads that facility into
     * the {@code editing} request attribute so the form is pre-filled for editing.
     *
     * @return String the Struts result name
     */
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
            } catch (NumberFormatException e) {
                MiscUtils.getLogger().warn("Ignoring non-numeric HRM sending facility edit id: " + editId);
            }
        }
        return SUCCESS;
    }

    /**
     * Creates a new sending facility or updates an existing one from the posted
     * {@code sendingFacilityId} and {@code facilityName}. Validates that both fields are present
     * and that the sending-facility ID is not already used by a different entry; on any validation
     * error an {@code errorMessage} is set and the list view is re-rendered.
     *
     * @return String the Struts result name
     */
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

    /**
     * Deletes the sending facility identified by the posted {@code id}. A missing or invalid id is
     * a no-op; failures are logged and surfaced via an {@code errorMessage} on the list view.
     *
     * @return String the Struts result name
     */
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
