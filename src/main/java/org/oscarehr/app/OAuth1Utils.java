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
package org.oscarehr.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
// OAuth1 client utilities are no longer available in CXF 3.5+
// This class needs to be refactored to use OAuth2 or external OAuth1 library
// import org.apache.cxf.rs.security.oauth.client.OAuthClientUtils;
import org.apache.logging.log4j.Logger;
import org.oscarehr.common.model.AppDefinition;
import org.oscarehr.common.model.AppUser;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;

import oscar.log.LogAction;

public class OAuth1Utils {
    private static final Logger logger = MiscUtils.getLogger();

    public static List<Object> getProviderK2A() {
        List<Object> providers = new ArrayList<Object>();
        JSONProvider jsonProvider = new JSONProvider();
        jsonProvider.setDropRootElement(true);
        providers.add(jsonProvider);

        return providers;
    }

    public static String getOAuthGetResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        // TODO: This method needs to be refactored for OAuth2
        // OAuth1 client utilities are no longer available in CXF 3.5+
        logger.warn("OAuth1Utils.getOAuthGetResponse() is deprecated and needs OAuth2 migration");
        return null;
    }

    public static String getOAuthPostResponse(LoggedInInfo loggedInInfo, AppDefinition app, AppUser user, String requestURI, String baseRequestURI, List<Object> providers, Object obj) {
        // TODO: This method needs to be refactored for OAuth2
        // OAuth1 client utilities are no longer available in CXF 3.5+
        logger.warn("OAuth1Utils.getOAuthPostResponse() is deprecated and needs OAuth2 migration");
        return null;
    }

    public static void getOAuthDeleteResponse(AppDefinition app, AppUser user, String requestURI, String baseRequestURI) {
        // TODO: This method needs to be refactored for OAuth2
        // OAuth1 client utilities are no longer available in CXF 3.5+
        logger.warn("OAuth1Utils.getOAuthDeleteResponse() is deprecated and needs OAuth2 migration");
    }
}
