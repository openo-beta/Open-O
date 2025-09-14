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

package ca.openosp.openo.prescript.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import ca.openosp.openo.commn.model.Allergy;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.PMmodule.caisi_integrator.CaisiIntegratorManager;
import ca.openosp.openo.PMmodule.caisi_integrator.IntegratorFallBackManager;
import ca.openosp.openo.caisi_integrator.ws.CachedDemographicAllergy;
import ca.openosp.openo.commn.dao.AllergyDao;
import ca.openosp.openo.commn.dao.DiseasesDao;
import ca.openosp.openo.commn.dao.PartialDateDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.Diseases;
import ca.openosp.openo.commn.model.PartialDate;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

/**
 * Data access class for patient information within the prescription module.
 *
 * This class provides comprehensive patient data retrieval functionality specifically
 * tailored for prescription workflows. It includes methods for patient search,
 * demographic information retrieval, allergy management, disease tracking, and
 * prescription history access.
 *
 * The class integrates with the CAISI Integrator for multi-facility patient data
 * sharing, allowing access to remote patient allergies and other clinical information
 * when the integrator is enabled. This ensures prescribers have complete allergy
 * information even when patients receive care at multiple facilities.
 *
 * All methods use static access patterns for utility-style operations, with the
 * exception of the inner Patient class which provides an object-oriented interface
 * to patient data.
 *
 * @since 2006-03-01
 */
