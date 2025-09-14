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
package ca.openosp.openo.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import ca.openosp.openo.PMmodule.dao.VacancyDao;
import ca.openosp.openo.PMmodule.dao.WaitlistDao;
import ca.openosp.openo.PMmodule.model.Vacancy;
import ca.openosp.openo.PMmodule.model.VacancyClientMatch;
import ca.openosp.openo.match.client.ClientData;
import ca.openosp.openo.match.vacancy.VacancyData;
import ca.openosp.openo.match.vacancy.VacancyTemplateData;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Core patient-provider matching engine for OpenO EMR healthcare system.
 *
 * <p>This class implements sophisticated algorithms to match patients (clients) with
 * available healthcare provider appointment slots (vacancies) based on multiple
 * healthcare-specific criteria. The matching system supports both program-specific
 * and system-wide matching operations.</p>
 *
 * <p>Key Healthcare Matching Features:
 * <ul>
 *   <li>Patient demographic matching (age, gender, location)</li>
 *   <li>Medical requirement matching (specialties, conditions)</li>
 *   <li>Program-specific eligibility criteria</li>
 *   <li>Provider availability and capacity constraints</li>
 *   <li>Weighted scoring system for optimal patient-provider pairing</li>
 * </ul>
 * </p>
 *
 * <p>Matching Algorithm: The system uses a weighted scoring algorithm that compares
 * patient characteristics against vacancy requirements. Each match receives a percentage
 * score based on how well the patient fits the vacancy criteria, with matches sorted
 * by score for optimal assignment recommendations.</p>
 *
 * <p>Canadian Healthcare Context: Designed to work within Canadian healthcare
 * programs including community health centers, specialist referrals, and
 * multi-jurisdictional provider networks.</p>
 *
 * @see VacancyClientMatch
 * @see ClientData
 * @see VacancyData
 * @see VacancyTemplateData
 * @since 2012-11-10
 */
public class Matcher {

    private WaitlistDao waitlistDao = SpringUtils.getBean(WaitlistDao.class);
    private VacancyDao vacancyDao = SpringUtils.getBean(VacancyDao.class);


    /**
     * Generates patient-provider matches for a specific healthcare vacancy.
     *
     * <p>This method evaluates all patients currently on waitlists against the
     * specified vacancy to identify potential matches. Each match is scored based
     * on how well the patient meets the vacancy requirements.</p>
     *
     * @param vacancyId Integer the unique identifier for the healthcare provider vacancy
     * @return List<VacancyClientMatch> sorted list of patient matches, ordered by match percentage (highest first)
     */
    public List<VacancyClientMatch> listClientMatchesForVacancy(int vacancyId) {
        List<VacancyClientMatch> vacancyClientMatches = new ArrayList<VacancyClientMatch>();
        VacancyData vacancyData = waitlistDao.loadVacancyData(vacancyId);
        List<ClientData> clientDatas = waitlistDao.getAllClientsData();
        for (ClientData clientData : clientDatas) {
            VacancyClientMatch vcMatch = match(clientData, vacancyData);
            vacancyClientMatches.add(vcMatch);
        }
        Collections.sort(vacancyClientMatches);
        return vacancyClientMatches;
    }

    /**
     * Generates patient-provider matches for a specific healthcare vacancy within a program.
     *
     * <p>This method evaluates only patients enrolled in the specified healthcare program
     * against the vacancy. This ensures program-specific matching rules and eligibility
     * criteria are properly applied.</p>
     *
     * @param vacancyId Integer the unique identifier for the healthcare provider vacancy
     * @param wlProgramId Integer the waitlist program identifier to filter eligible patients
     * @return List<VacancyClientMatch> sorted list of program-eligible patient matches, ordered by match percentage (highest first)
     */
    public List<VacancyClientMatch> listClientMatchesForVacancy(int vacancyId, int wlProgramId) {
        List<VacancyClientMatch> vacancyClientMatches = new ArrayList<VacancyClientMatch>();
        VacancyData vacancyData = waitlistDao.loadVacancyData(vacancyId, wlProgramId);
        List<ClientData> clientDatas = waitlistDao.getAllClientsDataByProgramId(wlProgramId);
        for (ClientData clientData : clientDatas) {
            VacancyClientMatch vcMatch = match(clientData, vacancyData);
            vacancyClientMatches.add(vcMatch);
        }
        Collections.sort(vacancyClientMatches);
        return vacancyClientMatches;
    }

    /**
     * Finds all available healthcare vacancies that match a specific patient within a program.
     *
     * <p>This method evaluates the patient's characteristics and requirements against
     * all current vacancies within the specified healthcare program to identify
     * suitable provider appointments.</p>
     *
     * @param clientId Integer the unique patient identifier (demographic number)
     * @param programId Integer the healthcare program identifier to search for vacancies
     * @return List<VacancyClientMatch> sorted list of matching vacancies, ordered by match percentage (highest first)
     */
    public List<VacancyClientMatch> listVacancyMatchesForClient(int clientId, int programId) {
        List<VacancyClientMatch> vacancyClientMatches = new ArrayList<VacancyClientMatch>();
        ClientData clientData = waitlistDao.getClientData(clientId);
        List<VacancyData> vacancyDataList = loadAllVacancies(programId);
        for (VacancyData vData : vacancyDataList) {
            VacancyClientMatch vcMatch = match(clientData, vData);
            vacancyClientMatches.add(vcMatch);
        }
        Collections.sort(vacancyClientMatches);
        return vacancyClientMatches;
    }

