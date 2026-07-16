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
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DHDRManager extends OmdGateway {

  private final SystemPreferencesDao systemPreferencesDao = SpringUtils.getBean(
      SystemPreferencesDao.class);

  public String search2(LoggedInInfo loggedInInfo, Demographic demographic, Date startDate,
                        Date endDate, String searchId, Integer pageId) throws Exception {

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
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
    WebClient wc = getWebClient(loggedInInfo, dhdrEndpoint);

    wc.query("patient.identifier",
        "https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-patient-hcn|"
            + demographic.getHin());//"5365837912");

    wc.query("patient.birthdate", demographic.getBirthDayAsString());

    // DHDR02.02 (B2 #16): the DHDR search is keyed on HCN (+ optional DOB); patient.gender is an
    // optional param that must not be sent - the EMR's recorded sex can diverge from the provincial
    // registry and would then wrongly narrow the result set.

    wc.query("_count", "1000");

    // DHDR04.02/07.02: most-recent-first, so the first entry of each group is the group head.
    wc.query("_sort", "-whenprepared");

    // DHDR02.05: ge/le, not gt/lt - the search period the viewer advertises is inclusive of both
    // boundary dates. Upper bound defaults to today when no endDate is given.
    if (endDate == null) {
      wc.query("whenprepared", "le" + fmt.format(new Date()));
    } else {
      wc.query("whenprepared", "le" + fmt.format(endDate));
    }
    if (startDate != null) {
      wc.query("whenprepared", "ge" + fmt.format(startDate));
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
