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
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import ca.openosp.openo.prescript.pageUtil.RxSessionBean;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.logging.log4j.Logger;
import ca.openosp.openo.commn.dao.DrugDao;
import ca.openosp.openo.commn.dao.FavoriteDao;
import ca.openosp.openo.commn.dao.PrescriptionDao;
import ca.openosp.openo.commn.model.Drug;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.providers.data.ProSignatureData;
import ca.openosp.openo.prescript.util.RxUtil;
import ca.openosp.openo.util.ConversionUtils;
import ca.openosp.openo.util.DateUtils;

/**
 * Comprehensive data access layer for prescription management.
 *
 * This class serves as the central data repository for all prescription-related operations
 * in the EMR system. It manages the complete prescription lifecycle from creation through
 * refills, discontinuation, and archival. The class provides extensive functionality for
 * prescription CRUD operations, search capabilities, refill management, and prescription
 * formatting for various output formats.
 *
 * Key responsibilities include:
 * - Prescription creation and modification
 * - Refill and repeat prescription management
 * - Prescription search by patient, provider, or drug
 * - Favorite prescription management for providers
 * - Prescription formatting for printing and display
 * - Audit trail maintenance for prescription changes
 * - Integration with drug database and interaction checking
 *
 * The class uses an inner Prescription class to model prescription data with support
 * for complex dosing instructions, custom medications, and provider-specific preferences.
 * All prescription operations maintain audit trails for regulatory compliance.
 *
 * @since 2006-03-01
 */
public class RxPrescriptionData {

    private static final Logger logger = MiscUtils.getLogger();

    /**
     * Formats special instructions into a single line for prescription output.
     *
     * Converts multi-line special instructions into a semicolon-delimited single line
     * suitable for prescription labels and compact display. Each line is trimmed of
     * whitespace and joined with semicolons.
     *
     * @param special String multi-line special instructions text
     * @return String formatted single-line instructions, empty string if null
     */
    public static String getFullOutLine(String special) {
        String ret = "";
        if (special != null) {
            if (special.length() > 0) {
                int i;
                String[] arr = special.split("\n");
                for (i = 0; i < arr.length; i++) {
                    ret += arr[i].trim();
                    if (i < arr.length - 1) {
                        ret += "; ";
                    }
                }
            }
        } else {
            logger.warn("Drugs special field was null, this means nothing will print.");
        }

        return ret;
    }

    /**
     * Retrieves a complete prescription by drug ID.
     *
     * Loads a prescription from the database and populates all fields including
     * dosing instructions, dates, refill information, and special instructions.
     * The method handles null duration values and converts them to empty strings.
     *
     * @param drugId int unique identifier for the prescription
     * @return Prescription fully populated prescription object
     */
    public Prescription getPrescription(int drugId) {

        DrugDao drugDao = (DrugDao) SpringUtils.getBean(DrugDao.class);
        Drug drug = drugDao.find(drugId);

        Prescription prescription = new Prescription(drugId, drug.getProviderNo(), drug.getDemographicId());
        prescription.setRxCreatedDate(drug.getCreateDate());
        prescription.setRxDate(drug.getRxDate());
        prescription.setEndDate(drug.getEndDate());
        prescription.setWrittenDate(drug.getWrittenDate());
        prescription.setBrandName(drug.getBrandName());
        prescription.setGCN_SEQNO(drug.getGcnSeqNo());
        prescription.setCustomName(drug.getCustomName());
        prescription.setTakeMin(drug.getTakeMin());
        prescription.setTakeMax(drug.getTakeMax());
        prescription.setFrequencyCode(drug.getFreqCode());

        // Handle null duration values
        String dur = drug.getDuration();
        if (StringUtils.isBlank(dur) || dur.equalsIgnoreCase("null")) dur = "";
        prescription.setDuration(dur);

        prescription.setDurationUnit(drug.getDurUnit());
        prescription.setQuantity(drug.getQuantity());
        prescription.setRepeat(drug.getRepeat());
        prescription.setLastRefillDate(drug.getLastRefillDate());
        prescription.setNosubs(drug.isNoSubs());
        prescription.setPrn(drug.isPrn());
        prescription.setSpecial(drug.getSpecial());
        prescription.setGenericName(drug.getGenericName());
        prescription.setAtcCode(drug.getAtc());
        prescription.setScript_no(String.valueOf(drug.getScriptNo()));
        prescription.setRegionalIdentifier(drug.getRegionalIdentifier());
        prescription.setUnit(drug.getUnit());
        prescription.setUnitName(drug.getUnitName());
        prescription.setMethod(drug.getMethod());
        prescription.setRoute(drug.getRoute());
        prescription.setDrugForm(drug.getDrugForm());
        prescription.setCustomInstr(drug.isCustomInstructions());
        prescription.setDosage(drug.getDosage());
        prescription.setLongTerm(drug.getLongTerm());
        prescription.setShortTerm(drug.getShortTerm());
        prescription.setCustomNote(drug.isCustomNote());
        prescription.setPastMed(drug.getPastMed());
        prescription.setDispenseInternal(drug.getDispenseInternal());
        prescription.setStartDateUnknown(drug.getStartDateUnknown());
        prescription.setComment(drug.getComment());
        prescription.setPatientCompliance(drug.getPatientCompliance());
        prescription.setOutsideProviderName(drug.getOutsideProviderName());
        prescription.setOutsideProviderOhip(drug.getOutsideProviderOhip());
        prescription.setSpecialInstruction(drug.getSpecialInstruction());
        prescription.setPickupDate(drug.getPickUpDateTime());
        prescription.setPickupTime(drug.getPickUpDateTime());
        prescription.setProtocol(drug.getProtocol());
        prescription.setPriorRxProtocol(drug.getPriorRxProtocol());
        prescription.setETreatmentType(drug.getETreatmentType());
        prescription.setRxStatus(drug.getRxStatus());
        if (drug.getDispenseInterval() != null) prescription.setDispenseInterval(drug.getDispenseInterval());
        if (drug.getRefillDuration() != null) prescription.setRefillDuration(drug.getRefillDuration());
        if (drug.getRefillQuantity() != null) prescription.setRefillQuantity(drug.getRefillQuantity());

        if (prescription.getSpecial() == null || prescription.getSpecial().length() <= 6) {
            logger.warn("I strongly suspect something is wrong, either special is null or it appears to not contain anything useful. drugId=" + drugId + ", drug.special=" + prescription.getSpecial());
            logger.warn("data from db is : " + drug.getSpecial());
        }
        prescription.setDispenseInternal(drug.getDispenseInternal());
        prescription.setPharmacyId(drug.getPharmacyId());
        return prescription;
    }

    /**
     * Creates a new blank prescription in memory.
     *
     * Initializes a prescription with provider and patient information but no
     * drug details. The prescription exists only in memory until saved.
     *
     * @param providerNo String provider identifier who is prescribing
     * @param demographicNo int patient demographic number
     * @return Prescription new empty prescription object
     */
    public Prescription newPrescription(String providerNo, int demographicNo) {
        // Create new prescription (only in memory)
        return new Prescription(0, providerNo, demographicNo);
    }

    /**
     * Creates a new prescription from a provider's favorite template.
     *
     * Populates a new prescription with all details from a saved favorite,
     * including drug information, dosing instructions, and special notes.
     * Sets current date as prescription and written dates.
     *
     * @param providerNo String provider identifier who is prescribing
     * @param demographicNo int patient demographic number
     * @param favorite Favorite template containing prescription details
     * @return Prescription new prescription populated from favorite
     */
    public Prescription newPrescription(String providerNo, int demographicNo, Favorite favorite) {
        // Create new prescription from favorite (only in memory)
        Prescription prescription = new Prescription(0, providerNo, demographicNo);

        prescription.setRxDate(RxUtil.Today());
        prescription.setWrittenDate(RxUtil.Today());
        prescription.setEndDate(null);
        prescription.setBrandName(favorite.getBN());
        prescription.setGCN_SEQNO(favorite.getGCN_SEQNO());
        prescription.setCustomName(favorite.getCustomName());
        prescription.setTakeMin(favorite.getTakeMin());
        prescription.setTakeMax(favorite.getTakeMax());
        prescription.setFrequencyCode(favorite.getFrequencyCode());
        prescription.setDuration(favorite.getDuration());
        prescription.setDurationUnit(favorite.getDurationUnit());
        prescription.setQuantity(favorite.getQuantity());
        prescription.setRepeat(favorite.getRepeat());
        prescription.setNosubs(favorite.getNosubs());
        prescription.setPrn(favorite.getPrn());
        prescription.setSpecial(favorite.getSpecial());
        prescription.setGenericName(favorite.getGN());
        prescription.setAtcCode(favorite.getAtcCode());
        prescription.setRegionalIdentifier(favorite.getRegionalIdentifier());
        prescription.setUnit(favorite.getUnit());
        prescription.setUnitName(favorite.getUnitName());
        prescription.setMethod(favorite.getMethod());
        prescription.setRoute(favorite.getRoute());
        prescription.setDrugForm(favorite.getDrugForm());
        prescription.setCustomInstr(favorite.getCustomInstr());
        prescription.setDosage(favorite.getDosage());

        return prescription;
    }

    /**
     * Creates a new prescription by copying an existing prescription.
     *
     * Used for re-prescribing medications, this method copies all drug details
     * from an existing prescription while setting new prescription and written
     * dates. The new prescription maintains all dosing and special instructions
     * from the original.
     *
     * @param providerNo String provider identifier who is prescribing
     * @param demographicNo int patient demographic number
     * @param rePrescribe Prescription existing prescription to copy
     * @return Prescription new prescription with copied details
     */
    public Prescription newPrescription(String providerNo, int demographicNo, Prescription rePrescribe) {
        // Create new prescription
        Prescription prescription = new Prescription(0, providerNo, demographicNo);

        prescription.setRxDate(RxUtil.Today());
        prescription.setWrittenDate(RxUtil.Today());
        prescription.setEndDate(null);
        prescription.setBrandName(rePrescribe.getBrandName());
        prescription.setGCN_SEQNO(rePrescribe.getGCN_SEQNO());
        prescription.setCustomName(rePrescribe.getCustomName());
        prescription.setTakeMin(rePrescribe.getTakeMin());
        prescription.setTakeMax(rePrescribe.getTakeMax());
        prescription.setFrequencyCode(rePrescribe.getFrequencyCode());
        prescription.setDuration(rePrescribe.getDuration());
        prescription.setDurationUnit(rePrescribe.getDurationUnit());
        prescription.setQuantity(rePrescribe.getQuantity());
        prescription.setRepeat(rePrescribe.getRepeat());
        prescription.setLastRefillDate(rePrescribe.getLastRefillDate());
        prescription.setNosubs(rePrescribe.getNosubs());
        prescription.setPrn(rePrescribe.getPrn());
        prescription.setSpecial(rePrescribe.getSpecial());
        prescription.setGenericName(rePrescribe.getGenericName());
        prescription.setAtcCode(rePrescribe.getAtcCode());
        prescription.setScript_no(rePrescribe.getScript_no());
        prescription.setRegionalIdentifier(rePrescribe.getRegionalIdentifier());
        prescription.setUnit(rePrescribe.getUnit());
        prescription.setUnitName(rePrescribe.getUnitName());
        prescription.setMethod(rePrescribe.getMethod());
        prescription.setRoute(rePrescribe.getRoute());
        prescription.setDrugForm(rePrescribe.getDrugForm());
        prescription.setCustomInstr(rePrescribe.getCustomInstr());
        prescription.setDosage(rePrescribe.getDosage());
        prescription.setLongTerm(rePrescribe.getLongTerm());
        prescription.setShortTerm(rePrescribe.getShortTerm());
        prescription.setCustomNote(rePrescribe.isCustomNote());
        prescription.setPastMed(rePrescribe.getPastMed());
        prescription.setDispenseInternal(rePrescribe.isDispenseInternal());
        prescription.setPatientCompliance(rePrescribe.getPatientCompliance());
        prescription.setOutsideProviderName(rePrescribe.getOutsideProviderName());
        prescription.setOutsideProviderOhip(rePrescribe.getOutsideProviderOhip());
        prescription.setSpecialInstruction(rePrescribe.getSpecialInstruction());
        prescription.setPickupDate(rePrescribe.getPickupDate());
        prescription.setPickupTime(rePrescribe.getPickupTime());
        prescription.setETreatmentType(rePrescribe.getETreatmentType());
        prescription.setRxStatus(rePrescribe.getRxStatus());
        if (rePrescribe.getDispenseInterval() != null)
            prescription.setDispenseInterval(rePrescribe.getDispenseInterval());
        if (rePrescribe.getRefillDuration() != null) prescription.setRefillDuration(rePrescribe.getRefillDuration());
        if (rePrescribe.getRefillQuantity() != null) prescription.setRefillQuantity(rePrescribe.getRefillQuantity());
        prescription.setDrugReferenceId(rePrescribe.getDrugId());
        prescription.setDispenseInternal(rePrescribe.getDispenseInternal());
        prescription.setProtocol(rePrescribe.getProtocol());
        prescription.setPriorRxProtocol(rePrescribe.getPriorRxProtocol());
        return prescription;
    }

