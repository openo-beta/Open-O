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

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.oscarehr.PMmodule.model.ProgramProvider;
import org.oscarehr.PMmodule.web.formbean.ClientListsReportFormBean;
import org.oscarehr.PMmodule.web.formbean.ClientSearchFormBean;
import org.oscarehr.caisi_integrator.ws.MatchingDemographicParameters;
import org.oscarehr.common.DemographicSearchResultTransformer;
import org.oscarehr.common.Gender;
import org.oscarehr.common.NativeSql;
import org.oscarehr.common.model.Admission;
import org.oscarehr.common.model.Demographic;
import org.oscarehr.common.model.DemographicExt;
import org.oscarehr.event.DemographicCreateEvent;
import org.oscarehr.event.DemographicUpdateEvent;
import org.oscarehr.integration.hl7.generators.HL7A04Generator;
import org.oscarehr.util.DbConnectionFilter;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.ws.rest.to.model.DemographicSearchRequest;
import org.oscarehr.ws.rest.to.model.DemographicSearchRequest.SEARCHMODE;
import org.oscarehr.ws.rest.to.model.DemographicSearchRequest.SORTMODE;
import org.oscarehr.ws.rest.to.model.DemographicSearchResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;

import oscar.MyDateFormat;
import oscar.OscarProperties;
import oscar.util.SqlUtils;

/**
 *
 */
public interface DemographicDao {

    List<Integer> getMergedDemographics(Integer demographicNo);

    Demographic getDemographic(String demographic_no);

    List getDemographics();

    List<Demographic> getDemographics(List<Integer> demographicIds);

    Long getActiveDemographicCount();

    List<Demographic> getActiveDemographics(final int offset, final int limit);

    Demographic getDemographicById(Integer demographic_id);

    List<Demographic> getDemographicByProvider(String providerNo);

    List<Demographic> getDemographicByProvider(String providerNo, boolean onlyActive);

    List<Integer> getDemographicNosByProvider(String providerNo, boolean onlyActive);


    List getActiveDemographicByProgram(int programId, Date dt, Date defdt);

    List<Demographic> getActiveDemosByHealthCardNo(String hcn, String hcnType);

    Set getArchiveDemographicByProgramOptimized(int programId, Date dt, Date defdt);

    List getProgramIdByDemoNo(Integer demoNo);

    void clear();

    List getDemoProgram(Integer demoNo);

    List getDemoProgramCurrent(Integer demoNo);

    List<Integer> getDemographicIdsAdmittedIntoFacility(int facilityId);

    List<Demographic> searchDemographic(String searchStr);

    List<Demographic> searchDemographicByNameString(String searchString, int startIndex, int itemsToReturn);

    List<Demographic> searchDemographicByName(String searchStr, int limit, int offset, String providerNo,
                                              boolean outOfDomain);

