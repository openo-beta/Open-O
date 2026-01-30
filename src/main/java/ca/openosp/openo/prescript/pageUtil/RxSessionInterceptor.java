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

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.struts2.ServletActionContext;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Struts2 interceptor that ensures the correct per-patient RxSessionBean
 * is set in the session before each Rx-related action executes.
 * <p>
 * This enables multiple patient tabs to work correctly without modifying
 * existing action code that uses session.getAttribute("RxSessionBean").
 * <p>
 * The interceptor:
 * <ol>
 *   <li>Extracts demographicNo from the request parameter</li>
 *   <li>Looks up the per-patient RxSessionBean (RxSessionBean_{demographicNo})</li>
 *   <li>Sets it as the legacy "RxSessionBean" key</li>
 *   <li>Allows the action to proceed with the correct bean</li>
 * </ol>
 * <p>
 * This solves the bug where opening a different patient's Medications tab
 * would overwrite the existing RxSessionBean, causing staged prescriptions
 * to be lost.
 *
 * @since 2026-01-30
 */
public class RxSessionInterceptor extends AbstractInterceptor {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = MiscUtils.getLogger();
    private static final String LEGACY_KEY = "RxSessionBean";

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpSession session = request.getSession(false);

        if (session != null) {
            int demographicNo = RxSessionBean.extractDemographicNo(request);

            if (demographicNo > 0) {
                RxSessionBean perPatientBean = RxSessionBean.getPerPatient(session, demographicNo);

                if (perPatientBean != null) {
                    // Swap in the correct bean for this patient's request
                    session.setAttribute(LEGACY_KEY, perPatientBean);
                    if (logger.isDebugEnabled()) {
                        logger.debug("RxSessionInterceptor: Swapped in bean for demographic " + demographicNo);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("RxSessionInterceptor: No per-patient bean found for demographic " + demographicNo);
                    }
                }
            }
        }

        return invocation.invoke();
    }
}