    /**
     * Retrieves all prescriptions for a patient.
     *
     * Returns all prescriptions (active and inactive) for the specified patient,
     * ordered by their display position. This includes current medications,
     * past medications, and discontinued drugs.
     *
     * @param demographicNo int patient demographic number
     * @return Prescription[] array of all patient prescriptions
     */
    public Prescription[] getPrescriptionsByPatient(int demographicNo) {
        List<Prescription> lst = new ArrayList<Prescription>();

        DrugDao dao = SpringUtils.getBean(DrugDao.class);
        for (Drug drug : dao.findByDemographicIdOrderByPosition(demographicNo, false)) {
            Prescription p = toPrescription(drug, demographicNo);
            lst.add(p);
        }

        return lst.toArray(new Prescription[lst.size()]);
    }

    /**
     * Retrieves prescriptions for export purposes.
     *
     * Similar to getPrescriptionsByPatient but optimized for data export,
     * potentially including additional fields or different filtering criteria
     * suitable for external systems or reporting.
     *
     * @param demographicNo int patient demographic number
     * @return Prescription[] array of prescriptions formatted for export
     */
    public Prescription[] getPrescriptionsByPatientForExport(int demographicNo) {
        List<Prescription> lst = new ArrayList<Prescription>();

        DrugDao dao = SpringUtils.getBean(DrugDao.class);
        for (Drug drug : dao.findByDemographicIdOrderByPositionForExport(demographicNo, null)) {
            Prescription p = toPrescription(drug, demographicNo);
            lst.add(p);
        }

        return lst.toArray(new Prescription[lst.size()]);
    }

    /**
     * Converts a Drug entity to a Prescription object.
     *
     * Maps all fields from the database Drug entity to the Prescription
     * data transfer object. Handles null values appropriately and ensures
     * all prescription fields are properly populated.
     *
     * @param drug Drug entity from database
     * @param demographicNo int patient demographic number
     * @return Prescription fully populated prescription object
     */
    public Prescription toPrescription(Drug drug, int demographicNo) {
        Prescription p = new Prescription(drug.getId(), drug.getProviderNo(), demographicNo);
        p.setRxCreatedDate(drug.getCreateDate());
        p.setRxDate(drug.getRxDate());
        p.setEndDate(drug.getEndDate());
        p.setWrittenDate(drug.getWrittenDate());
        p.setBrandName(drug.getBrandName());
        p.setGCN_SEQNO(drug.getGcnSeqNo());
        p.setCustomName(drug.getCustomName());
        p.setTakeMin(drug.getTakeMin());
        p.setTakeMax(drug.getTakeMax());
        p.setFrequencyCode(drug.getFreqCode());
        p.setDuration(drug.getDuration());
        p.setDurationUnit(drug.getDuration());
        p.setQuantity(drug.getQuantity());
        p.setRepeat(drug.getRepeat());
        p.setLastRefillDate(drug.getLastRefillDate());
        p.setNosubs(drug.isNoSubs());
        p.setPrn(drug.isPrn());
        p.setSpecial(drug.getSpecial());
        p.setSpecialInstruction(drug.getSpecialInstruction());
        p.setArchived(String.valueOf(drug.isArchived()));
        p.setGenericName(drug.getGenericName());
        p.setAtcCode(drug.getAtc());
        p.setScript_no(String.valueOf(drug.getScriptNo()));
        p.setRegionalIdentifier(drug.getRegionalIdentifier());
        p.setUnit(drug.getUnit());
        p.setUnitName(drug.getUnitName());
        p.setMethod(drug.getMethod());
        p.setRoute(drug.getRoute());
        p.setDrugForm(drug.getDrugForm());
        p.setCustomInstr(drug.isCustomInstructions());
        p.setDosage(drug.getDosage());
        p.setLongTerm(drug.getLongTerm());
        p.setShortTerm(drug.getShortTerm());
        p.setCustomNote(drug.isCustomNote());
        p.setPastMed(drug.getPastMed());
        p.setStartDateUnknown(drug.getStartDateUnknown());
        p.setComment(drug.getComment());
        p.setPatientCompliance(drug.getPatientCompliance());
        p.setOutsideProviderName(drug.getOutsideProviderName());
        p.setOutsideProviderOhip(drug.getOutsideProviderOhip());
        p.setPickupDate(drug.getPickUpDateTime());
        p.setPickupTime(drug.getPickUpDateTime());
        p.setProtocol(drug.getProtocol());
        p.setPriorRxProtocol(drug.getPriorRxProtocol());
        p.setETreatmentType(drug.getETreatmentType());
        p.setRxStatus(drug.getRxStatus());

        // Handle optional fields that may be null
        if (drug.getDispenseInterval() != null) p.setDispenseInterval(drug.getDispenseInterval());
        if (drug.getRefillDuration() != null) p.setRefillDuration(drug.getRefillDuration());
        if (drug.getRefillQuantity() != null) p.setRefillQuantity(drug.getRefillQuantity());

        p.setHideCpp(drug.getHideFromCpp());
        p.setPharmacyId(drug.getPharmacyId());
        if (drug.isNonAuthoritative() != null) p.setNonAuthoritative(drug.isNonAuthoritative());
        p.discontinued = drug.isDiscontinued();
        p.setArchivedDate(drug.getArchivedDate());
        return p;
    }

    /**
     * Retrieves prescriptions by ATC code for a patient.
     *
     * Finds all prescriptions for a specific drug class identified by ATC
     * (Anatomical Therapeutic Chemical) code. Useful for finding all medications
     * in a therapeutic class.
     *
     * @param demographicNo int patient demographic number
     * @param atc String ATC code to search for
     * @return Prescription[] array of prescriptions matching the ATC code
     */
    public Prescription[] getPrescriptionScriptsByPatientATC(int demographicNo, String atc) {
        List<Prescription> lst = new ArrayList<Prescription>();

        DrugDao dao = SpringUtils.getBean(DrugDao.class);
        for (Drug drug : dao.findByDemographicIdAndAtc(demographicNo, atc))
            lst.add(toPrescription(drug, demographicNo));

        return lst.toArray(new Prescription[lst.size()]);
    }

    /**
     * Retrieves prescriptions by regional identifier (e.g., DIN).
     *
     * Finds all prescriptions for a specific drug identified by its regional
     * identifier such as DIN (Drug Identification Number) in Canada. Does not
     * return custom drugs as they don't have regional identifiers.
     *
     * @param demographicNo int patient demographic number
     * @param regionalIdentifier String regional drug identifier (e.g., DIN)
     * @return Prescription[] array of prescriptions with the specified identifier
     */
    public Prescription[] getPrescriptionScriptsByPatientRegionalIdentifier(int demographicNo, String regionalIdentifier) {
        List<Prescription> lst = new ArrayList<Prescription>();
        DrugDao dao = SpringUtils.getBean(DrugDao.class);

        for (Drug drug : dao.findByDemographicIdAndRegion(demographicNo, regionalIdentifier)) {
            Prescription p = toPrescription(drug, demographicNo);
            lst.add(p);
        }
        return lst.toArray(new Prescription[lst.size()]);
    }

    /**
     * Retrieves the most recent prescription for a specific drug.
     *
     * Finds the latest prescription instance for a particular drug ID,
     * useful for checking the most recent prescribing details.
     *
     * @param demographicNo int patient demographic number
     * @param drugId String drug identifier to search for
     * @return Prescription most recent prescription, null if not found
     */
    public Prescription getLatestPrescriptionScriptByPatientDrugId(int demographicNo, String drugId) {
        DrugDao dao = SpringUtils.getBean(DrugDao.class);
        List<Drug> drugs = dao.findByDemographicIdAndDrugId(demographicNo, Integer.parseInt(drugId));
        if (drugs.isEmpty()) return null;
        return toPrescription(drugs.get(0), demographicNo);
    }

    /**
     * Retrieves prescriptions that have been printed.
     *
     * Returns only prescriptions that have entries in both the drugs and prescription
     * tables, indicating they have been printed at least once. Includes print count
     * and print date information.
     *
     * @param demographicNo int patient demographic number
     * @return Prescription[] array of printed prescriptions
     */
    public Prescription[] getPrescriptionScriptsByPatient(int demographicNo) {
        List<Prescription> lst = new ArrayList<Prescription>();
        DrugDao dao = SpringUtils.getBean(DrugDao.class);
        for (Object[] pair : dao.findDrugsAndPrescriptions(demographicNo)) {
            Drug drug = (Drug) pair[0];
            ca.openosp.openo.commn.model.Prescription rx = (ca.openosp.openo.commn.model.Prescription) pair[1];
            MiscUtils.getLogger().debug("Looking at drug " + drug + " and prescript " + rx);
            lst.add(toPrescription(demographicNo, drug, rx));
        }
        return lst.toArray(new Prescription[lst.size()]);
    }

    /**
     * Converts drug and prescription entities to a complete Prescription object.
     *
     * Combines data from both Drug and Prescription database entities,
     * including print history and digital signature information.
     *
     * @param demographicNo int patient demographic number
     * @param drug Drug entity with medication details
     * @param rx Prescription entity with print information
     * @return Prescription combined prescription object
     */
    private Prescription toPrescription(int demographicNo, Drug drug, ca.openosp.openo.commn.model.Prescription rx) {
        Prescription prescription = toPrescription(drug, demographicNo);
        if (!rx.isReprinted()) prescription.setNumPrints(1);
        else prescription.setNumPrints(rx.getReprintCount() + 1);

        prescription.setPrintDate(rx.getDatePrinted());
        prescription.setDatesReprinted(rx.getDatesReprinted());
		prescription.setDigitalSignatureId(rx.getDigitalSignatureId());
        return prescription;
    }

