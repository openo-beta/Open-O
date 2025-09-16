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
 * Provider entity representing healthcare providers and practitioners within OpenO EMR.
 * This entity encapsulates comprehensive healthcare provider information including
 * professional credentials, contact details, specialization, and provincial registration
 * numbers required for Canadian healthcare billing and regulatory compliance.
 *
 * <p>Healthcare providers in OpenO EMR include physicians, nurse practitioners, specialists,
 * residents, and other healthcare professionals authorized to provide medical services.
 * This entity supports multi-jurisdictional requirements across Canadian provinces,
 * maintaining essential identifiers for billing systems like OHIP (Ontario), MSP (BC),
 * and other provincial healthcare programs.
 *
 * <p>Key healthcare provider features include:
 * <ul>
 * <li>Professional identification with provider numbers and billing credentials
 * <li>Provincial healthcare registration numbers (OHIP, RMA, HSO numbers)
 * <li>Medical specialization and team assignment for clinical workflow
 * <li>Contact information for professional communication and referrals
 * <li>Status tracking for active/inactive provider management
 * <li>Supervisory relationships for training and oversight programs
 * </ul>
 *
 * <p>Provider credentials and identifiers include:
 * <ul>
 * <li><strong>OHIP Number:</strong> Ontario Health Insurance Plan provider identifier
 * <li><strong>RMA Number:</strong> Registered Medical Assistant identification
 * <li><strong>Billing Number:</strong> Provincial billing system identifier
 * <li><strong>HSO Number:</strong> Health Service Organization identifier
 * </ul>
 *
 * <p>This entity supports clinical workflows including appointment scheduling,
 * billing operations, clinical documentation, and inter-provider communication.
 * Provider information is essential for maintaining regulatory compliance and
 * ensuring proper attribution of healthcare services.
 *
 * @see ca.openosp.openo.entities.Patient
 * @see ca.openosp.openo.entities.Prescription
 * @since November 1, 2004
 */
public class Provider {
    private String providerNo = "";
    private String lastName = "";
    private String firstName = "";
    private String providerType = "";
    private String specialty = "";
    private String team = "";
    private String sex = "";
    private String dob = "";
    private String address = "";
    private String phone = "";
    private String workPhone = "";
    private String ohipNo = "";
    private String rmaNo = "";
    private String billingNo = "";
    private String hsoNo = "";
    private String status = "";
    private String comments = "";
    private String providerActivity = "";
    private String supervisor = "";


    /**
     * Default constructor for Provider entity.
     * Creates a new Provider instance with all String fields initialized to empty strings.
     * This constructor ensures that getter methods return empty strings rather than null
     * values, which is important for UI display and data processing consistency.
     */
    public Provider() {
    }

    /**
     * Full constructor
     *
     * @param providerNo       String
     * @param lastName         String
     * @param firstName        String
     * @param providerType     String
     * @param specialty        String
     * @param team             String
     * @param sex              String
     * @param dob              String
     * @param address          String
     * @param phone            String
     * @param workPhone        String
     * @param ohipNo           String
     * @param rmaNo            String
     * @param billingNo        String
     * @param hsoNo            String
     * @param status           String
     * @param comments         String
     * @param providerActivity String
     */
    public Provider(String providerNo, String lastName, String firstName,
                    String providerType, String specialty, String team,
                    String sex, String dob, String address, String phone,
                    String workPhone, String ohipNo, String rmaNo,
                    String billingNo, String hsoNo, String status,
                    String comments, String providerActivity) {
        this.providerNo = providerNo;
        this.lastName = lastName;
        this.firstName = firstName;
        this.providerType = providerType;
        this.specialty = specialty;
        this.team = team;
        this.sex = sex;
        this.dob = dob;
        this.address = address;
        this.phone = phone;
        this.workPhone = workPhone;
        this.ohipNo = ohipNo;
        this.rmaNo = rmaNo;
        this.billingNo = billingNo;
        this.hsoNo = hsoNo;
        this.status = status;
        this.comments = comments;
        this.providerActivity = providerActivity;
    }

