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
import ca.openosp.openo.commn.dao.ClinicDAO;
import ca.openosp.openo.commn.model.Clinic;
import ca.openosp.openo.commn.model.Demographic;
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
import org.hl7.fhir.r4.model.StringType;

import java.sql.Date;

public class FhirResources {

  ClinicDAO clinicDao = SpringUtils.getBean(ClinicDAO.class);
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
    identifier.setSystem("https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-provider-upi")
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
      organization.addTelecom().setSystem(ContactPointSystem.PHONE).setValue(clinic.getWorkPhone());
    }
    if (clinic.getFax() != null && clinic.getFax().trim().length() > 4) {
      organization.addTelecom().setSystem(ContactPointSystem.FAX).setValue(clinic.getFax());
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
        .setSystem("https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-license-physician")
        .setValue(loggedInInfo.getLoggedInProvider().getPractitionerNo());
    practitioner.addName().setFamily(loggedInInfo.getLoggedInProvider().getLastName())
        .addGiven(loggedInInfo.getLoggedInProvider().getFirstName());
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
    CodeableConcept cc = new CodeableConcept();
    Coding c = cc.addCoding();
    c.setSystem("https://www.hl7.org/fhir/valueset-languages.html");
    c.setCode("en-US");
    patient.addCommunication().setLanguage(cc);
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