    List<Demographic> searchDemographicByNameAndNotStatus(String searchStr, List<String> statuses, int limit,
                                                          int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByNameAndStatus(String searchStr, List<String> statuses, int limit,
                                                       int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByName(String searchStr, int limit, int offset, String orderBy,
                                              String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByNameAndNotStatus(String searchStr, List<String> statuses, int limit,
                                                          int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByNameAndStatus(String searchStr, List<String> statuses, int limit,
                                                       int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByNameAndStatus(String searchStr, List<String> statuses, int limit,
                                                       int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses);

    List<Demographic> searchDemographicByNameAndStatus(String searchStr, List<String> statuses, int limit,
                                                       int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses,
                                                       boolean ignoreMerged);

    List<Demographic> searchMergedDemographicByName(String searchStr, int limit, int offset, String providerNo,
                                                    boolean outOfDomain);

    List<Demographic> searchDemographicByDOB(String dobStr, int limit, int offset, String providerNo,
                                             boolean outOfDomain);

    List<Demographic> searchDemographicByDOBWithMerged(String dobStr, int limit, int offset, String providerNo,
                                                       boolean outOfDomain);

    List<Demographic> getByHinAndGenderAndDobAndLastName(String hin, String gender, String dob, String lastName);

    List<Demographic> searchDemographicByDOBAndNotStatus(String dobStr, List<String> statuses, int limit,
                                                         int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByDOBAndStatus(String dobStr, List<String> statuses, int limit,
                                                      int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByDOB(String dobStr, int limit, int offset, String orderBy,
                                             String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByDOBAndNotStatus(String dobStr, List<String> statuses, int limit,
                                                         int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByDOBAndStatus(String dobStr, List<String> statuses, int limit,
                                                      int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByDOBAndStatus(String dobStr, List<String> statuses, int limit,
                                                      int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses);

    List<Demographic> searchDemographicByDOBAndStatus(String dobStr, List<String> statuses, int limit,
                                                      int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses,
                                                      boolean ignoreMerged);

    List<Demographic> searchMergedDemographicByDOB(String dobStr, int limit, int offset, String providerNo,
                                                   boolean outOfDomain);

    List<Demographic> searchDemographicByPhone(String phoneStr, int limit, int offset, String providerNo,
                                               boolean outOfDomain);

    List<Demographic> searchDemographicByPhoneAndNotStatus(String phoneStr, List<String> statuses, int limit,
                                                           int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByPhoneAndStatus(String phoneStr, List<String> statuses, int limit,
                                                        int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByPhone(String phoneStr, int limit, int offset, String orderBy,
                                               String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByPhoneAndNotStatus(String phoneStr, List<String> statuses, int limit,
                                                           int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByPhoneAndStatus(String phoneStr, List<String> statuses, int limit,
                                                        int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByPhoneAndStatus(String phoneStr, List<String> statuses, int limit,
                                                        int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses);

    List<Demographic> searchDemographicByPhoneAndStatus(String phoneStr, List<String> statuses, int limit,
                                                        int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses,
                                                        boolean ignoreMerged);

    List<Demographic> searchMergedDemographicByPhone(String phoneStr, int limit, int offset, String providerNo,
                                                     boolean outOfDomain);

    List<Demographic> searchDemographicByHIN(String hinStr);

    List<Demographic> searchDemographicByHIN(String hinStr, int limit, int offset, String providerNo,
                                             boolean outOfDomain);

    List<Demographic> searchDemographicByHINAndNotStatus(String hinStr, List<String> statuses, int limit,
                                                         int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByHINAndStatus(String hinStr, List<String> statuses, int limit,
                                                      int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByHIN(String hinStr, int limit, int offset, String orderBy,
                                             String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByHINAndNotStatus(String hinStr, List<String> statuses, int limit,
                                                         int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByHINAndStatus(String hinStr, List<String> statuses, int limit,
                                                      int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByHINAndStatus(String hinStr, List<String> statuses, int limit,
                                                      int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses);

    List<Demographic> searchDemographicByHINAndStatus(String hinStr, List<String> statuses, int limit,
                                                      int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses,
                                                      boolean ignoreMerged);

    List<Demographic> findByAttributes(
        String hin,
        String firstName,
        String lastName,
        Gender gender,
        Calendar dateOfBirth,
        String city,
        String province,
        String phone,
        String email,
        String alias,
        int startIndex,
        int itemsToReturn);

    List<Demographic> findByAttributes(
        String hin,
        String firstName,
        String lastName,
        Gender gender,
        Calendar dateOfBirth,
        String city,
        String province,
        String phone,
        String email,
        String alias,
        int startIndex,
        int itemsToReturn,
        boolean orderByName);

    List<Demographic> searchMergedDemographicByHIN(String hinStr, int limit, int offset, String providerNo,
                                                   boolean outOfDomain);

    List<Demographic> searchDemographicByAddress(String addressStr, int limit, int offset, String providerNo,
                                                 boolean outOfDomain);

    List<Demographic> searchDemographicByAddressAndStatus(String addressStr, List<String> statuses, int limit,
                                                          int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByAddressAndNotStatus(String addressStr, List<String> statuses, int limit,
                                                             int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByAddress(String addressStr, int limit, int offset, String orderBy,
                                                 String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByAddressAndStatus(String addressStr, List<String> statuses, int limit,
                                                          int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByAddressAndNotStatus(String addressStr, List<String> statuses, int limit,
                                                             int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByAddressAndStatus(String addressStr, List<String> statuses, int limit,
                                                          int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses);

    List<Demographic> searchDemographicByAddressAndStatus(String addressStr, List<String> statuses, int limit,
                                                          int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses,
                                                          boolean ignoreMerged);

    List<Demographic> searchDemographicByExtKeyAndValueLike(DemographicExt.DemographicProperty key, String value,
                                                            int limit, int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByExtKeyAndValueLikeAndNotStatus(DemographicExt.DemographicProperty key,
                                                                        String value, List<String> statuses, int limit, int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByExtKeyAndValueLikeAndStatus(DemographicExt.DemographicProperty key,
                                                                     String value, List<String> statuses, int limit, int offset, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByExtKeyAndValueLike(DemographicExt.DemographicProperty key, String value,
                                                            int limit, int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByExtKeyAndValueLikeWithMerged(DemographicExt.DemographicProperty key,
                                                                      String value, int limit, int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> searchDemographicByExtKeyAndValueLikeAndNotStatus(DemographicExt.DemographicProperty key,
                                                                        String value, List<String> statuses, int limit, int offset, String orderBy, String providerNo,
                                                                        boolean outOfDomain);

    List<Demographic> searchDemographicByExtKeyAndValueLikeAndStatus(DemographicExt.DemographicProperty key,
                                                                     String value, List<String> statuses, int limit, int offset, String orderBy, String providerNo,
                                                                     boolean outOfDomain);

    List<Demographic> searchDemographicByExtKeyAndValueLikeAndStatus(DemographicExt.DemographicProperty key,
                                                                     String value, List<String> statuses, int limit, int offset, String orderBy, String providerNo,
                                                                     boolean outOfDomain, boolean ignoreStatuses);

    List<Demographic> searchDemographicByExtKeyAndValueLikeAndStatus(DemographicExt.DemographicProperty key,
                                                                     String value, List<String> statuses, int limit, int offset, String orderBy, String providerNo,
                                                                     boolean outOfDomain, boolean ignoreStatuses, boolean ignoreMerged);

    List<Demographic> searchMergedDemographicByAddress(String addressStr, int limit, int offset,
                                                       String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByChartNo(String chartNoStr, int limit, int offset, String providerNo,
                                               boolean outOfDomain);

    List<Demographic> findDemographicByChartNoAndStatus(String chartNoStr, List<String> statuses, int limit,
                                                        int offset, String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByChartNoAndNotStatus(String chartNoStr, List<String> statuses, int limit,
                                                           int offset, String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByChartNo(String chartNoStr, int limit, int offset, String orderBy,
                                               String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByChartNoAndStatus(String chartNoStr, List<String> statuses, int limit,
                                                        int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByChartNoAndNotStatus(String chartNoStr, List<String> statuses, int limit,
                                                           int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByChartNoAndStatus(String chartNoStr, List<String> statuses, int limit,
                                                        int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses);

    List<Demographic> findDemographicByDemographicNo(String demographicNoStr, int limit, int offset,
                                                     String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByDemographicNoAndStatus(String demographicNoStr, List<String> statuses,
                                                              int limit, int offset, String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByDemographicNoAndNotStatus(String demographicNoStr, List<String> statuses,
                                                                 int limit, int offset, String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByDemographicNo(String demographicNoStr, int limit, int offset,
                                                     String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByDemographicNoAndStatus(String demographicNoStr, List<String> statuses,
                                                              int limit, int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByDemographicNoAndNotStatus(String demographicNoStr, List<String> statuses,
                                                                 int limit, int offset, String orderBy, String providerNo, boolean outOfDomain);

    List<Demographic> findDemographicByDemographicNoAndStatus(String demographicNoStr, List<String> statuses,
                                                              int limit, int offset, String orderBy, String providerNo, boolean outOfDomain, boolean ignoreStatuses);

    void save(Demographic demographic);

    String getOrderField(String orderBy, boolean nativeQuery);

    String getOrderField(String orderBy);

    List<Integer> getDemographicIdsAlteredSinceTime(Date value);

    List<Integer> getDemographicIdsOpenedChartSinceTime(String value);

    List<String> getRosterStatuses();

    List<String> getAllRosterStatuses();

    List<String> getAllPatientStatuses();

    List<String> search_ptstatus();

    List<String> getAllProviderNumbers();

    boolean clientExists(Integer demographicNo);

    boolean clientExistsThenEvict(Integer demographicNo);

    Demographic getClientByDemographicNo(Integer demographicNo);

    List<Demographic> getClients();

    List<Demographic> search(ClientSearchFormBean bean, boolean returnOptinsOnly, boolean excludeMerged);

    List<Demographic> search(ClientSearchFormBean bean);

    void saveClient(Demographic client);

    // public Map<String, ClientListsReportResults>
    // findByReportCriteria(ClientListsReportFormBean x);

    List<Demographic> getClientsByChartNo(String chartNo);

    List<Demographic> getClientsByHealthCard(String num, String type);

    List<Demographic> searchByHealthCard(String hin, String hcType);

    List<Demographic> searchByHealthCard(String hin);

    Demographic getDemographicByNamePhoneEmail(String firstName, String lastName, String hPhone, String wPhone,
                                               String email);

    List<Demographic> getDemographicWithLastFirstDOB(String lastname, String firstname, String year_of_birth,
                                                     String month_of_birth, String date_of_birth);

    List<Demographic> getDemographicWithLastFirstDOBExact(String lastname, String firstname,
                                                          String year_of_birth, String month_of_birth, String date_of_birth);

    List<Demographic> getDemographicsByHealthNum(String hin);

    List<Integer> getActiveDemographicIds();

    List<Integer> getDemographicIds();

    List<Demographic> getDemographicWithGreaterThanYearOfBirth(int yearOfBirth);

    List<Demographic> search_catchment(String rosterStatus, int offset, int limit);

    List<Demographic> findByField(String fieldName, Object fieldValue, String orderBy, int offset);

    // public List<Demographic> findByCriterion(DemographicCriterion c);

    List<Object[]> findDemographicsForFluReport(String providerNo);

    List<Integer> getActiveDemographicIdsOlderThan(int age);

    void setApplicationEventPublisher(ApplicationEventPublisher publisher);

    List<Integer> getDemographicIdsAddedSince(Date value);

    List<Demographic> getDemographicByRosterStatus(String rosterStatus, String patientStatus);

    Integer searchPatientCount(LoggedInInfo loggedInInfo, DemographicSearchRequest searchRequest);

    List<DemographicSearchResult> searchPatients(LoggedInInfo loggedInInfo,
                                                 DemographicSearchRequest searchRequest, int startIndex, int itemsToReturn);


    List<Integer> getMissingExtKey(String keyName);


    List<Demographic> getActiveDemographicAfter(Date afterDatetimeExclusive);

    List<Demographic> findByLastNameAndDob(String lastName, Calendar dateOfBirth);

    List<Demographic> findByFirstAndLastName(String name, String start, String end);

    List<Demographic> findByDob(Calendar dateOfBirth, String start, int numToReturn);

    List<Demographic> findByPhone(String phone, String start, int numToReturn);

    List<Demographic> findByHin(String hin, String start, int numToReturn);
}
