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


package ca.openosp.openo.mds.pageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.ProviderLabRoutingFavoritesDao;
import ca.openosp.openo.commn.model.ProviderLabRoutingFavorite;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.lab.ca.on.CommonLabResultData;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

public class ReportReassign2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();


    private final Logger logger = MiscUtils.getLogger();
    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    public ReportReassign2Action() {
    }

    public String execute()
            throws ServletException, IOException {

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_lab", "w", null)) {
            throw new SecurityException("missing required sec object (_lab)");
        }

        String status = request.getParameter("status");
        String ajax = request.getParameter("ajax");
        String providerNo = loggedInInfo.getLoggedInProviderNo();
        String searchProviderNo = request.getParameter("searchProviderNo");
        JSONArray jsonArray = null;
        String[] selectedProvidersArray = new String[0];
        String[] arrNewFavs = new String[0];
        ArrayList<String[]> flaggedLabsList = new ArrayList<>();
        boolean success = Boolean.FALSE;
        /*
         * Group together any new favorite providers that may have been
         * set during the forward process.
         */
        String newFavorites = request.getParameter("selectedFavorites");
        if (newFavorites != null && !newFavorites.isEmpty()) {
            JSONObject jsonObject = JSONObject.fromObject(newFavorites);
            jsonArray = (JSONArray) jsonObject.get("favorites");
        }

        if (jsonArray != null) {
            arrNewFavs = (String[]) jsonArray.toArray(new String[jsonArray.size()]);
        }

        /*
         * Group together the providers selected during the forward
         * process.
         */
        String selectedProviders = request.getParameter("selectedProviders");
        logger.info("selected providers to forward labs to " + selectedProviders);

        if (selectedProviders != null && !selectedProviders.isEmpty()) {
            JSONObject jsonObject = JSONObject.fromObject(selectedProviders);
            jsonArray = jsonObject.getJSONArray("providers");
        }

        if (jsonArray != null) {
            selectedProvidersArray = (String[]) jsonArray.toArray(new String[jsonArray.size()]);
        }

        /*
         * Group together the lab ids and types checked off during the
         * forwarding process.
         */
        String flaggedLabs = request.getParameter("flaggedLabs");
        if (flaggedLabs != null && !flaggedLabs.isEmpty()) {
            JSONObject jsonObject = JSONObject.fromObject(flaggedLabs);
            jsonArray = (JSONArray) jsonObject.get("files");
        }

        if (jsonArray != null) {
            String[] labid;
            for (int i = 0; i < jsonArray.size(); i++) {
                labid = jsonArray.getString(i).split(":");
                flaggedLabsList.add(labid);
            }
        }

        String newURL = "";
        try {
            //Only route if there are selected providers
            if (selectedProvidersArray.length > 0) {
                success = CommonLabResultData.updateLabRouting(flaggedLabsList, selectedProvidersArray);
            }

            //update favorites
            ProviderLabRoutingFavoritesDao favDao = (ProviderLabRoutingFavoritesDao) SpringUtils.getBean(ProviderLabRoutingFavoritesDao.class);
            List<ProviderLabRoutingFavorite> currentFavorites = favDao.findFavorites(providerNo);

            if (arrNewFavs.length == 0) {
                for (ProviderLabRoutingFavorite fav : currentFavorites) {
                    favDao.remove(fav.getId());
                }
            } else {
                //Check for new favorites to add
                boolean isNew;
                for (int idx = 0; idx < arrNewFavs.length; ++idx) {
                    isNew = true;
                    for (ProviderLabRoutingFavorite fav : currentFavorites) {
                        if (fav.getRoute_to_provider_no().equals(arrNewFavs[idx])) {
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        ProviderLabRoutingFavorite newFav = new ProviderLabRoutingFavorite();
                        newFav.setProvider_no(providerNo);
                        newFav.setRoute_to_provider_no(arrNewFavs[idx]);
                        favDao.persist(newFav);
                    }
                }

                //check for favorites to remove
                boolean remove;
                for (ProviderLabRoutingFavorite fav : currentFavorites) {
                    remove = true;
                    for (int idx2 = 0; idx2 < arrNewFavs.length; ++idx2) {
                        if (fav.getRoute_to_provider_no().equals(arrNewFavs[idx2])) {
                            remove = false;
                            break;
                        }
                    }
                    if (remove) {
                        favDao.remove(fav.getId());
                    }
                }

            }

            // Build safe redirect URL using context path to prevent open redirect vulnerability
            String contextPath = request.getContextPath();
            String requestURI = request.getRequestURI();

            URI uri = new URI(requestURI);
            String normalizedPath = uri.normalize().getPath();

            if (!normalizedPath.startsWith(contextPath)) {
                logger.warn("Suspicious redirect path detected: '{}'. Sending error response.", normalizedPath);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid redirect path.");
            }
            
            // Extract the path relative to the context, ensuring it stays within the application
            String relativePath = normalizedPath.substring(contextPath.length());
            
            // Default to a safe page if the path is suspicious or empty
            if (relativePath.isEmpty() || relativePath.contains("..") || !relativePath.startsWith("/")) {
                logger.warn("Suspicious or invalid redirect path detected: '{}'. Sending error response.", relativePath);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid redirect path.");
                return null;
            }
            
            // Build the new URL with the context path to ensure it stays within the application
            newURL = contextPath + relativePath;
            
            // Add query parameters with proper URL encoding to prevent injection
            String queryDelimiter = relativePath.contains("labDisplay.jsp") ? "?" : "&";
            
            // Build query string with proper null checks and encoding
            StringBuilder queryParams = new StringBuilder();
            queryParams.append(queryDelimiter);
            queryParams.append("providerNo=").append(URLEncoder.encode(providerNo != null ? providerNo : "", "UTF-8"));
            queryParams.append("&searchProviderNo=").append(URLEncoder.encode(searchProviderNo != null ? searchProviderNo : "", "UTF-8"));
            queryParams.append("&status=").append(URLEncoder.encode(status != null ? status : "", "UTF-8"));
            
            // Add segmentID if flaggedLabsList is not empty
            if (!flaggedLabsList.isEmpty() && flaggedLabsList.get(0).length > 0) {
                queryParams.append("&segmentID=").append(URLEncoder.encode(flaggedLabsList.get(0)[0], "UTF-8"));
            }
            
            newURL = newURL + queryParams.toString();
            
            // Add optional parameters with proper encoding
            String lname = request.getParameter("lname");
            if (lname != null) {
                newURL = newURL + "&lname=" + URLEncoder.encode(lname, "UTF-8");
            }
            String fname = request.getParameter("fname");
            if (fname != null) {
                newURL = newURL + "&fname=" + URLEncoder.encode(fname, "UTF-8");
            }
            String hnum = request.getParameter("hnum");
            if (hnum != null) {
                newURL = newURL + "&hnum=" + URLEncoder.encode(hnum, "UTF-8");
            }
        } catch (Exception e) {
            logger.error("exception in ReportReassign2Action", e);
            return "failure";
        }

        if (ajax != null && ajax.equals("yes")) {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", success);
            jsonResponse.put("files", jsonArray);
            try {
                PrintWriter out = response.getWriter();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                out.print(jsonResponse);
                out.flush();
            } catch (IOException e) {
                MiscUtils.getLogger().error("Error with JSON response ", e);
            }
            return null;
        } else {
            response.sendRedirect(newURL);
            return NONE;
        }
    }
}
