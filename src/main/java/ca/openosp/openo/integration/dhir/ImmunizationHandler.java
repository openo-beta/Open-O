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

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationPerformerComponent;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.oscarehr.integration.fhir.r4.api.DHIR;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Map;

public class ImmunizationHandler {

  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

  Immunization immunization;

  public ImmunizationHandler(Immunization immunization) {
    this.immunization = immunization;
  }

  @Nullable
  public String getImmunizationDate() {
    if (immunization.hasOccurrenceDateTimeType()) {
      Calendar cal = immunization.getOccurrenceDateTimeType().getValueAsCalendar();
      Extension ext = immunization.getOccurrenceDateTimeType()
          .getExtensionByUrl(DHIR.BASE_STRUCTURE + "/ca-on-extension-estimated-date");
      String estimated =
          (ext != null && (Boolean) ext.getValueAsPrimitive().getValue()) ? " (Estimated)" : "";
      return sdf.format(cal.getTime()) + estimated;
    } else if (immunization.hasOccurrenceStringType()) {
      return immunization.getOccurrenceStringType().getValue();
    }
    return null;
  }

  @Nullable
  public Boolean getValidFlag() {
    Extension ext = immunization.getExtensionByUrl(
        DHIR.BASE_STRUCTURE + "/ca-on-immunizations-extension-valid-flag");
    if (ext != null) {
      return ((BooleanType) ext.getValue()).getValue();
    }
    return null;
  }

  @Nullable
  public String getAgent() {
    if (immunization.getVaccineCode().getCoding().size() > 0) {
      Coding coding = immunization.getVaccineCode().getCoding().get(0);
      if ("http://snomed.info/sct".equals(coding.getSystem())) {
        return coding.getDisplay();
      }
    }
    return null;
  }

  @Nullable
  public String getTradeName() {
    if (immunization.getVaccineCode().getCoding().size() > 1) {
      Coding coding = immunization.getVaccineCode().getCoding().get(1);
      return coding.getDisplay();
    }
    return null;
  }

  @Nullable
  public String getLotNumber() {
    if (immunization.hasLotNumber()) {
      return immunization.getLotNumber();
    }
    return null;
  }

  @Nullable
  public String getStatus() {
    if (immunization.hasStatus()) {
      return immunization.getStatus().getDisplay();
    }
    return null;
  }

  @Nullable
  public String getPHU() {
    Extension ext = immunization.getExtensionByUrl(
        DHIR.BASE_STRUCTURE + "/ca-on-immunizations-extension-public-health-unit");
    if (ext != null) {
      return ((StringType) ext.getValue()).getValue();
    }
    return null;
  }

  @Nullable
  public String getPerformerName(Map<String, Resource> allResources) {
    if (immunization.hasPerformer()) {
      for (ImmunizationPerformerComponent ipc : immunization.getPerformer()) {
        if (ipc.hasFunction()) {
          CodeableConcept cc = ipc.getFunction();
          if (cc.hasCoding()) {
            for (Coding coding : cc.getCoding()) {
              if ("http://terminology.hl7.org/CodeSystem/v2-0443".equals(coding.getSystem())
                  && "AP".equals(coding.getCode())) {
                Reference ref = ipc.getActor();
                String reference = ref.getReference();
                reference = reference.substring(reference.lastIndexOf("/") + 1);
                Resource resource = allResources.get(reference);
                if (resource != null) {
                  Practitioner practitioner = (Practitioner) resource;
                  HumanName hn = practitioner.getNameFirstRep();
                  return hn.getFamily() + "," + hn.getGivenAsSingleString();
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  @Nullable
  public String getExpirationDate() {
    if (immunization.hasExpirationDate()) {
      return sdf.format(immunization.getExpirationDate());
    }
    return null;
  }

  public static final Comparator<ImmunizationHandler> DATE_SORT = new Comparator<ImmunizationHandler>() {
    @Override
    public int compare(ImmunizationHandler i1, ImmunizationHandler i2) {
      String i1DateStr = i1.getImmunizationDate().replaceAll("(Estimated)", "").trim();
      String i2DateStr = i2.getImmunizationDate().replaceAll("(Estimated)", "").trim();
      return i1DateStr.compareTo(i2DateStr);
    }
  };
}
