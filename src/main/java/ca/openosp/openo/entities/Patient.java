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

package ca.openosp.openo.entities;

/**
 * Patient entity representing core patient demographic and contact information
 * for healthcare encounters within OpenO EMR. This entity serves as a data transfer
 * object for patient information exchange and billing operations, particularly for
 * integration with provincial healthcare systems and billing modules.
 *
 * <p>This entity includes essential patient identifiers such as Health Insurance Number (HIN)
 * verification code, demographic details, contact information, and administrative identifiers
 * used throughout the EMR system. The entity supports Canadian healthcare requirements
 * including provincial health insurance number validation and multi-jurisdictional
 * patient management.
 *
 * <p>Key healthcare features include:
 * <ul>
 * <li>Health Insurance Number (HIN) with verification code for provincial billing
 * <li>Comprehensive demographic information for patient identification
 * <li>Multiple contact methods including phone, email, and postal addresses
 * <li>Provider assignment and chart number management
 * <li>Birth date handling with multiple format support for clinical documentation
 * </ul>
 *
 * @see ca.openosp.openo.commn.model.Demographic
 * @see ca.openosp.openo.entities.Provider
 * @since November 1, 2004
 */
public class Patient {
    private String firstName;
    private String lastName;
    private String phone;
    private String gender;
    private String address;
    private String city;
    private String province;
    private String postal;
    private String phone2;
    private String email;
    private String yearOfBirth;
    private String monthOfBirth;
    private String dateOfBirth;
    private String hin;
    private String ver;
    private String id;
    private int providerNo;
    private String chartNo;
    private String demographicNo;

    /**
     * Default constructor for Patient entity.
     * Creates a new Patient instance with all fields initialized to null.
     */
    public Patient() {
    }

    /**
     * Gets the patient's first name.
     *
     * @return String the patient's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the patient's first name.
     *
     * @param firstName String the patient's first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostal() {
        return postal;
    }

    public void setPostal(String postal) {
        this.postal = postal;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public String getYearOfBirth() {
        return yearOfBirth;
    }

    public void setYearOfBirth(String yearOfBirth) {
        this.yearOfBirth = yearOfBirth;
    }

    public String getMonthOfBirth() {
        return monthOfBirth;
    }

    public void setMonthOfBirth(String monthOfBirth) {
        this.monthOfBirth = monthOfBirth;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Gets the Health Insurance Number (HIN) for provincial healthcare billing.
     * This is the patient's unique identifier within their provincial healthcare system.
     *
     * @return String the Health Insurance Number
     */
    public String getHin() {
        return hin;
    }

    /**
     * Sets the Health Insurance Number (HIN) for provincial healthcare billing.
     *
     * @param hin String the Health Insurance Number
     */
    public void setHin(String hin) {
        this.hin = hin;
    }

    /**
     * Gets the HIN verification code used for validating the Health Insurance Number.
     * This code is typically required for provincial billing validation.
     *
     * @return String the HIN verification code
     */
    public String getVer() {
        return ver;
    }

    /**
     * Sets the HIN verification code used for validating the Health Insurance Number.
     *
     * @param ver String the HIN verification code
     */
    public void setVer(String ver) {
        this.ver = ver;
    }

    /**
     * Constructs the complete birth date in MM/DD/YYYY format for clinical documentation.
     * This method combines the separate birth date components into a standardized format
     * commonly used in healthcare systems.
     *
     * @return String the formatted birth date as YYYY/MM/DD
     */
    public String getBirthDate() {
        return this.yearOfBirth + "/" + this.getMonthOfBirth() + "/" +
                this.getDateOfBirth();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the assigned healthcare provider number for this patient.
     * This identifies the primary care provider responsible for the patient's care.
     *
     * @return int the provider number
     * @see ca.openosp.openo.entities.Provider
     */
    public int getProviderNo() {
        return providerNo;
    }

    /**
     * Sets the assigned healthcare provider number for this patient.
     *
     * @param providerNo int the provider number
     */
    public void setProviderNo(int providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * Gets the patient's chart number used for medical record identification.
     * This is typically a clinic-specific identifier for organizing patient records.
     *
     * @return String the chart number
     */
    public String getChartNo() {
        return chartNo;
    }

    /**
     * Gets the patient's demographic number (primary key) from the EMR system.
     * This is the unique identifier used throughout the OpenO EMR database.
     *
     * @return String the demographic number
     */
    public String getDemographicNo() {
        return demographicNo;
    }

    /**
     * Sets the patient's chart number used for medical record identification.
     *
     * @param chartNo String the chart number
     */
    public void setChartNo(String chartNo) {
        this.chartNo = chartNo;
    }

    /**
     * Sets the patient's demographic number (primary key) from the EMR system.
     *
     * @param demographicNo String the demographic number
     */
    public void setDemographicNo(String demographicNo) {
        this.demographicNo = demographicNo;
    }

    /**
     * Constructs the complete birth date in ISO format (YYYY-MM-DD) for database operations.
     * This method provides the birth date in standard ISO format commonly used for
     * database storage and clinical system integration.
     *
     * @return String the formatted birth date as YYYY-MM-DD
     */
    public String getBirthDay() {
        return this.yearOfBirth + "-" + this.monthOfBirth + "-" + this.dateOfBirth;
    }
}
