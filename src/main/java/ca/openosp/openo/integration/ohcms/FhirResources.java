/**
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 * <p>
 * This software was written for the Department of Family Medicine McMaster University Hamilton
 * Ontario, Canada
 */
package ca.openosp.openo.integration.ohcms;

import ca.openosp.openo.commn.Gender;
import ca.openosp.openo.PMmodule.dao.ProviderDao;
import ca.openosp.openo.commn.dao.ClinicDAO;
import ca.openosp.openo.commn.dao.ContactDao;
import ca.openosp.openo.commn.dao.DemographicContactDao;
import ca.openosp.openo.commn.model.Clinic;
import ca.openosp.openo.commn.model.Contact;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicContact;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.integration.fhir.r4.utils.EnumMappingUtil;
import ca.openosp.openo.integration.oneId.OneIdGatewayData;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Address.AddressType;
import org.hl7.fhir.r4.model.Address.AddressUse;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.HumanName.NameUse;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class FhirResources {

  ClinicDAO clinicDao = SpringUtils.getBean(ClinicDAO.class);
  ContactDao contactDao = SpringUtils.getBean(ContactDao.class);
  ProviderDao providerDao = SpringUtils.getBean(ProviderDao.class);
  DemographicContactDao demographicContactDao = SpringUtils.getBean(DemographicContactDao.class);
  private static final FhirContext fhirContext = FhirContext.forR4();

  public Organization getOrganization(LoggedInInfo loggedInInfo) throws CMSException {
    Organization organization = new Organization();
    OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
    organization.setId(oneIdGatewayData.getProviderUPI());
    organization.getMeta().addProfile(
        "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-cms-profile-Organization|1.0.0");
    Identifier identifier = new Identifier();
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding().setSystem("http://hl7.org/fhir/v2/0203").setCode("RRI");
    identifier.setType(codeableConcept);
    identifier.setSystem(PROVIDER_UPI_SYSTEM)
        .setValue(oneIdGatewayData.getProviderUPI());
    organization.addIdentifier(identifier);
    Clinic clinic = clinicDao.getClinic();
    // name the organization by the selected authority, falling back to the clinic
    String organizationName = oneIdGatewayData.getUaoFriendlyName();
    if (organizationName == null || organizationName.trim().isEmpty()) {
      organizationName = clinic.getOrganizationName();
    }
    if (organizationName == null || organizationName.trim().isEmpty()) {
      throw new CMSException(
          "Organization name can not be blank. Select an authority (UAO) or edit Clinic details in the administration section.");
    }
    organization.setName(organizationName);
    // No address here. The Organization is the Health Information Custodian the provider acts for,
    // and the clinic's address is the Location's, which getLocation sends. The two are separate
    // entities: one custodian covers many clinics. OpenO holds no address for a custodian, so the
    // Organization goes out identified by name and UAO alone.
    if (clinic.getWorkPhone() != null && clinic.getWorkPhone().trim().length() > 4) {
      organization.addTelecom().setSystem(ContactPointSystem.PHONE)
          .setUse(ContactPointUse.WORK).setValue(clinic.getWorkPhone());
    }
    if (clinic.getFax() != null && clinic.getFax().trim().length() > 4) {
      organization.addTelecom().setSystem(ContactPointSystem.FAX)
          .setUse(ContactPointUse.WORK).setValue(clinic.getFax());
    }
    return organization;
  }

  /**
   * Builds the CMS Location profile for this clinic.
   *
   * <p>Location and Organization are separate things. The Organization is the Health Information
   * Custodian the provider is acting for, chosen by their Under Authority Of value, and a provider
   * can act for several. The Location is the clinic itself, the place this EMR instance serves, and
   * there is exactly one of it. That is why the clinic address lives here and not on the
   * Organization.
   *
   * @param loggedInInfo LoggedInInfo the acting provider session
   * @return Location the clinic to place in context, or null when no clinic is on file
   * @since 2026-08-04
   */
  public Location getLocation(LoggedInInfo loggedInInfo) {
    Clinic clinic = clinicDao.getClinic();
    if (clinic == null) {
      return null;
    }
    Location location = new Location();
    if (clinic.getId() != null) {
      location.setId(String.valueOf(clinic.getId()));
    }
    location.getMeta().addProfile(
        "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-cms-profile-Location|1.0.0");
    location.setStatus(Location.LocationStatus.ACTIVE);
    if (clinic.getClinicName() != null && !clinic.getClinicName().trim().isEmpty()) {
      location.setName(clinic.getClinicName().trim());
    }
    if (clinic.getAddress() != null && !clinic.getAddress().trim().isEmpty()) {
      location.getAddress()
          .setUse(AddressUse.WORK)
          .setType(AddressType.PHYSICAL)
          .addLine(clinic.getAddress().trim())
          .setCity(clinic.getCity())
          .setState(clinic.getProvince())
          .setPostalCode(clinic.getPostal());
    }
    if (clinic.getClinicPhone() != null && clinic.getClinicPhone().trim().length() > 4) {
      location.addTelecom().setSystem(ContactPointSystem.PHONE)
          .setValue(clinic.getClinicPhone().trim());
    }
    if (clinic.getClinicFax() != null && clinic.getClinicFax().trim().length() > 4) {
      location.addTelecom().setSystem(ContactPointSystem.FAX).setValue(clinic.getClinicFax().trim());
    }
    // The custodian this clinic is operating under, which is what ties the two resources
    // together: one custodian covers many clinics, so a Location arriving on its own leaves
    // nothing to say whose it is. Named by identifier rather than by a pointer to the
    // Organization's resource id, because that id is the UAO value and carries a colon, which a
    // FHIR resource id may not.
    OneIdGatewayData oneIdGatewayData = loggedInInfo.getOneIdGatewayData();
    String custodianUpi = oneIdGatewayData == null ? null : oneIdGatewayData.getProviderUPI();
    if (custodianUpi != null && !custodianUpi.trim().isEmpty()) {
      location.getManagingOrganization().getIdentifier()
          .setSystem(PROVIDER_UPI_SYSTEM)
          .setValue(custodianUpi.trim());
      String custodianName = oneIdGatewayData.getUaoFriendlyName();
      if (custodianName != null && !custodianName.trim().isEmpty()) {
        location.getManagingOrganization().setDisplay(custodianName.trim());
      }
    }
    return location;
  }


  public Practitioner getPractitioner(LoggedInInfo loggedInInfo) throws CMSException {

    if (loggedInInfo.getLoggedInProvider().getPractitionerNo() == null
        || loggedInInfo.getLoggedInProvider().getPractitionerNo().trim().isEmpty()) {
      throw new CMSException(
          "Practitioner Number can not be blank. Edit Provider details in the administration section.");
    }
    if (loggedInInfo.getLoggedInProvider().getLastName() == null
        || loggedInInfo.getLoggedInProvider().getLastName().trim().isEmpty()) {
      throw new CMSException(
          "Provider's Lastname can not be blank. Edit Provider details in the administration section.");
    }
    if (loggedInInfo.getLoggedInProvider().getFirstName() == null
        || loggedInInfo.getLoggedInProvider().getFirstName().trim().isEmpty()) {
      throw new CMSException(
          "Provider's Firstname can not be blank. Edit Provider details in the administration section.");
    }
    Practitioner practitioner = new Practitioner();
    practitioner.setId(loggedInInfo.getLoggedInProvider().getPractitionerNo());
    practitioner.getMeta().addProfile(
        "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-cms-profile-Practitioner|1.0.0");
    practitioner.addIdentifier()
        .setType(licenceIdentifierType())
        .setSystem(PHYSICIAN_LICENCE_SYSTEM)
        .setValue(loggedInInfo.getLoggedInProvider().getPractitionerNo())
        .getAssigner().setDisplay(PHYSICIAN_LICENCE_ASSIGNER);
    HumanName practitionerName = practitioner.addName()
        .setFamily(loggedInInfo.getLoggedInProvider().getLastName())
        .addGiven(loggedInInfo.getLoggedInProvider().getFirstName());
    String title = loggedInInfo.getLoggedInProvider().getTitle();
    if (title != null && !title.trim().isEmpty()) {
      practitionerName.addPrefix(title.trim());
    }
    return practitioner;
  }

  public Patient getPatient(Demographic demographic) throws CMSException {
    Patient patient = new Patient();
    patient.getMeta().addProfile(
        "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-cms-profile-Patient|1.0.0");
    if (demographic.getHin() == null || demographic.getHin().trim().isEmpty()) {
      throw new CMSException(
          "Patient's Health Card Number can not be blank. Verify Patient's details in the demographic record.");
    }
    patient.addIdentifier()
        .setSystem("https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-patient-hcn")
        .setValue(demographic.getHin());
    patient.setId(demographic.getDemographicNo() + "");
    try {
      patient.setBirthDate(new Date(demographic.getBirthDay().getTimeInMillis()));
    } catch (Exception e) {
      throw new CMSException(
          "Error processing birthdate of patient.  Verify birthdate in patient's demographic record.");
    }
    if (demographic.getSex() == null || demographic.getSex().trim().isEmpty()) {
      throw new CMSException(
          "Patient's gender can not be blank. Verify Patient's details in the demographic record.");
    }
    Gender gender;
    try {
      gender = Gender.valueOf(demographic.getSex().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new CMSException(
          "Patient's gender is not a recognized value. Verify Patient's details in the demographic record.");
    }
    patient.setGender(EnumMappingUtil.genderToAdministrativeGender(gender));
    if (demographic.getAddress() != null && demographic.getCity() != null
        && demographic.getProvince() != null && demographic.getPostal() != null) {
      patient.addAddress()
          .setUse(AddressUse.HOME)
          .addLine(demographic.getAddress())
          .setCity(demographic.getCity())
          .setState(demographic.getProvince())
          .setPostalCode(demographic.getPostal());
    }
    HumanName humanName = new HumanName();
    humanName.setUse(NameUse.OFFICIAL);
    humanName.getExtensionFirstRep()
        .setUrl("http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier");
    if (demographic.getLastName() == null || demographic.getLastName().trim().isEmpty()) {
      throw new CMSException(
          "Patient's Lastname can not be blank. Verify Patient's details in the demographic record.");
    }
    if (demographic.getFirstName() == null || demographic.getFirstName().trim().isEmpty()) {
      throw new CMSException(
          "Patient's Firstname can not be blank. Verify Patient's details in the demographic record.");
    }
    humanName.setFamily(demographic.getLastName());
    humanName.addGiven(demographic.getFirstName());

    if (demographic.getTitle() != null && !demographic.getTitle().trim().isEmpty()) {
      humanName.addPrefix(demographic.getTitle());
    }
    patient.addName(humanName);
    // Only sent when the record actually says which language the patient uses. Naming one the
    // patient never gave is worse than saying nothing: an absent element leaves the question open,
    // a present one asserts an answer the clinic never recorded.
    String languageCode = languageCode(demographic.getOfficialLanguage());
    if (languageCode != null) {
      CodeableConcept cc = new CodeableConcept();
      cc.addCoding().setSystem("urn:ietf:bcp:47").setCode(languageCode);
      patient.addCommunication().setLanguage(cc);
    }
    // The provider the patient is rostered to. Carried as an identifier rather than a pointer to
    // another resource, because the context holds one Practitioner, the signed-in one, and a
    // pointer to anything else would lead nowhere.
    Reference generalPractitioner = generalPractitionerReference(demographic.getProviderNo());
    if (generalPractitioner != null) {
      patient.addGeneralPractitioner(generalPractitioner);
    }
    for (Patient.ContactComponent contact : patientContacts(demographic.getDemographicNo())) {
      patient.addContact(contact);
    }
    if (demographic.getPhone() != null && !demographic.getPhone().trim().isEmpty()) {
      patient.addTelecom().setUse(ContactPointUse.HOME)
          .setSystem(ContactPointSystem.PHONE)
          .setValue(demographic.getPhone());
    }
    if (demographic.getPhone2() != null && !demographic.getPhone2().trim().isEmpty()) {
      patient.addTelecom().setUse(ContactPointUse.WORK)
          .setSystem(ContactPointSystem.PHONE)
          .setValue(demographic.getPhone2());
    }
    return patient;
  }

  /**
   * Turns the language recorded on a patient record into the BCP 47 code the profile expects.
   *
   * <p>The record holds a language name chosen from a two-option list, "English" or "French", so
   * anything else is a value this mapping does not know and the caller sends nothing rather than
   * guess.
   *
   * @param recorded String the language name held on the record, or null
   * @return String the BCP 47 code, or null when the record names no language this recognises
   * @since 2026-08-04
   */
  static String languageCode(String recorded) {
    if (recorded == null) {
      return null;
    }
    String name = recorded.trim();
    if ("English".equalsIgnoreCase(name)) {
      return "en";
    }
    if ("French".equalsIgnoreCase(name)) {
      return "fr";
    }
    return null;
  }

  /** The naming system OpenO asserts for the custodian a provider acts under. */
  private static final String PROVIDER_UPI_SYSTEM =
      "https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-provider-upi";

  /** The naming system OpenO asserts for a practitioner's licence number. */
  private static final String PHYSICIAN_LICENCE_SYSTEM =
      "https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-license-physician";

  /** The body that issues numbers in that system. */
  private static final String PHYSICIAN_LICENCE_ASSIGNER =
      "College of Physicians and Surgeons of Ontario";

  /** Marks an identifier as a medical licence number, from the HL7 identifier-type table. */
  private static CodeableConcept licenceIdentifierType() {
    CodeableConcept type = new CodeableConcept();
    type.addCoding()
        .setSystem("http://hl7.org/fhir/v2/0203")
        .setCode("MD")
        .setDisplay("Medical License number");
    return type;
  }

  /**
   * Points at the provider the patient is rostered to, by licence number.
   *
   * @param providerNo String the provider number held on the patient record, or null
   * @return Reference the provider as an identifier, or null when there is none to name
   * @since 2026-08-04
   */
  private Reference generalPractitionerReference(String providerNo) {
    if (providerNo == null || providerNo.trim().isEmpty()) {
      return null;
    }
    Provider provider = providerDao.getProvider(providerNo.trim());
    if (provider == null || provider.getPractitionerNo() == null
        || provider.getPractitionerNo().trim().isEmpty()) {
      return null;
    }
    Reference reference = new Reference();
    reference.getIdentifier()
        .setType(licenceIdentifierType())
        .setSystem(PHYSICIAN_LICENCE_SYSTEM)
        .setValue(provider.getPractitionerNo().trim());
    String name = fullName(provider.getFirstName(), provider.getLastName());
    if (name != null) {
      reference.setDisplay(name);
    }
    return reference;
  }

  /**
   * The patient's contacts, as recorded on their chart.
   *
   * @param demographicNo Integer the patient's demographic number
   * @return List the contacts to place on the Patient, empty when the chart names none
   * @since 2026-08-04
   */
  private List<Patient.ContactComponent> patientContacts(Integer demographicNo) {
    List<Patient.ContactComponent> contacts = new ArrayList<Patient.ContactComponent>();
    if (demographicNo == null) {
      return contacts;
    }
    for (DemographicContact link : demographicContactDao.findActiveByDemographicNo(demographicNo)) {
      Contact details = contactFor(link);
      if (details == null) {
        continue;
      }
      Patient.ContactComponent contact = new Patient.ContactComponent();
      String name = fullName(details.getFirstName(), details.getLastName());
      if (name == null) {
        continue;
      }
      contact.getName().setFamily(details.getLastName()).addGiven(details.getFirstName());
      if (link.getRole() != null && !link.getRole().trim().isEmpty()) {
        contact.addRelationship().setText(link.getRole().trim());
      }
      addContactPoint(contact, ContactPointSystem.PHONE, ContactPointUse.HOME, details.getResidencePhone());
      addContactPoint(contact, ContactPointSystem.PHONE, ContactPointUse.MOBILE, details.getCellPhone());
      addContactPoint(contact, ContactPointSystem.PHONE, ContactPointUse.WORK, details.getWorkPhone());
      addContactPoint(contact, ContactPointSystem.EMAIL, null, details.getEmail());
      if (details.getAddress() != null && !details.getAddress().trim().isEmpty()) {
        contact.getAddress()
            .setUse(AddressUse.HOME)
            .addLine(details.getAddress().trim())
            .setCity(details.getCity())
            .setState(details.getProvince())
            .setPostalCode(details.getPostal());
      }
      contacts.add(contact);
    }
    return contacts;
  }

  /**
   * Loads the person a chart contact row points at.
   *
   * <p>The row's own {@code details} field is not a database column, so it is null on every row
   * read back through the DAO and the person has to be fetched by the id the row holds. Only
   * personal contacts are returned; a row pointing at a provider or a specialist is skipped.
   *
   * @param link DemographicContact the chart's contact row
   * @return Contact the person named on the row, or null when there is none to return
   * @since 2026-08-05
   */
  private Contact contactFor(DemographicContact link) {
    if (link.getType() != DemographicContact.TYPE_CONTACT) {
      return null;
    }
    String contactId = link.getContactId();
    if (contactId == null || contactId.trim().isEmpty()) {
      return null;
    }
    try {
      return contactDao.find(Integer.valueOf(contactId.trim()));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static void addContactPoint(Patient.ContactComponent contact, ContactPointSystem system,
      ContactPointUse use, String value) {
    if (value == null || value.trim().length() <= 4) {
      return;
    }
    ContactPoint point = contact.addTelecom().setSystem(system).setValue(value.trim());
    if (use != null) {
      point.setUse(use);
    }
  }

  private static String fullName(String firstName, String lastName) {
    String first = firstName == null ? "" : firstName.trim();
    String last = lastName == null ? "" : lastName.trim();
    String name = (first + " " + last).trim();
    return name.isEmpty() ? null : name;
  }

  public Parameters getLanguageParameter(String id, String lang) {
    Parameters parameters = new Parameters();
    parameters.setId(id);
    parameters.getMeta().addProfile(
        "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-cms-profile-Parameters|1.0.0");
    Coding coding = new Coding();
    coding.setCode(lang);
    coding.setSystem("urn:ietf:bcp:47");
    parameters.addParameter().setName("appLanguage").setValue(coding);
    return parameters;
  }

  public Parameters getConsentTargetParameter(String id, String param) {
    Parameters parameters = new Parameters();
    parameters.setId(id);
    parameters.getMeta().addProfile(
        "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-cms-profile-Parameters|1.0.0");
    StringType stringType = new StringType();
    stringType.setValue(param);
    parameters.addParameter().setName("consentTarget").setValue(stringType);
    return parameters;
  }

  public Parameters getContextSessionIdParameter(String id, String param) {
    Parameters parameters = new Parameters();
    parameters.setId(id);
    parameters.getMeta().addProfile(
        "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-cms-profile-Parameters|1.0.0");
    IdType stringType = new IdType();
    stringType.setValue(param);
    parameters.addParameter().setName("contextSessionId").setValue(stringType);
    return parameters;
  }

  public String getString(BaseResource baseResource) {
    return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(baseResource);
  }
}
