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

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servlet filter that ensures the correct per-patient RxSessionBean
 * is set in the session before each Rx-related request is processed.
 * <p>
 * This enables multiple patient tabs to work correctly without modifying
 * existing action code that uses session.getAttribute("RxSessionBean").
 * <p>
 * The filter:
 * <ol>
 *   <li>Extracts demographicNo from the request parameter</li>
 *   <li>Falls back to the current legacy bean's demographicNo if not in params</li>
 *   <li>Looks up the per-patient RxSessionBean (RxSessionBean_{demographicNo})</li>
 *   <li>Sets it as the legacy "RxSessionBean" key for this request</li>
 *   <li>Periodically cleans up stale beans (1% of requests)</li>
 * </ol>
 * <p>
 * This solves the bug where opening a different patient's Medications tab
 * would overwrite the existing RxSessionBean, causing staged prescriptions
 * to be lost.
 *
 * @since 2026-01-30
 */
public class RxSessionFilter implements Filter {

    private static final Logger logger = MiscUtils.getLogger();
    private static final String LEGACY_KEY = "RxSessionBean";

    /** Probability of running cleanup on each request (1% = 0.01) */
    private static final double CLEANUP_PROBABILITY = 0.01;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RxSessionFilter initialized for multi-patient session management");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpSession session = request.getSession(false);

        if (session != null) {
            int demographicNo = RxSessionBean.extractDemographicNo(request);

            // Fallback: if demographicNo not in request params, check current legacy bean
            if (demographicNo <= 0) {
                RxSessionBean currentBean = (RxSessionBean) session.getAttribute(LEGACY_KEY);
                if (currentBean != null) {
                    demographicNo = currentBean.getDemographicNo();
                    if (logger.isDebugEnabled()) {
                        logger.debug("RxSessionFilter: Using demographicNo from current bean: " + demographicNo);
                    }
                }
            }

            if (demographicNo > 0) {
                RxSessionBean perPatientBean = RxSessionBean.getPerPatient(session, demographicNo);

                if (perPatientBean != null) {
                    // Swap in the correct bean for this patient's request
                    session.setAttribute(LEGACY_KEY, perPatientBean);
                    if (logger.isDebugEnabled()) {
                        logger.debug("RxSessionFilter: Swapped in bean for demographic " + demographicNo);
                    }
                }
            }

            // Periodic cleanup: ~1% of requests trigger stale bean cleanup
            if (ThreadLocalRandom.current().nextDouble() < CLEANUP_PROBABILITY) {
                try {
                    RxSessionBean.cleanupStaleBeans(session);
                    if (logger.isDebugEnabled()) {
                        logger.debug("RxSessionFilter: Performed periodic cleanup of stale beans");
                    }
                } catch (Exception e) {
                    // Don't let cleanup errors affect the request
                    logger.warn("RxSessionFilter: Error during stale bean cleanup", e);
                }
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        logger.info("RxSessionFilter destroyed");
    }
}
