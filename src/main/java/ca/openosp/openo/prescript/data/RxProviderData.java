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
import java.util.List;

import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.ClinicDAO;
import ca.openosp.openo.commn.dao.UserPropertyDAO;
import ca.openosp.openo.commn.model.Clinic;
import ca.openosp.openo.commn.model.UserProperty;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.SxmlMisc;

/**
 * Data access layer for provider information in the prescription module.
 *
 * This class manages provider (physician/prescriber) data for prescription generation.
 * It retrieves provider information including personal details, clinic information,
 * practitioner numbers, and prescription-specific contact details. The class supports
 * provider-specific customization of clinic information through user properties.
 *
 * Key features:
 * - Retrieves provider personal and professional information
 * - Manages clinic details (address, phone, fax) with provider-specific overrides
 * - Handles practitioner numbers for prescription authorization
 * - Supports customized prescription headers through user properties
 * - Automatically adds "Dr." prefix to provider names for prescriptions
 *
 * The class uses an inner Provider class to encapsulate all provider-related data
 * needed for prescription generation and printing.
 *
 * @since 2006-03-01
 */
public class RxProviderData {

    /**
     * DAO for accessing provider information.
     */
    private ProviderDao providerDao = (ProviderDao) SpringUtils.getBean(ProviderDao.class);

    /**
     * DAO for accessing user-specific property overrides.
     */
    private UserPropertyDAO userPropertyDao = (UserPropertyDAO) SpringUtils.getBean(UserPropertyDAO.class);

    /**
     * DAO for accessing clinic information.
     */
    private ClinicDAO clinicDao = (ClinicDAO) SpringUtils.getBean(ClinicDAO.class);

    /**
     * Retrieves all active providers in the system.
     *
     * Returns a list of all active providers with their clinic information
     * and prescription-specific settings. Only active providers are included.
     *
     * @return List<Provider> all active providers with prescription data
     */
    public List<Provider> getAllProviders() {
        List<ca.openosp.openo.commn.model.Provider> providers = providerDao.getActiveProviders();
        ArrayList<Provider> results = new ArrayList<Provider>();
        for (ca.openosp.openo.commn.model.Provider p : providers) {
            results.add(convertProvider(p));
        }
        return results;
    }

    /**
     * Retrieves a specific provider by provider number.
     *
     * @param providerNo String unique provider identifier
     * @return Provider with complete prescription-related information
     */
    public Provider getProvider(String providerNo) {
        return convertProvider(providerDao.getProvider(providerNo));
    }

