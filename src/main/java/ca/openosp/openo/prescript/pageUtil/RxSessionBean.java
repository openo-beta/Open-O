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


package ca.openosp.openo.prescript.pageUtil;

import ca.openosp.openo.prescript.data.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.model.Allergy;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.OscarProperties;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Session management bean for prescription workflow state.
 *
 * This class maintains the state of a prescription writing session,
 * including the prescriber, patient, pending prescriptions (stash),
 * drug interaction warnings, and allergy alerts. It serves as the
 * central state management component for the prescription module.
 *
 * The bean handles:
 * - Prescription stash (pending prescriptions being written)
 * - Allergy and interaction warnings
 * - Re-prescription tracking
 * - Medication history
 * - Provider and patient context
 *
 * Thread-safe collections are used where appropriate to handle
 * concurrent access in web application environments.
 *
 * @since 2003-12-10
 */
public class RxSessionBean implements java.io.Serializable {
    /**
     * Logger for debugging and error messages.
     */
    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Provider number for the prescriber.
     */
    private String providerNo = null;

    /**
     * Patient demographic number.
     */
    private int demographicNo = 0;

    /**
     * Current view mode ("Active", "All", etc.).
     */
    private String view = "Active";

    /**
     * List of pending prescriptions being written.
     */
    private ArrayList<RxPrescriptionData.Prescription> stash = new ArrayList();

    /**
     * Maps favorite IDs to random IDs for tracking.
     */
    private HashMap<Integer, Long> favIdRandomIdMap = new HashMap<Integer, Long>();

    /**
     * Current index in the stash.
     */
    private int stashIndex = -1;

    /**
     * Allergy warnings for current prescriptions.
     */
    private Hashtable allergyWarnings = new Hashtable();

    /**
     * Missing allergy data warnings.
     */
    private Hashtable missingAllergyWarnings = new Hashtable();

    /**
     * Temporary working set of allergy warnings.
     */
    private Hashtable workingAllergyWarnings = new Hashtable();

    /**
     * List of attribute names for prescriptions.
     */
    private ArrayList attributeNames = new ArrayList();

    /**
     * Serialized list of interacting drugs.
     */
    private String interactingDrugList = "";

    /**
     * Thread-safe list of drug IDs for re-prescription.
     */
    private CopyOnWriteArrayList reRxDrugIdList = new CopyOnWriteArrayList<>();

    /**
     * Maps random IDs to drug IDs.
     */
    private HashMap randomIdDrugIdPair = new HashMap();

    /**
     * Medication history for the patient.
     */
    private List<HashMap<String, String>> listMedHistory = new ArrayList();

    /**
     * Gets the medication history list.
     *
     * @return List<HashMap<String, String>> medication history
     */
    public List<HashMap<String, String>> getListMedHistory() {
        return listMedHistory;
    }

    /**
     * Sets the medication history list.
     *
     * @param l List<HashMap<String, String>> medication history
     */
    public void setListMedHistory(List<HashMap<String, String>> l) {
        listMedHistory = l;
    }

    /**
     * Gets the random ID to drug ID mapping.
     *
     * @return HashMap mapping of random IDs to drug IDs
     */
    public HashMap getRandomIdDrugIdPair() {
        return randomIdDrugIdPair;
    }

    /**
     * Sets the random ID to drug ID mapping.
     *
     * @param hm HashMap new mapping
     */
    public void setRandomIdDrugIdPair(HashMap hm) {
        randomIdDrugIdPair = hm;
    }

    /**
     * Adds a random ID to drug ID pair.
     *
     * @param r long random ID
     * @param d int drug ID
     */
    public void addRandomIdDrugIdPair(long r, int d) {
        randomIdDrugIdPair.put(r, d);
    }

    /**
     * Adds a drug ID to the re-prescription list.
     *
     * @param s String drug ID to add
     */
    public void addReRxDrugIdList(String s) {
        reRxDrugIdList.add(s);
    }

    /**
     * Sets the re-prescription drug ID list.
     *
     * @param sList List<String> new drug ID list
     */
    public void setReRxDrugIdList(List<String> sList) {
        reRxDrugIdList = (CopyOnWriteArrayList) sList;
    }

