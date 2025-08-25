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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Query;

import org.oscarehr.common.NativeSql;
import org.oscarehr.common.model.Measurement;
import org.oscarehr.common.model.MeasurementType;
import org.springframework.stereotype.Repository;
import org.oscarehr.common.dao.MeasurementDaoImpl.SearchCriteria;

public interface MeasurementDao extends AbstractDao<Measurement> {

    List<Measurement> findByDemographicIdUpdatedAfterDate(Integer demographicId, Date updatedAfterThisDate);

    List<Measurement> findByCreateDate(Date updatedAfterThisDateExclusive, int itemsToReturn);

    List<Integer> findDemographicIdsUpdatedAfterDate(Date updatedAfterThisDate);

    List<Measurement> findMatching(Measurement measurement);

    List<Measurement> findByDemographicNo(Integer demographicNo);

    List<Measurement> findByDemographicNoAndType(Integer demographicNo, String type);

    Measurement findLatestByDemographicNoAndType(int demographicNo, String type);

    List<Measurement> findByAppointmentNo(Integer appointmentNo);

    List<Measurement> findByAppointmentNo2(Integer appointmentNo);

    List<Measurement> findByAppointmentNoAndType(Integer appointmentNo, String type);

    Measurement findLatestByAppointmentNoAndType(int appointmentNo, String type);

    List<Measurement> findByDemographicIdObservedDate(Integer demographicId, Date startDate, Date endDate);

    List<Measurement> findByDemographicId(Integer demographicId);

    List<Measurement> find(SearchCriteria criteria);

    List<Measurement> findByIdTypeAndInstruction(Integer demographicId, String type, String instructions);

    HashMap<String, Measurement> getMeasurements(Integer demographicNo, String[] types);

    Set<Integer> getAppointmentNosByDemographicNoAndType(int demographicNo, String type, Date startDate,
                                                         Date endDate);

    HashMap<String, Measurement> getMeasurementsPriorToDate(Integer demographicNo, Date d);

    List<Date> getDatesForMeasurements(Integer demographicNo, String[] types);

    List<Object[]> findMeasurementsByDemographicIdAndLocationCode(Integer demoNo, String loincCode);

    List<Object[]> findMeasurementsWithIdentifiersByDemographicIdAndLocationCode(Integer demoNo,
                                                                                 String loincCode);

    List<Object> findLabNumbers(Integer demoNo, String identCode);

    List<Object> findLabNumbersOrderByObserved(Integer demoNo, String identCode);

    Measurement findLastEntered(Integer demo, String type);

    List<MeasurementType> findMeasurementsTypes(Integer demoNo);

    List<Object[]> findMeasurementsAndProviders(Integer measurementId);

    List<Object[]> findMeasurementsAndProvidersByType(String type, Integer demographicNo);

    Object[] findMeasurementsAndProvidersByDemoAndType(Integer demographicNo, String type);

    List<Measurement> findByValue(String key, String value);

    List<Object> findObservationDatesByDemographicNoTypeAndMeasuringInstruction(Integer demo, String type,
                                                                                String mInstrc);

    List<Date> findByDemographicNoTypeAndMeasuringInstruction(Integer demo, String type, String mInstrc);

    Measurement findByDemographicNoTypeAndDate(Integer demo, String type, java.util.Date date);

    List<Measurement> findByDemoNoTypeDateAndMeasuringInstruction(Integer demoNo, Date from, Date to,
                                                                  String type, String instruction);

    List<Object[]> findLastEntered(Date from, Date to, String measurementType, String mInstrc);

    List<Measurement> findByDemographicNoTypeAndDate(Integer demographicNo, Date createDate,
                                                     String measurementType, String mInstrc);

    List<Object[]> findByDemoNoDateTypeMeasuringInstrAndDataField(Integer demographicNo, Date dateEntered,
                                                                  String measurementType, String mInstrc, String upper, String lower);

    List<Object[]> findLastEntered(Date from, Date to, String measurementType);

    List<Measurement> findByDemoNoDateAndType(Integer demoNo, Date createDate, String type);

    List<Object[]> findByDemoNoDateTypeAndDataField(Integer demographicNo, Date dateEntered, String type,
                                                    String upper, String lower);

    List<Object[]> findTypesAndMeasuringInstructionByDemographicId(Integer demoNo);

    List<Object[]> findByCreateDate(Date from, Date to);

    List<Measurement> findByType(Integer demographicId, String type);

    List<Measurement> findByType(Integer demographicId, String type, Date after);

    List<Measurement> findByType(Integer demographicId, List<String> types);

    List<Measurement> findByType(Integer demographicId, List<String> types, Date after);

    List<Measurement> findByType(String type);

    List<Measurement> findByType(List<String> types);

    List<Integer> findDemographicIdsByType(List<String> types);

    List<Measurement> findByTypeBefore(Integer demographicId, String type, Date date);

    List<Measurement> findByProviderDemographicLastUpdateDate(String providerNo, Integer demographicId,
                                                              Date updatedAfterThisDateExclusive, int itemsToReturn);

    List<Measurement> findByDemographicLastUpdateAfterDate(Integer demographicId,
                                                           Date updatedAfterThisDateExclusive);

    List<Measurement> findLatestByDemographicObservedAfterDate(Integer demographicId,
                                                               Date observedAfterDateExclusive);

    List<Integer> findNewMeasurementsSinceDemoKey(String keyName);

    List<Measurement> findMeasurementByTypeAndDate(Integer demoNo, String type, Date start, Date end);
}
