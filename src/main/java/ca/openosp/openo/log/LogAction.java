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


package ca.openosp.openo.log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.OscarLogDao;
import ca.openosp.openo.commn.model.OscarLog;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.utility.DeamonThreadFactory;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Central healthcare audit logging service for OpenO EMR system.
 * This class provides comprehensive audit trail functionality required for healthcare
 * regulatory compliance including HIPAA, PIPEDA, and provincial health information
 * protection regulations.
 *
 * <p>The healthcare audit logging system captures all significant clinical and
 * administrative actions within the EMR to maintain complete accountability for
 * patient health information (PHI) access. This includes:</p>
 * <ul>
 *   <li>Patient demographic access and modifications</li>
 *   <li>Clinical note creation and updates</li>
 *   <li>Prescription and medication management</li>
 *   <li>Laboratory result viewing and acknowledgment</li>
 *   <li>Document and form access</li>
 *   <li>Provider authentication events</li>
 *   <li>Security and privilege violations</li>
 * </ul>
 *
 * <p>The logging system supports both synchronous and asynchronous operations:
 * <strong>Asynchronous logging</strong> is preferred for most operations to maintain
 * clinical workflow performance, while <strong>synchronous logging</strong> is used
 * when audit entries must participate in database transactions.</p>
 *
 * <p>All audit entries include contextual information such as provider identity,
 * patient identifiers, IP addresses, timestamps, and detailed action descriptions.
 * This information is essential for regulatory compliance, security investigations,
 * and quality assurance monitoring.</p>
 *
 * @see OscarLog
 * @see AddLogExecutorTask
 * @see LogConst
 * @see ca.openosp.openo.utility.LoggedInInfo
 * @since 2005-06-08
 */
public class LogAction {

    /**
     * Application logger for recording logging system errors and diagnostics.
     * This logger captures failures in the audit logging process itself.
     */
    private static Logger logger = MiscUtils.getLogger();

    /**
     * Data access object for persisting audit log entries to the database.
     * Used for both synchronous and asynchronous audit log operations.
     */
    private static OscarLogDao oscarLogDao = (OscarLogDao) SpringUtils.getBean(OscarLogDao.class);

    /**
     * Thread pool executor for asynchronous audit log processing.
     * Uses cached thread pool with high priority daemon threads to ensure
     * audit logging does not impact clinical operations while maintaining
     * audit trail integrity.
     */
    private static ExecutorService executorService = Executors.newCachedThreadPool(new DeamonThreadFactory(LogAction.class.getSimpleName() + ".executorService", Thread.MAX_PRIORITY));

    /**
     * Creates a synchronous audit log entry from logged-in user context.
     * This method extracts provider and security information from the current
     * session context to create a comprehensive audit trail entry.
     *
     * <p>This method is typically used within database transactions where the
     * audit log entry must be persisted as part of the same transaction as the
     * clinical action being logged.</p>
     *
     * @param loggedInInfo the current user session information containing provider
     *                     and security context for audit trail attribution
     * @param action String describing the clinical or administrative action
     *               performed, typically using constants from {@link LogConst}
     * @param data String containing additional context or details about the action,
     *             such as specific values changed or forms accessed
     * @see LogConst
     * @see #addLogSynchronous(OscarLog)
     */
    public static void addLogSynchronous(LoggedInInfo loggedInInfo, String action, String data) {
        OscarLog logEntry = new OscarLog();

        // Extract security context for audit attribution
        if (loggedInInfo.getLoggedInSecurity() != null)
            logEntry.setSecurityId(loggedInInfo.getLoggedInSecurity().getSecurityNo());

        // Extract provider context for clinical accountability
        if (loggedInInfo.getLoggedInProvider() != null)
            logEntry.setProviderNo(loggedInInfo.getLoggedInProviderNo());

        logEntry.setAction(action);
        logEntry.setData(data);
        LogAction.addLogSynchronous(logEntry);
    }

    /**
     * Creates an asynchronous audit log entry with minimal parameters.
     * This convenience method creates an audit log entry with provider, action,
     * content type, and additional data, executing in a separate thread to
     * maintain clinical workflow performance.
     *
     * @param provider_no String identifier of the healthcare provider performing
     *                    the action, used for clinical accountability
     * @param action String describing the clinical or administrative action,
     *               typically using constants from {@link LogConst}
     * @param content String identifying the type of clinical content accessed,
     *                such as "demographic", "prescription", or "lab"
     * @param data String containing additional context about the action performed
     * @see LogConst
     * @see #addLog(String, String, String, String, String, String, String)
     */
    public static void addLog(String provider_no, String action, String content, String data) {
        addLog(provider_no, action, content, null, null, null, data);
    }

