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

import ca.openosp.openo.commn.dao.CVCImmunizationDao;
import ca.openosp.openo.commn.dao.LookupListDao;
import ca.openosp.openo.commn.dao.LookupListItemDao;
import ca.openosp.openo.commn.dao.PartialDateDao;
import ca.openosp.openo.commn.dao.PreventionDao;
import ca.openosp.openo.commn.model.AbstractModel;
import ca.openosp.openo.commn.model.CVCImmunization;
import ca.openosp.openo.commn.model.LookupList;
import ca.openosp.openo.commn.model.LookupListItem;
import ca.openosp.openo.commn.model.PartialDate;
import ca.openosp.openo.commn.model.Prevention;
import ca.openosp.openo.integration.fhir.r4.api.DHIR;
import ca.openosp.openo.integration.fhir.r4.interfaces.ImmunizationInterface;
import ca.openosp.openo.integration.fhir.r4.manager.OscarFhirConfigurationManager;
import ca.openosp.openo.util.UtilDateUtilities;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Immunization.ImmunizationStatus;
import org.hl7.fhir.r4.model.Reference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * constraint: Oscar class must implement ImmunizationInterface.
 */
public class Immunization<T extends AbstractModel<Integer> & ImmunizationInterface>
    extends AbstractOscarFhirResource<org.hl7.fhir.r4.model.Immunization, T> {

  static Logger logger = MiscUtils.getLogger();
  private static final Pattern measurementValuePattern = Pattern.compile("^([0-9])*(\\.)*([0-9])*");
  private boolean isHistorical;

  public Immunization(T from) {
    super(new org.hl7.fhir.r4.model.Immunization(), from);
  }

  public Immunization(T from, OscarFhirConfigurationManager configurationManager) {
    super(new org.hl7.fhir.r4.model.Immunization(), from, configurationManager);
  }

  @SuppressWarnings("unchecked")
  public Immunization(org.hl7.fhir.r4.model.Immunization from) {
    super((T) new Prevention(), from);
  }

  @Override
  protected void mapAttributes(T immunization) {
    // this is important to initialize easy access of the hash list of properties populated from the PreventionExt table.
    if (immunization instanceof Prevention) {
      ((Prevention) immunization).setPreventionExtendedProperties();
    }
    setAdministrationDate(immunization);
    setVaccineCode(immunization);
    setVaccineCode2(immunization);
    setRefused(immunization);
    setLotNumber(immunization);
    setExpirationDate(immunization);
    setSite(immunization);
    setDose(immunization);
    setRoute(immunization);
    setAnnotation(immunization);
  }

  @Override
  protected final void mapAttributes(org.hl7.fhir.r4.model.Immunization immunization) {
    // this is important to initialize easy access of the hash list of properties populated from the PreventionExt table.
    if (getOscarResource() instanceof Prevention) {
      ((Prevention) getOscarResource()).setPreventionExtendedProperties();
    }
    // this is a particular requirement for the DHIR - but it may have other applications.
    if (include(OptionalFHIRAttribute.dateIsEstimated)) {
      setHistorical(getOscarResource().isHistorical(14));
    }
    //mandatory
    setIsPrimarySource(immunization);
    setStatus(immunization);
    setAdministrationDate(immunization);
    setVaccineCode(immunization);
    setVaccineCode2(immunization);
    setRefused(immunization);
    setLotNumber(immunization);
    setExpirationDate(immunization);
    setSite(immunization);
    setDose(immunization);
    setRoute(immunization);
    setReason(immunization);
    // optional
    if (include(OptionalFHIRAttribute.annotation)) {
      setAnnotation(immunization);
    }
    if (!immunization.getPrimarySource()) {
      setReportOrigin(immunization);
    }
  }

  /**
   * Returns the Oscar patient Id for whom this immunization was for.
   */
  public int getDemographicNo() {
    return getOscarResource().getDemographicId();
  }

  @Override
  protected final void setId(org.hl7.fhir.r4.model.Immunization fhirResource) {
    if (getOscarResource() != null && getOscarResource().getId() != null) {
      fhirResource.setId(getOscarResource().getId() + "");
    } else {
      super.setId(fhirResource);
    }
  }

  /**
   * Status of the immunization. Options are Completed or NULL
   * When this status is coded as NULL it is assumed that the immunization was refused or
   * omitted.
   * It is assumed that this method will never consume Preventions coded as deleted.
   */
  private void setStatus(org.hl7.fhir.r4.model.Immunization immunization) {
    ImmunizationStatus immunizationStatus = ImmunizationStatus.NULL;
    if (getOscarResource().isComplete()) {
      immunizationStatus = ImmunizationStatus.COMPLETED;
    }
    immunization.setStatus(immunizationStatus);
  }

  /**
   * The extension URI for the administration date indicates if the immunization date was estimated.
   *
   */
  private void setAdministrationDate(org.hl7.fhir.r4.model.Immunization immunization) {
    immunization.getOccurrenceDateTimeType().setValue(getOscarResource().getImmunizationDate());
    PartialDateDao partialDateDao = SpringUtils.getBean(PartialDateDao.class);
    boolean partialDate = false;
    if (partialDateDao.getPartialDate(PartialDate.PREVENTION, getOscarResource().getId(),
        PartialDate.PREVENTION_PREVENTIONDATE) != null) {
      String preventionDate = UtilDateUtilities.DateToString(
          getOscarResource().getImmunizationDate(), "yyyy-MM-dd HH:mm");
      String prevDate = partialDateDao.getDatePartial(preventionDate, PartialDate.PREVENTION,
          getOscarResource().getId(), PartialDate.PREVENTION_PREVENTIONDATE);
      immunization.getOccurrenceDateTimeType().setValueAsString(prevDate);
      partialDate = true;
    }
    if (partialDate || include(OptionalFHIRAttribute.dateIsEstimated)) {
      //TODO the number of days to estimate a historical date will need to fetched from the configuration settings.
      BooleanType estimated = new BooleanType();
      estimated.setValue(isHistorical());
      immunization.getOccurrenceDateTimeType()
          .addExtension()
          .setUrl(DHIR.BASE_STRUCTURE + "/ca-on-extension-estimated-date")
          .setValue(estimated);
    }
  }

  private void setAdministrationDate(ImmunizationInterface immunization) {
  }

  /**
   * SNOMED is a fixed (static) system in Oscar.
   */
  private void setVaccineCode(org.hl7.fhir.r4.model.Immunization immunization) {
    CVCImmunizationDao cvcImmDao = SpringUtils.getBean(CVCImmunizationDao.class);
    if (!StringUtils.isEmpty(getOscarResource().getVaccineCode2())) {
      CVCImmunization cvcImm = cvcImmDao.findBySnomedConceptId(
          getOscarResource().getVaccineCode2());
      if (cvcImm != null) {
        logger.error("cvcImm " + cvcImm.getDisplayName());
      }
      immunization.getVaccineCode().addCoding()
          .setSystem("http://snomed.info/sct")
          .setCode(getOscarResource().getVaccineCode2())
          .setDisplay((getOscarResource().getName()).trim());
    } else {
      if (!StringUtils.isEmpty(getOscarResource().getVaccineCode())) {
        String display = getOscarResource().getName().trim();
        if (StringUtils.isEmpty(display) || !StringUtils.isEmpty(
            getOscarResource().getVaccineCode2())) {
          CVCImmunization cvcImm = cvcImmDao.findBySnomedConceptId(
              getOscarResource().getVaccineCode());
          if (cvcImm != null) {
            display = cvcImm.getDisplayName();
          }
        }
        immunization.getVaccineCode().addCoding()
            .setSystem("http://snomed.info/sct")
            .setCode(getOscarResource().getVaccineCode())
            .setDisplay(display);
      }
    }
  }

  private void setVaccineCode(ImmunizationInterface immunization) {
    immunization.setVaccineCode(getFhirResource().getVaccineCode().getCodingFirstRep().getCode());
  }

  /**
   * SNOMED is a fixed (static) system in Oscar.
   */
  private void setVaccineCode2(org.hl7.fhir.r4.model.Immunization immunization) {
  }

  private void setVaccineCode2(ImmunizationInterface immunization) {
  }


  private void setRefused(org.hl7.fhir.r4.model.Immunization immunization) {
  }

  private void setRefused(ImmunizationInterface immunization) {
  }

  private void setLotNumber(org.hl7.fhir.r4.model.Immunization immunization) {
    immunization.setLotNumber(getOscarResource().getLotNo());
  }

  private void setLotNumber(ImmunizationInterface immunization) {
    immunization.setLotNo(getFhirResource().getLotNumber());
  }

  private void setExpirationDate(org.hl7.fhir.r4.model.Immunization immunization) {
    immunization.setExpirationDate(getOscarResource().getExpiryDate());
  }

  private void setExpirationDate(ImmunizationInterface immunization) {
    immunization.setExpiryDate(getFhirResource().getExpirationDate());
  }

  /**
   * This is the body part - or location - the immunization was given.
   */
  private void setSite(org.hl7.fhir.r4.model.Immunization immunization) {
    if (!StringUtils.isEmpty(getOscarResource().getSite())) {
      immunization.getSite().setText(mapSite(getOscarResource().getSite()));
      LookupListDao lookupListDao = SpringUtils.getBean(LookupListDao.class);
      LookupListItemDao lookupListItemDao = SpringUtils.getBean(LookupListItemDao.class);
      LookupList ll = lookupListDao.findByName("AnatomicalSite");
      LookupListItem lli = null;
      if (ll != null) {
        lli = lookupListItemDao.findByLookupListIdAndValue(ll.getId(),
            getOscarResource().getSite());
      }
      Coding cc = immunization.getSite().addCoding();
      cc.setSystem("http://snomed.info/sct");
      cc.setCode(getOscarResource().getSite());
      cc.setDisplay(getOscarResource().getSite());
      if (lli != null) {
        cc.setDisplay(lli.getLabel());
      }
    }
  }

  private String mapSite(String oscarSite) {
    return oscarSite;
  }

  private void setSite(ImmunizationInterface immunization) {
    if (!StringUtils.isEmpty(getFhirResource().getSite().getText())) {
      immunization.setSite(mapSite(getFhirResource().getSite().getText()));
    }
  }

  private void setDose(org.hl7.fhir.r4.model.Immunization immunization) {
    String dose = getOscarResource().getDose();
    if (StringUtils.isEmpty(dose)) {
      return;
    }
    Matcher matcher = measurementValuePattern.matcher(dose);
    String number = "";
    double value = 0.0;
    if (matcher.find()) {
      number = matcher.group(0);
      if (!number.isEmpty()) {
        value = Double.parseDouble(number);
      }
    }
    String unit = dose.replace(number, "").trim();
    immunization.getDoseQuantity().setValue(value).setUnit(unit);
  }

  private void setDose(ImmunizationInterface immunization) {
    immunization.setDose(getFhirResource().getDoseQuantity().getValue().toString() + " "
        + getFhirResource().getDoseQuantity().getUnit());
  }

  private void setRoute(org.hl7.fhir.r4.model.Immunization immunization) {
    if (!StringUtils.isEmpty(getOscarResource().getRoute())) {
      String oscarRouteCode = getOscarResource().getRoute();
      String display = getOscarResource().getRoute();
      LookupListDao lookupListDao = SpringUtils.getBean(LookupListDao.class);
      LookupListItemDao lookupListItemDao = SpringUtils.getBean(LookupListItemDao.class);
      LookupList ll = lookupListDao.findByName("RouteOfAdmin");
      LookupListItem lli = null;
      if (ll != null) {
        lli = lookupListItemDao.findByLookupListIdAndValue(ll.getId(),
            getOscarResource().getRoute());
        MiscUtils.getLogger()
            .info("ll.id=" + ll.getId() + ",site=" + getOscarResource().getRoute());
        MiscUtils.getLogger().info("lli=" + lli);
        if (lli != null) {
          display = lli.getLabel();
        }
      }
      immunization.getRoute().addCoding()
          .setSystem("http://snomed.info/sct")
          .setCode(oscarRouteCode)
          .setDisplay(display);
    }
  }

  private void setRoute(ImmunizationInterface immunization) {
    if (!StringUtils.isEmpty(getFhirResource().getRoute().getText())) {
      immunization.setRoute(getFhirResource().getRoute().getText());
    }
  }

  private void setAnnotation(org.hl7.fhir.r4.model.Immunization immunization) {
    immunization.addNote().setText(getOscarResource().getImmunizationRefusedReason());
    immunization.addNote().setText(getOscarResource().getComment());
  }

  private void setAnnotation(ImmunizationInterface immunization) {
    StringBuilder note = new StringBuilder("");
    note.append(getFhirResource().getLocation());

    for (org.hl7.fhir.r4.model.Annotation annotation : getFhirResource().getNote()) {
      note.append(annotation.getText());
    }

    immunization.setComment(note.toString());
  }

  /**
   * For now the immunization reason is hard coded to routine.
   */
  private void setReason(org.hl7.fhir.r4.model.Immunization immunization) {
  }

  /**
   * Determined if the user has selected if the immunization has been "Done Externally".
   * If the dateIsEstimated is enabled. This value will also be set to NOT be a primary source.
   */
  private void setIsPrimarySource(org.hl7.fhir.r4.model.Immunization immunization) {

    boolean primarySource = getOscarResource().isPrimarySource();

    if (include(OptionalFHIRAttribute.dateIsEstimated)) {
      if (primarySource) {
        primarySource = !isHistorical();
      }
    }

    immunization.setPrimarySource(primarySource);
  }

  /**
   * This method is triggered when the user has indicated that the immunization has been performed externally.
   * The display value is changed if the dateIsEstimated is enabled.
   * The source of the data when the report of the immunization event is not based on
   * information from the person who administered the vaccine.
   */
  private void setReportOrigin(org.hl7.fhir.r4.model.Immunization immunization) {
    String provider = "223366009";
    String display = "Health care provider";
    PreventionDao preventionDao = SpringUtils.getBean(PreventionDao.class);
    Prevention prevention = preventionDao.find(getOscarResource().getImmunizationId());
    if (include(OptionalFHIRAttribute.dateIsEstimated)) {
      if (isHistorical() || "-1".equals(prevention.getProviderNo())) {
        provider = "116154003";
        display = "Client";
      }
    } else {
      if ("-1".equals(prevention.getProviderNo())) {
        provider = "116154003";
        display = "Client";
      }
    }
    immunization.getReportOrigin().addCoding()
        .setSystem(
            "http://snomed.info/sct") // WAS THIS --> but simplifier shows it should be <-- http://hl7.org/fhir/immunization-origin")
        .setCode(provider)
        .setDisplay(display);
  }

  /**
   * All practitioners added here are ALWAYS the administering provider.
   * This is the provider that gave the immunization.
   */
  public void addPerformingPractitioner(Reference reference) {
    getFhirResource().addPerformer()
        .setActor(reference)
        .getFunction().addCoding()
        .setSystem("http://terminology.hl7.org/CodeSystem/v2-0443")
        .setCode("AP")
        .setDisplay("Administering Provider");
  }

  /**
   * This will add a reference link to any involved practitioner.
   * Not to be confused with administering provider.
   */
  public void addPractitioner(Reference reference) {
    getFhirResource().addPerformer().setActor(reference);
  }

  /**
   * This is a reference link to whom the immunization was given to.
   */
  public void setPatientReference(Reference reference) {
    getFhirResource().setPatient(reference);
  }

  public boolean isHistorical() {
    return isHistorical;
  }

  public void setHistorical(boolean isHistorical) {
    this.isHistorical = isHistorical;
  }
}
