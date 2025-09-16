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

import ca.openosp.openo.commn.dao.DemographicPharmacyDao;
import ca.openosp.openo.commn.dao.PharmacyInfoDao;
import ca.openosp.openo.commn.model.DemographicPharmacy;
import ca.openosp.openo.commn.model.PharmacyInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data access layer for pharmacy information management.
 *
 * This class provides comprehensive pharmacy data management functionality including
 * pharmacy CRUD operations, patient-pharmacy associations, and pharmacy searches.
 * It maintains the relationship between patients and their preferred pharmacies,
 * supporting multiple pharmacies per patient with preference ordering.
 *
 * The class tracks pharmacy information history, allowing for historical data
 * retention when pharmacy details change (e.g., address or phone number updates).
 * This is critical for prescription tracking and regulatory compliance.
 *
 * Key features include:
 * - Pharmacy creation, update, and soft deletion
 * - Patient-pharmacy linking with preference ordering
 * - Pharmacy search by name, city, or address
 * - Historical pharmacy data retention
 *
 * @since 2004-09-29
 */
public class RxPharmacyData {

    /**
     * DAO for pharmacy information access.
     */
    private PharmacyInfoDao pharmacyInfoDao = (PharmacyInfoDao) SpringUtils.getBean(PharmacyInfoDao.class);

    /**
     * DAO for patient-pharmacy relationship management.
     */
    private DemographicPharmacyDao demographicPharmacyDao = (DemographicPharmacyDao) SpringUtils.getBean(DemographicPharmacyDao.class);

    /**
     * Default constructor for RxPharmacyData.
     */
    public RxPharmacyData() {
    }


    /**
     * Adds a new pharmacy to the system.
     *
     * Creates a new pharmacy record with complete contact and location information.
     * The method is synchronized to prevent duplicate pharmacy creation during
     * concurrent operations.
     *
     * @param name String pharmacy name
     * @param address String street address
     * @param city String city name
     * @param province String province/state code
     * @param postalCode String postal/zip code
     * @param phone1 String primary phone number
     * @param phone2 String secondary phone number
     * @param fax String fax number
     * @param email String email address
     * @param serviceLocationIdentifier String unique identifier for electronic prescribing
     * @param notes String additional notes or instructions
     */
    synchronized public void addPharmacy(String name, String address, String city, String province, String postalCode, String phone1, String phone2, String fax, String email, String serviceLocationIdentifier, String notes) {
        pharmacyInfoDao.addPharmacy(name, address, city, province, postalCode, phone1, phone2, fax, email, serviceLocationIdentifier, notes);
    }


    /**
     * Updates an existing pharmacy's information.
     *
     * Creates a new record for the pharmacy while maintaining the same pharmacy ID.
     * This preserves historical data for audit and prescription tracking purposes.
     * The previous record remains for historical reference.
     *
     * @param ID String pharmacy ID to update
     * @param name String updated pharmacy name
     * @param address String updated street address
     * @param city String updated city name
     * @param province String updated province/state code
     * @param postalCode String updated postal/zip code
     * @param phone1 String updated primary phone number
     * @param phone2 String updated secondary phone number
     * @param fax String updated fax number
     * @param email String updated email address
     * @param serviceLocationIdentifier String updated identifier for electronic prescribing
     * @param notes String updated notes or instructions
     */
    public void updatePharmacy(String ID, String name, String address, String city, String province, String postalCode, String phone1, String phone2, String fax, String email, String serviceLocationIdentifier, String notes) {
        pharmacyInfoDao.updatePharmacy(Integer.parseInt(ID), name, address, city, province, postalCode, phone1, phone2, fax, email, serviceLocationIdentifier, notes);
    }

    /**
     * Soft deletes a pharmacy from the system.
     *
     * Sets the pharmacy status to inactive (0) rather than physically deleting.
     * Also unlinks all patient associations with this pharmacy. The pharmacy
     * data is retained for historical and audit purposes.
     *
     * @param ID String pharmacy ID to delete
     */
    public void deletePharmacy(String ID) {

        List<DemographicPharmacy> demographicPharmacies = demographicPharmacyDao.findAllByPharmacyId(Integer.parseInt(ID));

        for (DemographicPharmacy demographicPharmacy : demographicPharmacies) {
            demographicPharmacyDao.unlinkPharmacy(Integer.parseInt(ID), demographicPharmacy.getDemographicNo());
        }

        pharmacyInfoDao.deletePharmacy(Integer.parseInt(ID));
    }

    /**
     * Retrieves the current pharmacy information.
     *
     * Returns the most recent record for a pharmacy, reflecting any updates
     * that have been made to address, phone numbers, or other details.
     *
     * @param ID String pharmacy ID
     * @return PharmacyInfo current pharmacy data, null if not found
     */
    public PharmacyInfo getPharmacy(String ID) {
        PharmacyInfo pharmacyInfo = pharmacyInfoDao.getPharmacy(Integer.parseInt(ID));
        return pharmacyInfo;
    }

    /**
     * Retrieves a specific historical pharmacy record.
     *
     * Used to access historical pharmacy data, such as previous addresses
     * or phone numbers. Each update creates a new record, allowing access
     * to pharmacy information at specific points in time.
     *
     * @param recordID String specific pharmacy record ID
     * @return PharmacyInfo historical pharmacy data
     */
    public PharmacyInfo getPharmacyByRecordID(String recordID) {
        return pharmacyInfoDao.getPharmacyByRecordID(Integer.parseInt(recordID));
    }


