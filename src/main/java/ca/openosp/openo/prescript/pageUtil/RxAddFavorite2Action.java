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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.openosp.openo.commn.dao.DrugDao;
import ca.openosp.openo.commn.model.Drug;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.prescript.data.RxPrescriptionData;
import ca.openosp.openo.prescript.util.RxUtil;


import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for adding prescriptions to favorites.
 *
 * This action handles saving prescriptions as favorites for quick reuse.
 * Providers can save frequently prescribed medications with their preferred
 * dosing instructions as templates. Supports saving both existing prescriptions
 * from the database and pending prescriptions from the session stash.
 *
 * The action provides two methods:
 * - Standard favorite addition from prescription form
 * - AJAX-based addition for the Rx3 interface
 *
 * @since 2008-11-20
 */
public final class RxAddFavorite2Action extends ActionSupport {
    /**
     * HTTP request object.
     */
    HttpServletRequest request = ServletActionContext.getRequest();

    /**
     * HTTP response object.
     */
    HttpServletResponse response = ServletActionContext.getResponse();

    /**
     * Security manager for privilege checking.
     */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);


    /**
     * Executes the add favorite action.
     *
     * Routes to appropriate method based on parameter. Adds prescription
     * to provider's favorites either from database or session stash.
     *
     * @return String "success" on completion or null for AJAX calls
     * @throws IOException if I/O error occurs
     * @throws ServletException if servlet error occurs
     * @throws RuntimeException if user lacks required privileges
     */
    public String execute()
            throws IOException, ServletException {

        if ("addFav2".equals(request.getParameter("parameterValue"))) {
            return addFav2();
        }
        
        // Verify user has prescription write privileges
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_rx", "w", null)) {
            throw new RuntimeException("missing required sec object (_rx)");
        }

        RxSessionBean bean = (RxSessionBean) request.getSession().getAttribute("RxSessionBean");
        if (bean == null) {
            response.sendRedirect("error.html");
            return null;
        }

        String providerNo = bean.getProviderNo();

        if (this.getDrugId() != null) {
            int drugId = Integer.parseInt(this.getDrugId());

            DrugDao drugDao = (DrugDao) SpringUtils.getBean(DrugDao.class);
            Drug drug = drugDao.find(drugId);
            RxPrescriptionData.addToFavorites(providerNo, favoriteName, drug);
        } else {
            int stashId = Integer.parseInt(this.getStashId());

            bean.getStashItem(stashId).AddToFavorites(providerNo, favoriteName);
        }

        return "success";
    }

    /**
     * AJAX method for adding favorites in Rx3 interface.
     *
     * Handles asynchronous favorite addition without page refresh.
     * Uses random ID instead of stash index for identification.
     *
     * @return String null for AJAX response
     * @throws IOException if I/O error occurs
     * @throws RuntimeException if user lacks required privileges
     */
    public String addFav2()
            throws IOException {

        // Verify user has prescription write privileges
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_rx", "w", null)) {
            throw new RuntimeException("missing required sec object (_rx)");
        }

        RxSessionBean bean = (RxSessionBean) request.getSession().getAttribute("RxSessionBean");
        if (bean == null) {
            response.sendRedirect("error.html");
            return null;
        }
        String randomId = request.getParameter("randomId");
        String favoriteName = request.getParameter("favoriteName");
        String drugIdStr = request.getParameter("drugId");
        String providerNo = bean.getProviderNo();

        if (drugIdStr != null) {
            int drugId = Integer.parseInt(drugIdStr);
            DrugDao drugDao = (DrugDao) SpringUtils.getBean(DrugDao.class);
            Drug drug = drugDao.find(drugId);
            RxPrescriptionData.addToFavorites(providerNo, favoriteName, drug);
        } else {
            int stashId = bean.getIndexFromRx(Integer.parseInt(randomId));
            bean.getStashItem(stashId).AddToFavorites(providerNo, favoriteName);
        }
       
        /*
        request.setAttribute("BoxNoFillFirstLoad", "true");
        MiscUtils.getLogger().debug("fill box no");
        */
        RxUtil.printStashContent(bean);

        return null;
    }


    /**
     * Database ID of drug to add to favorites.
     */
    private String drugId = null;

    /**
     * Stash index of prescription to add to favorites.
     */
    private String stashId = null;

    /**
     * User-defined name for the favorite.
     */
    private String favoriteName = null;

    /**
     * Return parameters for redirect.
     */
    private String returnParams = null;

    public String getDrugId() {
        return (this.drugId);
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    public String getStashId() {
        return (this.stashId);
    }

    public void setStashId(String stashId) {
        this.stashId = stashId;
    }

    public String getFavoriteName() {
        return (this.favoriteName);
    }

    public void setFavoriteName(String favoriteName) {
        this.favoriteName = favoriteName;
    }

    public String getReturnParams() {
        if (this.returnParams == null) {
            this.returnParams = "";
        }
        return (this.returnParams);
    }
}
