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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class RxSessionBean implements java.io.Serializable {
    private static final Logger logger = MiscUtils.getLogger();
    private static final String SESSION_KEY_PREFIX = "RxSessionBean_";
    private static final String LEGACY_SESSION_KEY = "RxSessionBean";

    /** Default max age for stale session cleanup: 30 minutes */
    private static final long DEFAULT_MAX_AGE_MS = 30 * 60 * 1000;

    /** Timestamp of last access to this bean, used for stale session cleanup */
    private long lastAccessTime = System.currentTimeMillis();

    /**
     * Updates the last access timestamp. Called when the bean is retrieved from session.
     */
    public void touch() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * Gets the last access time for this bean.
     * @return timestamp in milliseconds
     */
    public long getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * Cleans up stale RxSessionBeans from the session that haven't been accessed
     * within the specified time period. This prevents session bloat when users
     * work with multiple patients.
     *
     * @param session the HTTP session
     * @param maxAgeMs maximum age in milliseconds before a bean is considered stale
     */
    public static void cleanupStaleBeans(HttpSession session, long maxAgeMs) {
        long now = System.currentTimeMillis();
        List<String> keysToRemove = new ArrayList<>();

        try {
            // Copy attribute names to avoid ConcurrentModificationException
            List<String> attributeNames = new ArrayList<>();
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                attributeNames.add(names.nextElement());
            }

            for (String name : attributeNames) {
                if (name.startsWith(SESSION_KEY_PREFIX)) {
                    Object attr = session.getAttribute(name);
                    if (attr instanceof RxSessionBean) {
                        RxSessionBean bean = (RxSessionBean) attr;
                        // Only cleanup if stale AND has no staged prescriptions
                        if (now - bean.getLastAccessTime() > maxAgeMs && bean.getStashSize() == 0) {
                            keysToRemove.add(name);
                        }
                    }
                }
            }

            for (String key : keysToRemove) {
                logger.debug("Cleaning up stale RxSessionBean: {}", key);
                session.removeAttribute(key);
            }
        } catch (Exception e) {
            // Don't let cleanup errors affect the application
            logger.warn("Error during RxSessionBean cleanup: {}", e.getMessage());
        }
    }

    /**
     * Cleans up stale RxSessionBeans using the default max age (30 minutes).
     *
     * @param session the HTTP session
     */
    public static void cleanupStaleBeans(HttpSession session) {
        cleanupStaleBeans(session, DEFAULT_MAX_AGE_MS);
    }

    /**
     * Gets the session key for a specific patient's RxSessionBean.
     * Uses per-patient keying to allow multiple patients' Medications tabs
     * to be open simultaneously without interfering with each other.
     *
     * @param demographicNo the patient's demographic number
     * @return the session attribute key for this patient's RxSessionBean
     */
    public static String getSessionKey(int demographicNo) {
        return SESSION_KEY_PREFIX + demographicNo;
    }

    /**
     * Extracts demographicNo from request parameter.
     * Used by RxSessionInterceptor to determine which patient's bean to load.
     *
     * @param request the HTTP request
     * @return the demographicNo, or -1 if not found or invalid
     */
    public static int extractDemographicNo(HttpServletRequest request) {
        String param = request.getParameter("demographicNo");
        if (param == null || param.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Retrieves the RxSessionBean for a specific patient using only the per-patient key.
     * This is a simpler lookup used by RxSessionInterceptor that doesn't fall back
     * to the legacy key.
     *
     * @param session the HTTP session
     * @param demographicNo the patient's demographic number
     * @return the RxSessionBean for this patient, or null if not found
     */
    public static RxSessionBean getPerPatient(HttpSession session, int demographicNo) {
        RxSessionBean bean = (RxSessionBean) session.getAttribute(getSessionKey(demographicNo));
        if (bean != null) {
            bean.touch();
        }
        return bean;
    }

    /**
     * Retrieves the RxSessionBean for a specific patient from the session.
     * First checks for the per-patient key, then falls back to legacy key
     * if the demographic matches.
     *
     * @param session the HTTP session
     * @param demographicNo the patient's demographic number
     * @return the RxSessionBean for this patient, or null if not found
     */
    public static RxSessionBean getFromSession(HttpSession session, int demographicNo) {
        // First try per-patient key
        RxSessionBean bean = (RxSessionBean) session.getAttribute(getSessionKey(demographicNo));
        if (bean != null) {
            bean.touch();
            return bean;
        }
        // Fall back to legacy key if demographic matches (for backward compatibility)
        bean = (RxSessionBean) session.getAttribute(LEGACY_SESSION_KEY);
        if (bean != null && bean.getDemographicNo() == demographicNo) {
            bean.touch();
            // Migrate to new key
            session.setAttribute(getSessionKey(demographicNo), bean);
            return bean;
        }
        return null;
    }

    /**
     * Retrieves the RxSessionBean for a specific patient from the request's session.
     *
     * @param request the HTTP request
     * @param demographicNo the patient's demographic number
     * @return the RxSessionBean for this patient, or null if not found
     */
    public static RxSessionBean getFromSession(HttpServletRequest request, int demographicNo) {
        return getFromSession(request.getSession(), demographicNo);
    }

    /**
     * Retrieves the RxSessionBean from the request, automatically determining the demographicNo
     * from request parameters. Checks for "demographicNo" parameter first, then falls back
     * to legacy session key.
     *
     * @param request the HTTP request
     * @return the RxSessionBean, or null if not found
     */
    public static RxSessionBean getFromSession(HttpServletRequest request) {
        String demoNoParam = request.getParameter("demographicNo");
        if (demoNoParam != null && !demoNoParam.isEmpty()) {
            try {
                int demoNo = Integer.parseInt(demoNoParam);
                return getFromSession(request.getSession(), demoNo);
            } catch (NumberFormatException e) {
                // Fall through to legacy approach
            }
        }
        // Fallback to legacy key
        RxSessionBean bean = (RxSessionBean) request.getSession().getAttribute(LEGACY_SESSION_KEY);
        if (bean != null) {
            bean.touch();
        }
        return bean;
    }

    /**
     * Saves the RxSessionBean to the session using both per-patient key and legacy key.
     * The per-patient key ensures multiple patients' data doesn't interfere.
     * The legacy key maintains backward compatibility with JSPs that use
     * ${sessionScope.RxSessionBean}.
     * <p>
     * Note: Both keys point to the same object instance, so no memory duplication occurs.
     * The RxSessionFilter ensures the correct bean is swapped into the legacy key
     * at the start of each request when switching between patients.
     *
     * @param session the HTTP session
     * @param bean the RxSessionBean to save
     */
    public static void saveToSession(HttpSession session, RxSessionBean bean) {
        // Update access time
        bean.touch();

        // Save with per-patient key (preserves data across multiple patient tabs)
        session.setAttribute(getSessionKey(bean.getDemographicNo()), bean);

        // Also save with legacy key for JSP backward compatibility
        // (same object reference, no memory duplication)
        session.setAttribute(LEGACY_SESSION_KEY, bean);
    }

    /**
     * Saves the RxSessionBean to the request's session using a per-patient key.
     *
     * @param request the HTTP request
     * @param bean the RxSessionBean to save
     */
    public static void saveToSession(HttpServletRequest request, RxSessionBean bean) {
        saveToSession(request.getSession(), bean);
    }

    /**
     * Removes the RxSessionBean for a specific patient from the session.
     *
     * @param session the HTTP session
     * @param demographicNo the patient's demographic number
     */
    public static void removeFromSession(HttpSession session, int demographicNo) {
        session.removeAttribute(getSessionKey(demographicNo));
    }

    private String providerNo = null;
    private int demographicNo = 0;
    private String view = "Active";

    private ArrayList<RxPrescriptionData.Prescription> stash = new ArrayList();
    // private ArrayList stash=new ArrayList();
    private HashMap<Integer, Long> favIdRandomIdMap = new HashMap<Integer, Long>();
    private int stashIndex = -1;
    private Hashtable allergyWarnings = new Hashtable();
    private Hashtable missingAllergyWarnings = new Hashtable();
    private Hashtable workingAllergyWarnings = new Hashtable();
    private ArrayList attributeNames = new ArrayList();
    private String interactingDrugList = "";//contains hash tables, each hashtable has the a
    private CopyOnWriteArrayList reRxDrugIdList = new CopyOnWriteArrayList<>();
    private HashMap randomIdDrugIdPair = new HashMap();
    private List<HashMap<String, String>> listMedHistory = new ArrayList();

    public List<HashMap<String, String>> getListMedHistory() {
        return listMedHistory;
    }

    public void setListMedHistory(List<HashMap<String, String>> l) {
        listMedHistory = l;
    }

    public HashMap getRandomIdDrugIdPair() {
        return randomIdDrugIdPair;
    }

    public void setRandomIdDrugIdPair(HashMap hm) {
        randomIdDrugIdPair = hm;
    }

    public void addRandomIdDrugIdPair(long r, int d) {
        randomIdDrugIdPair.put(r, d);
    }

    public void addReRxDrugIdList(String s) {
        reRxDrugIdList.add(s);
    }

    public void setReRxDrugIdList(List<String> sList) {
        reRxDrugIdList = (CopyOnWriteArrayList) sList;
    }

    public CopyOnWriteArrayList<String> getReRxDrugIdList() {
        return reRxDrugIdList;
    }

    public void clearReRxDrugIdList() {
        reRxDrugIdList = new CopyOnWriteArrayList<>();
    }

    public String getInteractingDrugList() {
        return interactingDrugList;
    }

    public void setInteractingDrugList(String s) {
        interactingDrugList = s;
    }

    public String getProviderNo() {
        return this.providerNo;
    }

    public void setProviderNo(String RHS) {
        this.providerNo = RHS;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public int getDemographicNo() {
        return this.demographicNo;
    }

    public void setDemographicNo(int RHS) {
        this.demographicNo = RHS;
    }

    public ArrayList getAttributeNames() {
        return this.attributeNames;
    }

    public void setAttributeNames(ArrayList RHS) {
        this.attributeNames = RHS;
    }

    public void addAttributeName(String RHS) {
        this.attributeNames.add(RHS);
    }

    public void addAttributeName(String RHS, int index) {
        this.attributeNames.set(index, RHS);
    }

    //--------------------------------------------------------------------------

    public int getStashIndex() {
        return this.stashIndex;
    }

    public void setStashIndex(int RHS) {
        if (RHS < this.getStashSize()) {
            this.stashIndex = RHS;
        }
    }

    public int getStashSize() {
        return this.stash.size();
    }

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

    public RxPrescriptionData.Prescription[] getStash() {
        RxPrescriptionData.Prescription[] arr = {};

        arr = stash.toArray(arr);

        return arr;
    }

    public ArrayList<RxPrescriptionData.Prescription> getStashList() {
        return this.stash;
    }

    public RxPrescriptionData.Prescription getStashItem(int index) {
        return stash.get(index);
    }

    //return prescript from its random id
    public RxPrescriptionData.Prescription getStashItem2(int randomId) {
        RxPrescriptionData.Prescription psp = null;
        for (RxPrescriptionData.Prescription rx : stash) {
            if (rx.getRandomId() == randomId) {
                psp = rx;
            }
        }
        return psp;
    }

    public void setStashItem(int index, RxPrescriptionData.Prescription item) {
        //this.clearDAM();
        //this.clearDDI();
        stash.set(index, item);
    }

    public int addStashItem(LoggedInInfo loggedInInfo, RxPrescriptionData.Prescription item) {

        int ret = -1;

        int i;
        RxPrescriptionData.Prescription rx;

        //check to see if the item already exists
        //by checking for duplicate brandname and gcn seq no
        //if it exists, return it, else add it.
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
                            && Objects.equals(rx.getGCN_SEQNO(), item.getGCN_SEQNO())) {
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

    public void removeStashItem(int index) {
        //    this.clearDDI();
        //    this.clearDAM();
        stash.remove(index);
    }

    public void clearStash() {
        //    this.clearDDI();
        //    this.clearDAM();
        stash = new ArrayList();
    }

    public HashMap<Integer, Long> getFavIdRandomIdMaps() {
        return favIdRandomIdMap;
    }

    public void setFavIdRandomIdMap(HashMap<Integer, Long> stashedIds) {
        this.favIdRandomIdMap = stashedIds;
    }

    public void addNewRandomIdToMap(Integer newId, Long newRandomId) {
        this.favIdRandomIdMap.put(newId, newRandomId);
    }

    public Long getStashedFavId(Integer idToGet) {
        return this.favIdRandomIdMap.get(idToGet);
    }

    //--------------------------------------------------------------------------

    public boolean isValid() {
        if (this.demographicNo > 0
                && this.providerNo != null
                && this.providerNo.length() > 0) {
            return true;
        }
        return false;
    }

    private void preloadInteractions() {
        RxInteractionData interact = RxInteractionData.getInstance();
        interact.preloadInteraction(this.getAtcCodes());
    }

    public void clearAllergyWarnings() {
        allergyWarnings = null;
        allergyWarnings = new Hashtable();

        missingAllergyWarnings = null;
        missingAllergyWarnings = new Hashtable();
    }


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

    public void addAllergyWarnings(String atc, Allergy[] allergy) {
        if (atc != null && !atc.isEmpty()) {
            allergyWarnings.put(atc, allergy);
        }
    }

    public void addMissingAllergyWarnings(String atc, Allergy[] allergy) {
        if (atc != null && !atc.isEmpty()) {
            missingAllergyWarnings.put(atc, allergy);
        }
    }

    public void addToWorkingAllergyWarnings(String atc, RxAllergyWarningWorker worker) {
        if (atc != null && !atc.isEmpty()) {
            workingAllergyWarnings.put(atc, worker);
        }
    }

    public void removeFromWorkingAllergyWarnings(String atc) {
        if (atc != null && !atc.isEmpty()) {
            workingAllergyWarnings.remove(atc);
        }
    }


    public Allergy[] getAllergyWarnings(LoggedInInfo loggedInInfo, String atccode) {
        Allergy[] allergies = null;

        //Check to see if Allergy checking property is on and if atccode is not null and if atccode is not "" or "null"

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
