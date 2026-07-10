package ca.openosp.openo.webserv.rest;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.openosp.openo.commn.dao.DemographicDao;
import ca.openosp.openo.commn.exception.AccessDeniedException;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.integration.dhdr.AuditInfo;
import ca.openosp.openo.integration.dhdr.DHDRManager;
import ca.openosp.openo.integration.dhdr.DHDRPrint;
import ca.openosp.openo.integration.dhdr.DHDRServiceException;
import ca.openosp.openo.integration.dhdr.OmdGateway;
import ca.openosp.openo.integration.ohcms.CMSException;
import ca.openosp.openo.integration.oneId.TokenExpiredException;
import ca.openosp.openo.managers.SecurityInfoManager;
import ca.openosp.openo.utility.DateUtils;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.webserv.rest.to.DHDRSearchConfig;
import ca.openosp.openo.webserv.rest.to.model.DHDRErrorTo1;
import ca.openosp.openo.webserv.rest.to.model.MedicationDispenseTo1;
import ca.openosp.openo.webserv.rest.to.model.NotificationTo1;

/**
 * REST service exposing DHDR (Digital Health Drug Repository) dispensed-medication data to the
 * DHDR viewer.
 *
 * <p>This service consumes the OneID/OMD gateway stack ({@link DHDRManager}, {@link OmdGateway},
 * {@link DHDRPrint}) that is already present on the backend. It assumes a valid OneID session
 * exists for the logged-in provider; the endpoints here do not establish that session (that is the
 * responsibility of the EHR-connectivity login flow). When no valid session is present the
 * underlying gateway calls surface the appropriate error.</p>
 *
 * <p>Every endpoint is patient-scoped and guarded by the {@code _rx} security object at the
 * privilege level appropriate to the operation (read for retrieval, write for the consent-override
 * audit).</p>
 *
 * @since 2026-07-02
 */
@Path("/dhdr")
@Component("dhdrService")
public class DHDRService extends AbstractServiceImpl {

  private static final Logger logger = MiscUtils.getLogger();

  private static final String SECURITY_OBJECT = "_rx";

  private static final String ERROR_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm";

  /** DHDR14.01: a warning must direct the user to resolve the issue or receive support. */
  private static final String RETRY_GUIDANCE = "No drug or pharmacy service information was "
      + "retrieved. Retry the search; if the problem persists, contact your EMR support desk.";

  @Autowired
  DemographicDao demographicDao;

  @Autowired
  protected SecurityInfoManager securityInfoManager;

  /**
   * Retrieves a page of the patient's DHDR dispensed-medication history as a FHIR bundle.
   *
   * @param demographicNo int the patient's demographic number
   * @param offset int the result offset (unused by the current gateway paging model, retained for
   *     API compatibility)
   * @param limit int the result limit (see {@code offset})
   * @param searchConfig DHDRSearchConfig optional date bounds and pagination cursor
   * @return Response containing the FHIR bundle JSON returned by the DHDR EHR Service, or a
   *     {@link DHDRErrorTo1} notice when the service cannot be reached (DHDR14.01)
   */
  @POST
  @Path("/searchByDemographicNo2")
  @Produces("application/json")
  @Consumes("application/json")
  public Response searchByDemographicNo2(@QueryParam("demographicNo") int demographicNo,
      @QueryParam("offset") int offset, @QueryParam("limit") int limit,
      DHDRSearchConfig searchConfig) {
    LoggedInInfo loggedInInfo = getLoggedInInfo();
    if (!securityInfoManager.hasPrivilege(loggedInInfo, SECURITY_OBJECT, "r", demographicNo)) {
      throw new AccessDeniedException(SECURITY_OBJECT, "r", demographicNo);
    }
    DHDRManager dhdrManager = new DHDRManager();
    Date startDate = null;
    Date endDate = null;
    String searchId = null;
    Integer pageId = null;
    if (searchConfig != null) {
      startDate = searchConfig.getStartDate();
      endDate = searchConfig.getEndDate();
      searchId = searchConfig.getSearchId();
      try {
        pageId = Integer.parseInt(searchConfig.getPageId());
      } catch (Exception ignored) {
      }
    }
    Demographic demographic = demographicDao.getDemographicById(demographicNo);
    try {
      String bundle = dhdrManager.search2(loggedInInfo, demographic, startDate, endDate, searchId,
          pageId);
      return Response.ok().entity(bundle).build();
    } catch (TokenExpiredException e) {
      return Response.ok().entity(notice(Response.Status.UNAUTHORIZED.getStatusCode(),
          "Your ONE ID session has expired.",
          "Sign in to ONE ID again, then retry the search.")).build();
    } catch (DHDRServiceException e) {
      // The service answered, but not with an OperationOutcome the viewer could render. Its status
      // code is the DHDR14.01 error code, so it is passed through rather than flattened to a 503.
      logger.error("DHDR search failed for demographic " + demographicNo, e);
      return Response.ok().entity(notice(e.getHttpCode(),
          "The DHDR EHR Service reported an error.",
          RETRY_GUIDANCE)).build();
    } catch (Exception e) {
      // Landing here means the service was never reached: the gateway is misconfigured, the network
      // failed, or the service did not respond (DHDR14.01, v3.0 change note (q)). The exception
      // detail is already on the gateway audit row; the user gets a PHI-free notice.
      logger.error("DHDR search failed for demographic " + demographicNo, e);
      return Response.ok().entity(notice(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
          "The DHDR EHR Service could not be reached.", RETRY_GUIDANCE)).build();
    }
  }

