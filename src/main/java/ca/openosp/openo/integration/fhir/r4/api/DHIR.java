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
package ca.openosp.openo.integration.fhir.r4.api;

import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.integration.fhir.r4.builder.FhirBundleBuilder;
import ca.openosp.openo.integration.fhir.r4.builder.SenderFactory;
import ca.openosp.openo.integration.fhir.r4.manager.OscarFhirConfigurationManager;
import ca.openosp.openo.integration.fhir.r4.manager.OscarFhirResourceManager;
import ca.openosp.openo.integration.fhir.r4.model.AbstractOscarFhirResource;
import ca.openosp.openo.integration.fhir.r4.model.Patient;
import ca.openosp.openo.integration.fhir.r4.model.Sender;
import ca.openosp.openo.integration.fhir.r4.model.SubmittingPractitioner;
import ca.openosp.openo.integration.fhir.r4.resources.Settings;
import ca.openosp.openo.integration.fhir.r4.resources.constants.FhirDestination;
import ca.openosp.openo.integration.fhir.r4.resources.constants.Region;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import java.util.HashSet;

public class DHIR {

  private static final Settings SETTINGS = new Settings(FhirDestination.DHIR, Region.ON);

  public static String ID_SYSTEM_GLOBAL_BASE = "https://fhir.infoway-inforoute.ca/NamingSystem";
  public static String ID_SYSTEM_LOCAL_BASE = "http://ehealthontario.ca/fhir/NamingSystem";
  public static String CODE_SYSTEM_LOCAL_BASE = "http://ehealthontario.ca/fhir/NamingSystem";
  public static String BASE_STRUCTURE = "http://ehealthontario.ca/fhir/StructureDefinition";
  public static String BASE_CVC = "https://cvc.canimmunize.ca/v2";

  /**
   * Get the FhirBundleBuilder Object for all immunizations
   * Useful for adding additional resources or adjusting the message structure.
   */
  public static synchronized FhirBundleBuilder getFhirBundleBuilder(
      LoggedInInfo loggedInInfo,
      int demographicNo
  ) {
    OscarFhirConfigurationManager configurationManager = new OscarFhirConfigurationManager(
        loggedInInfo, SETTINGS);
    HashSet<AbstractOscarFhirResource<?, ?>> resourceList = new HashSet<>();
    Patient patient = OscarFhirResourceManager.getPatientByDemographicNumber(
        configurationManager, demographicNo);
    // The patient is the focus resource for this type of bundle. A reference link will be inserted into the MessageHeader.focus
    patient.setFocusResource(Boolean.TRUE);
    org.hl7.fhir.r4.model.Organization publicHealthUnit = OscarFhirResourceManager.getPublicHealthUnit(
        configurationManager, demographicNo);
    OscarFhirResourceManager.getImmunizationResourceBundle(configurationManager, patient, resourceList);
    FhirBundleBuilder fhirBundleBuilder = new FhirBundleBuilder(configurationManager);
    if (publicHealthUnit != null) {
      Reference reference = new Reference();
      reference.setReference(String.format(
          "%s/%s",
          ((Resource) publicHealthUnit).getResourceType(),
          publicHealthUnit.getId()
      ));
      reference.setResource(publicHealthUnit);
      fhirBundleBuilder.setMessageHeaderResponsible(reference);
      fhirBundleBuilder.addResource(publicHealthUnit);
    } else {
      MiscUtils.getLogger().warn(
          "Patient and Clinic default PHU ID is missing. Check Oscar properties file for Default PHU setting."
      );
    }
    fhirBundleBuilder.addResources(resourceList);
    return fhirBundleBuilder;
  }

  /**
   * Get the FhirBundleBuilder Object for a specific immunization
   * Useful for adding additional resources or adjusting the message structure.
   */
  public static synchronized FhirBundleBuilder getFhirBundleBuilder(
      LoggedInInfo loggedInInfo,
      int demographicNo,
      int preventionId,
      Provider externalProvider
  ) {
    //TODO this will be set in a properties file later. The default is false anyway.
    SETTINGS.setIncludeSenderEndpoint(Boolean.TRUE);
    OscarFhirConfigurationManager configurationManager = new OscarFhirConfigurationManager(
        loggedInInfo, SETTINGS);
    HashSet<AbstractOscarFhirResource<?, ?>> resourceList = new HashSet<AbstractOscarFhirResource<?, ?>>();
    Patient patient = OscarFhirResourceManager.getPatientByDemographicNumber(
        configurationManager, demographicNo);
    SubmittingPractitioner submittingPractitioner = new SubmittingPractitioner(
        configurationManager.getLoggedInInfo().getLoggedInProvider(), configurationManager);
    FhirBundleBuilder fhirBundleBuilder = new FhirBundleBuilder(configurationManager);
    // Public Health Unit requirement for Ontario submissions.
    //	org.hl7.fhir.r4.model.Organization publicHealthUnit = OscarFhirResourceManager.getPublicHealthUnit( configurationManager, demographicNo );
    // the patient is the focus resource for this type of bundle. A reference link will be inserted into the MessageHeader.focus
    patient.setFocusResource(Boolean.TRUE);
    // set the immunizations into the resource list
    OscarFhirResourceManager.getImmunizationResourceBundle(configurationManager, patient,
        preventionId, resourceList, externalProvider);
    Sender sender = SenderFactory.getSender(SETTINGS, configurationManager);
    sender.getOrganization().setId("Sender");
    sender.getOrganization().setTelecom(null);
    resourceList.add(sender.getOscarFhirResource());
    resourceList.add(submittingPractitioner);
    fhirBundleBuilder.addResources(resourceList);
    return fhirBundleBuilder;
  }

  /**
   * Get the FHIR Resource Bundle.
   * Useful if needing to extract and send specific resources separately from the bundle.
   * ie: send the MessageHeader as a reference back to this bundle resource.
   */
  public static Bundle getBundleResource(LoggedInInfo loggedInInfo, int demographicNo,
                                         int preventionId) {
    return DHIR.getFhirBundleBuilder(loggedInInfo, demographicNo, preventionId, null).getBundle();
  }

  /**
   * Get the raw JSON string of the entire Bundle message.
   * For a single transmission payload.
   */
  public static String getMessageJSON(LoggedInInfo loggedInInfo, int demographicNo,
      int preventionId) {
    return DHIR.getFhirBundleBuilder(loggedInInfo, demographicNo, preventionId, null)
        .getMessageJson();
  }

  /**
   * The Message Header from this bundle message.
   * This is useful for retrieving the destination endpoint, etc...
   */
  public static MessageHeader getMessageHeader(LoggedInInfo loggedInInfo, int demographicNo,
                                               int preventionId) {
    return DHIR.getFhirBundleBuilder(loggedInInfo, demographicNo, preventionId, null)
        .getMessageHeader();
  }
}
