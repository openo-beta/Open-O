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
package ca.openosp.openo.integration.oneId.web;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.integration.oneId.OneIdViewlet;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.log.LogConst;
import ca.openosp.openo.managers.EhrConnectivityManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Administrative screen for the Viewlet registry. Lists the configured Viewlets and supports
 * adding, editing, enabling and disabling entries, including each Viewlet's interface type
 * (modal or non-modal). Restricted to administrators.
 *
 * @since 2026-07-06
 */
public class ViewletAdminAction extends ActionSupport {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String SEC_OBJECT = "_admin.ehrConnectivity";
    private static final String MODE_MODAL = "modal";
    private static final String MODE_NON_MODAL = "non-modal";

    private final HttpServletRequest request = ServletActionContext.getRequest();
    private final HttpServletResponse response = ServletActionContext.getResponse();

    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final EhrConnectivityManager ehrConnectivityManager = SpringUtils.getBean(EhrConnectivityManager.class);
    private final ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);

    public String execute() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo, "r");
        List<OneIdViewlet> viewletList = ehrConnectivityManager.findAllViewlets(loggedInInfo);
        request.setAttribute("viewletList", viewletList);
        request.setAttribute("updatedByNames", resolveProviderNames(viewletList));
        return "success";
    }

    private Map<String, String> resolveProviderNames(List<OneIdViewlet> viewletList) {
        Map<String, String> names = new HashMap<>();
        for (OneIdViewlet viewlet : viewletList) {
            String providerNo = viewlet.getUpdatedBy();
            if (providerNo != null && !names.containsKey(providerNo)) {
                Provider provider = providerDao.getProvider(providerNo);
                names.put(providerNo, provider != null ? provider.getFormattedName() : providerNo);
            }
        }
        return names;
    }

    public String add() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo, "w");
        String name = trimToNull(request.getParameter("name"));
        String keyValue = trimToNull(request.getParameter("keyValue"));
        boolean showInEchart = "true".equals(request.getParameter("showInEchart"));
        String displayMode = normalizeDisplayMode(request.getParameter("displayMode"));

        if (name != null && keyValue != null) {
            OneIdViewlet viewlet = new OneIdViewlet();
            viewlet.setName(name);
            viewlet.setKeyValue(keyValue);
            viewlet.setShowInEchart(showInEchart);
            viewlet.setDisplayMode(displayMode);
            viewlet.setDeleted(false);
            viewlet.setUpdatedBy(loggedInInfo.getLoggedInProviderNo());
            viewlet.setUpdateTime(new Date());
            ehrConnectivityManager.createViewlet(loggedInInfo, viewlet);
            audit(loggedInInfo, LogConst.ADD, viewlet.getId(), "after=" + describe(viewlet));
        }
        redirectToList();
        return NONE;
    }

    public String update() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo, "w");
        OneIdViewlet viewlet = findViewlet(loggedInInfo, request.getParameter("id"));
        String name = trimToNull(request.getParameter("name"));
        String keyValue = trimToNull(request.getParameter("keyValue"));

        if (viewlet != null && name != null && keyValue != null) {
            String before = describe(viewlet);
            viewlet.setName(name);
            viewlet.setKeyValue(keyValue);
            viewlet.setShowInEchart("true".equals(request.getParameter("showInEchart")));
            viewlet.setDisplayMode(normalizeDisplayMode(request.getParameter("displayMode")));
            viewlet.setUpdatedBy(loggedInInfo.getLoggedInProviderNo());
            viewlet.setUpdateTime(new Date());
            ehrConnectivityManager.updateViewlet(loggedInInfo, viewlet);
            audit(loggedInInfo, LogConst.UPDATE, viewlet.getId(), "before=" + before + " after=" + describe(viewlet));
        }
        redirectToList();
        return NONE;
    }

    public String disable() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo, "w");
        OneIdViewlet viewlet = findViewlet(loggedInInfo, request.getParameter("id"));
        if (viewlet != null && !viewlet.isDeleted()) {
            String before = describe(viewlet);
            viewlet.setDeleted(true);
            viewlet.setUpdatedBy(loggedInInfo.getLoggedInProviderNo());
            viewlet.setUpdateTime(new Date());
            ehrConnectivityManager.updateViewlet(loggedInInfo, viewlet);
            audit(loggedInInfo, LogConst.DELETE, viewlet.getId(), "before=" + before + " after={disabled}");
        }
        redirectToList();
        return NONE;
    }

    public String enable() {
        LoggedInInfo loggedInInfo = loggedInInfo();
        checkPrivilege(loggedInInfo, "w");
        OneIdViewlet viewlet = findViewlet(loggedInInfo, request.getParameter("id"));
        if (viewlet != null && viewlet.isDeleted()) {
            viewlet.setDeleted(false);
            viewlet.setUpdatedBy(loggedInInfo.getLoggedInProviderNo());
            viewlet.setUpdateTime(new Date());
            ehrConnectivityManager.updateViewlet(loggedInInfo, viewlet);
            audit(loggedInInfo, LogConst.UPDATE, viewlet.getId(), "before={disabled} after=" + describe(viewlet));
        }
        redirectToList();
        return NONE;
    }

    private OneIdViewlet findViewlet(LoggedInInfo loggedInInfo, String idValue) {
        Integer id = parseId(idValue);
        if (id == null) {
            return null;
        }
        return ehrConnectivityManager.findViewlet(loggedInInfo, id);
    }

    private void audit(LoggedInInfo loggedInInfo, String action, Integer viewletId, String beforeAfter) {
        LogAction.addLog(loggedInInfo.getLoggedInProviderNo(), action, "viewlet-config",
                viewletId == null ? null : String.valueOf(viewletId), request.getRemoteAddr(), null, beforeAfter);
    }

    private static String describe(OneIdViewlet viewlet) {
        return "{name=" + viewlet.getName() + ", key=" + viewlet.getKeyValue()
                + ", showInEchart=" + viewlet.isShowInEchart() + ", displayMode=" + viewlet.getDisplayMode() + "}";
    }

    private static String normalizeDisplayMode(String value) {
        return MODE_MODAL.equals(value) ? MODE_MODAL : MODE_NON_MODAL;
    }

    private void redirectToList() {
        try {
            response.sendRedirect(request.getContextPath() + "/admin/viewletAdmin.do");
        } catch (Exception e) {
            logger.error("Failed to redirect to the Viewlet admin list", e);
        }
    }

    private LoggedInInfo loggedInInfo() {
        return LoggedInInfo.getLoggedInInfoFromSession(request);
    }

    private void checkPrivilege(LoggedInInfo loggedInInfo, String right) {
        if (!securityInfoManager.hasPrivilege(loggedInInfo, SEC_OBJECT, right, null)) {
            throw new SecurityException("missing required sec object (" + SEC_OBJECT + ")");
        }
    }

    private static Integer parseId(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }
}
