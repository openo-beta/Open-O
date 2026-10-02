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
package ca.openosp.openo.utility;

import ca.openosp.openo.filters.OscarBaseFilter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Sends the Cross-Origin-Opener-Policy header on every response, including .do
 * responses that redirect or write their own output.
 * <p>
 * A popup stays linked to the window that opened it only when every page it
 * loads, redirects included, sends the same value. When one does not, the
 * browser clears the popup's window name and window.opener, so the next click
 * opens a second window instead of reusing the first.
 * <p>
 * This filter is mapped above the struts2 filter in web.xml because Struts does
 * not pass .do requests on to the filters after it. The coop interceptor in
 * struts.xml sets the header again just before a result renders, so its mode
 * must be the same value as {@link #COOP_VALUE}.
 * <p>
 * "same-origin-allow-popups" is used instead of the strict "same-origin"
 * because the shared outcomes dashboard opens an external site in a popup and
 * talks to it with postMessage, and the strict value would cut that window off.
 *
 * @since 2026-09-25
 */
public final class CoopHeaderFilter extends OscarBaseFilter {

    private static final String COOP_HEADER = "Cross-Origin-Opener-Policy";
    private static final String COOP_VALUE = "same-origin-allow-popups";

    /**
     * Nothing to configure.
     *
     * @param filterConfig FilterConfig the filter configuration from web.xml
     */
    @Override
    public void init(FilterConfig filterConfig) {
    }

    /**
     * Sets the Cross-Origin-Opener-Policy header, then passes the request on.
     *
     * @param request ServletRequest the incoming request
     * @param response ServletResponse the response the header is added to
     * @param chain FilterChain the rest of the filter chain
     * @throws IOException if a later filter or servlet fails with an I/O error
     * @throws ServletException if a later filter or servlet fails
     */
    @Override
    protected void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ((HttpServletResponse) response).setHeader(COOP_HEADER, COOP_VALUE);
        chain.doFilter(request, response);
    }

    /**
     * Nothing to release.
     */
    @Override
    public void destroy() {
    }
}