    /**
     * Converts a provider entity to prescription-specific Provider object.
     *
     * This method enriches provider data with clinic information and applies
     * provider-specific customizations from user properties. It handles:
     * - Default clinic information from the clinic table
     * - Provider-specific overrides for phone, fax, and address
     * - Prescription-specific properties (rxPhone, rxAddress, etc.)
     * - Automatic "Dr." prefix addition to provider names
     *
     * The method prioritizes provider-specific settings over clinic defaults,
     * allowing providers to use custom contact information on prescriptions.
     *
     * @param p Provider entity from database
     * @return Provider object with complete prescription information
     */
    public Provider convertProvider(ca.openosp.openo.commn.model.Provider p) {
        String surname = null, firstName = null, clinicName = null, clinicAddress = null, clinicCity = null, clinicPostal = null, clinicPhone = null, clinicFax = null, clinicProvince = null, practitionerNo = null;
        String practitionerNoType = null;
        boolean useFullAddress = true;
        // Get default clinic information

        Clinic clinic = clinicDao.getClinic();
        if (clinic != null) {
            clinicName = clinic.getClinicName();
            clinicAddress = clinic.getClinicAddress();
            clinicCity = clinic.getClinicCity();
            clinicPostal = clinic.getClinicPostal();
            clinicPhone = clinic.getClinicPhone();
            clinicProvince = clinic.getClinicProvince();
            clinicFax = clinic.getClinicFax();
        }

        Provider prov = null;
        String providerNo = null;

        if (p != null) {
            surname = p.getLastName();
            firstName = p.getFirstName();
            practitionerNo = p.getPractitionerNo();
            practitionerNoType = p.getPractitionerNoType();
            // Add "Dr." prefix if not already present
            if (firstName.indexOf("Dr.") < 0) {
                firstName = "Dr. " + firstName;
            }

            // Use provider's work phone if available
            if (p.getWorkPhone() != null && p.getWorkPhone().length() > 0) {
                clinicPhone = p.getWorkPhone();
            }

            // Extract fax number from provider comments XML
            if (p.getComments() != null && p.getComments().length() > 0) {
                String pFax = SxmlMisc.getXmlContent(p.getComments(), "xml_p_fax");
                if (pFax != null && pFax.length() > 0) {
                    clinicFax = pFax;
                }
            }

            // Use provider's address if specified (single-line format)
            if (p.getAddress() != null && p.getAddress().length() > 0) {
                clinicAddress = p.getAddress();
                useFullAddress = false;
            }

            providerNo = p.getProviderNo();
            UserProperty prop = null;

            prop = userPropertyDao.getProp(providerNo, "faxnumber");
            if (prop != null && prop.getValue() != null && prop.getValue().length() > 0) {
                clinicFax = prop.getValue();
            }

            prop = userPropertyDao.getProp(providerNo, "rxPhone");
            if (prop != null && prop.getValue() != null && prop.getValue().length() > 0) {
                clinicPhone = prop.getValue();
            }

            // Override with prescription-specific address if configured
            prop = userPropertyDao.getProp(providerNo, "rxAddress");
            if (prop != null && prop.getValue() != null && prop.getValue().length() > 0) {
                // Use complete prescription address with all components
                clinicAddress = prop.getValue();
                clinicCity = readProperty(providerNo, "rxCity");
                clinicProvince = readProperty(providerNo, "rxProvince");
                clinicPostal = readProperty(providerNo, "rxPostal");
                useFullAddress = true;
            }

        }

        prov = new Provider(providerNo, surname, firstName, clinicName, clinicAddress,
                clinicCity, clinicPostal, clinicPhone, clinicFax, clinicProvince, practitionerNo, practitionerNoType);

        if (!useFullAddress) {
            prov.fullAddress = false;
        }

        return prov;
    }

    /**
     * Reads a user property value for a provider.
     *
     * Helper method to retrieve provider-specific configuration values.
     * Returns empty string if property doesn't exist.
     *
     * @param providerNo String provider identifier
     * @param key String property key to retrieve
     * @return String property value or empty string if not found
     */
    private String readProperty(String providerNo, String key) {
        UserProperty prop = userPropertyDao.getProp(providerNo, key);
        if (prop != null) {
            return prop.getValue();
        }
        return "";
    }

    /**
     * Provider data model for prescription generation.
     *
     * This inner class encapsulates all provider information needed for
     * prescription generation including personal details, clinic information,
     * and practitioner authorization numbers. It supports both full
     * multi-line addresses and single-line address formats.
     */
    public class Provider {
        /**
         * Flag indicating whether to use full multi-line address format.
         */
        boolean fullAddress = true;

        /**
         * Unique provider identifier.
         */
        String providerNo;

        /**
         * Provider's last name.
         */
        String surname;

        /**
         * Provider's first name (includes "Dr." prefix).
         */
        String firstName;

        /**
         * Name of the clinic.
         */
        String clinicName;

        /**
         * Clinic street address.
         */
        String clinicAddress;

        /**
         * Clinic city.
         */
        String clinicCity;

        /**
         * Clinic postal/zip code.
         */
        String clinicPostal;

        /**
         * Clinic phone number.
         */
        String clinicPhone;

        /**
         * Clinic fax number.
         */
        String clinicFax;

        /**
         * Clinic province/state.
         */
        String clinicProvince;

        /**
         * Provider's practitioner/license number.
         */
        String practitionerNo;

        /**
         * Type of practitioner number (e.g., CPSO, CNO).
         */
        String practitionerNoType;

