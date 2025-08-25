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

package org.oscarehr.casemgmt.service;

import com.quatro.model.security.Secrole;
import com.quatro.service.security.RolesManager;
import org.oscarehr.PMmodule.dao.ProgramAccessDAO;
import org.oscarehr.PMmodule.dao.ProgramProviderDAO;
import org.oscarehr.PMmodule.dao.ProgramQueueDao;
import org.oscarehr.PMmodule.model.AccessType;
import org.oscarehr.PMmodule.model.ProgramAccess;
import org.oscarehr.PMmodule.model.ProgramProvider;
import org.oscarehr.PMmodule.service.AdmissionManager;
import org.oscarehr.PMmodule.service.ProgramManager;
import org.oscarehr.caisi_integrator.ws.CachedDemographicNote;
import org.oscarehr.casemgmt.common.EChartNoteEntry;
import org.oscarehr.casemgmt.dao.*;
import org.oscarehr.casemgmt.model.*;
import org.oscarehr.common.dao.*;
import org.oscarehr.common.model.*;
import org.oscarehr.util.LoggedInInfo;
import oscar.util.LabelValueBean;

import java.util.*;

public interface CaseManagementManager {

    enum IssueType {
        OMEDS, SOCHISTORY, MEDHISTORY, CONCERNS, REMINDERS, FAMHISTORY, RISKFACTORS
    }

    int SIGNATURE_SIGNED = 1;
    int SIGNATURE_VERIFY = 2;

    void setAppointmentArchiveDao(AppointmentArchiveDao appointmentArchiveDao);

    void setDxDao(DxDao dxDao);

    CaseManagementIssue getIssueByIssueCode(String demo, String issue_code);

    CaseManagementIssue getIssueById(String demo, String issue_id);

    void setProgramManager(ProgramManager programManager);

    void getEditors(CaseManagementNote note);

    void getEditors(Collection<CaseManagementNote> notes);

    List<Provider> getAllEditors(String demographicNo);

    UserProperty getUserProperty(String provider_no, String name);

    void saveUserProperty(UserProperty prop);

    void saveEctWin(EncounterWindow ectWin);

    EncounterWindow getEctWin(String provider);

    SecRole getSecRoleByRoleName(String roleName);

    void saveNoteExt(CaseManagementNoteExt cExt);

    void updateNoteExt(CaseManagementNoteExt cExt);

    void saveNoteLink(CaseManagementNoteLink cLink);

    void updateNoteLink(CaseManagementNoteLink cLink);

    String saveNote(CaseManagementCPP cpp, CaseManagementNote note, String cproviderNo, String userName,
                    String lastStr, String roleName);

    List getNotes(String demographic_no, UserProperty prop);

    List<CaseManagementNote> getCPP(String demoNo, long issueId, UserProperty prop);

    List<CaseManagementNote> getNotes(String demographic_no, String[] issues, UserProperty prop);

    List<CaseManagementNote> getNotes(LoggedInInfo loggedInInfo, String demographic_no, String[] issues,
                                      UserProperty prop);

    List<CaseManagementNote> getNotes(String demographic_no);

    List<CaseManagementNote> getNotes(String demographic_no, Integer maxNotes);

    List<CaseManagementNote> getNotes(String demographic_no, String[] issues);

    List<CaseManagementNote> getNotes(LoggedInInfo loggedInInfo, String demographic_no, String[] issues);

    List<CaseManagementNote> getNotes(String demographic_no, String[] issues, Integer maxNotes);

    List<CaseManagementNote> getNotesWithLimit(String demographic_no, Integer offset, Integer numToReturn);

    List<CaseManagementNote> getNotesInDateRange(String demographic_no, Date startDate, Date endDate);

    List<CaseManagementNote> getActiveNotes(String demographic_no, String[] issues);

    List<CaseManagementNote> getActiveNotes(LoggedInInfo loggedInInfo, String demographic_no, String[] issues);

    List<CaseManagementIssue> getIssues(int demographic_no);

    Issue getIssueIByCmnIssueId(int cmnIssueId);

    List<CaseManagementIssue> getIssuesByNote(int noteId);

    List<CaseManagementIssue> getIssues(int demographic_no, Boolean resolved);

    List<CaseManagementIssue> getIssues(String demographic_no, List accessRight);

    boolean inAccessRight(String right, String issueAccessType, List accessRight);

    Issue getIssue(String issue_id);

    Issue getIssueByCode(IssueType issueCode);

    Issue getIssueByCode(String issueCode);

