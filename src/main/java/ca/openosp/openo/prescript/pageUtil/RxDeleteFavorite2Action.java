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

import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.prescript.data.RxPrescriptionData;


import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

/**
 * Struts2 action for deleting favorite prescriptions.
 * <p>
 * This action handles the deletion of favorite prescription templates
 * from the prescription system. Favorite prescriptions allow providers
 * to save commonly used prescription configurations for quick reuse.
 * <p>
 * The action validates user permissions and performs the deletion
 * operation on the specified favorite prescription record.
 *
 * @since 2008
 */
public final class RxDeleteFavorite2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for handling the response */
    HttpServletResponse response = ServletActionContext.getResponse();

    /** Security manager for validating user permissions */
    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);


    /**
     * Main execution method for deleting favorite prescriptions.
     * <p>
     * This method:
     * 1. Validates user permissions for prescription updates
     * 2. Retrieves the favorite ID from request parameters
     * 3. Deletes the specified favorite prescription
     *
     * @return String the result status (SUCCESS) to continue with the workflow
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     * @throws RuntimeException if user lacks required prescription permissions
     */
    public String execute()
            throws IOException, ServletException {

        // Validate user has permission to update prescriptions
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_rx", "u", null)) {
            throw new RuntimeException("missing required sec object (_rx)");
        }


        // Parse favorite ID and delete the favorite prescription
        int favoriteId = Integer.parseInt(this.getFavoriteId());
        new RxPrescriptionData().deleteFavorite(favoriteId);

        // Setup variables
        return SUCCESS;
    }

    /** ID of the favorite prescription to delete */
    private String favoriteId = null;

    /**
     * Gets the favorite prescription ID.
     *
     * @return String the favorite prescription ID
     */
    public String getFavoriteId() {
        return (this.favoriteId);
    }

    /**
     * Sets the favorite prescription ID to delete.
     *
     * @param favoriteId String the favorite prescription ID
     */
    public void setFavoriteId(String favoriteId) {
        this.favoriteId = favoriteId;
    }
}
