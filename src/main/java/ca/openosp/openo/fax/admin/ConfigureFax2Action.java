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

import com.opensymphony.xwork2.ActionSupport;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import ca.openosp.openo.commn.dao.FaxConfigDao;
import ca.openosp.openo.commn.model.FaxConfig;
import ca.openosp.openo.managers.FaxManager;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import ca.openosp.openo.form.JSONUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Administrative action for configuring fax service providers and gateway accounts in healthcare environments.
 *
 * This action manages the critical configuration of medical fax communication systems, which remain
 * essential in healthcare for secure transmission of patient health information (PHI) between providers,
 * specialists, laboratories, and other healthcare facilities. Proper fax configuration is mandatory
 * for HIPAA/PIPEDA compliance and ensures reliable document delivery in medical workflows.
 *
 * The configuration system supports multiple fax service providers including:
 * - SRFax for cloud-based secure medical fax transmission
 * - MyFax and other HIPAA-compliant fax services
 * - Traditional fax servers with secure connectivity
 * - Multi-account setups for different departments or providers
 *
 * Key features include:
 * - Secure credential management with password protection
 * - Queue-based inbox routing for different medical departments
 * - Account activation/deactivation for service management
 * - Download configuration for incoming medical documents
 * - Fax scheduler management for automated processing
 *
 * Administrative privileges are required for all configuration operations to maintain
 * system security and prevent unauthorized access to healthcare communication settings.
 *
 * @see ca.openosp.openo.managers.FaxManager
 * @see ca.openosp.openo.commn.model.FaxConfig
 * @see ca.openosp.openo.fax.core.FaxSchedulerJob
 * @since 2014-08-29
 */
