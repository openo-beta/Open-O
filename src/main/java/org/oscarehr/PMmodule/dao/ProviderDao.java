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

package org.oscarehr.PMmodule.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.oscarehr.common.NativeSql;
import org.oscarehr.common.dao.ProviderFacilityDao;
import org.oscarehr.common.dao.UserPropertyDAO;
import org.oscarehr.common.model.Provider;
import org.oscarehr.common.model.ProviderFacility;
import org.oscarehr.common.model.ProviderFacilityPK;
import org.oscarehr.common.model.UserProperty;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.hibernate.type.StandardBasicTypes;
import oscar.OscarProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.hibernate.SessionFactory;

import com.quatro.model.security.SecProvider;

@SuppressWarnings("unchecked")
public interface ProviderDao {

    String PR_TYPE_DOCTOR = "doctor";
    String PR_TYPE_RESIDENT = "resident";

    boolean providerExists(String providerNo);

    Provider getProvider(String providerNo);

    String getProviderName(String providerNo);

    String getProviderNameLastFirst(String providerNo);

    List<Provider> getProviders();

    List<Provider> getProviders(String[] providers);

    List<Provider> getProviderFromFirstLastName(String firstname, String lastname);

    List<Provider> getProviderLikeFirstLastName(String firstname, String lastname);

    List<Provider> getActiveProviderLikeFirstLastName(String firstname, String lastname);

    List<SecProvider> getActiveProviders(Integer programId);

    List<Provider> getActiveProviders(String facilityId, String programId);

    List<Provider> getActiveProviders();

    List<Provider> getActiveProviders(boolean filterOutSystemAndImportedProviders);

    List<Provider> getActiveProvidersByRole(String role);

    List<Provider> getDoctorsWithOhip();

    List<Provider> getBillableProviders();

    List<Provider> getBillableProvidersInBC(LoggedInInfo loggedInInfo);

    List<Provider> getBillableProvidersInBC();

    List<Provider> getProviders(boolean active);

    List<Provider> getActiveProviders(String providerNo, Integer shelterId);

    List<Provider> getActiveProvider(String providerNo);

    List<Provider> search(String name);

    List<Provider> getProvidersByTypeWithNonEmptyOhipNo(String type);

    List<Provider> getProvidersByType(String type);

    List<Provider> getProvidersByTypePattern(String typePattern);

    List getShelterIds(String provider_no);

    void addProviderToFacility(String provider_no, int facilityId);

    void removeProviderFromFacility(String provider_no,
                                    int facilityId);

    List<Integer> getFacilityIds(String provider_no);

    List<String> getProviderIds(int facilityId);

    void updateProvider(Provider provider);

    void saveProvider(Provider provider);

    Provider getProviderByPractitionerNo(String practitionerNo);

    Provider getProviderByPractitionerNo(String practitionerNoType, String practitionerNo);

    Provider getProviderByPractitionerNo(String[] practitionerNoTypes, String practitionerNo);

    List<String> getUniqueTeams();

    List<Provider> getBillableProvidersOnTeam(Provider p);

    List<Provider> getBillableProvidersByOHIPNo(String ohipNo);

    List<Provider> getProvidersWithNonEmptyOhip(LoggedInInfo loggedInInfo);

    List<Provider> getProvidersWithNonEmptyOhip();

    List<Provider> getCurrentTeamProviders(String providerNo);

    List<String> getActiveTeams();

    List<String> getActiveTeamsViaSites(String providerNo);

    List<Provider> getProviderByPatientId(Integer patientId);

    List<Provider> getDoctorsWithNonEmptyCredentials();

    List<Provider> getProvidersWithNonEmptyCredentials();

    List<String> getProvidersInTeam(String teamName);

    List<Object[]> getDistinctProviders();

    List<String> getRecordsAddedAndUpdatedSinceTime(Date date);

    List<Provider> searchProviderByNamesString(String searchString, int startIndex, int itemsToReturn);

    List<Provider> search(String term, boolean active, int startIndex, int itemsToReturn);

    List<String> getProviderNosWithAppointmentsOnDate(Date appointmentDate);

    List<Provider> getOlisHicProviders();

    Provider getProviderByPractitionerNoAndOlisType(String practitionerNo, String olisIdentifierType);

    List<Provider> getOlisProvidersByPractitionerNo(List<String> practitionerNumbers);

    List<Provider> getProvidersByIds(List<String> providerNumbers);

    Map<String, String> getProviderNamesByIdsAsMap(List<String> providerNumbers);
}
