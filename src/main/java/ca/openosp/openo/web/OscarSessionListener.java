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
package ca.openosp.openo.web;

import ca.openosp.openo.commn.dao.CasemgmtNoteLockDao;
import ca.openosp.openo.commn.exception.UserSessionNotFoundException;
import ca.openosp.openo.commn.model.CasemgmtNoteLock;
import ca.openosp.openo.managers.UserSessionManager;
import ca.openosp.openo.managers.UserSessionManagerImpl;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * HTTP session lifecycle listener for healthcare application session management.
 *
 * This listener manages critical healthcare session lifecycle events, ensuring
 * proper cleanup of healthcare-specific resources when user sessions are created
 * or destroyed. It handles the secure management of clinical note locks and
 * user session registrations that are essential for maintaining data integrity
 * and preventing unauthorized access to patient health information.
 *
 * <p>The listener performs several critical healthcare security functions:</p>
 * <ul>
 *   <li>Logs session creation for audit trail compliance</li>
 *   <li>Releases case management note locks on session termination</li>
 *   <li>Unregisters user security sessions to prevent orphaned access</li>
 *   <li>Maintains session tracking for healthcare compliance requirements</li>
 * </ul>
 *
 * <p>Clinical note locking is essential in healthcare environments to prevent
 * simultaneous editing of patient records by multiple providers, which could
 * result in data loss or conflicting clinical documentation. The session cleanup
 * ensures that locks are not held indefinitely when users log out or their
 * sessions expire.</p>
 *
 * @since May 2009
 * @see ca.openosp.openo.commn.model.CasemgmtNoteLock
 * @see ca.openosp.openo.managers.UserSessionManager
 */
public class OscarSessionListener implements HttpSessionListener {

    /**
     * Handles healthcare session creation events for audit and tracking.
     *
     * Logs the creation of new user sessions for security audit purposes,
     * recording session identifiers that can be used to track user activity
     * and maintain compliance with healthcare access logging requirements.
     *
     * <p>Session creation logging is important for healthcare security audits
     * and helps track user access patterns for compliance with HIPAA/PIPEDA
     * requirements regarding patient health information access.</p>
     *
     * @param se HttpSessionEvent containing the newly created session information
     *
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        MiscUtils.getLogger().info("Creating new OSCAR session.");
        MiscUtils.getLogger().info("Session id: " + se.getSession().getId());
    }

    /**
     * Handles healthcare session destruction with comprehensive resource cleanup.
     *
     * Performs critical cleanup operations when user sessions end, ensuring
     * that healthcare-specific resources are properly released and security
     * constraints are maintained. This includes releasing clinical note locks
     * and unregistering security sessions.
     *
     * <p>The cleanup process prevents several serious healthcare system issues:</p>
     * <ul>
     *   <li>Orphaned note locks that could block other providers from editing records</li>
     *   <li>Persistent security registrations that could enable unauthorized access</li>
     *   <li>Session resource leaks that could impact system performance</li>
     * </ul>
     *
     * <p>Clinical note locks are particularly important in healthcare environments
     * where multiple providers may need to access and edit the same patient records.
     * Proper cleanup ensures that patient care is not delayed due to locked records
     * from terminated sessions.</p>
     *
     * @param se HttpSessionEvent containing the session being destroyed
     *
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     * @see ca.openosp.openo.commn.model.CasemgmtNoteLock
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String id = se.getSession().getId();
        MiscUtils.getLogger().info("session is being destroyed - " + id);

        // Release all case management note locks held by this session
        CasemgmtNoteLockDao casemgmtNoteLockDao = SpringUtils.getBean(CasemgmtNoteLockDao.class);

        for (CasemgmtNoteLock lock : casemgmtNoteLockDao.findBySession(id)) {
            MiscUtils.getLogger().info("removing note locks for this session - " + lock);
            casemgmtNoteLockDao.remove(lock.getId());
        }

        // Unregister user security session to prevent orphaned access
        HttpSession session = se.getSession();
        Integer userSecurityCode = (Integer) session.getAttribute(UserSessionManagerImpl.KEY_USER_SECURITY_CODE);
        if (userSecurityCode != null) {
            try {
                UserSessionManager userSessionManager = SpringUtils.getBean(UserSessionManager.class);
                userSessionManager.unregisterUserSession(userSecurityCode);
            } catch (UserSessionNotFoundException e) {
                MiscUtils.getLogger().warn("Failed to unregister session on destroy: {}", e.getMessage());
            }
        }
    }

}
