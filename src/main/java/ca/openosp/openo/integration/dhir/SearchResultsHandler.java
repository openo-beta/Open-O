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
package ca.openosp.openo.integration.dhir;

import ca.openosp.openo.utility.MiscUtils;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;

import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultsHandler {

  FhirContext ctx = FhirContext.forR4();

  private static final Logger logger = MiscUtils.getLogger();

  Bundle bundle;

  Map<String, Resource> allResources = new HashMap<>();
  Map<String, Immunization> immunizationResources = new HashMap<>();

  public Date getTimestamp() {
    return bundle.getTimestamp();
  }

  public String getId() {
    return bundle.getId();
  }

  public String getResourceAsString(Resource resource) {
    return ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
  }

  public SearchResultsHandler(Bundle bundle) throws DataFormatException {
    this.bundle = bundle;
    for (BundleEntryComponent comp : bundle.getEntry()) {
      Resource resource = comp.getResource();
      logger.debug("bundle resource: " + resource.getResourceType());
      if (resource.getResourceType() == ResourceType.Immunization) {
        immunizationResources.put(resource.getId(), (Immunization) resource);
      }
      allResources.put(resource.getIdElement().getIdPart(), resource);
    }
  }

  public List<Immunization> getImmunizationResources() {
    return new ArrayList<>(immunizationResources.values());
  }

  public Map<String, Resource> getAllResources() {
    return allResources;
  }
}
