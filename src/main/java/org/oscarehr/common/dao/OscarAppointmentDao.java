//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * <p>
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
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package org.oscarehr.common.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Query;

import org.oscarehr.PMmodule.model.Program;
import org.oscarehr.common.NativeSql;
import org.oscarehr.common.model.Appointment;
import org.oscarehr.common.model.AppointmentArchive;
import org.oscarehr.common.model.Facility;
import org.oscarehr.util.MiscUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import org.apache.commons.lang.StringUtils;
import oscar.util.UtilDateUtilities;

public interface OscarAppointmentDao extends AbstractDao<Appointment> {

    boolean checkForConflict(Appointment appt);

    List<Appointment> getAppointmentHistory(Integer demographicNo, Integer offset, Integer limit);

    List<Appointment> getAllAppointmentHistory(Integer demographicNo, Integer offset, Integer limit);

    List<AppointmentArchive> getDeletedAppointmentHistory(Integer demographicNo, Integer offset, Integer limit);

    List<Appointment> getAppointmentHistory(Integer demographicNo);

    void archiveAppointment(int appointmentNo);

    List<Appointment> getAllByDemographicNo(Integer demographicNo);

    List<Appointment> findByUpdateDate(Date updatedAfterThisDateExclusive, int itemsToReturn);

    List<Appointment> findByDemographicIdUpdateDate(Integer demographicId, Date updatedAfterThisDateExclusive);

    List<Appointment> getAllByDemographicNoSince(Integer demographicNo, Date lastUpdateDate);

    List<Integer> getAllDemographicNoSince(Date lastUpdateDate, List<Program> programs);

    List<Appointment> findByDateRange(Date startTime, Date endTime);

    List<Appointment> findByDateRangeAndProvider(Date startTime, Date endTime, String providerNo);

    List<Appointment> getByProviderAndDay(Date date, String providerNo);

    List<Appointment> getByDemoNoAndDay(int demoNo, Date date);

    List<Appointment> findByProviderAndDayandNotStatuses(String providerNo, Date date, String[] notThisStatus);

    List<Appointment> findByProviderAndDayandNotStatus(String providerNo, Date date, String notThisStatus);

    List<Appointment> findByProviderDayAndStatus(String providerNo, Date date, String status);

    List<Appointment> findByDayAndStatus(Date date, String status);

    List<Appointment> find(Date date, String providerNo, Date startTime, Date endTime, String name,
                           String notes, String reason, Date createDateTime, String creator, Integer demographicNo);

    List<Appointment> findByDemographicId(Integer demographicId, int startIndex, int itemsToReturn);

    List<Appointment> findAll();

    List<Appointment> findNonCancelledFutureAppointments(Integer demographicId);

    Appointment findNextAppointment(Integer demographicId);

    Appointment findDemoAppointmentToday(Integer demographicNo);

    List<Appointment> findByEverything(Date appointmentDate, String providerNo, Date startTime, Date endTime,
                                       String name, String notes, String reason, Date createDateTime, String creator, int demographicNo);

    List<Appointment> findByProviderAndDate(String providerNo, Date appointmentDate);

    List<Object[]> findAppointments(Date sDate, Date eDate);

    List<Object[]> findPatientAppointments(String providerNo, Date from, Date to);

    List<Appointment> search_unbill_history_daterange(String providerNo, Date startDate, Date endDate);

    List<Appointment> findByDateAndProvider(Date date, String provider_no);

    List<Appointment> search_appt(Date startTime, Date endTime, String providerNo);

    List<Appointment> search_appt(Date date, String providerNo, Date startTime1, Date startTime2, Date endTime1,
                                  Date endTime2, Date startTime3, Date endTime3, Integer programId);

    List<Object[]> search_appt_future(Integer demographicNo, Date from, Date to);

    List<Object[]> search_appt_past(Integer demographicNo, Date from, Date to);

    Appointment search_appt_no(String providerNo, Date appointmentDate, Date startTime, Date endTime,
                               Date createDateTime, String creator, Integer demographicNo);

    List<Object[]> search_appt_data1(String providerNo, Date appointmentDate, Date startTime, Date endTime,
                                     Date createDateTime, String creator, Integer demographicNo);

    List<Object[]> export_appt(Integer demographicNo);

    List<Appointment> search_otherappt(Date appointmentDate, Date startTime1, Date endTime1, Date startTime2,
                                       Date startTime3);

    List<Appointment> search_group_day_appt(String myGroup, Integer demographicNo, Date appointmentDate);

    Appointment findByDate(Date appointmentDate);

    List<Object[]> findAppointmentAndProviderByAppointmentNo(Integer apptNo);

    List<Appointment> searchappointmentday(String providerNo, Date appointmentDate, Integer programId);

    List<Appointment> searchAppointmentDaySite(String providerNo, Date appointmentDate, Integer programId,
                                               String selectedSiteId);

    List<Object[]> findAppointmentsByDemographicIds(Set<String> demoIds, Date from, Date to);

    List<Appointment> findPatientBilledAppointmentsByProviderAndAppointmentDate(
        String providerNo,
        Date startAppointmentDate,
        Date endAppointmentDate);

    List<Appointment> findPatientUnbilledAppointmentsByProviderAndAppointmentDate(
        String providerNo,
        Date startAppointmentDate,
        Date endAppointmentDate);

    List<Appointment> findByProgramProviderDemographicDate(Integer programId, String providerNo,
                                                           Integer demographicId, Date updatedAfterThisDateExclusive, int itemsToReturn);

    List<Integer> findAllDemographicIdByProgramProvider(Integer programId, String providerNo);

    List<Appointment> findDemoAppointmentsToday(Integer demographicNo);

    List<Appointment> findDemoAppointmentsOnDate(Integer demographicNo, Date date);

    int findProvideAppointmentTodayNum(String provide, String appdate);

    int updateApptStatus(String ids, String status);

    List<Object[]> listAppointmentsByPeriodProvider(Date sDate, Date eDate, List<Integer> providerNos);

    List<Object[]> listProviderAppointmentCounts(Date sDate, Date eDate);

}