    CaseManagementNote getNote(String note_id);

    List<CaseManagementNote> getNotesByUUID(String uuid);

    CaseManagementNote getMostRecentNote(String uuid);

    CaseManagementNoteExt getNoteExt(Long id);

    List<CaseManagementNoteExt> getExtByNote(Long noteId);

    List getExtByKeyVal(String keyVal);

    List getExtByValue(String keyVal, String value);

    List getExtBeforeDate(String keyVal, Date dateValue);

    List getExtAfterDate(String keyVal, Date dateValue);

    CaseManagementNoteLink getNoteLink(Long id);

    List getLinkByNote(Long noteId);

    CaseManagementNoteLink getLatestLinkByNote(Long noteId);

    List getLinkByTableId(Integer tableName, Long tableId);

    List<CaseManagementNoteLink> getLinkByTableId(Integer tableName, Long tableId, String otherId);

    List<CaseManagementNoteLink> getLinkByTableIdDesc(Integer tableName, Long tableId);

    List<CaseManagementNoteLink> getLinkByTableIdDesc(Integer tableName, Long tableId, String otherId);

    CaseManagementNoteLink getLatestLinkByTableId(Integer tableName, Long tableId, String otherId);

    CaseManagementNoteLink getLatestLinkByTableId(Integer tableName, Long tableId);

    Integer getTableNameByDisplay(String disp);

    CaseManagementCPP getCPP(String demographic_no);

    List<Allergy> getAllergies(String demographic_no);

    List<Drug> getPrescriptions(String demographic_no, boolean all);

    List<Drug> getCurrentPrescriptions(int demographic_no);

    List<Drug> getPrescriptions(LoggedInInfo loggedInInfo, int demographicId, boolean all);

    List<LabelValueBean> getMsgBeans(Integer demographicNo);

    void deleteIssueById(CaseManagementIssue issue);

    void saveAndUpdateCaseIssues(List issuelist);

    void saveCaseIssue(CaseManagementIssue issue);

    Issue getIssueInfo(Long l);

    List getAllIssueInfo();

    void saveCPP(CaseManagementCPP cpp, String providerNo);

    List<Issue> getIssueInfoByCode(String providerNo, String[] codes);

    List<Issue> getIssueInfoByCode(String providerNo, String code);

    Issue getIssueInfoByCode(String code);

    Issue getIssueInfoByTypeAndCode(String type, String code);

    List<Issue> getIssueInfoBySearch(String providerNo, String search, List accessRight);

    void addNewIssueToConcern(String demoNo, String issueName);

    void removeIssueFromCPP(String demoNo, CaseManagementIssue issue);

    void changeIssueInCPP(String demoNo, String origIssueDesc, String newIssueDesc);

    void updateCurrentIssueToCPP(String demoNo, List issueList);

    List<CaseManagementIssue> getFilteredNotes(String providerNo, String demographic_no);

    boolean haveIssue(Long issid, Long noteId, String demoNo);

    boolean haveIssue(Long issid, String demoNo);

    boolean greaterEqualLevel(int level, String providerNo);

    List<AccessType> getAccessRight(String providerNo, String demoNo, String programId);

    boolean roleInAccess(Long roleId, ProgramAccess pa);

    void addrt(List<AccessType> rt, AccessType at);

    boolean hasAccessRight(String accessName, String accessType, String providerNo, String demoNo, String pId);

    String getRoleName(String providerNo, String program_id);

    String getDemoName(String demoNo);

    String getDemoGender(String demoNo);

    String getDemoAge(String demoNo);

    String getDemoPhn(String demoNo);

    String getDemoDOB(String demoNo);

    String getCaisiRoleById(String id);

    List<CaseManagementNote> search(CaseManagementSearchBean searchBean);

    List<CaseManagementNote> filterNotesByAccess(List<CaseManagementNote> notes, String providerNo);

    void tmpSave(String providerNo, String demographicNo, String programId, String noteId, String note);

    void deleteTmpSave(String providerNo, String demographicNo, String programId);

    CaseManagementTmpSave getTmpSave(String providerNo, String demographicNo, String programId);

    CaseManagementTmpSave restoreTmpSave(String providerNo, String demographicNo, String programId);

    CaseManagementTmpSave restoreTmpSave(String providerNo, String demographicNo, String programId, Date date);

    List getHistory(String note_id);

    List<CaseManagementNote> getIssueHistory(String issueIds, String demoNo);

    List<CaseManagementNote> filterNotes(LoggedInInfo loggedInInfo, String providerNo,
                                         Collection<CaseManagementNote> notes, String programId);