  /**
   * Builds the PHI-free notice DHDR14.01 requires: an error code, a description, a severity, and the
   * date and time of the incident.
   *
   * @param httpCode int the error code to show the user
   * @param description String a user-friendly description of what went wrong
   * @param moreInformation String direction on how to resolve the issue or get support
   * @return DHDRErrorTo1 the notice, ready to serialize to the viewer
   */
  private DHDRErrorTo1 notice(int httpCode, String description, String moreInformation) {
    DHDRErrorTo1 error = new DHDRErrorTo1();
    error.setHttpCode(httpCode);
    error.setHttpMessage(description);
    error.setSeverity("error");
    error.setDateTime(DateUtils.format(ERROR_TIMESTAMP_FORMAT, new Date(), null));
    error.setMoreInformation(moreInformation);
    return error;
  }

  /**
   * Maps a FHIR R4 {@code MedicationDispense} resource onto the DHDR viewer transfer object.
   *
   * @param medicationDispense MedicationDispense the FHIR dispense resource
   * @return MedicationDispenseTo1 the populated transfer object
   */
  public MedicationDispenseTo1 translate(MedicationDispense medicationDispense) {
    MedicationDispenseTo1 medicationDispenseTo1 = new MedicationDispenseTo1();
    List<Resource> listRes = medicationDispense.getContained();
    medicationDispenseTo1.setDispenseDate(medicationDispense.getWhenPrepared());
    medicationDispenseTo1.setDispensedQuantity(
        medicationDispense.getQuantity().getValue().toPlainString());
    medicationDispenseTo1.setEstimatedDaysSupply(
        medicationDispense.getDaysSupply().getValue().toPlainString());
    for (Resource resource : listRes) {
      if (resource.getResourceType() == ResourceType.Medication) {
        Medication medication = (Medication) resource;
        if (medication != null && medication.getCode() != null) {
          medicationDispenseTo1.setDrugDosageForm(medication.getForm().getText());
          Extension ext = medication.getExtensionByUrl(
              "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-ext-medication-strength");
          if (ext != null) {
            medicationDispenseTo1.setDispensedDrugStrength(ext.getValue().primitiveValue());
          }
          List<Coding> codings = medication.getCode().getCoding();
          for (Coding coding : codings) {
            if ("http://hl7.org/fhir/NamingSystem/ca-hc-din".equals(coding.getSystem())) {
              medicationDispenseTo1.setBrandName(coding.getDisplay());
            }
            if ("http://ehealthontario.ca/fhir/NamingSystem/ca-drug-gen-name".equals(
                coding.getSystem())) {
              medicationDispenseTo1.setGenericName(coding.getDisplay());
            }
          }
        } else {
          logger.error("was null " + medication);
        }
      } else if (resource.getResourceType() == ResourceType.Organization) {
        Organization organization = (Organization) resource;
        medicationDispenseTo1.setDispensingPharmacy(organization.getName());
        medicationDispenseTo1.setDispensingPharmacyFaxNumber(
            organization.getTelecom().get(1).getValue());
      } else if (resource.getResourceType() == ResourceType.Practitioner) {
        Practitioner practitioner = (Practitioner) resource;
        for (Identifier identifier : practitioner.getIdentifier()) {
          if ("https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-license-physician".equals(
              identifier.getSystem())) {
            for (HumanName humanName : practitioner.getName()) {
              medicationDispenseTo1.setPrescriberLastname(humanName.getFamily());
              medicationDispenseTo1.setPrescriberFirstname(humanName.getGivenAsSingleString());
            }
            medicationDispenseTo1.setPrescriberPhoneNumber(
                practitioner.getTelecom().get(0).getValue());
          }
        }
      } else {
        logger.error("resource.getResourceType() " + resource.getResourceType());
      }
    }
    return medicationDispenseTo1;
  }