    /**
     * Gets the re-prescription drug ID list.
     *
     * @return CopyOnWriteArrayList<String> thread-safe list of drug IDs
     */
    public CopyOnWriteArrayList<String> getReRxDrugIdList() {
        return reRxDrugIdList;
    }

    /**
     * Clears the re-prescription drug ID list.
     */
    public void clearReRxDrugIdList() {
        reRxDrugIdList = new CopyOnWriteArrayList<>();
    }

    /**
     * Gets the interacting drug list.
     *
     * @return String serialized list of drug interactions
     */
    public String getInteractingDrugList() {
        return interactingDrugList;
    }

    /**
     * Sets the interacting drug list.
     *
     * @param s String serialized drug interactions
     */
    public void setInteractingDrugList(String s) {
        interactingDrugList = s;
    }

    /**
     * Gets the provider number.
     *
     * @return String provider identifier
     */
    public String getProviderNo() {
        return this.providerNo;
    }

    /**
     * Sets the provider number.
     *
     * @param RHS String provider identifier
     */
    public void setProviderNo(String RHS) {
        this.providerNo = RHS;
    }

    /**
     * Gets the current view mode.
     *
     * @return String view mode ("Active", "All", etc.)
     */
    public String getView() {
        return view;
    }

    /**
     * Sets the view mode.
     *
     * @param view String new view mode
     */
    public void setView(String view) {
        this.view = view;
    }

    /**
     * Gets the patient demographic number.
     *
     * @return int patient identifier
     */
    public int getDemographicNo() {
        return this.demographicNo;
    }

    /**
     * Sets the patient demographic number.
     *
     * @param RHS int patient identifier
     */
    public void setDemographicNo(int RHS) {
        this.demographicNo = RHS;
    }

    /**
     * Gets the attribute names list.
     *
     * @return ArrayList attribute names
     */
    public ArrayList getAttributeNames() {
        return this.attributeNames;
    }

    /**
     * Sets the attribute names list.
     *
     * @param RHS ArrayList new attribute names
     */
    public void setAttributeNames(ArrayList RHS) {
        this.attributeNames = RHS;
    }

    /**
     * Adds an attribute name to the list.
     *
     * @param RHS String attribute name to add
     */
    public void addAttributeName(String RHS) {
        this.attributeNames.add(RHS);
    }

    /**
     * Sets an attribute name at a specific index.
     *
     * @param RHS String attribute name
     * @param index int position in the list
     */
    public void addAttributeName(String RHS, int index) {
        this.attributeNames.set(index, RHS);
    }

    // Stash management methods

    /**
     * Gets the current stash index.
     *
     * @return int current position in stash
     */
    public int getStashIndex() {
        return this.stashIndex;
    }

    /**
     * Sets the stash index.
     *
     * @param RHS int new index position
     */
    public void setStashIndex(int RHS) {
        if (RHS < this.getStashSize()) {
            this.stashIndex = RHS;
        }
    }

    /**
     * Gets the number of prescriptions in the stash.
     *
     * @return int stash size
     */
    public int getStashSize() {
        return this.stash.size();
    }

    /**
     * Finds the stash index for a prescription by random ID.
     *
     * @param randomId int random ID to search for
     * @return int stash index or -1 if not found
     */
    public int getIndexFromRx(int randomId) {
        int ret = -1;
        for (int i = 0; i < stash.size(); i++) {
            if (stash.get(i).getRandomId() == randomId) {
                ret = i;
                break;
            }
        }
        logger.debug("in getIndexFromRx=" + ret);
        return ret;
    }

    /**
     * Gets the stash as an array.
     *
     * @return RxPrescriptionData.Prescription[] array of prescriptions
     */
    public RxPrescriptionData.Prescription[] getStash() {
        RxPrescriptionData.Prescription[] arr = {};

        arr = stash.toArray(arr);

        return arr;
    }

    /**
     * Gets the stash as a list.
     *
     * @return ArrayList<RxPrescriptionData.Prescription> list of prescriptions
     */
    public ArrayList<RxPrescriptionData.Prescription> getStashList() {
        return this.stash;
    }

    /**
     * Gets a prescription from the stash by index.
     *
     * @param index int position in stash
     * @return RxPrescriptionData.Prescription prescription at index
     */
    public RxPrescriptionData.Prescription getStashItem(int index) {
        return stash.get(index);
    }

