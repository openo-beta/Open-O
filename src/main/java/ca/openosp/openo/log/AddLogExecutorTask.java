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

package ca.openosp.openo.log;

import ca.openosp.openo.commn.model.OscarLog;
import ca.openosp.openo.utility.DbConnectionFilter;

/**
 * Asynchronous executor task for healthcare audit logging in OpenO EMR system.
 * This class implements the asynchronous processing of audit log entries to ensure
 * that healthcare compliance logging does not impact clinical workflow performance.
 *
 * <p>The healthcare industry requires comprehensive audit trails for all patient
 * health information (PHI) access and clinical actions to maintain HIPAA and PIPEDA
 * compliance. This task executes in a separate thread pool to ensure that audit
 * logging operations do not block clinical operations or degrade system performance.</p>
 *
 * <p>Each audit log entry captures critical information including provider identity,
 * patient demographics, clinical actions performed, and system access details.
 * This information is essential for:</p>
 * <ul>
 *   <li>Regulatory compliance reporting</li>
 *   <li>Security breach investigation</li>
 *   <li>Quality assurance monitoring</li>
 *   <li>Clinical workflow analysis</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This class should only be instantiated and used by
 * {@link LogAction} class. Direct usage by other components may compromise the
 * integrity of the audit logging system.</p>
 *
 * @see LogAction
 * @see OscarLog
 * @see ca.openosp.openo.utility.DbConnectionFilter
 * @since 2010-07-08
 */
class AddLogExecutorTask implements Runnable {

    /**
     * The audit log entry containing healthcare action details to be persisted.
     * This log entry contains sensitive information including provider identity,
     * patient identifiers, and clinical action details.
     */
    private OscarLog oscarLog;

    /**
     * Constructs a new asynchronous audit logging task.
     *
     * @param oscarLog the audit log entry containing healthcare action details
     *                 including provider identity, patient information, and
     *                 clinical action performed. Must not be null.
     * @throws IllegalArgumentException if oscarLog is null
     */
    public AddLogExecutorTask(OscarLog oscarLog) {
        this.oscarLog = oscarLog;
    }

    /**
     * Executes the asynchronous audit logging operation.
     * This method persists the healthcare audit log entry to the database
     * in a separate thread to maintain clinical workflow performance.
     *
     * <p>The method ensures proper database resource cleanup through the
     * DbConnectionFilter to prevent connection leaks in the thread pool
     * environment. This is critical for maintaining system stability in
     * high-volume clinical environments.</p>
     *
     * <p>Any exceptions during audit logging are handled gracefully to ensure
     * that audit logging failures do not impact clinical operations. However,
     * failed audit entries are logged to the application log for investigation.</p>
     */
    public void run() {
        try {
            // Persist the audit log entry synchronously within this thread
            LogAction.addLogSynchronous(oscarLog);
        } finally {
            // Critical cleanup to prevent database connection leaks in thread pool
            DbConnectionFilter.releaseAllThreadDbResources();
        }
    }
}