  /**
   * Obtains the PCOI (Patient Consent Override Interface) viewlet URL for the patient, used when a
   * consent block must be overridden before dispensed-medication data can be retrieved.
   *
   * @param demographicNo int the patient's demographic number
   * @return Response containing a {@link NotificationTo1} with the viewlet URL and a correlation
   *     token, or an error notification if the consent-management service rejects the request
   * @throws Exception if the gateway call fails
   */
  @GET
  @Path("/getConsentOveride")
  @Produces("application/json")
  public Response getConsentOveride(@QueryParam("demographicNo") int demographicNo)
      throws Exception {
    LoggedInInfo loggedInInfo = getLoggedInInfo();
    if (!securityInfoManager.hasPrivilege(loggedInInfo, SECURITY_OBJECT, "r", demographicNo)) {
      throw new AccessDeniedException(SECURITY_OBJECT, "r", demographicNo);
    }
    try {
      OmdGateway omdGateway = new OmdGateway();
      String uuid = UUID.randomUUID().toString();
      String uniqueToken = Base64.getUrlEncoder().encodeToString(uuid.getBytes());
      String url = omdGateway.getConsentViewletURL(loggedInInfo, demographicNo,
          "http://ehealthontario.ca/fhir/StructureDefinition/ca-on-medications-profile-MedicationDispense",
          uniqueToken);
      NotificationTo1 notif = new NotificationTo1();
      notif.setReferenceURL(url);
      notif.setUuid(uniqueToken);
      return Response.ok().entity(notif).build();
    } catch (CMSException e) {
      NotificationTo1 notif = new NotificationTo1();
      notif.setSummary(e.getMessage());
      return Response.status(268).entity(notif).build();
    }
  }

  /**
   * Records a PCOI consent-override event against the patient in the gateway transaction log.
   *
   * @param demographicNo Integer the patient's demographic number
   * @param uniqueToken String the correlation token issued by {@link #getConsentOveride(int)}
   * @param status String the consent-override outcome status
   * @param message String the raw JSON payload describing the consent-override event
   * @return Response indicating the event was logged
   */
  @POST
  @Path("/logConsentOverride/{demographicNo}/{uniqueToken}")
  @Produces("application/json")
  @Consumes("application/json")
  public Response logConsentOverride(
      @PathParam("demographicNo") Integer demographicNo,
      @PathParam("uniqueToken") String uniqueToken,
      @QueryParam("status") String status,
      String message) {
    LoggedInInfo loggedInInfo = getLoggedInInfo();
    if (!securityInfoManager.hasPrivilege(loggedInInfo, SECURITY_OBJECT, "w", demographicNo)) {
      throw new AccessDeniedException(SECURITY_OBJECT, "w", demographicNo);
    }
    OmdGateway omdGateway = new OmdGateway();
    omdGateway.logDataReceived(loggedInInfo, "PCOI", status, message,
        demographicNo, uniqueToken);
    return Response.ok(true).build();
  }

  /**
   * Streams a PDF rendering of one of the DHDR views for the patient.
   *
   * @param demographicNo Integer the patient's demographic number
   * @param view String the view to render: {@code summary}, {@code detail}, or {@code comparative}
   * @param jsonBody String the raw JSON view configuration (row/column selection) supplied by the
   *     viewer; parsed into the view model the print engine consumes
   * @return StreamingOutput that writes the generated PDF
   */
  @POST
  @Path("/{demographicNo}/print/{view}")
  @Produces("application/pdf")
  @Consumes(MediaType.APPLICATION_JSON)
  public StreamingOutput print(@PathParam("demographicNo") Integer demographicNo,
      @PathParam("view") String view, String jsonBody) {
    final Integer demo = demographicNo;
    final LoggedInInfo loggedInInfo = getLoggedInInfo();
    if (!securityInfoManager.hasPrivilege(loggedInInfo, SECURITY_OBJECT, "r", demographicNo)) {
      throw new AccessDeniedException(SECURITY_OBJECT, "r", demographicNo);
    }
    final JSONObject jsonObject;
    try {
      jsonObject = new JSONObject(jsonBody);
    } catch (JSONException e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
    // DHDR15.01: printing takes retrieved DHDR data out of the EMR, so the release is audited here,
    // where the request is authorized - not inside the stream, which may fail after the fact.
    new OmdGateway().logInteraction(loggedInInfo, AuditInfo.DHDR, AuditInfo.PRINT, demographicNo);
    final String printViewType = view;
    return new StreamingOutput() {
      @Override
      public void write(java.io.OutputStream os) throws WebApplicationException {
        try {
          DHDRPrint dhdrPrint = new DHDRPrint();
          if ("summary".equals(printViewType)) {
            dhdrPrint.printSummary(loggedInInfo, demo, os, jsonObject);
          } else if ("detail".equals(printViewType)) {
            dhdrPrint.printDetail(loggedInInfo, demo, os, jsonObject);
          } else if ("comparative".equals(printViewType)) {
            dhdrPrint.printComparative(loggedInInfo, demo, os, jsonObject);
          }
        } catch (Exception e) {
          logger.error("error streaming", e);
        } finally {
          IOUtils.closeQuietly(os);
        }
      }
    };
  }
}
