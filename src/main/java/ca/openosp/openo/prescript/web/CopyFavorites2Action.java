/**
 * Copyright (c) 2006-. OSCARservice, OpenSoft System. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package ca.openosp.openo.prescript.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import ca.openosp.openo.commn.dao.FavoritesDao;
import ca.openosp.openo.commn.dao.FavoritesPrivilegeDao;
import ca.openosp.openo.commn.model.Favorites;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import com.opensymphony.xwork2.ActionSupport;

/**
 * Struts2 action responsible for managing prescription favorites functionality including
 * copying favorites between providers and managing sharing privileges.
 *
 * This action handles three main operations:
 * <ul>
 * <li>Copying selected favorite prescriptions from one provider to another</li>
 * <li>Updating sharing privileges for a provider's favorites</li>
 * <li>Refreshing the view with a selected provider's favorites</li>
 * </ul>
 *
 * The action supports method-based routing using the 'dispatch' parameter to determine
 * which operation to perform: 'copy', 'update', or default 'refresh'.
 *
 * @since 2025-04-07
 */
public class CopyFavorites2Action extends ActionSupport {
    /** HTTP request object for accessing request parameters and attributes */
    HttpServletRequest request = ServletActionContext.getRequest();

    /** HTTP response object for sending responses back to the client */
    HttpServletResponse response = ServletActionContext.getResponse();


    /** Logger instance for debugging and error tracking */
    private static final Logger logger = MiscUtils.getLogger();

    /** Data access object for managing favorites privilege settings */
    FavoritesPrivilegeDao favoritesPrivilegeDao = SpringUtils.getBean(FavoritesPrivilegeDao.class);

    /** Data access object for managing favorites prescriptions */
    FavoritesDao favoritesDao = SpringUtils.getBean(FavoritesDao.class);
    
    /**
     * Main entry point for the action that routes to specific methods based on the 'dispatch' parameter.
     *
     * Supports three dispatch methods:
     * <ul>
     * <li>'update' - Updates sharing privileges for a provider's favorites</li>
     * <li>'copy' - Copies selected favorites from one provider to another</li>
     * <li>default - Refreshes the view with selected provider information</li>
     * </ul>
     *
     * @return String Struts2 result string, typically "success"
     */
    public String execute() {
        String method = request.getParameter("dispatch");
        if ("update".equals(method)) {
            return update();
        } else if ("copy".equals(method)) {
            return copy();
        }
        return refresh();
    }


    /**
     * Updates the sharing privilege settings for a provider's prescription favorites.
     *
     * This method processes form data to determine whether a provider's favorites
     * should be shared with other providers. The sharing setting is stored in the
     * database via the FavoritesPrivilegeDao.
     *
     * @return String Struts2 SUCCESS result
     */
    public String update() {
        logger.debug("copyFavorites-update");

        // Extract provider number from request parameters
        String providerNo = request.getParameter("userProviderNo");

        // Parse share radio button value (0=false, 1=true)
        int share = Integer.parseInt(request.getParameter("rb_share"));

        // Update the sharing privilege in the database
        favoritesPrivilegeDao.setFavoritesPrivilege(providerNo, share==0?false:true, false);

        return SUCCESS;
    }

    /**
     * Refreshes the favorites view by setting the selected provider for copying operations.
     *
     * This method extracts the selected provider from the dropdown list and sets it
     * as a request attribute for use by the JSP view. This allows the interface to
     * display the selected provider's favorites for potential copying.
     *
     * @return String Struts2 SUCCESS result
     */
    public String refresh() {
        logger.debug("copyFavorites-refresh");

        // Get the selected provider from dropdown parameter
        String providerNo = request.getParameter("ddl_provider");

        // Set as request attribute for JSP access
        request.setAttribute("copyProviderNo", providerNo);

        return SUCCESS;
    }

    /**
     * Copies selected favorite prescriptions from one provider to another provider.
     *
     * This method processes a form containing checkboxes for favorite selection and
     * creates duplicate entries for the target provider. Each selected favorite is
     * cloned using BeanUtils.copyProperties() and assigned to the destination provider.
     *
     * The copying process involves:
     * <ol>
     * <li>Validating that a source provider is selected</li>
     * <li>Collecting all selected favorite IDs from form checkboxes</li>
     * <li>Creating copies of each selected favorite</li>
     * <li>Assigning copies to the target provider and persisting to database</li>
     * </ol>
     *
     * @return String Struts2 SUCCESS result
     */
    public String copy() {
        logger.debug("copyFavorites-copy");

        // Get the target provider who will receive the copied favorites
        String providerNo = request.getParameter("providerNo");

        // Validate that a source provider is selected
        if (request.getParameter("ddl_provider") == null || request.getParameter("ddl_provider").equals(""))
            return SUCCESS;

        // Get the count of available favorites to check
        int count = Integer.parseInt(request.getParameter("countFavorites"));
        List<Integer> favIDs = new ArrayList<Integer>();

        // Collect IDs of selected favorites from form checkboxes
        for (int i=0;i<count;i++){
            String search = "selected"+i;
            if (request.getParameter(search) != null) {
                int id = Integer.parseInt(request.getParameter("fldFavoriteId"+i));
                favIDs.add(id);
            }
        }

        // Copy each selected favorite to the target provider
        for(Integer id:favIDs) {
        	Favorites f = favoritesDao.find(id);
        	Favorites copy = new Favorites();
        	try {
        		// Clone the favorite using Apache Commons BeanUtils
	        	BeanUtils.copyProperties(copy, f);

	        	// Assign to target provider and clear ID for new record
	        	copy.setProviderNo(providerNo);
	        	copy.setId(null);

	        	// Persist the new favorite record
	        	favoritesDao.persist(copy);
        	}catch(Exception e) {
        		logger.error("error",e);
        	}
        }

        return SUCCESS;
    }

}
