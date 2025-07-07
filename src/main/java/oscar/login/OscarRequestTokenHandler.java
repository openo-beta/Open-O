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
package oscar.login;

import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.rs.security.oauth2.provider.OAuthDataProvider;

/**
 * OAuth2 Authorization Handler
 * Replaces the OAuth1 request token handler
 */
public class OscarRequestTokenHandler {

    private static final Logger LOG = LogUtils.getL7dLogger(OscarRequestTokenHandler.class);
    private long tokenLifetime = 3600L;
    private String defaultScope;

    public Response handle(MessageContext mc, OAuthDataProvider dataProvider) {
        // TODO: Implement OAuth2 authorization code flow
        // This is a placeholder for OAuth2 migration
        LOG.warning("OAuth1 request token handler is deprecated. Please use OAuth2 authorization code flow.");
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("OAuth1 support has been removed. Please use OAuth2.")
                .build();
    }

    public void setTokenLifetime(long tokenLifetime) {
        this.tokenLifetime = tokenLifetime;
    }

    public void setDefaultScope(String defaultScope) {
        this.defaultScope = defaultScope;
    }
}