public class RxPatientData {
    private static Logger logger = MiscUtils.getLogger();
    private static final DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);

    /**
     * Private constructor prevents instantiation.
     *
     * This class uses static methods exclusively, following a utility class pattern.
     */
    private RxPatientData() {
        // prevent instantiation
    }

    /**
     * Searches for patients by name.
     *
     * Performs a demographic search using surname and first name combination.
     * The search is performed with security context to ensure only authorized
     * patient records are returned.
     *
     * @param loggedInInfo LoggedInInfo security and session context
     * @param surname String patient's last name (partial matches supported)
     * @param firstName String patient's first name (partial matches supported)
     * @return Patient[] array of matching patients, empty if none found
     */
    public static Patient[] PatientSearch(LoggedInInfo loggedInInfo, String surname, String firstName) {

        Patient[] arr = {};
        List<Patient> patients = new ArrayList<Patient>();
        List<Demographic> demographics = demographicManager.searchDemographic(loggedInInfo, surname + "," + firstName);
        for (Demographic demographic : demographics) {
            Patient p = new Patient(demographic);
            patients.add(p);
        }
        return patients.toArray(arr);
    }

    /**
     * Retrieves a patient by demographic number.
     *
     * @param loggedInInfo LoggedInInfo security and session context
     * @param demographicNo int unique patient identifier
     * @return Patient object with demographic and clinical data
     */
    public static Patient getPatient(LoggedInInfo loggedInInfo, int demographicNo) {
        Demographic demographic = demographicManager.getDemographic(loggedInInfo, demographicNo);
        return new Patient(demographic);
    }

    /**
     * Retrieves a patient by demographic number string.
     *
     * Convenience overload that accepts string demographic number.
     *
     * @param loggedInInfo LoggedInInfo security and session context
     * @param demographicNo String unique patient identifier
     * @return Patient object with demographic and clinical data
     */
    public static Patient getPatient(LoggedInInfo loggedInInfo, String demographicNo) {
        Demographic demographic = demographicManager.getDemographic(loggedInInfo, demographicNo);
        return new Patient(demographic);
    }

    /**
     * Calculates patient age from date of birth.
     *
     * Accurately calculates age accounting for whether the birthday has
     * occurred this year. Returns 0 for null dates.
     *
     * @param DOB Date patient's date of birth
     * @return int age in years, 0 if DOB is null
     */
    private static int calcAge(java.util.Date DOB) {
        if (DOB == null) return 0;

        GregorianCalendar now = new GregorianCalendar();
        int curYear = now.get(Calendar.YEAR);
        int curMonth = (now.get(Calendar.MONTH) + 1);
        int curDay = now.get(Calendar.DAY_OF_MONTH);

        Calendar cal = new GregorianCalendar();
        cal.setTime(DOB);
        int iYear = cal.get(Calendar.YEAR);
        int iMonth = (cal.get(Calendar.MONTH) + 1);
        int iDay = cal.get(Calendar.DAY_OF_MONTH);
        int age = 0;

        if (curMonth > iMonth || (curMonth == iMonth && curDay >= iDay)) {
            age = curYear - iYear;
        } else {
            age = curYear - iYear - 1;
        }

        return age;
    }

    /**
     * Comprehensive patient data model for prescription workflows.
     *
     * This inner class encapsulates all patient-related data needed for prescribing,
     * including demographics, allergies, diseases, and prescription history. It provides
     * a rich API for accessing and managing patient clinical information.
     *
     * The class integrates with both local and remote (via CAISI Integrator) data sources
     * to provide a complete view of the patient's medical information across facilities.
     * This is critical for safe prescribing in multi-facility healthcare networks.
     */
    public static class Patient {
        /**
         * Core demographic information for the patient.
         */
        private Demographic demographic = null;

        /**
         * DAO for allergy data access.
         */
        private static AllergyDao allergyDao = (AllergyDao) SpringUtils.getBean(AllergyDao.class);

        /**
         * DAO for partial date handling (for imprecise allergy start dates).
         */
        private PartialDateDao partialDateDao = (PartialDateDao) SpringUtils.getBean(PartialDateDao.class);

        /**
         * Constructs a Patient from demographic data.
         *
         * @param demographic Demographic entity containing patient information
         */
        public Patient(Demographic demographic) {
            this.demographic = demographic;

            if (demographic == null) MiscUtils.getLogger().warn("Demographic is not set!");
        }

        /**
         * Gets the underlying demographic entity.
         *
         * @return Demographic entity with complete patient information
         */
        public Demographic getDemographic() {
            return this.demographic;
        }

        /**
         * Gets the patient's unique demographic number.
         *
         * @return int demographic number, -1 if not set
         */
        public int getDemographicNo() {
            if (demographic != null) {
                return demographic.getDemographicNo();
            } else {
                MiscUtils.getLogger().warn("DemographicNo is not set!");
                return -1;
            }
        }

        /**
         * Gets the patient's surname.
         *
         * @return String last name, empty string if not available
         */
        public String getSurname() {
            if (demographic != null) return demographic.getLastName();
            else return "";
        }

        /**
         * Gets the patient's first name.
         *
         * @return String first name, empty string if not available
         */
        public String getFirstName() {
            if (demographic != null) return demographic.getFirstName();
            else return "";
        }

        /**
         * Gets the patient's biological sex.
         *
         * @return String sex (M/F), empty string if not available
         */
        public String getSex() {
            if (demographic != null) return demographic.getSex();
            else return "";
        }

        /**
         * Gets the patient's Health Insurance Number.
         *
         * @return String HIN/health card number, empty string if not available
         */
        public String getHin() {
            if (demographic != null) return demographic.getHin();
            else return "";
        }

        /**
         * Gets the patient's date of birth.
         *
         * @return Date of birth, null if not available
         */
        public java.util.Date getDOB() {
            Date dob = null;
            if (demographic != null) dob = demographic.getBirthDay().getTime();

            return dob;
        }

        /**
         * Calculates the patient's current age.
         *
         * @return int age in years based on date of birth
         */
        public int getAge() {
            return calcAge(this.getDOB());
        }

        /**
         * Gets the patient's street address.
         *
         * @return String street address, empty string if not available
         */
        public String getAddress() {
            if (demographic != null) return demographic.getAddress();
            else return "";
        }

        /**
         * Gets the patient's city.
         *
         * @return String city name, empty string if not available
         */
        public String getCity() {
            if (demographic != null) return demographic.getCity();
            else return "";
        }

        /**
         * Gets the patient's province/state.
         *
         * @return String province code, empty string if not available
         */
        public String getProvince() {
            if (demographic != null) return demographic.getProvince();
            else return "";
        }

        /**
         * Gets the patient's postal/zip code.
         *
         * @return String postal code, empty string if not available
         */
        public String getPostal() {
            if (demographic != null) return demographic.getPostal();
            else return "";
        }

        /**
         * Gets the patient's primary phone number.
         *
         * @return String phone number, empty string if not available
         */
        public String getPhone() {
            if (demographic != null) return demographic.getPhone();
            else return "";
        }

        /**
         * Gets the patient's medical record/chart number.
         *
         * @return String chart number, empty string if not available
         */
        public String getChartNo() {
            if (demographic != null) return demographic.getChartNo();
            else return "";
        }

        /**
         * Retrieves a specific allergy by ID.
         *
         * Includes partial date formatting for imprecise allergy start dates.
         *
         * @param id int allergy record ID
         * @return Allergy object with complete allergy information
         */
        public Allergy getAllergy(int id) {
            Allergy allergy = allergyDao.find(id);
            PartialDate pd = partialDateDao.getPartialDate(PartialDate.ALLERGIES, allergy.getId(), PartialDate.ALLERGIES_STARTDATE);
            if (pd != null) allergy.setStartDateFormat(pd.getFormat());

            return allergy;
        }

        /**
         * Retrieves all allergies for the patient including remote allergies.
         *
         * This method aggregates allergies from both the local database and
         * remote facilities via the CAISI Integrator when enabled. This ensures
         * prescribers have complete allergy information even when patients
         * receive care at multiple facilities.
         *
         * Remote allergies are retrieved with fallback to cached data if the
         * integrator is temporarily offline.
         *
         * @param loggedInInfo LoggedInInfo security context with facility information
         * @return Allergy[] comprehensive array of patient allergies from all sources
         */
        public Allergy[] getAllergies(LoggedInInfo loggedInInfo) {
            ArrayList<Allergy> results = new ArrayList<Allergy>();
            Integer demographicNo = getDemographicNo();

            // Get local allergies
            List<Allergy> allergies = allergyDao.findAllergies(demographicNo);
            results.addAll(allergies);

            // Get remote allergies if integrator is enabled
            if (loggedInInfo.getCurrentFacility().isIntegratorEnabled()) {
                try {
                    List<CachedDemographicAllergy> remoteAllergies = null;
                    try {
                        // Attempt to get remote allergies from integrator
                        if (!CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                            remoteAllergies = CaisiIntegratorManager.getDemographicWs(loggedInInfo, loggedInInfo.getCurrentFacility()).getLinkedCachedDemographicAllergies(demographicNo);
                        }
                    } catch (Exception e) {
                        MiscUtils.getLogger().error("Unexpected error.", e);
                        CaisiIntegratorManager.checkForConnectionError(loggedInInfo.getSession(), e);
                    }

                    // Fall back to cached data if integrator is offline
                    if (CaisiIntegratorManager.isIntegratorOffline(loggedInInfo.getSession())) {
                        remoteAllergies = IntegratorFallBackManager.getRemoteAllergies(loggedInInfo, demographicNo);
                    }

                    // Convert remote allergies to local format
                    for (CachedDemographicAllergy remoteAllergy : remoteAllergies) {
                        Date date = null;
                        if (remoteAllergy.getEntryDate() != null) date = remoteAllergy.getEntryDate().getTime();

                        Allergy a = new Allergy();
                        a.setDemographicNo(demographicNo);
                        a.setId(remoteAllergy.getFacilityIdIntegerCompositePk().getCaisiItemId().intValue());
                        a.setEntryDate(date);
                        a.setDescription(remoteAllergy.getDescription());
                        a.setHiclSeqno(remoteAllergy.getHiclSeqNo());
                        a.setHicSeqno(remoteAllergy.getHicSeqNo());
                        a.setAgcsp(remoteAllergy.getAgcsp());
                        a.setAgccs(remoteAllergy.getAgccs());
                        a.setTypeCode(remoteAllergy.getTypeCode());
                        a.setIntegratorResult(true);
                        a.setReaction(remoteAllergy.getReaction());

                        if (remoteAllergy.getStartDate() != null) date = remoteAllergy.getStartDate().getTime();

                        a.setStartDate(date);
                        a.setAgeOfOnset(remoteAllergy.getAgeOfOnset());
                        a.setSeverityOfReaction(remoteAllergy.getSeverityCode());
                        a.setOnsetOfReaction(remoteAllergy.getOnSetCode());
                        a.setRegionalIdentifier(remoteAllergy.getRegionalIdentifier());
                        a.setLifeStage(remoteAllergy.getLifeStage());
                        a.setDrugrefId(String.valueOf(remoteAllergy.getPickId()));

                        results.add(a);
                    }
                } catch (Exception e) {
                    logger.error("error getting remote allergies", e);
                }
            }

            return (results.toArray(new Allergy[0]));
        }

        /**
         * Retrieves only active (non-archived) allergies.
         *
         * Filters out deleted/archived allergies to show only current allergies.
         *
         * @return Allergy[] array of active allergies
         */
        public Allergy[] getActiveAllergies() {
            List<Allergy> allergies = allergyDao.findActiveAllergies(getDemographicNo());
            return allergies.toArray(new Allergy[allergies.size()]);
        }

        /**
         * Adds a new allergy to the patient's record.
         *
         * Persists the allergy with entry date and handles partial date formatting
         * for imprecise start dates (e.g., "2020" or "2020-03").
         *
         * @param entryDate Date when the allergy was recorded
         * @param allergy Allergy object containing allergy details
         */
        public void addAllergy(java.util.Date entryDate, Allergy allergy) {
            allergy.setEntryDate(entryDate);
            allergyDao.persist(allergy);
            partialDateDao.setPartialDate(PartialDate.ALLERGIES, allergy.getId(), PartialDate.ALLERGIES_STARTDATE, allergy.getStartDateFormat());
        }

        /**
         * Sets the archive status of an allergy.
         *
         * Used internally to mark allergies as deleted (archived) or active.
         *
         * @param allergyId int ID of the allergy to modify
         * @param archive boolean true to archive, false to activate
         * @return boolean true if successful, false if allergy not found
         */
        private static boolean setAllergyArchive(int allergyId, boolean archive) {
            Allergy allergy = allergyDao.find(allergyId);
            if (allergy != null) {
                allergy.setArchived(archive);
                allergyDao.merge(allergy);
                return (true);
            }

            return (false);
        }

        /**
         * Marks an allergy as deleted (archived).
         *
         * Allergies are not physically deleted but marked as archived
         * to maintain audit history.
         *
         * @param allergyId int ID of the allergy to delete
         * @return boolean true if successful
         */
        public boolean deleteAllergy(int allergyId) {
            return (setAllergyArchive(allergyId, true));
        }

        /**
         * Reactivates a previously deleted allergy.
         *
         * Restores an archived allergy to active status.
         *
         * @param allergyId int ID of the allergy to activate
         * @return boolean true if successful
         */
        public boolean activateAllergy(int allergyId) {
            return (setAllergyArchive(allergyId, false));
        }

        /**
         * Retrieves the patient's disease/diagnosis list.
         *
         * Returns all recorded diseases and conditions for the patient,
         * typically using ICD-9 coding.
         *
         * @return Diseases[] array of patient diseases
         */
        public Diseases[] getDiseases() {
            DiseasesDao diseasesDao = SpringUtils.getBean(DiseasesDao.class);
            List<Diseases> diseases = diseasesDao.findByDemographicNo(getDemographicNo());
            return diseases.toArray(new Diseases[diseases.size()]);
        }

        /**
         * Adds a new disease/diagnosis to the patient's record.
         *
         * Records a new diagnosis with ICD-9 code and entry date.
         *
         * @param ICD9 String ICD-9 diagnosis code
         * @param entryDate Date when the diagnosis was made
         * @return Diseases the newly created disease record
         */
        public Diseases addDisease(String ICD9, java.util.Date entryDate) {
            DiseasesDao diseasesDao = SpringUtils.getBean(DiseasesDao.class);
            Diseases disease = new Diseases();
            disease.setDemographicNo(getDemographicNo());
            disease.setIcd9Entry(ICD9);
            disease.setEntryDate(entryDate);
            diseasesDao.persist(disease);
            return disease;
        }

        /**
         * Gets unique prescribed drugs (one per medication).
         *
         * Returns the most recent prescription for each unique medication,
         * useful for medication reconciliation and current medication lists.
         *
         * @return Prescription[] array of unique current prescriptions
         */
        public RxPrescriptionData.Prescription[] getPrescribedDrugsUnique() {
            return new RxPrescriptionData().getUniquePrescriptionsByPatient(this.getDemographicNo());
        }

        /**
         * Gets all prescribed drugs for the patient.
         *
         * Returns complete prescription history including all refills
         * and discontinued medications.
         *
         * @return Prescription[] array of all prescriptions
         */
        public RxPrescriptionData.Prescription[] getPrescribedDrugs() {
            return new RxPrescriptionData().getPrescriptionsByPatient(this.getDemographicNo());
        }

        /**
         * Gets prescription scripts (individual prescription instances).
         *
         * Returns individual prescription records as written, including
         * all scripts from the same prescription order.
         *
         * @return Prescription[] array of prescription scripts
         */
        public RxPrescriptionData.Prescription[] getPrescribedDrugScripts() {
            return new RxPrescriptionData().getPrescriptionScriptsByPatient(this.getDemographicNo());
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("demographic", demographic)
                    .append("partialDateDao", partialDateDao)
                    .toString();
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .toString();
    }
}