    /**
     * Creates an asynchronous audit log entry with network context.
     * This method includes IP address information for security audit trails,
     * particularly important for tracking remote access to patient information.
     *
     * @param provider_no String identifier of the healthcare provider
     * @param action String describing the clinical or administrative action
     * @param content String identifying the type of clinical content accessed
     * @param contentId String specific identifier of the content item accessed,
     *                  such as patient ID or document ID
     * @param ip String IP address of the client accessing the system, used for
     *           security audit trails and breach investigation
     * @see #addLog(String, String, String, String, String, String, String)
     */
    public static void addLog(String provider_no, String action, String content, String contentId, String ip) {
        addLog(provider_no, action, content, contentId, ip, null, null);
    }

    /**
     * Creates an asynchronous audit log entry with patient context.
     * This method includes patient demographic information for clinical audit
     * trails, essential for PHI access tracking and regulatory compliance.
     *
     * @param provider_no String identifier of the healthcare provider
     * @param action String describing the clinical or administrative action
     * @param content String identifying the type of clinical content accessed
     * @param contentId String specific identifier of the content accessed
     * @param ip String IP address of the client session
     * @param demographicNo String patient demographic identifier for PHI access
     *                      tracking and clinical audit requirements
     * @see #addLog(String, String, String, String, String, String, String)
     */
    public static void addLog(String provider_no, String action, String content, String contentId, String ip, String demographicNo) {
        addLog(provider_no, action, content, contentId, ip, demographicNo, null);
    }

    /**
     * Creates an asynchronous audit log entry from session context with full details.
     * This method extracts comprehensive context from the current user session and
     * creates a complete audit trail entry including patient demographic linkage.
     *
     * <p>This is the preferred method for comprehensive clinical audit logging as it
     * automatically captures provider identity, security context, and network information
     * from the current session while allowing specific patient and action context.</p>
     *
     * @param loggedInInfo the current user session containing provider, security,
     *                     and network context for complete audit attribution
     * @param action String describing the clinical or administrative action
     * @param content String identifying the type of clinical content accessed
     * @param contentId String specific identifier of the content accessed
     * @param demographicNo String patient identifier for PHI access tracking,
     *                      may be null for non-patient-specific actions
     * @param data String containing additional context about the action
     * @see AddLogExecutorTask
     * @see LoggedInInfo
     */
    public static void addLog(LoggedInInfo loggedInInfo, String action, String content, String contentId, String demographicNo, String data) {
        OscarLog logEntry = new OscarLog();

        // Extract security context for audit attribution
        if (loggedInInfo.getLoggedInSecurity() != null)
            logEntry.setSecurityId(loggedInInfo.getLoggedInSecurity().getSecurityNo());

        // Extract provider context for clinical accountability
        if (loggedInInfo.getLoggedInProvider() != null)
            logEntry.setProviderNo(loggedInInfo.getLoggedInProviderNo());

        logEntry.setAction(action);
        logEntry.setContent(content);
        logEntry.setContentId(contentId);
        logEntry.setIp(loggedInInfo.getIp());

        try {
            // Parse patient demographic ID for PHI access tracking
            demographicNo = StringUtils.trimToNull(demographicNo);
            if (demographicNo != null)
                logEntry.setDemographicId(Integer.parseInt(demographicNo));
        } catch (Exception e) {
            // Log parsing errors but continue with audit entry creation
            logger.error("Unexpected error parsing demographic number: " + demographicNo, e);
        }

        logEntry.setData(data);

        // Execute asynchronously to maintain clinical workflow performance
        executorService.execute(new AddLogExecutorTask(logEntry));
    }

    /**
     * Creates an asynchronous audit log entry with complete parameter set.
     * This is the primary method for creating comprehensive audit log entries
     * with all available context information for regulatory compliance.
     *
     * <p>This method provides the most complete audit trail by capturing all
     * relevant clinical and administrative context including provider identity,
     * patient demographics, network information, and detailed action descriptions.</p>
     *
     * @param provider_no String identifier of the healthcare provider performing
     *                    the action, required for clinical accountability
     * @param action String describing the clinical or administrative action,
     *               using standardized values from {@link LogConst}
     * @param content String identifying the type of clinical content accessed,
     *                such as "demographic", "prescription", "lab", or "document"
     * @param contentId String specific identifier of the accessed content item,
     *                  such as patient ID, prescription ID, or document ID
     * @param ip String IP address of the client session, critical for security
     *           audit trails and breach investigation
     * @param demographicNo String patient demographic identifier for PHI access
     *                      tracking, may be null for non-patient actions
     * @param data String containing additional context, error messages, or
     *             specific values related to the action performed
     * @see LogConst
     * @see AddLogExecutorTask
     */
    public static void addLog(String provider_no, String action, String content, String contentId, String ip, String demographicNo, String data) {
        OscarLog oscarLog = new OscarLog();

        oscarLog.setProviderNo(provider_no);
        oscarLog.setAction(action);
        oscarLog.setContent(content);
        oscarLog.setContentId(contentId);
        oscarLog.setIp(ip);

        try {
            // Parse and set patient demographic ID for PHI tracking
            demographicNo = StringUtils.trimToNull(demographicNo);
            if (demographicNo != null)
                oscarLog.setDemographicId(Integer.parseInt(demographicNo));
        } catch (Exception e) {
            // Log parsing errors but continue with audit entry creation
            logger.error("Unexpected error parsing demographic number: " + demographicNo, e);
        }

        oscarLog.setData(data);

        // Execute asynchronously to maintain system performance
        executorService.execute(new AddLogExecutorTask(oscarLog));
    }