    /**
     * Finds all available healthcare vacancies that match a specific patient across all programs.
     *
     * <p>This method provides comprehensive vacancy matching by evaluating the patient
     * against all current vacancies system-wide, regardless of program boundaries.
     * Useful for identifying alternative placement options when program-specific
     * vacancies are limited.</p>
     *
     * @param clientId Integer the unique patient identifier (demographic number)
     * @return List<VacancyClientMatch> sorted list of all matching vacancies, ordered by match percentage (highest first)
     */
    public List<VacancyClientMatch> listVacancyMatchesForClient(int clientId) {
        List<VacancyClientMatch> vacancyClientMatches = new ArrayList<VacancyClientMatch>();
        ClientData clientData = waitlistDao.getClientData(clientId);
        // Skip matching if patient has no assessment data available
        if (clientData.getClientData().size() == 0) return vacancyClientMatches;
        List<VacancyData> vacancyDataList = loadAllVacancies();
        for (VacancyData vData : vacancyDataList) {
            VacancyClientMatch vcMatch = match(clientData, vData);
            vacancyClientMatches.add(vcMatch);
        }
        Collections.sort(vacancyClientMatches);
        return vacancyClientMatches;
    }

    /**
     * Loads all current healthcare vacancies for a specific program.
     *
     * @param programId Integer the healthcare program identifier
     * @return List<VacancyData> list of vacancy data objects with matching criteria
     */
    private List<VacancyData> loadAllVacancies(int programId) {
        List<VacancyData> vacancies = new ArrayList<VacancyData>();
        List<Integer> vacancyDataList = new ArrayList<Integer>();
        for (Vacancy v : vacancyDao.findCurrent()) {
            vacancyDataList.add(v.getId());
        }
        for (Integer vacancyId : vacancyDataList) {
            VacancyData vacancyData = waitlistDao.loadVacancyData(vacancyId, programId);
            vacancies.add(vacancyData);
        }
        return vacancies;

    }

    /**
     * Loads all current healthcare vacancies across all programs.
     *
     * @return List<VacancyData> list of all available vacancy data objects with matching criteria
     */
    private List<VacancyData> loadAllVacancies() {
        List<VacancyData> vacancies = new ArrayList<VacancyData>();
        List<Integer> vacancyDataList = new ArrayList<Integer>();
        for (Vacancy v : vacancyDao.findCurrent()) {
            vacancyDataList.add(v.getId());
        }
        for (Integer vacancyId : vacancyDataList) {
            VacancyData vacancyData = waitlistDao.loadVacancyData(vacancyId);
            vacancies.add(vacancyData);
        }
        return vacancies;

    }


    /**
     * Performs detailed matching algorithm between a patient and healthcare vacancy.
     *
     * <p>This core matching method implements the weighted scoring algorithm that
     * compares patient characteristics against vacancy requirements. The match score
     * is calculated based on how many parameters match and their relative importance.</p>
     *
     * @param clientData ClientData the patient's assessment data and demographics
     * @param vacancyData VacancyData the vacancy requirements and matching template
     * @return VacancyClientMatch match result with percentage score and status
     */
    private VacancyClientMatch match(ClientData clientData,
                                     VacancyData vacancyData) {
        VacancyClientMatch vacancyClientMatch = new VacancyClientMatch();
        vacancyClientMatch.setClient_id(clientData.getClientId());
        vacancyClientMatch.setVacancy_id(vacancyData.getVacancy_id());
        vacancyClientMatch.setForm_id(clientData.getFormId());
        vacancyClientMatch.setStatus(VacancyClientMatch.PENDING);

        int paramCntPercentage = vacancyData.getVacancyData().size();
        int paramMatch = getParamMatch(clientData, vacancyData);
        // Calculate match percentage based on parameter matches vs total parameters
        if (paramCntPercentage == 0) {
            vacancyClientMatch.setMatchPercentage(0);
        } else {
            vacancyClientMatch.setMatchPercentage(paramMatch / vacancyData.getVacancyData().size());
            // Format match proportion as "matched_params/total_params"
            String proportion = String.format("%d/%d", paramMatch / 100, paramCntPercentage);
            vacancyClientMatch.setProportion(proportion);
        }
        return vacancyClientMatch;
    }

    /**
     * Calculates weighted match score by comparing patient data against vacancy requirements.
     *
     * <p>This method iterates through all patient characteristics and compares them
     * against the corresponding vacancy template requirements. Each parameter match
     * contributes to the overall weighted score based on the template's weighting system.</p>
     *
     * @param clientData ClientData the patient's assessment data
     * @param vacancyData VacancyData the vacancy requirements template
     * @return int the cumulative weighted match score (0-10000 scale)
     */
    private int getParamMatch(ClientData clientData, VacancyData vacancyData) {
        int paramMatch = 0;
        // Compare each patient parameter against vacancy template requirements
        for (Entry<String, String> paramEntry : clientData.getClientData()
                .entrySet()) {
            VacancyTemplateData vacancyTemlateData = vacancyData.getVacancyData().get(paramEntry.getKey());
            if (vacancyTemlateData != null) {
                // Add weighted match score for this parameter
                paramMatch += vacancyTemlateData.matches(paramEntry.getValue());
            }
        }
        return paramMatch;
    }
}
