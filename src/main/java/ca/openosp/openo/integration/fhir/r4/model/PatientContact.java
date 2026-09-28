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
package ca.openosp.openo.integration.fhir.r4.model;

import ca.openosp.openo.commn.model.Contact;
import ca.openosp.openo.commn.model.DemographicContact;
import ca.openosp.openo.commn.model.ProfessionalContact;
import ca.openosp.openo.commn.model.ProfessionalSpecialist;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointSystemEnum;
import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
import ca.uhn.fhir.model.primitive.PositiveIntDt;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;

import java.util.List;

/**
 * Converts an Oscar Contact entity into a FHIR Patient.Contact Patient
 */
public class PatientContact extends
		AbstractOscarFhirResource<org.hl7.fhir.r4.model.Patient, Contact> {

  private Contact contact;
  private ProfessionalSpecialist professionalSpecialist;
  private DemographicContact demographicContact;
  private DemographicContact[] demographicContacts;
  private Contact[] contacts;
  private ca.uhn.fhir.model.dstu2.resource.Patient.Contact[] fhirContacts;
  private ca.uhn.fhir.model.dstu2.resource.Patient.Contact fhirContact;
  private List<CodeableConceptDt> contactRelationships;

  public PatientContact(Contact[] contacts) {
    this.setContacts(contacts);
    this.setFhirContacts(new ca.uhn.fhir.model.dstu2.resource.Patient.Contact[contacts.length]);
  }

  public PatientContact(Contact contact) {
    setContact(contact);
    setFhirContact(new ca.uhn.fhir.model.dstu2.resource.Patient.Contact());
  }

  /**
   * Demographic Contact wraps around a Contact entity. This wrapper contains meta data
   * about the contact, such as relationship and legal status.
   */
  public PatientContact(DemographicContact demographicContact) {
    setDemographicContact(demographicContact);
    setFhirContactFromDemographicContact(new ca.uhn.fhir.model.dstu2.resource.Patient.Contact());
  }

  /**
   * Demographic Contact wraps around a Contact entity. This wrapper contains meta data
   * about the contact, such as relationship and legal status.
   */
  public PatientContact(DemographicContact[] demographicContacts) {
    setDemographicContacts(demographicContacts);
    setFhirContactFromDemographicContact(
        new ca.uhn.fhir.model.dstu2.resource.Patient.Contact[demographicContacts.length]);
  }

  /**
   * Extract the contact info from a FHIR Location or Organization resource.
   */
  public PatientContact(IBaseResource fhirResource) {
    if (fhirResource instanceof Location) {
      setContactFromLocation((Location) fhirResource);
    }
    if (fhirResource instanceof Organization) {
      setContactFromOrganization((Organization) fhirResource);
    }
    if (this.contact != null) {
      setProfessionalSpecialist(new ProfessionalSpecialist());
    }
  }

  @Override
  protected void mapAttributes(Patient fhirResource) {
  }

  @Override
  protected void mapAttributes(Contact oscarResource) {
  }


  /**
   * Get the Oscar Entity Contact.
   */
  public Contact getContact() {
    return contact;
  }

  private void setContact(Contact contact) {
    this.contact = contact;
  }

  public ca.uhn.fhir.model.dstu2.resource.Patient.Contact getFhirContact() {
    return fhirContact;
  }

  public Contact[] getContacts() {
    return contacts;
  }

  private void setContacts(Contact[] contacts) {
    this.contacts = contacts;
  }

  public DemographicContact getDemographicContact() {
    return demographicContact;
  }

  private void setDemographicContact(
      DemographicContact demographicContact) {
    this.demographicContact = demographicContact;
  }

  public DemographicContact[] getDemographicContacts() {
    return demographicContacts;
  }

  private void setDemographicContacts(
      DemographicContact[] demographicContacts) {
    this.demographicContacts = demographicContacts;
  }

  public ca.uhn.fhir.model.dstu2.resource.Patient.Contact[] getFhirContacts() {
    return fhirContacts;
  }

  public ProfessionalSpecialist getProfessionalSpecialist() {
    return professionalSpecialist;
  }

  private void setProfessionalSpecialist(ProfessionalSpecialist professionalSpecialist) {
    professionalSpecialist.setReferralNo(((ProfessionalContact) contact).getCpso());
    professionalSpecialist.setLastName(contact.getLastName());
    String address2 = contact.getAddress2();
    String city = contact.getCity();
    String province = contact.getProvince();
    String postal = contact.getPostal();
    StringBuilder stringBuilder = new StringBuilder("");
    stringBuilder.append(contact.getAddress());
    if (address2 != null) {
      stringBuilder.append(address2);
    }
    if (city != null) {
      stringBuilder.append("," + city);
    }
    if (province != null) {
      stringBuilder.append("," + province);
    }
    if (postal != null) {
      stringBuilder.append("," + postal);
    }
    professionalSpecialist.setStreetAddress(stringBuilder.toString());
    professionalSpecialist.setLastUpdated(contact.getUpdateDate());
    professionalSpecialist.setPhoneNumber(contact.getWorkPhone());
    professionalSpecialist.setFaxNumber(contact.getFax());
    professionalSpecialist.setWebSite(contact.getNote());
    professionalSpecialist.setAnnotation(contact.getNote());
    professionalSpecialist.setEmailAddress(contact.getEmail());
    this.professionalSpecialist = professionalSpecialist;
  }

  public List<CodeableConceptDt> getContactRelationships() {
    return contactRelationships;
  }

  public void setContactRelationships(List<CodeableConceptDt> contactRelationships) {
    this.contactRelationships = contactRelationships;
  }

  private void setFhirContactFromDemographicContact(
      ca.uhn.fhir.model.dstu2.resource.Patient.Contact contact) {
    if (demographicContact != null) {
      setContact(demographicContact.getDetails());
      contact.addRelationship().addCoding().setCode("Relationship")
          .setDisplay(demographicContact.getRole());
      contact.addRelationship().addCoding().setCode("Emergency")
          .setDisplay(demographicContact.getEc());
      contact.addRelationship().addCoding().setCode("Legal")
          .setDisplay(demographicContact.getSdm());
      setFhirContact(contact);
    }
  }

  private void setFhirContactFromDemographicContact(
      ca.uhn.fhir.model.dstu2.resource.Patient.Contact[] contacts) {
    if (demographicContacts != null) {
      for (int i = 0; i < demographicContacts.length; i++) {
        this.setDemographicContact(demographicContacts[i]);
        this.setFhirContactFromDemographicContact(
            new ca.uhn.fhir.model.dstu2.resource.Patient.Contact());
        contacts[i] = this.fhirContact;
        this.setDemographicContact(null);
        this.setFhirContact(null);
      }
    }
    this.fhirContacts = contacts;
  }

  private void setFhirContacts(ca.uhn.fhir.model.dstu2.resource.Patient.Contact[] fhirContacts) {
    if (contacts != null) {
      for (int i = 0; i < contacts.length; i++) {
        this.setContact(contacts[i]);
        this.setFhirContact(new ca.uhn.fhir.model.dstu2.resource.Patient.Contact());
        fhirContacts[i] = this.fhirContact;
        this.setContact(null);
        this.setFhirContact(null);
      }
    }
    this.fhirContacts = fhirContacts;
  }

  private void setContactFromOrganization(Organization organization) {
  }

  private void setContactFromLocation(Location location) {
  }

  /**
   * Get a FHIR Contact object.
   */
  private void setFhirContact(ca.uhn.fhir.model.dstu2.resource.Patient.Contact fhirContact) {
    if (fhirContact != null && getContact() != null) {
      fhirContact.getName().addFamily(getContact().getLastName());
      fhirContact.getName().addGiven(getContact().getFirstName());
      fhirContact.getAddress().addLine(getContact().getAddress());
      fhirContact.getAddress().setCity(getContact().getCity());
      fhirContact.getAddress().setState(getContact().getProvince());
      fhirContact.getAddress().setPostalCode(getContact().getPostal());
      fhirContact.addTelecom()
          .setSystem(ContactPointSystemEnum.PHONE)
          .setValue(getContact().getResidencePhone())
          .setRank(new PositiveIntDt(1));
      fhirContact.addTelecom()
          .setSystem(ContactPointSystemEnum.PHONE)
          .setValue(getContact().getWorkPhone())
          .setRank(new PositiveIntDt(2));
      fhirContact.addTelecom()
          .setSystem(ContactPointSystemEnum.FAX)
          .setValue(getContact().getFax());
      fhirContact.addTelecom()
          .setSystem(ContactPointSystemEnum.EMAIL)
          .setValue(getContact().getEmail())
          .setUse(ContactPointUseEnum.HOME);
      if (this.contactRelationships != null) {
        fhirContact.getRelationship().addAll(this.contactRelationships);
      }
    }
    this.fhirContact = fhirContact;
  }
}
