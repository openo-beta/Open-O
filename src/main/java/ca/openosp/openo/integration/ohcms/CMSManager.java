/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * <p>
 * This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.integration.ohcms;

import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.integration.dhdr.OmdGateway;
import ca.openosp.openo.integration.fhircast.Event;
import ca.openosp.openo.integration.fhircast.UserLogin;
import ca.openosp.openo.integration.oneId.OneIdGatewayData;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;
import java.util.UUID;

public class CMSManager {

  static Logger logger = MiscUtils.getLogger();

  public static String createHubTopic(LoggedInInfo loggedInInfo) throws Exception {
    OmdGateway omdGateway = new OmdGateway();
    OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
    WebClient createHubTopic = omdGateway.getWebClientWholeURL(loggedInInfo,
        oneIdGatewayData.getCmsUrl() + "/createHubTopic");
    Response hubTopicResponse = omdGateway.doPost(loggedInInfo, createHubTopic,
        new Event(UUID.randomUUID().toString(), "hubTopic", "createHubTopic"));
    String hubTopicResponseBody = hubTopicResponse.readEntity(String.class);
    JSONObject responseB = new JSONObject(hubTopicResponseBody);
    logger.debug("hubTopicResponse: " + hubTopicResponseBody);
    oneIdGatewayData.setHubTopic(responseB.getString("hub.topic"));
    return null;
  }

  @Nullable
  public static String userLogin(LoggedInInfo loggedInInfo) throws Exception {
    FhirResources fhirResources = new FhirResources();
    OmdGateway omdGateway = new OmdGateway();
    OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
    if (oneIdGatewayData.getHubTopic() == null) {
      createHubTopic(loggedInInfo);
    }
    WebClient createHubTopic = omdGateway.getWebClientWholeURL(loggedInInfo,
        oneIdGatewayData.getCmsUrl());
    String uuid = UUID.randomUUID().toString();
    UserLogin userLogin = new UserLogin(uuid, oneIdGatewayData.getHubTopic());
    try {
      userLogin.addContext("organization",
          fhirResources.getString(fhirResources.getOrganization(loggedInInfo)));
      userLogin.addContext("practitioner",
          fhirResources.getString(fhirResources.getPractitioner(loggedInInfo)));
      String language = loggedInInfo.getLocale().getLanguage();
      if ("en".equals(language) || "fr".equals(language)) {
        logger.debug("Language selected was " + language);//all good
      } else {
        logger.info("unknown Language selected: " + language + " changing to en");//all good
      }
      userLogin.addContext("parameters", fhirResources.getString(
          fhirResources.getLanguageParameter(UUID.randomUUID().toString(), "en")));

    } catch (CMSException cme) {
      omdGateway.logError(loggedInInfo, "CMS", "userLogin configuration error",
          cme.getLocalizedMessage());
      throw (cme);
    }
    Response hubTopicResponse = omdGateway.doPost(loggedInInfo, createHubTopic, userLogin);
    String hubTopicResponseBody = hubTopicResponse.readEntity(String.class);
    logger.error("userLoginResponse: " + hubTopicResponseBody);
    if (hubTopicResponse.getStatus() >= 200 && hubTopicResponse.getStatus() < 300) {
      oneIdGatewayData.setCmsLoggedIn(hubTopicResponseBody);
    } else if (hubTopicResponse.getStatus() >= 400 && hubTopicResponseBody != null) {
      throw new CMSException(hubTopicResponseBody);
    } else {
      throw new CMSException();
    }
    return null;
  }