    List<EChartNoteEntry> filterNotes1(String providerNo, Collection<EChartNoteEntry> notes, String programId);

    boolean hasRole(String providerNo, CachedDemographicNote cachedDemographicNote, String programId);

    boolean isRoleIncludedInAccess(ProgramAccess pa, Secrole role);

    Map<String, ProgramAccess> convertProgramAccessListToMap(List<ProgramAccess> paList);

    Integer searchIssuesCount(String providerNo, String programId, String search);

    List<Issue> searchIssues(String providerNo, String programId, String search);

    List<Issue> searchIssues(String providerNo, String programId, String search, int startIndex,
                             int numToReturn);

    List searchIssuesNoRolesConcerned(String providerNo, String programId, String search);

    List<CaseManagementIssue> filterIssues(LoggedInInfo loggedInInfo, String providerNo,
                                           List<CaseManagementIssue> issues, String programId);

    void updateNote(CaseManagementNote note);

    void saveNoteSimple(CaseManagementNote note);

    Long saveNoteSimpleReturnID(CaseManagementNote note);

    boolean isClientInProgramDomain(String providerNo, String demographicNo);

    List<ProgramProvider> getProgramProviders(String providerNo);

    List<Admission> getAdmission(Integer demographicNo);

    boolean isClientInProgramDomain(List<ProgramProvider> providerPrograms, List<Admission> allAdmissions);

    boolean isClientReferredInProgramDomain(String providerNo, String demographicNo);

    boolean unlockNote(int noteId, String password);

    void updateIssue(String demographicNo, Long originalIssueId, Long newIssueId);

    boolean getEnabled();

    void setEnabled(boolean enabled);

    void setHashAuditDao(HashAuditDao dao);

    void setCaseManagementNoteDAO(CaseManagementNoteDAO dao);

    void setCaseManagementNoteExtDAO(CaseManagementNoteExtDAO dao);

    void setCaseManagementNoteLinkDAO(CaseManagementNoteLinkDAO dao);

    void setCaseManagementIssueDAO(CaseManagementIssueDAO dao);

    void setProgramAccessDAO(ProgramAccessDAO programAccessDAO);

    void setIssueDAO(IssueDAO dao);

    void setCaseManagementCPPDAO(CaseManagementCPPDAO dao);

    void setProgramProviderDao(ProgramProviderDAO programProviderDao);

    void setProgramQueueDao(ProgramQueueDao programQueueDao);

    void setRolesManager(RolesManager mgr);

    void setProviderExtDao(ProviderExtDao providerExtDao);

    void setRoleProgramAccessDAO(RoleProgramAccessDAO roleProgramAccessDAO);

    void setDemographicDao(DemographicDao demographicDao);

    void setCaseManagementTmpSaveDao(CaseManagementTmpSaveDao dao);

    void setAdmissionManager(AdmissionManager mgr);

    void setUserPropertyDAO(UserPropertyDAO dao);

    void setDxresearchDAO(DxresearchDAO dao);

    void setSecRoleDao(SecRoleDao secRoleDao);

    void saveToDx(LoggedInInfo loggedInInfo, String demographicNo, String code, String codingSystem,
                  boolean association);

    void saveToDx(LoggedInInfo loggedInInfo, String demographicNo, String code);

    List<Dxresearch> getDxByDemographicNo(String demographicNo);

    String getTemplateSignature(String template, ResourceBundle rc, Map<String, String> map);

    String getSignature(String cproviderNo, String userName, String roleName, Locale locale, int type);

    void seteChartDao(EChartDao eChartDao);

    void setEncounterWindowDao(EncounterWindowDao encounterWindowDao);

    CaseManagementNote getLastSaved(String programId, String demono, String providerNo, Map unlockedNotesMap);

    CaseManagementNote makeNewNote(String providerNo, String demographicNo, String encType, String appointmentNo,
                                   Locale locale);

    void addNewNoteLink(Long noteId);

    CaseManagementNote saveCaseManagementNote(LoggedInInfo loggedInInfo, CaseManagementNote note,
                                              List<CaseManagementIssue> issuelist, CaseManagementCPP cpp, String ongoing, boolean verify, Locale locale,
                                              Date now, CaseManagementNote annotationNote, String userName, String user, String remoteAddr,
                                              String lastSavedNoteString) throws Exception;

    void setCPPMedicalHistory(CaseManagementCPP cpp, String providerNo, List accessRight);

    String listNotes(String code, String providerNo, String demoNo);

}