    /**
     * Retrieves all prescriptions with a specific script number.
     *
     * Script numbers group related prescriptions together (e.g., multiple drugs
     * prescribed at the same time). This method returns all prescriptions sharing
     * the same script number.
     *
     * @param script_no int script number to search for
     * @param demographicNo int patient demographic number
     * @return List<Prescription> prescriptions with the specified script number
     */
    public List<Prescription> getPrescriptionsByScriptNo(int script_no, int demographicNo) {
        List<Prescription> lst = new ArrayList<Prescription>();
        DrugDao dao = SpringUtils.getBean(DrugDao.class);
        for (Object[] pair : dao.findDrugsAndPrescriptionsByScriptNumber(script_no)) {
            Drug drug = (Drug) pair[0];
            ca.openosp.openo.commn.model.Prescription rx = (ca.openosp.openo.commn.model.Prescription) pair[1];

            lst.add(toPrescription(demographicNo, drug, rx));
        }
        return lst;
    }

    /**
     * Retrieves active prescriptions excluding deleted ones.
     *
     * Returns current prescriptions that are not archived, deleted, or discontinued.
     * Long-term medications are always included regardless of status. This is the
     * primary method for displaying a patient's current medication list.
     *
     * @param demographicNo int patient demographic number
     * @return Prescription[] array of active prescriptions
     */
    public Prescription[] getPrescriptionsByPatientHideDeleted(int demographicNo) {
        List<Prescription> lst = new ArrayList<Prescription>();
        DrugDao dao = SpringUtils.getBean(DrugDao.class);

        for (Drug drug : dao.findByDemographicId(demographicNo)) {
            if ((drug.isCurrent() && !drug.isArchived() && !drug.isDeleted() && !drug.isDiscontinued()) || drug.isLongTerm()) {
                lst.add(toPrescription(drug, demographicNo));
            }
        }
        return lst.toArray(new Prescription[lst.size()]);
    }

    /**
     * Retrieves only currently active prescriptions.
     *
     * Returns prescriptions that are current and not archived or discontinued.
     * More restrictive than getPrescriptionsByPatientHideDeleted as it doesn't
     * automatically include long-term medications if they're discontinued.
     *
     * @param demographicNo int patient demographic number
     * @return Prescription[] array of currently active prescriptions
     */
    public Prescription[] getActivePrescriptionsByPatient(int demographicNo) {
        List<Prescription> lst = new ArrayList<Prescription>();
        DrugDao dao = SpringUtils.getBean(DrugDao.class);

        for (Drug drug : dao.findByDemographicId(demographicNo)) {
            Prescription p = toPrescription(drug, demographicNo);
            if (!p.isArchived() && !p.isDiscontinued() && p.isCurrent()) {
                lst.add(p);
            }
        }
        return lst.toArray(new Prescription[lst.size()]);
    }

    /**
     * Gets unique ATC codes for patient's current medications.
     *
     * Returns a list of distinct ATC codes for all current medications.
     * Used for drug interaction checking and therapeutic class analysis.
     * Only includes valid ATC codes (non-null and properly formatted).
     *
     * @param demographicNo int patient demographic number
     * @return Vector unique ATC codes for current medications
     */
    public Vector getCurrentATCCodesByPatient(int demographicNo) {
        List<String> result = new ArrayList<String>();

        Prescription[] p = getPrescriptionsByPatientHideDeleted(demographicNo);
        for (int i = 0; i < p.length; i++) {
            if (p[i].isCurrent()) {
                if (!result.contains(p[i].getAtcCode())) {
                    if (p[i].isValidAtcCode()) result.add(p[i].getAtcCode());
                }
            }
        }
        return new Vector(result);
    }

