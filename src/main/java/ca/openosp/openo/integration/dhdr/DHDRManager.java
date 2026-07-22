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
package ca.openosp.openo.integration.dhdr;

import ca.openosp.openo.commn.dao.SystemPreferencesDao;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.SystemPreferences;
import ca.openosp.openo.utility.LoggedInInfo;
import ca.openosp.openo.utility.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Date;

public class DHDRManager extends OmdGateway {

  private final SystemPreferencesDao systemPreferencesDao = SpringUtils.getBean(
      SystemPreferencesDao.class);

  /**
   * Runs a DHDR dispense search for one patient.
   *
   * <p>The bounds are {@code yyyy-MM-dd} calendar dates, passed through to the query untouched. They
   * are deliberately not {@link Date}: converting a zone-less calendar date to an instant and back
   * shifts it by a day in any zone behind UTC, which silently drops the dispenses on the end date
   * the clinician asked for.</p>
   *
   * @param startDate String inclusive lower bound as {@code yyyy-MM-dd}, or null for unbounded
   * @param endDate String inclusive upper bound as {@code yyyy-MM-dd}, or null to default to today
   * @return String the FHIR bundle JSON the DHDR EHR Service returned
   */
  public String search2(LoggedInInfo loggedInInfo, Demographic demographic, String startDate,
                        String endDate, String searchId, Integer pageId) throws Exception {

    // Mirror oscarPro's getPreferenceValueByName("oneid.dhdr.endpoint", "/MedicationDispense"):
    // read the configured value, falling back to the default path when the preference is unset.
    // The path is appended to the FHIR_iss base in getWebClient(). (DHDR01.02 follow-up: compose
    // the endpoint from FHIR_iss + path rather than a single preference.)
    SystemPreferences dhdrEndpointPref =
        systemPreferencesDao.findPreferenceByName(SystemPreferences.ONEID_KEYS.dhdr_endpoint);
    String dhdrEndpoint = (dhdrEndpointPref != null
        && dhdrEndpointPref.getValue() != null && !dhdrEndpointPref.getValue().trim().isEmpty())
        ? dhdrEndpointPref.getValue()
        : "/MedicationDispense";
    WebClient wc = getWebClient(loggedInInfo, dhdrEndpoint);

    wc.query("patient.identifier",
        "https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-patient-hcn|"
            + demographic.getHin());//"5365837912");

    // DHDR02.02: date of birth is an optional search parameter, so send it only when it can be
    // rendered as a valid FHIR date. Sending a malformed one invites the service to reject the
    // whole search over a parameter we did not have to supply.
    String birthDate = fhirBirthDate(demographic);
    if (birthDate != null) {
      wc.query("patient.birthdate", birthDate);
    }

    // DHDR02.02 (B2 #16): the DHDR search is keyed on HCN (+ optional DOB); patient.gender is an
    // optional param that must not be sent - the EMR's recorded sex can diverge from the provincial
    // registry and would then wrongly narrow the result set.

    wc.query("_count", "1000");

    // DHDR04.02/07.02: most-recent-first, so the first entry of each group is the group head.
    wc.query("_sort", "-whenprepared");

    // DHDR02.05: ge/le, not gt/lt - the search period the viewer advertises is inclusive of both
    // boundary dates. Upper bound defaults to today when no endDate is given; that default is a
    // real "now", so it is the one date here that is formatted rather than passed through.
    if (endDate == null || endDate.trim().isEmpty()) {
      wc.query("whenprepared", "le" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    } else {
      wc.query("whenprepared", "le" + endDate);
    }
    if (startDate != null && !startDate.trim().isEmpty()) {
      wc.query("whenprepared", "ge" + startDate);
    }
    wc.query("_format", "application/fhir+json");

    if (searchId != null) {
      wc.query("search-id", searchId);
    }

    if (pageId != null) {
      wc.query("page", pageId);
    }

    AuditInfo auditInfo = new AuditInfo(AuditInfo.DHDR, AuditInfo.SEARCH,
        demographic.getDemographicNo());
    Response response2 = doGet(loggedInInfo, wc, auditInfo);
    String body = response2.readEntity(String.class);

    // The FHIR response body carries dispense PHI - it must not be written to the application log
    // (DHDR-07 PHI/audit boundary). Access is recorded via AuditInfo above.

    // DHDR14.01: a failure the service described with an OperationOutcome is handed to the viewer,
    // which renders its issues. A failure it described some other way must not reach the viewer as a
    // bundle - with no entries it would be indistinguishable from a search that found nothing.
    if (response2.getStatus() >= 300 && !describesOperationOutcome(body)) {
      throw new DHDRServiceException(response2.getStatus());
    }

    return body;
  }

  /**
   * Renders a demographic's date of birth as a FHIR {@code date}, or null when it cannot be.
   *
   * <p>{@link Demographic#getBirthDayAsString()} joins the three birth columns with no padding and
   * no null handling, so it can yield {@code 1985-4-05} or {@code null-06-15}. FHIR R4 requires
   * {@code YYYY-MM-DD} with a padded month and day. That method is left alone because its other
   * callers may rely on its current shape; the padding and validation belong here instead.</p>
   *
   * <p>{@link LocalDate} does nearly all of it: it rejects an out-of-range month or day and an
   * impossible calendar date such as {@code 1985-02-30}, and its {@code toString()} is already
   * {@code yyyy-MM-dd} with both parts padded. The one thing it does not constrain is the year,
   * so that is bounded to FHIR's own range of 1-9999. Outside it {@code toString()} renders
   * {@code 0000-...}, a negative year, or the sign-prefixed {@code +12345-...}, none of which is
   * a FHIR date.</p>
   *
   * @param demographic Demographic the patient whose date of birth is being rendered
   * @return String the date as {@code yyyy-MM-dd}, or null if any part is missing or invalid
   */
  static String fhirBirthDate(Demographic demographic) {
    if (demographic == null) {
      return null;
    }
    try {
      int year = Integer.parseInt(StringUtils.trimToEmpty(demographic.getYearOfBirth()));
      if (year < 1 || year > 9999) {
        return null;
      }
      return LocalDate.of(year,
          Integer.parseInt(StringUtils.trimToEmpty(demographic.getMonthOfBirth())),
          Integer.parseInt(StringUtils.trimToEmpty(demographic.getDateOfBirth()))).toString();
    } catch (NumberFormatException | DateTimeException e) {
      // A missing, non-numeric or impossible part; the caller omits the parameter.
      return null;
    }
  }

  /**
   * Reports whether a response body is, or contains, a FHIR {@code OperationOutcome} the viewer can
   * render.
   *
   * <p>The outcome may be the whole body, or an entry inside a returned searchset {@code Bundle}.
   * Parsed rather than substring-matched, so a body that merely mentions the type (an HTML error
   * page, say) is not handed to the viewer as issues it cannot render.
   *
   * @param body String the raw response body, possibly {@code null}
   * @return boolean {@code true} if the viewer can render the failure from the body itself
   */
  static boolean describesOperationOutcome(String body) {
    if (body == null || body.trim().isEmpty()) {
      return false;
    }
    try {
      JSONObject root = new JSONObject(body);
      if ("OperationOutcome".equals(root.optString("resourceType"))) {
        return true;
      }
      if ("Bundle".equals(root.optString("resourceType"))) {
        JSONArray entries = root.optJSONArray("entry");
        if (entries != null) {
          for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.optJSONObject(i);
            JSONObject resource = entry != null ? entry.optJSONObject("resource") : null;
            if (resource != null
                && "OperationOutcome".equals(resource.optString("resourceType"))) {
              return true;
            }
          }
        }
      }
      return false;
    } catch (JSONException notJson) {
      // A body that will not parse as JSON is not a renderable FHIR OperationOutcome.
      return false;
    }
  }
}
