//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
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
 * <p>
 * Modifications made by Magenta Health in 2024.
 */

package org.oscarehr.managers;

import org.apache.logging.log4j.Logger;
import org.oscarehr.PMmodule.caisi_integrator.CaisiIntegratorManager;
import org.oscarehr.PMmodule.model.ProgramProvider;
import org.oscarehr.caisi_integrator.ws.DemographicTransfer;
import org.oscarehr.caisi_integrator.ws.DemographicWs;
import org.oscarehr.caisi_integrator.ws.GetConsentTransfer;
import org.oscarehr.common.Gender;
import org.oscarehr.common.dao.*;
import org.oscarehr.common.exception.PatientDirectiveException;
import org.oscarehr.common.model.*;
import org.oscarehr.common.model.Demographic.PatientStatus;
import org.oscarehr.common.model.enumerator.DemographicExtKey;
import org.oscarehr.util.DemographicContactCreator;
import org.oscarehr.util.LoggedInInfo;
import org.oscarehr.util.MiscUtils;
import org.oscarehr.util.SpringUtils;
import org.oscarehr.ws.rest.to.model.DemographicSearchRequest;
import org.oscarehr.ws.rest.to.model.DemographicSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oscar.log.LogAction;
import oscar.util.StringUtils;

import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Will provide access to demographic data, as well as closely related data such as
 * extensions (DemographicExt), merge data, archive data, etc.
 * <p>
 * Future Use: Add privacy, security, and consent profiles
 */

public interface DemographicManager {

    Demographic getDemographic(LoggedInInfo loggedInInfo, Integer demographicId)
            throws PatientDirectiveException;

    Demographic getDemographic(LoggedInInfo loggedInInfo, String demographicNo);

    Demographic getDemographicWithExt(LoggedInInfo loggedInInfo, Integer demographicId);

    String getDemographicFormattedName(LoggedInInfo loggedInInfo, Integer demographicId);

    String getDemographicEmail(LoggedInInfo loggedInInfo, Integer demographicId);


    List<Demographic> searchDemographicByName(LoggedInInfo loggedInInfo, String searchString, int startIndex,
                                              int itemsToReturn);

    List<Demographic> getActiveDemographicAfter(LoggedInInfo loggedInInfo, Date afterDateExclusive);

    List<DemographicExt> getDemographicExts(LoggedInInfo loggedInInfo, Integer id);

    DemographicExt getDemographicExt(LoggedInInfo loggedInInfo, Integer demographicNo,
                                     DemographicExt.DemographicProperty key);

    DemographicExt getDemographicExt(LoggedInInfo loggedInInfo, Integer demographicNo, String key);

    DemographicCust getDemographicCust(LoggedInInfo loggedInInfo, Integer id);

    void createUpdateDemographicCust(LoggedInInfo loggedInInfo, DemographicCust demoCust);

    List<DemographicContact> getDemographicContacts(LoggedInInfo loggedInInfo, Integer id, String category);

    List<DemographicContact> getDemographicContacts(LoggedInInfo loggedInInfo, Integer id);

    List<Provider> getDemographicMostResponsibleProviders(LoggedInInfo loggedInInfo, int demographicNo);

    List<Demographic> getDemographicsNameRangeByProvider(LoggedInInfo loggedInInfo, Provider provider,
                                                         String regex);

    List<Demographic> getDemographicsByProvider(LoggedInInfo loggedInInfo, Provider provider);

    void createDemographic(LoggedInInfo loggedInInfo, Demographic demographic, Integer admissionProgramId);

    void updateDemographic(LoggedInInfo loggedInInfo, Demographic demographic);

    Demographic findExactMatchToDemographic(LoggedInInfo loggedInInfo, Demographic demographic);

    List<Demographic> findFuzzyMatchToDemographic(LoggedInInfo loggedInInfo, Demographic demographic);

    void addDemographic(LoggedInInfo loggedInInfo, Demographic demographic);

    void createExtension(LoggedInInfo loggedInInfo, DemographicExt ext);

    void updateExtension(LoggedInInfo loggedInInfo, DemographicExt ext);

    void archiveExtension(DemographicExt ext);

    void createUpdateDemographicContact(LoggedInInfo loggedInInfo, DemographicContact demoContact);

    void deleteDemographic(LoggedInInfo loggedInInfo, Demographic demographic);

    void deleteExtension(LoggedInInfo loggedInInfo, DemographicExt ext);

    void mergeDemographics(LoggedInInfo loggedInInfo, Integer parentId, List<Integer> children);

    void unmergeDemographics(LoggedInInfo loggedInInfo, Integer parentId, List<Integer> children);

    Long getActiveDemographicCount(LoggedInInfo loggedInInfo);

    List<Demographic> getActiveDemographics(LoggedInInfo loggedInInfo, int offset, int limit);

    List<DemographicMerged> getMergedDemographics(LoggedInInfo loggedInInfo, Integer parentId);


    String getDemographicWorkPhoneAndExtension(LoggedInInfo loggedInInfo, Integer demographicNo);

    List<Demographic> searchDemographicsByAttributes(LoggedInInfo loggedInInfo, String hin, String firstName,
                                                     String lastName, Gender gender, Calendar dateOfBirth, String city, String province, String phone,
                                                     String email, String alias, int startIndex, int itemsToReturn);

    List<String> getPatientStatusList();