    /**
     * Gets unique regional identifiers for patient's current medications.
     *
     * Returns distinct regional identifiers (e.g., DINs) for all current medications.
     * Filters out null and empty identifiers. Used for regulatory reporting and
     * drug identification.
     *
     * @param demographicNo int patient demographic number
     * @return List<String> unique regional identifiers for current medications
     */
    public List<String> getCurrentRegionalIdentifiersCodesByPatient(int demographicNo) {
        List<String> result = new ArrayList<String>();

        Prescription[] p = getPrescriptionsByPatientHideDeleted(demographicNo);
        for (int i = 0; i < p.length; i++) {
            if (p[i].isCurrent()) {
                if (!result.contains(p[i].getRegionalIdentifier())) {
                    if (p[i].getRegionalIdentifier() != null && p[i].getRegionalIdentifier().trim().length() != 0) {
                        result.add(p[i].getRegionalIdentifier());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets unique prescriptions avoiding duplicates.
     *
     * Returns the most recent prescription for each unique medication based on
     * GCN (Generic Code Number) for formulary drugs or custom name for custom drugs.
     * Deleted prescriptions are excluded. Useful for medication reconciliation
     * and current medication lists.
     *
     * @param demographicNo int patient demographic number
     * @return Prescription[] array of unique prescriptions, one per medication
     */
    public Prescription[] getUniquePrescriptionsByPatient(int demographicNo) {
        List<Prescription> result = new ArrayList<Prescription>();
        DrugDao dao = SpringUtils.getBean(DrugDao.class);

        List<Drug> drugList = dao.findByDemographicId(demographicNo);

        // Sort by ID descending to get most recent first
        Collections.sort(drugList, new Drug.ComparatorIdDesc());

        for (Drug drug : drugList) {

            if (drug.isDeleted())
                continue;

            boolean isCustomName = true;

            // Check if this drug is already in the result list
            for (Prescription p : result) {
                if (p.getGCN_SEQNO() == drug.getGcnSeqNo()) {
                    // Not custom - safe GCN match
                    if (p.getGCN_SEQNO() != 0)
                        isCustomName = false;
                    // Custom drug - check by name
                    else if (p.getCustomName() != null && drug.getCustomName() != null)
                        isCustomName = !p.getCustomName().equals(drug.getCustomName());

                }
            }

            if (isCustomName) {
                logger.debug("ADDING PRESCRIPTION " + drug.getId());
                Prescription p = toPrescription(drug, demographicNo);


                p.setPosition(drug.getPosition());
                result.add(p);
            }
        }
        return result.toArray(new Prescription[result.size()]);
    }

    /**
     * Retrieves provider's favorite prescriptions.
     *
     * Gets all saved prescription templates for a provider. Favorites allow
     * providers to quickly prescribe commonly used medications with pre-filled
     * dosing instructions.
     *
     * @param providerNo String provider identifier
     * @return Favorite[] array of provider's favorite prescriptions
     */
    public Favorite[] getFavorites(String providerNo) {
        FavoriteDao dao = SpringUtils.getBean(FavoriteDao.class);

        List<Favorite> result = new ArrayList<Favorite>();

        for (ca.openosp.openo.commn.model.Favorite f : dao.findByProviderNo(providerNo))
            result.add(toFavorite(f));

        return result.toArray(new Favorite[result.size()]);
    }

    private Favorite toFavorite(ca.openosp.openo.commn.model.Favorite f) {
        Favorite result = new Favorite(f.getId(), f.getProviderNo(), f.getName(), f.getBn(), (int) f.getGcnSeqno(), f.getCustomName(), f.getTakeMin(), f.getTakeMax(), f.getFrequencyCode(), f.getDuration(), f.getDurationUnit(), f.getQuantity(), f.getRepeat(), f.isNosubs(), f.isPrn(), f.getSpecial(), f.getGn(), f.getAtc(), f.getRegionalIdentifier(), f.getUnit(), f.getUnitName(), f.getMethod(), f.getRoute(), f.getDrugForm(), f.isCustomInstructions(), f.getDosage());
        return result;
    }

    public Favorite getFavorite(int favoriteId) {
        FavoriteDao dao = SpringUtils.getBean(FavoriteDao.class);
        ca.openosp.openo.commn.model.Favorite result = dao.find(favoriteId);
        if (result == null) return null;
        return toFavorite(result);
    }

    /**
     * Deletes a provider's favorite prescription template.
     *
     * Permanently removes a favorite prescription from the provider's list.
     *
     * @param favoriteId int unique identifier of the favorite to delete
     * @return boolean true if deletion was successful
     */
    public boolean deleteFavorite(int favoriteId) {
        FavoriteDao dao = SpringUtils.getBean(FavoriteDao.class);
        return dao.remove(favoriteId);
    }

    /**
     * Saves a prescription script containing one or more medications.
     *
     * Creates a prescription record in the database with full text view for printing.
     * The text view includes provider information, patient demographics, and all
     * prescribed medications. This creates an audit trail and historical record
     * of exactly what was prescribed.
     *
     * The method generates a formatted prescription text including:
     * - Provider name, clinic details, and contact information
     * - Patient name and address
     * - Date of prescription
     * - Full details of all medications in the prescription
     *
     * @param loggedInInfo LoggedInInfo security context and session information
     * @param bean RxSessionBean containing the prescription session data and stashed medications
     * @return String the prescription ID for linking to individual drug records
     */
    public String saveScript(LoggedInInfo loggedInInfo, RxSessionBean bean) {
        // Prescription table structure for reference
        String provider_no = bean.getProviderNo();
        int demographic_no = bean.getDemographicNo();

        Date today = RxUtil.Today();

        StringBuilder textView = new StringBuilder();

        // Create full text view of prescription for printing
        RxPatientData.Patient patient = null;
        RxProviderData.Provider provider = null;
        try {
            patient = RxPatientData.getPatient(loggedInInfo, demographic_no);
            provider = new RxProviderData().getProvider(provider_no);
        } catch (Exception e) {
            logger.error("unexpected error", e);
        }
        ProSignatureData sig = new ProSignatureData();
        boolean hasSig = sig.hasSignature(bean.getProviderNo());
        String doctorName = "";
        if (hasSig) {
            doctorName = sig.getSignature(bean.getProviderNo());
        } else {
            doctorName = (provider.getFirstName() + ' ' + provider.getSurname());
        }

        textView.append(doctorName + "\n");
        textView.append(provider.getClinicName() + "\n");
        textView.append(provider.getClinicAddress() + "\n");
        textView.append(provider.getClinicCity() + "\n");
        textView.append(provider.getClinicPostal() + "\n");
        textView.append(provider.getClinicPhone() + "\n");
        textView.append(provider.getClinicFax() + "\n");
        textView.append(patient.getFirstName() + " " + patient.getSurname() + "\n");
        textView.append(patient.getAddress() + "\n");
        textView.append(patient.getCity() + " " + patient.getPostal() + "\n");
        textView.append(patient.getPhone() + "\n");
        textView.append(RxUtil.DateToString(today, "MMMM d, yyyy") + "\n");

        String txt;
        for (int i = 0; i < bean.getStashSize(); i++) {
            Prescription rx = bean.getStashItem(i);

            String fullOutLine = rx.getFullOutLine();
            if (fullOutLine == null || fullOutLine.length() < 6) {
                logger.warn("Drug full outline appears to be null or empty : " + fullOutLine);
            }

            txt = fullOutLine.replaceAll(";", "\n");
            textView.append("\n" + txt);
        }
        // Save prescription to database

        ca.openosp.openo.commn.model.Prescription rx = new ca.openosp.openo.commn.model.Prescription();
        rx.setProviderNo(provider_no);
        rx.setDemographicId(demographic_no);
        rx.setDatePrescribed(today);
        rx.setDatePrinted(today);
        rx.setTextView(textView.toString());

        PrescriptionDao dao = SpringUtils.getBean(PrescriptionDao.class);
        dao.persist(rx);
        return rx.getId().toString();
    }

    public int setScriptComment(String scriptNo, String comment) {
        PrescriptionDao dao = SpringUtils.getBean(PrescriptionDao.class);
        return dao.updatePrescriptionsByScriptNo(Integer.parseInt(scriptNo), comment);
    }

    public String getScriptComment(String scriptNo) {
        PrescriptionDao dao = SpringUtils.getBean(PrescriptionDao.class);
        ca.openosp.openo.commn.model.Prescription p = dao.find(ConversionUtils.fromIntString(scriptNo));
        if (p == null) return null;

        return p.getComments();
    }

    /**
     * Comprehensive prescription data model.
     *
     * This inner class represents a complete prescription with all clinical, administrative,
     * and regulatory information. It supports both regular drug prescriptions (using brand
     * names and GCN codes) and custom medications. The class includes extensive fields for
     * dosing instructions, refill management, compliance tracking, and audit trails.
     *
     * Key features:
     * - Support for both branded and custom medications
     * - Complex dosing instructions with min/max ranges
     * - Refill and repeat prescription tracking
     * - Provider and pharmacy information
     * - Audit fields for creation, modification, and printing
     * - Special instructions and patient-specific notes
     * - Integration with drug databases via ATC codes and regional identifiers
     *
     * The class maintains compatibility with various provincial requirements and
     * supports features like controlled substance tracking, compliance monitoring,
     * and prescription protocol management.
     */
    public static class Prescription {

        /**
         * Unique database identifier for this prescription.
         */
        int drugId;

        /**
         * Provider number of the prescribing physician.
         */
        String providerNo;

        /**
         * Patient demographic number.
         */
        int demographicNo;

        /**
         * Random ID for temporary prescription identification.
         */
        long randomId = 0;

        /**
         * Date when prescription was created in the system.
         */
        java.util.Date rxCreatedDate = null;

        /**
         * Date when prescription starts (prescription date).
         */
        java.util.Date rxDate = null;

        /**
         * Date when prescription ends.
         */
        java.util.Date endDate = null;

        /**
         * Date when prescription was picked up by patient.
         */
        java.util.Date pickupDate = null;

        /**
         * Time when prescription was picked up.
         */
        java.util.Date pickupTime = null;

        /**
         * Date when prescription was written by provider.
         */
        java.util.Date writtenDate = null;

        /**
         * Format string for written date display.
         */
        String writtenDateFormat = null;

        /**
         * Format string for prescription date display.
         */
        String rxDateFormat = null;

        /**
         * Date when prescription was last printed.
         */
        java.util.Date printDate = null;

        /**
         * Number of times prescription has been printed.
         */
        int numPrints = 0;

        /**
         * Brand name for regular drug prescriptions.
         */
        String BN = null;

        /**
         * Generic Code Number sequence for drug identification.
         */
        int GCN_SEQNO = 0;

        /**
         * Custom medication name for non-formulary drugs.
         */
        String customName = null;

        /**
         * Minimum dosage amount per administration.
         */
        float takeMin = 0;

        /**
         * Maximum dosage amount per administration.
         */
        float takeMax = 0;

        /**
         * Frequency code (e.g., BID, TID, QID).
         */
        String frequencyCode = null;

        /**
         * Duration of treatment.
         */
        String duration = null;

        /**
         * Unit for duration (days, weeks, months).
         */
        String durationUnit = null;

        /**
         * Quantity to dispense.
         */
        String quantity = null;

        /**
         * Number of repeats/refills allowed.
         */
        int repeat = 0;

        /**
         * Date of last refill.
         */
        java.util.Date lastRefillDate = null;

        /**
         * No substitution flag (dispense as written).
         */
        boolean nosubs = false;

        /**
         * PRN (as needed) flag.
         */
        boolean prn = false;

        /**
         * Long-term medication flag.
         */
        Boolean longTerm = null;

        /**
         * Short-term medication flag.
         */
        boolean shortTerm = false;

        /**
         * Past medication flag.
         */
        Boolean pastMed = null;

        /**
         * Flag indicating start date is unknown.
         */
        boolean startDateUnknown = false;

        /**
         * Patient compliance indicator.
         */
        Boolean patientCompliance = null;

        /**
         * Special instructions for prescription.
         */
        String special = null;

        /**
         * Generic drug name.
         */
        String genericName = null;

        /**
         * Archive status flag.
         */
        boolean archived = false;

        /**
         * ATC (Anatomical Therapeutic Chemical) classification code.
         */
        String atcCode = null;

        /**
         * Prescription script number.
         */
        String script_no = null;

        /**
         * Regional identifier (e.g., DIN in Canada).
         */
        String regionalIdentifier = null;

        /**
         * Administration method.
         */
        String method = null;

        /**
         * Dosage unit.
         */
        String unit = null;

        /**
         * Human-readable unit name.
         */
        String unitName = null;

        /**
         * Route of administration.
         */
        String route = null;

        /**
         * Drug form (tablet, capsule, liquid, etc.).
         */
        String drugForm = null;

        /**
         * Dosage strength.
         */
        String dosage = null;

        /**
         * Name of external provider if prescribed elsewhere.
         */
        String outsideProviderName = null;

        /**
         * OHIP number of external provider.
         */
        String outsideProviderOhip = null;
        boolean custom = false;
        private final String docType = "Rx";
        private boolean discontinued = false;//indicate if the prescript has isDisontinued before.
        private String lastArchDate = null;
        private String lastArchReason = null;
        private Date archivedDate;
        private boolean discontinuedLatest = false;
        String special_instruction = null;
        private boolean durationSpecifiedByUser = false;
        private boolean customNote = false;
        boolean nonAuthoritative = false;
        String eTreatmentType = null;
        private boolean hideCpp = false;
        String rxStatus = null;
        private Integer refillDuration = 0;
        private Integer refillQuantity = 0;
        private String dispenseInterval = "";
        private int position = 0;
        private String comment = null;

        private String drugFormList = "";
        private String datesReprinted = "";
        private boolean dispenseInternal = false;

        private List<String> policyViolations = new ArrayList<String>();

        private int drugReferenceId;

        private String drugReasonCode;
        private String drugReasonCodeSystem;

        private String protocol;
        private String priorRxProtocol;
        private Integer pharmacyId;

		private Integer digitalSignatureId;

		public Integer getDigitalSignatureId() {
			return digitalSignatureId;
		}

		public void setDigitalSignatureId(Integer digitalSignatureId) {
			this.digitalSignatureId = digitalSignatureId;
		}

        public String getDrugReasonCode() {
            return drugReasonCode;
        }

        public void setDrugReasonCode(String drugReasonCode) {
            this.drugReasonCode = drugReasonCode;
        }

        public String getDrugReasonCodeSystem() {
            return drugReasonCodeSystem;
        }

        public void setDrugReasonCodeSystem(String drugReasonCodeSystem) {
            this.drugReasonCodeSystem = drugReasonCodeSystem;
        }

        public List<String> getPolicyViolations() {
            return policyViolations;
        }

        public void setPolicyViolations(List<String> policyViolations) {
            this.policyViolations = policyViolations;
        }

        public void setDrugReferenceId(int drugId2) {
            this.drugReferenceId = drugId2;

        }

        public int getDrugReferenceId() {
            return drugReferenceId;
        }

        public boolean getStartDateUnknown() {
            return startDateUnknown;
        }

        public void setStartDateUnknown(boolean startDateUnknown) {
            this.startDateUnknown = startDateUnknown;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getDatesReprinted() {
            return datesReprinted;
        }

        public void setDatesReprinted(String datesReprinted) {
            this.datesReprinted = datesReprinted;
        }

        public String getDrugFormList() {
            return drugFormList;
        }

        public void setDrugFormList(String drugFormList) {
            this.drugFormList = drugFormList;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public boolean isHideCpp() {
            return hideCpp;
        }

        public void setHideCpp(boolean hideCpp) {
            this.hideCpp = hideCpp;
        }

        public String getETreatmentType() {
            return eTreatmentType;
        }

        public void setETreatmentType(String treatmentType) {
            eTreatmentType = treatmentType;
        }

        public String getRxStatus() {
            return rxStatus;
        }

        public void setRxStatus(String status) {
            rxStatus = status;
        }

        public boolean isCustomNote() {
            return customNote;
        }

        public void setCustomNote(boolean b) {
            customNote = b;
        }

        public boolean isMitte() {
            if (unitName != null && (unitName.equalsIgnoreCase("D") || unitName.equalsIgnoreCase("W") || unitName.equalsIgnoreCase("M") || unitName.equalsIgnoreCase("day") || unitName.equalsIgnoreCase("week") || unitName.equalsIgnoreCase("month") || unitName.equalsIgnoreCase("days") || unitName.equalsIgnoreCase("weeks") || unitName.equalsIgnoreCase("months") || unitName.equalsIgnoreCase("mo")))
                return true;
            else return false;
        }

        public boolean isDurationSpecifiedByUser() {
            return durationSpecifiedByUser;
        }

        public void setDurationSpecifiedByUser(boolean b) {
            this.durationSpecifiedByUser = b;
        }

        public String getSpecialInstruction() {
            return special_instruction;
        }

        public void setSpecialInstruction(String s) {
            special_instruction = s;
        }

        public boolean isLongTerm() {
            boolean trueFalse = Boolean.FALSE;
            if (longTerm != null) {
                trueFalse = longTerm;
            }
            return trueFalse;
        }

        public boolean isDiscontinuedLatest() {
            return this.discontinuedLatest;
        }

        public void setDiscontinuedLatest(boolean dl) {
            this.discontinuedLatest = dl;
        }

        public String getLastArchDate() {
            return this.lastArchDate;
        }

        public void setLastArchDate(String lastArchDate) {
            this.lastArchDate = lastArchDate;
        }

        public String getLastArchReason() {
            return this.lastArchReason;
        }

        public void setLastArchReason(String lastArchReason) {
            this.lastArchReason = lastArchReason;
        }

        public boolean isDiscontinued() {
            return this.discontinued;
        }

        public void setDiscontinued(boolean discon) {
            this.discontinued = discon;
        }

        public void setArchivedDate(Date ad) {
            this.archivedDate = ad;
        }

        public Date getArchivedDate() {
            return this.archivedDate;
        }

        // RxDrugData.GCN gcn = null;
        public Prescription(int drugId, String providerNo, int demographicNo) {
            this.drugId = drugId;
            this.providerNo = providerNo;
            this.demographicNo = demographicNo;
        }

        public long getRandomId() {
            return this.randomId;
        }

        public void setRandomId(long randomId) {
            this.randomId = randomId;
        }

        public int getNumPrints() {
            return this.numPrints;
        }

        public void setNumPrints(int numPrints) {
            this.numPrints = numPrints;
        }

        public java.util.Date getPrintDate() {
            return this.printDate;
        }

        public void setPrintDate(java.util.Date printDate) {
            this.printDate = printDate;
        }

        public void setScript_no(String script_no) {
            this.script_no = script_no;
        }

        public String getScript_no() {
            return this.script_no;
        }


        public String getGenericName() {
            return genericName;
        }

        public void setGenericName(String genericName) {
            this.genericName = genericName;
        }

        // ADDED BY JAY DEC 03 2002
        public boolean isArchived() {
            return this.archived;
        }

        public void setArchived(String tf) {
            this.archived = Boolean.parseBoolean(tf);
        }

        //////////////////////////////
        public int getDrugId() {
            return this.drugId;
        }

        public String getProviderNo() {
            return this.providerNo;
        }

        public int getDemographicNo() {
            return this.demographicNo;
        }

        public java.util.Date getRxDate() {
            return this.rxDate;
        }

        public void setRxDate(java.util.Date RHS) {
            this.rxDate = RHS;
        }

        public java.util.Date getPickupDate() {
            return this.pickupDate;
        }

        public void setPickupDate(java.util.Date RHS) {
            this.pickupDate = RHS;
        }

        public java.util.Date getPickupTime() {
            return this.pickupTime;
        }

        public void setPickupTime(java.util.Date RHS) {
            this.pickupTime = RHS;
        }

        public java.util.Date getEndDate() {
            if (this.isDiscontinued()) return this.archivedDate;
            else return this.endDate;
        }

        public void setEndDate(java.util.Date RHS) {
            this.endDate = RHS;
        }

        public java.util.Date getWrittenDate() {
            return this.writtenDate;
        }

        public void setWrittenDate(java.util.Date RHS) {
            this.writtenDate = RHS;
        }

        public String getWrittenDateFormat() {
            return this.writtenDateFormat;
        }

        public void setWrittenDateFormat(String RHS) {
            this.writtenDateFormat = RHS;
        }

        public String getRxDateFormat() {
            return this.rxDateFormat;
        }

        public void setRxDateFormat(String RHS) {
            this.rxDateFormat = RHS;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }


        public String getPriorRxProtocol() {
            return priorRxProtocol;
        }

        public void setPriorRxProtocol(String priorRxProtocol) {
            this.priorRxProtocol = priorRxProtocol;
        }

        public Integer getPharmacyId() {
            return pharmacyId;
        }

        public void setPharmacyId(Integer pharmacyId) {
            this.pharmacyId = pharmacyId;
        }

        /*
         * Current should contain non-expired drugs, as well as long terms drugs that are not deleted/discontinued
         */
        public boolean isCurrent() {
            if (isLongTerm() && !isDiscontinued() && !isArchived()) {
                return true;
            }
            boolean b = false;

            try {
                GregorianCalendar cal = new GregorianCalendar(Locale.CANADA);
                cal.add(GregorianCalendar.DATE, -1);

                if (this.getEndDate().after(cal.getTime())) {
                    b = true;
                }
            } catch (Exception e) {
                b = false;
            }

            return b;
        }

        public void calcEndDate() {
            try {
                GregorianCalendar cal = new GregorianCalendar(Locale.CANADA);
                int days = 0;

                //          p("this.getRxDate()",this.getRxDate().toString());
                cal.setTime(this.getRxDate());

                if (this.getDuration() != null && this.getDuration().length() > 0) {
                    if (Integer.parseInt(this.getDuration()) > 0) {
                        int i = Integer.parseInt(this.getDuration());
                        //      p("i",Integer.toString(i));
                        //      p("this.getDurationUnit()",this.getDurationUnit());
                        if (this.getDurationUnit() != null && this.getDurationUnit().equalsIgnoreCase("D")) {
                            days = i;
                        }
                        if (this.getDurationUnit() != null && this.getDurationUnit().equalsIgnoreCase("W")) {
                            days = i * 7;
                        }
                        if (this.getDurationUnit() != null && this.getDurationUnit().equalsIgnoreCase("M")) {
                            days = i * 30;
                        }

                        if (this.getRepeat() > 0) {
                            int r = this.getRepeat();

                            r++; // if we have a repeat of 1, multiply days by 2

                            days = days * r;
                        }
                        //    p("days",Integer.toString(days));
                        if (days > 0) {
                            cal.add(GregorianCalendar.DATE, days);
                        }
                    }
                }

                this.endDate = cal.getTime();
            } catch (Exception e) {
                MiscUtils.getLogger().error("Error", e);
            }
            //     p("endDate",RxUtil.DateToString(this.endDate));
        }

        public String getBrandName() {
            return this.BN;
        }

        public void setBrandName(String RHS) {
            this.BN = RHS;
            // this.gcn=null;
        }

        public int getGCN_SEQNO() {
            return this.GCN_SEQNO;
        }

        public void setGCN_SEQNO(int RHS) {
            this.GCN_SEQNO = RHS;
            // this.gcn=null;
        }

        /*
         * public RxDrugData.GCN getGCN() { if (this.gcn==null) { this.gcn = new RxDrugData().getGCN(this.BN, this.GCN_SEQNO); }
         *
         * return gcn; }
         */
        public boolean isCustom() {
            boolean b = false;

            if (this.customName != null) {
                b = true;
            } else if (this.GCN_SEQNO == 0) {
                b = true;
            }
            return b;
        }

        public String getCustomName() {
            return this.customName;
        }

        public void setCustomName(String RHS) {
            this.customName = RHS;
            if (this.customName != null) {
                if (this.customName.equalsIgnoreCase("null") || this.customName.equalsIgnoreCase("")) {
                    this.customName = null;
                }
            }
        }

        public float getTakeMin() {
            return this.takeMin;
        }

        public void setTakeMin(float RHS) {
            this.takeMin = RHS;
        }

        public String getTakeMinString() {
            return RxUtil.FloatToString(this.takeMin);
        }

        public float getTakeMax() {
            return this.takeMax;
        }

        public void setTakeMax(float RHS) {
            this.takeMax = RHS;
        }

        public String getTakeMaxString() {
            return RxUtil.FloatToString(this.takeMax);
        }

        public String getFrequencyCode() {
            return this.frequencyCode;
        }

        public void setFrequencyCode(String RHS) {
            this.frequencyCode = RHS;
        }

        public String getDuration() {
            return this.duration;
        }

        public void setDuration(String RHS) {
            this.duration = RHS;
        }

        public String getDurationUnit() {
            return this.durationUnit;
        }

        public void setDurationUnit(String RHS) {
            this.durationUnit = RHS;
        }

        public String getQuantity() {
            if (this.quantity == null) {
                this.quantity = "";
            }
            return this.quantity;
        }

        public void setQuantity(String RHS) {
            if (RHS == null || RHS.equals("null") || RHS.length() < 1) {
                this.quantity = "0";
            } else {
                this.quantity = RHS;
            }
        }

        public int getRepeat() {
            return this.repeat;
        }

        public void setRepeat(int RHS) {
            this.repeat = RHS;
        }

        public java.util.Date getLastRefillDate() {
            return this.lastRefillDate;
        }

        public void setLastRefillDate(java.util.Date RHS) {
            this.lastRefillDate = RHS;
        }

        public boolean getNosubs() {
            return this.nosubs;
        }

        public int getNosubsInt() {
            if (this.getNosubs() == true) {
                return 1;
            } else {
                return 0;
            }
        }

        public void setNosubs(boolean RHS) {
            this.nosubs = RHS;
        }

        public void setNosubs(int RHS) {
            if (RHS == 0) {
                this.setNosubs(false);
            } else {
                this.setNosubs(true);
            }
        }

        public boolean isPrn() {//conventional name for getter of boolean variable
            return this.prn;
        }

        /**
         * Gets the PRN (as needed) flag.
         *
         * @return boolean true if medication is taken as needed
         */
        public boolean getPrn() {
            return this.prn;
        }

        /**
         * Gets PRN flag as integer.
         *
         * Converts boolean PRN flag to integer for database storage.
         *
         * @return int 1 if PRN, 0 otherwise
         */
        public int getPrnInt() {
            if (this.getPrn() == true) {
                return 1;
            } else {
                return 0;
            }
        }

        /**
         * Sets the PRN (as needed) flag.
         *
         * @param RHS boolean true if medication is PRN
         */
        public void setPrn(boolean RHS) {
            this.prn = RHS;
        }

        /**
         * Sets PRN flag from integer value.
         *
         * @param RHS int 0 for false, non-zero for true
         */
        public void setPrn(int RHS) {
            if (RHS == 0) {
                this.setPrn(false);
            } else {
                this.setPrn(true);
            }
        }

        /**
         * Gets the long-term medication flag.
         *
         * @return Boolean true if long-term, false if not, null if unspecified
         */
        public Boolean getLongTerm() {
            return this.longTerm;
        }

        /**
         * Sets the long-term medication flag.
         *
         * @param trueFalseNull Boolean three-state flag (true/false/null)
         */
        public void setLongTerm(Boolean trueFalseNull) {
            this.longTerm = trueFalseNull;
        }

        /**
         * Gets the short-term medication flag.
         *
         * @return boolean true if short-term medication
         */
        public boolean getShortTerm() {
            return this.shortTerm;
        }

        /**
         * Sets the short-term medication flag.
         *
         * @param st boolean true if short-term
         */
        public void setShortTerm(boolean st) {
            this.shortTerm = st;
        }

        /**
         * Sets the non-authoritative flag.
         *
         * Indicates prescription was not prescribed by this provider/clinic.
         *
         * @param nonAuthoritative boolean true if from external source
         */
        public void setNonAuthoritative(boolean nonAuthoritative) {
            this.nonAuthoritative = nonAuthoritative;
        }

        /**
         * Checks if prescription is non-authoritative.
         *
         * @return boolean true if from external source
         */
        public boolean isNonAuthoritative() {
            return this.nonAuthoritative;
        }

        /**
         * Checks if this is a past medication.
         *
         * Past medications are historical records not currently active.
         *
         * @return boolean true if past medication
         */
        public boolean isPastMed() {
            boolean trueFalse = Boolean.FALSE;
            if (this.pastMed != null) {
                trueFalse = this.pastMed;
            }
            return trueFalse;
        }

        /**
         * Gets the past medication flag.
         *
         * @return Boolean three-state flag (true/false/null)
         */
        public Boolean getPastMed() {
            return this.pastMed;
        }

        /**
         * Sets the past medication flag.
         *
         * @param trueFalseNull Boolean three-state flag
         */
        public void setPastMed(Boolean trueFalseNull) {
            this.pastMed = trueFalseNull;
        }

        /**
         * Gets internal dispensing flag.
         *
         * @return boolean true if dispensed internally
         */
        public boolean getDispenseInternal() {
            return isDispenseInternal();
        }

        /**
         * Checks if medication is dispensed internally.
         *
         * Internal dispensing means clinic provides medication directly.
         *
         * @return boolean true if dispensed by clinic
         */
        public boolean isDispenseInternal() {
            return dispenseInternal;
        }

        /**
         * Sets internal dispensing flag.
         *
         * @param dispenseInternal boolean true if clinic dispenses
         */
        public void setDispenseInternal(boolean dispenseInternal) {
            this.dispenseInternal = dispenseInternal;
        }

        /**
         * Checks patient medication compliance.
         *
         * @return boolean true if patient is compliant
         */
        public boolean isPatientCompliance() {
            boolean trueFalse = Boolean.FALSE;
            if (this.patientCompliance != null) {
                trueFalse = this.patientCompliance;
            }
            return trueFalse;
        }

        /**
         * Gets patient compliance flag.
         *
         * @return Boolean three-state compliance indicator
         */
        public Boolean getPatientCompliance() {
            return this.patientCompliance;
        }

        /**
         * Sets patient compliance flag.
         *
         * @param trueFalseNull Boolean compliance status
         */
        public void setPatientCompliance(Boolean trueFalseNull) {
            this.patientCompliance = trueFalseNull;
        }

        /**
         * Gets special instructions for prescription.
         *
         * Logs warnings if special instructions appear too short,
         * which may indicate data issues.
         *
         * @return String special instructions text
         */
        public String getSpecial() {

            // Check for potentially missing special instructions
            if (special == null || special.length() < 4) {
                // the reason this is here is because Tomislav/Caisi was having massive problems tracking down
                // drugs that randomly go missing in prescriptions, like a list of 20 drugs and 3 would be missing on the prescription.
                // it was tracked down to some code which required a special, but we couldn't figure out why a special was required or missing.
                // so now we have code to log an error when a drug is missing a special, we still don't know why it's required or missing
                // but at least we know which drug does it.
                logger.warn("Some one is retrieving the drug special but it appears to be blank : " + special);
            }

            return special;
        }

        /**
         * Gets external provider name.
         *
         * Used when prescription originated from outside provider.
         *
         * @return String external provider's name
         */
        public String getOutsideProviderName() {
            return this.outsideProviderName;
        }

        /**
         * Sets external provider name.
         *
         * @param outsideProviderName String provider name
         */
        public void setOutsideProviderName(String outsideProviderName) {
            this.outsideProviderName = outsideProviderName;
        }

        /**
         * Gets external provider OHIP number.
         *
         * Ontario Health Insurance Plan provider number.
         *
         * @return String OHIP billing number
         */
        public String getOutsideProviderOhip() {
            return this.outsideProviderOhip;
        }

        /**
         * Sets external provider OHIP number.
         *
         * @param outsideProviderOhip String OHIP number
         */
        public void setOutsideProviderOhip(String outsideProviderOhip) {
            this.outsideProviderOhip = outsideProviderOhip;
        }

        /**
         * Sets special instructions for prescription.
         *
         * Validates and processes special instructions text.
         * Logs warnings for suspiciously short instructions.
         *
         * @param RHS String special instructions
         */
        public void setSpecial(String RHS) {

            // Validate special instructions length
            if (RHS == null || RHS.length() < 4) {
                logger.warn("Some one is setting the drug special but it appears to be blank : " + special);
            }

            if (RHS != null) {
                if (!RHS.equals("null")) {
                    special = RHS;
                } else {
                    special = null;
                }
            } else {
                special = null;
            }

            //if (special == null || special.length() < 6) {
            if (special == null || special.length() < 4) {
                logger.warn("after processing the drug special but it appears to be blank : " + special);
            }
        }

        /**
         * Gets formatted special instructions for display.
         *
         * Converts multi-line special instructions to HTML format
         * with semicolon separators for better display.
         *
         * @return String HTML-formatted special instructions
         */
        public String getSpecialDisplay() {
            String ret = "";

            String s = this.getSpecial();

            if (s != null) {
                if (s.length() > 0) {
                    ret = "<br>";

                    int i;
                    String[] arr = s.split("\n");

                    for (i = 0; i < arr.length; i++) {
                        ret += arr[i].trim();
                        if (i < arr.length - 1) {
                            ret += "; ";
                        }
                    }
                }
            }

            return ret;
        }

        /**
         * Gets audit string for logging.
         *
         * Used for passing data to LogAction; maps to data column in log table.
         *
         * @return String complete prescription details for audit
         */
        public String getAuditString() {
            return getFullOutLine();
        }

        /**
         * Gets complete prescription output line.
         *
         * Formats all prescription details including special instructions,
         * substitution restrictions, and refill information.
         *
         * @return String formatted prescription details
         */
        public String getFullOutLine() {
            String extra = "";

            if (getNosubs()) {
                extra += "SUBSTITUTION NOT ALLOWED";
            }
            if ((getRefillQuantity() != null && getRefillQuantity() > 0) || (getRefillDuration() != null && getRefillDuration() > 0)) {
                extra = "Refill: Qty:" + (getRefillQuantity() != null ? getRefillQuantity() : "0") + " Duration:" + (getRefillDuration() != null ? getRefillDuration() : "0") + " Days";
            }
            return (RxPrescriptionData.getFullOutLine(getSpecial() + "\n" + extra));
        }

        /**
         * Gets formatted dosage for display.
         *
         * Shows dosage range if min and max differ.
         *
         * @return String formatted dosage (e.g., "1-2" or "1")
         */
        public String getDosageDisplay() {
            String ret = "";
            if (this.getTakeMin() != this.getTakeMax()) {
                ret += this.getTakeMinString() + "-" + this.getTakeMaxString();
            } else {
                ret += this.getTakeMinString();
            }
            return ret;
        }

        /**
         * Gets formatted frequency for display.
         *
         * Includes PRN indication if applicable.
         *
         * @return String frequency with PRN if needed
         */
        public String getFreqDisplay() {
            String ret = this.getFrequencyCode();
            if (this.getPrn()) {
                ret += " PRN ";
            }
            return ret;
        }

        /**
         * Gets complete prescription display string.
         *
         * Formats all prescription components into a single display string
         * including drug name, dosage, frequency, duration, quantity, and
         * special flags like no substitution.
         *
         * @return String complete prescription display or null on error
         */
        public String getRxDisplay() {
            try {
                String ret;

                if (this.isCustom()) {
                    if (this.customName != null) {
                        ret = this.customName + " ";
                    } else {
                        ret = "Unknown ";
                    }
                } else {
                    // RxDrugData.GCN gcn = this.getGCN();

                    ret = this.getBrandName() + " "; // gcn.getBrandName() + " ";
                    // + gcn.getStrength() + " "
                    // + gcn.getDoseForm() + " "
                    // + gcn.getRoute() + " ";
                }

                if (this.getTakeMin() != this.getTakeMax()) {
                    ret += this.getTakeMinString() + "-" + this.getTakeMaxString();
                } else {
                    ret += this.getTakeMinString();
                }

                ret += " " + this.getFrequencyCode();

                if (this.getPrn()) {
                    ret += " PRN ";
                }
                ret += " " + this.getDuration() + " ";

                if (getDurationUnit() != null && this.getDurationUnit().equals("D")) {
                    ret += "Day";
                }
                if (getDurationUnit() != null && this.getDurationUnit().equals("W")) {
                    ret += "Week";
                }
                if (getDurationUnit() != null && this.getDurationUnit().equals("M")) {
                    ret += "Month";
                }

                try {
                    if (this.getDuration() != null && this.getDuration().trim().length() == 0) {
                        this.setDuration("0");
                    }
                    if (this.getDuration() != null && !this.getDuration().equalsIgnoreCase("null") && Integer.parseInt(this.getDuration()) > 1) {
                        ret += "s";
                    }
                } catch (Exception durationCalcException) {
                    logger.error("Error with duration:", durationCalcException);
                }
                ret += "  ";
                ret += this.getQuantity();
                ret += " Qty  Repeats: ";
                ret += String.valueOf(this.getRepeat());

                if (this.getNosubs()) {
                    ret += " No subs ";
                }

                return ret;
            } catch (Exception e) {
                logger.error("unexpected error", e);
                return null;
            }
        }

        /**
         * Gets the drug name for display.
         *
         * Returns custom name for custom drugs or brand name for standard drugs.
         *
         * @return String drug name
         */
        public String getDrugName() {
            String ret;
            if (this.isCustom()) {
                if (this.customName != null) {
                    ret = this.customName + " ";
                } else {
                    ret = "Unknown ";
                }
            } else {
                ret = this.getBrandName() + " ";
            }
            return ret;
        }

        /**
         * Gets complete frequency string.
         *
         * Combines dosage amount with frequency code.
         *
         * @return String full frequency (e.g., "1-2 TID")
         */
        public String getFullFrequency() {
            String ret = "";
            if (this.getTakeMin() != this.getTakeMax()) {
                ret += this.getTakeMinString() + "-" + this.getTakeMaxString();
            } else {
                ret += this.getTakeMinString();
            }

            ret += " " + this.getFrequencyCode();
            return ret;
        }

        /**
         * Formats duration into human-readable string.
         *
         * Converts duration and unit into a readable format like "7 Days" or "2 Weeks".
         * Handles pluralization based on duration value.
         *
         * @return String formatted duration (e.g., "30 Days", "1 Week")
         */
        public String getFullDuration() {
            String ret = this.getDuration() + " ";
            if (this.getDurationUnit().equals("D")) {
                ret += "Day";
            }
            if (this.getDurationUnit().equals("W")) {
                ret += "Week";
            }
            if (this.getDurationUnit().equals("M")) {
                ret += "Month";
            }

            // Add plural 's' if duration is greater than 1
            if (Integer.parseInt(this.getDuration()) > 1) {
                ret += "s";
            }
            return ret;
        }

        /**
         * Archives this prescription.
         *
         * Marks the prescription as archived (soft delete) rather than physically
         * removing it from the database. This maintains audit trail and allows
         * restoration if needed.
         */
        public void Delete() {
            try {
                DrugDao drugDao = (DrugDao) SpringUtils.getBean(DrugDao.class);
                Drug drug = drugDao.find(getDrugId());
                if (drug != null) {
                    drug.setArchived(true);
                    drugDao.merge(drug);
                }
            } catch (Exception e) {
                logger.error("unexpected error", e);
            }
        }

        /**
         * Saves this prescription to the database.
         *
         * Creates a new prescription record without linking to a script.
         *
         * @return boolean true if save was successful
         */
        public boolean Save() {
            return Save(null);
        }


        /**
         * Records that this prescription has been printed.
         *
         * Updates the prescription's print history including the date/time
         * and provider who printed it. Increments the print count. This
         * audit trail is important for controlled substances and regulatory
         * compliance.
         *
         * @param loggedInInfo LoggedInInfo containing provider information
         * @return boolean true if print record was successfully saved
         */
        public boolean Print(LoggedInInfo loggedInInfo) {
            PrescriptionDao dao = SpringUtils.getBean(PrescriptionDao.class);
            ca.openosp.openo.commn.model.Prescription p = dao.find(ConversionUtils.fromIntString(getScript_no()));
            String providerNo = loggedInInfo.getLoggedInProviderNo();

            if (p == null) return false;

            // Append print date and provider to reprint history
            String dates_reprinted = p.getDatesReprinted();
            String now = DateUtils.format("yyyy-MM-dd HH:mm:ss", new Date());
            if (dates_reprinted != null && dates_reprinted.length() > 0) {
                dates_reprinted += "," + now + ";" + providerNo;
            } else {
                dates_reprinted = now + ";" + providerNo;
            }
            p.setDatesReprinted(dates_reprinted);
            dao.merge(p);
            this.setNumPrints(this.getNumPrints() + 1);

            return true;

        }

        /**
         * Gets the next display position for this patient's prescriptions.
         *
         * Prescriptions are ordered by position for display. This method
         * finds the highest position and returns the next available one.
         *
         * @return int next available position number
         */
        public int getNextPosition() {
            DrugDao dao = SpringUtils.getBean(DrugDao.class);
            int position = dao.getMaxPosition(this.getDemographicNo());
            return (position + 1);
        }

        /**
         * Saves this prescription to the database with script linking.
         *
         * Performs validation, calculates end date, and saves the prescription.
         * Links to a prescription script if scriptId is provided. Ensures dosage
         * min/max values are logical and escapes special instructions for SQL safety.
         *
         * @param scriptId String optional script ID to link this prescription to
         * @return boolean true if save was successful
         */
        public boolean Save(String scriptId) {

            this.calcEndDate();

            // Ensure takeMax is at least as large as takeMin
            if (this.takeMin > this.takeMax) this.takeMax = this.takeMin;

            if (getSpecial() == null || getSpecial().length() < 6)
                logger.warn("drug special appears to be null or empty : " + getSpecial());

            String escapedSpecial = StringEscapeUtils.escapeSql(this.getSpecial());

            if (escapedSpecial == null || escapedSpecial.length() < 6)
                logger.warn("drug special after escaping appears to be null or empty : " + escapedSpecial);

            DrugDao dao = SpringUtils.getBean(DrugDao.class);
            Drug drug = new Drug();

            // Set display position and sync all fields
            this.position = this.getNextPosition();
            syncDrug(drug, ConversionUtils.fromIntString(scriptId));
            dao.persist(drug);
            drugId = drug.getId();

            return true;
        }

        /**
         * Synchronizes prescription data to drug entity.
         *
         * Maps all prescription fields to the corresponding drug entity fields
         * for database persistence. This method handles the complex mapping
         * between the prescription model and the database entity.
         *
         * @param drug Drug entity to populate
         * @param scriptId Integer optional script ID for linking
         */
        private void syncDrug(Drug drug, Integer scriptId) {
            // Map prescription fields to drug entity
            drug.setProviderNo(getProviderNo());
            drug.setDemographicId(getDemographicNo());
            drug.setRxDate(getRxDate());
            drug.setEndDate(getEndDate());
            drug.setWrittenDate(getWrittenDate());
            drug.setBrandName(getBrandName());
            drug.setGcnSeqNo(getGCN_SEQNO());
            drug.setCustomName(getCustomName());
            drug.setTakeMin(getTakeMin());
            drug.setTakeMax(getTakeMax());
            drug.setFreqCode(getFrequencyCode());
            drug.setDuration(getDuration());
            drug.setDurUnit(getDurationUnit());
            drug.setQuantity(getQuantity());
            drug.setRepeat(getRepeat());
            drug.setLastRefillDate(getLastRefillDate());
            drug.setNoSubs(getNosubs());
            drug.setPrn(getPrn());
            drug.setSpecial(getSpecial());
            drug.setGenericName(getGenericName());
            drug.setScriptNo(scriptId);
            drug.setAtc(atcCode);
            drug.setRegionalIdentifier(regionalIdentifier);
            drug.setUnit(getUnit());
            drug.setMethod(getMethod());
            drug.setRoute(getRoute());
            drug.setDrugForm(getDrugForm());
            drug.setOutsideProviderName(getOutsideProviderName());
            drug.setOutsideProviderOhip(getOutsideProviderOhip());
            drug.setCustomInstructions(getCustomInstr());
            drug.setDosage(getDosage());
            drug.setUnitName(getUnitName());
            drug.setLongTerm(getLongTerm());
            drug.setShortTerm(getShortTerm());
            drug.setCustomNote(isCustomNote());
            drug.setPastMed(getPastMed());
            drug.setDispenseInternal(getDispenseInternal());
            drug.setSpecialInstruction(getSpecialInstruction());
            drug.setPatientCompliance(getPatientCompliance());
            drug.setNonAuthoritative(isNonAuthoritative());
            drug.setPickUpDateTime(getPickupDate());
            drug.setETreatmentType(getETreatmentType());
            drug.setRxStatus(getRxStatus());
            drug.setDispenseInterval(getDispenseInterval());
            drug.setRefillQuantity(getRefillQuantity());
            drug.setRefillDuration(getRefillDuration());
            drug.setHideFromCpp(false);
            drug.setPosition(position);
            drug.setComment(getComment());
            drug.setStartDateUnknown(getStartDateUnknown());
            drug.setDispenseInternal(getDispenseInternal());
            drug.setProtocol(protocol);
            drug.setPriorRxProtocol(priorRxProtocol);
            drug.setPharmacyId(getPharmacyId());
        }

        /**
         * Adds this prescription to provider's favorites.
         *
         * Creates a favorite prescription template for quick reuse.
         *
         * @param providerNo String provider identifier
         * @param favoriteName String name for the favorite
         * @return boolean true if successfully added
         */
        public boolean AddToFavorites(String providerNo, String favoriteName) {
            Favorite fav = new Favorite(0, providerNo, favoriteName, this.getBrandName(), this.getGCN_SEQNO(), this.getCustomName(), this.getTakeMin(), this.getTakeMax(), this.getFrequencyCode(), this.getDuration(), this.getDurationUnit(), this.getQuantity(), this.getRepeat(), this.getNosubsInt(), this.getPrnInt(), this.getSpecial(), this.getGenericName(), this.getAtcCode(), this.getRegionalIdentifier(), this.getUnit(), this.getUnitName(), this.getMethod(), this.getRoute(), this.getDrugForm(),
                    this.getCustomInstr(), this.getDosage());
            fav.setDispenseInternal(this.getDispenseInternal());

            return fav.Save();
        }

        /**
         * Getter for property atcCode.
         *
         * @return Value of property atcCode.
         */
        public java.lang.String getAtcCode() {
            return atcCode;
        }

        /**
         * Checks if ATC code is valid.
         *
         * Validates that ATC code is not null or empty string.
         *
         * @return boolean true if ATC code is valid
         */
        public boolean isValidAtcCode() {
            if (atcCode != null && !atcCode.trim().equals("")) {
                return true;
            }
            return false;
        }

        /**
         * Setter for property atcCode.
         *
         * @param atcCode New value of property atcCode.
         */
        public void setAtcCode(java.lang.String atcCode) {
            this.atcCode = atcCode;
        }

        /**
         * Getter for property regionalIdentifier.
         *
         * @return Value of property regionalIdentifier.
         */
        public java.lang.String getRegionalIdentifier() {
            return regionalIdentifier;
        }

        /**
         * Setter for property regionalIdentifier.
         *
         * @param regionalIdentifier New value of property regionalIdentifier.
         */
        public void setRegionalIdentifier(java.lang.String regionalIdentifier) {
            this.regionalIdentifier = regionalIdentifier;
        }

        /**
         * Getter for property method.
         *
         * @return Value of property method.
         */
        public java.lang.String getMethod() {
            return method;
        }

        /**
         * Setter for property method.
         *
         * @param method New value of property method.
         */
        public void setMethod(java.lang.String method) {
            this.method = method;
        }

        /**
         * Getter for property unit.
         *
         * @return Value of property unit.
         */
        public java.lang.String getUnit() {
            return unit;
        }

        /**
         * Setter for property unit.
         *
         * @param unit New value of property unit.
         */
        public void setUnit(java.lang.String unit) {
            this.unit = unit;
        }

        /**
         * Getter for property unitName
         *
         * @return Value of property unitName.
         */
        public java.lang.String getUnitName() {
            return unitName;
        }

        /**
         * Setter for property unitName.
         *
         * @param unitName New value of property unitName.
         */
        public void setUnitName(java.lang.String unitName) {
            this.unitName = unitName;
        }

        /**
         * Getter for property route.
         *
         * @return Value of property route.
         */
        public java.lang.String getRoute() {
            return route;
        }

        /**
         * Setter for property route.
         *
         * @param route New value of property route.
         */
        public void setRoute(java.lang.String route) {
            this.route = route;
        }

        public java.lang.String getDrugForm() {
            return drugForm;
        }

        public void setDrugForm(java.lang.String drugForm) {
            this.drugForm = drugForm;
        }

        /**
         * Setter for property custom (does it have customized directions)
         *
         * @param custom value for custom
         */
        public void setCustomInstr(boolean custom) {
            this.custom = custom;
        }

        public boolean getCustomInstr() {
            return this.custom;
        }

        /**
         * Getter for property rxCreatedDate.
         *
         * @return Value of property rxCreatedDate.
         */
        public java.util.Date getRxCreatedDate() {
            return rxCreatedDate;
        }

        /**
         * Setter for property rxCreatedDate.
         *
         * @param rxCreatedDate New value of property rxCreatedDate.
         */
        public void setRxCreatedDate(java.util.Date rxCreatedDate) {
            this.rxCreatedDate = rxCreatedDate;
        }

        /**
         * Getter for property dosage.
         *
         * @return Value of property dosage.
         */
        public java.lang.String getDosage() {
            return dosage;
        }

        /**
         * Setter for property dosage.
         *
         * @param dosage New value of property dosage.
         */
        public void setDosage(java.lang.String dosage) {
            this.dosage = dosage;
        }

        public Integer getRefillDuration() {
            return refillDuration;
        }

        public void setRefillDuration(int refillDuration) {
            this.refillDuration = refillDuration;
        }

        public Integer getRefillQuantity() {
            return refillQuantity;
        }

        public void setRefillQuantity(int refillQuantity) {
            this.refillQuantity = refillQuantity;
        }

        public String getDispenseInterval() {
            return dispenseInterval;
        }

        public void setDispenseInterval(String dispenseInterval) {
            this.dispenseInterval = dispenseInterval;
        }

    }

    public static class Favorite {

        int favoriteId;
        String providerNo;
        String favoriteName;
        String BN;
        int GCN_SEQNO;
        String customName;
        float takeMin;
        float takeMax;
        String frequencyCode;
        String duration;
        String durationUnit;
        String quantity;
        int repeat;
        boolean nosubs;
        boolean prn;
        boolean customInstr;
        String special;
        String GN;
        String atcCode;
        String regionalIdentifier;
        String unit;
        String unitName;
        String method;
        String route;
        String drugForm;
        String dosage;
        Boolean dispenseInternal;

        public Favorite(int favoriteId, String providerNo, String favoriteName, String BN, int GCN_SEQNO, String customName, float takeMin, float takeMax, String frequencyCode, String duration, String durationUnit, String quantity, int repeat, int nosubs, int prn, String special, String GN, String atc, String regionalIdentifier, String unit, String unitName, String method, String route, String drugForm, boolean customInstr, String dosage) {
            this.favoriteId = favoriteId;
            this.providerNo = providerNo;
            this.favoriteName = favoriteName;
            this.BN = BN;
            this.GCN_SEQNO = GCN_SEQNO;
            this.customName = customName;
            this.takeMin = takeMin;
            this.takeMax = takeMax;
            this.frequencyCode = frequencyCode;
            this.duration = duration;
            this.durationUnit = durationUnit;
            this.quantity = quantity;
            this.repeat = repeat;
            this.nosubs = RxUtil.IntToBool(nosubs);
            this.prn = RxUtil.IntToBool(prn);
            this.special = special;
            this.GN = GN;
            this.atcCode = atc;
            this.regionalIdentifier = regionalIdentifier;
            this.unit = unit;
            this.unitName = unitName;
            this.method = method;
            this.route = route;
            this.drugForm = drugForm;
            this.customInstr = customInstr;
            this.dosage = dosage;
        }

        public Favorite(int favoriteId, String providerNo, String favoriteName, String BN, int GCN_SEQNO, String customName, float takeMin, float takeMax, String frequencyCode, String duration, String durationUnit, String quantity, int repeat, boolean nosubs, boolean prn, String special, String GN, String atc, String regionalIdentifier, String unit, String unitName, String method, String route, String drugForm, boolean customInstr, String dosage) {
            this.favoriteId = favoriteId;
            this.providerNo = providerNo;
            this.favoriteName = favoriteName;
            this.BN = BN;
            this.GCN_SEQNO = GCN_SEQNO;
            this.customName = customName;
            this.takeMin = takeMin;
            this.takeMax = takeMax;
            this.frequencyCode = frequencyCode;
            this.duration = duration;
            this.durationUnit = durationUnit;
            this.quantity = quantity;
            this.repeat = repeat;
            this.nosubs = nosubs;
            this.prn = prn;
            this.special = special;
            this.GN = GN;
            this.atcCode = atc;
            this.regionalIdentifier = regionalIdentifier;
            this.unit = unit;
            this.unitName = unitName;
            this.method = method;
            this.route = route;
            this.drugForm = drugForm;
            this.customInstr = customInstr;
            this.dosage = dosage;
        }

        public String getGN() {
            return this.GN;
        }

        public void setGN(String RHS) {
            this.GN = RHS;
        }

        public int getFavoriteId() {
            return this.favoriteId;
        }

        public String getProviderNo() {
            return this.providerNo;
        }

        public String getFavoriteName() {
            return this.favoriteName;
        }

        public void setFavoriteName(String RHS) {
            this.favoriteName = RHS;
        }

        public String getBN() {
            return this.BN;
        }

        public void setBN(String RHS) {
            this.BN = RHS;
        }

        public int getGCN_SEQNO() {
            return this.GCN_SEQNO;
        }

        public void setGCN_SEQNO(int RHS) {
            this.GCN_SEQNO = RHS;
        }

        public String getCustomName() {
            return this.customName;
        }

        public void setCustomName(String RHS) {
            this.customName = RHS;
        }

        public float getTakeMin() {
            return this.takeMin;
        }

        public void setTakeMin(float RHS) {
            this.takeMin = RHS;
        }

        public String getTakeMinString() {
            return RxUtil.FloatToString(this.takeMin);
        }

        public float getTakeMax() {
            return this.takeMax;
        }

        public void setTakeMax(float RHS) {
            this.takeMax = RHS;
        }

        public String getTakeMaxString() {
            return RxUtil.FloatToString(this.takeMax);
        }

        public String getFrequencyCode() {
            return this.frequencyCode;
        }

        public void setFrequencyCode(String RHS) {
            this.frequencyCode = RHS;
        }

        public String getDuration() {
            return this.duration;
        }

        public void setDuration(String RHS) {
            this.duration = RHS;
        }

        public String getDurationUnit() {
            return this.durationUnit;
        }

        public void setDurationUnit(String RHS) {
            this.durationUnit = RHS;
        }

        public String getQuantity() {
            return this.quantity;
        }

        public void setQuantity(String RHS) {
            this.quantity = RHS;
        }

        public int getRepeat() {
            return this.repeat;
        }

        public void setRepeat(int RHS) {
            this.repeat = RHS;
        }

        public boolean getNosubs() {
            return this.nosubs;
        }

        public void setNosubs(boolean RHS) {
            this.nosubs = RHS;
        }

        public int getNosubsInt() {
            if (this.getNosubs() == true) {
                return 1;
            } else {
                return 0;
            }
        }

        public boolean getPrn() {
            return this.prn;
        }

        public void setPrn(boolean RHS) {
            this.prn = RHS;
        }

        public int getPrnInt() {
            if (this.getPrn() == true) {
                return 1;
            } else {
                return 0;
            }
        }

        public String getSpecial() {
            return this.special;
        }

        public void setSpecial(String RHS) {
            this.special = RHS;
        }

        public boolean getCustomInstr() {
            return this.customInstr;
        }

        public void setCustomInstr(boolean customInstr) {
            this.customInstr = customInstr;
        }

        public Boolean getDispenseInternal() {
            return dispenseInternal;
        }

        public void setDispenseInternal(Boolean dispenseInternal) {
            this.dispenseInternal = dispenseInternal;
        }

        public boolean Save() {
            boolean b = false;

            // clean up fields
            if (this.takeMin > this.takeMax) {
                this.takeMax = this.takeMin;
            }
            if (getSpecial() == null || getSpecial().length() < 4) {
                //if (getSpecial() == null || getSpecial().length() < 6) {
                logger.warn("drug special appears to be null or empty : " + getSpecial());
            }
            String parsedSpecial = RxUtil.replace(this.getSpecial(), "'", "");
            //if (parsedSpecial == null || parsedSpecial.length() < 6) {
            if (parsedSpecial == null || parsedSpecial.length() < 4) {
                logger.warn("drug special after parsing appears to be null or empty : " + parsedSpecial);
            }

            FavoriteDao dao = SpringUtils.getBean(FavoriteDao.class);
            ca.openosp.openo.commn.model.Favorite favorite = dao.findByEverything(this.getProviderNo(), this.getFavoriteName(), this.getBN(), this.getGCN_SEQNO(), this.getCustomName(), this.getTakeMin(), this.getTakeMax(), this.getFrequencyCode(), this.getDuration(), this.getDurationUnit(), this.getQuantity(), this.getRepeat(), this.getNosubs(), this.getPrn(), parsedSpecial, this.getGN(), this.getUnitName(), this.getCustomInstr());

            if (this.getFavoriteId() == 0) {

                if (favorite != null) this.favoriteId = favorite.getId();

                b = true;

                if (this.getFavoriteId() == 0) {
                    favorite = new ca.openosp.openo.commn.model.Favorite();
                    favorite = syncFavorite(favorite);

                    dao.persist(favorite);
                    this.favoriteId = favorite.getId();

                    b = true;
                }

            } else {
                if (favorite == null) {
                    //we never found it..try by id
                    favorite = dao.find(this.getFavoriteId());
                }
                favorite = syncFavorite(favorite);
                dao.merge(favorite);

                b = true;
            }

            return b;
        }

        /**
         * Synchronizes favorite data to entity.
         *
         * Maps all favorite fields to the corresponding entity fields
         * for database persistence.
         *
         * @param f Favorite entity to populate
         * @return Favorite populated entity
         */
        private ca.openosp.openo.commn.model.Favorite syncFavorite(ca.openosp.openo.commn.model.Favorite f) {
            f.setProviderNo(this.getProviderNo());
            f.setName(this.getFavoriteName());
            f.setBn(this.getBN());
            f.setGcnSeqno(this.getGCN_SEQNO());
            f.setCustomName(this.getCustomName());
            f.setTakeMin(this.getTakeMin());
            f.setTakeMax(this.getTakeMax());
            f.setFrequencyCode(this.getFrequencyCode());
            f.setDuration(this.getDuration());
            f.setDurationUnit(this.getDurationUnit());
            f.setQuantity(this.getQuantity());
            f.setRepeat(this.getRepeat());
            f.setNosubs(this.getNosubsInt() != 0);
            f.setPrn(this.getPrnInt() != 0);
            f.setSpecial(this.getSpecial());
            f.setGn(this.getGN());
            f.setAtc(this.getAtcCode());
            f.setRegionalIdentifier(this.getRegionalIdentifier());
            f.setUnit(this.getUnit());
            f.setUnitName(this.getUnitName());
            f.setMethod(this.getMethod());
            f.setRoute(this.getRoute());
            f.setDrugForm(this.getDrugForm());
            f.setCustomInstructions(this.getCustomInstr());
            f.setDosage(this.getDosage());
            return f;
        }

        /**
         * Getter for property atcCode.
         *
         * @return Value of property atcCode.
         */
        public java.lang.String getAtcCode() {
            return atcCode;
        }

        /**
         * Setter for property atcCode.
         *
         * @param atcCode New value of property atcCode.
         */
        public void setAtcCode(java.lang.String atcCode) {
            this.atcCode = atcCode;
        }

        /**
         * Getter for property regionalIdentifier.
         *
         * @return Value of property regionalIdentifier.
         */
        public java.lang.String getRegionalIdentifier() {
            return regionalIdentifier;
        }

        /**
         * Setter for property regionalIdentifier.
         *
         * @param regionalIdentifier New value of property regionalIdentifier.
         */
        public void setRegionalIdentifier(java.lang.String regionalIdentifier) {
            this.regionalIdentifier = regionalIdentifier;
        }

        /**
         * Getter for property unit.
         *
         * @return Value of property unit.
         */
        public java.lang.String getUnit() {
            return unit;
        }

        /**
         * Setter for property unit.
         *
         * @param unit New value of property unit.
         */
        public void setUnit(java.lang.String unit) {
            this.unit = unit;
        }

        /**
         * Getter for property unitName.
         *
         * @return Value of property unitName.
         */
        public java.lang.String getUnitName() {
            return unitName;
        }

        /**
         * Setter for property unitName.
         *
         * @param unitName New value of property unitName.
         */
        public void setUnitName(java.lang.String unitName) {
            this.unitName = unitName;
        }

        /**
         * Getter for property method.
         *
         * @return Value of property method.
         */
        public java.lang.String getMethod() {
            return method;
        }

        /**
         * Setter for property method.
         *
         * @param method New value of property method.
         */
        public void setMethod(java.lang.String method) {
            this.method = method;
        }

        /**
         * Getter for property route.
         *
         * @return Value of property route.
         */
        public java.lang.String getRoute() {
            return route;
        }

        /**
         * Setter for property route.
         *
         * @param route New value of property route.
         */
        public void setRoute(java.lang.String route) {
            this.route = route;
        }

        public java.lang.String getDrugForm() {
            return drugForm;
        }

        public void setDrugForm(java.lang.String drugForm) {
            this.drugForm = drugForm;
        }

        /**
         * Getter for property dosage.
         *
         * @return Value of property dosage.
         */
        public java.lang.String getDosage() {
            return dosage;
        }

        /**
         * Setter for property dosage.
         *
         * @param dosage New value of property dosage.
         */
        public void setDosage(java.lang.String dosage) {
            this.dosage = dosage;
        }

    }

    public static boolean addToFavorites(String providerNo, String favoriteName, Drug drug) {
        Favorite fav = new Favorite(0, providerNo, favoriteName, drug.getBrandName(), drug.getGcnSeqNo(), drug.getCustomName(), drug.getTakeMin(), drug.getTakeMax(), drug.getFreqCode(), drug.getDuration(), drug.getDurUnit(), drug.getQuantity(), drug.getRepeat(), drug.isNoSubs(), drug.isPrn(), drug.getSpecial(), drug.getGenericName(), drug.getAtc(), drug.getRegionalIdentifier(), drug.getUnit(), drug.getUnitName(), drug.getMethod(), drug.getRoute(), drug.getDrugForm(), drug.isCustomInstructions(),
                drug.getDosage());
        fav.setDispenseInternal(drug.getDispenseInternal());
        return fav.Save();
    }

    @Override
    public String toString() {
        return (ReflectionToStringBuilder.toString(this));
    }

}