    /**
     * Gets the unique healthcare provider number used throughout the EMR system.
     * This identifier is used for linking clinical activities, appointments, prescriptions,
     * and billing records to the specific healthcare provider.
     *
     * @return String the provider number, never null (returns empty string if not set)
     */
    public String getProviderNo() {
        return (providerNo != null ? providerNo : "");
    }

    /**
     * Gets the lastName
     *
     * @return String lastName
     */
    public String getLastName() {
        return (lastName != null ? lastName : "");
    }

    /**
     * Gets the firstName
     *
     * @return String firstName
     */
    public String getFirstName() {
        return (firstName != null ? firstName : "");
    }

    /**
     * Gets the provider type classification.
     * This field was previously used for categorizing healthcare providers but has been
     * superseded by more specific provider role and specialty tracking systems.
     *
     * @return String the provider type, never null (returns empty string if not set)
     * @deprecated No longer in use as of April 23, 2010. Use specialty and team fields instead.
     *             Marked for future removal in favor of more granular provider classification.
     */
    @Deprecated
    public String getProviderType() {
        return (providerType != null ? providerType : "");
    }

    /**
     * Gets the medical specialty or area of clinical expertise for this provider.
     * This field indicates the provider's specialization such as Family Medicine,
     * Internal Medicine, Pediatrics, Surgery, etc., which is used for referral
     * routing and clinical care coordination.
     *
     * @return String the medical specialty, never null (returns empty string if not set)
     */
    public String getSpecialty() {
        return (specialty != null ? specialty : "");
    }

    /**
     * Gets the healthcare team or clinical group assignment for this provider.
     * Team assignments facilitate collaborative care models, shared patient panels,
     * and interdisciplinary healthcare delivery within the EMR system.
     *
     * @return String the team assignment, never null (returns empty string if not set)
     */
    public String getTeam() {
        return (team != null ? team : "");
    }

    /**
     * Gets the sex
     *
     * @return String sex
     */
    public String getSex() {
        return (sex != null ? sex : "");
    }

    /**
     * Gets the dob
     *
     * @return String dob
     */
    public String getDob() {
        return dob;
    }

    /**
     * Gets the address
     *
     * @return String address
     */
    public String getAddress() {
        return (address != null ? address : "");
    }

    /**
     * Gets the phone
     *
     * @return String phone
     */
    public String getPhone() {
        return (phone != null ? phone : "");
    }

    /**
     * Gets the workPhone
     *
     * @return String workPhone
     */
    public String getWorkPhone() {
        return (workPhone != null ? workPhone : "");
    }

    /**
     * Gets the OHIP (Ontario Health Insurance Plan) provider number.
     * This identifier is required for billing medical services to the Ontario
     * Ministry of Health and Long-Term Care. Essential for regulatory compliance
     * and payment processing in Ontario healthcare.
     *
     * @return String the OHIP provider number, never null (returns empty string if not set)
     */
    public String getOhipNo() {
        return (ohipNo != null ? ohipNo : "");
    }

    /**
     * Gets the RMA (Registered Medical Assistant) number.
     * This identifier is used for healthcare support staff and medical assistants
     * who provide clinical support under physician supervision.
     *
     * @return String the RMA number, never null (returns empty string if not set)
     */
    public String getRmaNo() {
        return (rmaNo != null ? rmaNo : "");
    }

    /**
     * Gets the provincial billing number used for healthcare service billing.
     * This identifier varies by province and is used for submitting claims to
     * provincial healthcare plans (MSP in BC, OHIP in Ontario, etc.).
     *
     * @return String the billing number, never null (returns empty string if not set)
     */
    public String getBillingNo() {
        return (billingNo != null ? billingNo : "");
    }

    /**
     * Gets the HSO (Health Service Organization) number.
     * HSOs are organized healthcare delivery models that provide comprehensive
     * care through capitation funding rather than fee-for-service billing.
     *
     * @return String the HSO number, never null (returns empty string if not set)
     */
    public String getHsoNo() {
        return (hsoNo != null ? hsoNo : "");
    }