    List<String> getRosterStatusList();

    List<DemographicSearchResult> searchPatients(LoggedInInfo loggedInInfo,
                                                 DemographicSearchRequest searchRequest, int startIndex, int itemsToReturn);

    int searchPatientsCount(LoggedInInfo loggedInInfo, DemographicSearchRequest searchRequest);

    List<Integer> getAdmittedDemographicIdsByProgramAndProvider(LoggedInInfo loggedInInfo, Integer programId,
                                                                String providerNo);


    List<Demographic> getDemographics(LoggedInInfo loggedInInfo, List<Integer> demographicIds);

    List<Demographic> searchDemographic(LoggedInInfo loggedInInfo, String searchStr);

    List<Demographic> getActiveDemosByHealthCardNo(LoggedInInfo loggedInInfo, String hcn, String hcnType);

    List<Integer> getMergedDemographicIds(LoggedInInfo loggedInInfo, Integer demographicNo);

    List<Demographic> getDemosByChartNo(LoggedInInfo loggedInInfo, String chartNo);

    List<Demographic> searchByHealthCard(LoggedInInfo loggedInInfo, String hin);

    Demographic getDemographicByNamePhoneEmail(LoggedInInfo loggedInInfo, String firstName, String lastName,
                                               String hPhone, String wPhone, String email);

    List<Demographic> getDemographicWithLastFirstDOB(LoggedInInfo loggedInInfo, String lastname,
                                                     String firstname, String year_of_birth, String month_of_birth, String date_of_birth);

    List<Integer> getDemographicNumbersByMidwifeNumberAndDemographicLastNameRegex(
        LoggedInInfo loggedInInfo,
        final String midwifeNumber,
        final String lastNameRegex);

    List<Integer> getDemographicNumbersByNurseNumberAndDemographicLastNameRegex(
        LoggedInInfo loggedInInfo,
        final String nurseNumber,
        final String lastNameRegex);

    List<Integer> getDemographicNumbersByResidentNumberAndDemographicLastNameRegex(
        LoggedInInfo loggedInInfo,
        final String residentNumber,
        final String lastNameRegex);

    List<DemographicExt> getMultipleMidwifeForDemographicNumbersByProviderNumber(
        LoggedInInfo loggedInInfo,
        final Collection<Integer> demographicNumbers,
        final String midwifeNumber);

    List<DemographicExt> getMultipleNurseForDemographicNumbersByProviderNumber(
        LoggedInInfo loggedInInfo,
        final Collection<Integer> demographicNumbers,
        final String nurseNumber);

    List<DemographicExt> getMultipleResidentForDemographicNumbersByProviderNumber(
        LoggedInInfo loggedInInfo,
        final Collection<Integer> demographicNumbers,
        final String residentNumber);

    Demographic getRemoteDemographic(LoggedInInfo loggedInInfo, int remoteFacilityId, int remoteDemographicId);

    Demographic copyRemoteDemographic(LoggedInInfo loggedInInfo, Demographic remoteDemographic,
                                      int remoteFacilityId, int remoteDemographicId);

    void updatePatientConsent(LoggedInInfo loggedInInfo, int demographic_no, String consentType,
                              boolean consented);

    List<DemographicContact> findSDMByDemographicNo(LoggedInInfo loggedInInfo, int demographicNo);

    boolean isPatientConsented(LoggedInInfo loggedInInfo, int demographic_no, String consentType);

    boolean linkDemographicToRemoteDemographic(LoggedInInfo loggedInInfo, int demographicNo,
                                               int remoteFacilityId, int remoteDemographicNo);

    List<Integer> getLinkedDemographicIds(LoggedInInfo loggedInInfo, int demographicNo, int sourceFacilityId);

    List<DemographicTransfer> getLinkedDemographics(LoggedInInfo loggedInInfo, int demographicNo);

    void checkPrivilege(LoggedInInfo loggedInInfo, String privilege);

    void checkPrivilege(LoggedInInfo loggedInInfo, String privilege, int demographicNo);

    List<DemographicContact> getHealthCareTeam(LoggedInInfo loggedInInfo, Integer demographicNo);

    List<Object[]> getArchiveMeta(LoggedInInfo loggedInInfo, Integer demographicNo);

    DemographicContact getMostResponsibleProviderFromHealthCareTeam(LoggedInInfo loggedInInfo,
                                                                    Integer demographicNo);

    DemographicContact getHealthCareMemberbyRole(LoggedInInfo loggedInInfo, Integer demographicNo, String role);

    DemographicContact getHealthCareMemberbyId(LoggedInInfo loggedInInfo, Integer demographicContactId);

    List<DemographicContact> getPersonalEmergencyContacts(LoggedInInfo loggedInInfo, Integer demographicNo);

    DemographicContact getPersonalEmergencyContactById(LoggedInInfo loggedInInfo, Integer demographicContactId);

    List<DemographicContact> getEmergencyContacts(LoggedInInfo loggedInInfo, Integer demographicNo);

    Provider getMRP(LoggedInInfo loggedInInfo, Integer demographicNo);

    Provider getMRP(LoggedInInfo loggedInInfo, Demographic demographic);

    String getNextAppointmentDate(LoggedInInfo loggedInInfo, Integer demographicNo);

    String getNextAppointmentDate(LoggedInInfo loggedInInfo, Demographic demographic);
}
