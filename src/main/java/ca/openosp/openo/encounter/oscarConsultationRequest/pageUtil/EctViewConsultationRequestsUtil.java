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


package ca.openosp.openo.encounter.oscarConsultationRequest.pageUtil;

import ca.openosp.openo.commn.model.*;
import org.apache.commons.lang.time.DateFormatUtils;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.ConsultationRequestDao;
import ca.openosp.openo.commn.dao.ConsultationRequestExtDao;
import ca.openosp.openo.commn.dao.ConsultationServiceDao;
import ca.openosp.openo.commn.model.enumerator.ConsultationRequestExtKey;
import ca.openosp.openo.managers.ConsultationManager;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class EctViewConsultationRequestsUtil {

   public boolean estConsultationVecByTeam(LoggedInInfo loggedInInfo,String team) {
      return estConsultationVecByTeam(loggedInInfo,team,false,null,null);
   }
   public boolean estConsultationVecByTeam(LoggedInInfo loggedInInfo,String team,boolean showCompleted) {
      return estConsultationVecByTeam(loggedInInfo,team,showCompleted,null,null);
   }
   public boolean estConsultationVecByTeam(LoggedInInfo loggedInInfo, String team,boolean showCompleted,Date startDate, Date endDate) {
      return estConsultationVecByTeam(loggedInInfo,team,showCompleted,null,null,null);
   }
   public boolean estConsultationVecByTeam(LoggedInInfo loggedInInfo, String team,boolean showCompleted,Date startDate, Date endDate,String orderby) {
      return estConsultationVecByTeam(loggedInInfo,team,showCompleted,null,null,null,null);
   }
   public boolean estConsultationVecByTeam(LoggedInInfo loggedInInfo, String team,boolean showCompleted,Date startDate, Date endDate,String orderby,String desc) {
      return estConsultationVecByTeam(loggedInInfo,team,showCompleted,null,null,null,null,null,null,null);
   }

   public boolean estConsultationVecByTeam(LoggedInInfo loggedInInfo, String team,boolean showCompleted,Date startDate, Date endDate,String orderby,String desc,String searchDate, Integer offset, Integer limit) {
      ids = new Vector<String>();
      status = new Vector<String>();
      patient = new Vector<String>();
      provider = new Vector<String>();
      providerNo = new Vector();
      teams = new Vector<String>();
      service = new Vector<String>();
      vSpecialist = new Vector<String>();
      urgency = new Vector<String>();
      date = new Vector<String>();
      demographicNo = new Vector<String>();
      siteName = new Vector<String>();
      this.patientWillBook = new Vector<String>();
      apptDate = new Vector<String>();
      followUpDate = new Vector<String>();
      boolean verdict = true;
      consultProvider = new Vector();
      eReferral = Collections.synchronizedList(new ArrayList<>());

      try {
          ConsultationRequestDao consultReqDao = (ConsultationRequestDao) SpringUtils.getBean(ConsultationRequestDao.class);
          ConsultationRequestExtDao consultationRequestExtDao = SpringUtils.getBean(ConsultationRequestExtDao.class);
          DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
          ConsultationManager consultationManager = SpringUtils.getBean(ConsultationManager.class);
          ProviderDao providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);
          ConsultationServiceDao serviceDao = (ConsultationServiceDao) SpringUtils.getBean(ConsultationServiceDao.class);
          ConsultationRequest consult;
          Demographic demo;
          Provider prov;
          ProfessionalSpecialist specialist;
          ConsultationServices services;
          Calendar cal = Calendar.getInstance();
          Date date1, date2;
          String providerId, providerName, specialistName;
          List consultList = consultReqDao.getConsults(team, showCompleted, startDate, endDate, orderby, desc, searchDate, offset, limit);

          for( int idx = 0; idx < consultList.size(); ++idx ) {
              consult = (ConsultationRequest)consultList.get(idx);
              demo = demographicManager.getDemographic(loggedInInfo, consult.getDemographicId());

              List<ConsultationRequestExt> extras = consultationRequestExtDao.getConsultationRequestExts(consult.getId());
              Map<String, String> extraMap = consultationManager.getExtValuesAsMap(extras);

              String serviceDescription = "";
              // If service id is 0, check the extensions table
              if (consult.getServiceId() == 0) {
                 serviceDescription = extraMap.getOrDefault(ConsultationRequestExtKey.EREFERRAL_SERVICE.getKey(), "");
              } else {
                 services = serviceDao.find(consult.getServiceId());
                 if (services != null) {
                    serviceDescription = services.getServiceDesc();
                 }
              }

                providerId = demo.getProviderNo();
                if (providerId != null && !providerId.equals("")) {
                    prov = providerDao.getProvider(demo.getProviderNo());
                    providerName = prov.getFormattedName();
                    providerNo.add(prov.getProviderNo());
                } else {
                    providerName = "N/A";
                    providerNo.add("-1");
                }

                if (consult.getProfessionalSpecialist() == null) {
                    specialistName = "N/A";
                  if (consult.getServiceId() == 0) {
                     specialistName = extraMap.getOrDefault(ConsultationRequestExtKey.EREFERRAL_DOCTOR.getKey(), "N/A");
                  }
              }
              else {
                    specialist = consult.getProfessionalSpecialist();
                    specialistName = specialist.getLastName() + ", " + specialist.getFirstName();
                }

              boolean isEReferral = extraMap.containsKey(ConsultationRequestExtKey.EREFERRAL_REF.getKey());

              demographicNo.add(consult.getDemographicId().toString());
              date.add(DateFormatUtils.ISO_DATE_FORMAT.format(consult.getReferralDate()));
              ids.add(consult.getId().toString());
              status.add(consult.getStatus());
              patient.add(demo.getFormattedName());
              provider.add(providerName);
              service.add(serviceDescription);
              vSpecialist.add(specialistName);
              urgency.add(consult.getUrgency());
              siteName.add(consult.getSiteName());
              teams.add(consult.getSendTo());
              eReferral.add(isEReferral);

              date1 = consult.getAppointmentDate();
              date2 = consult.getAppointmentTime();

              String apptDateStr = "";
              if( date1 == null ) {
            	  apptDateStr = "N/A";
              } else if( date1 != null && date2 == null ) {
            	  apptDateStr = DateFormatUtils.ISO_DATE_FORMAT.format(date1) + " T00:00:00";
              } else {
            	  apptDateStr = DateFormatUtils.ISO_DATE_FORMAT.format(date1) + " " +  DateFormatUtils.ISO_TIME_FORMAT.format(date2);
              }

              apptDate.add(apptDateStr);
              patientWillBook.add(""+consult.isPatientWillBook());

              date1 = consult.getFollowUpDate();
              if( date1 == null ) {
                  followUpDate.add("N/A");
              }
              else {
                followUpDate.add(DateFormatUtils.ISO_DATE_FORMAT.format(date1));
              }

              Provider cProv = providerDao.getProvider(consult.getProviderNo());
              consultProvider.add(cProv);
          }
      } catch(Exception e) {
         MiscUtils.getLogger().error("Error", e);
         verdict = false;
      }
      return verdict;
   }


   public boolean estConsultationVecByDemographic(LoggedInInfo loggedInInfo, String demoNo) {
      ids = new Vector<String>();
      status = new Vector<String>();
      patient = new Vector<String>();
      provider = new Vector<String>();
      service = new Vector<String>();
      date = new Vector<String>();
      this.patientWillBook = new Vector<String>();
      urgency = new Vector<String>();
      apptDate = new Vector<String>();
      consultProvider = new Vector();

      boolean verdict = true;
      try {

          ConsultationRequestDao consultReqDao = (ConsultationRequestDao) SpringUtils.getBean(ConsultationRequestDao.class);
          ConsultationRequestExtDao consultationRequestExtDao = SpringUtils.getBean(ConsultationRequestExtDao.class);
          ProviderDao providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);
          DemographicManager demoManager = SpringUtils.getBean(DemographicManager.class);
          ConsultationServiceDao serviceDao = (ConsultationServiceDao) SpringUtils.getBean(ConsultationServiceDao.class);

          List <ConsultationRequest> consultList = consultReqDao.getConsults(Integer.parseInt(demoNo));
          for( ConsultationRequest consult : consultList ) {
              String serviceDescription = "unknown";
              // If service id is 0, check the extensions table
              if (consult.getServiceId() == 0) {
                 serviceDescription = consultationRequestExtDao.getConsultationRequestExtsByKey(consult.getId(), ConsultationRequestExtKey.EREFERRAL_SERVICE.getKey());
              } else {
                 ConsultationServices services = serviceDao.find(consult.getServiceId());
                 if (services != null) {
                    serviceDescription = services.getServiceDesc();
                 }
              }

                Demographic demo = demoManager.getDemographic(loggedInInfo, consult.getDemographicId());
                String providerId = demo.getProviderNo();
                String providerName = (providerId != null && !providerId.isEmpty()) ? providerDao.getProvider(providerId).getFormattedName() : "N/A";

              ids.add(consult.getId().toString());
              status.add(consult.getStatus());
              patient.add(demo.getFormattedName());
              provider.add(providerName);
              service.add(serviceDescription);
              urgency.add(consult.getUrgency());
              patientWillBook.add(""+consult.isPatientWillBook());
              date.add(DateFormatUtils.ISO_DATE_FORMAT.format(consult.getReferralDate()));

              Provider cProv = providerDao.getProvider(consult.getProviderNo());
              consultProvider.add(cProv);
          }
      } catch(Exception e) {
         MiscUtils.getLogger().error("Error", e);
         verdict = false;
      }
      return verdict;
   }


   public Vector<String> ids;
   public Vector<String> status;
   public Vector<String> patient;
   public Vector<String> teams;
   public Vector<String> provider;
   public Vector<String> service;
   public Vector<String> vSpecialist;
   public Vector<String> date;
   public Vector<String> demographicNo;
   public Vector<String> apptDate;
   public Vector<String> patientWillBook;
   public Vector<String> urgency;
   public Vector<String> followUpDate;
   public Vector<String> providerNo;
   public Vector<String> siteName;
   public List<Boolean> eReferral;
   public Vector consultProvider;
}