    /**
     * Gets the status
     *
     * @return String status
     */
    public String getStatus() {
        return (status != null ? status : "");
    }

    /**
     * Gets the comments
     *
     * @return String comments
     */
    public String getComments() {
        return (comments != null ? comments : "");
    }

    /**
     * Gets the providerActivity
     *
     * @return String providerActivity
     */
    public String getProviderActivity() {
        return (providerActivity != null ? providerActivity : "");
    }

    /**
     * Gets the supervisor provider number for this healthcare provider.
     * This field establishes supervisory relationships important for training programs,
     * resident supervision, and clinical oversight in academic healthcare settings.
     *
     * @return String the supervisor provider number, never null (returns empty string if not set)
     */
    public String getSupervisor() {
        return (supervisor != null ? supervisor : "");
    }

    /**
     * Sets the providerNo
     *
     * @param providerNo String
     */
    public void setProviderNo(String providerNo) {
        this.providerNo = providerNo;
    }

    /**
     * Sets the lastName
     *
     * @param lastName String
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Sets the firstName
     *
     * @param firstName String
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the providerType
     *
     * @param providerType String
     */
    public void setProviderType(String providerType) {
        this.providerType = providerType;
    }

    /**
     * Sets the specialty
     *
     * @param specialty String
     */
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    /**
     * Sets the team
     *
     * @param team String
     */
    public void setTeam(String team) {
        this.team = team;
    }

    /**
     * Sets the sex
     *
     * @param sex String
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * Sets the dob
     *
     * @param dob String
     */
    public void setDob(String dob) {
        this.dob = dob;
    }

    /**
     * Sets the address
     *
     * @param address String
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Sets the phone
     *
     * @param phone String
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Sets the workPhone
     *
     * @param workPhone String
     */
    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    /**
     * Sets the ohipNo
     *
     * @param ohipNo String
     */
    public void setOhipNo(String ohipNo) {
        this.ohipNo = ohipNo;
    }

    /**
     * Sets the rmaNo
     *
     * @param rmaNo String
     */
    public void setRmaNo(String rmaNo) {
        this.rmaNo = rmaNo;
    }

    /**
     * Sets the billingNo
     *
     * @param billingNo String
     */
    public void setBillingNo(String billingNo) {
        this.billingNo = billingNo;
    }

    /**
     * Sets the hsoNo
     *
     * @param hsoNo String
     */
    public void setHsoNo(String hsoNo) {
        this.hsoNo = hsoNo;
    }

    /**
     * Sets the status
     *
     * @param status String
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets the comments
     *
     * @param comments String
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Sets the providerActivity
     *
     * @param providerActivity String
     */
    public void setProviderActivity(String providerActivity) {
        this.providerActivity = providerActivity;
    }


    /**
     * Sets the supervisor provider number for this healthcare provider.
     *
     * @param supervisor String the supervisor provider number
     */
    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    /**
     * Constructs the provider's full name by combining first and last names.
     * This convenience method provides a formatted full name for display purposes
     * in user interfaces, reports, and clinical documentation.
     *
     * @return String the provider's full name in "FirstName LastName" format
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    /**
     * Constructs the provider's initials from first and last names.
     * This method extracts the first character from both first and last names
     * to create a two-character initial combination for compact display purposes
     * in clinical interfaces and appointment scheduling.
     *
     * @return String the provider's initials (first letter of first name + first letter of last name)
     */
    public String getInitials() {
        String firstInit = "";
        String lastInit = "";
        if (this.firstName != null && this.firstName.length() > 0) {
            if (this.firstName.length() > 1) {
                firstInit = firstName.substring(0, 1);
            } else {
                firstInit = firstName;
            }
        }
        if (this.lastName != null && this.lastName.length() > 0) {
            if (this.lastName.length() > 1) {
                lastInit = lastName.substring(0, 1);
            } else {
                lastInit = lastName;
            }
        }

        return firstInit + lastInit;
    }

}