    /**
     * Retrieves all active pharmacies in the system.
     *
     * Returns only active pharmacies (status != 0) with their most current
     * information. Deleted pharmacies are excluded from results.
     *
     * @return List<PharmacyInfo> all active pharmacies
     */
    public List<PharmacyInfo> getAllPharmacies() {
        return pharmacyInfoDao.getAllPharmacies();
    }

    /**
     * Associates a pharmacy with a patient.
     *
     * Links a pharmacy to a patient's profile with a preference order.
     * Patients can have multiple preferred pharmacies ranked by preference.
     * Lower preference numbers indicate higher priority.
     *
     * @param pharmacyId String pharmacy ID to link
     * @param demographicNo String patient demographic number
     * @param preferredOrder String preference ranking (1 = primary, 2 = secondary, etc.)
     * @return PharmacyInfo the linked pharmacy with preference order set
     */
    public PharmacyInfo addPharmacyToDemographic(String pharmacyId, String demographicNo, String preferredOrder) {
        demographicPharmacyDao.addPharmacyToDemographic(Integer.parseInt(pharmacyId), Integer.parseInt(demographicNo), Integer.parseInt(preferredOrder));

        PharmacyInfo pharmacyInfo = pharmacyInfoDao.find(Integer.parseInt(pharmacyId));
        pharmacyInfo.setPreferredOrder(Integer.parseInt(preferredOrder));

        return pharmacyInfo;

    }

    /**
     * Retrieves all pharmacies associated with a patient.
     *
     * Returns a list of pharmacies linked to the patient, sorted by preference order.
     * Each pharmacy includes its preference ranking. Returns null if the demographic
     * number is invalid or if no pharmacies are linked.
     *
     * @param demographicNo String patient demographic number
     * @return List<PharmacyInfo> patient's pharmacies sorted by preference, null if none
     */
    public List<PharmacyInfo> getPharmacyFromDemographic(String demographicNo) {

		// Validate demographic number
		if (demographicNo == null || demographicNo.isEmpty() || !demographicNo.matches("\\d+")) {
			return null;
		}


        // Get patient-pharmacy associations
        List<DemographicPharmacy> dpList = demographicPharmacyDao.findByDemographicId(Integer.parseInt(demographicNo));
        if (dpList.isEmpty()) {
            return null;
        }

        // Extract pharmacy IDs
        List<Integer> pharmacyIds = new ArrayList<Integer>();
        for (DemographicPharmacy demoPharmacy : dpList) {
            pharmacyIds.add(demoPharmacy.getPharmacyId());
            MiscUtils.getLogger().debug("ADDING ID " + demoPharmacy.getPharmacyId());
        }

        // Get pharmacy details
        List<PharmacyInfo> pharmacyInfos = pharmacyInfoDao.getPharmacies(pharmacyIds);

        // Set preference order for each pharmacy
        for (DemographicPharmacy demographicPharmacy : dpList) {
            for (PharmacyInfo pharmacyInfo : pharmacyInfos) {
                if (demographicPharmacy.getPharmacyId() == pharmacyInfo.getId()) {
                    pharmacyInfo.setPreferredOrder(demographicPharmacy.getPreferredOrder());
                    break;
                }
            }
        }

        // Sort by preference order
        Collections.sort(pharmacyInfos);
        return pharmacyInfos;
    }

    /**
     * Searches for pharmacy cities matching a search term.
     *
     * Used for auto-complete functionality when searching for pharmacies
     * by city. Returns distinct city names that match the search term.
     *
     * @param searchTerm String partial city name to search
     * @return List<String> matching city names
     */
    public List<String> searchPharmacyCity(String searchTerm) {

        return pharmacyInfoDao.searchPharmacyByCity(searchTerm);

    }

    /**
     * Searches for pharmacies by name and/or city.
     *
     * Supports compound searches using comma separation (e.g., "Pharmacy Name, City").
     * If no comma is present, searches by name only. The search matches partial
     * strings in pharmacy name, address, or city fields.
     *
     * @param searchTerm String search query, optionally comma-separated for name,city
     * @return List<PharmacyInfo> matching pharmacies
     */
    public List<PharmacyInfo> searchPharmacy(String searchTerm) {

        String[] terms;
        String name = "", city = "";

        // Parse search term for name and city components
        if (searchTerm.indexOf(",") > -1) {
            terms = searchTerm.split(",", -1);

            switch (terms.length) {
                case 2:
                    city = terms[1];
                    // Fall through intentionally
                case 1:
                    name = terms[0];
            }
        } else {
            name = searchTerm;
        }

        return pharmacyInfoDao.searchPharmacyByNameAddressCity(name, city);

    }

    /**
     * Removes the association between a patient and a pharmacy.
     *
     * Unlinks a pharmacy from a patient's profile without deleting
     * either the patient or pharmacy records.
     *
     * @param pharmacyId String pharmacy ID to unlink
     * @param demographicNo String patient demographic number
     */
    public void unlinkPharmacy(String pharmacyId, String demographicNo) {

        demographicPharmacyDao.unlinkPharmacy(Integer.parseInt(pharmacyId), Integer.parseInt(demographicNo));

    }

    /**
     * Counts patients who have selected a pharmacy as their preferred.
     *
     * Returns the total number of patients who have this pharmacy set
     * as their primary (preferred) pharmacy. Useful for pharmacy usage
     * statistics and impact analysis before deletion.
     *
     * @param pharmacyId String pharmacy ID to count patients for
     * @return Long number of patients preferring this pharmacy
     */
    public Long getTotalDemographicsPreferedToPharmacyByPharmacyId(String pharmacyId) {
        return demographicPharmacyDao.getTotalDemographicsPreferedToPharmacyByPharmacyId(Integer.parseInt(pharmacyId));
    }
}