    /**
     * Gets a prescription from the stash by random ID.
     *
     * @param randomId int random ID of prescription
     * @return RxPrescriptionData.Prescription matching prescription or null
     */
    public RxPrescriptionData.Prescription getStashItem2(int randomId) {
        RxPrescriptionData.Prescription psp = null;
        for (RxPrescriptionData.Prescription rx : stash) {
            if (rx.getRandomId() == randomId) {
                psp = rx;
            }
        }
        return psp;
    }

    /**
     * Updates a prescription in the stash.
     *
     * @param index int position to update
     * @param item RxPrescriptionData.Prescription new prescription
     */
    public void setStashItem(int index, RxPrescriptionData.Prescription item) {
        stash.set(index, item);
    }

    /**
     * Adds a prescription to the stash.
     *
     * Checks for duplicates by brand name and GCN sequence number.
     * If duplicate found, returns existing index. Otherwise adds
     * new prescription and preloads interaction/allergy warnings.
     *
     * @param loggedInInfo LoggedInInfo current user session
     * @param item RxPrescriptionData.Prescription prescription to add
     * @return int index of prescription in stash
     */
    public int addStashItem(LoggedInInfo loggedInInfo, RxPrescriptionData.Prescription item) {

        int ret = -1;

        int i;
        RxPrescriptionData.Prescription rx;

        // Check for duplicate prescriptions
        // by comparing brand name and GCN sequence number
        for (i = 0; i < this.getStashSize(); i++) {
            rx = this.getStashItem(i);

            if (item.isCustom()) {
                if (rx.isCustom() && rx.getCustomName() != null && item.getCustomName() != null) {
                    if (rx.getCustomName().equals(item.getCustomName())) {
                        ret = i;
                        break;
                    }
                }
            } else {
                if (rx.getBrandName() != null && item.getBrandName() != null) {
                    if (rx.getBrandName().equals(item.getBrandName())
                            && rx.getGCN_SEQNO() == item.getGCN_SEQNO()) {
                        ret = i;
                        break;
                    }
                }
            }
        }

        if (ret > -1) {


            return ret;
        } else {
            stash.add(item);
            preloadInteractions();
            preloadAllergyWarnings(loggedInInfo, item.getAtcCode());


            return this.getStashSize() - 1;
        }

    }

    /**
     * Removes a prescription from the stash.
     *
     * @param index int position to remove
     */
    public void removeStashItem(int index) {
        stash.remove(index);
    }

    /**
     * Clears all prescriptions from the stash.
     */
    public void clearStash() {
        stash = new ArrayList();
    }

    /**
     * Gets the favorite ID to random ID mapping.
     *
     * @return HashMap<Integer, Long> mapping of favorite IDs to random IDs
     */
    public HashMap<Integer, Long> getFavIdRandomIdMaps() {
        return favIdRandomIdMap;
    }

    /**
     * Sets the favorite ID to random ID mapping.
     *
     * @param stashedIds HashMap<Integer, Long> new mapping
     */
    public void setFavIdRandomIdMap(HashMap<Integer, Long> stashedIds) {
        this.favIdRandomIdMap = stashedIds;
    }

    /**
     * Adds a new favorite ID to random ID mapping.
     *
     * @param newId Integer favorite ID
     * @param newRandomId Long random ID
     */
    public void addNewRandomIdToMap(Integer newId, Long newRandomId) {
        this.favIdRandomIdMap.put(newId, newRandomId);
    }

    /**
     * Gets the random ID for a favorite.
     *
     * @param idToGet Integer favorite ID
     * @return Long corresponding random ID
     */
    public Long getStashedFavId(Integer idToGet) {
        return this.favIdRandomIdMap.get(idToGet);
    }

    // Validation and utility methods