  @Nullable
  public static String organizationChange(LoggedInInfo loggedInInfo) throws Exception {
    OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
    if (oneIdGatewayData.getCmsLoggedIn() == null) {
      return null;
    }
    FhirResources fhirResources = new FhirResources();
    OmdGateway omdGateway = new OmdGateway();
    WebClient createHubTopic = omdGateway.getWebClientWholeURL(loggedInInfo,
        oneIdGatewayData.getCmsUrl());
    String uuid = UUID.randomUUID().toString();
    Event event = new Event(uuid, oneIdGatewayData.getHubTopic(), "OH.Organization-change");
    event.addContext("organization",
        fhirResources.getString(fhirResources.getOrganization(loggedInInfo)));
    Response hubTopicResponse = omdGateway.doPost(loggedInInfo, createHubTopic, event);
    String hubTopicResponseBody = hubTopicResponse.readEntity(String.class);
    logger.debug("OH.Organization-change: " + hubTopicResponseBody);
    if (hubTopicResponse.getStatus() >= 200 && hubTopicResponse.getStatus() < 300) {
      oneIdGatewayData.setUpdateUAOInCMS(false);
    } else if (hubTopicResponse.getStatus() >= 400 && hubTopicResponseBody != null) {
      throw new CMSException(hubTopicResponseBody);
    } else {
      throw new CMSException();
    }
    return null;
  }

  @Nullable
  public static String patientOpen(LoggedInInfo loggedInInfo, int demographicNo) throws Exception {
    DemographicDao demographicDao = SpringUtils.getBean(DemographicDao.class);
    Demographic demographic = demographicDao.getDemographicById(demographicNo);
    FhirResources fhirResources = new FhirResources();
    OmdGateway omdGateway = new OmdGateway();
    OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
    if (oneIdGatewayData.getCmsLoggedIn() == null) {
      userLogin(loggedInInfo);
    }
    WebClient createHubTopic = omdGateway.getWebClientWholeURL(loggedInInfo,
        oneIdGatewayData.getCmsUrl());
    String uuid = UUID.randomUUID().toString();
    Event event = new Event(uuid, oneIdGatewayData.getHubTopic(), "Patient-open");
    try {
      event.addContext("patient",
          fhirResources.getString(fhirResources.getPatient(demographic)));
    } catch (CMSException cme) {
      omdGateway.logError(loggedInInfo, "CMS", "Patient-open error", cme.getLocalizedMessage());
      throw (cme);
    }
    Response hubTopicResponse = omdGateway.doPost(loggedInInfo, createHubTopic, event);
    String hubTopicResponseBody = hubTopicResponse.readEntity(String.class);
    logger.debug("patientOpen: " + hubTopicResponseBody);

    if (hubTopicResponse.getStatus() >= 200 && hubTopicResponse.getStatus() < 300) {
      oneIdGatewayData.setCmsPatientInContext("" + demographicNo);
    } else if (hubTopicResponse.getStatus() >= 400 && hubTopicResponseBody != null) {
      throw new CMSException(hubTopicResponseBody);
    } else {
      throw new CMSException();
    }
    return null;
  }

  public static String patientClose(LoggedInInfo loggedInInfo, int demographicNo) throws Exception {
    DemographicDao demographicDao = SpringUtils.getBean(DemographicDao.class);
    Demographic demographic = demographicDao.getDemographicById(demographicNo);
    FhirResources fhirResources = new FhirResources();
    OmdGateway omdGateway = new OmdGateway();
    OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
    if (oneIdGatewayData.getHubTopic() == null) {
      createHubTopic(loggedInInfo);
    }
    WebClient createHubTopic = omdGateway.getWebClientWholeURL(loggedInInfo,
        oneIdGatewayData.getCmsUrl());
    String uuid = UUID.randomUUID().toString();
    Event event = new Event(uuid, oneIdGatewayData.getHubTopic(), "Patient-close");
    event.addContext("patient",
        fhirResources.getString(fhirResources.getPatient(demographic)));
    Response hubTopicResponse = omdGateway.doPost(loggedInInfo, createHubTopic, event);
    String hubTopicResponseBody = hubTopicResponse.readEntity(String.class);
    logger.debug("patientOpen: " + hubTopicResponseBody);
    if (hubTopicResponse.getStatus() >= 200 && hubTopicResponse.getStatus() < 300) {
      oneIdGatewayData.setCmsPatientInContext(null);
    } else if (hubTopicResponse.getStatus() >= 400 && hubTopicResponseBody != null) {
      throw new CMSException(hubTopicResponseBody);
    } else {
      throw new CMSException();
    }
    return null;
  }

