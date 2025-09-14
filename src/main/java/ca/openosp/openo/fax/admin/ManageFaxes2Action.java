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
package ca.openosp.openo.fax.admin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.http.HttpStatus;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.FaxClientLogDao;
import ca.openosp.openo.commn.dao.FaxConfigDao;
import ca.openosp.openo.commn.dao.FaxJobDao;
import ca.openosp.openo.commn.model.FaxClientLog;
import ca.openosp.openo.commn.model.FaxConfig;
import ca.openosp.openo.commn.model.FaxJob;
import ca.openosp.openo.managers.FaxManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import ca.openosp.openo.form.JSONUtil;

/**
 * Administrative action for managing and monitoring medical fax transmissions in healthcare workflows.
 *
 * This action provides comprehensive fax management capabilities essential for healthcare operations,
 * including tracking of medical document transmissions, handling failed deliveries, and maintaining
 * audit trails required for regulatory compliance. The management system supports the critical need
 * for reliable document exchange between healthcare providers, specialists, laboratories, and patients.
 *
 * Key healthcare fax management operations include:
 * - Monitoring transmission status of lab results, consultation requests, and referrals
 * - Cancelling fax jobs for time-sensitive medical communications
 * - Resending failed transmissions to ensure critical medical information reaches recipients
 * - Generating comprehensive reports for regulatory auditing and quality assurance
 * - Managing fax queues and prioritizing urgent medical documents
 *
 * The system maintains detailed logs of all fax activities including:
 * - Transmission timestamps and delivery confirmations
 * - Recipient information and document types
 * - Error conditions and retry attempts
 * - Patient demographic associations for PHI tracking
 * - Provider and team-based filtering for workflow management
 *
 * Administrative privileges are required for fax cancellation and management operations
 * to maintain system security and prevent unauthorized interference with medical communications.
 *
 * @see ca.openosp.openo.managers.FaxManager
 * @see ca.openosp.openo.commn.model.FaxJob
 * @see ca.openosp.openo.commn.model.FaxClientLog
 * @since 2014-08-29
 */