        /**
         * Constructs a Provider without province information.
         *
         * @param providerNo String provider identifier
         * @param surname String provider's last name
         * @param firstName String provider's first name
         * @param clinicName String clinic name
         * @param clinicAddress String clinic address
         * @param clinicCity String clinic city
         * @param clinicPostal String clinic postal code
         * @param clinicPhone String clinic phone
         * @param clinicFax String clinic fax
         * @param practitionerNo String practitioner number
         * @param practitionerNoType String practitioner number type
         */
        public Provider(String providerNo, String surname, String firstName,
                        String clinicName, String clinicAddress, String clinicCity,
                        String clinicPostal, String clinicPhone, String clinicFax, String practitionerNo, String practitionerNoType) {
            this.providerNo = providerNo;
            this.surname = surname;
            this.firstName = firstName;
            this.clinicName = clinicName;
            this.clinicAddress = clinicAddress;
            this.clinicCity = clinicCity;
            this.clinicPostal = clinicPostal;
            this.clinicPhone = clinicPhone;
            this.clinicFax = clinicFax;
            this.practitionerNo = practitionerNo;
            this.practitionerNoType = practitionerNoType;
        }

        /**
         * Constructs a Provider with complete address information.
         *
         * @param providerNo String provider identifier
         * @param surname String provider's last name
         * @param firstName String provider's first name
         * @param clinicName String clinic name
         * @param clinicAddress String clinic address
         * @param clinicCity String clinic city
         * @param clinicPostal String clinic postal code
         * @param clinicPhone String clinic phone
         * @param clinicFax String clinic fax
         * @param clinicProvince String clinic province/state
         * @param practitionerNo String practitioner number
         * @param practitionerNoType String practitioner number type
         */
        public Provider(String providerNo, String surname, String firstName,
                        String clinicName, String clinicAddress, String clinicCity,
                        String clinicPostal, String clinicPhone, String clinicFax, String clinicProvince, String practitionerNo, String practitionerNoType) {
            this(providerNo, surname, firstName, clinicName, clinicAddress, clinicCity, clinicPostal, clinicPhone, clinicFax, practitionerNo, practitionerNoType);
            this.clinicProvince = clinicProvince;
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
         * Gets the provider's surname.
         *
         * @return String last name
         */
        public String getSurname() {
            return this.surname;
        }

        /**
         * Gets the provider's first name.
         *
         * @return String first name with "Dr." prefix
         */
        public String getFirstName() {
            return this.firstName;
        }

        /**
         * Gets the clinic name.
         *
         * @return String clinic name
         */
        public String getClinicName() {
            return this.clinicName;
        }

        /**
         * Gets the clinic street address.
         *
         * @return String clinic address
         */
        public String getClinicAddress() {
            return this.clinicAddress;
        }

        /**
         * Gets the clinic city.
         *
         * @return String city name
         */
        public String getClinicCity() {
            return this.clinicCity;
        }

        /**
         * Gets the clinic postal code.
         *
         * @return String postal/zip code
         */
        public String getClinicPostal() {
            return this.clinicPostal;
        }

        /**
         * Gets the clinic phone number.
         *
         * @return String phone number
         */
        public String getClinicPhone() {
            return this.clinicPhone;
        }

        /**
         * Gets the clinic fax number.
         *
         * @return String fax number
         */
        public String getClinicFax() {
            return this.clinicFax;
        }

        /**
         * Gets the clinic province/state.
         *
         * @return String province code
         */
        public String getClinicProvince() {
            return this.clinicProvince;
        }

        /**
         * Gets the provider's practitioner number.
         *
         * @return String practitioner/license number
         */
        public String getPractitionerNo() {
            return this.practitionerNo;
        }

        /**
         * Gets the practitioner number type.
         *
         * @return String practitioner number type (e.g., CPSO)
         */
        public String getPractitionerNoType() {
            return this.practitionerNoType;
        }

        /**
         * Gets formatted clinic address for prescription display.
         *
         * Returns either a complete multi-line address with city, province,
         * and postal code, or just the street address depending on the
         * fullAddress flag setting.
         *
         * @return String formatted address for prescription printing
         */
        public String getFullAddress() {
            if (fullAddress)
                return (getClinicAddress() + "  " + getClinicCity() + "   " + getClinicProvince() + "  " + getClinicPostal()).trim();
            else
                return getClinicAddress().trim();
        }

    }
}