    /**
     * Checks if the session bean is valid.
     *
     * Validates that both demographic number and provider number
     * are properly set.
     *
     * @return boolean true if valid session
     */
    public boolean isValid() {
        if (this.demographicNo > 0
                && this.providerNo != null
                && this.providerNo.length() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Preloads drug interaction data.
     *
     * Initiates background loading of interaction data for all
     * ATC codes in the current stash to improve performance.
     */
    private void preloadInteractions() {
        RxInteractionData interact = RxInteractionData.getInstance();
        interact.preloadInteraction(this.getAtcCodes());
    }

    /**
     * Clears all allergy warning caches.
     */
    public void clearAllergyWarnings() {
        allergyWarnings = null;
        allergyWarnings = new Hashtable();

        missingAllergyWarnings = null;
        missingAllergyWarnings = new Hashtable();
    }


    /**
     * Preloads allergy warnings for a drug.
     *
     * Starts a background worker thread to check for allergies
     * related to the given ATC code.
     *
     * @param loggedInInfo LoggedInInfo current user session
     * @param atccode String ATC code to check
     */
    private void preloadAllergyWarnings(LoggedInInfo loggedInInfo, String atccode) {
        try {
            Allergy[] allergies = RxPatientData.getPatient(loggedInInfo, getDemographicNo()).getActiveAllergies();
            RxAllergyWarningWorker worker = new RxAllergyWarningWorker(this, atccode, allergies);
            addToWorkingAllergyWarnings(atccode, worker);
            worker.start();
        } catch (Exception e) {
            logger.error("Error for demographic " + getDemographicNo(), e);
        }
    }

    /**
     * Adds allergy warnings to the cache.
     *
     * @param atc String ATC code
     * @param allergy Allergy[] warnings for this ATC code
     */
    public void addAllergyWarnings(String atc, Allergy[] allergy) {
        if (atc != null && !atc.isEmpty()) {
            allergyWarnings.put(atc, allergy);
        }
    }

    /**
     * Adds missing allergy warnings to the cache.
     *
     * Tracks allergies that could not be checked due to missing data.
     *
     * @param atc String ATC code
     * @param allergy Allergy[] missing allergies
     */
    public void addMissingAllergyWarnings(String atc, Allergy[] allergy) {
        if (atc != null && !atc.isEmpty()) {
            missingAllergyWarnings.put(atc, allergy);
        }
    }

    /**
     * Adds a worker thread to the working set.
     *
     * Tracks background threads currently checking for allergies.
     *
     * @param atc String ATC code being checked
     * @param worker RxAllergyWarningWorker background thread
     */
    public void addToWorkingAllergyWarnings(String atc, RxAllergyWarningWorker worker) {
        if (atc != null && !atc.isEmpty()) {
            workingAllergyWarnings.put(atc, worker);
        }
    }

    /**
     * Removes a completed worker from the working set.
     *
     * @param atc String ATC code that was checked
     */
    public void removeFromWorkingAllergyWarnings(String atc) {
        if (atc != null && !atc.isEmpty()) {
            workingAllergyWarnings.remove(atc);
        }
    }


    /**
     * Gets allergy warnings for a drug.
     *
     * Retrieves cached allergy warnings or initiates a new check.
     * If a check is in progress, waits for completion. Respects
     * the RX_ALLERGY_CHECKING system property.
     *
     * @param loggedInInfo LoggedInInfo current user session
     * @param atccode String ATC code to check
     * @return Allergy[] array of allergy warnings or null
     */
    public Allergy[] getAllergyWarnings(LoggedInInfo loggedInInfo, String atccode) {
        Allergy[] allergies = null;

        // Check if allergy checking is enabled and ATC code is valid

        if (OscarProperties.getInstance().getBooleanProperty("RX_ALLERGY_CHECKING", "yes") && atccode != null && !atccode.equals("") && !atccode.equals("null")) {
            logger.debug("Checking allergy reaction : " + atccode);
            if (allergyWarnings.containsKey(atccode)) {

                allergies = (Allergy[]) allergyWarnings.get(atccode);
            } else if (workingAllergyWarnings.contains(atccode)) {

                RxAllergyWarningWorker worker = (RxAllergyWarningWorker) workingAllergyWarnings.get(atccode);
                if (worker != null) {
                    try {
                        worker.join();

                        // Finished
                    } catch (InterruptedException e) {
                        // Thread was interrupted

                        logger.error("Error", e);
                    }


                }
                allergies = (Allergy[]) allergyWarnings.get(atccode);

            } else {
                logger.debug("NEW ATC CODE for allergy");
                try {
                    RxDrugData drugData = new RxDrugData();
                    Allergy[] allAllergies = RxPatientData.getPatient(loggedInInfo, getDemographicNo()).getActiveAllergies();
                    List<Allergy> missing = new ArrayList<Allergy>();
                    allergies = drugData.getAllergyWarnings(atccode, allAllergies, missing);
                    if (allergies != null) {
                        addAllergyWarnings(atccode, allergies);
                        addMissingAllergyWarnings(atccode, missing.toArray(new Allergy[missing.size()]));
                    }
                } catch (Exception e) {
                    logger.error("Error", e);
                }
            }
        }
        return allergies;
    }


    /**
     * Gets all ATC codes for current patient.
     *
     * Combines ATC codes from patient's current medications
     * and prescriptions in the stash.
     *
     * @return Vector all ATC codes
     */
    public Vector getAtcCodes() {
        RxPrescriptionData rxData = new RxPrescriptionData();
        Vector atcCodes = rxData.getCurrentATCCodesByPatient(this.getDemographicNo());
        RxPrescriptionData.Prescription rx;
        for (int i = 0; i < this.getStashSize(); i++) {
            rx = this.getStashItem(i);
            atcCodes.add(rx.getAtcCode());
        }
        return atcCodes;
    }

    /**
     * Gets all regional identifiers for current patient.
     *
     * Combines regional identifiers from patient's current
     * medications and prescriptions in the stash.
     *
     * @return List all regional identifiers
     */
    public List getRegionalIdentifier() {
        RxPrescriptionData rxData = new RxPrescriptionData();
        List regionalIdentifierCodes = rxData.getCurrentRegionalIdentifiersCodesByPatient(this.getDemographicNo());
        RxPrescriptionData.Prescription rx;
        for (int i = 0; i < this.getStashSize(); i++) {
            rx = this.getStashItem(i);
            regionalIdentifierCodes.add(rx.getRegionalIdentifier());
        }
        return regionalIdentifierCodes;
    }

    /**
     * Gets drug interactions for current prescriptions.
     *
     * Checks for interactions between all active medications
     * and pending prescriptions in the stash. Results are sorted
     * by severity. Performance timing is logged for monitoring.
     *
     * @return RxDrugData.Interaction[] sorted array of interactions
     */
    public RxDrugData.Interaction[] getInteractions() {
        RxDrugData.Interaction[] interactions = null;
        long start = System.currentTimeMillis();
        long start2 = 0;
        long end2 = 0;
        try {
            start2 = System.currentTimeMillis();
            RxPrescriptionData rxData = new RxPrescriptionData();

            RxInteractionData rxInteract = RxInteractionData.getInstance();
            Vector atcCodes = rxData.getCurrentATCCodesByPatient(this.getDemographicNo());

            logger.debug("atccode " + atcCodes);
            RxPrescriptionData.Prescription rx;
            for (int i = 0; i < this.getStashSize(); i++) {
                rx = this.getStashItem(i);
                if (rx.isValidAtcCode()) {
                    atcCodes.add(rx.getAtcCode());
                }
            }
            logger.debug("atccode 2" + atcCodes);
            if (atcCodes != null && atcCodes.size() > 1) {
                try {
                    interactions = rxInteract.getInteractions(atcCodes);
                    logger.debug("interactions " + interactions.length);
                    for (int i = 0; i < interactions.length; i++) {
                        logger.debug(interactions[i].affectingatc + " " + interactions[i].effect + " " + interactions[i].affectedatc);
                    }
                    Arrays.sort(interactions);
                } catch (Exception e) {
                    logger.error("Error", e);
                }
            }

            end2 = System.currentTimeMillis() - start2;
        } catch (Exception e2) {
        }
        long end = System.currentTimeMillis() - start;


        logger.debug("took " + end + "milliseconds vs " + end2);
        return interactions;
    }

    /**
     * Returns string representation for debugging.
     *
     * @return String detailed state information
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("providerNo", providerNo)
                .append("demographicNo", demographicNo)
                .append("view", view)
                .append("stash", stash)
                .append("favIdRandomIdMap", favIdRandomIdMap)
                .append("stashIndex", stashIndex)
                .append("allergyWarnings", allergyWarnings)
                .append("missingAllergyWarnings", missingAllergyWarnings)
                .append("workingAllergyWarnings", workingAllergyWarnings)
                .append("attributeNames", attributeNames)
                .append("interactingDrugList", interactingDrugList)
                .append("reRxDrugIdList", reRxDrugIdList)
                .append("randomIdDrugIdPair", randomIdDrugIdPair)
                .append("listMedHistory", listMedHistory)
                .toString();
    }
}