public class ManageFaxes2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private final Logger log = MiscUtils.getLogger();
    private final SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);

    private final FaxManager faxManager = SpringUtils.getBean(FaxManager.class);

    /**
     * Main execution method that routes fax management requests to appropriate handlers.
     *
     * This method supports various healthcare fax management operations including cancellation
     * of urgent medical transmissions, resending failed documents, and retrieving status information
     * for audit and monitoring purposes.
     *
     * @return String result name for view forwarding, defaults to fetchFaxStatus for monitoring
     */
    public String execute() {
        String method = request.getParameter("method");
        if ("CancelFax".equals(method)) {
            return CancelFax();
        } else if ("ResendFax".equals(method)) {
            return ResendFax();
        } else if ("fetchFaxStatus".equals(method)) {
            return fetchFaxStatus();
        }
        return fetchFaxStatus();
        
    }


    /**
     * Cancels an active or pending medical fax transmission to prevent delivery of outdated information.
     *
     * This method provides critical functionality for healthcare workflows where medical information
     * may become outdated or incorrect after a fax has been queued for transmission. Common scenarios include:
     * - Updated lab results requiring cancellation of preliminary reports
     * - Changes in patient condition affecting consultation requests
     * - Correction of medication prescriptions before transmission to pharmacy
     * - Recall of documents due to patient privacy concerns
     *
     * The cancellation process includes:
     * - Validation of administrative privileges for security
     * - Communication with external fax service providers to halt transmission
     * - Status updates for tracking and audit purposes
     * - Proper logging for regulatory compliance requirements
     *
     * @param jobId String unique identifier of the fax job to cancel
     * @return String null indicating direct JSON response
     * @throws SecurityException if user lacks administrative privileges
     */
    @SuppressWarnings("unused")
    public String CancelFax() {

        String jobId = request.getParameter("jobId");

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin", "w", null)) {
            throw new SecurityException("missing required sec object (_admin)");
        }

        FaxJobDao faxJobDao = SpringUtils.getBean(FaxJobDao.class);
        FaxConfigDao faxConfigDao = SpringUtils.getBean(FaxConfigDao.class);
        FaxJob faxJob = faxJobDao.find(Integer.parseInt(jobId));
        FaxConfig faxConfig = faxConfigDao.getConfigByNumber(faxJob.getFax_line());
        String result = "{success:false}";

        log.info("TRYING TO CANCEL FAXJOB " + faxJob.getJobId());

        if (faxConfig == null) {
            log.error("Could not find faxConfig while processing fax id: " + faxJob.getId() + " Has the fax number changed?");
        } else if (faxConfig.isActive()) {

            if (faxJob.getStatus().equals(FaxJob.STATUS.SENT)) {
                faxJob.setStatus(FaxJob.STATUS.CANCELLED);
                faxJobDao.merge(faxJob);
                result = "{success:true}";

            }

            if (faxJob.getJobId() != null) {

                if (faxJob.getStatus().equals(FaxJob.STATUS.WAITING)) {
                    try (DefaultHttpClient client = new DefaultHttpClient()) {
                        client.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(faxConfig.getSiteUser(), faxConfig.getPasswd()));

                        HttpPut mPut = new HttpPut(faxConfig.getUrl() + "/fax/" + faxJob.getJobId());
                        mPut.setHeader("accept", "application/json");
                        mPut.setHeader("user", faxConfig.getFaxUser());
                        mPut.setHeader("passwd", faxConfig.getFaxPasswd());

                        HttpResponse httpResponse = client.execute(mPut);

                        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

                            HttpEntity httpEntity = httpResponse.getEntity();
                            result = EntityUtils.toString(httpEntity);

                            faxJob.setStatus(FaxJob.STATUS.CANCELLED);
                            faxJobDao.merge(faxJob);
                        }

                    } catch (IOException e) {
                        log.error("PROBLEM COMM WITH WEB SERVICE");
                    }
                }
            }
        }

        JSONUtil.jsonResponse(response, JSONObject.fromObject(result));

        return null;

    }

    /**
     * Resends a failed medical fax transmission to ensure critical healthcare information reaches recipients.
     *
     * This method handles the resending of medical documents that failed initial transmission due to
     * network issues, busy recipient lines, or temporary service outages. Reliable document delivery
     * is essential in healthcare for:
     * - Time-sensitive lab results requiring immediate attention
     * - Urgent consultation requests and specialist referrals
     * - Critical medication orders and prescription changes
     * - Emergency medical information and patient transfers
     *
     * The resend process includes:
     * - Administrative privilege validation
     * - Service availability verification
     * - Document retrieval from secure storage
     * - New transmission attempt with updated parameters
     * - Audit logging for regulatory compliance
     *
     * @param jobId String identifier of the failed fax job to resend
     * @param faxNumber String destination fax number (may be updated)
     * @return String null indicating direct JSON response with success status
     * @throws SecurityException if user lacks administrative privileges
     */
    @SuppressWarnings("unused")
    public String ResendFax() {

        JSONObject jsonObject = JSONObject.fromObject("{success:false}");
        String JobId = request.getParameter("jobId");
        String faxNumber = request.getParameter("faxNumber");
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);

        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin", "w", null)) {
            throw new SecurityException("missing required sec object (_admin)");
        }

        boolean success = false;

        /*
         *  Dont even try to resend a fax if the service is not enabled.
         */
        if (FaxManager.isEnabled()) {
            success = faxManager.resendFax(loggedInInfo, JobId, faxNumber);
        }

        JSONUtil.jsonResponse(response, JSONObject.fromObject("{success:" + success + "}"));

        return null;
    }

    /**
     * Provides secure viewing access to transmitted medical fax documents for audit and review purposes.
     *
     * This method enables healthcare administrators and authorized personnel to review previously
     * transmitted medical documents for quality assurance, regulatory compliance, and patient care
     * coordination. Document viewing is essential for:
     * - Verifying accurate transmission of patient health information
     * - Regulatory audits and compliance reporting
     * - Quality assurance reviews of medical communications
     * - Investigation of transmission issues or delivery disputes
     *
     * Access control ensures that only users with document viewing privileges can access
     * sensitive medical information, maintaining HIPAA/PIPEDA compliance requirements.
     *
     * @throws SecurityException if user lacks _edoc read privileges
     */
    @SuppressWarnings("unused")
    public void viewFax() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_edoc", "r", null)) {
            throw new SecurityException("missing required sec object (_edoc)");
        }

        getPreview();
    }

    /**
     * Get a preview image of the entire fax document.
     */
    @SuppressWarnings("unused")
    public void getPreview() {

        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        String faxFilePath = request.getParameter("faxFilePath");
        String pageNumber = request.getParameter("pageNumber");
        String showAs = request.getParameter("showAs");
        Path outfile = null;
        int page = 1;
        String jobId = request.getParameter("jobId");
        FaxJob faxJob = null;

        if (jobId != null && !jobId.isEmpty()) {
            faxJob = faxManager.getFaxJob(loggedInInfo, Integer.parseInt(jobId));
        }

        if (faxJob != null) {
            faxFilePath = faxJob.getFile_name();
        }

        if (pageNumber != null && !pageNumber.isEmpty()) {
            page = Integer.parseInt(pageNumber);
        }

        /*
         * Displaying the entire PDF using the default browser's view before faxing an EForm (in CoverPage.jsp),
         * and when viewing it in the fax records (Manage Faxes), it is shown as images.
         */
        if (faxFilePath != null && !faxFilePath.isEmpty()) {
            if (showAs != null && showAs.equals("image")) {
                outfile = faxManager.getFaxPreviewImage(loggedInInfo, faxFilePath, page);
                response.setContentType("image/pnsg");
                response.setHeader("Content-Disposition", "attachment;filename=" + outfile.getFileName().toString());
            } else {
                outfile = Paths.get(faxFilePath);
                response.setContentType("application/pdf");
            }
        }

        if (outfile != null) {
            try (InputStream inputStream = Files.newInputStream(outfile);
                 BufferedInputStream bfis = new BufferedInputStream(inputStream);
                 ServletOutputStream outs = response.getOutputStream()) {

                int data;
                while ((data = bfis.read()) != -1) {
                    outs.write(data);
                }
                outs.flush();
            } catch (IOException e) {
                log.error("Error", e);
            }
        }
    }

    /**
     * Retrieves comprehensive fax transmission status information for healthcare monitoring and reporting.
     *
     * This method provides detailed status tracking for medical fax transmissions, supporting healthcare
     * quality assurance and regulatory compliance requirements. The status information includes transmission
     * outcomes, delivery confirmations, error conditions, and associated patient demographic data.
     *
     * Query parameters support complex filtering for healthcare workflows:
     * - Provider-specific filtering for individual physician practices
     * - Team-based filtering for department or clinic-level reporting
     * - Date range filtering for regulatory reporting periods
     * - Status-based filtering for failed transmission analysis
     * - Patient demographic filtering for specific case reviews
     *
     * The comprehensive reporting supports:
     * - Daily operational monitoring of medical communications
     * - Monthly and quarterly regulatory compliance reporting
     * - Investigation of transmission failures affecting patient care
     * - Performance analysis of fax service providers
     *
     * @param status String filter by transmission status (SENT, WAITING, ERROR, etc.)
     * @param team String filter by healthcare team or department
     * @param dateBegin String start date for reporting period (yyyy-MM-dd format)
     * @param dateEnd String end date for reporting period (yyyy-MM-dd format)
     * @param oscarUser String filter by specific healthcare provider
     * @param demographic_no String filter by specific patient demographic
     * @return String "faxstatus" result forwarding to status display view
     */
    @SuppressWarnings("unused")
    public String fetchFaxStatus() {

        String statusStr = request.getParameter("status");
        String teamStr = request.getParameter("team");
        String dateBeginStr = request.getParameter("dateBegin");
        String dateEndStr = request.getParameter("dateEnd");
        String provider_no = request.getParameter("oscarUser");
        String demographic_no = request.getParameter("demographic_no");

        // Convert UI placeholder values to null for database queries
        // -1 indicates "All" in dropdown selections for healthcare filtering
        if (provider_no.equalsIgnoreCase("-1")) {
            provider_no = null;
        }

        if (statusStr.equalsIgnoreCase("-1")) {
            statusStr = null;
        }

        if (teamStr.equalsIgnoreCase("-1")) {
            teamStr = null;
        }

        // Handle empty or null demographic numbers for patient filtering
        if ("null".equalsIgnoreCase(demographic_no) || "".equals(demographic_no)) {
            demographic_no = null;
        }

        Calendar calendar = GregorianCalendar.getInstance();
        Date dateBegin = null, dateEnd = null;
        String datePattern[] = new String[]{"yyyy-MM-dd"};

        if (dateBeginStr != null && !dateBeginStr.isEmpty()) {
            try {
                dateBegin = DateUtils.parseDate(dateBeginStr, datePattern);
                // Set start of day for inclusive date range filtering
                calendar.setTime(dateBegin);
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                dateBegin = calendar.getTime();
            } catch (ParseException e) {
                dateBegin = null;
                MiscUtils.getLogger().error("UNPARSEABLE DATE " + dateBeginStr);
            }
        }
        if (dateEndStr != null && !dateEndStr.isEmpty()) {
            try {
                dateEnd = DateUtils.parseDate(dateEndStr, datePattern);
                // Set end of day for inclusive date range filtering
                calendar.setTime(dateEnd);
                calendar.set(Calendar.HOUR, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.MILLISECOND, 59);
                dateEnd = calendar.getTime();

            } catch (ParseException e) {
                dateEnd = null;
                MiscUtils.getLogger().error("UNPARSEABLE DATE " + dateEndStr);
            }
        }

        FaxJobDao faxJobDao = SpringUtils.getBean(FaxJobDao.class);
        FaxClientLogDao faxClientLogDao = SpringUtils.getBean(FaxClientLogDao.class);

        List<FaxJob> faxJobList = faxJobDao.getFaxStatusByDateDemographicProviderStatusTeam(demographic_no, provider_no, statusStr, teamStr, dateBegin, dateEnd);

        List<Integer> faxIds = new ArrayList<>();
        for (FaxJob faxJob : faxJobList) {
            faxIds.add(faxJob.getId());
        }
        List<FaxClientLog> faxClientLogs = faxClientLogDao.findClientLogbyFaxIds(faxIds);

        request.setAttribute("faxes", faxJobList);
        request.setAttribute("faxClientLogs", faxClientLogs);

        return "faxstatus";
    }

    /**
     * Marks a fax transmission as resolved or completed in the healthcare workflow system.
     *
     * This method provides closure for fax transmission tracking by marking jobs as resolved,
     * typically used when:
     * - Delivery has been confirmed by the receiving healthcare provider
     * - Issues with failed transmissions have been addressed through alternative means
     * - Administrative review has determined that no further action is required
     * - Quality assurance processes have validated successful document delivery
     *
     * Marking faxes as completed is important for:
     * - Maintaining accurate transmission statistics
     * - Closing workflow loops in healthcare communication
     * - Supporting regulatory audit requirements
     * - Enabling performance analysis of fax operations
     *
     * @param jobId String identifier of the fax job to mark as completed
     * @throws SecurityException if user lacks administrative privileges
     */
    @SuppressWarnings("unused")
    public void SetCompleted() {

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin", "w", null)) {
            throw new SecurityException("missing required sec object (_admin)");
        }


        String id = request.getParameter("jobId");
        FaxJobDao faxJobDao = SpringUtils.getBean(FaxJobDao.class);

        FaxJob faxJob = faxJobDao.find(Integer.parseInt(id));
        faxJob.setStatus(FaxJob.STATUS.RESOLVED);
        faxJobDao.merge(faxJob);
    }

}