    /**
     * Creates a synchronous audit log entry that participates in current transaction.
     * This method persists the audit log entry in the same thread and database
     * transaction as the calling code, ensuring transactional consistency between
     * clinical actions and their audit trails.
     *
     * <p>This method is used when audit log entries must be committed or rolled back
     * together with the clinical data changes they document. This ensures audit
     * trail integrity in complex transactional scenarios.</p>
     *
     * @param provider_no String identifier of the healthcare provider
     * @param action String describing the clinical or administrative action
     * @param content String identifying the type of clinical content
     * @param contentId String specific identifier of the content accessed
     * @param ip String IP address of the client session
     * @see #addLogSynchronous(OscarLog)
     */
    public static void addLogSynchronous(String provider_no, String action, String content, String contentId, String ip) {
        OscarLog oscarLog = new OscarLog();

        oscarLog.setProviderNo(provider_no);
        oscarLog.setAction(action);
        oscarLog.setContent(content);
        oscarLog.setContentId(contentId);
        oscarLog.setIp(ip);

        addLogSynchronous(oscarLog);
    }

    /**
     * Persists an audit log entry synchronously within the current transaction.
     * This is the core synchronous logging method that directly persists audit
     * entries to the database within the calling thread's transaction context.
     *
     * <p>This method provides fail-safe audit logging by catching and logging
     * any persistence errors without throwing exceptions. This ensures that
     * audit logging failures do not disrupt critical clinical operations while
     * still recording the failure for investigation.</p>
     *
     * <p><strong>Error Handling:</strong> This method will never throw exceptions.
     * Instead, it logs any persistence errors to the application log for
     * administrative review and troubleshooting.</p>
     *
     * @param oscarLog the complete audit log entry to persist, containing all
     *                 relevant clinical and administrative context
     * @see OscarLogDao#persist(OscarLog)
     */
    public static void addLogSynchronous(OscarLog oscarLog) {
        try {
            oscarLogDao.persist(oscarLog);
        } catch (Exception e) {
            // Log audit system errors for administrative investigation
            logger.error("Error in healthcare audit logging system.", e);
            logger.error("Failed to persist audit log entry: " + oscarLog);
        }
    }


    /**
     * Legacy synchronous logging method for CAISI integration compatibility.
     * This method provides backward compatibility with the legacy CAISI (Client
     * Access to Integrated Services and Information) system audit logging.
     *
     * <p>This method extracts provider context directly from the HTTP session
     * and persists audit entries synchronously. It is maintained for legacy
     * system compatibility and should not be used for new development.</p>
     *
     * <p><strong>Note:</strong> New code should use the {@link LoggedInInfo}-based
     * logging methods for better security context management and asynchronous
     * processing capabilities.</p>
     *
     * @param accessType String describing the type of access or action performed,
     *                   typically using legacy CAISI access type constants
     * @param entity String identifying the clinical entity accessed, such as
     *               "client", "program", or "service"
     * @param entityId String specific identifier of the entity accessed
     * @param request HttpServletRequest containing session and network context
     *                for audit trail attribution
     * @deprecated Use {@link #addLog(LoggedInInfo, String, String, String, String, String)}
     *             for new development with better context management
     * @see #addLog(LoggedInInfo, String, String, String, String, String)
     * @since 2005-06-08
     */
    public static void log(String accessType, String entity, String entityId, HttpServletRequest request) {
        OscarLog log = new OscarLog();

        // Extract provider context from HTTP session (legacy approach)
        Provider provider = (Provider) request.getSession().getAttribute("provider");
        if (provider != null)
            log.setProviderNo(provider.getProviderNo());

        log.setAction(accessType);
        log.setContent(entity);
        log.setContentId(entityId);
        log.setIp(request.getRemoteAddr());

        // Persist synchronously for legacy compatibility
        oscarLogDao.persist(log);
    }
}