  @Nullable
  public static String consentTargetChange(
      LoggedInInfo loggedInInfo,
      int demographicNo,
      String param
  ) throws Exception {
    OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
    String patientInContext = oneIdGatewayData.getCmsPatientInContext();
    if (patientInContext == null) {
      patientOpen(loggedInInfo, demographicNo);
    } else if (Integer.parseInt(patientInContext) != demographicNo) {
      patientClose(loggedInInfo, Integer.parseInt(patientInContext));
      patientOpen(loggedInInfo, demographicNo);
    }
    OmdGateway omdGateway = new OmdGateway();
    FhirResources fhirResources = new FhirResources();
    WebClient createHubTopic = omdGateway.getWebClientWholeURL(loggedInInfo,
        oneIdGatewayData.getCmsUrl());
    String uuid = UUID.randomUUID().toString();
    Event event = new Event(uuid, oneIdGatewayData.getHubTopic(), "OH.consentTargetChange");

    event.addContext("parameters", fhirResources.getString(
        fhirResources.getConsentTargetParameter(UUID.randomUUID().toString(),
            param)));
    Response hubTopicResponse = omdGateway.doPost(loggedInInfo, createHubTopic, event);
    String hubTopicResponseBody = hubTopicResponse.readEntity(String.class);
    logger.debug("userLoginResponse: " + hubTopicResponseBody);
    return null;
  }

  @Nullable
  public static String legacyLaunch(
      LoggedInInfo loggedInInfo,
      int demographicNo,
      String param
  )
      throws Exception {
    OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
    String patientInContext = oneIdGatewayData.getCmsPatientInContext();
    logger.debug("is legacy for same patient ? current patientInContext " + patientInContext
        + " request demogrpahic " + demographicNo);
    if (patientInContext == null) {
      patientOpen(loggedInInfo, demographicNo);
    } else if (Integer.parseInt(patientInContext)
        != demographicNo) {
      patientClose(loggedInInfo, Integer.parseInt(patientInContext));
      patientOpen(loggedInInfo, demographicNo);
    }
    OmdGateway omdGateway = new OmdGateway();
    FhirResources fhirResources = new FhirResources();
    WebClient createHubTopic = omdGateway.getWebClientWholeURL(loggedInInfo,
        oneIdGatewayData.getCmsUrl());
    String uuid = UUID.randomUUID().toString();
    Event event = new Event(uuid, oneIdGatewayData.getHubTopic(), "OH.legacyLaunch");
    event.addContext("parameters", fhirResources.getString(
        fhirResources.getContextSessionIdParameter(UUID.randomUUID().toString(),
            param)));
    Response hubTopicResponse = omdGateway.doPost(loggedInInfo, createHubTopic, event);
    String hubTopicResponseBody = hubTopicResponse.readEntity(String.class);
    logger.debug("legacyLaunch: " + hubTopicResponseBody);
    return null;
  }



  @Nullable
  //Should check to see if the user is logged in. Also check to see if a patient is still in context
  public static String userLogout(LoggedInInfo loggedInInfo) throws Exception {
    if (loggedInInfo != null) {
      OmdGateway omdGateway = new OmdGateway();
      OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
      if (oneIdGatewayData != null && oneIdGatewayData.getHubTopic() != null) {
        //TODO:Need to check if a patient is still in context
        WebClient createHubTopic = omdGateway.getWebClientWholeURL(loggedInInfo,
            oneIdGatewayData.getCmsUrl());
        Response hubTopicResponse = omdGateway.doPost(loggedInInfo, createHubTopic,
            new Event(UUID.randomUUID().toString(), oneIdGatewayData.getHubTopic(), "userLogout"));
        String hubTopicResponseBody = hubTopicResponse.readEntity(String.class);
        logger.debug("hubTopicResponse: " + hubTopicResponseBody);
      }
    }
    return null;
  }
}