public class ConfigureFax2Action extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse response = ServletActionContext.getResponse();

    private SecurityInfoManager securityInfoManager = SpringUtils.getBean(SecurityInfoManager.class);
    private final FaxManager faxManager = SpringUtils.getBean(FaxManager.class);

    /** Mask string used to protect actual passwords in form submissions */
    private static final String PASSWORD_BLANKET = "**********";

    /**
     * Main execution method that delegates to the configure method for fax setup.
     *
     * @return String result of configuration operation
     */
    public String execute() {
        return configure();
    }

    /**
     * Configures fax service provider accounts and gateway settings for medical document transmission.
     *
     * This method handles the complete configuration lifecycle for healthcare fax systems including:
     * - Creating new fax service accounts with secure credential storage
     * - Updating existing account settings while preserving encrypted passwords
     * - Managing multiple fax accounts for different medical departments
     * - Configuring inbox queues for automatic routing of incoming medical documents
     * - Setting up download preferences for received fax documents
     *
     * The configuration process implements several security measures:
     * - Password blanking to prevent credential exposure in form data
     * - Administrative privilege validation before any configuration changes
     * - Secure credential persistence with encrypted storage
     * - Automatic cleanup of removed accounts to prevent orphaned configurations
     *
     * The method supports complex healthcare workflows where different fax numbers
     * may be associated with specific medical departments, providers, or document types.
     *
     * @return String null to indicate direct JSON response (no view forwarding)
     */
    public String configure() {
        JSONObject jsonObject;

        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin", "r", null)) {
            throw new SecurityException("missing required sec object (_admin)");
        }

        try {
            FaxConfigDao faxConfigDao = SpringUtils.getBean(FaxConfigDao.class);
            List<FaxConfig> savedFaxConfigList = faxConfigDao.findAll(null, null);
            List<FaxConfig> faxConfigList = new ArrayList<FaxConfig>();

            String faxUrl = request.getParameter("faxUrl");
            String siteUser = request.getParameter("siteUser");
            String sitePasswd = request.getParameter("sitePasswd");

            String[] faxConfigIds = request.getParameterValues("id");
            String[] faxUsers = request.getParameterValues("faxUser");
            String[] faxPasswds = request.getParameterValues("faxPassword");
            String[] inboxQueues = request.getParameterValues("inboxQueue");
            String[] activeState = request.getParameterValues("activeState");
            String[] faxNumbers = request.getParameterValues("faxNumber");
            String[] senderEmails = request.getParameterValues("senderEmail");
            String[] accountNames = request.getParameterValues("accountName");
            String[] downloadState = request.getParameterValues("downloadState");

            Integer id;
            int savedidx;
            FaxConfig faxConfig;
            FaxConfig savedFaxConfig;
            FaxConfig masterFaxConfig;

            // Handle complete removal of all fax accounts
            if (faxConfigIds == null) {
                for (FaxConfig sfaxConfig : savedFaxConfigList) {
                    faxConfigDao.remove(sfaxConfig.getId());
                }
            } else {
                for (int idx = 0; idx < faxConfigIds.length; ++idx) {
                    if (StringUtils.trimToNull(faxConfigIds[idx]) == null) {
                        continue;
                    }
                    id = Integer.parseInt(faxConfigIds[idx]);
                    faxConfig = new FaxConfig();
                    faxConfig.setId(id);

                    savedidx = savedFaxConfigList.indexOf(faxConfig);
                    if (savedidx > -1) {
                        savedFaxConfig = savedFaxConfigList.get(savedidx);
                        savedFaxConfig.setUrl(faxUrl);
                        savedFaxConfig.setSiteUser(siteUser);

                        // Only update password if it's not the masked placeholder
                        // This preserves existing encrypted passwords in the database
                        if (!PASSWORD_BLANKET.equals(sitePasswd)) {
                            savedFaxConfig.setPasswd(sitePasswd);
                        }

                        savedFaxConfig.setFaxUser(faxUsers[idx]);

                        if (!PASSWORD_BLANKET.equals(faxPasswds[idx])) {
                            savedFaxConfig.setFaxPasswd(faxPasswds[idx]);
                        }

                        // Normalize fax numbers by removing all non-digit characters
                        // This ensures consistent format for healthcare provider lookup
                        String faxNumber = faxNumbers[idx];
                        if (faxNumber != null) {
                            faxNumber = faxNumber.trim().replaceAll("\\D", "");
                        }
                        savedFaxConfig.setFaxNumber(faxNumber);
                        savedFaxConfig.setSenderEmail(senderEmails[idx]);
                        savedFaxConfig.setQueue(Integer.parseInt(inboxQueues[idx]));
                        savedFaxConfig.setAccountName(accountNames[idx]);
                        savedFaxConfig.setActive(Boolean.parseBoolean(activeState[idx]));
                        savedFaxConfig.setDownload(Boolean.parseBoolean(downloadState[idx]));
                        faxConfigList.add(savedFaxConfig);
                    } else {
                        faxConfig.setId(null);
                        faxConfig.setSiteUser(siteUser);

                        if (!PASSWORD_BLANKET.equals(sitePasswd)) {
                            faxConfig.setPasswd(sitePasswd);
                        }
                        // the password carries over from the last configuration. Usually the first entry
                        else if ((masterFaxConfig = savedFaxConfigList.get(0)) != null) {
                            faxConfig.setPasswd(masterFaxConfig.getPasswd());
                        }

                        faxConfig.setUrl(faxUrl);
                        faxConfig.setFaxUser(faxUsers[idx]);

                        if (!PASSWORD_BLANKET.equals(faxPasswds[idx])) {
                            faxConfig.setFaxPasswd(faxPasswds[idx]);
                        }

                        faxConfig.setFaxNumber(faxNumbers[idx]);
                        faxConfig.setSenderEmail(senderEmails[idx]);
                        faxConfig.setQueue(Integer.parseInt(inboxQueues[idx]));
                        faxConfig.setAccountName(accountNames[idx]);
                        faxConfig.setActive(Boolean.parseBoolean(activeState[idx]));
                        faxConfig.setDownload(Boolean.parseBoolean(downloadState[idx]));
                        faxConfigList.add(faxConfig);
                    }
                }


                for (FaxConfig faxConfig1 : faxConfigList) {
                    faxConfigDao.saveEntity(faxConfig1);
                }


                for (FaxConfig faxConfig2 : savedFaxConfigList) {
                    if (!faxConfigList.contains(faxConfig2)) {
                        faxConfigDao.remove(faxConfig2.getId());
                    }
                }
            }

            // Preserve fax server configuration even when all accounts are removed
            // This maintains connection settings for future account setup
            int auditList = faxConfigDao.getCountAll();
            if (auditList == 0) {
                faxConfig = new FaxConfig();
                faxConfig.setUrl(faxUrl);
                faxConfig.setSiteUser(siteUser);

                if (!PASSWORD_BLANKET.equals(sitePasswd)) {
                    faxConfig.setPasswd(sitePasswd);
                }
                // the password carries over from the last configuration. Usually the first entry
                else if ((masterFaxConfig = savedFaxConfigList.get(0)) != null) {
                    faxConfig.setPasswd(masterFaxConfig.getPasswd());
                }
                faxConfigDao.saveEntity(faxConfig);
            }

            jsonObject = JSONObject.fromObject("{success:true}");
        } catch (Exception ex) {
            jsonObject = JSONObject.fromObject("{success:false}");
            MiscUtils.getLogger().error("COULD NOT SAVE FAX CONFIGURATION", ex);
        }


        try {
            MiscUtils.getLogger().info("JSON: " + jsonObject);
            jsonObject.write(response.getWriter());
        } catch (IOException e) {
            MiscUtils.getLogger().error("JSON WRITER ERROR", e);
        }
        return null;

    }

    /**
     * Restarts the fax scheduler service for processing medical document transmissions.
     *
     * This method provides administrative control over the automated fax processing system
     * that handles queued medical documents. The scheduler manages:
     * - Batch transmission of outgoing medical faxes
     * - Status updates for sent documents
     * - Importing of incoming medical documents
     * - Cleanup of temporary files containing PHI
     *
     * Restarting the scheduler may be necessary when:
     * - Configuration changes require service reload
     * - Error conditions have stopped automated processing
     * - Network connectivity issues have been resolved
     * - Performance optimization requires service restart
     *
     * Administrative privileges with fax restart permissions are required to prevent
     * unauthorized interruption of critical medical document transmission services.
     *
     * @throws SecurityException if user lacks _admin.fax.restart write privileges
     */
    public void restartFaxScheduler() {
        if (!securityInfoManager.hasPrivilege(LoggedInInfo.getLoggedInInfoFromSession(request), "_admin.fax.restart", "w", null)) {
            throw new SecurityException("missing required sec object (_admin.fax.restart)");
        }
        faxManager.restartFaxScheduler(LoggedInInfo.getLoggedInInfoFromSession(request));
    }

    /**
     * Retrieves the current operational status of the fax scheduler service.
     *
     * This method provides real-time monitoring capabilities for healthcare administrators
     * to ensure continuous operation of medical document transmission services. The status
     * information includes:
     * - Service running state (active/inactive)
     * - Last execution timestamp
     * - Error conditions or processing issues
     * - Queue sizes and processing statistics
     *
     * Status monitoring is critical in healthcare environments where delayed or failed
     * document transmission can impact patient care coordination between providers.
     *
     * @return JSON response containing current scheduler status information
     * @throws SecurityException if user lacks _admin.fax.restart read privileges
     */
    public void getFaxSchedularStatus() {
        LoggedInInfo loggedInInfo = LoggedInInfo.getLoggedInInfoFromSession(request);
        if (!securityInfoManager.hasPrivilege(loggedInInfo, "_admin.fax.restart", "r", null)) {
            throw new SecurityException("missing required sec object (_admin.fax.restart)");
        }
        JSONUtil.jsonResponse(response, faxManager.getFaxSchedularStatus(loggedInInfo));
    }

}
