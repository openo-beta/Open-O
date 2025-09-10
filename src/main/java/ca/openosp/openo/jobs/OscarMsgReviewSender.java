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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.openosp.openo.jobs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.ProviderDataDao;
import ca.openosp.openo.commn.dao.ResidentOscarMsgDao;
import ca.openosp.openo.commn.dao.UserPropertyDAO;
import ca.openosp.openo.commn.jobs.OscarRunnable;
import ca.openosp.openo.commn.model.OscarMsgType;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.ProviderData;
import ca.openosp.openo.commn.model.ResidentOscarMsg;
import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.commn.model.UserProperty;
import ca.openosp.openo.managers.MessagingManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.messenger.data.MessengerSystemMessage;


/**
 * This class implements a runnable job responsible for sending OSCAR messages
 * to providers, reminding them to review patient charts that require their attention.
 * The job identifies providers who have pending chart reviews and sends them a
 * notification message at a configurable time. This is particularly useful in
 * teaching environments where supervisors must review resident encounters.
 *
 * The process is as follows:
 * 1. The job runs at a scheduled interval.
 * 2. It checks the current time and rounds it to the nearest half-hour to match
 *    provider preference settings for receiving notifications.
 * 3. It fetches a list of all active billing providers.
 * 4. It queries for providers who have a user preference set to receive review
 *    messages at the calculated time.
 * 5. For each of these providers, it retrieves a list of resident encounters
 *    (represented by `ResidentOscarMsg`) that are pending their review.
 * 6. If there are pending reviews, it constructs and sends a system message
 *    containing the details of the charts to be reviewed.
 * 7. It also has a fallback mechanism to send notifications at a default time (9:00 AM)
 *    to providers who have pending reviews but have not configured a specific notification time.
 *
 * This ensures that supervisors are timely notified of their review responsibilities,
 * facilitating a more efficient workflow.
 *
 * @author rjonasz
 */
public class OscarMsgReviewSender implements OscarRunnable {

    private Provider provider;
    private Security security;
    private static final String MESSAGE = "Hello, the following charts require your attention\n Sapere aude!";
    private static final String SUBJECT = "Chart Review";
    private static final Calendar DEFAULT_TIME = new GregorianCalendar(0, 0, 0, 9, 0);
    private final Logger logger = MiscUtils.getLogger();

