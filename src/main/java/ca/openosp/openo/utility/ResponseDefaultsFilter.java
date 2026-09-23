//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */
package ca.openosp.openo.utility;

import ca.openosp.openo.filters.OscarBaseFilter;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class ResponseDefaultsFilter extends OscarBaseFilter {
    private static Logger logger = MiscUtils.getLogger();
    private boolean setEncoding = true;
    private String encoding = "UTF-8";
    private boolean setNoCache = true;
    private String[] noCacheEndings = new String[]{".jsp", ".json", ".jsf"};
    private boolean forceStrongETag = true;
    private boolean warnCharsetCacheChange = false;

    /**
     * Sends the Cross-Origin-Opener-Policy header on every page.
     * <p>
     * Struts already sends this header on .do pages, but JSP pages did not send
     * it. When a JSP page opens a .do page in a popup and only one of the two has
     * the header, the browser breaks the link between the two windows: the next
     * click opens a new popup instead of reusing the old one, and window.opener
     * is null inside the popup. Sending the header from here on every page keeps
     * the windows linked.
     * <p>
     * The value must be the same as the coop interceptor mode in struts.xml.
     * "same-origin-allow-popups" is used instead of the strict "same-origin"
     * because OpenO also opens external links in popups, such as Resource on the
     * schedule page or the BMI calculator. With the strict value the browser
     * forgets those windows, so every click on the same external link opens a
     * new window. With "allow-popups" OpenO can reuse the window it already
     * opened, the same way it reuses its own popups.
     */
    private static final String COOP_HEADER = "Cross-Origin-Opener-Policy";
    private static final String COOP_VALUE = "same-origin-allow-popups";

    public ResponseDefaultsFilter() {
    }

    public void init(FilterConfig filterConfig) {
        logger.info("Initialising " + ResponseDefaultsFilter.class.getSimpleName());
        String temp = filterConfig.getInitParameter("setEncoding");
        if (temp != null) {
            this.setEncoding = Boolean.parseBoolean(temp);
            this.encoding = filterConfig.getInitParameter("encoding");
        }

        logger.info("setEncoding=" + this.setEncoding + ", encoding=" + this.encoding);
        temp = filterConfig.getInitParameter("setNoCache");
        if (temp != null) {
            this.setNoCache = Boolean.parseBoolean(temp);
            temp = filterConfig.getInitParameter("noCacheEndings");
            if (temp != null) {
                this.noCacheEndings = temp.split(",");
            }
        }

        logger.info("setNoCache=" + this.setNoCache + ", noCacheEndings=" + Arrays.toString(this.noCacheEndings));
        temp = filterConfig.getInitParameter("forceStrongETag");
        if (temp != null) {
            this.forceStrongETag = Boolean.parseBoolean(temp);
        }

        logger.info("forceStrongETag=" + this.forceStrongETag);
        temp = filterConfig.getInitParameter("warnCharsetCacheChange");
        if (temp != null) {
            this.warnCharsetCacheChange = Boolean.parseBoolean(temp);
        }

        logger.info("warnCharsetCacheChange=" + this.warnCharsetCacheChange);
    }

    public void destroy() {
        logger.info("shutdown " + ResponseDefaultsFilter.class.getSimpleName());
    }

    public void doFilterInternal(ServletRequest originalRequest, ServletResponse originalResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) originalRequest;
        HttpServletResponse response = (HttpServletResponse) originalResponse;
        if (this.setEncoding) {
            this.setEncoding(request, (HttpServletResponse) response);
        }

        if (this.setNoCache) {
            this.setCaching(request, (HttpServletResponse) response);
        }

        response.setHeader(COOP_HEADER, COOP_VALUE);

        if (this.forceStrongETag || this.warnCharsetCacheChange) {
            response = new ResponseDefaultsFilterResponseWrapper((HttpServletResponse) response, this.forceStrongETag, this.warnCharsetCacheChange);
        }

        chain.doFilter(request, (ServletResponse) response);
    }

    private void setCaching(HttpServletRequest request, HttpServletResponse response) {
        String requestUri = request.getRequestURI();
        String[] arr$ = this.noCacheEndings;
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            String noCacheEnding = arr$[i$];
            if (requestUri.endsWith(noCacheEnding)) {
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                return;
            }
        }

    }

    private void setEncoding(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(this.encoding);
        }

        response.setCharacterEncoding(this.encoding);
    }
}

