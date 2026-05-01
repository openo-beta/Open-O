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
package ca.openosp.openo.integration.fhir.dstu3.manager;

import ca.openosp.OscarProperties;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.DemographicExt;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.commn.model.Prevention;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.integration.fhir.dstu3.model.AbstractOscarFhirResource;
import ca.openosp.openo.integration.fhir.dstu3.model.Immunization;
import ca.openosp.openo.integration.fhir.dstu3.model.Patient;
import ca.openosp.openo.integration.fhir.dstu3.model.PerformingPractitioner;
import ca.openosp.openo.integration.fhir.dstu3.model.Practitioner;
import ca.openosp.openo.integration.fhir.dstu3.resources.types.PublicHealthUnitType;
import ca.openosp.openo.log.LogAction;
import ca.openosp.openo.managers.DemographicManager;
import ca.openosp.openo.managers.LookupListManager;
import ca.openosp.openo.managers.PreventionManager;
import ca.openosp.openo.managers.ProviderManager2;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.hl7.fhir.dstu3.model.Identifier;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class OscarFhirResourceManager {

    /**
     * Retrieves a list of immunizations for a specific demographic number.
     *
     * @param configurationManager The configuration manager providing access to logged-in user information and general configurations.
     * @param demographicNo The unique demographic number identifying the individual whose immunization records are to be retrieved.
     * @return A list of {@code Immunization<Prevention>} objects representing the immunizations associated with the specified demographic number,
     *         or {@code null} if no immunizations are found.
     */
  public static List<Immunization<Prevention>> getImmunizationsByDemographicNo(
		  OscarFhirConfigurationManager configurationManager, int demographicNo) {
    PreventionManager preventionManager = SpringUtils.getBean(PreventionManager.class);
    //TODO what kind of security check goes here?
    List<Immunization<Prevention>> immunizations = null;
    List<Prevention> preventions = preventionManager.getPreventionsByDemographicNo(
        configurationManager.getLoggedInInfo(), demographicNo);
    if (preventions != null) {
      LogAction.addLogSynchronous(configurationManager.getLoggedInInfo(),
          "OscarFhirResourceManager.getImmunizationsByDemographicNo",
          "Retrieved Immunization list for FHIR transport ");
      for (Prevention prevention : preventions) {
        //TODO there needs to be a better method to identify an ISPA Immunization.  This "isImmunization" method can be changed
        if (prevention.isImmunization()) {
          if (immunizations == null) {
            immunizations = new ArrayList<Immunization<Prevention>>();
          }
          Immunization<Prevention> immunization = new Immunization<Prevention>(
              prevention, configurationManager);
          immunizations.add(immunization);
        }
      }
    }
    return immunizations;
  }

    /**
     * Retrieves an immunization object based on the specified prevention ID.
     *
     * @param configurationManager the OscarFhirConfigurationManager instance containing configuration and logged-in user details
     * @param preventionId the unique identifier of the prevention associated with the immunization
     * @return an Immunization instance associated with the specified prevention ID, or null if no matching prevention is found
     */
  public static Immunization<Prevention> getImmunizationById(
		  OscarFhirConfigurationManager configurationManager, int preventionId) {
    PreventionManager preventionManager = SpringUtils.getBean(PreventionManager.class);
    Prevention prevention = preventionManager.getPrevention(configurationManager.getLoggedInInfo(),
        preventionId);
    Immunization<Prevention> immunization = null;
    if (prevention != null) {
      LogAction.addLogSynchronous(configurationManager.getLoggedInInfo(),
          "OscarFhirResourceManager.getImmunizationsByDemographicNo",
          "Retrieved Immunization list for FHIR transport ");
      immunization = new Immunization<Prevention>(prevention,
          configurationManager);
    }
    return immunization;
  }

    /**
     * Retrieves a patient using a specified demographic number.
     *
     * @param configurationManager the configuration manager used to access logged-in information
     * @param demographic_no the demographic number of the patient to retrieve
     * @return a Patient object if the demographic exists, otherwise returns null
     */
  public static Patient getPatientByDemographicNumber(
		  OscarFhirConfigurationManager configurationManager, int demographic_no) {
    DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
    Demographic demographic = demographicManager.getDemographic(
        configurationManager.getLoggedInInfo(), demographic_no);
    Patient patient = null;
    if (demographic != null) {
      patient = new Patient(demographic, configurationManager);
      LogAction.addLogSynchronous(configurationManager.getLoggedInInfo(),
          "OscarFhirResourceManager.getPatientByDemographicNumber",
          "Retrieved demographic " + demographic_no + " " + patient.toString());
    }
    return patient;
  }

    /**
     * Retrieves a list of patients based on the provided health card number (PHN) and health card type.
     *
     * @param configurationManager The configuration manager used for accessing logged-in information and context.
     * @param hcn The health card number (PHN) used to search for patients.
     * @param hcnType The type of the health card number (e.g., provincial, federal) used in the search.
     * @return A list of patients matching the specified health card number and type, or null if none are found.
     */
  public static List<Patient> getPatientsByPHN(
		  OscarFhirConfigurationManager configurationManager, String hcn, String hcnType) {
    DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
    List<Demographic> demographicList = demographicManager.getActiveDemosByHealthCardNo(
        configurationManager.getLoggedInInfo(), hcn, hcnType);
    List<Patient> patientList = null;
    if (demographicList != null) {
      LogAction.addLogSynchronous(configurationManager.getLoggedInInfo(),
          "OscarFhirResourceManager.getPatientsByPHN",
          "Retrieved demographic hcn " + hcn + " " + demographicList.toString());
      for (Demographic demographic : demographicList) {

        if (patientList == null) {
          patientList = new ArrayList<Patient>();
        }
        Patient patient = new Patient(
            demographic, configurationManager);
        patientList.add(patient);
      }
    }
    return patientList;
  }

    /**
     * Retrieves the Practitioner who is the most responsible provider for a given demographic.
     *
     * This method accesses the demographic information and determines the most responsible provider.
     * If no explicit most-responsible provider is associated with the demographic, the method retrieves
     * the most-responsible providers list and selects the first provider from the list.
     *
     * @param configurationManager the configuration manager containing the logged-in user's context
     *                             and configuration details
     * @param demographic_no       the unique identifier of the demographic whose most
     *                             responsible provider is to be retrieved
     * @return the Practitioner object representing the most responsible provider for the provided
     *         demographic, or null if no such provider exists
     */
  public static Practitioner getDemographicMostResponsiblePractitioner(
		  OscarFhirConfigurationManager configurationManager, int demographic_no) {
    DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
    Demographic demographic = demographicManager.getDemographic(
        configurationManager.getLoggedInInfo(), demographic_no);
    Provider mrp = demographic.getProvider();
    List<Provider> providerList = Collections.emptyList();
    Practitioner practitioner = null;
    if (mrp == null) {
      providerList = demographicManager.getDemographicMostResponsibleProviders(
          configurationManager.getLoggedInInfo(), demographic_no);
    }
    if (!providerList.isEmpty()) {
      mrp = providerList.get(0);
    }
    if (mrp != null) {
      practitioner = new Practitioner(mrp, configurationManager);
    }
    return practitioner;
  }

    /**
     * Retrieves the performing practitioner associated with the specified provider number.
     *
     * @param configurationManager The configuration manager containing the logged-in user's information.
     * @param providerNo The provider number for which the performing practitioner needs to be retrieved.
     * @return The PerformingPractitioner object if a valid provider is found for the given provider number, 
     *         or null if no matching provider is found.
     */
  public static PerformingPractitioner getPerformingPractitionerByProviderNumber(
		  OscarFhirConfigurationManager configurationManager, String providerNo) {
    ProviderManager2 providerManager = SpringUtils.getBean(ProviderManager2.class);
    Provider provider = providerManager.getProvider(configurationManager.getLoggedInInfo(),
        providerNo);
    PerformingPractitioner practitioner = null;
    if (provider != null) {
      practitioner = new PerformingPractitioner(provider,
          configurationManager);
      LogAction.addLogSynchronous(configurationManager.getLoggedInInfo(),
          "OscarFhirResourceManager.getProviderByProviderNumber",
          "Retrieved provider " + providerNo + " " + provider.toString());
    }
    return practitioner;
  }

    /**
     * Retrieves a Practitioner based on the given provider number.
     *
     * @param configurationManager An instance of OscarFhirConfigurationManager used to obtain configuration and logged-in information.
     * @param providerNo           The provider number used to identify the desired provider.
     * @return A Practitioner object representing the provider associated with the given provider number,
     *         or null if no such provider is found.
     */
  public static Practitioner getPractitionerByProviderNumber(
		  OscarFhirConfigurationManager configurationManager, String providerNo) {
    ProviderManager2 providerManager = SpringUtils.getBean(ProviderManager2.class);
    Provider provider = providerManager.getProvider(configurationManager.getLoggedInInfo(),
        providerNo);
    Practitioner practitioner = null;
    if (provider != null) {
      practitioner = new Practitioner(provider,
          configurationManager);
      LogAction.addLogSynchronous(configurationManager.getLoggedInInfo(),
          "OscarFhirResourceManager.getProviderByProviderNumber",
          "Retrieved provider " + providerNo + " " + provider.toString());
    }
    return practitioner;
  }

  /**
   * Builds a list of linked resources of Immunization data by patient for insertion into a message
   * Bundle The returned HashSet contains: - Immunizations - Patient - SubmittingPractitioner -
   * PerformingPractitioner
   */
  public static HashSet<AbstractOscarFhirResource<?, ?>> getImmunizationResourceBundle(
      OscarFhirConfigurationManager configurationManager,
      Patient patient,
      HashSet<AbstractOscarFhirResource<?, ?>> resourceList) {

    List<Immunization<Prevention>> immunizations = OscarFhirResourceManager.getImmunizationsByDemographicNo(
        configurationManager, patient.getOscarResource().getDemographicNo());
    if (immunizations != null) {
      OscarFhirResourceManager.linkPerformingPractitionerAndPatient(configurationManager,
          immunizations, patient, resourceList);
    }
    return resourceList;
  }

  /**
   * Builds a list of linked resources of Immunization data by patient for insertion into a message
   * Bundle The returned HashSet contains: - Immunizations - Patient - SubmittingPractitioner -
   * PerformingPractitioner
   */
  public static HashSet<AbstractOscarFhirResource<?, ?>> getImmunizationResourceBundle(
		  OscarFhirConfigurationManager configurationManager,
		  Patient patient, int preventionId,
		  HashSet<AbstractOscarFhirResource<?, ?>> resourceList) {
    Immunization<Prevention> immunization = OscarFhirResourceManager.getImmunizationById(
        configurationManager, preventionId);
    if (immunization != null) {
      OscarFhirResourceManager.linkPerformingPractitionerAndPatient(configurationManager,
          immunization, patient, resourceList);
    } else {
      MiscUtils.getLogger().warn("Requested Immunization id " + preventionId + " was not found.");
    }
    return resourceList;
  }

    /**
     * Retrieves the public health unit as an HL7 FHIR Organization resource based on the
     * provided demographic number and configuration manager.
     *
     * @param configurationManager the configuration manager providing logged-in user information and settings
     * @param demographicNo the demographic number associated with the public health unit
     * @return the public health unit as an {@code org.hl7.fhir.dstu3.model.Organization} object,
     *         or {@code null} if the public health unit cannot be determined
     */
  public static org.hl7.fhir.dstu3.model.Organization getPublicHealthUnit(
		  OscarFhirConfigurationManager configurationManager, int demographicNo) {

    DemographicManager demographicManager = SpringUtils.getBean(DemographicManager.class);
    DemographicExt demographicExt = demographicManager.getDemographicExt(
        configurationManager.getLoggedInInfo(), demographicNo,
        DemographicExt.DemographicProperty.PHU.toString());
    String phuId = null;
    org.hl7.fhir.dstu3.model.Organization organization = null;
    if (demographicExt != null) {
      phuId = demographicExt.getValue();
    }
    PublicHealthUnitType publicHealthUnitType = getPublicHealthUnitType(configurationManager,
        phuId);
    if (publicHealthUnitType != null) {
      organization = new org.hl7.fhir.dstu3.model.Organization();
      organization.setId(UUID.randomUUID().toString());
      Identifier identifier = new Identifier();
      identifier.setSystem(publicHealthUnitType.getSystemURI())
          .setValue(publicHealthUnitType.getId());
      organization.addIdentifier(identifier);
      organization.setName(publicHealthUnitType.getName());
    }
    return organization;
  }

  /* PRIVATE HELPER METHODS BELOW THIS LINE */

  /**
   * Helper method intended for use from inside the class.
   *
   * @param configurationManager
   * @param immunization
   * @param patient
   * @param resourceList
   * @return HashSet<OscarFhirResource < ?, ?>>
   */
  private static HashSet<AbstractOscarFhirResource<?, ?>> linkPerformingPractitionerAndPatient(
      OscarFhirConfigurationManager configurationManager,
      Immunization<Prevention> immunization,
      Patient patient,
      HashSet<AbstractOscarFhirResource<?, ?>> resourceList) {
    String performingProviderNo = immunization.getOscarResource().getProviderNo();
    if (performingProviderNo != null && !"-1".equals(performingProviderNo)) {
      PerformingPractitioner performingPractitioner = OscarFhirResourceManager.getPerformingPractitionerByProviderNumber(
          configurationManager, performingProviderNo);
      if (performingPractitioner != null) {
        immunization.addPerformingPractitioner(performingPractitioner.getReference());
        resourceList.add(performingPractitioner);
      }
    } else if ("-1".equals(performingProviderNo)) {
      Provider provider = new Provider();
      provider.setProviderNo(UUID.randomUUID().toString().substring(0, 8));
      PerformingPractitioner performingPractitioner = new PerformingPractitioner(
          provider, configurationManager);
      immunization.addPerformingPractitioner(performingPractitioner.getReference());
      resourceList.add(performingPractitioner);
    }
    immunization.setPatientReference(patient.getReference());
    resourceList.add(patient);
    resourceList.add(immunization);
    return resourceList;
  }

  /**
   * Helper method. Intended for use inside the class.
   *
   * @param configurationManager
   * @param immunizations
   * @param patient
   * @param resourceList
   * @return HashSet<OscarFhirResource < ?, ?>>
   */
  private static HashSet<AbstractOscarFhirResource<?, ?>> linkPerformingPractitionerAndPatient(
      OscarFhirConfigurationManager configurationManager,
      List<Immunization<Prevention>> immunizations,
      Patient patient,
      HashSet<AbstractOscarFhirResource<?, ?>> resourceList) {
    if (immunizations != null && !immunizations.isEmpty()) {
      for (Immunization<Prevention> immunization : immunizations) {
        linkPerformingPractitionerAndPatient(configurationManager, immunization, patient,
            resourceList);
      }
    }
    return resourceList;
  }

  /**
   * Helper method. For use inside the class.
   *
   * @param configurationManager
   * @param phuId
   * @return
   */
  private static PublicHealthUnitType getPublicHealthUnitType(
		  OscarFhirConfigurationManager configurationManager, String phuId) {
    PublicHealthUnitType publicHealthUnitType = null;
    LookupListItem lookupListItem = null;
    if (phuId == null || phuId.isEmpty()) {
      phuId = OscarProperties.getInstance()
          .getProperty(PublicHealthUnitType.PhuKey.default_phu.name(), null);
    }
    LookupListManager lookupListManager = SpringUtils.getBean(LookupListManager.class);
    LookupList lookupList = lookupListManager.findLookupListByName(
        configurationManager.getLoggedInInfo(), PublicHealthUnitType.PhuKey.phu.name());
    if (lookupList != null) {
      lookupListItem = lookupListManager.findLookupListItemByLookupListIdAndValue(
          configurationManager.getLoggedInInfo(), lookupList.getId(), phuId);
    }
    if (lookupListItem != null) {
      publicHealthUnitType = new PublicHealthUnitType(lookupListItem.getValue(),
          lookupListItem.getLabel());
      // TODO inject the system URI from the configuration manager.
      publicHealthUnitType.setSystemURI(
          "https://ehealthontario.ca/API/FHIR/NamingSystem/ca-on-panorama-phu-id");
    }
    return publicHealthUnitType;
  }
}