    /**
     * The main execution method for the OscarMsgReviewSender job.
     * This method is invoked by the scheduling system. It identifies providers with
     * pending chart reviews and sends them notification messages.
     */
    @Override
    public void run() {

        String userNo = provider.getProviderNo();
        LoggedInInfo loggedInInfo = new LoggedInInfo();
        loggedInInfo.setLoggedInSecurity(security);
        loggedInInfo.setLoggedInProvider(provider);

        logger.info("Starting to send OSCAR Review Messages");

        // Set a default notification time of 9:00 AM as a fallback.
        Integer defaultHour = DEFAULT_TIME.get(Calendar.HOUR_OF_DAY);
        Integer defaultMin = DEFAULT_TIME.get(Calendar.MINUTE);

        // Get the current time to determine which providers should be notified.
        Calendar now = GregorianCalendar.getInstance();
        Integer currentHour = now.get(Calendar.HOUR_OF_DAY);
        Integer currentMinute = now.get(Calendar.MINUTE);

        // Round the current time to the nearest half-hour (0 or 30 minutes) to align with notification preferences.
        // This simplifies matching against user-defined settings.
        currentHour = (currentMinute > 45 ? currentHour + 1 : currentHour);
        currentMinute = (currentMinute > 45 || currentMinute < 15 ? 0 : 30);


        // Construct the time string for querying user properties (e.g., "9:30").
        String time = String.valueOf(currentHour) + ":" + String.valueOf(currentMinute);
        ProviderDataDao providerDataDao = SpringUtils.getBean(ProviderDataDao.class);
        List<ProviderData> providerList = providerDataDao.findAllBilling("1");

        // Create a list of provider numbers for all active billing providers.
        List<String> providerNosList = new ArrayList<String>();
        for (ProviderData providerData : providerList) {
            providerNosList.add(providerData.getId());
        }

        UserPropertyDAO propertyDao = SpringUtils.getBean(UserPropertyDAO.class);
        // Find all users who have a preference to receive review messages at the current time.
        List<UserProperty> properties = propertyDao.getPropValues(UserProperty.OSCAR_MSG_RECVD, time);
        ResidentOscarMsgDao residentOscarMsgDao = SpringUtils.getBean(ResidentOscarMsgDao.class);
        List<ResidentOscarMsg> residentOscarMsgList;
        MessagingManager messagingManager = SpringUtils.getBean(MessagingManager.class);

        // This variable will hold the ID of the message after it has been sent.
        Integer messageId = null;

        // Iterate through the providers who have a notification preference set for the current time.
        for (UserProperty property : properties) {

            // Check if the user is in the list of active billing providers.
            if (providerNosList.indexOf(property.getProviderNo()) > -1) {

                // Retrieve all resident messages/charts that require this provider's supervision.
                residentOscarMsgList = residentOscarMsgDao.findBySupervisor(property.getProviderNo());
                StringBuilder msgInfo = new StringBuilder();
                int idx = 1;
                // Compile a comma-separated string of identifiers for the items needing review.
                // This string is passed to the messenger to create links.
                for (ResidentOscarMsg residentOscarMsg : residentOscarMsgList) {
                    msgInfo = msgInfo
                            .append(residentOscarMsg.getDemographic_no())
                            .append(":")
                            .append((residentOscarMsg.getAppointment_no() == null ? "null" : residentOscarMsg.getAppointment_no()))
                            .append(":")
                            .append(residentOscarMsg.getId())
                            .append(":")
                            .append(residentOscarMsg.getNote_id());

                    if (idx < residentOscarMsgList.size()) {
                        msgInfo = msgInfo.append(",");
                    }
                    ++idx;
                }

                // Remove the provider from the list to avoid sending them a duplicate notification later.
                providerNosList.remove(property.getProviderNo());
                if (residentOscarMsgList.size() > 0) {

                    // Collect demographic numbers to attach to the message.
                    List<Integer> attachDemographic = new ArrayList<Integer>();
                    for (ResidentOscarMsg res : residentOscarMsgList) {
                        attachDemographic.add(res.getDemographic_no());
                    }

                    // Construct the system message to be sent.
                    MessengerSystemMessage systemMessage = new MessengerSystemMessage(new String[]{property.getProviderNo()});
                    systemMessage.setType_link(msgInfo.toString());
                    systemMessage.setType(OscarMsgType.OSCAR_REVIEW_TYPE);
                    systemMessage.setSentByNo(userNo);
                    systemMessage.setSubject(SUBJECT);
                    systemMessage.setMessage(MESSAGE);
                    systemMessage.setAttachedDemographicNo(attachDemographic.toArray(new Integer[attachDemographic.size()]));

                    // Send the message using the MessagingManager.
                    messageId = messagingManager.sendSystemMessage(loggedInInfo, systemMessage);

                    if (messageId != null) {
                        logger.info("SENT Review OSCAR MESSAGE");
                    }
                }
            }
        }

        // This section handles providers who have not set a specific notification time.
        // It runs only at the default time (e.g., 9:00 AM) and only for providers who haven't already been sent a message.
        if (currentHour.equals(defaultHour) && currentMinute.equals(defaultMin) && providerNosList.size() > 0) {
            for (String providerNo : providerNosList) {
                // Check if the provider has a value set for OSCAR_MSG_RECVD. If they do, they were handled above
                // or have a different preferred time. If null, it means they have no preference set.
                String userProp = propertyDao.getStringValue(providerNo, UserProperty.OSCAR_MSG_RECVD);

                if (userProp == null) {
                    residentOscarMsgList = residentOscarMsgDao.findBySupervisor(providerNo);
                    StringBuilder msgInfo = new StringBuilder();
                    int idx = 1;
                    for (ResidentOscarMsg r : residentOscarMsgList) {
                        msgInfo = msgInfo.append(r.getDemographic_no()).append(":").append(r.getAppointment_no() == null ? "null" : r.getAppointment_no()).append(":").append(r.getId()).append(":").append(r.getNote_id());
                        if (idx < residentOscarMsgList.size()) {
                            msgInfo = msgInfo.append(",");
                        }
                        ++idx;
                    }
                    if (residentOscarMsgList.size() > 0) {

                        List<Integer> attachDemographic = new ArrayList<Integer>();
                        for (ResidentOscarMsg res : residentOscarMsgList) {
                            attachDemographic.add(res.getDemographic_no());
                        }

                        MessengerSystemMessage systemMessage = new MessengerSystemMessage(new String[]{providerNo});
                        systemMessage.setType_link(msgInfo.toString());
                        systemMessage.setType(OscarMsgType.OSCAR_REVIEW_TYPE);
                        systemMessage.setSentByNo(userNo);
                        systemMessage.setSubject(SUBJECT);
                        systemMessage.setMessage(MESSAGE);
                        systemMessage.setAttachedDemographicNo(attachDemographic.toArray(new Integer[attachDemographic.size()]));

                        messageId = messagingManager.sendSystemMessage(loggedInInfo, systemMessage);

                        if (messageId != null) {
                            logger.info("SENT DEFAULT TIME OSCAR Review MESSAGE");
                        }
                    }
                }
            }
        }

        logger.info("Completed Sending OSCAR Review Messages");
    }

    /**
     * Sets the provider context for this job. This is typically the provider
     * under whose authority the job is executed.
     *
     * @param provider The provider object representing the user running the job.
     */
    @Override
    public void setLoggedInProvider(Provider provider) {
        this.provider = provider;
    }

    /**
     * Sets the security context for this job. This contains the security credentials
     * required to perform actions within the system.
     *
     * @param security The security object containing user credentials and permissions.
     */
    @Override
    public void setLoggedInSecurity(Security security) {
        this.security = security;
    }

    /**
     * A configuration method required by the OscarRunnable interface. This method
     * is not used in this implementation.
     *
     * @param string Configuration string (not used).
     */
    @Override
    public void setConfig(String string) {
    }
}
